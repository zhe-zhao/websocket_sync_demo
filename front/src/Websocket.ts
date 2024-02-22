import { applyPatch, Operation } from "fast-json-patch";
import { useEffect, useState } from "react";
import { ContactBook, ContactAction } from "./Contact";

interface Patch {
  type: "Patch";
  ops: Operation[];
}

interface Full {
  type: "Full";
  contactBook: ContactBook;
}

type ServerMessage = Patch | Full;

let websocket: WebSocket | undefined;
let contactBook: ContactBook | undefined;

const setupWebsocket = (onContactBookUpdate: (contactBook: ContactBook) => void) => {
  const loc = window.location;
  const uri = `${loc.protocol === "https:" ? "wss:" : "ws:"}//${loc.host}/contact-book`;
  console.log(`Connecting websocket: ${uri}`);

  const connection = new WebSocket(uri);

  connection.onopen = () => {
    console.log("Websocket Connected");
    websocket = connection;
  };

  // If we receive a close event the backend has gone away, we try reconnecting in a bit of time
  connection.onclose = (reason) => {
    websocket = undefined;

    // https://developer.mozilla.org/en-US/docs/Web/API/CloseEvent
    if (reason.code !== 1000 && reason.code !== 1001) {
      console.error("Websocket connection closed", reason);

      setTimeout(() => {
        setupWebsocket(onContactBookUpdate);
      }, 500);
    }
  };

  connection.onerror = (error) => {
    console.error("Error with websocket", error);
    connection.close();
  };

  connection.onmessage = (message) => {
    const msg = JSON.parse(message.data) as ServerMessage;

    switch (msg.type) {
      case "Patch": {
        if (contactBook !== undefined) {
          let { newDocument: newContactBook } = applyPatch(
            contactBook,
            msg.ops,
            false,
            false
          );

          onContactBookUpdate(newContactBook);
          contactBook = newContactBook;
        }
        break;
      }
      case "Full": {
        onContactBookUpdate(msg.contactBook);
        contactBook = msg.contactBook;
        break;
      }
    }
  };
};

export const useWebsocket = () => {
  let [contactBook, updateContactBook] = useState<ContactBook>();

  useEffect(() => {
    // Update our app state when changes are received
    setupWebsocket((msg) => {
      updateContactBook(msg);
    });
    // If the destructor runs, clean up the websocket
    return () => {
      if (websocket) {
        websocket.close(1000);
      }
    };
    // The empty `[]` dependency list makes this `useEffect` callback execute only once on construction
  }, []);

  return contactBook;
};

export const sendAction = (action: ContactAction): void => {
  if (websocket) {
    websocket.send(JSON.stringify(action));
  }
};

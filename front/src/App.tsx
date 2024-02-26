import "./App.scss";

import {ContactBook, ContactRow} from "./Contact";
import {sendAction, useWebsocket} from "./Websocket";
import {Fragment, useState} from "react";


function App() {
    let [userId, setId] = useState("")

    return (
        <Fragment>
            {userId === "" &&
                <Fragment>
                    <strong>Select a user: </strong>
                    <select id="users"
                            name="users"
                            defaultValue={""}
                            onChange={ev => setId(ev.currentTarget.value)}
                    >
                        <option value="---">---</option>
                        <option value="dummy_user_id_1">Dummy User 1</option>
                        <option value="dummy_user_id_2">Dummy User 2</option>
                    </select>
                </Fragment>
            }

            {userId != "" && <MainPage userId={userId}/>}
        </Fragment>
    )
}

function MainPage({userId}: { userId: String }) {
    const contactBook = useWebsocket(userId);

    return (
        <div className="container grid-lg">
            <div className="columns">
                <div className="column col-lg-12 contact-book-title">
                    <h1>Contact Book App Example</h1>
                    <h4>User ID: {userId}</h4>
                </div>
            </div>
            {contactBook === undefined && <div className="loading loading-lg"></div>}
            {contactBook && <ContactBookComponent contactBook={contactBook}/>}
        </div>
    );
}

function ContactBookComponent({contactBook}: { contactBook: ContactBook }) {
    return (
        <>
            <div className="form-horizontal">
                <div className="form-group">
                    <div className="col-2 col-lg-12">
                        <label className="form-label" htmlFor="name">
                            <strong>Contact Book Name</strong>
                        </label>
                    </div>
                    <div className="col-10 col-lg-12">
                        <input
                            className="form-input"
                            type="text"
                            id="name"
                            placeholder="LP Contact Book"
                            value={contactBook.name}
                            onChange={(ev) => {
                                sendAction({
                                    type: "ChangeName",
                                    name: ev.currentTarget.value,
                                });
                            }}
                        />
                    </div>
                </div>
            </div>
            {Object.entries(contactBook.contacts).map(([index, row]) => (
                <ContactRowComponent key={index} row={row} index={+index}/>
            ))}
            <div className="columns">
                <div className="column col-lg-12 contact-book-buttons">
                    <div className="btn-group">
                        {Object.entries(contactBook.contacts).some(([, val]) => val.nonEngage) && (
                            <button
                                className="btn"
                                onClick={() => {
                                    sendAction({
                                        type: "RemoveCompleted",
                                    });
                                }}
                            >
                                Remove Non-engaged
                            </button>
                        )}
                        <button
                            className="btn btn-primary"
                            onClick={() => {
                                sendAction({
                                    type: "Add",
                                    row: {
                                        name: "",
                                        nonEngage: false,
                                    },
                                });
                            }}
                        >
                            Add Contact
                        </button>
                    </div>
                </div>
            </div>
        </>
    );
}

function ContactRowComponent({row, index}: { row: ContactRow; index: number }) {
    return (
        <div className="columns">
            <div className="column col-lg-12 contact-book-row">
                <div className="input-group">
                    <label className="form-checkbox">
                        <input
                            type="checkbox"
                            checked={row.nonEngage}
                            onChange={() =>
                                sendAction({
                                    type: "Update",
                                    row: {
                                        ...row,
                                        nonEngage: !row.nonEngage,
                                    },
                                    index,
                                })
                            }
                        />
                        <i className="form-icon"></i>
                    </label>
                    <input
                        className={`form-input ${row.nonEngage ? "non-engage" : ""}`}
                        value={row.name}
                        type="text"
                        placeholder="Add Contact Here!"
                        onChange={(ev) =>
                            sendAction({
                                type: "Update",
                                row: {
                                    ...row,
                                    name: ev.currentTarget.value,
                                },
                                index,
                            })
                        }
                    />

                    <button
                        className="btn input-group-btn"
                        onClick={() =>
                            sendAction({
                                type: "Remove",
                                index,
                            })
                        }
                    >
                        <i className="icon icon-cross"></i>
                    </button>
                </div>
            </div>
        </div>
    );
}

export default App;

export interface ContactBook {
  name: string;
  contacts: { [index: number]: ContactRow };
}

export interface ContactRow {
  name: string;
  nonEngage: boolean;
}

export interface Add {
  type: "Add";
  row: ContactRow;
}

export interface ChangeName {
  type: "ChangeName";
  name: string;
}

export interface Update {
  type: "Update";
  row: ContactRow;
  index: number;
}

export interface Remove {
  type: "Remove";
  index: number;
}

export interface RemoveCompleted {
  type: "RemoveCompleted";
}

export type ContactAction = Add | ChangeName | Update | Remove | RemoveCompleted;

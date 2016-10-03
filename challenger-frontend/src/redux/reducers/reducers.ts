import currentSelection from "./currentSelection";
import users from "./users.ts";
import tasks from "./tasks.ts";
import challenges from "./challenges.ts";
import {combineReducers} from "redux";

export const rootReducer = combineReducers({
    currentSelection,
    challenges,
    tasks,
    users
})






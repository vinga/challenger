import {combineReducers} from "redux";
import {currentSelection} from "./reducers/currentSelection";
import challenges from "../module_challenges/challengeReducers";
import {tasksState} from "../module_tasks/taskReducers";
import {accounts, registerState} from "../module_accounts/accountReducers";


export const rootReducer = combineReducers({
    currentSelection,
    registerState,
    challenges,
    tasksState,
    accounts
});






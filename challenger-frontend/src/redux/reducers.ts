import {combineReducers} from "redux";
import {currentSelection} from "./reducers/currentSelection";
import {challenges} from "../module_challenges/challengeReducers";
import {tasksState} from "../module_tasks/taskReducers";
import {eventsState} from "../module_events/eventReducers";
import {accounts, registerState, confirmationLinkState} from "../module_accounts/accountReducers";
import {reportsState} from "../module_reports/reportReducers";


export const rootReducer = combineReducers({
    currentSelection,
    registerState,
    challenges,
    tasksState,
    eventsState,
    accounts,
    reportsState,
    confirmationLinkState
});






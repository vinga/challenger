import * as taskActions from "../actions/tasks";
import ajaxWrapper from "../../logic/AjaxWrapper";


export default function tasks(state = [], action) {
    switch (action.type) {
        case taskActions.LOAD_TASKS_RESPONSE:
            var newState = {
                ...state,
                ["" + action.challengeId + "-" + action.day.toISOString().slice(0, 10)]: {
                    ...action,
                    lastUpdated: Date.now()
                }
            };
            console.log("TASKS LOADED!!!");
            //console.log(newState);
            return newState;

        default:
            return state
    }
}

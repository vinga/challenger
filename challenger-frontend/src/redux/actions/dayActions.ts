


import {INCREMENT_DAY} from "./actions";
import {ReduxState} from "../ReduxState";
import {fetchTasksWhenNeeded} from "../../module_tasks/taskActions";
export function incrementDayAction(amount:number) {
    return function(dispatch, getState: ()=>ReduxState) {
        dispatch(INCREMENT_DAY.new({amount}));
        dispatch(fetchTasksWhenNeeded(getState().challenges.selectedChallengeId, getState().currentSelection.day));
    }
}




import {INCREMENT_DAY} from "./actions";
import {fetchTasksWhenNeeded} from "./taskActions";
import {ReduxState} from "../ReduxState";
export function incrementDayAction(amount:number) {
    return function(dispatch, getState: ()=>ReduxState) {
        dispatch(INCREMENT_DAY.new({amount}));
        dispatch(fetchTasksWhenNeeded(getState().challenges.selectedChallengeId, getState().currentSelection.day));
    }
}

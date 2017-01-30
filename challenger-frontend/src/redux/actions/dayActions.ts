import {INCREMENT_DAY, DISPLAY_LONG_CALL, HIDE_LONG_CALL} from "./actions";
import {ReduxState, LongCallVisible} from "../ReduxState";
import {fetchTasksProgressesWhenNeeded} from "../../module_tasks/taskActions";
export function incrementDayAction(amount: number) {
    return function (dispatch, getState: ()=>ReduxState) {
        dispatch(INCREMENT_DAY.new({amount}));

        dispatch(fetchTasksProgressesWhenNeeded(getState().challenges.selectedChallengeId, getState().currentSelection.day));
    }
}

/**
 * async calls are not showed at all
 * other calls are by default showed only for LONG and VERY_LONG
 * showing FROM_START must be set explicitly in WebCallData
 * @returns {(dispatch:any, getState:()=>ReduxState)=>undefined}
 */
export function checkForTooLongWebCallsOneTime() {
    return function (dispatch, getState: ()=>ReduxState) {
        var d = new Date();
        var SECONDS_10 = 1000*5;
        var SECONDS_2=1000*2;
        //getState().webCallsState.forEach(e=>console.log(d.getTime() - e.startDate.getTime()));


        //console.log("check for too long web calls "+getState().webCallsState.length);
        if (getState().webCallsState.some(e=>(d.getTime() - e.startDate.getTime()) > SECONDS_10)) {
            dispatch(DISPLAY_LONG_CALL.new({longCallVisible: LongCallVisible.VERY_LONG}));
        } else if (getState().webCallsState.some(e=>(d.getTime() - e.startDate.getTime()) > SECONDS_2)) {
            dispatch(DISPLAY_LONG_CALL.new({longCallVisible: LongCallVisible.LONG}));
        } else  if (getState().webCallsState.some(e=>e.fromStart && (d.getTime() - e.startDate.getTime()) > 700)) {
            dispatch(DISPLAY_LONG_CALL.new({longCallVisible: LongCallVisible.FROM_START}));
        } else {
            dispatch(HIDE_LONG_CALL.new({}));
        }
    }
}

export function checkForTooLongWebCalls() {
    return function (dispatch, getState: ()=>ReduxState) {
        dispatch(checkForTooLongWebCallsOneTime());
        setTimeout(() => {
            dispatch(checkForTooLongWebCalls())
        }, 1000) // everySec

    }
}
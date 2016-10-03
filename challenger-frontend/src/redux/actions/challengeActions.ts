
import {CHANGE_CHALLENGE, WEB_CHALLENGES_REQUEST, WEB_CHALLENGES_RESPONSE} from "./actions";
import {fetchTasksWhenNeeded, fetchTasks} from "./taskActions";
import ajaxWrapper from "../../logic/AjaxWrapper";
import {ReduxState} from "../ReduxState";


export function changeChallengeAction(challengeId: number) {
    return function (dispatch, getState) {
        dispatch(CHANGE_CHALLENGE.new({challengeId}));
        dispatch(fetchTasksWhenNeeded(challengeId,getState().currentSelection.day));
    };
}
export function fetchWebChallenges() {
    return function (dispatch, getState: ()=>ReduxState) {
        dispatch(WEB_CHALLENGES_REQUEST.new({}));
        ajaxWrapper.loadVisibleChallenges(
            visibleChallengesDTO=> {
                var loadTasks= (getState().challenges.selectedChallengeId!=visibleChallengesDTO.selectedChallengeId);
                dispatch(WEB_CHALLENGES_RESPONSE.new(visibleChallengesDTO));
                if (loadTasks) {
                    dispatch(fetchTasks(visibleChallengesDTO.selectedChallengeId,getState().currentSelection.day));
                }

            }
        )
    }
};
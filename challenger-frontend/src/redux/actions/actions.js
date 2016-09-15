import {fetchTasks, fetchTasksWhenNeeded} from "./tasks";

export const INCREMENT_DAY = 'INCREMENT_DAY'



export function incrementDay(amount) {
    return { type: INCREMENT_DAY, amount: amount }
}




export const CHANGE_CHALLENGE = 'CHANGE_CHALLENGE'
export const WEB_CHALLENGES_REQUEST='WEB_CHALLENGES_REQUEST';
export const WEB_CHALLENGES_RESPONSE='WEB_CHALLENGES_RESPONSE'




export function incrementDayAction(amount) {
    return function(dispatch, getState) {
        dispatch(incrementDay(amount));
        dispatch(fetchTasksWhenNeeded(getState().visibleChallengesDTO.selectedChallengeId, getState().mainReducer.day));
    }
}

export function webChallengesRequest() {
    return {type: WEB_CHALLENGES_REQUEST}
}
export function webChallengesResponse(visibleChallengesDTO) {
    return { type: WEB_CHALLENGES_RESPONSE, visibleChallengesDTO: visibleChallengesDTO }
}
function changeChallenge(challengeId) {
    return { type: CHANGE_CHALLENGE, challengeId: challengeId }
}

export function changeChallengeAction(challengeId) {
    return function (dispatch, getState) {
        dispatch(changeChallenge(challengeId));
        dispatch(fetchTasksWhenNeeded(challengeId,getState().mainReducer.day));
    };
}




import ajaxWrapper from "../../logic/AjaxWrapper";
export function fetchWebChallenges() {
    return function (dispatch, getState) {
        dispatch(webChallengesRequest());
        ajaxWrapper.loadVisibleChallenges(
            visibleChallengesDTO=> {
                var loadTasks= (getState().visibleChallengesDTO.selectedChallengeId!=visibleChallengesDTO.selectedChallengeId);

                dispatch(webChallengesResponse(visibleChallengesDTO));

                if (loadTasks) {
                    dispatch(fetchTasks(visibleChallengesDTO.selectedChallengeId,getState().mainReducer.day));
                }

            }
        )
    }
};



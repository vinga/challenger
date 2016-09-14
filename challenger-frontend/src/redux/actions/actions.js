/*
 * action types
 */

export const INCREMENT_DAY = 'INCREMENT_DAY'



export function incrementDay(amount) {
    return { type: INCREMENT_DAY, amount: amount }
}




export const CHANGE_CHALLENGE = 'CHANGE_CHALLENGE'
export const WEB_CHALLENGES_REQUEST='WEB_CHALLENGES_REQUEST';
export const WEB_CHALLENGES_RESPONSE='WEB_CHALLENGES_RESPONSE'

export const LOAD_TASKS_REQUEST='LOAD_TASKS_REQUEST';
export const LOAD_TASKS_RESPONSE='LOAD_TASKS_RESPONSE';




export function webChallengesRequest() {
    return {type: WEB_CHALLENGES_REQUEST}
}
export function webChallengesResponse(visibleChallengesDTO) {
    return { type: WEB_CHALLENGES_RESPONSE, visibleChallengesDTO: visibleChallengesDTO }
}
export function changeChallenge(challengeId) {
    return { type: CHANGE_CHALLENGE, challengeId: challengeId }
}

export function loadTasksRequest(challengeId, userNo, day) {
    return { type: LOAD_TASKS_REQUEST, challengeId: challengeId, day: day, userNo: userNo }
}
export function loadTasksResponse(taskList, challengeId, userNo , day) {
    return { type: LOAD_TASKS_RESPONSE, taskList: taskList, challengeId: challengeId, day: day, userNo: userNo }
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
                    dispatch(fetchTasks(visibleChallengesDTO.selectedChallengeId, 0, getState().mainReducer.day));
                    dispatch(fetchTasks(visibleChallengesDTO.selectedChallengeId, 1, getState().mainReducer.day));
                }

            }
        )
    }
};

export function fetchTasks(challengeId, userNo, day) {
    return function (dispatch) {
        dispatch(loadTasksRequest(challengeId, userNo, day));
        ajaxWrapper.loadTasksFromServer(challengeId, userNo, day,
            taskList=> {
                dispatch(loadTasksResponse(taskList, challengeId, userNo, day))
            }
        )
    }
};

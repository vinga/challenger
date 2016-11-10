import {CHANGE_CHALLENGE, WEB_CHALLENGES_REQUEST, WEB_CHALLENGES_RESPONSE} from "./challengeActionTypes";
import {ReduxState} from "../redux/ReduxState";
import * as webCall from "./challengeWebCalls";
import {fetchTasksWhenNeeded, fetchTasks} from "../module_tasks/index";
import {fetchInitialEvents} from "../module_events/index";
import {loadEventsAsyncAllTheTime} from "../module_events/eventActions";
import {authPromiseErr} from "../module_accounts/accountActions";
import {ChallengeDTO} from "./ChallengeDTO";


export function changeChallengeAction(challengeId: number) {
    return function (dispatch, getState) {
        dispatch(CHANGE_CHALLENGE.new({challengeId}));
        dispatch(fetchTasksWhenNeeded(challengeId, getState().currentSelection.day));
        dispatch(fetchInitialEvents(challengeId));
        dispatch(loadEventsAsyncAllTheTime());
    };
}

export function fetchWebChallenges() {
    return function (dispatch, getState: ()=>ReduxState) {
        dispatch(WEB_CHALLENGES_REQUEST.new({}));
        webCall.loadVisibleChallenges().then(
            visibleChallengesDTO=> {
                var initialLoad = (getState().challenges.selectedChallengeId != visibleChallengesDTO.selectedChallengeId);
                dispatch(WEB_CHALLENGES_RESPONSE.new(visibleChallengesDTO));
                if (initialLoad)
                    dispatch(changeChallengeAction(visibleChallengesDTO.selectedChallengeId))
            }
        ).catch((reason)=>authPromiseErr(reason,dispatch));
    }
};

export function updateChallenge(challenge: ChallengeDTO) {
    return function (dispatch, getState: ()=>ReduxState) {
        /*dispatch(MODIFY_TASK_OPTIMISTIC.new(task));
        dispatch(MODIFY_TASK_REQUEST.new(task));
        if (task.id <= 0) {
            webCall.createTask(task)
                .then((task: TaskDTO)=> {
                    dispatch(fetchTasks(getState().challenges.selectedChallengeId, getState().currentSelection.day));
                }).catch((reason)=>authPromiseErr(reason,dispatch));
        } else {
            webCall.updateTask(task)
                .then((task: TaskDTO)=> {
                    dispatch(fetchTasks(getState().challenges.selectedChallengeId, getState().currentSelection.day));
                }).catch((reason)=>authPromiseErr(reason,dispatch));
        }
        dispatch(displayInProgressWebRequestsIfAny());*/
    }
}





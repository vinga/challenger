import {CHANGE_CHALLENGE, WEB_CHALLENGES_REQUEST, WEB_CHALLENGES_RESPONSE} from "./challengeActionTypes";
import {ReduxState} from "../redux/ReduxState";
import * as webCall from "./challengeWebCalls";
import {fetchTasksWhenNeeded, fetchTasks} from "../module_tasks/index";
import {fetchInitialEvents} from "../module_events/index";
import {loadEventsAsyncAllTheTime} from "../module_events/eventActions";


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
        )
    }
};





import {CHANGE_CHALLENGE, WEB_CHALLENGES_REQUEST, WEB_CHALLENGES_RESPONSE} from "./challengeActionTypes";
import {ReduxState} from "../redux/ReduxState";
import * as webCall from "./challengeWebCalls";
import {fetchTasksWhenNeeded} from "../module_tasks/index";
import {fetchInitialEvents} from "../module_events/index";
import {loadEventsAsyncAllTheTime} from "../module_events/eventActions";
import {ChallengeDTO, ChallengeStatus} from "./ChallengeDTO";
import {downloadProgressiveReports} from "../module_reports/index";
import {challengeStatusSelector} from "./challengeSelectors";


var loading: boolean=false;
export function changeChallengeAction(challengeId: number) {
    return function (dispatch, getState) {


        dispatch(CHANGE_CHALLENGE.new({challengeId}));
        if (challengeStatusSelector(getState()) == ChallengeStatus.ACTIVE) {
            dispatch(fetchTasksWhenNeeded(challengeId, getState().currentSelection.day));
            dispatch(fetchInitialEvents(challengeId));
            if (!loading) {
                loading=true;
                dispatch(loadEventsAsyncAllTheTime());
            }
            dispatch(downloadProgressiveReports(challengeId));
        }
    };
}

export function fetchWebChallenges() {
    return function (dispatch, getState: ()=>ReduxState) {
        dispatch(WEB_CHALLENGES_REQUEST.new({}));
        webCall.loadVisibleChallenges(dispatch).then(
            visibleChallengesDTO=> {
                var initialLoad = (getState().challenges.selectedChallengeId != visibleChallengesDTO.selectedChallengeId);
                dispatch(WEB_CHALLENGES_RESPONSE.new(visibleChallengesDTO));
                if (initialLoad && visibleChallengesDTO.selectedChallengeId != null)
                    dispatch(changeChallengeAction(visibleChallengesDTO.selectedChallengeId))
            }
        );
    }
}

// we want just refesh state, like sb who accepted or rejected
export function fetchWebChallengesNoReload() {
    console.log("fetchWebChallengesNoReload");
    return function (dispatch, getState: ()=>ReduxState) {
        dispatch(WEB_CHALLENGES_REQUEST.new({}));
        webCall.loadVisibleChallenges(dispatch).then(
            visibleChallengesDTO=> {

                dispatch(WEB_CHALLENGES_RESPONSE.new(visibleChallengesDTO));

            }
        );
    }
}

export function createChallengeAction(challenge: ChallengeDTO) {
    return function (dispatch, getState: ()=>ReduxState) {
        challenge.userLabels = getState().challenges.editedChallenge.userLabels
        webCall.createChallenge(dispatch, challenge).then(
            ()=> {
                dispatch(fetchWebChallenges());
            }
        );
    }
}

export function updateChallengeAction(challenge: ChallengeDTO) {
    return function (dispatch, getState: ()=>ReduxState) {
        challenge.userLabels = getState().challenges.editedChallenge.userLabels
        webCall.updateChallenge(dispatch, challenge).then(
            ()=> {
                dispatch(fetchWebChallenges());
            }
        );
    }
}

export function deleteChallengeAction(challengeId: number) {
    return function (dispatch, getState: ()=>ReduxState) {

        webCall.deleteChallenge(dispatch, challengeId).then(
            ()=> {
                dispatch(fetchWebChallenges());
            }
        );
    }
}


export function acceptOrRejectChallenge(challengeId: number, accept: boolean) {
    return function (dispatch, getState: ()=>ReduxState) {
        /*var challenge = getState().challenges.visibleChallenges.find(ch => (ch.id == challengeId));
         if(!accept)
         challenge.challengeStatus = ChallengeStatus.REFUSED;*/

        webCall.acceptOrRejectChallenge(dispatch, challengeId, accept).then(
            ()=> {
                dispatch(fetchWebChallenges());
            }
        );
    }
}








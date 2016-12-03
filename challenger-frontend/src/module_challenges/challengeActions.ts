import {CHANGE_CHALLENGE, WEB_CHALLENGES_REQUEST, WEB_CHALLENGES_RESPONSE} from "./challengeActionTypes";
import {ReduxState} from "../redux/ReduxState";
import * as webCall from "./challengeWebCalls";
import {fetchTasksWhenNeeded} from "../module_tasks/index";
import {fetchInitialEvents} from "../module_events/index";
import {loadEventsAsyncAllTheTime} from "../module_events/eventActions";
import {authPromiseErr} from "../module_accounts/accountActions";
import {ChallengeDTO, ChallengeStatus} from "./ChallengeDTO";
import {downloadProgressiveReports} from "../module_reports/index";


export function changeChallengeAction(challengeId: number) {
    return function (dispatch, getState) {
        dispatch(CHANGE_CHALLENGE.new({challengeId}));
        dispatch(fetchTasksWhenNeeded(challengeId, getState().currentSelection.day));
        dispatch(fetchInitialEvents(challengeId));
        dispatch(loadEventsAsyncAllTheTime());
        dispatch(downloadProgressiveReports(challengeId));
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
        ).catch((reason)=>authPromiseErr(reason, dispatch));
    }
};

export function createChallenge(challenge: ChallengeDTO) {
    return function (dispatch, getState: ()=>ReduxState) {
        challenge.userLabels=getState().challenges.editedChallenge.userLabels
        webCall.createChallenge(challenge).then(
            ()=> {
                dispatch(fetchWebChallenges());
            }
        ).catch((reason)=>authPromiseErr(reason, dispatch));
    }
}


export function acceptOrRejectChallenge(challengeId: number, accept: boolean) {
    return function (dispatch, getState: ()=>ReduxState) {
        /*var challenge = getState().challenges.visibleChallenges.find(ch => (ch.id == challengeId));
        if(!accept)
            challenge.challengeStatus = ChallengeStatus.REFUSED;*/

        webCall.acceptOrRejectChallenge(challengeId, accept).then(
            ()=> {
                dispatch(fetchWebChallenges());
            }
        ).catch((reason)=>authPromiseErr(reason, dispatch));
    }
}








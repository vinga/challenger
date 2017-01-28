import {CHANGE_CHALLENGE, WEB_CHALLENGES_REQUEST, WEB_CHALLENGES_RESPONSE, SET_NO_CHALLENGES_LOADED_YET, ACCEPT_REJECT_CHALLENGE_OPTIMISTIC} from "./challengeActionTypes";
import {ReduxState} from "../redux/ReduxState";
import * as webCall from "./challengeWebCalls";
import {fetchTasksProgressesWhenNeeded} from "../module_tasks/index";
import {fetchInitialEventsForChallenge} from "../module_events/index";
import {ChallengeDTO, ChallengeStatus} from "./ChallengeDTO";
import {downloadProgressiveReports} from "../module_reports/index";
import {challengeStatusSelector} from "./challengeSelectors";
import {loadEventsAsyncAllTheTimeSingleton} from "../module_events/eventActions";
import {getAccountsSelector, loggedUserSelector} from "../module_accounts/accountSelectors";



export function changeChallengeAction(challengeId: number) {
    return function (dispatch, getState) {


        dispatch(CHANGE_CHALLENGE.new({challengeId}));
        if (challengeStatusSelector(getState()) == ChallengeStatus.ACTIVE) {
            dispatch(fetchTasksProgressesWhenNeeded(challengeId, getState().currentSelection.day));
            dispatch(fetchInitialEventsForChallenge(challengeId));
            dispatch(downloadProgressiveReports(challengeId));
        }
    };
}

export function fetchWebChallenges() {
    return function (dispatch, getState: ()=>ReduxState):Promise<any> {

        dispatch(WEB_CHALLENGES_REQUEST.new({}));
        return webCall.loadVisibleChallenges(dispatch).then(
            visibleChallengesDTO=> {


                var initialLoad = (getState().challenges.selectedChallengeId != visibleChallengesDTO.selectedChallengeId);
                dispatch(WEB_CHALLENGES_RESPONSE.new(visibleChallengesDTO));
                if (initialLoad && visibleChallengesDTO.selectedChallengeId != null)


                    dispatch(changeChallengeAction(visibleChallengesDTO.selectedChallengeId))
                    dispatch(loadEventsAsyncAllTheTimeSingleton());

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
                dispatch(SET_NO_CHALLENGES_LOADED_YET.new({})); // if set, after fetching the selected challenge id will be updated to new one
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
        const loggedUserId = loggedUserSelector(getState()).id;
        dispatch(ACCEPT_REJECT_CHALLENGE_OPTIMISTIC.new({challengeId,accept,loggedUserId}));
        webCall.acceptOrRejectChallenge(dispatch, challengeId, accept).then(
            ()=> {
                dispatch(fetchWebChallenges());
            }
        );
    }
}








import {
    WEB_CHALLENGES_REQUEST,
    CHANGE_CHALLENGE,
    WEB_CHALLENGES_RESPONSE,
    CLOSE_EDIT_CHALLENGE,
    CREATE_NEW_CHALLENGE,
    UPDATE_CHALLENGE_PARTICIPANTS,
    DELETE_CHALLENGE_PARTICIPANT,
    UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION,
    CHECK_CHALLENGE_PARTICIPANTS_REQUEST,
    CHECK_CHALLENGE_PARTICIPANTS_RESPONSE,
    EDIT_CHALLENGE,
    SET_NO_CHALLENGES_LOADED_YET,
    ACCEPT_REJECT_CHALLENGE_OPTIMISTIC
} from "./challengeActionTypes";
import {isAction} from "../redux/ReduxTask";
import {VisibleChallengesDTO, ChallengeStatus, NO_CHALLENGES_LOADED_YET, ChallengeDTO, ChallengeParticipantDTO} from "./ChallengeDTO";
import _ = require("lodash");
import path = require("immutable-path");


const initial = (): VisibleChallengesDTO => {
    return {
        selectedChallengeId: NO_CHALLENGES_LOADED_YET,
        visibleChallenges: [],
        editedChallenge: undefined,
        errorText: null
    }
}

export function challenges(state: VisibleChallengesDTO = initial(), action): VisibleChallengesDTO {
    if (isAction(action, 'LOGOUT')) {
        return initial();
    } else if (isAction(action, ACCEPT_REJECT_CHALLENGE_OPTIMISTIC)) {

        const challengeStatus = action.accept ? ChallengeStatus.ACTIVE : ChallengeStatus.REFUSED;
        return path.map(state, `visibleChallenges[id=${action.challengeId}].userLabels[id=${action.loggedUserId}]`, (userLabel) =>
            <ChallengeParticipantDTO>{... userLabel, challengeStatus}
        );
        /*
         const challenge = state.visibleChallenges.find(ch => ch.id == action.challengeId);
         const newUserLabels = challenge.userLabels.map(ul => {
         if (ul.id == action.loggedUserId) {
         return {... ul, challengeStatus: action.accept ? ChallengeStatus.ACTIVE : ChallengeStatus.REFUSED}
         } else {
         return ul;
         }
         });

         const visibleChallenges = state.visibleChallenges.map(
         ch => {
         if (ch.id == challenge.id) {
         return {... ch, userLabels: newUserLabels}
         } else {
         return ch;
         }
         }
         );
         return {... state, visibleChallenges}*/
    } else if (isAction(action, WEB_CHALLENGES_REQUEST)) {
        return state;
    } else if (isAction(action, CHANGE_CHALLENGE)) {
        return {... state, selectedChallengeId: action.challengeId};
    } else if (isAction(action, SET_NO_CHALLENGES_LOADED_YET)) {
        return {...state, selectedChallengeId: NO_CHALLENGES_LOADED_YET};
    }
    else if (isAction(action, WEB_CHALLENGES_RESPONSE)) {

        var newState: VisibleChallengesDTO = action;
        if (state.selectedChallengeId != null && state.selectedChallengeId != NO_CHALLENGES_LOADED_YET && newState.visibleChallenges.some(vc => vc.id == state.selectedChallengeId)) {
            newState.selectedChallengeId = state.selectedChallengeId;
        }
        newState.visibleChallenges.map(vc => {
            var ord = 0;
            vc.userLabels.forEach(ul => {
                ul.ordinal = ord++;
            })
        });
        return newState;
    } else if (isAction(action, CLOSE_EDIT_CHALLENGE)) {
        return {...state, editedChallenge: null}
    } else if (isAction(action, CREATE_NEW_CHALLENGE)) {
        return {
            ...state,
            editedChallenge: {
                id: 0,
                label: "New challenge",
                challengeStatus: ChallengeStatus.ACTIVE,
                creatorId: 0,
                myId: 0,
                userLabels: [{id: 0, label: action.creatorLabel, ordinal: 0, challengeStatus: ChallengeStatus.ACTIVE}]
            } as ChallengeDTO
        }

    } else if (isAction(action, UPDATE_CHALLENGE_PARTICIPANTS)) {
        if (state.editedChallenge.userLabels.some(chp => chp.label == action.loginOrEmail))
            return state;

        var participant = {
            id: 0,
            label: action.loginOrEmail,
            ordinal: 0,
            challengeStatus: ChallengeStatus.WAITING_FOR_ACCEPTANCE
        }

        return {
            ...state,
            editedChallenge: {
                ... state.editedChallenge,
                userLabels: state.editedChallenge.userLabels.concat(participant)
            }
        }
    } else if (isAction(action, DELETE_CHALLENGE_PARTICIPANT)) {
        return {
            ...state,
            editedChallenge: {
                ...state.editedChallenge,
                userLabels: state.editedChallenge.userLabels.filter(participant => participant.label != action.label)
            }
        };
    } else if (isAction(action, UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION)) {
        return {
            ...state,
            errorText: action.errorText
        };
    } else if (isAction(action, CHECK_CHALLENGE_PARTICIPANTS_REQUEST)) {
        return {
            ...state,
            challengeParticipantIsChecked: true
        };
    } else if (isAction(action, CHECK_CHALLENGE_PARTICIPANTS_RESPONSE)) {
        return {
            ...state,
            challengeParticipantIsChecked: false
        };
    } else if (isAction(action, EDIT_CHALLENGE)) {
        return {
            ...state,
            editedChallenge: state.visibleChallenges.find(
                it => it.id == action.challengeId
            )
        }
    }
    return state
}

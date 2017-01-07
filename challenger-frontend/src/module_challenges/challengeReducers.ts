import {
    WEB_CHALLENGES_REQUEST,
    CHANGE_CHALLENGE,
    WEB_CHALLENGES_RESPONSE,
    CLOSE_EDIT_CHALLENGE,
    CREATE_NEW_CHALLENGE,
    UPDATE_CHALLENGE_PARTICIPANTS,
    DELETE_CHALLENGE_PARTICIPANT,
    UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION, CHECK_CHALLENGE_PARTICIPANTS_REQUEST, CHECK_CHALLENGE_PARTICIPANTS_RESPONSE, EDIT_CHALLENGE
} from "./challengeActionTypes";
import {isAction} from "../redux/ReduxTask";
import {VisibleChallengesDTO, ChallengeStatus, NO_CHALLENGES_LOADED_YET} from "./ChallengeDTO";
import {copy} from "../redux/ReduxState";
import _ = require("lodash");
import {loadVisibleChallenges} from "./challengeWebCalls";


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
    } else if (isAction(action, WEB_CHALLENGES_REQUEST)) {
        return state;
    } else if (isAction(action, CHANGE_CHALLENGE)) {
        console.log("change challenge to " + action.challengeId);
        return copy(state).and({selectedChallengeId: action.challengeId});
    } else if (isAction(action, WEB_CHALLENGES_RESPONSE)) {

        var newState:VisibleChallengesDTO=action;
        if (state.selectedChallengeId!=null && state.selectedChallengeId!=NO_CHALLENGES_LOADED_YET && newState.visibleChallenges.some(vc=>vc.id==state.selectedChallengeId)) {
            newState.selectedChallengeId=state.selectedChallengeId;
        }
        newState.visibleChallenges.map(vc=> {
            var ord = 0;
            vc.userLabels.forEach(ul=> {
                ul.ordinal = ord++;
            })
        });
        return newState;
    } else if (isAction(action, CLOSE_EDIT_CHALLENGE)) {
        return Object.assign({}, state, {
            editedChallenge: null,
        })
    } else if (isAction(action, CREATE_NEW_CHALLENGE)) {
        return Object.assign({}, state, {
            editedChallenge: {
                id: 0,
                label: "New challenge",
                challengeStatus: ChallengeStatus.ACTIVE,
                creatorId: 0,
                myId: 0,
                userLabels: [{id: 0, label: action.creatorLabel}]
            }
        })
    } else if (isAction(action, UPDATE_CHALLENGE_PARTICIPANTS)) {
        if (state.editedChallenge.userLabels.some(chp=>chp.label == action.loginOrEmail))
            return state;

        var participant = {
            id: 0,
            label: action.loginOrEmail,
            ordinal: 0,
            challengeStatus: ChallengeStatus.WAITING_FOR_ACCEPTANCE
        }

        var editedChCopy = Object.assign({}, state.editedChallenge, {
            userLabels: state.editedChallenge.userLabels.concat(participant)
        })

        return Object.assign({}, state, {
            editedChallenge: editedChCopy
        })
    } else if (isAction(action, DELETE_CHALLENGE_PARTICIPANT)) {
        var arr = state.editedChallenge.userLabels.filter(participant => participant.label != action.label)

        var editedChCopy = Object.assign({}, state.editedChallenge, {
            userLabels: arr
        })

        return Object.assign({}, state, {
            editedChallenge: editedChCopy
        })
    } else if (isAction(action, UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION)) {
        return Object.assign({}, state, {
            errorText: action.errorText
        })
    } else if (isAction(action, CHECK_CHALLENGE_PARTICIPANTS_REQUEST)) {
        return Object.assign({}, state, {
            challengeParticipantIsChecked: true
        })
    } else if (isAction(action, CHECK_CHALLENGE_PARTICIPANTS_RESPONSE)) {
        return Object.assign({}, state, {
            challengeParticipantIsChecked: false
        })
    } else if (isAction(action, EDIT_CHALLENGE)) {
        var selectedChallenge = state.visibleChallenges.find(
            it => it.id == action.challengeId
        )
        return Object.assign({}, state, {
            editedChallenge: selectedChallenge
        })
    }
    return state
}

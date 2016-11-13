import {
    WEB_CHALLENGES_REQUEST, CHANGE_CHALLENGE, WEB_CHALLENGES_RESPONSE, CLOSE_EDIT_CHALLENGE, CREATE_NEW_CHALLENGE, UPDATE_CHALLENGE_PARTICIPANTS,
    DELETE_CHALLENGE_PARTICIPANT
} from "./challengeActionTypes";
import {isAction} from "../redux/ReduxTask";
import {VisibleChallengesDTO, ChallengeStatus} from "./ChallengeDTO";
import {copy} from "../redux/ReduxState";


const initial = (): VisibleChallengesDTO => {
    return {
        selectedChallengeId: -1,
        visibleChallenges: [],
        editedChallenge: undefined
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
        state.visibleChallenges.map(vc=> {
            var ord=0;
            vc.userLabels.forEach(ul=> {
                ul.ordinal=ord++;
            })
        });
        return action;
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
                userLabels: []}
        })
    } else if (isAction(action, UPDATE_CHALLENGE_PARTICIPANTS)) {
        if (state.editedChallenge.userLabels.some(chp=>chp.label==action.loginOrEmail))
            return state;

        var participant = {
            id: 0,
            label: action.loginOrEmail,
            ordinal: 0,
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
    }
    return state
}

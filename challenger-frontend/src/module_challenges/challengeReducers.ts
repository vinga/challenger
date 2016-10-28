import {WEB_CHALLENGES_REQUEST, CHANGE_CHALLENGE, WEB_CHALLENGES_RESPONSE} from "./challengeActionTypes";
import {isAction} from "../redux/ReduxTask";
import {VisibleChallengesDTO} from "./ChallengeDTO";
import {copy} from "../redux/ReduxState";


const initial = (): VisibleChallengesDTO => {
    return {
        selectedChallengeId: -1,
        visibleChallenges: [],
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
        return action;
    }
    return state
}

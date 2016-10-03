

import {WEB_CHALLENGES_REQUEST, CHANGE_CHALLENGE, WEB_CHALLENGES_RESPONSE} from "../actions/actions";
import {isAction} from "../ReduxTask";
import {VisibleChallengesDTO} from "../../logic/domain/ChallengeDTO";


export default function challenges(state:VisibleChallengesDTO = {
    selectedChallengeId: -1,
    visibleChallenges: []
},
                              action) {
    if (isAction(action, WEB_CHALLENGES_REQUEST)) {
        return state;
    } else if (isAction(action, CHANGE_CHALLENGE)) {
        console.log("change challenge to " + action.challengeId);
        return Object.assign({}, state, {
            selectedChallengeId: action.challengeId
        })
    } else if (isAction(action, WEB_CHALLENGES_RESPONSE)) {

        //if (state.selectedChallengeId!=action.visibleChallengesDTO.selectedChallengeId)
        //     dispatch(changeChallenge(state.selectedChallengeId));
        return action;
    }
    return state
}

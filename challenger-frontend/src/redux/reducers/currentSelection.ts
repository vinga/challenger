import {isAction} from "../ReduxTask";
import {INCREMENT_DAY} from "../actions/actions";
import {CurrentSelection, copy} from "../ReduxState";
import jwtDecode = require("jwt-decode");


export function currentSelection(state: CurrentSelection = {day: new Date(), showChallengeConversation: true}, action): CurrentSelection {
    if (isAction(action, INCREMENT_DAY)) {
        return copy(state).and({
            day: state.day.addDays(action.amount)
        })
    }
    return state;
}


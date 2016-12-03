import {isAction} from "../ReduxTask";
import {INCREMENT_DAY, INTERNAL_ERROR_WEB_RESPONSE} from "../actions/actions";
import {CurrentSelection, copy} from "../ReduxState";
import {LOGIN_USER_RESPONSE_FAILURE, LOGIN_USER_RESPONSE_SUCCESS} from "../../module_accounts/accountActionTypes";


const getInitialState = () => {
  return  {day: new Date()};
}
export function currentSelection(state: CurrentSelection = getInitialState(), action): CurrentSelection {
    if (isAction(action, 'LOGOUT')) {
        return getInitialState();
    } else if (isAction(action, INCREMENT_DAY)) {
        return copy(state).and({
            day: state.day.addDays(action.amount)
        })
    }
    else if (isAction(action, LOGIN_USER_RESPONSE_FAILURE)) {

        return copy(state).and({
            loginErrorDescription: getErrorDescriptionForLogin(action.status, action.textStatus, action.responseText)
        })
    } else if (isAction(action, LOGIN_USER_RESPONSE_SUCCESS)) {
        return copy(state).and({
            loginErrorDescription: null
        })
    } else if (isAction(action, INTERNAL_ERROR_WEB_RESPONSE)) {
        return copy(state).and({
            loginErrorDescription: "Unexpected error occurred. Please login again"
        })
     }

    return state;
}

const getErrorDescriptionForLogin = (status: number, textStatus, responseText: string) => {
    if (status === 0)
        return "Connection refused"; //('Not connect.\n Verify Network.');
    else if (status == 401) {
        return responseText;
    } else {
        console.log("Error unexpected... " + status + " " + responseText);
        return "Unexpected problem"
    }
};


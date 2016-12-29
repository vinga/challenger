import {isAction} from "../ReduxTask";
import {INCREMENT_DAY, INTERNAL_ERROR_WEB_RESPONSE} from "../actions/actions";
import {CurrentSelection, copy} from "../ReduxState";
import {
    LOGIN_USER_RESPONSE_FAILURE, LOGIN_USER_RESPONSE_SUCCESS, START_FORGOT_PASSWORD_MODE, FINISH_FORGOT_PASSWORD_MODE,
    REGISTER_USER_RESPONSE
} from "../../module_accounts/accountActionTypes";
import {CHECK_CHALLENGE_PARTICIPANTS_REQUEST} from "../../module_challenges/challengeActionTypes";


const getInitialState = () => {
    return {day: new Date(), internalErrorsCount: 0};
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
            loginErrorDescription: getErrorDescriptionForLogin(action.status, action.textStatus, action.responseText),
            loginInfoDescription: null
        })
    } else if (isAction(action, LOGIN_USER_RESPONSE_SUCCESS)) {
        return copy(state).and({
            loginErrorDescription: null,
            loginInfoDescription: null
        })
    } else if (isAction(action, INTERNAL_ERROR_WEB_RESPONSE)) {
        /*        console.log("Internal error web response");
         if (state.internalErrorsCount > 1)
         return copy(state).and({
         loginErrorDescription: "Unexpected error occurred. Please login again",
         })
         else*/
        return copy(state).and({

            internalErrorsCount: state.internalErrorsCount + 1
        })
    } else if (isAction(action, START_FORGOT_PASSWORD_MODE)) {
        return copy(state).and({
            forgotPasswordMode: true
        })
    } else if (isAction(action, FINISH_FORGOT_PASSWORD_MODE)) {
        if (action.emailSent)
            return copy(state).and({
                forgotPasswordMode: false,
                loginInfoDescription: "An email with link to reset your password has been sent."
            })
        else {
            return copy(state).and({
                forgotPasswordMode: false,
                loginInfoDescription: null
            });
        }
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


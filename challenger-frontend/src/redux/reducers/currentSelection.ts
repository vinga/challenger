import {isAction} from "../ReduxTask";
import {
    INCREMENT_DAY, INTERNAL_ERROR_WEB_RESPONSE, WEB_CALL_START, WEB_CALL_END, WEB_CALL_END_ERROR,
    DISPLAY_LONG_CALL, HIDE_LONG_CALL
} from "../actions/actions";
import {CurrentSelection, copy, WebCallData} from "../ReduxState";
import {LOGIN_USER_RESPONSE_FAILURE, LOGIN_USER_RESPONSE_SUCCESS, START_FORGOT_PASSWORD_MODE, FINISH_FORGOT_PASSWORD_MODE} from "../../module_accounts/accountActionTypes";


const getInitialState = () => {
    return {day: new Date(), internalErrorsCount: 0, currentWebCalls: []};
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
    } else if (isAction(action,DISPLAY_LONG_CALL)) {
        return copy(state).and({
            longCallVisible: action.longCallVisible
        });
    } else if (isAction(action,HIDE_LONG_CALL)) {
        return copy(state).and({
            longCallVisible: null
        });
    }

    return state;
}

export function webCallsState(state: WebCallData[] = [], action): WebCallData[] {
    if (isAction(action, 'LOGOUT')) {
        return []
    } else if (isAction(action, WEB_CALL_START)) {
        return state.concat(action.webCallData)
    } else if (isAction(action, WEB_CALL_END)) {
        return state.filter(cw=> cw.callUid != action.callUid)
    } else if (isAction(action, WEB_CALL_END_ERROR)) {
        return state.filter(cw=> cw.callUid != action.callUid)
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


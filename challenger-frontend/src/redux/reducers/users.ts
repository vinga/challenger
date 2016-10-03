import * as jwtDecode from "jwt-decode";
import ajaxWrapper from "../../logic/AjaxWrapper.ts";
import {Action, isAction} from "../ReduxTask";
import {
    LOGOUT,
    LOGIN_USER_REQUEST,
    LOGIN_USER_RESPONSE_SUCCESS,
    LOGIN_USER_RESPONSE_FAILURE, ON_LOGOUT_SECOND_USER
} from "../actions/actions";
import {AccountDTO} from "../../logic/domain/AccountDTO";


export default function users(state:Array<AccountDTO> = [], action:Action) {
    if (isAction(action, LOGOUT)) {
        return [];
    }
    else if (isAction(action, LOGIN_USER_REQUEST)) {
        var nstate:Array<AccountDTO> = [
            ...state,
            {
                userId: -1,
                login: action.login,
                errorDescription: null,
                inProgress: true,
                primary: action.primary
            }
        ];
        return nstate;
    } else if (isAction(action, LOGIN_USER_RESPONSE_SUCCESS)) {
        return state.map((u:AccountDTO) => {
            if (u.login == action.login) {

                if (u.primary)
                    ajaxWrapper.webToken = action.jwtToken;

                return Object.assign({}, u, {
                    userId: jwtDecode(action.jwtToken).info.userId,
                    login: action.login,
                    jwtToken: action.jwtToken,
                    lastUpdated: Date.now(),
                    errorDescription: null,
                    inProgress: false,

                });

            } else
                return Object.assign({}, u);
        });
    } else if (isAction(action, LOGIN_USER_RESPONSE_FAILURE)) {
        console.log("login failure");
        return state.map((u:AccountDTO) => {
            if (u.login == action.login) {
                return Object.assign({}, u, {
                    jwtToken: null,
                    errorDescription: getErrorDescriptionForLogin(action.jqXHR.responseText, action.jqXHR, action.textStatus, action.exception),
                    inProgress: false
                });

            } else
                return Object.assign({}, u);
        });
    } else if (isAction(action, ON_LOGOUT_SECOND_USER)) {
        console.log("second ser "+action.userId);
        return state.map((u:AccountDTO) => {
            if (u.userId == action.userId) {
                return Object.assign({}, u, {jwtToken: null});
            } else
                return Object.assign({}, u);
        });
    }
    return state;
}

const getErrorDescriptionForLogin = (data, jqXHR, textStatus, exception) => {
    if (jqXHR.status === 0)
        return "Connection refused"; //('Not connect.\n Verify Network.');
    else if (jqXHR.status == 401) {
        if (data!=null)
            return data;
        return jqXHR.responseText;
    } else {
        console.log("Error unexpected... " + jqXHR.status + " " + jqXHR.responseText);
        return "Unexpected problem"
    }
}


import * as jwtDecode from "jwt-decode";
import {AccountDTO} from "./AccountDTO";
import {
    LOGOUT, LOGIN_USER_REQUEST, LOGIN_USER_RESPONSE_SUCCESS, LOGIN_USER_RESPONSE_FAILURE, ON_LOGOUT_SECOND_USER, REGISTER_SHOW_REGISTRATION_PANEL, REGISTER_USER_REQUEST,
    REGISTER_USER_RESPONSE
} from "./accountActionTypes";
import {isAction} from "../redux/ReduxTask";
import ajaxWrapper from "../logic/AjaxWrapper";
import {RegisterState} from "../redux/ReduxState";


export function accounts(state:Array<AccountDTO>=[], action) {
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

                var decodedToken:any=jwtDecode(action.jwtToken);
                var iat:number=decodedToken.iat;
                var exp:number=decodedToken.exp;
                console.log("IAT "+new Date(iat*1000).simpleFormatWithMinutes());
                console.log("EXP "+new Date(exp*1000).simpleFormatWithMinutes());
                // co jak issued at bedzie

                return Object.assign({}, u, {
                    userId: jwtDecode(action.jwtToken).info.userId,
                    login: action.login,
                    jwtToken: action.jwtToken,
                    lastUpdated: Date.now(),
                    errorDescription: null,
                    inProgress: false,
                    tokenExpirationDate: new Date(exp*1000)
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
                    tokenExpirationDate: null,
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
                return Object.assign({}, u, {jwtToken: null, tokenExpirationDate: null});
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
};


export function registerState(state: RegisterState = null, action): RegisterState {
    if (isAction(action, REGISTER_SHOW_REGISTRATION_PANEL)) {
        if (state == null)
            state = {};
        return state;
    }
    else if (isAction(action, REGISTER_USER_REQUEST)) {

        return Object.assign({}, state, {
            registerError: null,
            registerInProgress: true
        });
    }
    else if (isAction(action, REGISTER_USER_RESPONSE)) {
        if (action.registerSuccess) {
            return Object.assign({}, state, {
                registerError: null,
                registerInProgress: false,
                registeredSuccessfully: true,
            });
        } else {
            return Object.assign({}, state, {
                registerError: action.registerError,
                registerInProgress: false,
                registeredSuccessfully: false,
            });
        }
    } else if (isAction(action, LOGIN_USER_REQUEST)) {
        return null;
    }
    else return state;

}

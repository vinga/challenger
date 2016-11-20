import * as jwtDecode from "jwt-decode";
import {AccountDTO} from "./AccountDTO";
import {
    LOGOUT,
    LOGIN_USER_REQUEST,
    LOGIN_USER_RESPONSE_SUCCESS,
    LOGIN_USER_RESPONSE_FAILURE,
    ON_LOGOUT_SECOND_USER,
    REGISTER_SHOW_REGISTRATION_PANEL,
    REGISTER_USER_REQUEST,
    REGISTER_USER_RESPONSE, UNAUTHORIZED_WEB_RESPONSE, REGISTER_USER_RESPONSE_FAILURE,
} from "./accountActionTypes";
import {isAction} from "../redux/ReduxTask";
import {baseWebCall} from "../logic/WebCall";
import {RegisterState} from "./RegisterResponseDTO";


export function accounts(state: Array<AccountDTO> = [], action) {
    if (isAction(action, LOGOUT)) {
        return [];
    }
    else if (isAction(action, LOGIN_USER_REQUEST)) {



        var nstate: Array<AccountDTO> = [
            ...state,
            {
                id: action.userId!=null ? action.userId: -1,
                login: action.login,
                errorDescription: null,
                infoDescription: null,
                inProgress: true,
                primary: action.primary
            }
        ];
        return nstate;
    } else if (isAction(action, LOGIN_USER_RESPONSE_SUCCESS)) {
        return state.map((u: AccountDTO) => {
            if (u.login == action.login) {

                console.log("update token "+u.primary+" "+baseWebCall.webToken+" "+action.jwtToken);
                if (u.primary) {
                    baseWebCall.webToken = action.jwtToken;
                }

                var decodedToken: any = jwtDecode(action.jwtToken);
                var iat: number = decodedToken.iat;
                var exp: number = decodedToken.exp;
                console.log("IAT " + new Date(iat * 1000).simpleFormatWithMinutes());
                console.log("EXP " + new Date(exp * 1000).simpleFormatWithMinutes());
                // co jak issued at bedzie

                return Object.assign({}, u, {
                    id: jwtDecode(action.jwtToken).info.userId,
                    login: action.login,
                    jwtToken: action.jwtToken,
                    lastUpdated: Date.now(),
                    errorDescription: null,
                    infoDescription: null,
                    inProgress: false,
                    tokenExpirationDate: new Date(exp * 1000)
                });

            } else
                return Object.assign({}, u);
        });
    } else if (isAction(action, LOGIN_USER_RESPONSE_FAILURE)) {
        console.log("login failure",action);
        return state.map((u: AccountDTO) => {
            if (u.login == action.login) {
                var uu= Object.assign({}, u, {
                    jwtToken: null,
                    tokenExpirationDate: null,
                    errorDescription: getErrorDescriptionForLogin(action.status, action.textStatus, action.responseText),
                    inProgress: false
                });
        console.log("UU ",uu);
                return uu;
            } else
                return Object.assign({}, u);
        });
    } else if (isAction(action, ON_LOGOUT_SECOND_USER)) {
        console.log("second ser " + action.userId);
        return state.map((u: AccountDTO) => {
            if (u.id == action.userId) {
                return Object.assign({}, u, {jwtToken: null, tokenExpirationDate: null});
            } else
                return Object.assign({}, u);
        });
    } else if (isAction(action, UNAUTHORIZED_WEB_RESPONSE)) {

        // multusr response returned as unauthorized
        if (action.jwtToken.constructor === Array) {
           // just log out primary user
            state.filter(u=>u.primary==true).map((u: AccountDTO) => {
                return Object.assign({}, u, {
                    jwtToken: null,
                    tokenExpirationDate: null,
                    errorDescription: null,
                    infoDescription: "You (or other challenge participant) have been logged out. Please login again",
                    inProgress: false
                });
            });

        }


        return state.filter(u=>u.jwtToken==action.jwtToken).map((u: AccountDTO) => {
                return Object.assign({}, u, {
                    jwtToken: null,
                    tokenExpirationDate: null,
                    errorDescription: null,
                    infoDescription: "You've been logged out. Please login again",
                    inProgress: false
                });
        });
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
    } else if (isAction(action, REGISTER_USER_RESPONSE_FAILURE)) {
        return Object.assign({}, state, {
            registerError: action.responseText,
            registerInProgress: false,
            registeredSuccessfully: false,
        });
    } else if (isAction(action, LOGIN_USER_REQUEST)) {
        return null;
    }
    else return state;

}

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
    REGISTER_USER_RESPONSE,
    UNAUTHORIZED_WEB_RESPONSE,
    REGISTER_USER_RESPONSE_FAILURE,
    REGISTER_EXIT_TO_LOGIN_PANEL,
    SET_CURRENT_CONFIRMATION_ID,
    CONFIRMATION_LINK_RESPONSE,
    CLEAR_CONFIRMATION_LINK_STATE
} from "./accountActionTypes";
import {isAction} from "../redux/ReduxTask";
import {baseWebCall} from "../logic/WebCall";
import {RegisterState, ConfirmationLinkState, RegisterResponseDTO} from "./RegisterResponseDTO";
import {WebCallState} from "../logic/domain/Common";


export function accounts(state: Array<AccountDTO> = [], action) {
    if (isAction(action, LOGOUT)) {
        return [];
    }
    else if (isAction(action, LOGIN_USER_REQUEST)) {
        var nstate: Array<AccountDTO> = [
            ...state,
            {
                id: action.userId != null ? action.userId : -1,
                login: action.login,
                infoDescription: null,
                inProgress: true,
                primary: action.primary
            }
        ];
        return nstate;

    } else if (isAction(action, LOGIN_USER_RESPONSE_SUCCESS)) {
        return state.map((u: AccountDTO) => {
            if (u.login == action.login) {

                console.log("update token " + u.primary + " " + baseWebCall.webToken + " " + action.jwtToken);
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
                    infoDescription: null,
                    inProgress: false,
                    tokenExpirationDate: new Date(exp * 1000)
                });

            } else
                return u;
        });

    } else if (isAction(action, LOGIN_USER_RESPONSE_FAILURE)) {
        console.log("login failure", action);
        return state.map((u: AccountDTO) => {
            if (u.login == action.login) {
                return  Object.assign({}, u, {
                    jwtToken: null,
                    tokenExpirationDate: null,
                    inProgress: false
                });
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
            state.filter(u=>u.primary == true).map((u: AccountDTO) => {
                return Object.assign({}, u, {
                    jwtToken: null,
                    tokenExpirationDate: null,
                    infoDescription: "You (or other challenge participant) have been logged out. Please login again",
                    inProgress: false
                });
            });

        }


        return state.filter(u=>u.jwtToken == action.jwtToken).map((u: AccountDTO) => {
            return Object.assign({}, u, {
                jwtToken: null,
                tokenExpirationDate: null,
                infoDescription: "You've been logged out. Please login again",
                inProgress: false
            });
        });
    }
    return state;
}


const initialRegisterState = (): RegisterState => {
    return {webCall: {webCallState: WebCallState.INITIAL},
        finishedWithSuccess: false,
        stillRequireEmailConfirmation: false}
}
export function registerState(state: RegisterState = null, action): RegisterState {
    if (isAction(action, REGISTER_SHOW_REGISTRATION_PANEL)) {
        var state = initialRegisterState();
        state.requiredEmail = action.requiredEmail;
        state.proposedLogin = action.proposedLogin;
        state.emailIsConfirmedByConfirmationLink = action.emailIsConfirmedByConfirmationLink;
        return state;
    }
    else if (isAction(action, REGISTER_EXIT_TO_LOGIN_PANEL)) {
        return null;
    } else if (isAction(action, REGISTER_USER_REQUEST)) {

        return Object.assign({}, state, {
            registerError: null,
            stillRequireEmailConfirmation: null,
            finishedWithSuccess: false,
            webCall: {webCallState: WebCallState.IN_PROGRESS}
        });
    }
    else if (isAction(action, REGISTER_USER_RESPONSE)) {
        var regResp: RegisterResponseDTO = action;
        return Object.assign({}, state, {
            stillRequireEmailConfirmation: regResp.needsEmailConfirmation,
            finishedWithSuccess: action.registerError == null,
            registerError: action.registerError,
            webCall: {webCallState: WebCallState.RESPONSE_OK}
        });

    } else if (isAction(action, REGISTER_USER_RESPONSE_FAILURE)) {
        return Object.assign({}, state, {
            registerError: action.humanReadableException,
            stillRequireEmailConfirmation: null,
            finishedWithSuccess: false,
            webCall: {webCallState: WebCallState.RESPONSE_FAILURE}
        });
    } else if (isAction(action, LOGIN_USER_REQUEST)) {
        return null;
    }
    else return state;

}


export function confirmationLinkState(state: ConfirmationLinkState = null, action): ConfirmationLinkState {
    if (isAction(action, CONFIRMATION_LINK_RESPONSE)) {
        if (state == null)
            state = {};
        return Object.assign({}, state, {
            confirmationLinkResponse: action.confirmationLink
        });
    } else if (isAction(action, SET_CURRENT_CONFIRMATION_ID)) {
        if (state == null)
            state = {};
        return {uid: action.uid}
    } else if (isAction(action, CLEAR_CONFIRMATION_LINK_STATE)) {
        return null;
    } else
        return state;
}
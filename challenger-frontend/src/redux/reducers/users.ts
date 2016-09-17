import * as jwtDecode from "jwt-decode";
import ajaxWrapper from "../../logic/AjaxWrapper.ts";
import {isAction,  Action} from "../actions/actions.ts";
import {LOGIN_USER_REQUEST, LOGIN_USER_RESPONSE_SUCCESS, LOGIN_USER_RESPONSE_FAILURE} from "../actions/users";

export default function users(state = [], action:Action) {
    if (isAction(action, LOGIN_USER_REQUEST)) {
        console.log("LOGIN REQUEST "+action.login);
        var nstate= [
            ...state,
            {
                login: action.login,
                errorDescription: null,
                inProgress: true,
                primary: action.primary
            }
        ];
        return nstate;
    } else if (isAction(action, LOGIN_USER_RESPONSE_SUCCESS)) {
        return state.map((u) => {
            if (u.login == action.login) {

                if (u.primary)
                    ajaxWrapper.webToken = action.jwtToken;

                return Object.assign({}, u, {
                    userId: jwtDecode(action.jwtToken).info.userId,
                    login: action.login,
                    jwtToken: action.jwtToken,
                    lastUpdated: Date.now(),
                    errorDescription: null,
                    inProgress: false
                });

            } else
                return Object.assign({}, u);
        });
    } else if (isAction(action, LOGIN_USER_RESPONSE_FAILURE)) {
        console.log("login failure");
        return state.map((u) => {
            if (u.login == action.login) {
                return Object.assign({}, u, {
                    jwtToken: null,
                    errorDescription: getErrorDescriptionForLogin(action.jqXHR, action.exception),
                    inProgress: false
                });

            } else
                return Object.assign({}, u);
        });
    }
    return state;
}

const getErrorDescriptionForLogin = (jqXHR, exception) => {
    if (jqXHR.status === 0)
        return "Connection refused"; //('Not connect.\n Verify Network.');
    else if (jqXHR.status == 401)
        return jqXHR.responseText;
    else {
        console.log("Error unexpected... " + jqXHR.status + " " + jqXHR.responseText);
        return "Unexpected problem"
    }
}


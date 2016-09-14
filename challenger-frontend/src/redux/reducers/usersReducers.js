import * as userActions from "../actions/usersActions";
import jwtDecode from "jwt-decode";
import ajaxWrapper from "../../logic/AjaxWrapper";

export default function users(state = [], action) {
    switch (action.type) {
        case userActions.LOGIN_USER_REQUEST:
            return [
                ...state,
                {
                    login: action.login,
                    errorDescription: null,
                    inProgress: true,
                    primary: action.primary
                }
            ]
        case userActions.LOGIN_USER_RESPONSE_SUCCESS:
            return state.map((u) => {
                if (u.login == action.login) {

                    if (u.primary)
                        ajaxWrapper.webToken=action.jwtToken;

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
            return res;
        case userActions.LOGIN_USER_RESPONSE_FAILURE:

            return
                state.map((u) => {
                if (u.login == action.login) {

                    return Object.assign({}, u, {
                        jwtToken: null,
                        errorDescription: action.jqXHR.responseText,
                        inProgress: false
                    });

                } else
                    return Object.assign({}, u);
            });

        default:
            return state
    }
}

export function isLogged(state) {

}
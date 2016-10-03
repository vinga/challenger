import ajaxWrapper from "../../logic/AjaxWrapper.ts";
import {
     LOGIN_USER_REQUEST, LOGIN_USER_RESPONSE_SUCCESS,
    LOGIN_USER_RESPONSE_FAILURE
} from "./actions.ts";
import {ActionType} from "../ReduxTask";
import {fetchWebChallenges} from "./challengeActions";






export function loginUserAction(login: string, password: string, primary: boolean) {
    return function (dispatch) {
        dispatch(LOGIN_USER_REQUEST.new({login, password, primary}));
        ajaxWrapper.login(login, password).then(
            jwtToken=> {
                dispatch(LOGIN_USER_RESPONSE_SUCCESS.new({login, jwtToken}));
                dispatch(fetchWebChallenges());
            },
            (jqXHR:any, textStatus:string, exception)=> dispatch(LOGIN_USER_RESPONSE_FAILURE.new({
                login,
                textStatus,
                jqXHR,
                exception
            }))
        )
    }
}

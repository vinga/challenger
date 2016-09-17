import ajaxWrapper from "../../logic/AjaxWrapper.ts";
import {fetchWebChallenges, ActionType} from "./actions.ts";

/*export const LOGIN_USER_REQUEST='LOGIN_USER_REQUEST';
export const LOGIN_USER_RESPONSE_SUCCESS='LOGIN_USER_RESPONSE_SUCCESS';
export const LOGIN_USER_RESPONSE_FAILURE='LOGIN_USER_RESPONSE_FAILURE';*/
//export const LOGOUT='LOGOUT';

/*
export function logout() {
    return { type: LOGOUT }
}*/
export const LOGIN_USER_REQUEST: ActionType<{login: string, password: string, primary:boolean}> = new ActionType<any>('LOGIN_USER_REQUEST');
export const LOGIN_USER_RESPONSE_SUCCESS: ActionType<{login: string, jwtToken: string}> = new ActionType<any>('LOGIN_USER_RESPONSE_SUCCESS');
export const LOGIN_USER_RESPONSE_FAILURE: ActionType<{login: string, exception: any,data: any, jqXHR : JQueryXHR}> = new ActionType<any>('LOGIN_USER_RESPONSE_FAILURE');



/*
function loginUserRequest(login: string, password: string, primary :boolean) {
    return { type: LOGIN_USER_REQUEST, login:login, password: password, primary: primary }
}
function loginUserResponseSuccess(login: string, jwtToken: string) {
    return { type: LOGIN_USER_RESPONSE_SUCCESS, login: login, jwtToken:jwtToken }
}
function loginUserResponseFailure(login: string, data: string, jqXHR: JQueryXHR, exception: any) {
    return { type: LOGIN_USER_RESPONSE_FAILURE, login: login, data:data, jqXHR: jqXHR, exception: exception }
}*/

export function loginUserAction(login: string, password: string, primary: boolean) {
    return function (dispatch) {
        dispatch(LOGIN_USER_REQUEST.new({login, password, primary}));
        ajaxWrapper.login(login, password).then(
            jwtToken=>{
                dispatch(LOGIN_USER_RESPONSE_SUCCESS.new({login, jwtToken}));
                dispatch(fetchWebChallenges());
            },
            (data:any, jqXHR:any, exception)=> dispatch(LOGIN_USER_RESPONSE_FAILURE.new({login, data, jqXHR, exception}))
        )
    }
}
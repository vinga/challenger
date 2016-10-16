import ajaxWrapper from "../../logic/AjaxWrapper.ts";
import {LOGIN_USER_REQUEST, LOGIN_USER_RESPONSE_SUCCESS, LOGIN_USER_RESPONSE_FAILURE} from "./actions.ts";
import {fetchWebChallenges} from "./challengeActions";
import {ReduxState} from "../ReduxState";


function renewToken(login:string, jwtToken:string) {
    return function (dispatch, getState:()=>ReduxState) {
        console.log("renew token");
        ajaxWrapper.renewToken(login, jwtToken).then(jwtToken=> {
                dispatch(LOGIN_USER_RESPONSE_SUCCESS.new({login, jwtToken}));
                dispatch(queueRenewAccessToken(login));
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




function queueRenewAccessToken(login) {
    return function (dispatch, getState:()=>ReduxState) {
        getState().users.filter(u=>login == login).map(u=> {
console.log("queueAccessToken");
            // one minute before expiration time
            var requestTimeMillis = u.tokenExpirationDate.getTime() - 1000 * 60;
            requestTimeMillis=new Date().getTime()+6000;
            console.log(u.tokenExpirationDate + " " + new Date(requestTimeMillis));

            setTimeout(() => {
                dispatch(renewToken(login, u.jwtToken))
            }, 5000)
        });
        /* setTimeout(() => {
         store.dispatch({ type: 'HIDE_NOTIFICATION' })
         }, 5000)*/
    }
}


export function loginUserAction(login:string, password:string, primary:boolean) {
    return function (dispatch) {
        dispatch(LOGIN_USER_REQUEST.new({login, password, primary}));
        ajaxWrapper.login(login, password).then(
            jwtToken=> {
                dispatch(LOGIN_USER_RESPONSE_SUCCESS.new({login, jwtToken}));
             //   dispatch(queueRenewAccessToken(login));
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

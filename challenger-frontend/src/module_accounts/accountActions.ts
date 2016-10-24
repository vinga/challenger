import {ReduxState} from "../redux/ReduxState";
import ajaxWrapper from "../logic/AjaxWrapper";
import {LOGIN_USER_RESPONSE_SUCCESS, LOGIN_USER_RESPONSE_FAILURE, LOGIN_USER_REQUEST, REGISTER_USER_REQUEST, REGISTER_USER_RESPONSE} from "./accountActionTypes";
import {fetchWebChallenges} from "../module_challenges/index";

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
        getState().accounts.filter(u=>login == login).map(u=> {
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

export function registerUserAction(email:string, login:string, password:string) {
    return function (dispatch) {
        dispatch(REGISTER_USER_REQUEST.new({}));
        ajaxWrapper.register(email, login, password).then(
            registerResponseDTO=> {
                dispatch(REGISTER_USER_RESPONSE.new(registerResponseDTO));
            }

        )
    }
}




import {ReduxState} from "../redux/ReduxState";
import * as webCall from "./accountWebCalls";
import {
    LOGIN_USER_RESPONSE_SUCCESS, LOGIN_USER_RESPONSE_FAILURE, LOGIN_USER_REQUEST, REGISTER_USER_REQUEST, REGISTER_USER_RESPONSE,
    UNAUTHORIZED_WEB_RESPONSE, REGISTER_USER_RESPONSE_FAILURE
} from "./accountActionTypes";
import {fetchWebChallenges} from "../module_challenges/index";
import {WEB_STATUS_UNAUTHORIZED} from "../logic/domain/Common";
import {validateEmail} from "../views/common-components/TextFieldExt";
import {UPDATE_CHALLENGE_PARTICIPANTS, UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION} from "../module_challenges/challengeActionTypes";
import {ChallengeParticipantDTO} from "../module_challenges/ChallengeDTO";

function renewToken(login: string, jwtToken: string) {
    return function (dispatch, getState: ()=>ReduxState) {
        console.log("renew token");
        webCall.renewToken(login, jwtToken).then(jwtToken=> {
                dispatch(LOGIN_USER_RESPONSE_SUCCESS.new({login, jwtToken}));
                dispatch(queueRenewAccessToken(login));
            }).catch((reason)=>authPromiseErr(reason,dispatch));
    }
}


function queueRenewAccessToken(login) {
    return function (dispatch, getState: ()=>ReduxState) {
        getState().accounts.filter(u=>login == login).map(u=> {

            console.log("queueAccessToken");
            // one minute before expiration time
            var requestTimeMillis = u.tokenExpirationDate.getTime() - 1000 * 60;
            var offset=requestTimeMillis-Date.now();
            setTimeout(() => {
                dispatch(renewToken(login, u.jwtToken))
            }, offset)
        });
    }
}


export function loginUserAction(login: string, password: string, primary: boolean, userId?: number) {
    return function (dispatch) {
        dispatch(LOGIN_USER_REQUEST.new({login, password, primary, userId}));
        return webCall.login(login, password).then(
                jwtToken=> {
                    dispatch(LOGIN_USER_RESPONSE_SUCCESS.new({login, jwtToken}));
                    dispatch(queueRenewAccessToken(login));
                    if (primary) {
                        dispatch(fetchWebChallenges());
                    }
                }
            ).catch((response:XMLHttpRequest)=> {
            console.log("resss",response);
                dispatch(LOGIN_USER_RESPONSE_FAILURE.new({
                    login,
                    textStatus: response.responseText,
                    status: response.status,
                    responseText: response.responseText
                }));
            })

    }
}

export function registerUserAction(email: string, login: string, password: string) {
    return function (dispatch) {
        dispatch(REGISTER_USER_REQUEST.new({}));
        webCall.register(email, login, password).then(
            registerResponseDTO=> {
                dispatch(REGISTER_USER_RESPONSE.new(registerResponseDTO));
            }
        ).catch((response:XMLHttpRequest)=> {
            dispatch(REGISTER_USER_RESPONSE_FAILURE.new({
                login,
                textStatus: response.responseText,
                status: response.status,
                responseText: response.responseText
            }));
        })
    }
}

export function authPromiseErr(reason: any, dispatch) {
    if (reason.status==WEB_STATUS_UNAUTHORIZED) {
        console.log("FOUND UNAUTHORIZED");
        dispatch(UNAUTHORIZED_WEB_RESPONSE.new({jwtToken:reason.jwtToken}));
    }
    throw reason
}


export function updateChallengeParticipants(loginOrEmail: string) {
    return function (dispatch, getState: ()=>ReduxState) {
        if(validateEmail(loginOrEmail)) {
            dispatch(UPDATE_CHALLENGE_PARTICIPANTS.new({loginOrEmail: loginOrEmail}))
        } else {
            webCall.checkIfLoginExists(loginOrEmail).then(
                exists => {
                    if (exists) {
                        dispatch(UPDATE_CHALLENGE_PARTICIPANTS.new({loginOrEmail: loginOrEmail}))
                    } else {
                        dispatch(UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION.new({errorText: "Invalid login or email"}))
                    }

                }
            ).catch((reason)=>authPromiseErr(reason,dispatch));
        }
    }
}


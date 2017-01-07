import {ReduxState} from "../redux/ReduxState";
import * as webCall from "./accountWebCalls";
import {
    LOGIN_USER_RESPONSE_SUCCESS,
    LOGIN_USER_RESPONSE_FAILURE,
    LOGIN_USER_REQUEST,
    REGISTER_USER_REQUEST,
    REGISTER_USER_RESPONSE,
    REGISTER_USER_RESPONSE_FAILURE,
    FINISH_FORGOT_PASSWORD_MODE,
    SET_CURRENT_CONFIRMATION_ID,
    CONFIRMATION_LINK_RESPONSE,
    CLEAR_CONFIRMATION_LINK_STATE, REGISTER_EXIT_TO_LOGIN_PANEL
} from "./accountActionTypes";
import {fetchWebChallenges} from "../module_challenges/index";
import {validateEmail} from "../views/common-components/TextFieldExt";
import {
    UPDATE_CHALLENGE_PARTICIPANTS,
    UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION,
    CHECK_CHALLENGE_PARTICIPANTS_REQUEST,
    CHECK_CHALLENGE_PARTICIPANTS_RESPONSE
} from "../module_challenges/challengeActionTypes";
import {ConfirmationLinkRequestDTO, ConfirmationLinkResponseDTO} from "./AccountDTO";
import {parseExceptionToHumanReadable} from "../logic/WebCall";
import {SHOW_CUSTOM_NOTIFICATION} from "../redux/actions/actions";

function renewToken(login: string, jwtToken: string) {
    return function (dispatch, getState: ()=>ReduxState) {
        console.log("renew token");
        webCall.renewToken(dispatch, login, jwtToken).then(jwtToken=> {
            dispatch(LOGIN_USER_RESPONSE_SUCCESS.new({login, jwtToken}));
            dispatch(queueRenewAccessToken(login));
        });
    }
}


function queueRenewAccessToken(login) {
    return function (dispatch, getState: ()=>ReduxState) {
        getState().accounts.filter(u=>u.login == login).map(u=> {
            console.log("queueAccessToken " + u.tokenExpirationDate);
            // one minute before expiration time
            var requestTimeMillis = u.tokenExpirationDate.getTime() - 1000 * 60;
            var offset = requestTimeMillis - Date.now();
            setTimeout(() => {
                dispatch(renewToken(login, u.jwtToken))
            }, offset)
        });
    }
}


function onLoginWithJWTToken(dispatch, login: string, jwtToken, primary: boolean) {
    dispatch(LOGIN_USER_RESPONSE_SUCCESS.new({login, jwtToken}));
    dispatch(queueRenewAccessToken(login));
    if (primary) {
        dispatch(fetchWebChallenges());
    }
}
export function loginUserAction(login: string, password: string, primary: boolean, userId?: number) {
    return function (dispatch) {
        dispatch(LOGIN_USER_REQUEST.new({login, primary, userId}));
        return webCall.login(dispatch, login, password).then(
            jwtToken=> {
                onLoginWithJWTToken(dispatch, login, jwtToken, primary);
            }
        ).catch((response: XMLHttpRequest)=> {

            dispatch(LOGIN_USER_RESPONSE_FAILURE.new({login, humanReadableException: parseExceptionToHumanReadable(response)}));

        })

    }
}

export function registerUserAction(email: string, login: string, password: string, emailIsConfirmedByConfirmationLink: string) {
    return function (dispatch) {
        dispatch(REGISTER_USER_REQUEST.new({}));
        webCall.register(dispatch, email, login, password, emailIsConfirmedByConfirmationLink).then(
            registerResponseDTO=> {
                if (registerResponseDTO.registerError==null && !registerResponseDTO.needsEmailConfirmation) {
                    // we an already login, cause no email confirmation is neededd
                    dispatch(loginUserAction(login, password,true)).then(()=>{
                        dispatch(REGISTER_EXIT_TO_LOGIN_PANEL.new({}));
                        dispatch(SHOW_CUSTOM_NOTIFICATION.new({textClosable: "Welcome to Challenger"}));
                    });
                } else
                    dispatch(REGISTER_USER_RESPONSE.new(registerResponseDTO));
            }
        ).catch((response: XMLHttpRequest)=> {
            dispatch(REGISTER_USER_RESPONSE_FAILURE.new({humanReadableException: parseExceptionToHumanReadable(response)}));
        })
    }
}


export function updateChallengeParticipantsAction(loginOrEmail: string) {
    return function (dispatch, getState: ()=>ReduxState) {
        if (validateEmail(loginOrEmail)) {
            dispatch(UPDATE_CHALLENGE_PARTICIPANTS.new({loginOrEmail: loginOrEmail}))
        } else {
            dispatch(CHECK_CHALLENGE_PARTICIPANTS_REQUEST.new({}));
            webCall.checkIfLoginExists(dispatch, loginOrEmail).then(
                exists => {

                    if (exists) {
                        dispatch(UPDATE_CHALLENGE_PARTICIPANTS.new({loginOrEmail: loginOrEmail}))
                    } else {
                        dispatch(UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION.new({errorText: "Invalid login or email"}))
                    }
                    dispatch(CHECK_CHALLENGE_PARTICIPANTS_RESPONSE.new({}));

                }
            );
        }
    }
}


export function getConfirmationLinkResponse(uid: string, confirmationRequest: ConfirmationLinkRequestDTO) {
    return function (dispatch, getState: ()=>ReduxState) {
        history.pushState("", document.title, window.location.pathname);
        dispatch(SET_CURRENT_CONFIRMATION_ID.new({uid: uid}));
        webCall.getConfirmationLinkResponse(dispatch, uid, confirmationRequest).then(
            (response: ConfirmationLinkResponseDTO) => {
                console.log("cwir cwir ", response);

                dispatch(CONFIRMATION_LINK_RESPONSE.new({confirmationLink: response}))

                if (response.jwtToken != null) {
                    dispatch(LOGIN_USER_REQUEST.new({login: response.login, primary: true}));
                    dispatch(CLEAR_CONFIRMATION_LINK_STATE.new({}));
                    var primary=true;
                    onLoginWithJWTToken(dispatch, response.login, response.jwtToken, primary);
                    dispatch(SHOW_CUSTOM_NOTIFICATION.new({textClosable: "Welcome to Challenger!"}));
                }
            }
        );
    }
}


export function sendResetPasswordLinkAction(email: string) {
    return function (dispatch, getState: ()=>ReduxState) {
        dispatch(FINISH_FORGOT_PASSWORD_MODE.new({emailSent: true}))
        webCall.sendResetPasswordLink(dispatch, email);
    }
}

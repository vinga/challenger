import {baseWebCall} from "../logic/WebCall";
import {RegisterResponseDTO} from "./RegisterResponseDTO";
import {ConfirmationLinkRequestDTO, ConfirmationLinkResponseDTO} from "./AccountDTO";


export function login(dispatch, login: string, pass: string): Promise<string> {
    return baseWebCall.postUnauthorizedNoJson(dispatch, "/accounts/newToken", {
        'login': login,
        'pass': pass
    });

}

export function register(dispatch, email: string, login: string, pass: string): Promise<RegisterResponseDTO> {
    return baseWebCall.postUnauthorized(dispatch, "/accounts/register", {
        'email': email,
        'login': login,
        'password': pass
    });
}

export function renewToken(dispatch,login: string, jwtToken: string): Promise<string> {
    return baseWebCall.postCustomNoJson(dispatch,"/accounts/renewToken", {'login': login}, jwtToken);
}

export function checkIfLoginExists(dispatch, login: string): Promise<boolean> {
    return baseWebCall.get(dispatch,`/accounts?login=${login}`);
}

export function getConfirmationLinkResponse(dispatch, uid: string, confirmationLink: ConfirmationLinkRequestDTO): Promise<ConfirmationLinkResponseDTO> {
    return baseWebCall.post(dispatch, `/accounts/confirmationLinks/${uid}`, confirmationLink);
}

export function sendResetPasswordLink(dispatch, email: string): Promise<void> {
    // for security reasons email is not in URL
    return baseWebCall.postUnauthorizedString(dispatch, "/accounts/passwordReset", email);
}
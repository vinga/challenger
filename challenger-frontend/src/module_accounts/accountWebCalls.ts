import {baseWebCall} from "../logic/WebCall";
import {RegisterResponseDTO} from "./RegisterResponseDTO";
import {ConfirmationLinkRequestDTO, ConfirmationLinkResponseDTO} from "./AccountDTO";


export function login(login: string, pass: string): Promise<string> {
    return baseWebCall.postUnauthorizedNoJson("/accounts/newToken", {
        'login': login,
        'pass': pass
    });

}

export function register(email: string, login: string, pass: string): Promise<RegisterResponseDTO> {
    return baseWebCall.postUnauthorized("/accounts/register", {
        'email': email,
        'login': login,
        'password': pass
    });
}

export function renewToken(login: string, jwtToken: string): Promise<string> {
    return baseWebCall.postCustomNoJson("/accounts/renewToken", {'login': login}, jwtToken);
}

export function checkIfLoginExists(login: string): Promise<boolean> {
    return baseWebCall.get(`/accounts?login=${login}`);
}

export function getConfirmationLinkResponse(uid: string, confirmationLink: ConfirmationLinkRequestDTO): Promise<ConfirmationLinkResponseDTO> {
    return baseWebCall.post(`/accounts/confirmationLinks/${uid}`, confirmationLink);
}

export function sendResetPasswordLink(email: string): Promise<void> {
    // for security reasons email is not in URL
    return baseWebCall.postUnauthorizedString("/accounts/passwordReset", email);
}
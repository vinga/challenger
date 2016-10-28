import {webCall} from "../logic/WebCall"
import {RegisterResponseDTO} from "./RegisterResponseDTO";


export function login(login: string, pass: string): JQueryPromise<string> {
    return webCall.postUnauthorizedNoJson("/accounts/newToken",{
        'login': login,
        'pass': pass
    });

}

export function register(email: string, login: string, pass: string): JQueryPromise<RegisterResponseDTO> {
    return webCall.postUnauthorized("/accounts/register",{
        'email':email,
        'login': login,
        'pass': pass
    });
}

export function renewToken(login: string, jwtToken: string): JQueryPromise<string> {
    return webCall.getAuthorizedCustom("/accounts/renewToken", jwtToken);
}
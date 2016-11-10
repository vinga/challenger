import {baseWebCall} from "../logic/WebCall"
import {RegisterResponseDTO} from "./RegisterResponseDTO";


export function login(login: string, pass: string): Promise<string> {
    return baseWebCall.postUnauthorizedNoJson("/accounts/newToken",{
        'login': login,
        'pass': pass
    });

}

export function register(email: string, login: string, pass: string): Promise<RegisterResponseDTO> {
    return baseWebCall.postUnauthorized("/accounts/register",{
        'email':email,
        'login': login,
        'pass': pass
    });
}

export function renewToken(login: string, jwtToken: string): Promise<string> {
    return baseWebCall.postCustomNoJson("/accounts/renewToken", { 'login': login }, jwtToken);
}
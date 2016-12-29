

import {ConfirmationLinkResponseDTO} from "./AccountDTO";
import {WebCallDTO} from "../logic/domain/Common";
export interface RegisterResponseDTO {
    registerError?:string,
    registerSuccess:boolean,
    needsEmailConfirmation:boolean,



}


export interface RegisterState {
    webCall: WebCallDTO<RegisterResponseDTO>

    //registerInProgress?: boolean;
    registerError?: string;
    registeredSuccessfully?: boolean;


}

export interface ConfirmationLinkState {
    confirmationLinkResponse?: ConfirmationLinkResponseDTO
    uid?: string
}
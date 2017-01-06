

import {ConfirmationLinkResponseDTO} from "./AccountDTO";
import {WebCallDTO} from "../logic/domain/Common";
export interface RegisterResponseDTO {
    registerError?:string,
    registerSuccess:boolean,
    needsEmailConfirmation:boolean,



}


export interface RegisterState {
    webCall: WebCallDTO<RegisterResponseDTO>

    finishedWithSuccess: boolean,
    stillRequireEmailConfirmation: boolean;

    registerError?: string;



    emailIsConfirmedByConfirmationLink?: string,
    requiredEmail?: string;
    proposedLogin?: string


}

export interface ConfirmationLinkState {
    confirmationLinkResponse?: ConfirmationLinkResponseDTO
    uid?: string
}
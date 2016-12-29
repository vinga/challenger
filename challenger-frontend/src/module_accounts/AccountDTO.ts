
export interface AccountDTO {
    id: number,
    login: string,
    label?: string,
    //errorDescription?: string,
    infoDescription?: string,
    inProgress: boolean,
    primary: boolean,
    jwtToken?: string,
    tokenExpirationDate?: Date,
    challengeStatus?: string, // filled dynamically
}

export interface ConfirmationLinkRequestDTO {
    newLogin?: string,
    newPassword?: string
}

export interface ConfirmationLinkResponseDTO {
    description: string,
    newLoginRequired: boolean
    newPasswordRequired: boolean
    displayLoginButton: boolean
    validationError?: string
    done: boolean
    proposedLogin?: string
}


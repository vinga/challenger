
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
    newPasswordRequired: boolean,
    newLoginRequired: boolean,

    registerInternalData? : RegisterInternalDataDTO

    displayRegisterButton: boolean,
    displayLoginButton: boolean
    validationError?: string
    done: boolean
    proposedLogin?: string
    jwtToken?: string,
    login?: string,
    displayLoginWelcomeInfo? :boolean,
    nextActions: string[]
}
export interface RegisterInternalDataDTO { // used in ConfirmationLinkResponseDTO
    emailRequiredForRegistration: string,
    loginProposedForRegistration: string,
    emailIsConfirmedByConfirmationLink: string,
}


export const NextActionType = {
    AUTO_LOGIN: "AUTO_LOGIN",
    MANAGED_REGISTER_BUTTON: "MANAGED_REGISTER_BUTTON",
    NEXT: "NEXT",
    MAIN_PAGE: "MAIN_PAGE",
    LOGIN_BUTTON: "LOGIN_BUTTON",
    REGISTER_BUTTON: "REGISTER_BUTTON"
}





export interface RegisterResponseDTO {
    registerError?:string,
    registerSuccess:boolean,
    needsEmailConfirmation:boolean


}


export interface RegisterState {
    registerInProgress?: boolean;
    registerError?: string;
    registeredSuccessfully?: boolean;

}
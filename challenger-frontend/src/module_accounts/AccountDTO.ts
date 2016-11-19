
export interface AccountDTO {
    id: number,
    login: string,
    label?: string,
    errorDescription?: string,
    infoDescription?: string,
    inProgress: boolean,
    primary: boolean,
    jwtToken?: string,
    tokenExpirationDate?: Date
}


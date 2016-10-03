
export interface AccountDTO {
    userId: number,
    login: string,
    label?: string,
    errorDescription: string,
    inProgress: boolean,
    primary: boolean,
    jwtToken?: string
}
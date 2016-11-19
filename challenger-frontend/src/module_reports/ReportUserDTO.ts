
export interface ReportState {
    challengeReports: ChallengeReportsDTO[]
}
export interface ChallengeReportsDTO {
    challengeId: number,
    reports: ReportDataDTO[],
    lastMaxProgressive? :number
}

export const ReportType = {
    progressive: "progressive",
};

export interface ReportDataDTO {
    challengeId: number,
    reportType: string,
    userId: number,
    labels: string[],
    values: number[],

    maxValue: number // value is calculated locally for reports with same type in the same challenge, to have unified Y-axes
}





export interface ReportUserDTO {
    id: number,
    label: string,
    ordinal: number

}
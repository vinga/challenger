
export interface ChallengeDTO {
    id: number,
    label: string,
    challengeStatus: string;
    creatorId: number;
    myId: number;
    userLabels: Array<ChallengeParticipantDTO>,
}

export interface VisibleChallengesDTO {
    selectedChallengeId: number,
    visibleChallenges: Array<ChallengeDTO>
    editedChallenge: ChallengeDTO,
    errorText?: string
}

export const ChallengeStatus = {
    INACTIVE: "INACTIVE",
    ACTIVE: "ACTIVE",
    WAITING_FOR_ACCEPTANCE: "WAITING_FOR_ACCEPTANCE",
    REFUSED: "REFUSED"
};


export interface ChallengeParticipantDTO {
    id: number,
    label: string,
    login?: string,
    ordinal: number, //  ordinal will be different for different users, because caller has always 0
    email?: string
}



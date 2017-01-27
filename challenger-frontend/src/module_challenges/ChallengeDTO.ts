
export interface ChallengeDTO {
    id: number,
    label: string,
    challengeStatus: string;
    creatorId: number;
    myId: number;
    userLabels: Array<ChallengeParticipantDTO>,
}

export const NO_CHALLENGES_LOADED_YET=-1;
export const NO_ACTIVE_CHALLENGES=-2;
export interface VisibleChallengesDTO {
    selectedChallengeId?: number,
    visibleChallenges: Array<ChallengeDTO>
    editedChallenge: ChallengeDTO,
    challengeParticipantIsChecked? : boolean // remote call is progress, edited challenge cannot be added until this is false or null
    errorText?: string
}

export const ChallengeStatus = {
    INACTIVE: "INACTIVE",
    ACTIVE: "ACTIVE",
    WAITING_FOR_ACCEPTANCE: "WAITING_FOR_ACCEPTANCE",
    REFUSED: "REFUSED",
    REMOVED: "REMOVED"
};


export interface ChallengeParticipantDTO {
    id: number,
    label: string,
    login?: string,
    ordinal: number, //  ordinal will be different for different users, because caller has always 0
    email?: string,
    challengeStatus: string,
    jwtToken?: string
}



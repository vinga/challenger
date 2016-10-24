import {UserDTO} from "./UserDTO";
import {EventGroupDTO} from "../logic/domain/EventGroupDTO";

export interface ChallengeDTO {
    id: number,
    label: string,
    challengeStatus: string;
    creatorId: number;
    myId: number;
    userLabels: Array<UserDTO>;
    displayedConversation?:EventGroupDTO;
}

export interface VisibleChallengesDTO {
    selectedChallengeId: number,
    visibleChallenges: Array<ChallengeDTO>
}

export const ChallengeStatus = {
    INACTIVE: "INACTIVE",
    ACTIVE: "ACTIVE",
    WAITING_FOR_ACCEPTANCE: "WAITING_FOR_ACCEPTANCE",
    REFUSED: "REFUSED"
};

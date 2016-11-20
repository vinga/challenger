import {baseWebCall} from "../logic/WebCall";
import {VisibleChallengesDTO, ChallengeParticipantDTO, ChallengeDTO} from "./ChallengeDTO";

export function loadVisibleChallenges(): Promise<VisibleChallengesDTO> {
    return baseWebCall.get("/challenges");
}

export function createChallenge(challenge: ChallengeDTO) : Promise<ChallengeDTO> {
    return baseWebCall.post("/challenges", challenge);
}







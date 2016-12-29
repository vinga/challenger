import {baseWebCall} from "../logic/WebCall";
import {VisibleChallengesDTO, ChallengeParticipantDTO, ChallengeDTO} from "./ChallengeDTO";

export function loadVisibleChallenges(dispatch): Promise<VisibleChallengesDTO> {
    return baseWebCall.get(dispatch, "/challenges");
}

export function createChallenge(dispatch, challenge: ChallengeDTO) : Promise<ChallengeDTO> {
    return baseWebCall.post(dispatch, "/challenges", challenge);
}

export function acceptOrRejectChallenge(dispatch, challengeId: number, accept: boolean) : Promise<ChallengeDTO> {
    return baseWebCall.post(dispatch, `/challenges/${challengeId}/acceptance`, accept);
}







import {baseWebCall} from "../logic/WebCall";
import {VisibleChallengesDTO, ChallengeDTO} from "./ChallengeDTO";

export function loadVisibleChallenges(dispatch): Promise<VisibleChallengesDTO> {
    return baseWebCall.get(dispatch, "/challenges");
}

export function createChallenge(dispatch, challenge: ChallengeDTO): Promise<ChallengeDTO> {
    return baseWebCall.put(dispatch, "/challenges", challenge);
}

export function acceptOrRejectChallenge(dispatch, challengeId: number, accept: boolean): Promise<ChallengeDTO> {
    return baseWebCall.post(dispatch, `/challenges/${challengeId}/acceptance`, accept);
}

export function updateChallenge(dispatch, challenge: ChallengeDTO): Promise<ChallengeDTO> {
    return baseWebCall.post(dispatch, `/challenges/${challenge.id}`, challenge);
}

export function deleteChallenge(dispatch, challengeId: number): Promise<boolean> {
    return baseWebCall.delete(dispatch, `/challenges/${challengeId}`);
}






import {baseWebCall} from "../logic/WebCall";
import {VisibleChallengesDTO, ChallengeParticipantDTO} from "./ChallengeDTO";

export function loadVisibleChallenges(): Promise<VisibleChallengesDTO> {
    return baseWebCall.get("/challenge/visibleChallenges");
}







import {baseWebCall} from "../logic/WebCall";
import {VisibleChallengesDTO} from "./ChallengeDTO";

export function loadVisibleChallenges(): Promise<VisibleChallengesDTO> {
    return baseWebCall.get("/challenge/visibleChallenges");
}





import {webCall} from "../logic/WebCall";
import {VisibleChallengesDTO} from "./ChallengeDTO";

export function loadVisibleChallenges(): JQueryPromise<VisibleChallengesDTO> {
    return webCall.get("/challenge/visibleChallenges");
}





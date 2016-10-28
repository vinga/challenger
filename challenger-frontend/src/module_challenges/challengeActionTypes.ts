import {ActionType} from "../redux/ReduxTask";
import {VisibleChallengesDTO} from "./ChallengeDTO";


export const CHANGE_CHALLENGE: ActionType<{challengeId: number}> = new ActionType<any>('CHANGE_CHALLENGE');
export const WEB_CHALLENGES_REQUEST: ActionType<{}> = new ActionType<any>('WEB_CHALLENGES_REQUEST');
export const WEB_CHALLENGES_RESPONSE: ActionType<VisibleChallengesDTO> = new ActionType<any>('WEB_CHALLENGES_RESPONSE');


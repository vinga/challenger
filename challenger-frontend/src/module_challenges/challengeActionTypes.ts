import {ActionType} from "../redux/ReduxTask";
import {VisibleChallengesDTO} from "./ChallengeDTO";


export const CHANGE_CHALLENGE: ActionType<{challengeId: number}> = new ActionType<any>('CHANGE_CHALLENGE');
export const WEB_CHALLENGES_REQUEST: ActionType<{}> = new ActionType<any>('WEB_CHALLENGES_REQUEST');
export const WEB_CHALLENGES_RESPONSE: ActionType<VisibleChallengesDTO> = new ActionType<any>('WEB_CHALLENGES_RESPONSE');
export const CLOSE_EDIT_CHALLENGE: ActionType<{}> = new ActionType<any>('CLOSE_EDIT_CHALLENGE');
export const CREATE_NEW_CHALLENGE: ActionType<{}> = new ActionType<any>('CREATE_NEW_CHALLENGE');
export const UPDATE_CHALLENGE_PARTICIPANTS: ActionType<{loginOrEmail: string}> = new ActionType<any>('UPDATE_CHALLENGE_PARTICIPANTS');


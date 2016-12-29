import {ActionType} from "../redux/ReduxTask";
import {VisibleChallengesDTO, ChallengeParticipantDTO} from "./ChallengeDTO";
import {ConfirmationLinkResponseDTO} from "../module_accounts/index";


export const CHANGE_CHALLENGE: ActionType<{challengeId: number}> = new ActionType<any>('CHANGE_CHALLENGE');
export const WEB_CHALLENGES_REQUEST: ActionType<{}> = new ActionType<any>('WEB_CHALLENGES_REQUEST');
export const WEB_CHALLENGES_RESPONSE: ActionType<VisibleChallengesDTO> = new ActionType<any>('WEB_CHALLENGES_RESPONSE');
export const CLOSE_EDIT_CHALLENGE: ActionType<{}> = new ActionType<any>('CLOSE_EDIT_CHALLENGE');
export const CREATE_NEW_CHALLENGE: ActionType<{creatorLabel: string}> = new ActionType<any>('CREATE_NEW_CHALLENGE');

export const CHECK_CHALLENGE_PARTICIPANTS_REQUEST: ActionType<{}> = new ActionType<any>('CHECK_CHALLENGE_PARTICIPANTS_REQUEST');
export const CHECK_CHALLENGE_PARTICIPANTS_RESPONSE: ActionType<{}> = new ActionType<any>('CHECK_CHALLENGE_PARTICIPANTS_RESPONSE');
export const UPDATE_CHALLENGE_PARTICIPANTS: ActionType<{loginOrEmail: string}> = new ActionType<any>('UPDATE_CHALLENGE_PARTICIPANTS');
export const DELETE_CHALLENGE_PARTICIPANT: ActionType<{label: string}> = new ActionType<any>('DELETE_CHALLENGE_PARTICIPANT');
export const UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION: ActionType<{errorText?: string}> = new ActionType<any>('UPDATE_ERROR_TEXT_IN_USER_LOGIN_EMAIL_VALIDATION');


//export const ACCEPT_OR_REJECT_NEW_CHALLENGE: ActionType<{challengeId: number, accept: boolean}> = new ActionType<any>('ACCEPT_OR_REJECT_NEW_CHALLENGE');


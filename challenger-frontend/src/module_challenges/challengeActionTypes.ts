import {ActionType} from "../redux/ReduxTask";
import {VisibleChallengesDTO} from "./ChallengeDTO";
import {EventGroupDTO} from "../logic/domain/EventGroupDTO";
import {EventDTO} from "../logic/domain/EventDTO";


export const CHANGE_CHALLENGE: ActionType<{challengeId: number}> = new ActionType<any>('CHANGE_CHALLENGE');
export const WEB_CHALLENGES_REQUEST: ActionType<{}> = new ActionType<any>('WEB_CHALLENGES_REQUEST');
export const WEB_CHALLENGES_RESPONSE: ActionType<VisibleChallengesDTO> = new ActionType<any>('WEB_CHALLENGES_RESPONSE');

export const WEB_EVENTS_RESPONSE: ActionType<EventGroupDTO> = new ActionType<any>('WEB_EVENTS_RESPONSE');
export const ADD_NEW_EVENT_OPTIMISTIC: ActionType<EventDTO> = new ActionType<any>('ADD_NEW_EVENT_OPTIMISTIC');

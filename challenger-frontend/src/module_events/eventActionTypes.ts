import {ActionType} from "../redux/ReduxTask";
import {EventGroupDTO, EventDTO, EventGroupSynchDTO} from "./EventDTO";
import {TaskDTO} from "../module_tasks/TaskDTO";



export const WEB_EVENTS_SYNC_RESPONSE: ActionType<{eventGroup: EventGroupSynchDTO, loggedUserId: number}> = new ActionType<any>('WEB_EVENTS_SYNC_RESPONSE');
export const WEB_EVENTS_SYNC_RESPONSE_PREVIOUS: ActionType<{eventGroup: EventGroupSynchDTO, loggedUserId: number}> = new ActionType<any>('WEB_EVENTS_SYNC_RESPONSE_PREVIOUS');
export const WEB_EVENTS_ASYNC_RESPONSE: ActionType<{events: EventDTO[], loggedUserId: number, selectedChallengeId: number, maxTotalEventReadId: number}> = new ActionType<any>('WEB_EVENTS_ASYNC_RESPONSE');


export const ADD_NEW_EVENT_OPTIMISTIC: ActionType<EventDTO> = new ActionType<any>('ADD_NEW_EVENT_OPTIMISTIC');
export const MARK_EVENT_AS_READ_OPTIMISTIC: ActionType<{challengeId: number, eventId: number, readDate: number}> = new ActionType<any>('MARK_EVENT_AS_READ_OPTIMISTIC');
export const EXPAND_EVENTS_WINDOW: ActionType<{expanded: boolean}> = new ActionType<any>('EXPAND_EVENTS_WINDOW');
export const SHOW_TASK_EVENTS: ActionType<{task?: TaskDTO, no?: number, toggle: boolean}> = new ActionType<any>('SHOW_TASK_EVENTS');
export const TOGGLE_EVENT_ACTIONS_VISIBILITY: ActionType<{}> = new ActionType<any>('TOGGLE_EVENT_ACTIONS_VISIBILITY');
export const NEW_EVENTS_SEED: ActionType<{seed: number}> = new ActionType<any>('NEW_EVENTS_SEED');

export const CLEAR_UNREAD_NOTIFICATIONS: ActionType<{challengeId: number}> = new ActionType<any>('CLEAR_UNREAD_NOTIFICATIONS');



export const SHOW_GLOBAL_NOTIFICATIONS_DIALOG: ActionType<{show: boolean}> = new ActionType<any>('SHOW_GLOBAL_NOTIFICATIONS_DIALOG');


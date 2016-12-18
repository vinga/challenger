import {ActionType} from "../redux/ReduxTask";
import {EventGroupDTO, EventDTO} from "./EventDTO";
import {TaskDTO} from "../module_tasks/TaskDTO";



export const WEB_EVENTS_RESPONSE: ActionType<EventGroupDTO> = new ActionType<any>('WEB_EVENTS_RESPONSE');
export const WEB_ASYNC_EVENT_RESPONSE: ActionType<{events: EventDTO[]}> = new ActionType<any>('WEB_ASYNC_EVENT_RESPONSE');
export const ADD_NEW_EVENT_OPTIMISTIC: ActionType<EventDTO> = new ActionType<any>('ADD_NEW_EVENT_OPTIMISTIC');
export const MARK_EVENT_AS_READ_OPTIMISTIC: ActionType<{challengeId: number, eventId: number, readDate: number}> = new ActionType<any>('MARK_EVENT_AS_READ_OPTIMISTIC');
export const EXPAND_EVENTS_WINDOW: ActionType<{expanded: boolean}> = new ActionType<any>('EXPAND_EVENTS_WINDOW');
export const SHOW_TASK_EVENTS: ActionType<{task?: TaskDTO, no?: number, toggle: boolean}> = new ActionType<any>('SHOW_TASK_EVENTS');


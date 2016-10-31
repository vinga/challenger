import {ActionType} from "../redux/ReduxTask";
import {EventGroupDTO, EventDTO} from "./EventDTO";
import {TaskDTO} from "../module_tasks/TaskDTO";



export const WEB_EVENTS_RESPONSE: ActionType<EventGroupDTO> = new ActionType<any>('WEB_EVENTS_RESPONSE');
export const ADD_NEW_EVENT_OPTIMISTIC: ActionType<EventDTO> = new ActionType<any>('ADD_NEW_EVENT_OPTIMISTIC');
export const EXPAND_EVENTS_WINDOW: ActionType<{expanded: boolean}> = new ActionType<any>('EXPAND_EVENTS_WINDOW');
export const SHOW_TASK_EVENTS: ActionType<{task?: TaskDTO, no?: number}> = new ActionType<any>('SHOW_TASK_EVENTS');


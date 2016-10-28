import {ActionType} from "../redux/ReduxTask";
import {EventGroupDTO, EventDTO} from "./EventDTO";



export const WEB_EVENTS_RESPONSE: ActionType<EventGroupDTO> = new ActionType<any>('WEB_EVENTS_RESPONSE');
export const ADD_NEW_EVENT_OPTIMISTIC: ActionType<EventDTO> = new ActionType<any>('ADD_NEW_EVENT_OPTIMISTIC');
export const EXPAND_EVENTS_WINDOW: ActionType<{expanded: boolean}> = new ActionType<any>('EXPAND_EVENTS_WINDOW');


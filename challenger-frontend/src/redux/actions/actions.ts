import {ActionType} from "../ReduxTask";
import {LongCallVisible, WebCallData} from "../ReduxState";


export const INCREMENT_DAY: ActionType<{amount: number}> = new ActionType<any>('INCREMENT_DAY');


export const DISPLAY_REQUEST_IN_PROGRESS: ActionType<{}> = new ActionType<any>('DISPLAY_REQUEST_IN_PROGRESS');

export const INTERNAL_ERROR_WEB_RESPONSE: ActionType<{jwtToken: string | Array<string>}> = new ActionType<any>('INTERNAL_ERROR_WEB_RESPONSE');


export const DISPLAY_LONG_CALL: ActionType<{longCallVisible: LongCallVisible}> = new ActionType<any>('DISPLAY_LONG_CALL');
export const HIDE_LONG_CALL: ActionType<{}> = new ActionType<any>('HIDE_LONG_CALL');

export const WEB_CALL_START: ActionType<{webCallData: WebCallData}> = new ActionType<any>('WEB_CALL_START');
export const WEB_CALL_END: ActionType<{callUid: number}> = new ActionType<any>('WEB_CALL_END');
export const WEB_CALL_END_ERROR: ActionType<{callUid: number}> = new ActionType<any>('WEB_CALL_END_ERROR');


export const SHOW_CUSTOM_NOTIFICATION: ActionType<{textClosable: string}> = new ActionType<any>('SHOW_CUSTOM_NOTIFICATION');












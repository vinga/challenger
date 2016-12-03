import {ActionType} from "../ReduxTask";


export const INCREMENT_DAY: ActionType<{amount: number}> = new ActionType<any>('INCREMENT_DAY');


export const DISPLAY_REQUEST_IN_PROGRESS: ActionType<{}> = new ActionType<any>('DISPLAY_REQUEST_IN_PROGRESS');
export const INTERNAL_ERROR_WEB_RESPONSE: ActionType<{jwtToken: string | Array<string>}> = new ActionType<any>('INTERNAL_ERROR_WEB_RESPONSE');













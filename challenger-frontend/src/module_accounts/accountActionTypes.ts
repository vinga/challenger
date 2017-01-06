
import {ActionType} from "../redux/ReduxTask";


import {RegisterResponseDTO} from "./RegisterResponseDTO";
import {ConfirmationLinkResponseDTO} from "./AccountDTO";


export const LOGIN_USER_REQUEST: ActionType<{login: string, primary: boolean, userId?: number}> = new ActionType<any>('LOGIN_USER_REQUEST');
export const LOGIN_USER_RESPONSE_SUCCESS: ActionType<{login: string, jwtToken: string}> = new ActionType<any>('LOGIN_USER_RESPONSE_SUCCESS');
export const LOGIN_USER_RESPONSE_FAILURE: ActionType<{login: string, humanReadableException: string}> = new ActionType<any>('LOGIN_USER_RESPONSE_FAILURE');
export const LOGOUT: ActionType<{}> = new ActionType<any>('LOGOUT');
export const UNAUTHORIZED_WEB_RESPONSE: ActionType<{jwtToken: string | Array<string>}> = new ActionType<any>('UNAUTHORIZED_WEB_RESPONSE');




export const REGISTER_USER_REQUEST: ActionType<{}> = new ActionType<any>('REGISTER_USER_REQUEST');
export const REGISTER_USER_RESPONSE: ActionType<RegisterResponseDTO> = new ActionType<RegisterResponseDTO>('REGISTER_USER_RESPONSE');
export const REGISTER_USER_RESPONSE_FAILURE: ActionType<{humanReadableException: string}> = new ActionType<any>('REGISTER_USER_RESPONSE_FAILURE');
export const REGISTER_SHOW_REGISTRATION_PANEL: ActionType<{requiredEmail?: string, proposedLogin?: string,  emailIsConfirmedByConfirmationLink?: string}> = new ActionType<any>('REGISTER_SHOW_REGISTRATION_PANEL');
export const REGISTER_EXIT_TO_LOGIN_PANEL: ActionType<{}> = new ActionType<any>('REGISTER_EXIT_TO_LOGIN_PANEL');


export const START_FORGOT_PASSWORD_MODE: ActionType<{}> = new ActionType<any>('START_FORGOT_PASSWORD_MODE');
export const FINISH_FORGOT_PASSWORD_MODE: ActionType<{emailSent: boolean}> = new ActionType<any>('FINISH_FORGOT_PASSWORD_MODE');


export const CONFIRMATION_LINK_RESPONSE: ActionType<{confirmationLink: ConfirmationLinkResponseDTO}> = new ActionType<any>('CONFIRMATION_LINK_RESPONSE');
export const SET_CURRENT_CONFIRMATION_ID: ActionType<{uid: string   }> = new ActionType<any>('SET_CURRENT_CONFIRMATION_ID');
export const CLEAR_CONFIRMATION_LINK_STATE: ActionType<{}> = new ActionType<any>('CLEAR_CONFIRMATION_LINK_STATE');



export const ON_LOGOUT_SECOND_USER: ActionType<{userId: number}> = new ActionType<any>('ON_LOGOUT_SECOND_USER');

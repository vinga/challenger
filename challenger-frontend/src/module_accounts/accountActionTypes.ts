
import {ActionType} from "../redux/ReduxTask";


import {RegisterResponseDTO} from "./RegisterResponseDTO";


export const LOGIN_USER_REQUEST: ActionType<{login: string, password: string, primary: boolean}> = new ActionType<any>('LOGIN_USER_REQUEST');
export const LOGIN_USER_RESPONSE_SUCCESS: ActionType<{login: string, jwtToken: string}> = new ActionType<any>('LOGIN_USER_RESPONSE_SUCCESS');
export const LOGIN_USER_RESPONSE_FAILURE: ActionType<{login: string, status: number, textStatus: string, responseText: string}> = new ActionType<any>('LOGIN_USER_RESPONSE_FAILURE');
export const LOGOUT: ActionType<{}> = new ActionType<any>('LOGOUT');
export const UNAUTHORIZED_WEB_RESPONSE: ActionType<{jwtToken: string | Array<string>}> = new ActionType<any>('UNAUTHORIZED_WEB_RESPONSE');


export const REGISTER_USER_REQUEST: ActionType<{}> = new ActionType<any>('REGISTER_USER_REQUEST');
export const REGISTER_USER_RESPONSE: ActionType<RegisterResponseDTO> = new ActionType<RegisterResponseDTO>('REGISTER_USER_RESPONSE_SUCCESS');
export const REGISTER_USER_RESPONSE_FAILURE: ActionType<{login: string, status: number, textStatus: string, responseText: string}> = new ActionType<any>('REGISTER_USER_RESPONSE_FAILURE');
export const REGISTER_SHOW_REGISTRATION_PANEL: ActionType<{}> = new ActionType<any>('REGISTER_SHOW_REGISTRATION_PANEL');



export const ON_LOGOUT_SECOND_USER: ActionType<{userId: number}> = new ActionType<any>('ON_LOGOUT_SECOND_USER');

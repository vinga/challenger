
import {VisibleChallengesDTO} from "../../logic/domain/ChallengeDTO";
import {ActionType} from "../ReduxTask";
import {TaskDTOListForDay, TaskDTO} from "../../logic/domain/TaskDTO";
import {TaskProgressDTO} from "../../logic/domain/TaskProgressDTO";
import {ConversationDTO} from "../../logic/domain/ConversationDTO";


export const INCREMENT_DAY: ActionType<{amount: number}> = new ActionType<any>('INCREMENT_DAY');
export const CHANGE_CHALLENGE: ActionType<{challengeId: number}> = new ActionType<any>('CHANGE_CHALLENGE');
export const WEB_CHALLENGES_REQUEST: ActionType<{}> = new ActionType<any>('WEB_CHALLENGES_REQUEST');
export const WEB_CHALLENGES_RESPONSE: ActionType<{visibleChallenges: VisibleChallengesDTO}> = new ActionType<any>('WEB_CHALLENGES_RESPONSE');
export const LOAD_TASKS_REQUEST: ActionType<{challengeId: number, day: Date}> = new ActionType<any>('LOAD_TASKS_REQUEST');
export const LOAD_TASKS_RESPONSE: ActionType<TaskDTOListForDay> = new ActionType<any>('LOAD_TASKS_RESPONSE');
export const LOAD_CONVERSATION_RESPONSE: ActionType<ConversationDTO> = new ActionType<any>('LOAD_CONVERSATION_RESPONSE');
export const LOGIN_USER_REQUEST: ActionType<{login: string, password: string, primary:boolean}> = new ActionType<any>('LOGIN_USER_REQUEST');
export const LOGIN_USER_RESPONSE_SUCCESS: ActionType<{login: string, jwtToken: string}> = new ActionType<any>('LOGIN_USER_RESPONSE_SUCCESS');
export const LOGIN_USER_RESPONSE_FAILURE: ActionType<{login: string, textStatus: string, jqXHR : JQueryXHR, exception: any}> = new ActionType<any>('LOGIN_USER_RESPONSE_FAILURE');
export const LOGOUT: ActionType<{}> = new ActionType<any>('LOGOUT');



export const MARK_TASK_DONE_OPTIMISTIC: ActionType<{challengeId: number, taskProgress:TaskProgressDTO}> = new ActionType<any>('MARK_TASK_DONE_OPTIMISTIC');
export const MODIFY_TASK_OPTIMISTIC: ActionType<TaskDTO> = new ActionType<any>('MODIFY_TASK_OPTIMISTIC');
export const DELETE_TASK_OPTIMISTIC: ActionType<TaskDTO> = new ActionType<any>('DELETE_TASK_OPTIMISTIC');


export const OPEN_EDIT_TASK: ActionType<TaskDTO> = new ActionType<any>('OPEN_EDIT_TASK');
export const MODIFY_TASK_REQUEST: ActionType<TaskDTO> = new ActionType<any>('MODIFY_TASK_REQUEST');
export const TASK_PROGRESS_REQUEST: ActionType<{challengeId: number, taskProgress:TaskProgressDTO}> = new ActionType<any>('TASKPROGRESS_REQUEST');

export const CLOSE_EDIT_TASK: ActionType<{}> = new ActionType<any>('CLOSE_EDIT_TASK');
export const DISPLAY_REQUEST_IN_PROGRESS: ActionType<{}> = new ActionType<any>('DISPLAY_REQUEST_IN_PROGRESS');

export const ON_LOGOUT_SECOND_USER: ActionType<{userId:number}> = new ActionType<any>('ON_LOGOUT_SECOND_USER');












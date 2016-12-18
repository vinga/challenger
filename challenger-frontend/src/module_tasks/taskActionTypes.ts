import {ActionType} from "../redux/ReduxTask";
import {TaskDTOListForDay, TaskDTO, TaskProgressDTO, TaskProgressDTOListForDay} from "./TaskDTO";


export const LOAD_TASKS_REQUEST_OLDWAY: ActionType<{challengeId: number, day: Date}> = new ActionType<any>('LOAD_TASKS_REQUEST_OLDWAY');
export const LOAD_TASKS_RESPONSE_OLDWAY: ActionType<TaskDTOListForDay> = new ActionType<any>('LOAD_TASKS_RESPONSE_OLDWAY');


export const LOAD_TASKS_REQUEST_NEWWAY: ActionType<{challengeId: number}> = new ActionType<any>('LOAD_TASKS_REQUEST_NEWWAY');
export const LOAD_TASKS_RESPONSE_NEWWAY: ActionType<{tasks: TaskDTO[]}> = new ActionType<any>('LOAD_TASKS_RESPONSE_NEWWAY');


export const LOAD_TASK_PROGRESSES_REQUEST: ActionType<{challengeId: number, day: Date}> = new ActionType<any>('LOAD_TASK_PROGRESSES_REQUEST');
export const LOAD_TASK_PROGRESSES_RESPONSE: ActionType<{payload: TaskProgressDTOListForDay}> = new ActionType<any>('LOAD_TASK_PROGRESSES_RESPONSE');

export const MARK_TASK_DONE_OPTIMISTIC: ActionType<{challengeId: number, taskProgress:TaskProgressDTO}> = new ActionType<any>('MARK_TASK_DONE_OPTIMISTIC');
export const MARK_TASK_DONE_UNDONE_REMOTE: ActionType<{challengeId: number, taskId:number, done: boolean, forDay: Date}> = new ActionType<any>('MARK_TASK_DONE_UNDONE_REMOTE');

export const CLOSE_TASK_OPTIMISTIC: ActionType<{task: TaskDTO}> = new ActionType<any>('CLOSE_TASK_OPTIMISTIC');
export const MODIFY_TASK_OPTIMISTIC: ActionType<TaskDTO> = new ActionType<any>('MODIFY_TASK_OPTIMISTIC');
export const DELETE_TASK_OPTIMISTIC: ActionType<TaskDTO> = new ActionType<any>('DELETE_TASK_OPTIMISTIC');
export const DELETE_TASKS_REMOTE: ActionType<{taskIdsToDelete: number[]}> = new ActionType<any>('DELETE_TASKS_REMOTE');

export const OPEN_EDIT_TASK: ActionType<TaskDTO> = new ActionType<any>('OPEN_EDIT_TASK');
export const CREATE_AND_OPEN_EDIT_TASK: ActionType<{creatorId: number, forUserId: number, challengeId: number}> = new ActionType<any>('CREATE_AND_OPEN_EDIT_TASK');
export const MODIFY_TASK_REQUEST: ActionType<TaskDTO> = new ActionType<any>('MODIFY_TASK_REQUEST');
export const TASK_PROGRESS_REQUEST: ActionType<{challengeId: number, taskProgress:TaskProgressDTO}> = new ActionType<any>('TASKPROGRESS_REQUEST');
export const CLOSE_EDIT_TASK: ActionType<{}> = new ActionType<any>('CLOSE_EDIT_TASK');
export const MARK_ALL_CHALLENGE_TASKS_PROGRESSES_AS_INVALID: ActionType<{challengeId: number}> = new ActionType<any>('MARK_ALL_CHALLENGE_TASKS_PROGRESSES_AS_INVALID');

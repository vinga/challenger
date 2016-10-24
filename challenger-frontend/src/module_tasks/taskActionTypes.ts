import {ActionType} from "../redux/ReduxTask";
import {TaskDTOListForDay, TaskDTO, TaskProgressDTO} from "./TaskDTO";


export const LOAD_TASKS_REQUEST: ActionType<{challengeId: number, day: Date}> = new ActionType<any>('LOAD_TASKS_REQUEST');
export const LOAD_TASKS_RESPONSE: ActionType<TaskDTOListForDay> = new ActionType<any>('LOAD_TASKS_RESPONSE');
export const MARK_TASK_DONE_OPTIMISTIC: ActionType<{challengeId: number, taskProgress:TaskProgressDTO}> = new ActionType<any>('MARK_TASK_DONE_OPTIMISTIC');
export const MODIFY_TASK_OPTIMISTIC: ActionType<TaskDTO> = new ActionType<any>('MODIFY_TASK_OPTIMISTIC');
export const DELETE_TASK_OPTIMISTIC: ActionType<TaskDTO> = new ActionType<any>('DELETE_TASK_OPTIMISTIC');
export const OPEN_EDIT_TASK: ActionType<TaskDTO> = new ActionType<any>('OPEN_EDIT_TASK');
export const MODIFY_TASK_REQUEST: ActionType<TaskDTO> = new ActionType<any>('MODIFY_TASK_REQUEST');
export const TASK_PROGRESS_REQUEST: ActionType<{challengeId: number, taskProgress:TaskProgressDTO}> = new ActionType<any>('TASKPROGRESS_REQUEST');
export const CLOSE_EDIT_TASK: ActionType<{}> = new ActionType<any>('CLOSE_EDIT_TASK');

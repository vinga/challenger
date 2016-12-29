import {baseWebCall} from "../logic/WebCall";
import {TaskDTO, TaskApprovalDTO, TaskProgressDTO} from "./TaskDTO";


export function loadTaskProgresses(dispatch,challengeId: number, date: Date, loadTasks: boolean): Promise<Array<TaskProgressDTO>> {
    return baseWebCall.get(dispatch,`/challenges/${challengeId}/taskProgresses/?day=${date.yy_mm_dd()}&loadTasks=${loadTasks}`);
}
export function loadTasksNewWay(dispatch, challengeId: number, newTaskIds: number[]) : Promise<Array<TaskDTO>> {
    var joined=newTaskIds.join(",")
    return baseWebCall.get(dispatch, `/challenges/${challengeId}/tasks/?ids=${joined}`);
}

export function updateTask(dispatch, task: TaskDTO): Promise<TaskDTO> {
    return baseWebCall.put(dispatch, `/challenges/${task.challengeId}/tasks/${task.id}`, task);
}

export function createTask(dispatch, task: TaskDTO, jwtTokens: Array<String>): Promise<TaskDTO> {
    return baseWebCall.postMultiAuthorized(dispatch, `/challenges/${task.challengeId}/tasks/`, task, jwtTokens);
}

export function deleteTask(dispatch, task: TaskDTO, jwtToken: string): Promise<void> {
    return baseWebCall.deleteMultiAuthorized(dispatch, `/challenges/${task.challengeId}/tasks/${task.id}`,[].concat(jwtToken));
}
export function updateTaskStatus(dispatch, challengeId: number, taskStatus: TaskApprovalDTO, jwtTokens: Array<String>): Promise<TaskDTO> {
    return baseWebCall.postMultiAuthorized(dispatch, `/challenges/${challengeId}/tasks/${taskStatus.taskId}/taskStatus`, taskStatus, jwtTokens);
}

export function updateTaskProgress(dispatch, challengeId: number, taskProgress: TaskProgressDTO, jwtToken: string): Promise<TaskProgressDTO> {
    return baseWebCall.postAuthorizedCustom(dispatch, `/challenges/${challengeId}/tasks/${taskProgress.taskId}/taskProgress`, taskProgress, jwtToken);
}

export function closeTask(dispatch, challengeId: number, task: TaskDTO, jwtToken: string): Promise<TaskDTO> {
    return baseWebCall.putAuthorizedCustom(dispatch, `/challenges/${challengeId}/tasks/${task.id}/close`,null, jwtToken);
}
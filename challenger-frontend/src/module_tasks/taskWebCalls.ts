import {webCall} from "../logic/WebCall";
import {TaskDTO, TaskApprovalDTO, TaskProgressDTO} from "./TaskDTO";


export function loadTasks(challengeId: number, date: Date): JQueryPromise<Array<TaskDTO>> {
    return webCall.get(`/challenges/${challengeId}/tasks/?day=${date.yy_mm_dd()}`);
}

export function updateTask(task: TaskDTO): JQueryPromise<TaskDTO> {
    return webCall.put(`/challenges/${task.challengeId}/tasks/${task.id}`, task);
}

export function createTask(task: TaskDTO): JQueryPromise<TaskDTO> {
    return webCall.post(`/challenges/${task.challengeId}/tasks/`, task);
}

export function deleteTask(task: TaskDTO): JQueryPromise<void> {
    return webCall.delete(`/challenges/${task.challengeId}/tasks/${task.id}`);
}
export function updateTaskStatus(challengeId: number, taskStatus: TaskApprovalDTO, jwtTokens: Array<String>): JQueryPromise<TaskDTO> {
    return webCall.postMultiAuthorized(`/challenges/${challengeId}/tasks/${taskStatus.taskId}/taskStatus`, taskStatus, jwtTokens);
}

export function updateTaskProgress(challengeId: number, taskProgress: TaskProgressDTO, jwtToken: string): JQueryPromise<TaskProgressDTO> {
    return webCall.postAuthorizedCustom(`/challenges/${challengeId}/tasks/${taskProgress.taskId}/taskProgress`, taskProgress, jwtToken);
}


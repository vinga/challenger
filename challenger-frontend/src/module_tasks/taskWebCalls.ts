import {baseWebCall} from "../logic/WebCall";
import {TaskDTO, TaskApprovalDTO, TaskProgressDTO} from "./TaskDTO";


export function loadTasks(challengeId: number, date: Date): Promise<Array<TaskDTO>> {
    return baseWebCall.get(`/challenges/${challengeId}/tasks/?day=${date.yy_mm_dd()}`);
}

export function updateTask(task: TaskDTO): Promise<TaskDTO> {
    return baseWebCall.put(`/challenges/${task.challengeId}/tasks/${task.id}`, task);
}

export function createTask(task: TaskDTO): Promise<TaskDTO> {
    return baseWebCall.post(`/challenges/${task.challengeId}/tasks/`, task);
}

export function deleteTask(task: TaskDTO): Promise<void> {
    return baseWebCall.delete(`/challenges/${task.challengeId}/tasks/${task.id}`);
}
export function updateTaskStatus(challengeId: number, taskStatus: TaskApprovalDTO, jwtTokens: Array<String>): Promise<TaskDTO> {
    return baseWebCall.postMultiAuthorized(`/challenges/${challengeId}/tasks/${taskStatus.taskId}/taskStatus`, taskStatus, jwtTokens);
}

export function updateTaskProgress(challengeId: number, taskProgress: TaskProgressDTO, jwtToken: string): Promise<TaskProgressDTO> {
    return baseWebCall.postAuthorizedCustom(`/challenges/${challengeId}/tasks/${taskProgress.taskId}/taskProgress`, taskProgress, jwtToken);
}


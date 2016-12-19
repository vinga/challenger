import {baseWebCall} from "../logic/WebCall";
import {TaskDTO, TaskApprovalDTO, TaskProgressDTO} from "./TaskDTO";

// old method
export function loadTasks(challengeId: number, date: Date): Promise<Array<TaskDTO>> {
    return baseWebCall.get(`/challenges/${challengeId}/tasksForDay/?day=${date.yy_mm_dd()}`);
}

export function loadTaskProgresses(challengeId: number, date: Date, loadTasks: boolean): Promise<Array<TaskProgressDTO>> {
    return baseWebCall.get(`/challenges/${challengeId}/taskProgresses/?day=${date.yy_mm_dd()}&loadTasks=${loadTasks}`);
}
export function loadTasksNewWay(challengeId: number, newTaskIds: number[]) : Promise<Array<TaskDTO>> {
    var joined=newTaskIds.join(",")
    return baseWebCall.get(`/challenges/${challengeId}/tasks/?ids=${joined}`);
}

export function updateTask(task: TaskDTO): Promise<TaskDTO> {
    return baseWebCall.put(`/challenges/${task.challengeId}/tasks/${task.id}`, task);
}

export function createTask(task: TaskDTO): Promise<TaskDTO> {
    return baseWebCall.post(`/challenges/${task.challengeId}/tasks/`, task);
}

export function deleteTask(task: TaskDTO, jwtToken: string): Promise<void> {
    return baseWebCall.deleteMultiAuthorized(`/challenges/${task.challengeId}/tasks/${task.id}`,[].concat(jwtToken));
}
export function updateTaskStatus(challengeId: number, taskStatus: TaskApprovalDTO, jwtTokens: Array<String>): Promise<TaskDTO> {
    return baseWebCall.postMultiAuthorized(`/challenges/${challengeId}/tasks/${taskStatus.taskId}/taskStatus`, taskStatus, jwtTokens);
}

export function updateTaskProgress(challengeId: number, taskProgress: TaskProgressDTO, jwtToken: string): Promise<TaskProgressDTO> {
    return baseWebCall.postAuthorizedCustom(`/challenges/${challengeId}/tasks/${taskProgress.taskId}/taskProgress`, taskProgress, jwtToken);
}

export function closeTask(challengeId: number, task: TaskDTO, jwtTokens: Array<String>): Promise<TaskDTO> {
    return baseWebCall.put(`/challenges/${challengeId}/tasks/${task.id}/close`, jwtTokens);
}
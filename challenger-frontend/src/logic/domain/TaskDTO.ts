
export interface TaskDTO {
    label: string,
    dueDate?: number,
    taskType: string,
    icon: string,
    difficulty: number,
    taskStatus: string,
    done: boolean
}
//export interface TaskDTOList extends Array<TaskDTO>{}


export const TaskStatus = {
    waiting_for_acceptance: "waiting_for_acceptance",
    accepted: "accepted",
    rejected: "rejected"
};

export const TaskType = {
    onetime: "onetime",
    daily: "daily",
    weekly: "weekly",
    monthly: "monthly"
}


import {WebState} from "./Common";
import {TaskApprovalDTO} from "./TaskApprovalDTO";
export interface TaskDTO {
    id: number,
    label: string,
    dueDate?: number,
    taskType: string,
    icon: string,
    difficulty: number,
    taskStatus: string,
    done: boolean,
    userId:number;
    createdByUserId:number,
    deleted?: boolean,
    taskApproval?: TaskApprovalDTO;
}


export interface TaskDTOListForDay {
    day: Date;
    taskList: Array<TaskDTO>,
    lastUpdated: Date;
    challengeId: number;
    webState: WebState;
    invalidTasksIds: Array<number>



}




export function createTaskDTOListKey(challengeId: number, day: Date):string {
   return  "" + challengeId + "-" + day.toISOString().slice(0, 10)
}


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

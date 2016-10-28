import {WebState} from "../logic/domain/Common";

export interface TaskDTO {
    id: number,
    challengeId: number,
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
export interface TaskDTOState {
    editedTask?: TaskDTO;
    tasks: Map<string,TaskDTOListForDay>;
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
};

export interface TaskApprovalDTO {
    userId: number,
    taskId: number,
    taskStatus: string,
    rejectionReason?: string


}

export interface TaskProgressDTO {
    taskId: number;
    done: boolean;
    progressTime: number
};

export interface TaskUserDTO {
    id: number,
    label: string,
    login?: string,
    jwtToken?: string

}
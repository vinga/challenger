import {WebState} from "../logic/domain/Common";

export interface TaskDTO {
    id: number,
    challengeId: number,
    label: string,
    dueDate?: number,
    closeDate?: number,
    taskType: string,
    icon: string,
    difficulty: number,
    taskStatus: string,
    done: boolean,
    userId:number;
    createdByUserId:number,
    //deleted?: boolean,
    //taskApproval?: TaskApprovalDTO;
    taskApprovals?: TaskApprovalDTO[];
    monthDays?: string,
    weekDays?: string,


}


export interface TaskDTOListForDay {

    day: Date;
    taskList: Array<TaskDTO>,
    lastUpdated: Date;
    challengeId: number;
    webState: WebState;
    invalidTasksIds: Array<number>,


}
export interface TaskDTOState {
    editedTask?: TaskDTO;

    //OLD WAY
    tasksForDays: TaskForDays;


    taskProgressesForDays?: TaskProgressesForDays
    allTasks: TaskDTOList;

}

export interface TaskDTOList {
    [id: number]:TaskDTO
}
export interface TaskForDays {
    [index: string]:TaskDTOListForDay
}
export interface TaskProgressesForDays {
    [index: string]:TaskProgressDTOListForDay

}
export interface TaskProgressDTOListForDay {
    day: Date;
    challengeId: number;
    webState: WebState;
    taskProgresses: TaskProgressDTO[]
}

export interface TaskProgressForDay {

    taskId: number,
    done: boolean
}


export function createTaskDTOListKey(challengeId: number, day: Date):string {
   return  "t" + challengeId + "t" + day.yyyyxmmxdd();
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
    progressTime: number,
    task?: TaskDTO
};

export interface TaskUserDTO {
    id: number,
    label: string,
    login?: string,
    jwtToken?: string
    challengeStatus : string

}
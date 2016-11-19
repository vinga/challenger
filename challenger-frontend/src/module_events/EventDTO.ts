import {TaskDTO} from "../module_tasks/TaskDTO";
export const EventType = {
    POST:"POST",
    CREATE_TASK:"CREATE_TASK",
    UPDATE_TASK:"UPDATE_TASK",
    ACCEPT_TASK: "ACCEPT_TASK",
    REJECT_TASK:"REJECT_TASK",
    CHECKED_TASK:"CHECKED_TASK",
    UNCHECKED_TASK:"UNCHECKED_TASK",
    DELETE_TASK: "DELETE_TASK"
};



export interface EventDTO {
    id: number,
    challengeId?: number,
    taskId: number,
    content: string,
    sentDate: number,
    forDay: number,
    authorId: number,
    eventType: string,
    readDate?: number; // if message has been read for that user
}

export interface EventGroupDTO {
    challengeId?: number
    taskId?: number
    posts: Array<EventDTO>,
    maxEventId?: number
}

export interface EventState {
    eventWindowVisible: boolean,
    expandedEventWindow: boolean,
    eventGroups: Array<EventGroupDTO>,
    selectedTask: TaskDTO,
    selectedNo: number
}


export interface DisplayedEventUI {
    kind: string
    id: number,
    authorId: number,
    authorOrdinal: number,
    authorLabel: string,
    postContent: string,
    eventType: string
    isNew: boolean,
    readDate?: Date,
    sentDate: Date,
    task?: TaskDTO
}
export interface DateDiscrimUI {
    kind: 'DateDiscrimUI'
    date: Date
    id: number,
    title: string
}
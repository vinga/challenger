import {TaskDTO} from "../module_tasks/TaskDTO";
export const EventType = {
    POST:"POST",
    CREATE_TASK:"CREATE_TASK",
    UPDATE_TASK:"UPDATE_TASK",
    ACCEPT_TASK: "ACCEPT_TASK",
    REJECT_TASK:"REJECT_TASK",
    CHECKED_TASK:"CHECKED_TASK",
    UNCHECKED_TASK:"UNCHECKED_TASK",
    DELETE_TASK: "DELETE_TASK",
    CLOSE_TASK: "CLOSE_TASK",
    ACCEPT_CHALLENGE: "ACCEPT_CHALLENGE",
    REJECT_CHALLENGE: "REJECT_CHALLENGE",
    REMOVE_CHALLENGE: "REMOVE_CHALLENGE",
    UPDATE_CHALLENGE: "UPDATE_CHALLENGE",
    INVITE_USER_TO_CHALLENGE: "INVITE_USER_TO_CHALLENGE",
    REMOVE_USER_FROM_CHALLENGE: "REMOVE_USER_FROM_CHALLENGE",
    REMOVE_ME_FROM_CHALLENGE: "REMOVE_ME_FROM_CHALLENGE"

};



export interface EventDTO {
    id: number,
    eventReadId: number,
    challengeId?: number,
    taskId?: number,
    content: string,
    sentDate: number,
    forDay: number,
    authorId: number,
    eventType: string,
    readDate?: number, // if message has been read for that user
    affectedUserId?: number;
}

export interface EventGroupDTO {
    challengeId?: number
    taskId?: number
    events: Array<EventDTO>,
    maxTotalEventReadId? : number
}

export interface EventState {
    seed: number,
    eventWindowVisible: boolean,
    expandedEventWindow: boolean,
    eventGroups: Array<EventGroupDTO>,

    maxTotalEventReadId?: number,
    selectedTask: TaskDTO,
    selectedNo: number,
    eventActionsVisible: boolean,


    globalUnreadEvents: Array<EventDTO>
    unreadNotifications: UnreadNotificationsList

    globalEventsVisible: boolean
}




export interface UnreadNotificationsList {
    [challengeId: number]: Array<Number> // array of event Ids
}


export interface DisplayedEventUI {
    kind: string,
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
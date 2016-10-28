export const EventType = {
    POST:"POST",
    CREATE_TASK:"CREATE_TASK",
    UPDATE_TASK:"UPDATE_TASK",
    REJECT_TASK:"REJECT_TASK",
    CHECKED_TASK:"CHECKED_TASK",
    UNCHECKED_TASK:"UNCHECKED_TASK",
};

export interface EventDTO {
    id: number,
    challengeId?: number,
    taskId: number,
    content: string,
    sentDate: number,
    authorId: number,
    eventType: string
}

export interface EventGroupDTO {
    challengeId?: number
    taskId?: number
    posts: Array<EventDTO>
}

export interface EventState {
    eventWindowVisible: boolean,
    expandedEventWindow: boolean,
    eventGroups: Array<EventGroupDTO>
}
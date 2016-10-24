import {EventDTO} from "./EventDTO";
export interface EventGroupDTO {
    challengeId?: number
    taskId?: number
    posts: Array<EventDTO>
}
import {PostDTO} from "./PostDTO";
export interface ConversationDTO {
    taskId?: number
    posts: Array<PostDTO>
}
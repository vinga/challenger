
import {VisibleChallengesDTO} from "../logic/domain/ChallengeDTO";
import {TaskDTOListForDay, TaskDTO} from "../logic/domain/TaskDTO";
import {AccountDTO} from "../logic/domain/AccountDTO";
import {ConversationDTO} from "../logic/domain/ConversationDTO";

export interface CurrentSelection {
    day: Date,
    editedTask?: TaskDTO;
    userId?:number;
    displayedConversation?:ConversationDTO;
}
export interface ReduxState {
    challenges: VisibleChallengesDTO,
    tasks: Map<string,TaskDTOListForDay>,
    users:Array<AccountDTO>,
    currentSelection: CurrentSelection
}




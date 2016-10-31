import {Selector, createSelector} from "reselect";
import {ReduxState} from "../redux/ReduxState";
import {DisplayedEventUI} from "./components/EventGroupPanel";

import {EventGroupDTO} from "./EventDTO";
import {TaskDTO} from "../module_tasks/TaskDTO";

import {selectedChallengeParticipantsSelector, ChallengeParticipantDTO} from "../module_challenges/index";

const displaySeletectedEventGroupSelector: Selector<ReduxState,EventGroupDTO> = (state: ReduxState): EventGroupDTO =>
    state.eventsState.eventGroups.find(eg=>eg.challengeId == state.challenges.selectedChallengeId)

const displayTaskSelector: Selector<ReduxState,TaskDTO> = (state: ReduxState): TaskDTO => state.eventsState.selectedTask



export const eventsSelector: Selector<ReduxState,Array<DisplayedEventUI>> = createSelector(
    selectedChallengeParticipantsSelector,
    displaySeletectedEventGroupSelector,
    displayTaskSelector,
    (challengeParticipants: Array<ChallengeParticipantDTO>, eventGroups: EventGroupDTO,  filteredTask?: TaskDTO) => {

        if (eventGroups != null)
            return eventGroups.posts.filter(p=> filteredTask==null || p.taskId==filteredTask.id).map(p=> {


                return {
                    id: p.id,
                    authorId: p.authorId,
                    eventType: p.eventType,
                    // maybe should be taken with explicitely spcified challengeId

                    // mode that to selector
                    authorOrdinal: challengeParticipants.find(cp=>cp.id==p.authorId).ordinal,
                    authorLabel:  challengeParticipants.find(cp=>cp.id==p.authorId).label,
                    postContent: p.content
                }
            });

        return [];
    }
)

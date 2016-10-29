import {Selector, createSelector} from "reselect";
import {ReduxState} from "../redux/ReduxState";
import {DisplayedEventUI} from "./components/EventGroupPanel";
import {selectedChallengeIdSelector, challengeUserIndex, challengeUserLabel} from "../module_challenges/challengeSelectors";
import {EventGroupDTO} from "./EventDTO";
import {TaskDTO} from "../module_tasks/TaskDTO";

const displayEventSelector: Selector<ReduxState,Boolean> = (state: ReduxState): Boolean => state.eventsState.eventWindowVisible

const displaySeletectedEventGroupSelector: Selector<ReduxState,EventGroupDTO> = (state: ReduxState): EventGroupDTO =>
    state.eventsState.eventGroups.find(eg=>eg.challengeId == state.challenges.selectedChallengeId)

const displayTaskSelector: Selector<ReduxState,TaskDTO> = (state: ReduxState): TaskDTO => state.eventsState.selectedTask

export const eventsSelector: Selector<ReduxState,Array<DisplayedEventUI>> = createSelector(
    (state: ReduxState) => state,
    displaySeletectedEventGroupSelector,
    displayEventSelector,
    (state: ReduxState): TaskDTO => state.eventsState.selectedTask,
    (state: ReduxState, eventGroups: EventGroupDTO,  displayLog: Boolean, filteredTask?: TaskDTO) => {

        if (eventGroups != null)
            return eventGroups.posts.filter(p=> filteredTask==null || p.taskId==filteredTask.id).map(p=> {


                return {
                    id: p.id,
                    authorId: p.authorId,
                    eventType: p.eventType,
                    // maybe should be taken with explicitely spcified challengeId

                    // mode that to selector
                    authorOrdinal: challengeUserIndex(state, p.authorId),
                    authorLabel: challengeUserLabel(state, p.authorId),
                    postContent: p.content
                }
            });

        return [];
    }
)

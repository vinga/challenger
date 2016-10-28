import {Selector, createSelector} from "reselect";
import {ReduxState} from "../redux/ReduxState";
import {DisplayedEventUI} from "./components/EventGroupPanel";
import {selectedChallengeIdSelector, challengeUserIndex, challengeUserLabel} from "../module_challenges/challengeSelectors";

const displayEventSelector: Selector<ReduxState,Boolean> = (state: ReduxState): Boolean => state.eventsState.eventWindowVisible


export const eventsSelector: Selector<ReduxState,Array<DisplayedEventUI>> = createSelector(
    (state: ReduxState)=>state,
    selectedChallengeIdSelector,
    displayEventSelector,
    (state: ReduxState, challengeId: number, displayLog: Boolean) => {


        var eventGroups = state.eventsState.eventGroups.find(eg=>eg.challengeId == challengeId);
        if (eventGroups != null)
            return eventGroups.posts.map(p=> {


                return {
                    id: p.id,
                    authorId: p.authorId,
                    eventType: p.eventType,
                    // maybe should be taken with explicitely spcified challengeId
                    authorOrdinal: challengeUserIndex(state, p.authorId),
                    authorLabel: challengeUserLabel(state, p.authorId),
                    postContent: p.content
                }
            });

        return [];
    }
)

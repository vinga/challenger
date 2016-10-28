import {EXPAND_EVENTS_WINDOW, WEB_EVENTS_RESPONSE, ADD_NEW_EVENT_OPTIMISTIC} from "./eventActionTypes";
import {copy, copyAndReplace} from "../redux/ReduxState";
import {isAction} from "../redux/ReduxTask";
import {EventState, EventDTO, EventGroupDTO} from "./EventDTO";

const getInitialState = (): EventState => {
    return {
        eventWindowVisible: true,
        expandedEventWindow: false,
        eventGroups: new Array<EventGroupDTO>()
    }
}

export function eventsState(state: EventState = getInitialState(), action): EventState {
    if (isAction(action, 'LOGOUT')) {
        return getInitialState();
    }
    if (isAction(action, EXPAND_EVENTS_WINDOW)) {
        return copy(state).and({
            expandedEventWindow: action.expanded
        })
    } else if (isAction(action, WEB_EVENTS_RESPONSE)) {

        var posts: Array<EventDTO> = action.posts;
        posts.sort((p1, p2)=> -(p2.sentDate - p1.sentDate))


        var eventGroups = copyAndReplace(state.eventGroups, action, eg=>eg.challengeId == action.challengeId);


        var res = copy(state).and({eventGroups: eventGroups});


        return res;
    } else if (isAction(action, ADD_NEW_EVENT_OPTIMISTIC)) {

        var eg: EventGroupDTO = state.eventGroups.find(eg=>eg.challengeId == action.challengeId);
        eg = eventGroup(eg, action);
        return copy(state).and({eventGroups: copyAndReplace(state.eventGroups, eg, eg=>eg.challengeId == action.challengeId)});

    }
    return state;
}

function eventGroup(state: EventGroupDTO, action): EventGroupDTO {

    if (isAction(action, ADD_NEW_EVENT_OPTIMISTIC)) {
        var temp;
        if (state==null)
            temp={posts:[]}
        else temp=state;
        var newPosts = temp.posts.concat(action);
        return Object.assign({}, temp, {posts: newPosts});
    }
    return state;
}
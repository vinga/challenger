import {EXPAND_EVENTS_WINDOW, WEB_EVENTS_RESPONSE, ADD_NEW_EVENT_OPTIMISTIC, SHOW_TASK_EVENTS, WEB_ASYNC_EVENT_RESPONSE, MARK_EVENT_AS_READ_OPTIMISTIC} from "./eventActionTypes";
import {copy, copyAndReplace} from "../redux/ReduxState";
import {isAction} from "../redux/ReduxTask";
import {EventState, EventDTO, EventGroupDTO} from "./EventDTO";
import _ = require("lodash");

const getInitialState = (): EventState => {
    return {
        eventWindowVisible: true,
        expandedEventWindow: false,
        eventGroups: new Array<EventGroupDTO>(),
        selectedTask: null,
        selectedNo: null
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

    } else if (isAction(action, SHOW_TASK_EVENTS)) {
        return copy(state).and({
            selectedTask: action.task,
            selectedNo: action.no,
        })
    } else if (isAction(action, WEB_ASYNC_EVENT_RESPONSE)) {
        var posts: Array<EventDTO> = action.events;

        var newState = state;






        posts.map(p=> {
            var eg: EventGroupDTO = state.eventGroups.find(eg=>eg.challengeId == p.challengeId);

            var newPosts = eg.posts.filter(po=>po.id > 0 && po.id != p.id).concat(p); // replace old ones with new

            var maxEventId=eg.maxEventId;
            if (newPosts.length>0) {
                maxEventId= _.maxBy(newPosts, it => it.id).id;

            }

            eg = Object.assign({}, eg, {posts: newPosts, maxEventId: maxEventId});


            newState = copy(newState).and({eventGroups: copyAndReplace(state.eventGroups, eg, eg=>eg.challengeId == p.challengeId)});
        })

        return newState

    } else if (isAction(action, MARK_EVENT_AS_READ_OPTIMISTIC)) {

        var eg: EventGroupDTO = state.eventGroups.find(eg=>eg.challengeId == action.challengeId);
        eg = Object.assign({}, eg, {
            posts: eg.posts.map(p=> {
                    if (p.id == action.eventId) {
                        return Object.assign({}, p, {readDate: action.readDate})
                    } else return p;
                }
            )
        });
        return copy(state).and({eventGroups: copyAndReplace(state.eventGroups, eg, eg=>eg.challengeId == action.challengeId)});
    }
    return state;
}

function eventGroup(state: EventGroupDTO = {posts: []}, action): EventGroupDTO {

    if (isAction(action, ADD_NEW_EVENT_OPTIMISTIC)) {
        var newPosts = state.posts.concat(action);
        return Object.assign({}, state, {posts: newPosts});
    }
    return state;
}
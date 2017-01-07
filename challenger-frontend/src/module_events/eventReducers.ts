import {
    EXPAND_EVENTS_WINDOW,
    WEB_EVENTS_RESPONSE,
    ADD_NEW_EVENT_OPTIMISTIC,
    SHOW_TASK_EVENTS,
    WEB_ASYNC_EVENT_RESPONSE,
    MARK_EVENT_AS_READ_OPTIMISTIC,
    TOGGLE_EVENT_ACTIONS_VISIBILITY,
    ADD_TO_UNREAD_NOTIFICATIONS,
    CLEAR_UNREAD_NOTIFICATIONS
} from "./eventActionTypes";
import {copy, copyAndReplace} from "../redux/ReduxState";
import {isAction} from "../redux/ReduxTask";
import {EventState, EventDTO, EventGroupDTO, UnreadNotificationsList} from "./EventDTO";
import _ = require("lodash");
import path = require("immutable-path");

const getInitialState = (): EventState => {
    return {
        eventWindowVisible: true,
        expandedEventWindow: false,
        eventGroups: new Array<EventGroupDTO>(),
        selectedTask: null,
        selectedNo: null,
        eventActionsVisible: false,
        unreadNotifications: {}
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


        return copy(state).and({eventGroups: eventGroups});


    } else if (isAction(action, ADD_NEW_EVENT_OPTIMISTIC)) {

        var eg: EventGroupDTO = state.eventGroups.find(eg=>eg.challengeId == action.challengeId);
        eg = eventGroup(eg, action);
        return copy(state).and({eventGroups: copyAndReplace(state.eventGroups, eg, eg=>eg.challengeId == action.challengeId)});

    } else if (isAction(action, SHOW_TASK_EVENTS)) {

        if (action.toggle) {
            if (state.selectedTask != null && action.task != null && state.selectedTask.id == action.task.id) {
                //just clear
                return copy(state).and({
                    selectedTask: null,
                    selectedNo: null
                })
            }
        }
        return copy(state).and({
            selectedTask: action.task,
            selectedNo: action.no,
        })
    } else if (isAction(action, WEB_ASYNC_EVENT_RESPONSE)) {
        var posts: Array<EventDTO> = action.events;

        var newState = state;


        posts.map(p=> {
            var eg: EventGroupDTO = state.eventGroups.find(eg=>eg.challengeId == p.challengeId);

            if (eg==null) {
                eg = {challengeId: p.challengeId, posts: []}
            }
            var newPosts = eg.posts.filter(po=>po.id > 0 && po.id != p.id).concat(p); // replace old ones with new

            var maxEventId = state.maxEventId
            if (newPosts.length > 0) {
                maxEventId = _.maxBy(newPosts, it => it.id).id;

            }

            eg = Object.assign({}, eg, {posts: newPosts});


            newState = copy(newState).and({
                maxEventId: maxEventId,
                eventGroups: copyAndReplace(state.eventGroups, eg, eg=>eg.challengeId == p.challengeId)});
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
    } else if (isAction(action, TOGGLE_EVENT_ACTIONS_VISIBILITY)) {
        return Object.assign({}, state, {eventActionsVisible: !state.eventActionsVisible});
    } else if (isAction(action, ADD_TO_UNREAD_NOTIFICATIONS)) {
        var current = state.unreadNotifications[action.challengeId];
        var newNotifications: UnreadNotificationsList = Object.assign({}, state.unreadNotifications);
        newNotifications[action.challengeId] = current == null ? 1 : current + 1;

        var newState = Object.assign({}, state, {unreadNotifications: newNotifications}) as EventState;

        //var newState= Object.assign({},state,{unreadNotifs: {...this.state.unreadNotifs, [action.challengeId]: newUnread}} )

        return newState;
    } else if (isAction(action, CLEAR_UNREAD_NOTIFICATIONS)) {
        var newNotifications: UnreadNotificationsList = Object.assign({}, state.unreadNotifications);
        newNotifications[action.challengeId] = 0;
        return Object.assign({}, state, {unreadNotifications: newNotifications});
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
import {
    EXPAND_EVENTS_WINDOW,
    WEB_EVENTS_RESPONSE,
    ADD_NEW_EVENT_OPTIMISTIC,
    SHOW_TASK_EVENTS,
    WEB_ASYNC_EVENT_RESPONSE,
    MARK_EVENT_AS_READ_OPTIMISTIC,
    TOGGLE_EVENT_ACTIONS_VISIBILITY,
    ADD_TO_UNREAD_NOTIFICATIONS,
    CLEAR_UNREAD_NOTIFICATIONS,
    SHOW_GLOBAL_NOTIFICATIONS_DIALOG
} from "./eventActionTypes";
import {copy, copyAndReplace} from "../redux/ReduxState";
import {isAction} from "../redux/ReduxTask";
import {EventState, EventDTO, EventGroupDTO, UnreadNotificationsList, EventType} from "./EventDTO";
import _ = require("lodash");
import path = require("immutable-path");

const getInitialState = (): EventState => {
    return {
        eventWindowVisible: true,
        expandedEventWindow: false,
        eventGroups: new Array<EventGroupDTO>(),
        globalUnreadEvents: [],
        selectedTask: null,
        selectedNo: null,
        eventActionsVisible: false,
        unreadNotifications: {},
        globalEventsVisible: false

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

        // update max event id
        var maxEventId = state.maxEventId
        if (posts.length > 0) {
            var temp = Math.max(_.maxBy(posts, it => it.id).id);
            if (temp > maxEventId || maxEventId == null) {
                maxEventId = temp;
            }



            //remove events that are global
            var incomingChallengeEvents = action.events.filter(e=> e.eventType != EventType.REMOVE_CHALLENGE)
            incomingChallengeEvents.forEach(p=> {
                var eg: EventGroupDTO = state.eventGroups.find(eg=>eg.challengeId == p.challengeId);
                if (eg == null) {
                    eg = {challengeId: p.challengeId, posts: []}
                }
                var newPosts = eg.posts.filter(po=>po.id > 0 && po.id != p.id).concat(p); // replace old ones with new

                eg = Object.assign({}, eg, {posts: newPosts});


                newState = copy(newState).and({

                    eventGroups: copyAndReplace(state.eventGroups, eg, eg=>eg.challengeId == p.challengeId)
                });
            })



            var incomingGlobalEvents = action.events.filter(e=> e.eventType == EventType.REMOVE_CHALLENGE);
            if (incomingGlobalEvents.length>0) {
                var ie=_.differenceBy(newState.globalUnreadEvents, incomingGlobalEvents, 'id').concat(incomingGlobalEvents);
                newState = Object.assign({}, newState,
                    {
                        globalUnreadEvents: ie
                    }
                );
            }

            newState = Object.assign({}, newState,
                {
                    maxEventId: maxEventId,
                }
            );


        }
        return newState

    } else if (isAction(action, MARK_EVENT_AS_READ_OPTIMISTIC)) {

        var eg: EventGroupDTO = state.eventGroups.find(eg=>eg.challengeId == action.challengeId);
        var newState: EventState;
        if (eg != null) {
            eg = Object.assign({}, eg, {
                posts: eg.posts.map(p=> {
                        if (p.id == action.eventId) {
                            return Object.assign({}, p, {readDate: action.readDate})
                        } else return p;
                    }
                )
            });
            newState = copy(state).and(
                {
                    eventGroups: copyAndReplace(state.eventGroups, eg, eg=>eg.challengeId == action.challengeId)
                });
        } else
            newState = state;


        var current = state.unreadNotifications[action.challengeId];
        var newNotifications: UnreadNotificationsList = Object.assign({}, state.unreadNotifications);
        newNotifications[action.challengeId] = current == null ? [] : _.without(current, action.eventId);

        newState = Object.assign({}, state, {unreadNotifications: newNotifications}) as EventState;

        return copy(newState).and(
            {
                globalUnreadEvents: state.globalUnreadEvents.filter(e=>e.id != action.eventId)
            });
    } else if (isAction(action, TOGGLE_EVENT_ACTIONS_VISIBILITY)) {

        return Object.assign({}, state, {eventActionsVisible: !state.eventActionsVisible});

    } else if (isAction(action, ADD_TO_UNREAD_NOTIFICATIONS)) {

        var newState=state;
        action.events.forEach((e:EventDTO)=> {
            var newNotifications: UnreadNotificationsList = Object.assign({}, state.unreadNotifications);
            var currentChallengeNotifications = state.unreadNotifications[e.challengeId];
            if (currentChallengeNotifications==null)
                currentChallengeNotifications=[];
            newNotifications[e.challengeId] = _.union(currentChallengeNotifications, [e.id]);
            newState = Object.assign({}, state, {unreadNotifications: newNotifications}) as EventState;
        })
        //var newState= Object.assign({},state,{unreadNotifs: {...this.state.unreadNotifs, [action.challengeId]: newUnread}} )
        return newState;

    } else if (isAction(action, CLEAR_UNREAD_NOTIFICATIONS)) {

        var newNotifications: UnreadNotificationsList = Object.assign({}, state.unreadNotifications);
        newNotifications[action.challengeId] = [];
        return Object.assign({}, state, {unreadNotifications: newNotifications});

    } else if (isAction(action, SHOW_GLOBAL_NOTIFICATIONS_DIALOG)) {

        return Object.assign({}, state, {globalEventsVisible: action.show});

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
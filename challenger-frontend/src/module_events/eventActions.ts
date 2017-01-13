import {ReduxState} from "../redux/ReduxState";
import {
    ADD_NEW_EVENT_OPTIMISTIC,
    WEB_EVENTS_RESPONSE,
    WEB_ASYNC_EVENT_RESPONSE,
    MARK_EVENT_AS_READ_OPTIMISTIC,
    ADD_TO_UNREAD_NOTIFICATIONS,
    CLEAR_UNREAD_NOTIFICATIONS,
} from "./eventActionTypes";
import * as webCall from "./eventWebCalls";
import {EventDTO, EventType, EventGroupDTO} from "./EventDTO";
import {WEB_STATUS_NOTHING_RETURNED_YET} from "../logic/domain/Common";
import {loadTasksNewWay, fetchTasksWhenNeededAfterDelay, fetchTasksProgresses} from "../module_tasks/taskActions";
import {downloadProgressiveReports} from "../module_reports/reportActions";
import {MARK_ALL_CHALLENGE_TASKS_PROGRESSES_AS_INVALID, DELETE_TASKS_REMOTE, MARK_TASK_DONE_UNDONE_REMOTE} from "../module_tasks/taskActionTypes";
import {NO_CHALLENGES_LOADED_YET} from "../module_challenges/ChallengeDTO";
import {fetchWebChallengesNoReload} from "../module_challenges/challengeActions";
import _ = require("lodash");
import {loggedUserSelector} from "../module_accounts/accountSelectors";


export function sendEvent(authorId: number, content: string) {
    return function (dispatch, getState: ()=>ReduxState) {
        var taskId = getState().eventsState.selectedTask != null ?
            getState().eventsState.selectedTask.id :
            null;

        var eventDTO: EventDTO = {
            id: -new Date().getTime(),
            forDay: new Date().getTime(),
            challengeId: getState().challenges.selectedChallengeId,
            taskId: taskId,
            content: content,
            authorId: authorId,
            sentDate: new Date().getTime(),
            eventType: EventType.POST
        }

        //dispatch(loadEventsAsyncAllTheTime());


        dispatch(ADD_NEW_EVENT_OPTIMISTIC.new(eventDTO));

        webCall.sendEventWithAuthFailure(dispatch, eventDTO, content == "ERR").then(
            (eventDTO: EventDTO) => {
                //loadEventsAsyncAllTheTime should handle this case
                // dispatch(fetchEvents(getState().challenges.selectedChallengeId));
            }
        );


    }
}


export function loadEventsAsyncAllTheTime() {
    return function (dispatch, getState: ()=>ReduxState) {
        var state = getState();
        var challengeId = state.challenges.selectedChallengeId;


        var maxEventId = state.eventsState.maxEventId;// eventGroup != null ? eventGroup.maxEventId : null;

        if (challengeId != null && challengeId != NO_CHALLENGES_LOADED_YET ) {
            console.log("loadEventsAsyncAllTheTime " + maxEventId );//+ ", challengeId=" + challengeId+" loggedUser: "+loggedUserSelector(state).label);

            //mozemy robic polling czy jest cos nowego i pobierac wszystkie oczekujace eventy
            webCall.loadEventsForChallengeAsync(dispatch, challengeId, maxEventId).then(
                (events: EventDTO[])=> {
                    dispatch(WEB_ASYNC_EVENT_RESPONSE.new({events}))

                    // update UI based on events
                    // Jak przekalkulowac czy task progress ma sie wyswietlic? prosciej bedzie na nowo wczytywac czy recznie kalkulowac?

                    events.map(e=>
                        console.log("@@@received event " + e.eventType +", "+e.id+" new: "+(e.readDate==null)));


                    var selectedChallengeEvents = events.filter(e=>e.challengeId == challengeId);
                    var otherChallengeEvents = events.filter(e=>e.challengeId != challengeId);
                    otherChallengeEvents.forEach(e=> {
                        // just mark them as new
                        dispatch(ADD_TO_UNREAD_NOTIFICATIONS.new({challengeId: e.challengeId}));
                    });



                    // refresh who accepted & who rejected
                    events.filter(e=> [
                        EventType.REJECT_CHALLENGE,
                        EventType.ACCEPT_CHALLENGE,
                        EventType.REMOVE_CHALLENGE,
                        EventType.UPDATE_CHALLENGE,
                        EventType.INVITE_USER_TO_CHALLENGE,
                        EventType.REMOVE_USER_FROM_CHALLENGE].contains(e.eventType)).forEach(
                        e=> {
                            dispatch(fetchWebChallengesNoReload());
                        }
                    );

                    // dispatch global events, and remove them from automatic read
                    events.filter(e=>e.eventType == EventType.REMOVE_CHALLENGE).forEach(
                        e=> {
                            _.pull(selectedChallengeEvents, e);
                        }
                    );
                    processInvomingSelectedChallengeEvents(selectedChallengeEvents, challengeId, dispatch, getState);


                    // no need to display such event for user
                    // actions where I am creator should be marked as read, maybe even optimistic?
                   // var deletedEvents=otherChallengeEvents.filter(e=>e.readDate==null && e.eventType==EventType.REMOVE_CHALLENGE && e.authorId==loggedUserSelector(state).id);

                    //loggedUserCreatedThisEvent but could do it on other machine
                    var loggedUserCreatedThisEvent=events.filter(e=>e.readDate==null && e.authorId==loggedUserSelector(state).id);
                    var promises = selectedChallengeEvents.concat(loggedUserCreatedThisEvent).filter(e=>e.readDate == null).map(e=>
                        dispatch(markAsRead(e.challengeId, e.id))
                    )

                    return Promise.all(promises);
                }
            ).then(()=> {
                dispatch(loadEventsAsyncAllTheTime());
            })
                .catch((reason: any)=> {
                    console.log(reason);
                    if (reason.status == WEB_STATUS_NOTHING_RETURNED_YET) {
                        // this means no message arrived so far, but it is not an error, we need to retry call
                        dispatch(loadEventsAsyncAllTheTime());
                    } else {
                        console.log("PERHAPS WE WANT TO LOGOUT HERE",reason);
                        setTimeout(()=> {
                            dispatch(loadEventsAsyncAllTheTime());
                            // other problem, wait 10 seconds and retry
                        }, 10000)
                    }
                });
        } else {
            setTimeout(()=> {
                dispatch(loadEventsAsyncAllTheTime());
                // other problem, wait 10 seconds and retry
            }, 10000)
        }
    }

}


function processInvomingSelectedChallengeEvents(selectedChallengeEvents: EventDTO[], challengeId: number, dispatch, getState: ()=>ReduxState) {
    selectedChallengeEvents.filter(e=>e.challengeId == challengeId).filter(e=>e.eventType == EventType.CHECKED_TASK || e.eventType == EventType.UNCHECKED_TASK).forEach(
        e=> {
            var done = e.eventType == EventType.CHECKED_TASK;
            var taskId = e.taskId;
            var forDay = new Date(e.forDay)
            var challengeId = e.challengeId;
            dispatch(MARK_TASK_DONE_UNDONE_REMOTE.new({challengeId, taskId, done, forDay}));
        }
    )

    var taskIdsToDelete = selectedChallengeEvents.filter(e=>e.eventType == EventType.DELETE_TASK).map(e=>e.taskId);
    dispatch(DELETE_TASKS_REMOTE.new({taskIdsToDelete}));



/*
    _(selectedChallengeEvents)
        .keyBy('eventType') // or .indexBy() if using lodash 3.x
        .at(
            EventType.ACCEPT_TASK,
            EventType.REJECT_TASK,
            EventType.CREATE_TASK,
            EventType.UPDATE_TASK,
            EventType.CLOSE_TASK)
        .value(); */

    var taskIdsToUpdate = selectedChallengeEvents.filter(e=>[
        EventType.ACCEPT_TASK,
        EventType.REJECT_TASK,
        EventType.CREATE_TASK,
        EventType.UPDATE_TASK,
        EventType.CLOSE_TASK].contains(e.eventType)).map(e=>e.taskId);
    dispatch(loadTasksNewWay(challengeId, taskIdsToUpdate));


    //TODO we may narrow it for custom users
    var actionsThatMayAlteredProgressiveReports = selectedChallengeEvents.some(e=>
        (e.eventType == EventType.ACCEPT_TASK ||
        e.eventType == EventType.DELETE_TASK ||
        e.eventType == EventType.CHECKED_TASK ||
        e.eventType == EventType.UNCHECKED_TASK))
    if (actionsThatMayAlteredProgressiveReports)
        dispatch(downloadProgressiveReports(challengeId));


    // actions below dosn't have impact on visibility, so no need to mark as invalid
    if (!selectedChallengeEvents.every(e=>e.eventType == EventType.POST
        || e.eventType == EventType.ACCEPT_TASK
        || e.eventType == EventType.REJECT_TASK
        || e.eventType == EventType.DELETE_TASK
        )) {
        // TODO daily task will also no affect visibility
        dispatch(MARK_ALL_CHALLENGE_TASKS_PROGRESSES_AS_INVALID.new({challengeId: challengeId}));
        console.log("FETCH TASKS WHEN NEEDED");
        dispatch(fetchTasksProgresses(challengeId, getState().currentSelection.day));
        dispatch(loadTasksNewWay(challengeId, selectedChallengeEvents.map(e=>e.taskId)));
        dispatch(fetchTasksWhenNeededAfterDelay(challengeId, getState().currentSelection.day.addDays(1), 50));
        dispatch(fetchTasksWhenNeededAfterDelay(challengeId, getState().currentSelection.day.addDays(-1), 500));

    }
}

export function markGlobalEventsAsRead(events: EventDTO[]) {
    return function (dispatch, getState: ()=>ReduxState) {
        var promises = events.filter(e=>e.readDate == null).map(e=>
            dispatch(markAsRead(e.challengeId, e.id))
        )
        return Promise.all(promises);
    }
}
function markAsRead(challengeId, eventId) {
    return function (dispatch, getState: ()=>ReduxState) {
        var readDate = new Date().getTime();
        dispatch(MARK_EVENT_AS_READ_OPTIMISTIC.new({challengeId, eventId, readDate}))
        var p = webCall.markEventRead(dispatch, challengeId, eventId, readDate);
        return p;
    }
}


export function fetchInitialEvents(challengeId: number) {
    return function (dispatch) {
        dispatch(CLEAR_UNREAD_NOTIFICATIONS.new({challengeId}));
        webCall.loadEventsForChallenge(dispatch, challengeId).then(
            (challengeConversation: EventGroupDTO)=> dispatch(WEB_EVENTS_RESPONSE.new(challengeConversation))
        );
    }
}


export function showTaskEvents(challengeId: number, taskId: number) {
    return function (dispatch) {
        webCall.loadEventsForTask(dispatch, challengeId, taskId).then(
            (eventGroup: EventGroupDTO)=> dispatch(WEB_EVENTS_RESPONSE.new(eventGroup))
        );
    }
}
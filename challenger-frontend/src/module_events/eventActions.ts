import {ReduxState} from "../redux/ReduxState";
import {
    ADD_NEW_EVENT_OPTIMISTIC,
    WEB_EVENTS_RESPONSE,
    WEB_ASYNC_EVENT_RESPONSE,
    MARK_EVENT_AS_READ_OPTIMISTIC,
    ADD_TO_UNREAD_NOTIFICATIONS,
    CLEAR_UNREAD_NOTIFICATIONS
} from "./eventActionTypes";
import * as webCall from "./eventWebCalls";
import {EventDTO, EventType, EventGroupDTO} from "./EventDTO";
import {WEB_STATUS_NOTHING_RETURNED_YET} from "../logic/domain/Common";
import {loadTasksNewWay, fetchTasks, fetchTasksWhenNeededAfterDelay} from "../module_tasks/taskActions";
import {downloadProgressiveReports} from "../module_reports/reportActions";
import {MARK_ALL_CHALLENGE_TASKS_PROGRESSES_AS_INVALID, DELETE_TASKS_REMOTE, MARK_TASK_DONE_UNDONE_REMOTE} from "../module_tasks/taskActionTypes";
import _ = require("lodash");
import {NO_CHALLENGES_LOADED_YET} from "../module_challenges/ChallengeDTO";
import {fetchWebChallengesNoReload} from "../module_challenges/challengeActions";


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

        if (challengeId != null && challengeId != NO_CHALLENGES_LOADED_YET) {
            console.log("loadEventsAsyncAllTheTime " + maxEventId+", challengeId="+challengeId);

            webCall.loadEventsForChallengeAsync(dispatch, challengeId, maxEventId).then(
                (events: EventDTO[])=> {
                    dispatch(WEB_ASYNC_EVENT_RESPONSE.new({events}))

                    // update UI based on events
                    // Jak przekalkulowac czy task progress ma sie wyswietlic? prosciej bedzie na nowo wczytywac czy recznie kalkulowac?

                    events.map(e=>
                        console.log("received event " + e.eventType));


                    var selectedChallengeEvents = events.filter(e=>e.challengeId == challengeId);
                    var otherChallengeEvents = events.filter(e=>e.challengeId!=challengeId);
                    otherChallengeEvents.forEach(e=> {
                        // just mark then as new
                        dispatch(ADD_TO_UNREAD_NOTIFICATIONS.new({challengeId: e.challengeId}));
                    });

                    selectedChallengeEvents.filter(e=>e.challengeId == challengeId).filter(e=>e.eventType == EventType.CHECKED_TASK || e.eventType == EventType.UNCHECKED_TASK).forEach(
                        e=> {
                            var done = e.eventType == EventType.CHECKED_TASK;
                            var taskId = e.taskId;
                            var forDay = new Date(e.forDay)
                            var challengeId = e.challengeId;
                            dispatch(MARK_TASK_DONE_UNDONE_REMOTE.new({challengeId, taskId, done, forDay}));
                        }
                    )

                    // refresh who accepted & who rejected
                    events.filter(e=>e.eventType == EventType.REJECT_CHALLENGE || e.eventType == EventType.ACCEPT_CHALLENGE).forEach(
                        e=>{
                            dispatch(fetchWebChallengesNoReload());
                        }
                    );


                    var taskIdsToDelete = selectedChallengeEvents.filter(e=>e.eventType == EventType.DELETE_TASK).map(e=>e.taskId);
                    dispatch(DELETE_TASKS_REMOTE.new({taskIdsToDelete}));


                    var taskIdsToUpdate = selectedChallengeEvents.filter(e=>
                        (e.eventType == EventType.ACCEPT_TASK ||
                        e.eventType == EventType.REJECT_TASK ||
                        e.eventType == EventType.CREATE_TASK ||
                        e.eventType == EventType.UPDATE_TASK ||
                        e.eventType == EventType.CLOSE_TASK)).map(e=>e.taskId);
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
                    if (!selectedChallengeEvents.every(e=>e.eventType == EventType.POST || e.eventType == EventType.ACCEPT_TASK || e.eventType == EventType.REJECT_TASK || e.eventType == EventType.DELETE_TASK)) {
                        // TODO daily task will also no affect visibility
                        dispatch(MARK_ALL_CHALLENGE_TASKS_PROGRESSES_AS_INVALID.new({challengeId: challengeId}));
                        console.log("FETCH TASKS WHEN NEEDED");
                        dispatch(fetchTasks(challengeId, getState().currentSelection.day));
                        dispatch(fetchTasksWhenNeededAfterDelay(challengeId, getState().currentSelection.day.addDays(1), 50));
                        dispatch(fetchTasksWhenNeededAfterDelay(challengeId, getState().currentSelection.day.addDays(1), 500));

                    }

                    var promises = selectedChallengeEvents.filter(e=>e.readDate == null).map(e=>
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
                        console.log("PERHAPS WE WANT TO LOGOUT HERE");
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

function markAsRead(challengeId, eventId) {
    return function (dispatch, getState: ()=>ReduxState) {
        var readDate = new Date().getTime();
        var p = webCall.markEventRead(dispatch, challengeId, eventId, readDate);
        dispatch(MARK_EVENT_AS_READ_OPTIMISTIC.new({challengeId, eventId, readDate}))
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
import {ReduxState} from "../redux/ReduxState";
import {
    ADD_NEW_EVENT_OPTIMISTIC,
    WEB_EVENTS_SYNC_RESPONSE,
    WEB_EVENTS_ASYNC_RESPONSE,
    MARK_EVENT_AS_READ_OPTIMISTIC,
    CLEAR_UNREAD_NOTIFICATIONS,
    NEW_EVENTS_SEED, WEB_EVENTS_SYNC_RESPONSE_PREVIOUS
} from "./eventActionTypes";
import * as webCall from "./eventWebCalls";
import {EventDTO, EventType, EventGroupDTO, EventGroupSynchDTO} from "./EventDTO";
import {WEB_STATUS_NOTHING_RETURNED_YET} from "../logic/domain/Common";
import {loadTasksNewWay, fetchTasksWhenNeededAfterDelay, fetchTasksProgresses} from "../module_tasks/taskActions";
import {downloadProgressiveReports} from "../module_reports/reportActions";
import {MARK_ALL_CHALLENGE_TASKS_PROGRESSES_AS_INVALID, DELETE_TASKS_REMOTE, MARK_TASK_DONE_UNDONE_REMOTE} from "../module_tasks/taskActionTypes";
import {NO_CHALLENGES_LOADED_YET, NO_ACTIVE_CHALLENGES} from "../module_challenges/ChallengeDTO";
import {fetchWebChallengesNoReload, fetchWebChallenges} from "../module_challenges/challengeActions";
import {loggedUserSelector} from "../module_accounts/accountSelectors";
import _ = require("lodash");
import {displaySeletectedEventGroupSelector} from "./eventSelectors";


export function sendEvent(authorId: number, content: string) {
    return function (dispatch, getState: () => ReduxState) {
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
            eventType: EventType.POST,
            eventReadId: null
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


export function loadEventsAsyncAllTheTimeSingleton() {
    return function (dispatch, getState: () => ReduxState) {

        if (getState().eventsState.seed == null) {
            var seed = new Date().getTime();
            dispatch(NEW_EVENTS_SEED.new({seed}));
            dispatch(loadEventsAsyncAllTheTime(seed));
        }

    }
}

function loadEventsAsyncAllTheTime(seed: number) {

    return function (dispatch, getState: () => ReduxState) {
        var state = getState();


        if (state.eventsState.seed != seed) // this version of function is outdated, user logged out in the meantime
            return;

        var challengeId = state.challenges.selectedChallengeId;
        if (challengeId == null)
            challengeId = NO_ACTIVE_CHALLENGES;
        var maxTotalEventReadId = state.eventsState.maxTotalEventReadId;

        if (challengeId != NO_CHALLENGES_LOADED_YET) {
            console.log("loadEventsAsyncAllTheTime, maxEventId: " + maxTotalEventReadId);

            webCall.loadEventsForChallengeAsync(dispatch, maxTotalEventReadId).then(
                (eventGroup: EventGroupDTO) => {
                    var events=eventGroup.events!=null ? eventGroup.events: [];

                    console.log("maxTotal: "+eventGroup.maxTotalEventReadId);
                    events.map(e => console.log(events.length+" @@@received event " + e.eventType + ", " + e.id + " new: " +
                        (e.readDate == null)));



                    if (events.length==0 && eventGroup.maxTotalEventReadId==null)
                        return Promise.resolve(true);

                    dispatch(WEB_EVENTS_ASYNC_RESPONSE.new({
                        events,
                        loggedUserId: loggedUserSelector(state).id,
                        selectedChallengeId: challengeId,
                        maxTotalEventReadId: eventGroup.maxTotalEventReadId
                    }))


                    // handle global events
                    // we don't have access to those challenges any more, so we are removing all events with that challenge from the list
                    // in order to avoid permission errors when executing service calls
                    events.filter(e =>
                    e.eventType == EventType.REMOVE_CHALLENGE
                    || e.eventType == EventType.REMOVE_ME_FROM_CHALLENGE)
                        .forEach(e => {
                                dispatch(fetchWebChallenges());
                                _.remove(events, e => e.challengeId == e.challengeId)
                                if (e.authorId == loggedUserSelector(state).id) {
                                    // we can still mark these events as read when I am creator. When I am not I want these to show in global notifications panel, so I keep them unread
                                    dispatch(markAsRead(e.challengeId, e.id))
                                }
                            }
                        )


                    var selectedChallengeEvents = events.filter(e => e.challengeId == challengeId);

                    // refresh who accepted & who rejected
                    if (events.filter(e => [
                            EventType.REJECT_CHALLENGE,
                            EventType.ACCEPT_CHALLENGE,
                            EventType.REMOVE_CHALLENGE,
                            EventType.UPDATE_CHALLENGE,
                            EventType.INVITE_USER_TO_CHALLENGE,
                            EventType.REMOVE_USER_FROM_CHALLENGE,
                            EventType.REMOVE_ME_FROM_CHALLENGE].contains(e.eventType)).length > 0)
                        dispatch(fetchWebChallengesNoReload());


                    processInvomingSelectedChallengeEvents(selectedChallengeEvents, challengeId, dispatch, getState);


                    // loggedUserCreatedThisEvent - no need to display as UNREAD events whose I am creator (even if I could do it on other machine)
                    var loggedUserCreatedThisEvent = events.filter(e => e.readDate == null && e.authorId == loggedUserSelector(state).id);

                    selectedChallengeEvents.concat(loggedUserCreatedThisEvent).filter(e => e.readDate == null).map(e =>
                      console.log(e.eventType+" mark as read")
                    );

                    var promises = selectedChallengeEvents.concat(loggedUserCreatedThisEvent).filter(e => e.readDate == null).map(e =>
                        dispatch(markAsRead(e.challengeId, e.id))
                    )
                   return Promise.all(promises) as any ;
                }
            ).then(() => {
                dispatch(loadEventsAsyncAllTheTime(seed));
            })
                .catch((reason: any) => {
                    console.log(reason);
                    if (reason.status == WEB_STATUS_NOTHING_RETURNED_YET) {
                        // this means no message arrived so far, but it is not an error, we need to retry call
                        dispatch(loadEventsAsyncAllTheTime(seed));
                    } else {
                        console.log("PERHAPS WE WANT TO LOGOUT HERE", reason);
                        setTimeout(() => {
                            dispatch(loadEventsAsyncAllTheTime(seed));
                            // other problem, wait 10 seconds and retry
                        }, 10000)
                    }
                });
        } else {
            setTimeout(() => {
                dispatch(loadEventsAsyncAllTheTime(seed));
            }, 1000)
        }
    }

}


function processInvomingSelectedChallengeEvents(selectedChallengeEvents: EventDTO[], challengeId: number, dispatch, getState: () => ReduxState) {
    selectedChallengeEvents.filter(e => [
        EventType.CHECKED_TASK,
        EventType.UNCHECKED_TASK].contains(e.eventType))
        .forEach(
            e => {
                var done = e.eventType == EventType.CHECKED_TASK;
                var taskId = e.taskId;
                var forDay = new Date(e.forDay)
                var challengeId = e.challengeId;
                dispatch(MARK_TASK_DONE_UNDONE_REMOTE.new({challengeId, taskId, done, forDay}));
            }
        )

    var taskIdsToDelete = selectedChallengeEvents.filter(e => e.eventType == EventType.DELETE_TASK).map(e => e.taskId);
    dispatch(DELETE_TASKS_REMOTE.new({taskIdsToDelete}));


    var actionsThatMayAlteredProgressiveReports = selectedChallengeEvents.some(e => [
        EventType.ACCEPT_TASK,
        EventType.DELETE_TASK,
        EventType.CHECKED_TASK,
        EventType.UNCHECKED_TASK].contains(e.eventType)
    )
    if (actionsThatMayAlteredProgressiveReports)
        dispatch(downloadProgressiveReports(challengeId));


    var sbRemovedMeFromChallengeIds = selectedChallengeEvents
        .filter(e => e.eventType == EventType.REMOVE_ME_FROM_CHALLENGE)
        .map(e => e.challengeId)

    // actions below dosn't have impact on visibility, so no need to mark as invalid


    var taskIdsToUpdate = selectedChallengeEvents.filter(e => [
        EventType.ACCEPT_TASK,
        EventType.REJECT_TASK,
        EventType.CREATE_TASK,
        EventType.UPDATE_TASK,
        EventType.CLOSE_TASK,
        EventType.UNCHECKED_TASK, // update is needed, because when task is onetime it becomes unclosed
        EventType.CHECKED_TASK, // update is needed, because when task is onetime it becomes closed
    ].contains(e.eventType)).map(e => e.taskId);


    if (taskIdsToUpdate.length > 0) {
        dispatch(loadTasksNewWay(challengeId, taskIdsToUpdate));


        dispatch(MARK_ALL_CHALLENGE_TASKS_PROGRESSES_AS_INVALID.new({challengeId: challengeId}));
        console.log("FETCH TASKS WHEN NEEDED");
        dispatch(fetchTasksProgresses(challengeId, getState().currentSelection.day));


        var uniqueTaskIds = _.without<number>(_.uniq(selectedChallengeEvents.filter(e => e.taskId != null).map(e => e.taskId)), ...taskIdsToUpdate);
        dispatch(loadTasksNewWay(challengeId, uniqueTaskIds));
        dispatch(fetchTasksWhenNeededAfterDelay(challengeId, getState().currentSelection.day.addDays(1), 50));
        dispatch(fetchTasksWhenNeededAfterDelay(challengeId, getState().currentSelection.day.addDays(-1), 500));
    }


}

export function markGlobalEventsAsRead(events: EventDTO[]) {
    return function (dispatch, getState: () => ReduxState) {
        var promises = events.filter(e => e.readDate == null).map(e =>
            dispatch(markAsRead(e.challengeId, e.id))
        )
        return Promise.all(promises);
    }
}
function markAsRead(challengeId, eventId) {
    return function (dispatch, getState: () => ReduxState) {
        var readDate = new Date().getTime();
        dispatch(MARK_EVENT_AS_READ_OPTIMISTIC.new({challengeId, eventId, readDate}))
        var p = webCall.markEventRead(dispatch, challengeId, eventId, readDate);
        return p;
    }
}





export function showTaskEvents(challengeId: number, taskId: number) {
    return function (dispatch, getState: () => ReduxState) {
        webCall.loadEventsForTask(dispatch, challengeId, taskId).then(
            (eventGroup: EventGroupSynchDTO) => dispatch(WEB_EVENTS_SYNC_RESPONSE.new({eventGroup: eventGroup, loggedUserId: loggedUserSelector(getState()).id}))
        );
    }
}

export function fetchInitialEventsForChallenge(challengeId: number) {
    return function (dispatch, getState: () => ReduxState) {
        dispatch(CLEAR_UNREAD_NOTIFICATIONS.new({challengeId}));
        webCall.loadEventsForChallenge(dispatch, challengeId).then(
            (eventGroup: EventGroupSynchDTO) => dispatch(WEB_EVENTS_SYNC_RESPONSE.new({eventGroup, loggedUserId: loggedUserSelector(getState()).id}))
        );
    }
}

export function loadPreviousEventsAction() {
    return function (dispatch, getState: () => ReduxState) {

        const state = getState();
        const eventGroupSynchDTO = displaySeletectedEventGroupSelector(state);



        var beforeEventReadId=_.minBy( eventGroupSynchDTO.events,e=>e.eventReadId).eventReadId;

        webCall.loadEventsForChallenge(dispatch, eventGroupSynchDTO.challengeId, beforeEventReadId).then(
            (eventGroup: EventGroupSynchDTO) => dispatch(WEB_EVENTS_SYNC_RESPONSE_PREVIOUS.new({eventGroup, loggedUserId: loggedUserSelector(getState()).id}))
        );

    }
}

import {ReduxState} from "../redux/ReduxState";
import {ADD_NEW_EVENT_OPTIMISTIC, WEB_EVENTS_RESPONSE, WEB_ASYNC_EVENT_RESPONSE, MARK_EVENT_AS_READ_OPTIMISTIC} from "./eventActionTypes";
import * as webCall from "./eventWebCalls";
import {EventDTO, EventType, EventGroupDTO} from "./EventDTO";
import {WEB_STATUS_NOTHING_RETURNED_YET, WEB_STATUS_UNAUTHORIZED} from "../logic/domain/Common";
import {authPromiseErr} from "../module_accounts/accountActions";


export function sendEvent(authorId: number, content: string) {
    return function (dispatch, getState: ()=>ReduxState) {
        var eventDTO: EventDTO = {
            id: -new Date().getTime(),
            challengeId: getState().challenges.selectedChallengeId,
            taskId: null,
            content: content,
            authorId: authorId,
            sentDate: new Date().getTime(),
            eventType: EventType.POST
        }

        //dispatch(loadEventsAsyncAllTheTime());


        dispatch(ADD_NEW_EVENT_OPTIMISTIC.new(eventDTO));

        webCall.sendEventWithAuthFailure(eventDTO, content == "ERR").then(
            (eventDTO: EventDTO) => {
                //loadEventsAsyncAllTheTime should handle this case
                // dispatch(fetchEvents(getState().challenges.selectedChallengeId));
            }
        ).catch((reason)=>authPromiseErr(reason,dispatch))



        /* webCall.sendEvent(eventDTO).then(
         (eventDTO: EventDTO) => {
         //loadEventsAsyncAllTheTime should handle this case
         // dispatch(fetchEvents(getState().challenges.selectedChallengeId));
         }
         )*/
    }
}





export function loadEventsAsyncAllTheTime() {
    return function (dispatch, getState: ()=>ReduxState) {
        var challengeId = getState().challenges.selectedChallengeId;
        if (challengeId != null)
            webCall.loadEventsForChallengeAsync(challengeId).then(
                (events: EventDTO[])=> {
                    console.log("loadEventsAsyncAllTheTime");
                    //console.log(events);
                    dispatch(WEB_ASYNC_EVENT_RESPONSE.new({events}))


                    var promises = [];
                    events.forEach(e=> {
                        if (e.readDate == null)
                            promises.push(dispatch(markAsRead(e.challengeId, e.id)))
                    })

                    return Promise.all(promises);
                }
            ).then(()=> {
                dispatch(loadEventsAsyncAllTheTime());
            }).catch((reason)=>authPromiseErr(reason,dispatch))
                .catch((reason: any)=> {
                console.log(reason);
                if (reason.status == WEB_STATUS_NOTHING_RETURNED_YET) {
                    // this means no message arrived so far, but it is not an error, we need to retry call
                    dispatch(loadEventsAsyncAllTheTime());
                } else {
                    setTimeout(()=> {
                        dispatch(loadEventsAsyncAllTheTime());
                        // other problem, wait 10 seconds and retry
                    }, 10000)
                }
            });
    }
}

function markAsRead(challengeId, eventId) {
    return function (dispatch, getState: ()=>ReduxState) {
        var readDate = new Date().getTime();
        var p = webCall.markEventRead(challengeId, eventId, readDate)
            .catch((reason)=>authPromiseErr(reason,dispatch))
        dispatch(MARK_EVENT_AS_READ_OPTIMISTIC.new({challengeId, eventId, readDate}))
        return p;
    }
}


export function fetchInitialEvents(challengeId: number) {
    return function (dispatch) {
        webCall.loadEventsForChallenge(challengeId).then(
            (challengeConversation: EventGroupDTO)=> dispatch(WEB_EVENTS_RESPONSE.new(challengeConversation))
        ).catch((reason)=>authPromiseErr(reason,dispatch));
    }
}


export function showTaskEvents(challengeId: number, taskId: number) {
    return function (dispatch) {
        webCall.loadEventsForTask(challengeId, taskId).then(
            (eventGroup: EventGroupDTO)=> dispatch(WEB_EVENTS_RESPONSE.new(eventGroup))
        ).catch((reason)=>authPromiseErr(reason,dispatch));
    }
}
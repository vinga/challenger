import {ReduxState} from "../redux/ReduxState";
import {ADD_NEW_EVENT_OPTIMISTIC, WEB_EVENTS_RESPONSE} from "./eventActionTypes";
import * as webCall from "./eventWebCalls";
import {EventDTO, EventType, EventGroupDTO} from "./EventDTO";




export function sendEvent(authorId: number, content: string) {
    return function (dispatch, getState: ()=>ReduxState) {
        var eventDTO: EventDTO = {
            id: 0,
            challengeId: getState().challenges.selectedChallengeId,
            taskId: null,
            content: content,
            authorId: authorId,
            sentDate: new Date().getTime(),
            eventType: EventType.POST
        }

        //dispatch(loadEventsAsyncAllTheTime());


        dispatch(ADD_NEW_EVENT_OPTIMISTIC.new(eventDTO));
        webCall.sendEvent(eventDTO).then(
            (eventDTO: EventDTO) => {
                //loadEventsAsyncAllTheTime should handle this case
                // dispatch(fetchEvents(getState().challenges.selectedChallengeId));
            }
        )
    }
}

export function loadEventsAsyncAllTheTime() {
    return function (dispatch, getState: ()=>ReduxState) {
        var challengeId = getState().challenges.selectedChallengeId;
        if (challengeId != null)
            webCall.loadEventsForChallengeAsync(challengeId).then(
                (challengeConversation: EventGroupDTO)=> {
                    console.log("loadEventsAsyncAllTheTime");
                    console.log(challengeConversation);
                    dispatch(loadEventsAsyncAllTheTime());
                    dispatch(WEB_EVENTS_RESPONSE.new(challengeConversation))
                }
            ).catch((reason:any)=>{
                console.log("REASON ");
                console.log(reason);
                if (reason.status==503) {
                    dispatch(loadEventsAsyncAllTheTime());
                    console.log("it was cancelled");
                }
            });
    }
}


export function fetchInitialEvents(challengeId: number) {
    return function (dispatch, getState: ()=>ReduxState) {
        webCall.loadEventsForChallenge(challengeId).then(
            (challengeConversation: EventGroupDTO)=> dispatch(WEB_EVENTS_RESPONSE.new(challengeConversation))
        );
    }
}


export function showTaskEvents(challengeId: number, taskId: number) {
    return function (dispatch, getState: ()=>ReduxState) {
        webCall.loadEventsForTask(challengeId, taskId).then(
            (eventGroup: EventGroupDTO)=> dispatch(WEB_EVENTS_RESPONSE.new(eventGroup))
        );
    }
};
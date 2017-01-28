import {baseWebCall} from "../logic/WebCall";
import {EventGroupDTO, EventDTO, EventGroupSynchDTO} from "./EventDTO";

export function loadEventsForChallenge(dispatch, challengeId: number, beforeEventReadId?: number): Promise<EventGroupSynchDTO> {
    const max=100;
    var url=`/challenges/${challengeId}/events?max=${max}`
    if (beforeEventReadId!=null) {
        url=`${url}&beforeEventReadId=${beforeEventReadId}`;
    }
    return Promise.resolve(baseWebCall.get(dispatch, url));
}

export function loadEventsForTask(dispatch, challengeId: number, taskId: number): Promise<EventGroupDTO> {
    return Promise.resolve(baseWebCall.get(dispatch, `/challenges/${challengeId}/tasks/${taskId}/events`));
}

export function sendEvent(dispatch, event: EventDTO): Promise<EventDTO> {
    return Promise.resolve(baseWebCall.post(dispatch, `/challenges/${event.challengeId}/events`, event));
}

export function sendEventWithAuthFailure(dispatch, event: EventDTO, failure: boolean): Promise<EventDTO> {
    return Promise.resolve(baseWebCall.postWithFailureIfTrue(dispatch, `/challenges/${event.challengeId}/events`, event, failure));
}
export function loadEventsForChallengeAsync(dispatch, lastEventReadId?: number): Promise<EventGroupDTO> {
    if (lastEventReadId != null) {
        return Promise.resolve(baseWebCall.post(dispatch, `/async/events?lastEventReadId=${lastEventReadId}`, null, {async: true}));
    }
    return Promise.resolve(baseWebCall.post(dispatch, `/async/events`, null, {async: true}));
}


export function markEventRead(dispatch, challengeId: number, eventId: number, dateInMillis: number): Promise<void> {
    return Promise.resolve(baseWebCall.post(dispatch, `/challenges/${challengeId}/events/${eventId}/markRead`, dateInMillis));
}





import {baseWebCall} from "../logic/WebCall";
import {EventGroupDTO, EventDTO} from "./EventDTO";

export function loadEventsForChallenge(challengeId: number): Promise<EventGroupDTO> {
    return Promise.resolve(baseWebCall.get(`/challenges/${challengeId}/events`));
}

export function loadEventsForTask(challengeId: number, taskId: number): Promise<EventGroupDTO> {
    return Promise.resolve(baseWebCall.get(`/challenges/${challengeId}/tasks/${taskId}/events`));
}

export function sendEvent(event: EventDTO): Promise<EventDTO> {
    return Promise.resolve(baseWebCall.post(`/challenges/${event.challengeId}/events`, event));
}

export function sendEventWithAuthFailure(event: EventDTO, failure: boolean): Promise<EventDTO> {
    return Promise.resolve(baseWebCall.postWithFailureIfTrue(`/challenges/${event.challengeId}/events`, event, failure));
}
export function loadEventsForChallengeAsync(challengeId: number, lastEventId?: number): Promise<EventDTO[]> {
    if (lastEventId != null) {
        return Promise.resolve(baseWebCall.post(`/async/challenges/${challengeId}/events?lastEventId=${lastEventId}`, null));
    }
    return Promise.resolve(baseWebCall.post(`/async/challenges/${challengeId}/events`, null));
}


export function markEventRead(challengeId: number, eventId: number, dateInMillis: number): Promise<void> {
    return Promise.resolve(baseWebCall.post(`/challenges/${challengeId}/events/${eventId}/markRead`, dateInMillis));
}





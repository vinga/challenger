import {webCall} from "../logic/WebCall";
import {EventGroupDTO, EventDTO} from "./EventDTO";

export function loadEventsForChallenge(challengeId: number): Promise<EventGroupDTO> {
    return Promise.resolve(webCall.get(`/challenges/${challengeId}/events`));
}

export function loadEventsForTask(challengeId: number, taskId: number): JQueryPromise<EventGroupDTO> {
    return webCall.get(`/challenges/${challengeId}/tasks/${taskId}/events`);
}

export function sendEvent(event: EventDTO): JQueryPromise<EventDTO> {
    return webCall.post(`/challenges/${event.challengeId}/events`, event);
}
export function loadEventsForChallengeAsync(challengeId: number): Promise<EventGroupDTO> {
    return Promise.resolve(webCall.post(`/async/challenges/${challengeId}/events`,null));
}




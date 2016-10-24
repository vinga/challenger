import {CHANGE_CHALLENGE, WEB_CHALLENGES_REQUEST, WEB_CHALLENGES_RESPONSE, WEB_EVENTS_RESPONSE, ADD_NEW_EVENT_OPTIMISTIC} from "./challengeActionTypes";

import {ReduxState} from "../redux/ReduxState";
import ajaxWrapper from "../logic/AjaxWrapper";
import {EventGroupDTO} from "../logic/domain/EventGroupDTO";
import {EventDTO, EventType} from "../logic/domain/EventDTO";
import {fetchTasksWhenNeeded, fetchTasks} from "../module_tasks/index";
import {loggedUserSelector} from "../module_accounts/index";

export function sendEvent(content: string) {
    return function (dispatch, getState: ()=>ReduxState) {
       var eventDTO: EventDTO = {
            id: 0,
            challengeId: getState().challenges.selectedChallengeId,
            taskId: null,
            content: content,
            authorId: loggedUserSelector(getState()).userId,
            sentDate: new Date().getTime(),
            eventType: EventType.POST
        }

        dispatch(ADD_NEW_EVENT_OPTIMISTIC.new(eventDTO));
        ajaxWrapper.sendEvent(eventDTO).then(
            (eventDTO: EventDTO) => {
                //TODO optimize
                dispatch(showChallengeConversation(getState().challenges.selectedChallengeId));
            }
        )
    }
}

export function changeChallengeAction(challengeId: number) {
    return function (dispatch, getState) {
        dispatch(CHANGE_CHALLENGE.new({challengeId}));
        dispatch(fetchTasksWhenNeeded(challengeId,getState().currentSelection.day));
        dispatch(showChallengeConversation(challengeId));
    };
}
export function fetchWebChallenges() {
    return function (dispatch, getState: ()=>ReduxState) {
        dispatch(WEB_CHALLENGES_REQUEST.new({}));
        ajaxWrapper.loadVisibleChallenges(
            visibleChallengesDTO=> {
                var loadTasks= (getState().challenges.selectedChallengeId!=visibleChallengesDTO.selectedChallengeId);
                dispatch(WEB_CHALLENGES_RESPONSE.new(visibleChallengesDTO));
                if (loadTasks) {
                    dispatch(fetchTasks(visibleChallengesDTO.selectedChallengeId,getState().currentSelection.day));
                    dispatch(showChallengeConversation(visibleChallengesDTO.selectedChallengeId));
                }
            }
        )
    }
};

function showChallengeConversation(challengeId:number) {
    return function (dispatch, getState: ()=>ReduxState) {
        ajaxWrapper.loadPostsForChallenge(challengeId).then(
            (challengeConversation:EventGroupDTO)=> dispatch(WEB_EVENTS_RESPONSE.new(challengeConversation))
        );
    }
}



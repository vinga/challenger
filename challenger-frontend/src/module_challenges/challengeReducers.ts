import {WEB_CHALLENGES_REQUEST, CHANGE_CHALLENGE, WEB_CHALLENGES_RESPONSE, ADD_NEW_EVENT_OPTIMISTIC, WEB_EVENTS_RESPONSE} from "./challengeActionTypes";
import {isAction} from "../redux/ReduxTask";
import {VisibleChallengesDTO, ChallengeDTO} from "./ChallengeDTO";
import {EventDTO} from "../logic/domain/EventDTO";
import {EventGroupDTO} from "../logic/domain/EventGroupDTO";
import {copy} from "../redux/ReduxState";
import {LOGOUT} from "../module_accounts/index";

const initial = ():VisibleChallengesDTO => { return {
    selectedChallengeId: -1,
    visibleChallenges: [],
}}

export default function challenges(state: VisibleChallengesDTO =initial(), action):VisibleChallengesDTO {
    if (isAction(action, LOGOUT)) {
        return initial();
    } else if (isAction(action, WEB_CHALLENGES_REQUEST)) {
        return state;
    } else if (isAction(action, CHANGE_CHALLENGE)) {
        console.log("change challenge to " + action.challengeId);
        return copy(state).and({selectedChallengeId: action.challengeId });
    } else if (isAction(action, WEB_CHALLENGES_RESPONSE)) {
        return action;
    } else if (isAction(action, WEB_EVENTS_RESPONSE)) {

        var posts:Array<EventDTO>=action.posts;
        posts.sort((p1,p2)=> -(p2.sentDate-p1.sentDate))

        var visibleChallenges=state.visibleChallenges.map(ch=> {
            if (ch.id==action.challengeId) {
                return copy(ch).and({displayedConversation: action});
            } else return ch;
        })
        return copy(state).and({ visibleChallenges: visibleChallenges});
    } else if (isAction(action, ADD_NEW_EVENT_OPTIMISTIC)) {
        var visibleChallenges:Array<ChallengeDTO>=state.visibleChallenges.map((ch:ChallengeDTO)=> {
            if (ch.id==action.challengeId) {
                var eventGroup:EventGroupDTO=ch.displayedConversation;
                if (eventGroup!=null) {
                    var newPosts:Array<EventDTO>=[...eventGroup.posts, action];
                    var newEventGroup:EventGroupDTO =
                        copy(eventGroup).and({posts: newPosts});
                    return copy(ch).and({displayedConversation: newEventGroup});
                }
            }
            return ch;
        });
        return copy(state).and({visibleChallenges:visibleChallenges});
    }
    return state
}

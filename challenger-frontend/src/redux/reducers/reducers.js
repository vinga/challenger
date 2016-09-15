import {
    INCREMENT_DAY,
    CHANGE_CHALLENGE,
    WEB_CHALLENGES_REQUEST,
    WEB_CHALLENGES_RESPONSE
} from "../actions/actions";

import users from "./users";
import tasks from "./tasks";
import {combineReducers, createStore, applyMiddleware} from "redux";
import {createEpicMiddleware, combineEpics} from "redux-observable";
import Rx from "rxjs/Rx";
import "rxjs";
const {ajax} = Rx.Observable;
// all operators



function mainReducer(state = { day: new Date() }, action) {
    // For now, don't handle any actions
    // and just return the state given to us.

    switch (action.type) {
        case INCREMENT_DAY:
            return Object.assign({}, state, {
                day: state.day.addDays(action.amount)
            })
        case CHANGE_CHALLENGE:
            return Object.assign({}, state, {
                challengeId: action.challengeId
            })
        default:
            return state
    }

    return state
}


function visibleChallengesDTO(state = {
    selectedChallengeId: -1,
    visibleChallenges: []
},
                              action) {
    switch (action.type) {
        case WEB_CHALLENGES_REQUEST:
            console.log("webcareqq");
            return state;
        case CHANGE_CHALLENGE:
            console.log("change challenge to " + action.challengeId);
            return Object.assign({}, state, {
                selectedChallengeId: action.challengeId
            })
        case WEB_CHALLENGES_RESPONSE:
            console.log("webchallenges response");
            console.log(action.visibleChallengesDTO);
            //if (state.selectedChallengeId!=action.visibleChallengesDTO.selectedChallengeId)
            //     dispatch(changeChallenge(state.selectedChallengeId));
            return action.visibleChallengesDTO;
        default:
            return state
    }
    return state
}


export const rootReducer = combineReducers({
    mainReducer,
    visibleChallengesDTO,
    tasks,
    users

})

/*
const webChallengesEpic = (action$, store) =>
    action$.ofType(WEB_CHALLENGES_REQUEST)
        .flatMap(action => {
                var obs = ajaxWrapper.loadVisibleChallengesObservable()
                    .flatMap(visibleChallengesDTO => {
                            // Concat 2 observables so they fire sequentially
                            return Rx.Observable.concat(
                                Rx.Observable.of(webChallengesResponse(visibleChallengesDTO)),
                                Rx.Observable.of(changeChallenge(visibleChallengesDTO.selectedChallengeId)),
                                Rx.Observable.of(loadTasksRequest(visibleChallengesDTO.selectedChallengeId, 0, new Date())),
                                Rx.Observable.of(loadTasksRequest(visibleChallengesDTO.selectedChallengeId, 1, new Date())),
                            )
                        }
                    )

                //obs=obs.mergeMap(ac=>Rx.Observable.of(webChallengesResponse));

                return obs;
            }
        );


const loadTasksEpic = (action$, store) =>
    action$.ofType(LOAD_TASKS_REQUEST)
        .flatMap(action => {
                console.log("load tasks request...." + action.challengeId + " " + action.userNo + " " + action.date);
                var obs = ajaxWrapper.loadTasksFromServerObservable(action.challengeId, action.userNo, action.date)
                    .map((tasksList)=>loadTasksResponse(tasksList, action.challengeId, action.userNo, action.date))
                return obs;
            }
        );

export const combinedEpics = combineEpics(
    webChallengesEpic,
    loadTasksEpic
);

*/




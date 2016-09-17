import {fetchTasks, fetchTasksWhenNeeded} from "./tasks.ts";
import ajaxWrapper from "../../logic/AjaxWrapper";
import {VisibleChallengesDTO} from "../../logic/domain/ChallengeDTO";



export interface Action {
    type?: string;
}
export class ActionType<T extends Action>{
    type: string;
    constructor(type: string) {
        this.type=type;
    }
    new(t: T):T {
        t.type=this.type;
        return t;
    }
}

export function isAction<T extends Action>(action: Action, type: ActionType<T>|string): action is T {
    if (typeof type === 'string') {
        return action.type==type;
    } else if (type instanceof ActionType) {
        return action.type === type.type;
    }
    return false;
}

/*
const FOO: ActionType<{foo: string, foo2: string}> = new ActionType<any>('FOO');
interface BarAction extends Action {
    bar: string;
}
const BAR: ActionType<BarAction> = new ActionType<BarAction>('BAR');

// in reducer:
export default function reducer(state = [], action:Action) {
    if (isAction(action, FOO)) {
        action.foo // have access
        return state;
    } else if (isAction(action, BAR)) {
        action.bar;
        return state;
    } else
        return state;
}

// create new action like this

FOO.new({foo: 'foo'}) // error, foo2 is required
FOO.new({foo: 'foo', foo2: 'foo'}) // OK

// dispatch it like this
dispatch(FOO.new({foo: 'foo', foo2: 'foo'}));*/




/*
function reduce(a: Action) {
    console.log("type of action "+a.type+" "+JSON.stringify(a));
    if (isAction2(a, BARR)) {

        console.log("is login action "+a.bar);
    } else
        console.log("is no login action");
}
function reduce2(a: Action) {
    console.log("type of action "+a.type+" "+JSON.stringify(a));
    if (isAction(a, BAR)) {

        console.log("is login action "+a.bar);
    } else
        console.log("is no login action");
}
/*



export const LOGIN_ACTION: ActionType<{login: string, password: string, primary:boolean}> = new ActionType<any>('LOGIN_ACTION');
export const LOGIN_USER_RESPONSE_SUCCESS: ActionType<{login: string, jwtToken: string}> = new ActionType<any>('LOGIN_USER_RESPONSE_SUCCESS');
export const LOGIN_USER_RESPONSE_FAILURE: ActionType<{login: string, exception: any, jqXHR : JQueryXHR}> = new ActionType<any>('LOGIN_USER_RESPONSE_FAILURE');




function reduce(a: Action) {
    console.log("type of action "+a.type+" "+JSON.stringify(a));
    if (isAction(a, LOGIN_ACTION)) {
       console.log("is login action "+a.login);
    } else
        console.log("is no login action");
}




//reduce(LOGIN_ACTION.new({login:"vv", password: "pass", primary: true}));





var c:BarAction={type: "BAR", bar: "bb"};
reduce(c);



reduce({type: "bar",bar: "bb"} as BarAction);
*/

export const INCREMENT_DAY = 'INCREMENT_DAY'



export function incrementDay(amount) {
    return { type: INCREMENT_DAY, amount: amount }
}




export const CHANGE_CHALLENGE = 'CHANGE_CHALLENGE'
export const WEB_CHALLENGES_REQUEST='WEB_CHALLENGES_REQUEST';
export const WEB_CHALLENGES_RESPONSE='WEB_CHALLENGES_RESPONSE'




export function incrementDayAction(amount) {
    return function(dispatch, getState) {
        dispatch(incrementDay(amount));
        dispatch(fetchTasksWhenNeeded(getState().visibleChallengesDTO.selectedChallengeId, getState().mainReducer.day));
    }
}

export function webChallengesRequest() {
    return {type: WEB_CHALLENGES_REQUEST}
}
export function webChallengesResponse(visibleChallengesDTO: VisibleChallengesDTO) {
    return { type: WEB_CHALLENGES_RESPONSE, visibleChallengesDTO: visibleChallengesDTO }
}
function changeChallenge(challengeId: number) {
    return { type: CHANGE_CHALLENGE, challengeId: challengeId }
}

export function changeChallengeAction(challengeId: number) {
    return function (dispatch, getState) {
        dispatch(changeChallenge(challengeId));
        dispatch(fetchTasksWhenNeeded(challengeId,getState().mainReducer.day));
    };
}





export function fetchWebChallenges() {
    return function (dispatch, getState) {
        dispatch(webChallengesRequest());
        ajaxWrapper.loadVisibleChallenges(
            visibleChallengesDTO=> {
                var loadTasks= (getState().visibleChallengesDTO.selectedChallengeId!=visibleChallengesDTO.selectedChallengeId);

                dispatch(webChallengesResponse(visibleChallengesDTO));

                if (loadTasks) {
                    dispatch(fetchTasks(visibleChallengesDTO.selectedChallengeId,getState().mainReducer.day));
                }

            }
        )
    }
};



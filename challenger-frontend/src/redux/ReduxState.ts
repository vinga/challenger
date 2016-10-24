import {connect} from "react-redux";
import {VisibleChallengesDTO} from "../module_challenges/index";
import {TaskDTOState} from "../module_tasks/index";
import {AccountDTO} from "../module_accounts/index";

export interface CurrentSelection {
    day: Date,
    //userId?:number;
    showChallengeConversation:boolean
}

export interface RegisterState {
    registerInProgress?: boolean;
    registerError?: string;
    registeredSuccessfully?: boolean;

}
export interface ReduxState {
    challenges: VisibleChallengesDTO,
    registerState?: RegisterState,
    tasksState: TaskDTOState,
    accounts:Array<AccountDTO>,
    currentSelection: CurrentSelection
}


function steal(result: any, data: any): any {
    for (var key in data) {
        if (data.hasOwnProperty(key)) {
            result[key] = data[key];
        }
    }
    return result;
}

export class SameAs<a> {
    constructor(public result: a) { }
    public andMore<b>(value: b): SameAs<a & b> {
        return new SameAs<a & b>(steal(this.result, value));
    }
    public and<b>(value: b): a {
        return new SameAs<a & b>(steal(this.result, value)).result;
    }
}
export function copy<a>(value: a): SameAs<a> {
    return new SameAs(steal({}, value));
}


export {connect};

import {connect} from "react-redux";
import {VisibleChallengesDTO} from "../module_challenges/index";
import {TaskDTOState} from "../module_tasks/index";
import {RegisterState, ConfirmationLinkState} from "../module_accounts/index";
import {EventState} from "../module_events/index";
import {AccountDTO} from "../module_accounts/AccountDTO";
import {ReportState} from "../module_reports/ReportUserDTO";
import _ = require("lodash");

export interface WebCallData {
    startDate: Date
    callUid: number,
    fromStart: boolean
}

export enum LongCallVisible {
    FROM_START,
    LONG,
    VERY_LONG,
    ERROR
}

export interface CurrentSelection {
    today: Date,
    day: Date,
    loginErrorDescription?: string,
    internalErrorsCount: number,
    forgotPasswordMode?: boolean,
    loginInfoDescription?: string,
    longCallVisible?: LongCallVisible
    closableText?: string,
    noInternetConnection?: boolean,
    snackbarInfo?: string

}

export interface ReduxState {

    challenges: VisibleChallengesDTO,
    registerState?: RegisterState,
    tasksState: TaskDTOState,
    accounts: Array<AccountDTO>,
    eventsState: EventState;
    reportsState: ReportState;
    currentSelection: CurrentSelection,
    confirmationLinkState: ConfirmationLinkState,
    webCallsState: WebCallData[]
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
    constructor(public result: a) {
    }

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


export function copyAndReplace<T>(source: Array<T>, incoming: T, exists: (t: T)=>boolean): Array<T> {
    var res: Array<T> = [];


    if (source.find(exists) == null) {
        res = Object.assign([], source);
        res.push(incoming);
    } else {
        res = source.map(eg=> {
            if (exists(eg)) {
                return incoming;
            } else
                return eg;
        });
    }
    return res;
}



export {connect};

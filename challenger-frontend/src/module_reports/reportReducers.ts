import {isAction} from "../redux/ReduxTask";
import {ReportState, ReportType, ReportDataDTO} from "./ReportUserDTO";
import {REPORT_RESPONSE} from "./reportActionTypes";
import _ = require("lodash");

const getInitialState = (): ReportState => {
    return {
        challengeReports: []
    }
}

export function reportsState(state: ReportState = getInitialState(), action): ReportState {
    if (isAction(action, 'LOGOUT')) {
        return getInitialState();
    }
    if (isAction(action, REPORT_RESPONSE)) {


        var challengeReport=state.challengeReports.find(chp=>chp.challengeId==action.challengeId);
        if (challengeReport==null) {
            challengeReport={challengeId: action.challengeId, reports: []};
        }
        var newReports=Object.assign([],challengeReport.reports);

        action.reportData.map(incoming=> {

           /* for (var i=0; i<incoming.values.length; i++) {
                if (incoming.values[i]==0)
                    incoming.values[i]=null;
            }*/

            _.remove(newReports, rep=>rep.reportType== incoming.reportType)
        })


        newReports=newReports.concat(action.reportData)
        var newChallengeReport=Object.assign({}, challengeReport, {reports: newReports})


        var reportsGroupedByType=_.groupBy(newReports, e=>e.reportType);


        // update max value
        var progressiveReports:ReportDataDTO[]=reportsGroupedByType[ReportType.progressive];
        var maxValue:any=_.maxBy(progressiveReports, rep=>Math.max(...rep.values));
        if (maxValue!=null) {
            maxValue=Math.max(...maxValue.values);
        } else maxValue=0;

        if (newChallengeReport.lastMaxProgressive==null || newChallengeReport.lastMaxProgressive<maxValue) {
            newChallengeReport.lastMaxProgressive=maxValue+5;

        }

        progressiveReports.forEach(rep=>rep.maxValue=newChallengeReport.lastMaxProgressive);




        var newChallengeReports=state.challengeReports.filter(chp=>chp.challengeId!=action.challengeId).concat(newChallengeReport);

        return Object.assign({}, state, {challengeReports: newChallengeReports})

    }
    return state;
}

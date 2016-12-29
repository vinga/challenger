import {authPromiseErr} from "../module_accounts/accountActions";
import {EventGroupDTO} from "../module_events/EventDTO";
import * as webCall from "./reportWebCalls";
import {ReportDataDTO} from "./ReportUserDTO";
import {REPORT_RESPONSE} from "./reportActionTypes";

export function downloadProgressiveReports(challengeId: number) {
    return function (dispatch) {

        var date=new Date();
        date.setDate(date.getDate()-5);
        webCall.downloadProgressiveReports(dispatch, challengeId, date ).then(
            (reportData: ReportDataDTO[])=> dispatch(REPORT_RESPONSE.new({challengeId, reportData}))
        ).catch((reason)=>authPromiseErr(reason, dispatch));
    }
}
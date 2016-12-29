import {baseWebCall} from "../logic/WebCall";
import {ReportDataDTO} from "./ReportUserDTO";

export function downloadProgressiveReports(dispatch, challengeId: number, fromDay: Date): Promise<ReportDataDTO[]> {
    var dateIso= fromDay.yyyy_mm_dd();//yyyy-MM-dd

    return Promise.resolve(baseWebCall.get(dispatch, `/async/challenges/${challengeId}/progressiveReport?fromDay=${dateIso}`));
}





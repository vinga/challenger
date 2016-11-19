import {baseWebCall} from "../logic/WebCall";
import {ReportDataDTO} from "./ReportUserDTO";

export function downloadProgressiveReports(challengeId: number, fromDay: Date): Promise<ReportDataDTO[]> {
    var dateIso= fromDay.yyyy_mm_dd();//yyyy-MM-dd

    return Promise.resolve(baseWebCall.get(`/async/challenges/${challengeId}/progressiveReport?fromDay=${dateIso}`));
}





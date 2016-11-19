import {ActionType} from "../redux/ReduxTask";
import {ReportDataDTO} from "./ReportUserDTO";


export const REPORT_RESPONSE: ActionType<{challengeId: number, reportData: ReportDataDTO[]}> = new ActionType<any>('REPORT_RESPONSE');


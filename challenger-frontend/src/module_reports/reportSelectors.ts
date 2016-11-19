import {Selector, createSelector} from "reselect";
import {ReduxState} from "../redux/ReduxState";
import {ChallengeReportsDTO, ReportDataDTO, ReportType} from "./ReportUserDTO";

const displaySeletectedReportsSelector: Selector<ReduxState,ChallengeReportsDTO> = (state: ReduxState): ChallengeReportsDTO =>
    state.reportsState.challengeReports.find(eg=>eg.challengeId == state.challenges.selectedChallengeId)


export const progressiveReportsSelector: Selector<ReduxState,Array<ReportDataDTO>> = createSelector(
    displaySeletectedReportsSelector,

    (challengeReport: ChallengeReportsDTO) => {

        if (challengeReport==null)
            return [];
        return challengeReport.reports.filter(rep=>rep.reportType==ReportType.progressive);
    }
)

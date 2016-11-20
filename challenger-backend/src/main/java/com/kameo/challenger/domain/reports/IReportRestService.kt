package com.kameo.challenger.domain.reports

import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended


interface IReportRestService {


    fun getProgessiveReport(@Suspended asyncResponse: AsyncResponse, challengeId: Long, fromDay: String): Array<ReportDataDTO>

    data class ReportDataDTO (
        val challengeId: Long,
        val reportType: ReportType,
        val userId: Long,
        val labels: List<String>,
        val values: List<out Number>)


    enum class ReportType {
        progressive
    }

}
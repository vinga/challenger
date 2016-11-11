package com.kameo.challenger.domain.reports

import javax.ws.rs.PathParam
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended

/**
 * Created by Kamila on 2016-11-10.
 */
interface IReportRestService {


    fun getProgessiveReport(@Suspended asyncResponse: AsyncResponse, challengeId: Long, fromDay: String): DayProgressiveReportDTO;

    class DayProgressiveReportDTO {

    }

}
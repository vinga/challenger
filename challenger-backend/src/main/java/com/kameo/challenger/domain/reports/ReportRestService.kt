package com.kameo.challenger.domain.reports

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.reports.IReportRestService.ReportDataDTO
import com.kameo.challenger.domain.reports.IReportRestService.ReportType.progressive
import com.kameo.challenger.domain.reports.ReportDAO.ProgressiveReportDTO
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus
import com.kameo.challenger.web.rest.ChallengerSess
import io.swagger.annotations.Api
import org.springframework.stereotype.Component
import java.time.LocalDate
import javax.inject.Inject
import javax.ws.rs.*
import javax.ws.rs.container.AsyncResponse
import javax.ws.rs.container.Suspended
import javax.ws.rs.core.MediaType

@Component
@Path(ServerConfig.restPath)
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Api
class ReportRestService : IReportRestService {


    @Inject
    private lateinit var reportDAO: ReportDAO
    @Inject
    private lateinit var session: ChallengerSess

    @GET
    @WebResponseStatus(WebResponseStatus.ACCEPTED)
    @Path("/async/challenges/{challengeId}/progressiveReport")
    override fun getProgessiveReport(@Suspended asyncResponse: AsyncResponse,
                                     @PathParam("challengeId") challengeId: Long,
                                     @QueryParam("fromDay") fromDay: String /*ISO_LOCAL_DATE _yyyy-MM-dd*/): Array<ReportDataDTO> {


        val dayFrom = LocalDate.parse(fromDay)


        val prep: ProgressiveReportDTO = reportDAO.getProgressiveReport(session.userId, challengeId, dayFrom, true, false)


        val resList = prep.data.map {
            val userId = it.key


            val sortedMap = it.value

            val labels = sortedMap.keys.map { it.toString() }
            val values = sortedMap.values.map { it }

            ReportDataDTO(challengeId, progressive, userId, labels, values)


        }

        asyncResponse.resume(resList.toTypedArray())
        return resList.toTypedArray()

    }

}
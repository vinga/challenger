package com.kameo.challenger.domain.reports

import com.kameo.challenger.config.ServerConfig
import com.kameo.challenger.domain.reports.IReportRestService.DayProgressiveReportDTO
import com.kameo.challenger.utils.rest.annotations.WebResponseStatus
import com.kameo.challenger.web.rest.ChallengerSess
import io.swagger.annotations.Api
import org.joda.time.format.DateTimeFormat
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
class ReportRestService :IReportRestService{


    @Inject
    private lateinit var reportDAO: ReportDAO;
    @Inject
    private lateinit var session: ChallengerSess;

    @GET
    @WebResponseStatus(WebResponseStatus.ACCEPTED)
    @Path("/async/challenges/{challengeId}/progressiveReport")
    override fun getProgessiveReport(@Suspended asyncResponse: AsyncResponse,
                                     @PathParam("challengeId") challengeId: Long,
                                     @QueryParam("fromDay") fromDay: String /*ISO_LOCAL_DATE _yyyy-MM-dd*/): DayProgressiveReportDTO {



        val dayFrom = LocalDate.parse(fromDay);
        val rep=DayProgressiveReportDTO();


        reportDAO.getProgressiveReport(session.userId, challengeId, dayFrom);

        return rep;

    }

}
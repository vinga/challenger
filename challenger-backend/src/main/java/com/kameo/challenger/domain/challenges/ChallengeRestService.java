package com.kameo.challenger.domain.challenges;

import com.kameo.challenger.web.rest.ChallengerSess;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.stream.Collectors;

@Component
@Path("/api/challenge")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ChallengeRestService implements IChallengeRestService {
    private final ChallengerSess session;
    private final ChallengeDAO challengeDao;

    @Inject
    public ChallengeRestService(ChallengeDAO challengeDao, ChallengerSess session) {
        this.challengeDao = challengeDao;
        this.session = session;
    }


    @GET
    @Path("visibleChallenges")
    public IChallengeRestService.VisibleChallengesDTO getVisibleChallenges() {
        long callerId = session.getUserId();
        ChallengeDAO.ChallengeInfoDTO cinfo = challengeDao
                .getVisibleChallenges(callerId);

        IChallengeRestService.VisibleChallengesDTO res =
                new IChallengeRestService.VisibleChallengesDTO(cinfo.getDefaultChallengeId());


        res.getVisibleChallenges().addAll(cinfo.getVisibleChallenges().stream()
                .map(IChallengeRestService.VisibleChallengesDTO.ChallengeDTO.Companion::fromODB)
                .map(c -> {
                    c.setCallerId(callerId);
                    return c;
                })
                .collect(Collectors.toList()));

        return res;
    }


}

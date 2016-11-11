package com.kameo.challenger.domain.reports;
/**
 * Created by Kamila on 2016-11-10.
 */

import com.google.common.collect.Lists;
import com.kameo.challenger.config.DatabaseTestConfig;
import com.kameo.challenger.config.ServicesLayerConfig;
import com.kameo.challenger.domain.accounts.db.UserODB;
import com.kameo.challenger.domain.challenges.db.ChallengeODB;
import com.kameo.challenger.domain.reports.ReportDAO.ProgressiveReportDTO;
import com.kameo.challenger.util.TestHelper;
import com.kameo.challenger.utils.odb.AnyDAO;
import cucumber.api.DataTable;
import cucumber.api.PendingException;
import cucumber.api.java.Before;
import cucumber.api.java8.En;
import org.junit.Assert;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import javax.inject.Inject;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

@TestPropertySource(locations = "classpath:application-test.properties")
@ContextConfiguration(classes = {DatabaseTestConfig.class, ServicesLayerConfig.class})
public class ReportsTest implements En {
    @Inject
    private AnyDAO anyDao;
    @Inject
    private TestHelper testHelper;
    @Inject
    private ReportDAO reportDAO;



    ProgressiveReportDTO progressiveReport;
    @Before
    public void recreateSchema() {
        testHelper.clearSchema();
    }

    public ReportsTest() {


        When("^\"([^\"]*)\" request for Progressive Points Report for challenge \"([^\"]*)\" for last (\\d+) days$", (String login, String challengeName, Integer daysAgo) -> {
            final UserODB u = testHelper.resolveUserByLogin(login);
            final ChallengeODB ch = testHelper.resolveChallenge(challengeName);
            LocalDate fromDate=LocalDate.now().minusDays(daysAgo-1);
            progressiveReport = reportDAO.getProgressiveReport(u.getId(), ch.getId(), fromDate);
        });

        Then("^\"([^\"]*)\" see that \"([^\"]*)\" has total points in progressive report$", (String login1, String login2,  DataTable points) -> {
            final UserODB u1 = testHelper.resolveUserByLogin(login1);
            final UserODB u2 = testHelper.resolveUserByLogin(login2);

            final List<Integer> expectedValues = points.asList(Integer.class);
            final Map<LocalDate, Integer> userData = progressiveReport.getData().get(u2.getId());

            Assert.assertNotNull(userData);
            Assert.assertTrue("Generated report doens't have more days than expected",expectedValues.size()>=userData.size());

            LocalDate df=progressiveReport.getDayFromMidnight();

            int[] expected=new int[expectedValues.size()];
            int[] real=new int[expectedValues.size()];
            for (int i=0; i<expectedValues.size(); i++) {
                expected[i]=expectedValues.get(i);
                real[i]=Optional.ofNullable(userData.get(df)).orElse(0);
                df=df.plusDays(1);
            }

            Assert.assertArrayEquals(expected, real);
        });



    }
}


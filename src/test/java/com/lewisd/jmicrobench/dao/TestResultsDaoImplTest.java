package com.lewisd.jmicrobench.dao;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.lewisd.jmicrobench.BuildInfo;
import com.lewisd.jmicrobench.BuildInfoImpl;
import com.lewisd.jmicrobench.PerformanceTestResults;
import com.lewisd.jmicrobench.PerformanceTestResultsImpl;
import com.lewisd.test.Assert;
import com.mysql.jdbc.Driver;

public class TestResultsDaoImplTest
{

    private TestResultsDaoImpl dao;
    private BuildInfo build1;
    private BuildInfo build2;
    private BuildInfo build3;
    private BuildInfo build4;
    private PerformanceTestResults build1Results1;
    private PerformanceTestResults build1Results2;
    private PerformanceTestResults build1Results3;
    private PerformanceTestResults build2Results1;
    private PerformanceTestResults build2Results2;
    private PerformanceTestResults build2Results3;
    private PerformanceTestResults build3Results1;
    private PerformanceTestResults build3Results2;
    private PerformanceTestResults build3Results3;
    private PerformanceTestResults build4Results1;
    private PerformanceTestResults build4Results2;
    private PerformanceTestResults build4Results3;

    private void storeABunchOfResults() throws SQLException
    {
        dao.storeResults(Arrays.asList(build1Results1, build1Results2, build1Results3, build2Results1, build2Results2, build2Results3, build3Results1, build3Results2, build3Results3, build4Results1,
                build4Results2, build4Results3));
    }

    @Test
    public void shouldRetrieveLastThreeBuildsWhenThereAreMore() throws SQLException
    {
        storeABunchOfResults();

        List<BuildInfo> builds = dao.getRecentBuilds(3);
        Assert.assertNotNull(builds);
        Assert.assertEquals(3, builds.size());
        Assert.assertEquals(build2, builds.get(0));
        Assert.assertEquals(build3, builds.get(1));
        Assert.assertEquals(build4, builds.get(2));
    }

    @Test
    public void shouldRetrieveResultsForBuilds() throws SQLException
    {
        storeABunchOfResults();

        Map<BuildInfo, List<PerformanceTestResults>> recentResults = dao.getBuildResults(Arrays.asList(build1, build2, build3));

        Assert.assertNotNull(recentResults);
        Assert.assertEquals(3, recentResults.size());
        Assert.assertTrue(recentResults.containsKey(build1));
        Assert.assertTrue(recentResults.containsKey(build2));
        Assert.assertTrue(recentResults.containsKey(build3));
        Assert.assertFalse(recentResults.containsKey(build4));

        List<PerformanceTestResults> build1Results = recentResults.get(build1);
        Assert.assertEquals(3, build1Results.size());
        Assert.assertEqualsReflectively(build1Results1, build1Results.get(0));
        Assert.assertEqualsReflectively(build1Results2, build1Results.get(1));
        Assert.assertEqualsReflectively(build1Results3, build1Results.get(2));

        List<PerformanceTestResults> build2Results = recentResults.get(build2);
        Assert.assertEquals(3, build2Results.size());
        Assert.assertEqualsReflectively(build2Results1, build2Results.get(0));
        Assert.assertEqualsReflectively(build2Results2, build2Results.get(1));
        Assert.assertEqualsReflectively(build2Results3, build2Results.get(2));

        List<PerformanceTestResults> build3Results = recentResults.get(build3);
        Assert.assertEquals(3, build3Results.size());
        Assert.assertEqualsReflectively(build3Results1, build3Results.get(0));
        Assert.assertEqualsReflectively(build3Results2, build3Results.get(1));
        Assert.assertEqualsReflectively(build3Results3, build3Results.get(2));

    }

    private PerformanceTestResults setupResults(BuildInfo build, String testGroupName, String testName)
    {
        int revision = build.getRevision();
        PerformanceTestResults values = new PerformanceTestResultsImpl(build, testGroupName, testName);
        values.setDurationNanos(TimeUnit.MILLISECONDS.toNanos(5L * revision));
        values.setNumberOfOperations(100000L * revision);

        return values;
    }

    @Before
    public void setUp() throws Exception
    {
        build1 = new BuildInfoImpl(100000, new Date(101000000));
        build2 = new BuildInfoImpl(200000, new Date(102000000));
        build3 = new BuildInfoImpl(300000, new Date(103000000));
        build4 = new BuildInfoImpl(400000, new Date(104000000));
        build1Results1 = setupResults(build1, "group1", "testA");
        build1Results2 = setupResults(build1, "group1", "testB");
        build1Results3 = setupResults(build1, "group2", "testA");

        build2Results1 = setupResults(build2, "group1", "testA");
        build2Results2 = setupResults(build2, "group1", "testB");
        build2Results3 = setupResults(build2, "group2", "testA");

        build3Results1 = setupResults(build3, "group1", "testA");
        build3Results2 = setupResults(build3, "group1", "testB");
        build3Results3 = setupResults(build3, "group2", "testA");

        build4Results1 = setupResults(build4, "group1", "testA");
        build4Results2 = setupResults(build4, "group1", "testB");
        build4Results3 = setupResults(build4, "group2", "testA");

        String url = "jdbc:mysql://wedge/testresults?autoReconnect=true";
        Properties properties = new Properties();
        properties.setProperty("user", "lewisd");
        dao = new TestResultsDaoImpl("test", new Driver(), url, properties);
        dao.removeAllResults();
    }

}

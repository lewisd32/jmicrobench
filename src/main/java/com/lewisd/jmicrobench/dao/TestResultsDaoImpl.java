package com.lewisd.jmicrobench.dao;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.lewisd.jmicrobench.BuildInfo;
import com.lewisd.jmicrobench.BuildInfoImpl;
import com.lewisd.jmicrobench.PerformanceTestResults;
import com.lewisd.jmicrobench.PerformanceTestResultsImpl;

public class TestResultsDaoImpl implements TestResultsDao
{

    private final Logger log = Logger.getLogger(this.getClass());

    private static final String STORE_RESULT_SQL = "insert into test_results (project, revision, timestamp, " + "groupName, testName, attribute, value) values (?,?,?,?,?,?,?)";

    private static final String GET_BUILD_RESULTS_SQL = "select groupName,testName,attribute,value from " + "test_results where project=? and revision=? and timestamp=? order by groupName,testName";

    private static final String GET_RECENT_BUILDS_SQL = "select revision,timestamp from " + "test_results where project=? group by revision,timestamp order by revision desc,timestamp desc limit ?";

    private final Driver driver;
    private final String url;
    private final Properties properties;

    private final String project;

    public TestResultsDaoImpl(String project, Driver driver, String url, Properties properties)
    {
        this.project = project;
        this.driver = driver;
        this.url = url;
        this.properties = properties;
    }

    @Override
    public List<BuildInfo> getRecentBuilds(int count) throws SQLException
    {
        List<BuildInfo> builds = new LinkedList<BuildInfo>();
        Connection conn = getConnection();
        try
        {
            PreparedStatement statement = conn.prepareStatement(GET_RECENT_BUILDS_SQL);
            statement.setString(1, project);
            statement.setInt(2, count);
            ResultSet rs = statement.executeQuery();
            while (rs.next())
            {
                int revision = rs.getInt("revision");
                Date timestamp = new Date(rs.getTimestamp("timestamp").getTime());
                BuildInfo build = new BuildInfoImpl(revision, timestamp);
                builds.add(0, build);
            }

            return builds;
        }
        finally
        {
            close(conn);
        }
    }

    @Override
    public Map<BuildInfo, List<PerformanceTestResults>> getBuildResults(List<BuildInfo> builds) throws SQLException
    {
        Connection conn = getConnection();
        try
        {
            Map<BuildInfo, List<PerformanceTestResults>> resultsByBuild = new HashMap<BuildInfo, List<PerformanceTestResults>>();

            for (BuildInfo build : builds)
            {

                PreparedStatement statement = conn.prepareStatement(GET_BUILD_RESULTS_SQL);
                statement.setString(1, project);
                statement.setInt(2, build.getRevision());
                statement.setTimestamp(3, new Timestamp(build.getTimestamp().getTime()));

                ResultSet rs = statement.executeQuery();
                List<PerformanceTestResults> resultsForBuild = new LinkedList<PerformanceTestResults>();
                ;
                Map<String, Double> testValues = null;

                String testName = null;
                String testGroupName = null;
                while (rs.next())
                {
                    String nextTestGroupName = rs.getString("groupName");
                    String nextTestName = rs.getString("testName");

                    if (!(nextTestName.equals(testName) && nextTestGroupName.equals(testGroupName)))
                    {
                        // we've moved on to another test
                        if (testName != null)
                        {
                            addResultsToBuildResultsList(resultsForBuild, testValues, testName, testGroupName, build);
                        }
                        testValues = new HashMap<String, Double>();
                        testName = nextTestName;
                        testGroupName = nextTestGroupName;
                    }
                    String attributeName = rs.getString("attribute");
                    Double value = rs.getDouble("value");
                    testValues.put(attributeName, value);
                }
                if (testValues != null && !testValues.isEmpty())
                {
                    addResultsToBuildResultsList(resultsForBuild, testValues, testName, testGroupName, build);
                }
                resultsByBuild.put(build, resultsForBuild);
                rs.close();
            }

            return resultsByBuild;
        }
        finally
        {
            close(conn);
        }
    }

    private void addResultsToBuildResultsList(List<PerformanceTestResults> resultsForBuild, Map<String, Double> testValues, String testName, String testGroupName, final BuildInfo buildInfo)
    {
        PerformanceTestResults results = new PerformanceTestResultsImpl(buildInfo, testGroupName, testName, testValues);
        resultsForBuild.add(results);
    }

    private void close(Connection conn)
    {
        try
        {
            conn.close();
        }
        catch (SQLException e)
        {
            log.error("Error closing connection", e);
        }
    }

    @Override
    public void storeResults(List<PerformanceTestResults> resultsList) throws SQLException
    {
        Connection conn = getConnection();
        try
        {
            for (PerformanceTestResults results : resultsList)
            {
                PreparedStatement statement = conn.prepareStatement(STORE_RESULT_SQL);
                statement.setString(1, project);
                int revision = results.getBuildInfo().getRevision();
                Date timestamp = results.getBuildInfo().getTimestamp();
                statement.setInt(2, revision);
                statement.setTimestamp(3, new Timestamp(timestamp.getTime()));
                statement.setString(4, results.getTestGroupName());
                statement.setString(5, results.getTestName());
                Map<String, Double> valuesAsMap = results.asMap();
                for (String attribteName : valuesAsMap.keySet())
                {
                    double value = valuesAsMap.get(attribteName);
                    statement.setString(6, attribteName);
                    statement.setDouble(7, value);
                    statement.execute();
                }
            }
        }
        finally
        {
            close(conn);
        }
    }

    public void removeAllResults() throws SQLException
    {
        if (!project.equals("test"))
        {
            throw new IllegalStateException("Can't delete all results for non-test project '" + project + "'");
        }
        Connection conn = getConnection();
        try
        {
            PreparedStatement statement = conn.prepareStatement("delete from test_results where project=?");
            statement.setString(1, project);
            statement.execute();
        }
        finally
        {
            close(conn);
        }
    }

    private Connection getConnection() throws SQLException
    {
        return driver.connect(url, properties);
    }

}

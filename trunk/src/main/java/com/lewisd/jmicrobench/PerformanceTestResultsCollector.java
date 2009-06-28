package com.lewisd.jmicrobench;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.lewisd.jmicrobench.dao.TestResultsDao;

public class PerformanceTestResultsCollector extends PerformanceTestResultsProxy
{

    private final static Logger LOG = Logger.getLogger(PerformanceTestResultsCollector.class);

    private static final BuildInfo BUILD = new BuildInfoImpl(getRevision(), getBuildTimestamp());
    private final String projectName;
    private String testGroupName;
    private String testName;
    private PerformanceTestResults results;

    private PerformanceTestRunner runner;

    public PerformanceTestResultsCollector(String projectName)
    {
        this.projectName = projectName;
    }

    public void setRunner(PerformanceTestRunner runner)
    {
        this.runner = runner;
    }

    void setupTest(String testGroupName, String testName)
    {
        this.testGroupName = testGroupName;
        this.testName = testName;
        clearResults();
    }

    PerformanceTestResults getResults()
    {
        return results;
    }

    @Override
    protected void setResults(PerformanceTestResults results)
    {
        super.setResults(results);
        this.results = results;
    }

    public boolean isDone()
    {
        return runner.isDone();
    }

    long getActualDurationNanos()
    {
        return runner.getActualDurationNanos();
    }

    PerformanceTestResults getAveragedResults()
    {
        return runner.getAverageResults();
    }

    public void startDurationTimer()
    {
        runner.startDurationTimer();
    }

    public void stopDurationTimer()
    {
        stopDurationTimer(true);
    }

    public void stopDurationTimer(boolean includeDurationInTotal)
    {
        runner.stopDurationTimer(includeDurationInTotal);
    }

    public boolean shouldRun()
    {
        return runner.shouldRun();
    }

    @Override
    public Long getDurationNanos()
    {
        Long durationNanos = super.getDurationNanos();
        if (durationNanos == null)
        {
            durationNanos = getActualDurationNanos();
        }
        return durationNanos;
    }

    @Override
    public Long getDurationMillis()
    {
        Long durationMillis = super.getDurationMillis();
        if (durationMillis == null)
        {
            durationMillis = TimeUnit.NANOSECONDS.toMillis(getActualDurationNanos());
        }
        return durationMillis;
    }

    @Override
    public Long getDurationSeconds()
    {
        Long durationSeconds = super.getDurationSeconds();
        if (durationSeconds == null)
        {
            durationSeconds = TimeUnit.NANOSECONDS.toSeconds(getActualDurationNanos());
        }
        return durationSeconds;
    }

    @Override
    public Double getOperationsPerSecond()
    {
        Double opsPerSec = super.getOperationsPerSecond();
        if (opsPerSec == null)
        {
            final long actualDurationNanos = getActualDurationNanos();
            opsPerSec = PerformanceTestResultsImpl.calculateOpsPerSec(getNumberOfOperations(), actualDurationNanos);
        }
        return opsPerSec;
    }

    void clearResults()
    {
        setResults(new PerformanceTestResultsImpl(BUILD, testGroupName, testName));
    }

    void storeResults() throws Exception
    {
        saveResultsAsProperties();
        saveResultsInDatabase();
    }

    private void logTestResults()
    {
        Map<String, Double> resultsAsMap = results.asMap();
        for (String attributeName : resultsAsMap.keySet())
        {
            double value = resultsAsMap.get(attributeName);
            LOG.info(results.getTestGroupName() + " - " + results.getTestName() + "." + attributeName + " = " + String.format("%f", value));
        }
    }

    private void saveResultsInDatabase() throws Exception
    {
        if (BUILD.getRevision() < 0)
        {
            LOG.info("No build.revision property, not storing results in database");
            logTestResults();
        }
        else
        {
            TestResultsDao dao = DBHelper.getTestResultsDao(projectName);
            dao.storeResults(Collections.singletonList(results));
        }
    }

    private void saveResultsAsProperties() throws IOException
    {
        File reportsDir = new File(PropertiesHelper.getProperty("build.performance.reports.dir"), getHostname());
        String filename = getPropertiesFilename(testName, testGroupName);
        LOG.info("Writing properties to " + filename);
        Properties prop = new Properties();
        Map<String, Double> valuesAsMap = results.asMap();
        for (String attributeName : valuesAsMap.keySet())
        {
            double value = valuesAsMap.get(attributeName);
            prop.put(attributeName, String.format("%f", value));
        }
        prop.put("YVALUE", String.format("%f", results.getPlotableAttribute()));

        reportsDir.mkdirs();

        File file = new File(reportsDir, filename);
        prop.store(new FileWriter(file), filename + " test results from " + getHostname());
    }

    private String getHostname() throws UnknownHostException
    {
        InetAddress localMachine = java.net.InetAddress.getLocalHost();
        String hostname = localMachine.getHostName();
        return hostname;
    }

    private String getPropertiesFilename(String testGroup, String testName)
    {
        return testGroup + "-" + testName + ".properties";
    }

    private static Date getBuildTimestamp()
    {
        String timestampString = System.getProperty("build.timestamp");
        if (timestampString == null || timestampString.isEmpty())
        {
            LOG.warn("No build.timestamp system property");
            return new Date();
        }
        String formatString = PropertiesHelper.getProperty("build.timestamp.format");
        if (formatString == null)
        {
            throw new RuntimeException("No build.timestamp.format system property");
        }
        SimpleDateFormat formatter = new SimpleDateFormat(formatString);
        try
        {
            return formatter.parse(timestampString);
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Error parsing build timestamp", e);
        }
    }

    private static int getRevision()
    {
        String revision = System.getProperty("build.revision");
        if (revision != null && !revision.isEmpty())
        {
            return Integer.parseInt(revision);
        }
        return -1;
    }

    public static void main(String[] args) throws Exception
    {
        System.err.println("Revision: " + getRevision());
        System.err.println("Timestamp: " + getBuildTimestamp());
    }

}

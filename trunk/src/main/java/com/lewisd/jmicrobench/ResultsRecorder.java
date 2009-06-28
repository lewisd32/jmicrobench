package com.lewisd.jmicrobench;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.lewisd.jmicrobench.dao.TestResultsDao;

public class ResultsRecorder
{
    private final static Logger LOG = Logger.getLogger(ResultsRecorder.class);
    private final String projectName;
    
    public ResultsRecorder(String projectName)
    {
        this.projectName = projectName;
    }

    void storeResults(PerformanceTestResults results) throws Exception
    {
        saveResultsAsProperties(results);
        saveResultsInDatabase(results);
    }

    private void saveResultsInDatabase(PerformanceTestResults results) throws Exception
    {
        if (results.getBuildInfo().getRevision() < 0)
        {
            LOG.info("No build.revision property, not storing results in database");
            logTestResults(results);
        }
        else
        {
            TestResultsDao dao = DBHelper.getTestResultsDao(projectName);
            dao.storeResults(Collections.singletonList(results));
        }
    }
    
    private void logTestResults(PerformanceTestResults results)
    {
        Map<String, Double> resultsAsMap = results.asMap();
        for (String attributeName : resultsAsMap.keySet())
        {
            Double value = resultsAsMap.get(attributeName);
            LOG.info(results.getTestGroupName() + " - " + results.getTestName() + "." + attributeName + " = " + String.format("%f", value));
        }
    }

    private void saveResultsAsProperties(PerformanceTestResults results) throws IOException
    {
        File reportsDir = new File(PropertiesHelper.getProperty("build.performance.reports.dir"), getHostname());
        String filename = getPropertiesFilename(results.getTestName(), results.getTestGroupName());
        LOG.info("Writing properties to " + filename);
        Properties prop = new Properties();
        Map<String, Double> valuesAsMap = results.asMap();
        for (String attributeName : valuesAsMap.keySet())
        {
            Double value = valuesAsMap.get(attributeName);
            prop.put(attributeName, String.format("%f", value));
        }
        prop.put("YVALUE", String.format("%f", Double.valueOf(results.getPlotableAttribute())));

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


}

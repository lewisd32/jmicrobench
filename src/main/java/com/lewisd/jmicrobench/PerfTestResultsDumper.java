package com.lewisd.jmicrobench;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.lewisd.jmicrobench.dao.TestResultsDao;

public class PerfTestResultsDumper
{
    private void dumpDataForRecentBuilds(String projectName, int buildCount, File outputDir) throws SQLException, IOException
    {

        outputDir.mkdirs();

        TestResultsDao dao = DBHelper.getTestResultsDao(projectName);
        List<BuildInfo> recentBuilds = dao.getRecentBuilds(buildCount);
        Map<BuildInfo, List<PerformanceTestResults>> buildResults = dao.getBuildResults(recentBuilds);
        List<PerformanceTestResults> allResults = new LinkedList<PerformanceTestResults>();
        for (BuildInfo build : recentBuilds)
        {
            allResults.addAll(buildResults.get(build));
        }

        Set<String> groups = findAllGroups(allResults);

        SortedMap<String, SortedMap<String, List<PerformanceTestResults>>> sortedTestData = new TreeMap<String, SortedMap<String, List<PerformanceTestResults>>>();
        for (String group : groups)
        {
            SortedMap<String, List<PerformanceTestResults>> groupResults = findResultsForGroupKeyedByTestName(allResults, group);
            sortedTestData.put(group, groupResults);
        }

        for (String groupName : sortedTestData.keySet())
        {
            SortedMap<String, List<PerformanceTestResults>> groupResults = sortedTestData.get(groupName);
            for (String testName : groupResults.keySet())
            {
                List<PerformanceTestResults> testResults = groupResults.get(testName);
                File file = openResultsFile(outputDir, groupName, testName);
                PrintWriter writer = new PrintWriter(new FileWriter(file));
                for (PerformanceTestResults result : testResults)
                {
                    writeTestResult(writer, result);
                }
                writer.close();
            }
        }
    }

    private File openResultsFile(File outputDir, String groupName, String testName)
    {
        File dir = new File(outputDir, groupName);
        dir.mkdirs();
        File file = new File(dir, testName + ".csv");
        return file;
    }

    private void writeTestResult(PrintWriter writer, PerformanceTestResults result)
    {
        Double value = result.getPlotableAttribute();
        writer.println(result.getBuildInfo().getRevision() + "," + value);
    }

    private SortedMap<String, List<PerformanceTestResults>> findResultsForGroupKeyedByTestName(List<PerformanceTestResults> allResults, String group)
    {
        SortedMap<String, List<PerformanceTestResults>> resultsListByTestName = new TreeMap<String, List<PerformanceTestResults>>();
        for (PerformanceTestResults result : allResults)
        {
            String groupName = result.getTestGroupName();
            if (groupName.equals(group))
            {
                String testName = result.getTestName();
                List<PerformanceTestResults> testResultsList = resultsListByTestName.get(testName);
                if (testResultsList == null)
                {
                    testResultsList = new LinkedList<PerformanceTestResults>();
                    resultsListByTestName.put(testName, testResultsList);
                }
                testResultsList.add(result);
            }
        }
        return resultsListByTestName;
    }

    private Set<String> findAllGroups(List<PerformanceTestResults> allResults)
    {
        Set<String> groups = new HashSet<String>();

        for (PerformanceTestResults result : allResults)
        {
            groups.add(result.getTestGroupName());
        }

        return groups;
    }

    public static void main(String[] args) throws SQLException, IOException
    {
        String projectName = args[0];
        int count = Integer.parseInt(args[1]);
        String outputDirName = args[2];
        File outputDir = new File(outputDirName);

        new PerfTestResultsDumper().dumpDataForRecentBuilds(projectName, count, outputDir);
    }

}

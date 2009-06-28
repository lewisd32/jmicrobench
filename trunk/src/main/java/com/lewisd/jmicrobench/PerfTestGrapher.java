package com.lewisd.jmicrobench;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import com.lewisd.jmicrobench.dao.TestResultsDao;
import com.lewisd.jmicrobench.dao.TestResultsDaoImpl;
import com.lewisd.jmicrobench.graph.BufferedImagePainter;
import com.lewisd.jmicrobench.graph.Graph;
import com.lewisd.jmicrobench.graph.Plot;

public class PerfTestGrapher extends PerfTestResultsDumper
{

    private static final Logger LOG = Logger.getLogger(PerfTestGrapher.class);

    private void plotGraphsForRecentBuilds(String projectName, int buildCount, File outputDir) throws SQLException, IOException
    {

        int width = getIntProperty("build.performance.graph.width", 800);
        int height = getIntProperty("build.performance.graph.height", 500);
        int minXGridLinesInterval = getIntProperty("build.performance.graph.minxgridlinesinterval", 15);
        int minYGridLinesInterval = getIntProperty("build.performance.graph.minygridlinesinterval", 15);

        outputDir.mkdirs();

        File imageOutputDir = new File(outputDir, "images");
        imageOutputDir.mkdirs();

        TestResultsDao dao = DBHelper.getTestResultsDao(projectName);
        List<BuildInfo> recentBuilds = dao.getRecentBuilds(buildCount);
        Map<BuildInfo, List<PerformanceTestResults>> buildResults = dao.getBuildResults(recentBuilds);
        List<PerformanceTestResults> allResults = new LinkedList<PerformanceTestResults>();
        for (BuildInfo build : recentBuilds)
        {
            allResults.addAll(buildResults.get(build));
        }
        Set<String> groups = findAllGroups(allResults);
        SortedMap<String, String> graphFilesByGroupName = new TreeMap<String, String>();
        for (String group : groups)
        {
            LOG.info("Plotting group " + group);
            BufferedImagePainter painter = new BufferedImagePainter();
            Graph graph = new Graph(painter);

            graph.setDimensions(width, height);

            graph.setMinXGridLinesPixelInterval(minXGridLinesInterval);
            graph.setMinYGridLinesPixelInterval(minYGridLinesInterval);

            graph.setXLabelHorizontalPadding(2);

            graph.setXLabelFormat("%.0f");
            graph.setYLabelFormat("%.1f");
            graph.setGridColour(new Color(0.9f, 0.9f, 0.9f));

            String testName = null;
            Map<String, Plot> plotsByTestName = new HashMap<String, Plot>();
            for (PerformanceTestResults result : allResults)
            {
                if (result.getTestGroupName().equals(group))
                {
                    testName = result.getTestName();
                    // LOG.info("Plotting " + testName);

                    Plot plot = plotsByTestName.get(testName);
                    if (plot == null)
                    {
                        plot = graph.newPlot(testName);

                        plot.setDrawLines(true);

                        plotsByTestName.put(testName, plot);
                    }

                    Double value = result.getPlotableAttribute();
                    if (value != null)
                    {
                        plot.addPoint(result.getBuildInfo().getRevision(), value);
                    }

                }
            }
            if (testName != null)
            {
                graph.draw();
                BufferedImage image = painter.getImage();
                String formatName = "png";
                String filename = group + ".png";
                File outputFile = new File(imageOutputDir, filename);
                graphFilesByGroupName.put(group, filename);
                ImageIO.write(image, formatName, outputFile);
            }
        }

        File indexFile = new File(outputDir, "index.html");

        BufferedWriter out = new BufferedWriter(new FileWriter(indexFile));

        out.write("<html><body>\n");

        for (String group : graphFilesByGroupName.keySet())
        {
            String graphFile = graphFilesByGroupName.get(group);
            out.write("<h2>" + group + "</h2>");
            out.write("<div><img src=\"images/" + graphFile + "\"/></div>");
        }

        out.write("</html></body>\n");
        out.close();
    }

    private int getIntProperty(String key, int defaultValue)
    {
        return Integer.parseInt(PropertiesHelper.getProperty(key, Integer.toString(defaultValue)));
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

        if ("test".equals(projectName))
        {
            populateDummyResults(projectName, count);
        }

        new PerfTestGrapher().plotGraphsForRecentBuilds(projectName, count, outputDir);
    }

    private static void populateDummyResults(String projectName, int buildCount) throws SQLException
    {
        TestResultsDaoImpl dao = (TestResultsDaoImpl) DBHelper.getTestResultsDao(projectName);
        dao.removeAllResults();

        String[] testGroupNames = new String[] { "GroupA", "GroupB" };
        String[][] testNamesForGroup = new String[][] { new String[] { "testA1", "testA2", "testA3-longer" }, new String[] { "testB1" } };

        List<PerformanceTestResults> resultsList = new LinkedList<PerformanceTestResults>();

        for (int i = 0; i < buildCount; ++i)
        {
            int revision = 1000 + i;
            Date date = new Date((1000 + i) * 1000000);
            BuildInfo build = new BuildInfoImpl(revision, date);

            for (int gi = 0; gi < testGroupNames.length; ++gi)
            {
                String testGroupName = testGroupNames[gi];
                for (int ti = 0; ti < testNamesForGroup[gi].length; ++ti)
                {
                    String testName = testNamesForGroup[gi][ti];
                    PerformanceTestResultsImpl results = new PerformanceTestResultsImpl(build, testGroupName, testName);
                    if (testName.equals("testB1"))
                    {
                        results.setMemoryBytes(500000000L + i * 1000);
                    }
                    else
                    {
                        results.setDurationNanos(TimeUnit.MILLISECONDS.toNanos(3000L));
                        results.setNumberOfOperations((ti + 1) * 100000L - i * 500);
                    }
                    resultsList.add(results);
                }
            }
        }

        dao.storeResults(resultsList);

    }

}

package com.lewisd.jmicrobench;

import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import com.lewisd.test.Assert;

public class PerformanceTestRunner extends BlockJUnit4ClassRunner
{

    private static final int DEFAULT_WARMUP_PASSES = Integer.parseInt(PropertiesHelper.getProperty("build.performance.warmup.passes", "0"));
    private static final int DEFAULT_MAX_PASSES = Integer.parseInt(PropertiesHelper.getProperty("build.performance.max.passes", "100"));
    private static final int DEFAULT_STABLE_PASSES = Integer.parseInt(PropertiesHelper.getProperty("build.performance.stable.passes", "3"));
    private static final int DEFAULT_RUNS_TO_AVERAGE = Integer.parseInt(PropertiesHelper.getProperty("build.performance.averaged.runs", "3"));
    private static final long DEFAULT_EXPECTED_DURATION = Long.parseLong(PropertiesHelper.getProperty("build.performance.duration", "3000"));
    private static final long DEFAULT_WARMUP_DURATION = Long.parseLong(PropertiesHelper.getProperty("build.performance.warmup.duration", "0"));

    private static final Logger LOG = Logger.getLogger(PerformanceTestRunner.class);
    private static final int RESULTS_REMOVED_BY_PRUNING = 2;

    private final Field resultsCollectorField;
    private boolean warmedUp = false;
    private int warmupPasses;
    private long warmupDuration;
    private int maxPasses;
    private int stablePasses;
    private int runsToAverage;
    private long expectedDuration;
    private long totalDurationNanos;
    private String testName;
    private String groupName;
    private final Class testClass;

    private long startTimeNanos = -1;
    private long endTimeNanos = -1;
    private int currentPass;
    private List<PerformanceTestResults> resultsList;
    private PerformanceTestResultsCollector resultsCollector;

    public PerformanceTestRunner(Class testClass) throws InitializationError
    {
        super(testClass);
        this.testClass = testClass;
        resultsCollectorField = findResultsCollectorField(testClass);
        resultsCollectorField.setAccessible(true);
    }

    public boolean isDone()
    {
        boolean addedDuration = false;
        if (resultsCollector.getResults().getDurationNanos() == null)
        {
            addedDuration = true;
            resultsCollector.setDurationNanos(getActualDurationNanos());
        }
        try
        {
            return checkIfDone();
        }
        finally
        {
            if (addedDuration)
            {
                resultsCollector.setDurationNanos(null);
            }
        }
    }

    public boolean shouldRun()
    {
        long timeSpent = TimeUnit.NANOSECONDS.toMillis(getActualDurationNanos());
        return (timeSpent < expectedDuration);
    }

    public void startDurationTimer()
    {
        startTimeNanos = System.nanoTime();
        endTimeNanos = -1;
    }

    public void stopDurationTimer(boolean includeDurationInTotal)
    {
        if (endTimeNanos < 0)
        {
            endTimeNanos = System.nanoTime();
            if (includeDurationInTotal)
            {
                totalDurationNanos += (endTimeNanos - startTimeNanos);
            }
        }
    }

    long getActualDurationNanos()
    {
        if (startTimeNanos < 0)
        {
            return 0;
        }
        if (endTimeNanos < 0)
        {
            final long now = System.nanoTime();
            return totalDurationNanos + (now - startTimeNanos);
        }
        return totalDurationNanos;
    }

    static Field findResultsCollectorField(Class testClass) throws InitializationError
    {
        for (Class klass = testClass; klass != Object.class; klass = klass.getSuperclass())
        {
            for (Field field : klass.getDeclaredFields())
            {
                if (PerformanceTestResultsCollector.class.isAssignableFrom(field.getType()))
                {
                    return field;
                }
            }
        }
        throw new InitializationError("no PerformanceTestResultsCollector found in test class " + testClass);
    }

    @Override
    protected Statement methodInvoker(FrameworkMethod method, Object test)
    {
        Statement statement = super.methodInvoker(method, test);
        return withStartAndStopDurationTimer(method, test, statement);
    }

    private Statement withStartAndStopDurationTimer(final FrameworkMethod method, final Object test, final Statement statement)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                startDurationTimer();
                statement.evaluate();
                stopDurationTimer(true);
            }
        };
    }

    protected PerformanceTestResultsCollector getResultsCollector(Object test)
    {
        try
        {
            PerformanceTestResultsCollector results = (PerformanceTestResultsCollector) resultsCollectorField.get(test);
            results.setRunner(PerformanceTestRunner.this);
            return results;
        }
        catch (IllegalArgumentException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Statement withAfters(FrameworkMethod method, Object target, Statement statement)
    {
        return withRunUntilStable(method, target, super.withAfters(method, target, statement));
    }

    private Statement withRunUntilStable(final FrameworkMethod method, final Object target, final Statement statement)
    {
        return new Statement()
        {
            @Override
            public void evaluate() throws Throwable
            {
                LOG.info("Running " + testName(method));
                runTestUntilStable(method, target, statement);
            }
        };
    }

    private void runTestUntilStable(FrameworkMethod method, Object test, Statement statement) throws Throwable
    {
        resultsCollector = getResultsCollector(test);

        this.maxPasses = DEFAULT_MAX_PASSES;
        this.stablePasses = DEFAULT_STABLE_PASSES;
        this.runsToAverage = DEFAULT_RUNS_TO_AVERAGE;
        this.expectedDuration = DEFAULT_EXPECTED_DURATION;
        this.warmupPasses = DEFAULT_WARMUP_PASSES;
        this.warmupDuration = DEFAULT_WARMUP_DURATION;
        this.groupName = method.getName();
        this.testName = testClass.getSimpleName();

        PerformanceTest configuration;

        configuration = findClassAnnotation();
        configureFromAnnotation(configuration);

        configuration = method.getAnnotation(PerformanceTest.class);
        configureFromAnnotation(configuration);

        resultsList = new LinkedList<PerformanceTestResults>();
        resultsCollector.setupTest(groupName, testName);

        warmedUp = false;
        if (warmupPasses == 0 && warmupDuration == 0)
        {
            warmedUp = true;
        }

        currentPass = 0;
        while (!isDone())
        {
            resultsCollector.setupTest(groupName, testName);
            totalDurationNanos = 0;
            startTimeNanos = -1;
            endTimeNanos = -1;
            currentPass++;
            LOG.info("Running " + (warmedUp ? "real " : "warmup ") + "pass for " + testName(method));
            // add the results class before we run, so that it's counted by
            // calls to isDone from within the test method
            resultsList.add(resultsCollector.getResults());
            statement.evaluate();
            if (resultsCollector.getResults().getDurationNanos() == null)
            {
                resultsCollector.setDurationNanos(getActualDurationNanos());
            }
            // LOG.info(String.format("%.0f",
            // resultsCollector.getResults().getOperationsPerSecond()));
        }
        PerformanceTestResults averageResults = getAverageResults();
        resultsCollector.setResults(averageResults);
        try
        {
            resultsCollector.storeResults();
        }
        catch (IOException e)
        {
            LOG.error("Error storing test results", e);
        }
        catch (SQLException e)
        {
            LOG.error("Error storing test results", e);
        }
    }

    @SuppressWarnings("unchecked")
    private PerformanceTest findClassAnnotation()
    {
        return (PerformanceTest) testClass.getAnnotation(PerformanceTest.class);
    }

    private boolean checkIfDone(/*
                                 * int currentPass, List<PerformanceTestResults>
                                 * resultsList
                                 */)
    {
        if (!warmedUp)
        {
            if (currentPass < warmupPasses)
            {
                return false;
            }
            long totalDurationNanos = 0;
            for (PerformanceTestResults results : resultsList)
            {
                totalDurationNanos += results.getDurationNanos();
            }
            if (totalDurationNanos / 1000000 < warmupDuration)
            {
                return false;
            }

            // once warmup is done, clear the results so that stability checks
            // aren't using the warmup passes
            resultsList.clear();
            warmedUp = true;
            currentPass = 0;
        }
        if (isStable(resultsList) && (resultsList.size() >= runsToAverage + RESULTS_REMOVED_BY_PRUNING || runsToAverage == 0))
        {
            return currentPass > 0;
        }
        if (currentPass >= maxPasses)
        {
            Assert.fail("Exceeded max passes for test");
        }

        return false;
    }

    private void configureFromAnnotation(PerformanceTest configuration)
    {
        if (configuration != null)
        {
            if (configuration.warmupPasses() >= 0)
            {
                warmupPasses = configuration.warmupPasses();
            }
            if (configuration.warmupDuration() >= 0)
            {
                warmupDuration = configuration.warmupDuration();
            }
            if (configuration.maxPasses() >= 0)
            {
                maxPasses = configuration.maxPasses();
            }
            if (configuration.stablePasses() >= 0)
            {
                stablePasses = configuration.stablePasses();
            }
            if (configuration.runsToAverage() >= 0)
            {
                runsToAverage = configuration.runsToAverage();
            }
            if (configuration.duration() >= 0)
            {
                expectedDuration = configuration.duration();
            }
            if (!configuration.testName().isEmpty())
            {
                testName = configuration.testName();
            }
            if (!configuration.groupName().isEmpty())
            {
                groupName = configuration.groupName();
            }
        }
    }

    PerformanceTestResults getAverageResults()
    {
        Set<String> attributes = findAllAttributes(resultsList);
        Map<String, Double> attributeAverages = new HashMap<String, Double>();
        for (String attributeName : attributes)
        {
            List<Double> resultList = getResultsForAttribute(attributeName, resultsList);
            List<Double> prunedResults;
            if (runsToAverage > 0)
            {
                prunedResults = getPrunedResults(resultList, runsToAverage);
            }
            else
            {
                prunedResults = Collections.singletonList(resultList.get(resultList.size() - 1));
            }
            double total = 0;
            for (Double result : prunedResults)
            {
                total += result;
            }
            double average = total / prunedResults.size();
            // TODO: we don't want to average all attributes (like duration)
            attributeAverages.put(attributeName, average);
        }
        PerformanceTestResults values = resultsList.get(0);
        return new PerformanceTestResultsImpl(values.getBuildInfo(), values.getTestGroupName(), values.getTestName(), attributeAverages);
    }

    private boolean isStable(List<PerformanceTestResults> resultsList)
    {
        if (stablePasses == 0)
        {
            return true;
        }
        else if (resultsList.size() < (stablePasses + RESULTS_REMOVED_BY_PRUNING))
        {
            return false;
        }
        else
        {
            double stabilityPercentage = Double.parseDouble(PropertiesHelper.getProperty("build.performance.stability.percentage", "5"));
            Set<String> attributes = findAllAttributes(resultsList);

            Map<String, List<Double>> attributeResultLists = new HashMap<String, List<Double>>();

            for (String attributeName : attributes)
            {
                List<Double> resultList = getResultsForAttribute(attributeName, resultsList);
                attributeResultLists.put(attributeName, resultList);
            }

            for (String attributeName : attributes)
            {
                List<Double> resultList = attributeResultLists.get(attributeName);
                if (!isAttributeStable(attributeName, resultList, stabilityPercentage))
                {
                    return false;
                }
            }
            return true;
        }
    }

    private List<Double> getResultsForAttribute(String attributeName, List<PerformanceTestResults> resultsList)
    {
        List<Double> resultList = new LinkedList<Double>();
        for (PerformanceTestResults testResult : resultsList)
        {
            Double result = testResult.asMap().get(attributeName);
            resultList.add(result);
        }
        return resultList;
    }

    private boolean isAttributeStable(String attributeName, List<Double> resultList, double stabilityPercentage)
    {
        LinkedList<Double> prunedResults = getPrunedResults(resultList, stablePasses);

        double first = prunedResults.getFirst();
        double last = prunedResults.getLast();

        double diff = Math.abs(last - first);
        double percent = diff * 100 / first;

        if (percent <= stabilityPercentage)
        {
            return true;
        }
        else
        {
            LOG.info("Result for " + attributeName + " was too different: " + diff + " (" + String.format("%.2f", percent) + "%)");
            return false;
        }
    }

    private LinkedList<Double> getPrunedResults(List<Double> resultList, int resultCount)
    {
        LinkedList<Double> prunedResults = new LinkedList<Double>();
        prunedResults.addAll(resultList.subList(resultList.size() - resultCount - RESULTS_REMOVED_BY_PRUNING, resultList.size()));
        Collections.sort(prunedResults);

        prunedResults.removeFirst();
        prunedResults.removeLast();
        return prunedResults;
    }

    private Set<String> findAllAttributes(List<PerformanceTestResults> resultsList)
    {
        Set<String> attributeNames = new HashSet<String>();
        for (PerformanceTestResults results : resultsList)
        {
            attributeNames.addAll(results.asMap().keySet());
        }
        return attributeNames;
    }

}

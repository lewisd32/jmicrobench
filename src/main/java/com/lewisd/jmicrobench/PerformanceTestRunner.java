package com.lewisd.jmicrobench;

import java.io.IOException;
import java.sql.SQLException;
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
    private static final double DEFAULT_STABILITY_PERCENTAGE= Double.parseDouble(PropertiesHelper.getProperty("build.performance.stability.percentage", "5"));
    private static final String DEFAULT_PROJECT_NAME = PropertiesHelper.getProperty("build.performance.project.name", "");

    private static final Logger LOG = Logger.getLogger(PerformanceTestRunner.class);

    private boolean warmedUp = false;
    private int warmupPasses;
    private long warmupDuration;
    private int maxPasses;
    private int stablePasses;
    private double stabilityPercentage;
    private int runsToAverage;
    private long expectedDuration;
    private long totalDurationNanos;
    private String testName;
    private String groupName;
    private String projectName;
    private final Class testClass;

    private long startTimeNanos = -1;
    private long endTimeNanos = -1;
    private int currentPass;
    private ResultsList resultsList;

    public PerformanceTestRunner(Class testClass) throws InitializationError
    {
        super(testClass);
        this.testClass = testClass;
    }

    public PerformanceTestResults getAverageResults()
    {
        return resultsList.getAverageResults();
    }

    public boolean testHasRunLongEnough()
    {
        if (expectedDuration < 0)
        {
            return true;
        }
        else
        {
            long timeSpent = TimeUnit.NANOSECONDS.toMillis(getActualDurationNanos());
            return (timeSpent >= expectedDuration);
        }
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
        configure(method);
        
        resultsList = new ResultsList(runsToAverage, stablePasses, stabilityPercentage);

        currentPass = 0;
        while (!isTestDone())
        {
            runPass(method, statement);
        }
        PerformanceTestResults averageResults = resultsList.getAverageResults();
        try
        {
            new ResultsRecorder(projectName).storeResults(averageResults);
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

    private void runPass(FrameworkMethod method, Statement statement) throws Throwable
    {
        InProgressPerformanceTestResults results = new InProgressPerformanceTestResults(BuildInfoImpl.getCurrentBuild(), groupName, testName, this);
        PerformanceTestController.setupTest(results, this);
        totalDurationNanos = 0;
        startTimeNanos = -1;
        endTimeNanos = -1;
        currentPass++;
        LOG.info("Running " + (warmedUp ? "real " : "warmup ") + "pass for " + testName(method));
        // add the results class before we run, so that it's counted by
        // calls to isDone from within the test method
        resultsList.add(results);

        statement.evaluate();
        while (!testHasRunLongEnough())
        {
            statement.evaluate();
        }
        
        if (!results.hasDurationNanos())
        {
            results.setDurationNanos(getActualDurationNanos());
        }
    }

    @SuppressWarnings("unchecked")
    private PerformanceTest findClassAnnotation()
    {
        return (PerformanceTest) testClass.getAnnotation(PerformanceTest.class);
    }

    public boolean isTestDone()
    {
        if (!testHasRunLongEnough())
        {
            return false;
        }
        if (!warmedUp)
        {
            if (currentPass < warmupPasses)
            {
                return false;
            }
            if (TimeUnit.NANOSECONDS.toMillis(resultsList.getTotalDurationNanos()) < warmupDuration)
            {
                return false;
            }

            // once warmup is done, clear the results so that stability checks
            // aren't using the warmup passes
            resultsList.clear();
            warmedUp = true;
            currentPass = 0;
        }
        if (resultsList.isStable() && resultsList.hasEnoughResults())
        {
            return currentPass > 0;
        }
        if (currentPass >= maxPasses)
        {
            Assert.fail("Exceeded max passes for test");
        }

        return false;
    }
    
    private void configure(FrameworkMethod method)
    {
        configureDefaults(method);

        PerformanceTest configuratinAnnotation;

        configuratinAnnotation = findClassAnnotation();
        configureFromAnnotation(configuratinAnnotation);

        configuratinAnnotation = method.getAnnotation(PerformanceTest.class);
        configureFromAnnotation(configuratinAnnotation);

        if (projectName.equals(""))
        {
            throw new IllegalStateException("'projectName' must be configured in jmicrobench.properties on PerformanceTest annotation");
        }
        
        warmedUp = false;
        if (warmupPasses == 0 && warmupDuration == 0)
        {
            warmedUp = true;
        }
    }

    private void configureDefaults(FrameworkMethod method)
    {
        this.maxPasses = DEFAULT_MAX_PASSES;
        this.stablePasses = DEFAULT_STABLE_PASSES;
        this.stabilityPercentage = DEFAULT_STABILITY_PERCENTAGE;
        this.runsToAverage = DEFAULT_RUNS_TO_AVERAGE;
        this.expectedDuration = DEFAULT_EXPECTED_DURATION;
        this.warmupPasses = DEFAULT_WARMUP_PASSES;
        this.warmupDuration = DEFAULT_WARMUP_DURATION;
        this.groupName = method.getName();
        this.testName = testClass.getSimpleName();
        this.projectName = DEFAULT_PROJECT_NAME;
    }

    private void configureFromAnnotation(PerformanceTest configuration)
    {
        if (configuration != null)
        {
            if (configuration.warmupPasses() >= 0)
            {
                warmupPasses = configuration.warmupPasses();
            }
            if (configuration.warmupDurationMillis() >= 0)
            {
                warmupDuration = configuration.warmupDurationMillis();
            }
            if (configuration.maxPasses() >= 0)
            {
                maxPasses = configuration.maxPasses();
            }
            if (configuration.stablePasses() >= 0)
            {
                stablePasses = configuration.stablePasses();
            }
            if (configuration.stabilityPercentage() >= 0)
            {
                stabilityPercentage = configuration.stabilityPercentage();
            }
            if (configuration.runsToAverage() >= 0)
            {
                runsToAverage = configuration.runsToAverage();
            }
            if (configuration.durationMillis() >= 0)
            {
                expectedDuration = configuration.durationMillis();
            }
            if (!configuration.testName().isEmpty())
            {
                testName = configuration.testName();
            }
            if (!configuration.groupName().isEmpty())
            {
                groupName = configuration.groupName();
            }
            if (!configuration.projectName().isEmpty())
            {
                projectName = configuration.projectName();
            }
        }
    }

}

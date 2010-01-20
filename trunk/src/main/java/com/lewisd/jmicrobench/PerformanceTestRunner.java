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
    private static final Logger LOG = Logger.getLogger(PerformanceTestRunner.class);

    private boolean warmedUp = false;
    private int warmupPasses;
    private long warmupDuration;
    private int maxPasses;
    private int stablePasses;
    private double stabilityPercentage;
    private int runsToAverage;
    private long expectedDuration;
    private boolean runBeforeAndAftersEachPass;
    private String testName;
    private String groupName;
    private String projectName;
    private final Class testClass;

    private long totalDurationNanos;
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
        configure(method);
        if (runBeforeAndAftersEachPass)
        {
            return withRunUntilStable(method, target, super.withAfters(method, target, statement));
        }
        else
        {
            return super.withBefores(method, target, super.withAfters(method, target, withRunUntilStable(method, target, statement)));
        }
    }
    
    @Override
    protected Statement withBefores(FrameworkMethod method, Object target, Statement statement)
    {
        configure(method);
        if (runBeforeAndAftersEachPass)
        {
            return super.withBefores(method, target, statement);
        }
        else
        {
            return statement;
        }
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
        if (resultsList.hasEnoughResults())
        {
            return currentPass > 0;
        }
        if (currentPass >= maxPasses)
        {
            Assert.fail("Exceeded max passes for test");
        }

        return false;
    }
    
    private void configure(final FrameworkMethod method)
    {
        Configuration configuration = new Configuration(method, testClass);
        
        this.maxPasses = configuration.getMaxPasses();
        this.stablePasses = configuration.getStablePasses();
        this.stabilityPercentage = configuration.getStabilityPercentage();
        this.runsToAverage = configuration.getRunsToAverage();
        this.expectedDuration = configuration.getExpectedDuration();
        this.warmupPasses = configuration.getWarmupPasses();
        this.warmupDuration = configuration.getWarmupDuration();
        this.groupName = configuration.getGroupName();
        this.testName = configuration.getTestName();
        this.projectName = configuration.getProjectName();
        this.runBeforeAndAftersEachPass = configuration.getRunBeforeAndAftersEachPass();
        
        warmedUp = false;
        if (warmupPasses == 0 && warmupDuration == 0)
        {
            warmedUp = true;
        }
    }

}

package com.lewisd.jmicrobench;

import java.util.concurrent.TimeUnit;

import junit.framework.AssertionFailedError;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.lewisd.test.Assert;

@RunWith(PerformanceTestRunner.class)
public class PerformanceTestRunnerTest
{

    private static final long DURATION = 2000;

    private PerformanceTestResultsCollector results = new PerformanceTestResultsCollector("test");
    private int shouldRunForAtLeastStablePassesCounter = 0;
    private int shouldRunForAtMostMaxPassesCounter = 0;
    private int shouldRunForAtLeastRunsToAveragePassesCounter = 0;
    private int shouldRunForMoreThanWarmupCounter = 0;
    private int shouldRunForOnePassAfterWarmupDurationCounter = 0;
    private int shouldRunForMaxPassesAfterWarmupDurationCounter = 0;
    private int shouldWaitForEnoughStablePassesCounter = 0;
    private int shouldAverageResultsCounter = 0;

    @Before
    public void setUp() throws Exception
    {
        // do nothing
    }

    @Test
    @PerformanceTest(duration = DURATION, runsToAverage = 1, stablePasses = 1)
    public void shouldRunForAtLeastSpecifiedDuration() throws Exception
    {
        results.startDurationTimer();
        long startTime = System.currentTimeMillis();
        while (results.shouldRun())
        {
            Thread.sleep(10);
        }
        long endTime = System.currentTimeMillis();
        Assert.assertTrue("Duration of " + (endTime - startTime) + " was not more than expected duration", endTime - startTime >= DURATION);
    }

    @Test
    @PerformanceTest(duration = 100, runsToAverage = 1, stablePasses = 3)
    public void shouldRunForAtLeastStablePasses() throws Exception
    {
        shouldRunForAtLeastStablePassesCounter++;
        while (results.shouldRun())
        {
            Thread.sleep(1);
        }
        results.addNumberOfOperations(1);
        results.setDurationNanos(100L);
        if (results.isDone())
        {
            Assert.assertEquals("Wrong number of passes.", 5, shouldRunForAtLeastStablePassesCounter);
        }
        else if (shouldRunForAtLeastStablePassesCounter >= 5)
        {
            Assert.fail("Called " + shouldRunForAtLeastStablePassesCounter + " times but isDone() was not true");
        }
    }

    @Test
    @PerformanceTest(runsToAverage = 1, stablePasses = 3)
    public void shouldWaitForEnoughStablePasses() throws Exception
    {
        shouldWaitForEnoughStablePassesCounter++;
        int ops;
        switch (shouldWaitForEnoughStablePassesCounter)
        {
        case 1:
            ops = 3;
            break;
        case 2:
            ops = 1;
            break;
        case 3:
            ops = 2;
            break;
        case 4:
            ops = 4;
            break;
        case 5:
            ops = 2;
            break;
        case 6:
            ops = 2;
            break;
        default:
            ops = 0;
            Assert.fail("Too many passes");
        }
        results.addNumberOfOperations(ops);
        results.setDurationNanos(100L);
        if (results.isDone())
        {
            Assert.assertEquals("Wrong number of passes.", 6, shouldWaitForEnoughStablePassesCounter);
        }
    }

    @Test
    @PerformanceTest(runsToAverage = 3, stablePasses = 0)
    public void shouldAverageResults() throws Exception
    {
        shouldAverageResultsCounter++;
        int ops;
        switch (shouldAverageResultsCounter)
        {
        case 1:
            ops = 5;
            break;
        case 2:
            ops = 1;
            break;
        case 3:
            ops = 2;
            break;
        case 4:
            ops = 7;
            break;
        case 5:
            ops = 2;
            break;
        default:
            ops = 0;
            Assert.fail("Too many passes");
        }
        results.addNumberOfOperations(ops);
        results.setDurationNanos(TimeUnit.SECONDS.toNanos(1));
        if (results.isDone())
        {
            Assert.assertEquals("Wrong number of passes.", 5, shouldAverageResultsCounter);
            Assert.assertEquals("", 3.0, results.getAveragedResults().getOperationsPerSecond());

        }
    }

    @Test
    @PerformanceTest(runsToAverage = 0, stablePasses = 0)
    public void shouldRecordSinglePassWhenRunsToAverageIsZero() throws Exception
    {
        shouldAverageResultsCounter++;
        int ops;
        switch (shouldAverageResultsCounter)
        {
        case 1:
            ops = 5;
            break;
        default:
            ops = 0;
            Assert.fail("Too many passes");
        }
        results.addNumberOfOperations(ops);
        results.setDurationNanos(TimeUnit.SECONDS.toNanos(1));
        if (results.isDone())
        {
            Assert.assertEquals("Wrong number of passes.", 1, shouldAverageResultsCounter);
            Assert.assertEquals("", 5.0, results.getAveragedResults().getOperationsPerSecond());

        }
    }

    @Test
    @PerformanceTest(duration = 100, runsToAverage = 3, stablePasses = 1)
    public void shouldRunForAtLeastRunsToAveragePasses() throws Exception
    {
        shouldRunForAtLeastRunsToAveragePassesCounter++;
        while (results.shouldRun())
        {
            Thread.sleep(1);
        }
        results.addNumberOfOperations(1);
        results.setDurationNanos(100L);
        if (results.isDone())
        {
            Assert.assertEquals("Too many passes", 5, shouldRunForAtLeastRunsToAveragePassesCounter);
        }
        else if (shouldRunForAtLeastRunsToAveragePassesCounter >= 5)
        {
            Assert.fail("Called " + shouldRunForAtLeastRunsToAveragePassesCounter + " times but isDone() was not true");
        }
    }

    @Ignore("This test is expected to fail with an exception about exceeding max passes")
    @Test
    @PerformanceTest(runsToAverage = 1, stablePasses = 2, maxPasses = 3, warmupPasses = 0)
    public void shouldRunForAtMostMaxPasses() throws Exception
    {
        shouldRunForAtMostMaxPassesCounter++;
        // adding this counter will make sure the results are "unstable"
        results.addNumberOfOperations(shouldRunForAtMostMaxPassesCounter);
        results.setDurationNanos(100L);
        if (shouldRunForAtMostMaxPassesCounter < 3)
        {
            Assert.assertFalse(results.isDone());
        }
        else if (shouldRunForAtMostMaxPassesCounter == 3)
        {
            try
            {
                results.isDone();
                Assert.fail("Was expecting exception");
            }
            catch (AssertionFailedError e)
            {
                Assert.assertStartsWith("Exceeded max passes for test", e.getMessage());
            }
        }
        else if (shouldRunForAtMostMaxPassesCounter > 3)
        {
            Assert.fail("Ran more than 3 passes");
        }
    }

    @Test
    @PerformanceTest(runsToAverage = 0, stablePasses = 0, warmupPasses = 2)
    public void shouldRunForMoreThanWarmupPasses() throws Exception
    {
        shouldRunForMoreThanWarmupCounter++;
        results.setDurationNanos(100L);
        results.addNumberOfOperations(1);
        if (results.isDone())
        {
            Assert.assertTrue("Expected 3 passes, was " + shouldRunForMoreThanWarmupCounter, shouldRunForMoreThanWarmupCounter == 3);
        }
        else if (shouldRunForMoreThanWarmupCounter >= 3)
        {
            Assert.fail("Called " + shouldRunForMoreThanWarmupCounter + " times but isDone() was not true");
        }
    }

    @Test
    @PerformanceTest(duration = 1000, runsToAverage = 0, stablePasses = 0, warmupDuration = 1900)
    public void shouldRunForOnePassAfterWarmupDuration() throws Exception
    {
        shouldRunForOnePassAfterWarmupDurationCounter++;
        while (results.shouldRun())
        {
            Thread.sleep(1);
        }
        results.addNumberOfOperations(1);
        results.setDurationNanos(TimeUnit.MILLISECONDS.toNanos(1000L));
        if (results.isDone())
        {
            Assert.assertTrue("Expected 3 passes, was " + shouldRunForOnePassAfterWarmupDurationCounter, shouldRunForOnePassAfterWarmupDurationCounter == 3);
        }
        else if (shouldRunForOnePassAfterWarmupDurationCounter >= 3)
        {
            Assert.fail("Called " + shouldRunForOnePassAfterWarmupDurationCounter + " times but isDone() was not true");
        }
    }

    @Test
    @PerformanceTest(duration = 1000, runsToAverage = 0, stablePasses = 0)
    public void shouldBeAbleToRetrieveOpsPerSecFromWithinTestWithoutStoppingTimer() throws Exception
    {
        while (results.shouldRun())
        {
            Thread.sleep(1);
        }
        results.addNumberOfOperations(3000);
        double opsPerSec = results.getOperationsPerSecond();
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec > 2000);
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec < 4000);
    }

    @Test
    @PerformanceTest(duration = 1000, runsToAverage = 0, stablePasses = 0)
    public void shouldBeAbleToRetrieveOpsPerSecFromWithinTestAfterStoppingTimer() throws Exception
    {
        while (results.shouldRun())
        {
            Thread.sleep(1);
        }
        results.addNumberOfOperations(3000);
        results.stopDurationTimer();
        Thread.sleep(2000);
        double opsPerSec = results.getOperationsPerSecond();
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec > 2000);
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec < 4000);
    }

    @Test
    @PerformanceTest(duration = 1000, runsToAverage = 0, stablePasses = 3)
    public void shouldGetPositiveOpsPerSecond() throws Exception
    {
        while (results.shouldRun())
        {
            Thread.sleep(1);
        }
        results.addNumberOfOperations(3000);
        double opsPerSec = results.getOperationsPerSecond();
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec > 2000);
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec < 4000);
    }

    @Test
    @PerformanceTest(duration = 1000, runsToAverage = 0, stablePasses = 0)
    public void shouldSumUpAllDurationPeriods() throws Exception
    {

        results.startDurationTimer();
        Thread.sleep(1000);
        results.stopDurationTimer();

        Thread.sleep(1000);

        results.startDurationTimer();
        Thread.sleep(1000);
        results.stopDurationTimer();

        long duration = results.getDurationMillis();
        Assert.assertTrue("Expected duration to be over 2000, but was " + duration, duration >= 2000);
        Assert.assertTrue("Expected duration to be around 2000, but was " + duration, duration < 2500);
    }

    @Test
    @PerformanceTest(warmupPasses = 1, warmupDuration = 1000, maxPasses = 3, runsToAverage = 1, stablePasses = 0)
    public void shouldRunForMaxPassesAfterWarmupDuration() throws Exception
    {
        shouldRunForMaxPassesAfterWarmupDurationCounter++;
        results.setDurationNanos(TimeUnit.MILLISECONDS.toNanos(100));
        if (shouldRunForMaxPassesAfterWarmupDurationCounter > 13)
        {
            Assert.fail("Should not run for more than 13 passes");
        }
        if (results.isDone())
        {
            Assert.assertEquals(13, shouldRunForMaxPassesAfterWarmupDurationCounter);
        }
    }

    public class DummyException extends Exception
    {
        // intentionally empty
    }

}

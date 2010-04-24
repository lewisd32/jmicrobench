package com.lewisd.jmicrobench;

import java.util.concurrent.TimeUnit;

import junit.framework.AssertionFailedError;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.lewisd.test.Assert;

@RunWith(PerformanceTestRunner.class)
@PerformanceTest(projectName = "test")
public class PerformanceTestRunnerTest
{
    private static final Logger LOGGER = Logger.getLogger(PerformanceTestRunnerTest.class);

    private static final long DURATION = 2000;

    private PerformanceTestController controller = new PerformanceTestController();
    private int shouldResetResultsBeforeEachPassCounter = 0;
    private int shouldRunForAtLeastStablePassesCounter = 0;
    private int shouldRunForAtMostMaxPassesCounter = 0;
    private int shouldRunForAtLeastRunsToAveragePassesCounter = 0;
    private int shouldRunForMoreThanWarmupCounter = 0;
    private int shouldRunForOnePassAfterWarmupDurationCounter = 0;
    private int shouldRunForMaxPassesAfterWarmupDurationCounter = 0;
    private int shouldWaitForEnoughStablePassesCounter = 0;
    private int shouldAverageResultsCounter = 0;
    private int shouldRunForAtLeastSpecifiedDurationWithLoopInTestMethod = 0;
    private int shouldRunForAtLeastSpecifiedDurationWithNoLoopInTestMethodCounter = 0;
    
    @Test
    @PerformanceTest(durationMillis = DURATION, runsToAverage = 1, stablePasses = 1)
    public void shouldRunForAtLeastSpecifiedDurationWithLoopInTestMethod() throws Exception
    {
        shouldRunForAtLeastSpecifiedDurationWithLoopInTestMethod++;
        controller.startDurationTimer();
        long startTime = System.nanoTime();
        while (controller.shouldLoop())
        {
            Thread.sleep(10);
        }
        long endTime = System.nanoTime();
        long actualDuration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        Assert.assertTrue("Duration of " + (endTime - startTime) + " was not more than expected duration", actualDuration >= DURATION);
        
        if (shouldRunForAtLeastSpecifiedDurationWithLoopInTestMethod > 3)
        {
            Assert.fail("Too many passes");
        }
        if (controller.isTestDone())
        {
            Assert.assertEquals("Wrong number of passes.", 3, shouldRunForAtLeastSpecifiedDurationWithLoopInTestMethod);
        }

    }

    @Test
    @PerformanceTest(durationMillis = DURATION, runsToAverage = 0, stablePasses = 0)
    public void shouldRunForAtLeastSpecifiedDurationWithNoLoopInTestMethod() throws Exception
    {
        PerformanceTestResults results = controller.getResults();
        shouldRunForAtLeastSpecifiedDurationWithNoLoopInTestMethodCounter++;
        controller.startDurationTimer();
        long startTime = System.nanoTime();
        while (System.nanoTime() - startTime < TimeUnit.MILLISECONDS.toNanos(100))
        {
            Thread.sleep(1);
        }
        controller.stopDurationTimer();

        final long durationNanos = results.getDurationNanos();
        Assert.assertTrue(TimeUnit.NANOSECONDS.toMillis(durationNanos) >= shouldRunForAtLeastSpecifiedDurationWithNoLoopInTestMethodCounter*100);
        Assert.assertTrue(TimeUnit.NANOSECONDS.toMillis(durationNanos) <= shouldRunForAtLeastSpecifiedDurationWithNoLoopInTestMethodCounter*105);
        
        if (shouldRunForAtLeastSpecifiedDurationWithNoLoopInTestMethodCounter > 20)
        {
            Assert.fail("Too many passes");
        }
        if (controller.isTestDone())
        {
            Assert.assertEquals("Wrong number of passes.", 20, shouldRunForAtLeastSpecifiedDurationWithNoLoopInTestMethodCounter);
        }
    }


    @Test
    @PerformanceTest(durationMillis = 100, runsToAverage = 1, stablePasses = 3)
    public void shouldRunForAtLeastStablePasses() throws Exception
    {
        shouldRunForAtLeastStablePassesCounter++;
        while (controller.shouldLoop())
        {
            Thread.sleep(1);
        }
        controller.addNumberOfOperations(1);
        controller.setDurationNanos(100L);
        if (controller.isTestDone())
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
    public void shouldResetResultsBeforeEachPass() throws Exception
    {
        PerformanceTestResults results = controller.getResults();
        Assert.assertFalse(results.hasNumberOfOperations());
        Assert.assertFalse(results.hasOperationsPerSecond());
        shouldResetResultsBeforeEachPassCounter++;
        int ops;
        switch (shouldResetResultsBeforeEachPassCounter)
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
        controller.addNumberOfOperations(ops);
        controller.setDurationNanos(100L);
        if (controller.isTestDone())
        {
            Assert.assertEquals("Wrong number of passes.", 6, shouldResetResultsBeforeEachPassCounter);
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
        controller.addNumberOfOperations(ops);
        controller.setDurationNanos(100L);
        if (controller.isTestDone())
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
        controller.addNumberOfOperations(ops);
        controller.setDurationNanos(TimeUnit.SECONDS.toNanos(1));
        if (controller.isTestDone())
        {
            Assert.assertEquals("Wrong number of passes.", 5, shouldAverageResultsCounter);
            Assert.assertEquals("", 3.0, controller.getAveragedResults().getOperationsPerSecond());

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
        controller.addNumberOfOperations(ops);
        controller.setDurationNanos(TimeUnit.SECONDS.toNanos(1));
        if (controller.isTestDone())
        {
            Assert.assertEquals("Wrong number of passes.", 1, shouldAverageResultsCounter);
            Assert.assertEquals("", 5.0, controller.getAveragedResults().getOperationsPerSecond());

        }
    }

    @Test
    @PerformanceTest(durationMillis = 100, runsToAverage = 3, stablePasses = 1)
    public void shouldRunForAtLeastRunsToAveragePasses() throws Exception
    {
       shouldRunForAtLeastRunsToAveragePassesCounter++;
        while (controller.shouldLoop())
        {
            Thread.sleep(1);
        }
        controller.addNumberOfOperations(1);
        controller.setDurationNanos(100L);
        if (controller.isTestDone())
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
        controller.addNumberOfOperations(shouldRunForAtMostMaxPassesCounter);
        controller.setDurationNanos(100L);
        if (shouldRunForAtMostMaxPassesCounter < 3)
        {
            Assert.assertFalse(controller.isTestDone());
        }
        else if (shouldRunForAtMostMaxPassesCounter == 3)
        {
            try
            {
                controller.isTestDone();
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
        controller.setDurationNanos(100L);
        controller.addNumberOfOperations(1);
        if (controller.isTestDone())
        {
            Assert.assertTrue("Expected 3 passes, was " + shouldRunForMoreThanWarmupCounter, shouldRunForMoreThanWarmupCounter == 3);
        }
        else if (shouldRunForMoreThanWarmupCounter >= 3)
        {
            Assert.fail("Called " + shouldRunForMoreThanWarmupCounter + " times but isDone() was not true");
        }
    }

    @Test
    @PerformanceTest(durationMillis = 1000, runsToAverage = 0, stablePasses = 0, warmupDurationMillis = 1900)
    public void shouldRunForOnePassAfterWarmupDuration() throws Exception
    {
        shouldRunForOnePassAfterWarmupDurationCounter++;
        while (controller.shouldLoop())
        {
            Thread.sleep(1);
        }
        controller.addNumberOfOperations(1);
        controller.setDurationNanos(TimeUnit.MILLISECONDS.toNanos(1000L));
        if (controller.isTestDone())
        {
            Assert.assertTrue("Expected 3 passes, was " + shouldRunForOnePassAfterWarmupDurationCounter, shouldRunForOnePassAfterWarmupDurationCounter == 3);
        }
        else if (shouldRunForOnePassAfterWarmupDurationCounter >= 3)
        {
            Assert.fail("Called " + shouldRunForOnePassAfterWarmupDurationCounter + " times but isDone() was not true");
        }
    }

    @Test
    @PerformanceTest(durationMillis = 1000, runsToAverage = 0, stablePasses = 0)
    public void shouldBeAbleToRetrieveOpsPerSecFromWithinTestWithoutStoppingTimer() throws Exception
    {
        PerformanceTestResults results = controller.getResults();
        while (controller.shouldLoop())
        {
            Thread.sleep(1);
        }
        controller.addNumberOfOperations(3000);
        double opsPerSec = results.getOperationsPerSecond();
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec > 2000);
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec < 4000);
    }

    @Test
    @PerformanceTest(durationMillis = 1000, runsToAverage = 0, stablePasses = 0)
    public void shouldBeAbleToRetrieveOpsPerSecFromWithinTestAfterStoppingTimer() throws Exception
    {
        PerformanceTestResults results = controller.getResults();
        while (controller.shouldLoop())
        {
            Thread.sleep(1);
        }
        controller.addNumberOfOperations(3000);
        controller.stopDurationTimer();
        Thread.sleep(2000);
        double opsPerSec = results.getOperationsPerSecond();
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec > 2000);
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec < 4000);
    }

    @Test
    @PerformanceTest(durationMillis = 1000, runsToAverage = 0, stablePasses = 3)
    public void shouldGetPositiveOpsPerSecond() throws Exception
    {
        PerformanceTestResults results = controller.getResults();
        while (controller.shouldLoop())
        {
            Thread.sleep(1);
        }
        controller.addNumberOfOperations(3000);
        double opsPerSec = results.getOperationsPerSecond();
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec > 2000);
        Assert.assertTrue("Expected opsPerSec to be nearly 3000, but was " + opsPerSec, opsPerSec < 4000);
    }

    @Test
    @PerformanceTest(durationMillis = 1000, runsToAverage = 0, stablePasses = 0)
    public void shouldSumUpAllDurationPeriods() throws Exception
    {
        PerformanceTestResults results = controller.getResults();

        controller.startDurationTimer();
        Thread.sleep(1001);
        controller.stopDurationTimer();

        Thread.sleep(1000);

        controller.startDurationTimer();
        Thread.sleep(1001);
        controller.stopDurationTimer();

        long duration = TimeUnit.NANOSECONDS.toMillis(results.getDurationNanos());
        Assert.assertTrue("Expected duration to be over 2000, but was " + duration, duration >= 2000);
        Assert.assertTrue("Expected duration to be around 2000, but was " + duration, duration < 2500);
    }
    
    @Test
    @PerformanceTest(runsToAverage = 0, stablePasses = 0)
    public void shouldSumUpAllDurationPeriodsUsingDifferentControllers() throws Exception
    {
        PerformanceTestController controller2 = new PerformanceTestController();
        PerformanceTestResults results = controller.getResults();

        controller.startDurationTimer();
        Thread.sleep(1000);
        controller2.stopDurationTimer();

        long duration = TimeUnit.NANOSECONDS.toMillis(results.getDurationNanos());
        Assert.assertTrue("Expected duration to be over 1000, but was " + duration, duration >= 1000);
        Assert.assertTrue("Expected duration to be around 1000, but was " + duration, duration < 1200);
    }

    @Test
    @PerformanceTest(warmupPasses = 1, warmupDurationMillis = 1000, maxPasses = 3, runsToAverage = 1, stablePasses = 0)
    public void shouldRunForMaxPassesAfterWarmupDuration() throws Exception
    {
        shouldRunForMaxPassesAfterWarmupDurationCounter++;
        controller.setDurationNanos(TimeUnit.MILLISECONDS.toNanos(100));
        if (shouldRunForMaxPassesAfterWarmupDurationCounter > 13)
        {
            Assert.fail("Should not run for more than 13 passes");
        }
        if (controller.isTestDone())
        {
            Assert.assertEquals(13, shouldRunForMaxPassesAfterWarmupDurationCounter);
        }
    }
}

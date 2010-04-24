package com.lewisd.jmicrobench;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.lewisd.test.Assert;

@RunWith(PerformanceTestRunner.class)
@PerformanceTest(projectName = "test")
public class PerformanceTestRunnerRunsBeforesEveryPassTest
{
    private PerformanceTestController controller = new PerformanceTestController();
    
    private static int setupRuns = 0;
    private static int teardownRuns = 0;
    
    @BeforeClass
    public static void beforeClass()
    {
        setupRuns = 0;
        teardownRuns = 0;
    }

    @Before
    public void setUp()
    {
        ++setupRuns;
    }
    
    @After
    public void tearDown()
    {
        ++teardownRuns;
    }
    
    @Test
    @PerformanceTest(durationMillis = 0, runsToAverage = 1, stablePasses = 0)
    public void shouldRunSetupForEachPass() throws Exception
    {
        if (controller.isTestDone())
        {
            Assert.assertEquals(3, setupRuns);
        }
    }

}

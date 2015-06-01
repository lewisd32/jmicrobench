# Testing throughput #

**microbench.properties**
```
build.performance.stability.percentage=5
build.performance.project.name=something
build.performance.warmup.passes=3
build.performance.max.passes=100
build.performance.stable.passes=0
build.performance.averaged.runs=3
build.performance.duration=1000
build.performance.warmup.duration=3000
```
  * Run at least 3 warmup passes
  * Run warmup passes for at least 3000 milliseconds
  * For each pass, run the method for at least 1000 milliseconds
  * Report the average of 3 passes, after discarding the highest and lowest results (for a total run of 5 passes)
  * Wait until 3 passes have produced results within 5% of each other (stable.passes and stability.percentage)
  * If more than 100 passes are performed without getting stable results, fail the test

**SomeComponentPerfTest.java**
```
@RunWith(PerformanceTestRunner.class)
@PerformanceTest(groupName="Some Component Name")
public class SomeComponentPerfTest
{
    private static final int ITERATIONS = 10000;
    private SomeComponent someComponent;

    @Before
    public void setUp()
    {
        someComponent = new SomeComponent();
    }

    @Test
    public void testThroughput()
    {
        for (int x = 0; x < ITERATIONS; ++x)
        {
            someComponent.doSomething();
        }
        new PerformanceTestController().addNumberOfOperations(ITERATIONS);
    }
}
```

With the above jmicrobench.properties, the each performance test pass of the testThroughput method will run it (and any @Before and @After methods) until the total recorded time spent in the method has reached 1000ms.

In addition, warmup passes (passes where results are discarded, in order to warm up the JIT) will be run until here have been 3 passes performed, or the total time spent in the method reaches 3000ms.

At this point, the framework will run 5 more passes (of at least 1000ms each), discarding the best and worse results, and using the remaining 3 to calculate average results.  However, if these remaining three are not within 5% of each other, it will continue to run further passes until 3 of the last 5 are within 5% of each other, or it exceeds 100 passes.

If you have multiple test methods within a single class, the @Before method may not be able to do all of the setup required for each test method.  In this case, you can do the setup in the test method, but tell jmicrobench not to record the time taken for that.

```
    @Test
    public void testThroughput()
    {
        someComponent.doSetup();
        controller.startDurationTimer();
        for (int x = 0; x < ITERATIONS; ++x)
        {
            someComponent.doSomething();
        }
        controller.stopDurationTimer();
        new PerformanceTestController().addNumberOfOperations(ITERATIONS);
    }
```

The call to stopDurationTimer() is somewhat optional, but will give more accurate results.  If it is not called explicitly, jmicrobench will call it as soon as control is returned to it after executing the test method (before calling any @After methods), however calling it immediately after the section of code who's performance you are testing can be more accurate.

Similarly, calling startDurationTimer() is also optional, but will be called by jmicrobench before executing your test method (though after it's @Before methods), so will include any setup done within the test method.
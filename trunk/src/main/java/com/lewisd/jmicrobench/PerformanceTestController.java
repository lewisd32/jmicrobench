package com.lewisd.jmicrobench;

public class PerformanceTestController
{
    private static ThreadLocal<InProgressPerformanceTestResults> resultsThreadLocal = new ThreadLocal<InProgressPerformanceTestResults>();
    private static ThreadLocal<PerformanceTestRunner> runnerThreadLocal = new ThreadLocal<PerformanceTestRunner>();

    public PerformanceTestResults getResults()
    {
        return resultsThreadLocal.get();
    }

    public boolean isTestDone()
    {
        return runnerThreadLocal.get().isTestDone();
    }

    public PerformanceTestResults getAveragedResults()
    {
        return runnerThreadLocal.get().getAverageResults();
    }

    public void startDurationTimer()
    {
        runnerThreadLocal.get().startDurationTimer();
    }

    public void stopDurationTimer()
    {
        stopDurationTimer(true);
    }

    public void stopDurationTimer(boolean includeDurationInTotal)
    {
        runnerThreadLocal.get().stopDurationTimer(includeDurationInTotal);
    }

    public boolean shouldLoop()
    {
        return !runnerThreadLocal.get().testHasRunLongEnough();
    }

    public void addNumberOfOperations(int operations)
    {
        resultsThreadLocal.get().addNumberOfOperations(operations);
    }

    public void setDurationNanos(long duration)
    {
        resultsThreadLocal.get().setDurationNanos(duration);
    }

    void setupTest(InProgressPerformanceTestResults results, PerformanceTestRunner runner)
    {
        resultsThreadLocal.set(results);
        runnerThreadLocal.set(runner);
    }
}

package com.lewisd.jmicrobench;


public class InProgressPerformanceTestResults extends PerformanceTestResultsImpl
{
    
    private final PerformanceTestRunner runner;
    
    public InProgressPerformanceTestResults(BuildInfo buildInfo, String testGroupName, String testName, PerformanceTestRunner runner)
    {
        super(buildInfo, testGroupName, testName);
        this.runner = runner;
    }


    @Override
    public long getDurationNanos()
    {
        if (super.hasDurationNanos())
        {
            return super.getDurationNanos();
        }
        else
        {
            return runner.getActualDurationNanos();
        }
    }

    @Override
    public double getOperationsPerSecond()
    {
        if (super.hasOperationsPerSecond())
        {
            return super.getOperationsPerSecond();
        }
        else
        {
            return calculateOpsPerSec(getNumberOfOperations(), getDurationNanos());
        }
    }

    @Override
    public boolean hasDurationNanos()
    {
        return true;
    }

    @Override
    public boolean hasOperationsPerSecond()
    {
        return hasDurationNanos() && hasNumberOfOperations();
    }

    @Override
    public boolean hasPlottableAttribute()
    {
        return true;
    }
    
    

}

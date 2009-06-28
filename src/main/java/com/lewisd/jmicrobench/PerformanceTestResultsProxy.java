package com.lewisd.jmicrobench;

import java.util.Map;

public class PerformanceTestResultsProxy implements PerformanceTestResults
{

    private PerformanceTestResults results;

    protected void setResults(PerformanceTestResults results)
    {
        this.results = results;
    }

    public Map<String, Double> asMap()
    {
        return results.asMap();
    }

    public Long getAverageLatencyNanos()
    {
        return results.getAverageLatencyNanos();
    }

    public Long getAverageLatencyMillis()
    {
        return results.getAverageLatencyMillis();
    }

    public BuildInfo getBuildInfo()
    {
        return results.getBuildInfo();
    }

    public Long getDurationNanos()
    {
        return results.getDurationNanos();
    }

    public Long getDurationMillis()
    {
        return results.getDurationMillis();
    }

    public Long getDurationSeconds()
    {
        return results.getDurationSeconds();
    }

    public Long getMemoryBytes()
    {
        return results.getMemoryBytes();
    }

    public Long getNumberOfOperations()
    {
        return results.getNumberOfOperations();
    }

    public Double getOperationsPerSecond()
    {
        return results.getOperationsPerSecond();
    }

    public Double getPlotableAttribute()
    {
        return results.getPlotableAttribute();
    }

    public String getTestGroupName()
    {
        return results.getTestGroupName();
    }

    public String getTestName()
    {
        return results.getTestName();
    }

    public void setAverageLatencyNs(Long latency)
    {
        results.setAverageLatencyNs(latency);
    }

    public void setDurationNanos(Long duration)
    {
        results.setDurationNanos(duration);
    }

    public void setMemoryBytes(Long memory)
    {
        results.setMemoryBytes(memory);
    }

    public void setNumberOfOperations(Long operations)
    {
        results.setNumberOfOperations(operations);
    }

    public void addNumberOfOperations(Integer operations)
    {
        results.addNumberOfOperations(operations);
    }

    public void addNumberOfOperations(Long operations)
    {
        results.addNumberOfOperations(operations);
    }

    public void setOperationsPerSecond(Double opsPerSecond)
    {
        results.setOperationsPerSecond(opsPerSecond);
    }

}

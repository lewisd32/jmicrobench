package com.lewisd.jmicrobench;

import java.util.HashMap;
import java.util.Map;

public class PerformanceTestResultsImpl implements PerformanceTestResults
{

    private boolean hasAverageLatencyNanos;
    private long averageLatencyNanos;
    private boolean hasDurationNanos;
    private long durationNanos;
    private boolean hasMemoryBytes;
    private long memoryBytes;
    private boolean hasNumberOfOperations;
    private long numberOfOperations;
    private boolean hasOperationsPerSecond;
    private double operationsPerSecond;
    
    private final BuildInfo buildInfo;
    private final String testGroupName;
    private final String testName;
    long maxLatencyNanos;
    boolean hasMaxLatencyNanos;
    long minLatencyNanos;
    boolean hasMinLatencyNanos;
    double standardDeviationLatencyNanos;
    boolean hasStandardDeviationLatencyNanos;

    public PerformanceTestResultsImpl(BuildInfo buildInfo, String testGroupName, String testName)
    {
        this.buildInfo = buildInfo;
        this.testGroupName = testGroupName;
        this.testName = testName;
    }

    public PerformanceTestResultsImpl(BuildInfo buildInfo, String testGroupName, String testName, Map<String, Double> map)
    {
        this.buildInfo = buildInfo;
        this.testGroupName = testGroupName;
        this.testName = testName;
        if (map.containsKey(DURATION_NANOS))
        {
            durationNanos = map.get(DURATION_NANOS).longValue();
            hasDurationNanos = true;
        }
        if (map.containsKey(LATENCY))
        {
            averageLatencyNanos = map.get(LATENCY).longValue();
            hasAverageLatencyNanos = true;
        }
        if (map.containsKey(MIN_LATENCY))
        {
            minLatencyNanos = map.get(MIN_LATENCY).longValue();
            hasMinLatencyNanos = true;
        }
        if (map.containsKey(MAX_LATENCY))
        {
            maxLatencyNanos = map.get(MAX_LATENCY).longValue();
            hasMaxLatencyNanos = true;
        }
        if (map.containsKey(STD_DEV_LATENCY))
        {
            standardDeviationLatencyNanos = map.get(STD_DEV_LATENCY).longValue();
            hasStandardDeviationLatencyNanos = true;
        }
        if (map.containsKey(OPERATIONS))
        {
            numberOfOperations = map.get(OPERATIONS).longValue();
            hasNumberOfOperations = true;
        }
        if (map.containsKey(OPS_PER_SECOND))
        {
            operationsPerSecond = map.get(OPS_PER_SECOND).doubleValue();
            hasOperationsPerSecond = true;
        }
        if (map.containsKey(MEMORY))
        {
            memoryBytes = map.get(MEMORY).longValue();
            hasMemoryBytes = true;
        }
        calculateUnsetFields();
    }

    @Override
    public double getPlotableAttribute()
    {
        if (hasOperationsPerSecond())
        {
            return getOperationsPerSecond();
        }
        else if (hasAverageLatencyNanos())
        {
            return getAverageLatencyNanos();
        }
        else if (hasMemoryBytes())
        {
            return getMemoryBytes();
        }
        else if (hasNumberOfOperations())
        {
            return getNumberOfOperations();
        }
        else if (hasDurationNanos())
        {
            return getDurationNanos();
        }
        else
        {
            throw new IllegalStateException("No plottable attribute has been set");
        }
    }
    
    @Override
    public boolean hasPlottableAttribute()
    {
        return hasOperationsPerSecond() || hasAverageLatencyNanos() || hasMemoryBytes() || hasNumberOfOperations() || hasDurationNanos();
    }

    public BuildInfo getBuildInfo()
    {
        return buildInfo;
    }

    public String getTestGroupName()
    {
        return testGroupName;
    }

    public String getTestName()
    {
        return testName;
    }

    @Override
    public long getAverageLatencyNanos()
    {
        if (!hasAverageLatencyNanos)
        {
            throw new IllegalStateException("Average latency is not set");
        }
        return averageLatencyNanos;
    }

    public void setAverageLatencyNanos(long latency)
    {
        averageLatencyNanos = latency;
        hasAverageLatencyNanos = true;
        calculateUnsetFields();
    }
    
    @Override
    public boolean hasAverageLatencyNanos()
    {
        return hasAverageLatencyNanos;
    }

    public void setMaxLatencyNanos(long latency)
    {
        maxLatencyNanos = latency;
        hasMaxLatencyNanos = true;
        calculateUnsetFields();
    }

    public boolean hasMaxLatencyNanos()
    {
        return hasMaxLatencyNanos;
    }
    
    public long getMaxLatencyNanos()
    {
        if (!hasMaxLatencyNanos)
        {
            throw new IllegalStateException("Max Latency is not set");
        }
        return maxLatencyNanos;
    }

    public void setMinLatencyNanos(long latency)
    {
        minLatencyNanos = latency;
        hasMinLatencyNanos = true;
        calculateUnsetFields();
    }

    public boolean hasMinLatencyNanos()
    {
        return hasMinLatencyNanos;
    }
    
    public long getMinLatencyNanos()
    {
        if (!hasMinLatencyNanos)
        {
            throw new IllegalStateException("Min Latency is not set");
        }
        return minLatencyNanos;
    }

    public void setStandardDeviationLatencyNanos(double latency)
    {
        standardDeviationLatencyNanos = latency;
        hasStandardDeviationLatencyNanos = true;
        calculateUnsetFields();
    }
    
    public boolean hasStandardDeviationLatencyNanos()
    {
        return hasMaxLatencyNanos;
    }
    
    public double getStandardDeviationLatencyNanos()
    {
        if (!hasStandardDeviationLatencyNanos)
        {
            throw new IllegalStateException("Standard Deviation Latency is not set");
        }
        return standardDeviationLatencyNanos;
    }
    
    @Override
    public long getDurationNanos()
    {
        if (!hasDurationNanos)
        {
            throw new IllegalStateException("Duration is not set");
        }
        return durationNanos;
    }
    
    public void setDurationNanos(long duration)
    {
        this.durationNanos = duration;
        hasDurationNanos = true;
        calculateUnsetFields();
    }
    
    @Override
    public boolean hasDurationNanos()
    {
        return hasDurationNanos;
    }

    @Override
    public long getMemoryBytes()
    {
        if (!hasMemoryBytes)
        {
            throw new IllegalStateException("Memory is not set");
        }
        return memoryBytes;
    }

    public void setMemoryBytes(long memory)
    {
        this.memoryBytes = memory;
        hasMemoryBytes = true;
        calculateUnsetFields();
    }
    
    @Override
    public boolean hasMemoryBytes()
    {
        return hasMemoryBytes;
    }

    @Override
    public long getNumberOfOperations()
    {
        if (!hasNumberOfOperations)
        {
            throw new IllegalStateException("Number of operations is not set");
        }
        return numberOfOperations;
    }

    public void addNumberOfOperations(long operations)
    {
        if (!hasNumberOfOperations)
        {
            this.numberOfOperations = operations;
        }
        else
        {
            this.numberOfOperations = this.numberOfOperations + operations;
        }
        hasNumberOfOperations = true;
        calculateUnsetFields();
    }
    
    public void setNumberOfOperations(long operations)
    {
        this.numberOfOperations = operations;
        hasNumberOfOperations = true;
        calculateUnsetFields();
    }

    @Override
    public boolean hasNumberOfOperations()
    {
        return hasNumberOfOperations;
    }

    @Override
    public double getOperationsPerSecond()
    {
        if (!hasOperationsPerSecond)
        {
            throw new IllegalStateException("Operations per second is not set");
        }
        return operationsPerSecond;
    }

    public void setOperationsPerSecond(double opsPerSecond)
    {
        this.operationsPerSecond = opsPerSecond;
        hasOperationsPerSecond = true;
        calculateUnsetFields();
    }
    
    @Override
    public boolean hasOperationsPerSecond()
    {
        return hasOperationsPerSecond;
    }

    @Override
    public Map<String, Double> asMap()
    {
        calculateUnsetFields();
        Map<String, Double> map = new HashMap<String, Double>();
        if (hasDurationNanos())
        {
            map.put(DURATION_NANOS, Double.valueOf(getDurationNanos()));
        }
        if (hasAverageLatencyNanos())
        {
            map.put(LATENCY, Double.valueOf(getAverageLatencyNanos()));
        }
        if (hasMinLatencyNanos())
        {
            map.put(MIN_LATENCY, Double.valueOf(getMinLatencyNanos()));
        }
        if (hasMaxLatencyNanos())
        {
            map.put(MAX_LATENCY, Double.valueOf(getMaxLatencyNanos()));
        }
        if (hasStandardDeviationLatencyNanos())
        {
            map.put(STD_DEV_LATENCY, Double.valueOf(getStandardDeviationLatencyNanos()));
        }
        if (hasNumberOfOperations())
        {
            map.put(OPERATIONS, Double.valueOf(getNumberOfOperations()));
        }
        if (hasMemoryBytes())
        {
            map.put(MEMORY, Double.valueOf(getMemoryBytes()));
        }
        if (hasOperationsPerSecond())
        {
            map.put(OPS_PER_SECOND, Double.valueOf(getOperationsPerSecond()));
        }
        return map;
    }

    @Override
    public String toString()
    {
        return testName + "." + testGroupName + ": " + asMap();
    }

    private void calculateUnsetFields()
    {
        if (hasDurationNanos() && hasNumberOfOperations())
        {
            operationsPerSecond = calculateOpsPerSec(getNumberOfOperations(), getDurationNanos());
            hasOperationsPerSecond = true;
        }
    }

    static double calculateOpsPerSec(long operations, long durationNanos)
    {
        return operations * 1000 * 1000 * 1000.0 / durationNanos;
    }

}

package com.lewisd.jmicrobench;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PerformanceTestResultsImpl implements PerformanceTestResults
{

    private Long latency;
    private Long durationNanos;
    private Long memory;
    private Long operations;
    private Double opsPerSecond;
    private final BuildInfo buildInfo;
    private final String testGroupName;
    private final String testName;

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
        }
        if (map.containsKey(LATENCY))
        {
            latency = map.get(LATENCY).longValue();
        }
        if (map.containsKey(OPERATIONS))
        {
            operations = map.get(OPERATIONS).longValue();
        }
        if (map.containsKey(OPS_PER_SECOND))
        {
            opsPerSecond = map.get(OPS_PER_SECOND);
        }
        if (map.containsKey(MEMORY))
        {
            memory = map.get(MEMORY).longValue();
        }
        calculateUnsetFields();
    }

    @Override
    public Double getPlotableAttribute()
    {
        if (opsPerSecond != null)
        {
            return opsPerSecond;
        }
        else if (latency != null)
        {
            return latency.doubleValue();
        }
        else if (memory != null)
        {
            return memory.doubleValue();
        }
        else if (operations != null)
        {
            return operations.doubleValue();
        }
        else if (durationNanos != null)
        {
            return durationNanos.doubleValue();
        }
        else
        {
            return null;
        }
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
    public Long getAverageLatencyNanos()
    {
        return latency;
    }

    @Override
    public Long getAverageLatencyMillis()
    {
        return TimeUnit.NANOSECONDS.toMillis(latency);
    }

    @Override
    public void setAverageLatencyNs(Long latency)
    {
        this.latency = latency;
        calculateUnsetFields();
    }

    @Override
    public Long getDurationNanos()
    {
        return durationNanos;
    }

    @Override
    public Long getDurationMillis()
    {
        if (durationNanos == null)
        {
            return null;
        }
        return TimeUnit.NANOSECONDS.toMillis(durationNanos);
    }

    @Override
    public Long getDurationSeconds()
    {
        if (durationNanos == null)
        {
            return null;
        }
        return TimeUnit.NANOSECONDS.toSeconds(durationNanos);
    }

    @Override
    public void setDurationNanos(Long duration)
    {
        this.durationNanos = duration;
        calculateUnsetFields();
    }

    @Override
    public void addNumberOfOperations(Integer operations)
    {
        addNumberOfOperations(operations.longValue());
    }

    @Override
    public void addNumberOfOperations(Long operations)
    {
        if (operations == null)
        {
            return;
        }
        if (this.operations == null)
        {
            this.operations = operations;
        }
        else
        {
            this.operations += operations;
        }
        calculateUnsetFields();
    }

    @Override
    public Long getMemoryBytes()
    {
        return memory;
    }

    @Override
    public void setMemoryBytes(Long memory)
    {
        this.memory = memory;
        calculateUnsetFields();
    }

    @Override
    public Long getNumberOfOperations()
    {
        return operations;
    }

    @Override
    public void setNumberOfOperations(Long operations)
    {
        this.operations = operations;
        calculateUnsetFields();
    }

    @Override
    public Double getOperationsPerSecond()
    {
        return opsPerSecond;
    }

    @Override
    public void setOperationsPerSecond(Double opsPerSecond)
    {
        this.opsPerSecond = opsPerSecond;
        calculateUnsetFields();
    }

    @Override
    public Map<String, Double> asMap()
    {
        calculateUnsetFields();
        Map<String, Double> map = new HashMap<String, Double>();
        if (durationNanos != null)
        {
            map.put(DURATION_NANOS, (double) durationNanos);
        }
        if (latency != null)
        {
            map.put(LATENCY, (double) latency);
        }
        if (operations != null)
        {
            map.put(OPERATIONS, (double) operations);
        }
        if (memory != null)
        {
            map.put(MEMORY, (double) memory);
        }
        if (opsPerSecond != null)
        {
            map.put(OPS_PER_SECOND, opsPerSecond);
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
        if (durationNanos != null && operations != null)
        {
            opsPerSecond = calculateOpsPerSec(operations, durationNanos);
        }
    }

    static double calculateOpsPerSec(long operations, long durationNanos)
    {
        return operations * 1000 * 1000 * 1000.0 / durationNanos;
    }

}

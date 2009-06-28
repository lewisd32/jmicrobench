package com.lewisd.jmicrobench;

import java.util.Map;

public interface PerformanceTestResults
{

    static final String OPS_PER_SECOND = "opsPerSecond";
    static final String MEMORY = "memory";
    static final String OPERATIONS = "operations";
    static final String LATENCY = "latency";
    static final String DURATION_NANOS = "duration_nanos";

    String getTestGroupName();

    String getTestName();

    BuildInfo getBuildInfo();

    Long getDurationNanos();

    Long getDurationMillis();

    Long getDurationSeconds();

    Long getNumberOfOperations();

    Double getOperationsPerSecond();

    Long getAverageLatencyNanos();

    Long getAverageLatencyMillis();

    Long getMemoryBytes();

    Double getPlotableAttribute();

    void setDurationNanos(Long duration);

    void setNumberOfOperations(Long operations);

    void addNumberOfOperations(Integer operations);

    void addNumberOfOperations(Long operations);

    void setOperationsPerSecond(Double opsPerSecond);

    void setAverageLatencyNs(Long latency);

    void setMemoryBytes(Long memory);

    Map<String, Double> asMap();

}

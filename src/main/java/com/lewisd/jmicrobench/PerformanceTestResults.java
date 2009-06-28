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

    boolean hasDurationNanos();
    long getDurationNanos();

    boolean hasNumberOfOperations();
    long getNumberOfOperations();

    boolean hasOperationsPerSecond();
    double getOperationsPerSecond();

    boolean hasAverageLatencyNanos();
    long getAverageLatencyNanos();

    boolean hasMemoryBytes();
    long getMemoryBytes();

    boolean hasPlottableAttribute();
    double getPlotableAttribute();
    
    // Unit getPlottableAttributeUnit();

    Map<String, Double> asMap();

}

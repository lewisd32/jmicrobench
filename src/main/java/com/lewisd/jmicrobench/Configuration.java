package com.lewisd.jmicrobench;

import org.junit.runners.model.FrameworkMethod;

public class Configuration
{
    private static final int DEFAULT_WARMUP_PASSES = Integer.parseInt(PropertiesHelper.getProperty("build.performance.warmup.passes", "0"));
    private static final int DEFAULT_MAX_PASSES = Integer.parseInt(PropertiesHelper.getProperty("build.performance.max.passes", "100"));
    private static final int DEFAULT_STABLE_PASSES = Integer.parseInt(PropertiesHelper.getProperty("build.performance.stable.passes", "3"));
    private static final int DEFAULT_RUNS_TO_AVERAGE = Integer.parseInt(PropertiesHelper.getProperty("build.performance.averaged.runs", "3"));
    private static final long DEFAULT_EXPECTED_DURATION = Long.parseLong(PropertiesHelper.getProperty("build.performance.duration", "3000"));
    private static final long DEFAULT_WARMUP_DURATION = Long.parseLong(PropertiesHelper.getProperty("build.performance.warmup.duration", "0"));
    private static final double DEFAULT_STABILITY_PERCENTAGE= Double.parseDouble(PropertiesHelper.getProperty("build.performance.stability.percentage", "5"));
    private static final String DEFAULT_PROJECT_NAME = PropertiesHelper.getProperty("build.performance.project.name", "");

    private int warmupPasses;
    private long warmupDuration;
    private int maxPasses;
    private int stablePasses;
    private double stabilityPercentage;
    private int runsToAverage;
    private long expectedDuration;
    private boolean runBeforeAndAftersEachPass;
    private String testName;
    private String groupName;
    private String projectName;
    
    public Configuration(final FrameworkMethod method, final Class testClass)
    {
        this.maxPasses = DEFAULT_MAX_PASSES;
        this.stablePasses = DEFAULT_STABLE_PASSES;
        this.stabilityPercentage = DEFAULT_STABILITY_PERCENTAGE;
        this.runsToAverage = DEFAULT_RUNS_TO_AVERAGE;
        this.expectedDuration = DEFAULT_EXPECTED_DURATION;
        this.warmupPasses = DEFAULT_WARMUP_PASSES;
        this.warmupDuration = DEFAULT_WARMUP_DURATION;
        this.groupName = method.getName();
        this.testName = testClass.getSimpleName();
        this.projectName = DEFAULT_PROJECT_NAME;
        this.runBeforeAndAftersEachPass = true;
        
        PerformanceTest configuratinAnnotation;

        configuratinAnnotation = findClassAnnotation(testClass);
        configureFromAnnotation(configuratinAnnotation);

        configuratinAnnotation = method.getAnnotation(PerformanceTest.class);
        configureFromAnnotation(configuratinAnnotation);

        if (projectName.equals(""))
        {
            throw new IllegalStateException("'projectName' must be configured in jmicrobench.properties on PerformanceTest annotation");
        }
    }
    
    public int getWarmupPasses()
    {
        return warmupPasses;
    }

    public long getWarmupDuration()
    {
        return warmupDuration;
    }

    public int getMaxPasses()
    {
        return maxPasses;
    }

    public int getStablePasses()
    {
        return stablePasses;
    }

    public double getStabilityPercentage()
    {
        return stabilityPercentage;
    }

    public int getRunsToAverage()
    {
        return runsToAverage;
    }

    public long getExpectedDuration()
    {
        return expectedDuration;
    }
    
    public boolean getRunBeforeAndAftersEachPass()
    {
        return runBeforeAndAftersEachPass;
    }

    public String getTestName()
    {
        return testName;
    }

    public String getGroupName()
    {
        return groupName;
    }

    public String getProjectName()
    {
        return projectName;
    }

    @SuppressWarnings("unchecked")
    private PerformanceTest findClassAnnotation(final Class testClass)
    {
        return (PerformanceTest) testClass.getAnnotation(PerformanceTest.class);
    }

    private void configureFromAnnotation(PerformanceTest configuration)
    {
        if (configuration != null)
        {
            if (configuration.warmupPasses() >= 0)
            {
                warmupPasses = configuration.warmupPasses();
            }
            if (configuration.warmupDurationMillis() >= 0)
            {
                warmupDuration = configuration.warmupDurationMillis();
            }
            if (configuration.maxPasses() >= 0)
            {
                maxPasses = configuration.maxPasses();
            }
            if (configuration.stablePasses() >= 0)
            {
                stablePasses = configuration.stablePasses();
            }
            if (configuration.stabilityPercentage() >= 0)
            {
                stabilityPercentage = configuration.stabilityPercentage();
            }
            if (configuration.runsToAverage() >= 0)
            {
                runsToAverage = configuration.runsToAverage();
            }
            if (configuration.durationMillis() >= 0)
            {
                expectedDuration = configuration.durationMillis();
            }
            if (!configuration.testName().isEmpty())
            {
                testName = configuration.testName();
            }
            if (!configuration.groupName().isEmpty())
            {
                groupName = configuration.groupName();
            }
            if (!configuration.projectName().isEmpty())
            {
                projectName = configuration.projectName();
            }
            if (!configuration.runBeforesAndAftersEachPass().isEmpty())
            {
                runBeforeAndAftersEachPass = parseBoolean(configuration.runBeforesAndAftersEachPass());
            }
        }
    }

    private boolean parseBoolean(final String value)
    {
        String lower = value.toLowerCase();
        if ("yes".equals(lower) || "true".equals(lower))
        {
            return true;
        }
        else if ("no".equals(lower) || "false".equals(lower))
        {
            return false;
        }
        else
        {
            throw new IllegalArgumentException("Invalid value specified for boolean: " + value);
        }
    }

}

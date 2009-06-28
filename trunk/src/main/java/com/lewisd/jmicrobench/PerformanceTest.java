package com.lewisd.jmicrobench;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target( { ElementType.METHOD, ElementType.TYPE })
public @interface PerformanceTest
{

    long durationMillis() default -1;

    int warmupPasses() default -1;

    int warmupDurationMillis() default -1;

    int maxPasses() default -1;

    int stablePasses() default -1;

    int runsToAverage() default -1;

    String testName() default "";

    String groupName() default "";
    
    String projectName() default "";

}

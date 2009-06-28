package com.lewisd.jmicrobench;

public class PerfUtil
{

    public static long getMemoryUsed()
    {
        runGc();
        Runtime rt = Runtime.getRuntime();
        long usedMemory = rt.totalMemory() - rt.freeMemory();
        return usedMemory;
    }

    public static long getMemoryUsed(long previouslyUsed)
    {
        return getMemoryUsed() - previouslyUsed;
    }

    public static void runGc()
    {
        runGc(5);
    }

    public static void runGc(int iterations)
    {
        runGc(iterations, 500);
    }

    public static void runGc(int iterations, long delay)
    {
        for (int x = iterations; x > 0; --x)
        {
            System.gc();
            try
            {
                Thread.sleep(delay);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

}

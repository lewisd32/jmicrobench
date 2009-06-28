package com.lewisd.jmicrobench;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

public class ResultsList
{
    private static final Logger LOG = Logger.getLogger(ResultsList.class);
    private static final int RESULTS_REMOVED_BY_PRUNING = 2;

    private List<PerformanceTestResults> resultsList= new LinkedList<PerformanceTestResults>();
    private final int runsToAverage;
    private final int stablePasses;
    private final double stabilityPercentage;
    
    public ResultsList(int runsToAverage, int stablePasses, double stabilityPercentage)
    {
        this.runsToAverage = runsToAverage;
        this.stablePasses = stablePasses;
        this.stabilityPercentage = stabilityPercentage;
    }

    public PerformanceTestResults getAverageResults()
    {
        Set<String> attributes = findAllAttributes(resultsList);
        Map<String, Double> attributeAverages = new HashMap<String, Double>();
        for (String attributeName : attributes)
        {
            List<Double> resultList = getResultsForAttribute(attributeName, resultsList);
            List<Double> prunedResults;
            if (runsToAverage > 0)
            {
                prunedResults = getPrunedResults(resultList, runsToAverage);
            }
            else
            {
                prunedResults = Collections.singletonList(resultList.get(resultList.size() - 1));
            }
            double total = 0;
            for (Double result : prunedResults)
            {
                total += result;
            }
            double average = total / prunedResults.size();
            // TODO: we don't want to average all attributes (like duration)
            attributeAverages.put(attributeName, average);
        }
        PerformanceTestResults values = resultsList.get(0);
        return new PerformanceTestResultsImpl(values.getBuildInfo(), values.getTestGroupName(), values.getTestName(), attributeAverages);
    }

    public boolean isStable()
    {
        if (stablePasses == 0)
        {
            return true;
        }
        else if (resultsList.size() < (stablePasses + RESULTS_REMOVED_BY_PRUNING))
        {
            return false;
        }
        else
        {
            Set<String> attributes = findAllAttributes(resultsList);

            Map<String, List<Double>> attributeResultLists = new HashMap<String, List<Double>>();

            for (String attributeName : attributes)
            {
                List<Double> resultList = getResultsForAttribute(attributeName, resultsList);
                attributeResultLists.put(attributeName, resultList);
            }

            for (String attributeName : attributes)
            {
                List<Double> resultList = attributeResultLists.get(attributeName);
                if (!isAttributeStable(attributeName, resultList, stabilityPercentage))
                {
                    return false;
                }
            }
            return true;
        }
    }

    private List<Double> getResultsForAttribute(String attributeName, List<PerformanceTestResults> resultsList)
    {
        List<Double> resultList = new LinkedList<Double>();
        for (PerformanceTestResults testResult : resultsList)
        {
            Double result = testResult.asMap().get(attributeName);
            resultList.add(result);
        }
        return resultList;
    }

    private boolean isAttributeStable(String attributeName, List<Double> resultList, double stabilityPercentage)
    {
        LinkedList<Double> prunedResults = getPrunedResults(resultList, stablePasses);

        double first = prunedResults.getFirst();
        double last = prunedResults.getLast();

        double diff = Math.abs(last - first);
        double percent = diff * 100 / first;

        if (percent <= stabilityPercentage)
        {
            return true;
        }
        else
        {
            LOG.info("Result for " + attributeName + " was too different: " + diff + " (" + String.format("%.2f", percent) + "%)");
            return false;
        }
    }

    private LinkedList<Double> getPrunedResults(List<Double> resultList, int resultCount)
    {
        LinkedList<Double> prunedResults = new LinkedList<Double>();
        prunedResults.addAll(resultList.subList(resultList.size() - resultCount - RESULTS_REMOVED_BY_PRUNING, resultList.size()));
        Collections.sort(prunedResults);

        prunedResults.removeFirst();
        prunedResults.removeLast();
        return prunedResults;
    }

    private Set<String> findAllAttributes(List<PerformanceTestResults> resultsList)
    {
        Set<String> attributeNames = new HashSet<String>();
        for (PerformanceTestResults results : resultsList)
        {
            attributeNames.addAll(results.asMap().keySet());
        }
        return attributeNames;
    }
    
    public long getTotalDurationNanos()
    {
        long totalDurationNanos = 0;
        for (PerformanceTestResults results : resultsList)
        {
            totalDurationNanos += results.getDurationNanos();
        }
        return totalDurationNanos;
    }
    
    public boolean hasEnoughResults()
    {
        return resultsList.size() >= runsToAverage + RESULTS_REMOVED_BY_PRUNING || runsToAverage == 0;
    }

    public void add(InProgressPerformanceTestResults results)
    {
        resultsList.add(results);
    }

    public void clear()
    {
        resultsList.clear();
    }

}

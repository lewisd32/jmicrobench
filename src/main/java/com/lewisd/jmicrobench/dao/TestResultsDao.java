package com.lewisd.jmicrobench.dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import com.lewisd.jmicrobench.BuildInfo;
import com.lewisd.jmicrobench.PerformanceTestResults;

public interface TestResultsDao
{

    void storeResults(List<? extends PerformanceTestResults> results) throws SQLException;

    List<BuildInfo> getRecentBuilds(int count) throws SQLException;

    Map<BuildInfo, List<PerformanceTestResults>> getBuildResults(List<BuildInfo> builds) throws SQLException;

}

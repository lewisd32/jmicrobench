package com.lewisd.jmicrobench;

import java.sql.SQLException;
import java.util.Properties;

import com.lewisd.jmicrobench.dao.TestResultsDao;
import com.lewisd.jmicrobench.dao.TestResultsDaoImpl;
import com.mysql.jdbc.Driver;

public class DBHelper
{

    public static TestResultsDao getTestResultsDao(String projectName) throws SQLException
    {
        String url = getDbUrl();
        String username = getDbUsername();

        Properties properties = new Properties();
        properties.setProperty("user", username);
        String password = getDbPassword();
        if (password != null && !password.isEmpty())
        {
            properties.setProperty("password", password);
        }
        return new TestResultsDaoImpl(projectName, new Driver(), url, properties);
    }

    private static String getDbUsername()
    {
        return PropertiesHelper.getProperty("build.performance.db.username");
    }

    private static String getDbPassword()
    {
        return PropertiesHelper.getProperty("build.performance.db.password");
    }

    private static String getDbUrl()
    {
        return PropertiesHelper.getProperty("build.performance.db.url");
    }

}

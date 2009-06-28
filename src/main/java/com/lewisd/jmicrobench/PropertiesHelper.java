package com.lewisd.jmicrobench;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesHelper
{

    private static final String PROPERTIES_FILENAME = "jmicrobench.properties";
    private static Properties properties;

    private PropertiesHelper()
    {
        // do nothing
    }

    public static String getProperty(String propertyName)
    {
        return getProperty(propertyName, null);
    }

    public static String getProperty(String propertyName, String defaultValue)
    {
        String value = System.getProperty(propertyName);
        if (value == null)
        {
            Properties properties = loadPropertiesFile();
            value = properties.getProperty(propertyName);
        }
        if (value == null)
        {
            value = defaultValue;
        }
        return value;
    }

    private static Properties loadPropertiesFile()
    {
        if (properties == null)
        {
            properties = new Properties();
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILENAME);
            try
            {
                properties.load(in);
            }
            catch (IOException e)
            {
                throw new RuntimeException("Could not load properties from " + PROPERTIES_FILENAME, e);
            }
        }
        return properties;
    }

}

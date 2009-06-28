package com.lewisd.jmicrobench;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class BuildInfoImpl implements BuildInfo
{
    private static final Logger LOG = Logger.getLogger(BuildInfoImpl.class);

    private static BuildInfoImpl currentBuild;

    private final int revision;
    private final Date timestamp;

    public BuildInfoImpl(int revision, Date timestamp)
    {
        this.revision = revision;
        this.timestamp = timestamp;
    }

    public int getRevision()
    {
        return revision;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    @Override
    public String toString()
    {
        return "Build " + revision + " at " + timestamp;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + revision;
        result = prime * result + ((timestamp == null) ? 0 : timestamp.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BuildInfoImpl other = (BuildInfoImpl) obj;
        if (revision != other.revision)
            return false;
        if (timestamp == null)
        {
            if (other.timestamp != null)
                return false;
        }
        else if (!timestamp.equals(other.timestamp))
            return false;
        return true;
    }

    private static Date getBuildTimestamp()
    {
        String timestampString = System.getProperty("build.timestamp");
        if (timestampString == null || timestampString.isEmpty())
        {
            LOG.warn("No build.timestamp system property");
            return new Date();
        }
        String formatString = PropertiesHelper.getProperty("build.timestamp.format");
        if (formatString == null)
        {
            throw new RuntimeException("No build.timestamp.format system property");
        }
        SimpleDateFormat formatter = new SimpleDateFormat(formatString);
        try
        {
            return formatter.parse(timestampString);
        }
        catch (ParseException e)
        {
            throw new RuntimeException("Error parsing build timestamp", e);
        }
    }

    private static int getRevisionProperty()
    {
        String revision = System.getProperty("build.revision");
        if (revision != null && !revision.isEmpty())
        {
            return Integer.parseInt(revision);
        }
        return -1;
    }

    static BuildInfo getCurrentBuild()
    {
        if (currentBuild == null)
        {
            currentBuild = new BuildInfoImpl(getRevisionProperty(), getBuildTimestamp());
        }
        return currentBuild;
    }

}

package com.lewisd.jmicrobench;

import java.util.Date;

public class BuildInfoImpl implements BuildInfo
{

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

}

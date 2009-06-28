package com.lewisd.jmicrobench.graph;

import java.awt.Color;

public class Point
{

    private final Color pointColour;
    private final PointType pointType;
    private final Color lineColour;
    private final double x;
    private final double y;

    public Point(Color pointColour, Color lineColour, PointType pointType, double x, double y)
    {
        this.pointColour = pointColour;
        this.lineColour = lineColour;
        this.pointType = pointType;
        this.x = x;
        this.y = y;
    }

    public Color getPointColour()
    {
        return pointColour;
    }

    public PointType getPointType()
    {
        return pointType;
    }

    public Color getLineColour()
    {
        return lineColour;
    }

    public double getX()
    {
        return x;
    }

    public double getY()
    {
        return y;
    }

}

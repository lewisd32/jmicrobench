package com.lewisd.jmicrobench.graph;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class PlotImpl implements Plot
{

    private final String ledgendName;
    private Color ledgendColour;
    private Color lineColour;
    private Color pointColour;
    private PointType pointType = PointType.X;
    private List<Point> points = new LinkedList<Point>();
    private boolean drawLines;

    public PlotImpl(String ledgendName, Color defaultColour)
    {
        this.ledgendName = ledgendName;
        setColour(defaultColour);
    }

    @Override
    public void setColour(Color colour)
    {
        setLedgendColour(colour);
        setPointColour(colour);
        setLineColour(colour);
    }

    @Override
    public void setLedgendColour(Color colour)
    {
        this.ledgendColour = colour;
    }

    @Override
    public void setLineColour(Color colour)
    {
        this.lineColour = colour;
    }

    @Override
    public void setPointColour(Color colour)
    {
        this.pointColour = colour;
    }

    @Override
    public void setDrawLines(boolean enabled)
    {
        this.drawLines = enabled;
    }

    public String getLedgendName()
    {
        return ledgendName;
    }

    public Color getLedgendColour()
    {
        return ledgendColour;
    }

    public List<Point> getPoints()
    {
        return points;
    }

    public boolean getDrawLines()
    {
        return drawLines;
    }

    @Override
    public void setDefaultPointType(PointType pointType)
    {
        this.pointType = pointType;
    }

    @Override
    public void addPoint(double x, double y)
    {
        addPoint(pointType, x, y);
    }

    @Override
    public void addPoint(PointType pointType, double x, double y)
    {
        Point point = new Point(pointColour, lineColour, pointType, x, y);
        points.add(point);
    }

    public PointType getPointType()
    {
        return pointType;
    }

}

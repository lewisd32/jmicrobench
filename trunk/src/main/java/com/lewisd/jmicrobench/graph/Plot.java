package com.lewisd.jmicrobench.graph;

import java.awt.Color;

public interface Plot
{

    void setColour(Color colour);

    void setLineColour(Color colour);

    void setPointColour(Color colour);

    void setLedgendColour(Color colour);

    void setDefaultPointType(PointType pointType);

    void addPoint(double x, double y);

    void addPoint(PointType pointType, double x, double y);

    void setDrawLines(boolean enabled);

}

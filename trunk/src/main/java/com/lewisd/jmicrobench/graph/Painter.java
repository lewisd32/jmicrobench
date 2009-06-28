package com.lewisd.jmicrobench.graph;

import java.awt.Color;

public interface Painter
{

    static final int TOP = 0;
    static final int MIDDLE = 1;
    static final int BOTTOM = 2;

    static final int LEFT = 0;
    static final int CENTER = 1;
    static final int RIGHT = 2;

    void setupCanvas(Color backgroundColour, int width, int height);

    void drawLine(float stroke, int x1, int y1, int x2, int y2);

    void setColour(Color colour);

    void drawPoint(PointType pointType, int x, int y);

    void drawText(int horizAlign, int vertAlign, int x, int y, String text);

    int getTextWidth(String text);

    int getTextHeight(String text);

}

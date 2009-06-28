package com.lewisd.jmicrobench.graph;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;

public class Graph
{

    private static final double[] SCALING_FACTORS = new double[] { 1, 2, 5 };

    private final Painter painter;

    private double minX = Double.NaN;
    private double maxX = Double.NaN;
    private double minY = Double.NaN;
    private double maxY = Double.NaN;

    private int width = 100;
    private int height = 100;

    private int leftMargin = 0;
    private int rightMargin = 0;
    private int topMargin = 0;
    private int bottomMargin = 0;

    private int gridHeight;
    private double yRange;
    private double yPixelsPerInteger;

    private int gridWidth;
    private double xRange;
    private double xPixelsPerInteger;

    private Color backgroundColour = Color.WHITE;
    private Color axisColour = Color.BLACK;
    private Color gridColour = new Color(0.8f, 0.8f, 0.8f);
    private Color axisLabelColour = Color.BLACK;

    private Color[] defaultPlotColours = new Color[] { Color.RED, Color.BLUE, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.CYAN };
    private PointType[] defaultPlotPointTypes = new PointType[] { PointType.X, PointType.PLUS, PointType.ASTERISK, PointType.CIRCLE };
    private int lastDefaultColourIndex = -1;
    private int lastDefaultPointTypeIndex = -1;
    private String xLabelFormat = "%.0f";
    private String yLabelFormat = "%.1f";

    private List<PlotImpl> plots = new LinkedList<PlotImpl>();

    private boolean autoScale = true;
    private boolean autoMargins = true;

    private int xLabelVerticalPadding = 1;
    private int yLabelVerticalPadding = 1;
    private int xLabelHorizontalPadding = 1;
    private int yLabelHorizontalPadding = 1;

    private int minXGridLinesPixelInterval = 15;
    private int minYGridLinesPixelInterval = 15;

    public Graph(Painter painter)
    {
        this.painter = painter;
    }

    public Plot newPlot(String ledgendName)
    {
        PlotImpl plot = new PlotImpl(ledgendName, nextDefaultPlotColour());
        plot.setDefaultPointType(nextDefaultPlotPointType());
        plots.add(plot);
        return plot;
    }

    private Color nextDefaultPlotColour()
    {
        lastDefaultColourIndex += 1;
        return defaultPlotColours[lastDefaultColourIndex % defaultPlotColours.length];
    }

    private PointType nextDefaultPlotPointType()
    {
        lastDefaultPointTypeIndex += 1;
        return defaultPlotPointTypes[(lastDefaultPointTypeIndex / defaultPlotColours.length) % defaultPlotPointTypes.length];
    }

    public void setDimensions(int width, int height)
    {
        this.width = width;
        this.height = height;
        recalculateVariables();
    }

    public void setXScale(int min, int max)
    {
        autoScale = false;
        this.minX = min;
        this.maxX = max;
        recalculateVariables();
    }

    public void setYScale(int min, int max)
    {
        autoScale = false;
        this.minY = min;
        this.maxY = max;
        recalculateVariables();
    }

    public void setBackgroundColour(Color backgroundColor)
    {
        this.backgroundColour = backgroundColor;
    }

    public void setAxisColour(Color axisColour)
    {
        this.axisColour = axisColour;
    }

    public void setAxisLabelColour(Color axisLabelColour)
    {
        this.axisLabelColour = axisLabelColour;
    }

    public void setGridColour(Color gridColour)
    {
        this.gridColour = gridColour;
    }

    public void setXLabelFormat(String labelFormat)
    {
        xLabelFormat = labelFormat;
    }

    public void setYLabelFormat(String labelFormat)
    {
        yLabelFormat = labelFormat;
    }

    public void setXLabelHorizontalPadding(int labelHorizontalPadding)
    {
        xLabelHorizontalPadding = labelHorizontalPadding;
    }

    public void setYLabelHorizontalPadding(int labelHorizontalPadding)
    {
        yLabelHorizontalPadding = labelHorizontalPadding;
    }

    public void setXLabelVerticalPadding(int labelVerticalPadding)
    {
        xLabelVerticalPadding = labelVerticalPadding;
    }

    public void setYLabelVerticalPadding(int labelVerticalPadding)
    {
        yLabelVerticalPadding = labelVerticalPadding;
    }

    public void setMinXGridLinesPixelInterval(int minXGridLinesPixelInterval)
    {
        this.minXGridLinesPixelInterval = minXGridLinesPixelInterval;
    }

    public void setMinYGridLinesPixelInterval(int minYGridLinesPixelInterval)
    {
        this.minYGridLinesPixelInterval = minYGridLinesPixelInterval;
    }

    public void draw()
    {
        painter.setupCanvas(backgroundColour, width, height);

        if (autoScale)
        {
            autoScale();
        }
        if (autoMargins)
        {
            autoMargins();
        }

        drawGridLinesAndNumbers();
        drawAxis();
        drawLedgend();

        for (PlotImpl plot : plots)
        {
            if (plot.getDrawLines())
            {
                Point lastPoint = null;
                for (Point point : plot.getPoints())
                {
                    if (lastPoint != null)
                    {
                        painter.setColour(point.getLineColour());
                        painter.drawLine(2, translateXToPixel(lastPoint.getX()) + 1, translateYToPixel(lastPoint.getY()) - 1, translateXToPixel(point.getX()) + 1, translateYToPixel(point.getY()) - 1);
                    }
                    lastPoint = point;
                }
            }
            for (Point point : plot.getPoints())
            {
                painter.setColour(point.getPointColour());
                painter.drawPoint(point.getPointType(), translateXToPixel(point.getX()) + 1, translateYToPixel(point.getY()) - 1);
            }
        }

    }

    private void autoScale()
    {
        minX = Double.MAX_VALUE;
        minY = Double.MAX_VALUE;
        maxX = Double.MIN_VALUE;
        maxY = Double.MIN_VALUE;
        for (PlotImpl plot : plots)
        {
            for (Point point : plot.getPoints())
            {
                double x = point.getX();
                double y = point.getY();
                if (x < minX)
                {
                    minX = x;
                }
                if (x > maxX)
                {
                    maxX = x;
                }

                if (y < minY)
                {
                    minY = y;
                }
                if (y > maxY)
                {
                    maxY = y;
                }
            }
        }
        double xRange = maxX - minX;
        double yRange = maxY - minY;
        maxX = maxX + xRange * 0.05;
        minX = minX - xRange * 0.05;
        maxY = maxY + yRange * 0.05;
        minY = minY - yRange * 0.05;

        recalculateVariables();
    }

    private void autoMargins()
    {
        leftMargin = painter.getTextWidth(String.format(yLabelFormat, minY));
        leftMargin = Math.max(leftMargin, painter.getTextWidth(String.format(yLabelFormat, maxY)));
        leftMargin += yLabelHorizontalPadding * 2;

        bottomMargin = painter.getTextHeight(String.format(xLabelFormat, minX));
        bottomMargin = Math.max(bottomMargin, painter.getTextHeight(String.format(xLabelFormat, maxX)));
        bottomMargin += xLabelVerticalPadding * 2;
        bottomMargin += getLedgendHeight();

        recalculateVariables();
    }

    private int getLedgendHeight()
    {
        int columns = getLedgendColumns();
        int rows = getLedgendRows(columns);
        int highest = getLedgendRowHeight();
        return highest * rows;
    }

    private int getLedgendRowHeight()
    {
        int highest = 0;
        for (PlotImpl plot : plots)
        {
            String name = plot.getLedgendName();
            int height = painter.getTextHeight(name);
            if (height > highest)
            {
                highest = height;
            }
        }
        return highest;
    }

    private int getLedgendColumnWidth()
    {
        int widest = 0;
        for (PlotImpl plot : plots)
        {
            String name = plot.getLedgendName();
            int width = painter.getTextWidth(name);
            if (width > widest)
            {
                widest = width;
            }
        }
        return widest + 2 * yLabelHorizontalPadding + 20;
    }

    private int getLedgendRows(int columns)
    {
        return (int) Math.ceil(plots.size() / (float) columns);
    }

    private int getLedgendColumns()
    {
        int widest = getLedgendColumnWidth();
        int columns = width / widest;
        return Math.min(columns, plots.size());
    }

    private void drawLedgend()
    {
        int columns = getLedgendColumns();
        int rows = getLedgendRows(columns);
        int rowHeight = getLedgendRowHeight();
        int columnWidth = getLedgendColumnWidth();

        int topY = height - getLedgendHeight();
        int leftX = width / 2 - (columns * columnWidth) / 2;

        int i = 0;
        for (PlotImpl plot : plots)
        {
            String name = plot.getLedgendName();
            painter.setColour(plot.getLedgendColour());

            int column = i / rows;
            int row = i % rows;

            int x = leftX + column * columnWidth;
            int y = topY + row * rowHeight;

            painter.drawText(Painter.LEFT, Painter.TOP, x + 10, y, name);
            painter.drawPoint(plot.getPointType(), x, y + (rowHeight / 2));

            ++i;
        }
    }

    private void drawGridLinesAndNumbers()
    {
        drawHorizontalLinesAndNumbers();
        drawVerticalLinesAndNumbers();
    }

    private void drawVerticalLinesAndNumbers()
    {
        double xIncrement = calculateIncrement(xPixelsPerInteger, minXGridLinesPixelInterval);
        double x = 0;
        while (x < minX)
        {
            x += xIncrement;
        }

        int lastNumberXPixel = 0;

        while (x < maxX)
        {
            int xPixel = translateXToPixel(x);
            painter.setColour(gridColour);
            painter.drawLine(1, xPixel, topMargin, xPixel, height - 1 - bottomMargin - 1);
            String number = String.format(xLabelFormat, x);
            int width = painter.getTextWidth(number);
            if (Math.abs(xPixel - lastNumberXPixel) > width + xLabelHorizontalPadding)
            {
                painter.setColour(axisLabelColour);
                painter.drawText(Painter.CENTER, Painter.TOP, xPixel, height - 1 - bottomMargin + xLabelVerticalPadding, number);
                lastNumberXPixel = xPixel;
            }
            x += xIncrement;
        }
    }

    private void drawHorizontalLinesAndNumbers()
    {
        double yIncrement = calculateIncrement(yPixelsPerInteger, minYGridLinesPixelInterval);
        double y = 0;
        while (y < minY)
        {
            y += yIncrement;
        }

        int lastNumberYPixel = 0;

        while (y < maxY)
        {
            int yPixel = translateYToPixel(y);
            painter.setColour(gridColour);
            painter.drawLine(1, leftMargin + 1, yPixel, width - 1 - rightMargin, yPixel);
            String number = String.format(yLabelFormat, y);
            int height = painter.getTextHeight(number);
            if (Math.abs(yPixel - lastNumberYPixel) > height + yLabelVerticalPadding)
            {
                painter.setColour(axisLabelColour);
                painter.drawText(Painter.RIGHT, Painter.MIDDLE, leftMargin - yLabelHorizontalPadding, yPixel, number);
                lastNumberYPixel = yPixel;
            }
            y += yIncrement;
        }
    }

    private void drawAxis()
    {
        painter.setColour(axisColour);
        painter.drawLine(1, leftMargin, topMargin, leftMargin, height - 1 - bottomMargin);
        painter.drawLine(1, leftMargin, height - 1 - bottomMargin, width - 1 - rightMargin, height - 1 - bottomMargin);
    }

    private double calculateIncrement(double pixelsPerInteger, int minIncrementPixels)
    {
        int scalingFactorIndex = 0;
        int scalingFactorMultiple = 0;

        double scalingFactor = SCALING_FACTORS[scalingFactorIndex] * Math.pow(10, scalingFactorMultiple);
        double pixelIncrement = scalingFactor * pixelsPerInteger;

        while (pixelIncrement < minIncrementPixels)
        {
            // too close together, must scale up
            scalingFactorIndex++;
            if (scalingFactorIndex > 2)
            {
                scalingFactorIndex = 0;
                scalingFactorMultiple++;
            }
            scalingFactor = SCALING_FACTORS[scalingFactorIndex] * Math.pow(10, scalingFactorMultiple);
            pixelIncrement = scalingFactor * pixelsPerInteger;
        }
        return scalingFactor;
    }

    private void recalculateVariables()
    {
        gridHeight = height - topMargin - bottomMargin;
        yRange = maxY - minY;
        yPixelsPerInteger = gridHeight / yRange;

        gridWidth = width - leftMargin - rightMargin;
        xRange = maxX - minX;
        xPixelsPerInteger = gridWidth / xRange;
    }

    private int translateYToPixel(double y)
    {
        if (y > maxY)
        {
            return Integer.MAX_VALUE;
        }
        else if (y < minY)
        {
            return Integer.MIN_VALUE;
        }
        double boundedY = y - minY;

        return height - 1 - bottomMargin - (int) (boundedY * yPixelsPerInteger);
    }

    private int translateXToPixel(double x)
    {
        if (x > maxX)
        {
            return Integer.MAX_VALUE;
        }
        else if (x < minX)
        {
            return Integer.MIN_VALUE;
        }
        double boundedX = x - minX;

        return (int) (boundedX * xPixelsPerInteger) + leftMargin;
    }
}

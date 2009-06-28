package com.lewisd.jmicrobench.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class BufferedImagePainter implements Painter
{

    private Color currentColour;
    private Graphics2D g;
    private BufferedImage image;

    public BufferedImage getImage()
    {
        return image;
    }

    @Override
    public void drawLine(float stroke, int x1, int y1, int x2, int y2)
    {
        g.setColor(currentColour);
        g.setStroke(new BasicStroke(stroke));
        g.drawLine(x1, y1, x2, y2);
    }

    @Override
    public void drawPoint(PointType pointType, int x, int y)
    {
        g.setColor(currentColour);
        switch (pointType)
        {
        case SIMPLE:
            g.drawLine(x, y, x, y);
            break;
        case PLUS:
            g.drawLine(x - 2, y, x + 2, y);
            g.drawLine(x, y - 2, x, y + 2);
            break;
        case X:
            g.drawLine(x - 2, y - 2, x + 2, y + 2);
            g.drawLine(x - 2, y + 2, x + 2, y - 2);
            break;
        default:
            throw new RuntimeException("Unknown pointType: " + pointType);

        }
    }

    @Override
    public void drawText(int horizAlign, int vertAlign, int x, int y, String text)
    {
        g.setColor(currentColour);
        int height = getTextHeight(text);
        int width = getTextWidth(text);
        FontMetrics metrics = g.getFontMetrics();
        int descent = metrics.getMaxDescent();
        y = y - descent;
        switch (horizAlign)
        {
        case LEFT:
            break;
        case CENTER:
            x = x - width / 2;
            break;
        case RIGHT:
            x = x - width;
            break;
        default:
            throw new IllegalArgumentException("Invalid horizAlign " + horizAlign);
        }
        switch (vertAlign)
        {
        case TOP:
            y = y + height;
            break;
        case MIDDLE:
            y = y + height / 2;
            break;
        case BOTTOM:
            break;
        default:
            throw new IllegalArgumentException("Invalid vertAlign " + vertAlign);
        }
        g.drawString(text, x, y);
    }

    @Override
    public void setColour(Color colour)
    {
        this.currentColour = colour;
    }

    @Override
    public void setupCanvas(Color backgroundColour, int width, int height)
    {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        g = (Graphics2D) image.getGraphics();

        g.setColor(backgroundColour);
        g.fillRect(0, 0, width, height);
    }

    @Override
    public int getTextHeight(String text)
    {
        FontMetrics metrics = g.getFontMetrics();
        return metrics.getHeight();
    }

    @Override
    public int getTextWidth(String text)
    {
        FontMetrics metrics = g.getFontMetrics();
        return metrics.stringWidth(text);
    }

}

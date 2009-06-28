package com.lewisd.jmicrobench.graph;

import java.awt.Color;
import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class GraphSwingDemo extends JFrame
{

    public GraphSwingDemo()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // JPanel panel = new JPanel();
        // getContentPane().add(panel);
        // FlowLayout layout = new FlowLayout();
        // panel.setLayout( layout );
        // panel.add(component);
    }

    public static void main(String[] args)
    {
        BufferedImagePainter painter = new BufferedImagePainter();
        Graph graph = new Graph(painter);

        GraphSwingDemo demo = new GraphSwingDemo();
        demo.setSize(415, 345);
        demo.setVisible(true);

        graph.setDimensions(400, 300);

        graph.setMinXGridLinesPixelInterval(15);
        graph.setMinYGridLinesPixelInterval(20);

        graph.setXLabelHorizontalPadding(2);

        graph.setXLabelFormat("%.0f");
        graph.setYLabelFormat("%.1f");
        graph.setGridColour(new Color(0.9f, 0.9f, 0.9f));

        // graph.setXScale(10, 179); // each x integer will be 1 pixels
        // graph.setYScale(90, 329); // each y integer will be 0.5 pixels

        Plot plot = graph.newPlot("Demo");
        plot.setDefaultPointType(PointType.X);
        plot.setDefaultPointType(PointType.X);
        plot.setDrawLines(true);

        plot.addPoint(10, 90);
        plot.addPoint(11, 91);
        plot.addPoint(11, 91);

        plot.addPoint(15, 100);
        plot.addPoint(16, 101);
        plot.addPoint(18, 102);
        plot.addPoint(19, 101);
        plot.addPoint(20, 101);
        plot.addPoint(21, 50);
        plot.addPoint(22, 52);
        plot.addPoint(23, 90);
        plot.addPoint(28, 105);
        plot.addPoint(29, 120);
        plot.addPoint(30, 121);
        plot.addPoint(31, 119);

        graph.draw();
        Image image = painter.getImage();
        Icon icon = new ImageIcon(image);
        JLabel label = new JLabel(icon);
        demo.add(label);
        demo.repaint();
    }

}

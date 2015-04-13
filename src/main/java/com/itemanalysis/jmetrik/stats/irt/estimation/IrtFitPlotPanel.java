package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.swing.GraphPanel;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class IrtFitPlotPanel extends GraphPanel {

    private LinkedHashMap<String, JFreeChart> charts = null;
    private ArrayList<String> itemNames = null;
    private int numberOfVariables = 0;
    private String xLabel = "";
    private String yLabel = "";
    private double minTestScore = Double.NEGATIVE_INFINITY;
    private double maxTestScore = Double.POSITIVE_INFINITY;
    private boolean showTcc = false;
    private boolean showPersonInfo = false;
    private boolean showPersonSe = false;
    private boolean showExpectedValue = false;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public IrtFitPlotPanel(ArrayList<String> itemNames){
        this.itemNames = itemNames;
        charts = new LinkedHashMap<String, JFreeChart>();
        initialize();
    }

    /**
     * Includes additional series for observed points. The dataset is twice
     * the size as the number of lines because there is a line and set of points for each category.
     *
     * @param name name of chart
     * @param dataset data set where the first half of the entries define the lines and the second half
     *                of the series define the points. The series for lines and points are assumed to be
     *                in the same order.
     * @param showLegend show legend.
     */
    public void updateDataset(String name, XYSeriesCollection dataset, boolean showLegend){
        JFreeChart chart = charts.get(name);
        if(!showLegend) chart.removeLegend();
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setDataset(dataset);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        setXYPlotRendererWithPoints(plot);
    }

    public void updateOrdinate(String name, double min, double max){
        JFreeChart chart = charts.get(name);
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.getRangeAxis().setRange(min, max);
    }

    public void setOrdinateAutoRange(String name, boolean autoRange){
        JFreeChart chart = charts.get(name);
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.getRangeAxis().setAutoRange(autoRange);
    }

    public void setOrdinateLabel(String name, String label){
        JFreeChart chart = charts.get(name);
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.getRangeAxis().setLabel(label);
    }

    public void savePlots(String path)throws IOException {
        File dir = new File(path);
        if(!dir.exists()) dir.mkdirs();
        for(String s : charts.keySet()){
            JFreeChart c = charts.get(s);
            ChartUtilities.saveChartAsJPEG(new File(dir.getAbsolutePath() + "/" + s + ".jpg"), c, width, height);
        }
    }

    private void initialize(){
        numberOfVariables = itemNames.size();
        minTestScore = -4.5;
        maxTestScore = 4.5;
        xLabel = "Theta";
        yLabel = "Probability";
    }

    public void setGraph(){
        this.setLayout(new GridLayout(numberOfVariables + 1, 1));
        for(String s : itemNames){
            createChart(s, xLabel, yLabel, minTestScore, maxTestScore);
        }
    }

    private void createChart(String name, String xLabel, String yLabel, double minScore, double maxScore){
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
                name,              // chart title
                xLabel,             // x axis label
                yLabel,             // y axis label
                dataset,            // data
                PlotOrientation.VERTICAL,   // chart orientation
                true,         // include legend
                true,               // tooltips
                false               // urls
        );

        // get a reference to the plot for further customisation...
        XYPlot plot = (XYPlot)chart.getPlot();
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        renderer.setBaseShapesFilled(false);
        renderer.setDrawOutlines(true);

        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        plot.getDomainAxis().setRange(minScore, maxScore);

        ChartPanel panel = new ChartPanel(chart);
        panel.getPopupMenu().addSeparator();
        panel.setPreferredSize(new Dimension(width, height));

        chart.setPadding(new RectangleInsets(20.0,5.0,20.0,5.0));
        charts.put(name, chart);

        JPanel subPanel = new JPanel();//additional panel needed to prevent gridlayout from stretching graph
        subPanel.add(panel);
        subPanel.setBackground(Color.WHITE);
        this.add(subPanel);

    }

}

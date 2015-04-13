/*
 * Copyright (c) 2012 Patrick Meyer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.itemanalysis.jmetrik.graph.irt;

import com.itemanalysis.jmetrik.swing.GraphPanel;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class IrtPlotPanel extends GraphPanel {

    private IrtPlotCommand command = null;
    private LinkedHashMap<String, JFreeChart> charts = null;
    private ArrayList<String> names = null;
    private int numberOfVariables = 0;
    private String title = "";
    private String xLabel = "";
    private String yLabel = "";
    private double minTestScore = Double.NEGATIVE_INFINITY;
    private double maxTestScore = Double.POSITIVE_INFINITY;
    private boolean showTcc = false;
    private boolean showPersonInfo = false;
    private boolean showPersonSe = false;
    private boolean showExpectedValue = false;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public IrtPlotPanel(IrtPlotCommand command){
        this.command = command;
        charts = new LinkedHashMap<String, JFreeChart>();
        processCommand();
    }

    public void updateDataset(String name, XYSeriesCollection dataset, boolean showLegend){
        JFreeChart chart = charts.get(name);
        if(!showLegend) chart.removeLegend();
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setDataset(dataset);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
        setXYPlotRenderer(plot);
    }

    /**
     * Same as updateDataset() but includes additional series for observed points. The dataset is twice
     * the size as the number of lines because there is a line and set of points for each category.
     *
     * @param name name of chart
     * @param dataset data set where the first half of the entries define the lines and the second half
     *                of the series define the points. The series for lines and points are assumed to be
     *                in the same order.
     * @param showLegend show legend.
     */
    public void updateDatasetLinesAndPoints(String name, XYSeriesCollection dataset, boolean showLegend){
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

    private void processCommand(){
        try{
            names = command.getFreeOptionList("variables").getString();
            numberOfVariables = names.size();
            title = "";
            minTestScore = command.getPairedOptionList("xaxis").getDoubleAt("min");
            maxTestScore = command.getPairedOptionList("xaxis").getDoubleAt("max");
            showTcc = command.getSelectAllOption("person").isArgumentSelected("tcc");
            showPersonInfo = command.getSelectAllOption("person").isArgumentSelected("info");
            showPersonSe = command.getSelectAllOption("person").isArgumentSelected("se");
            showExpectedValue = command.getSelectOneOption("type").isValueSelected("expected");
            xLabel = "Theta";
            yLabel = "Probability";
            if(showExpectedValue){
                yLabel = "Expected Value";
            }
        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }


    }

    public void setGraph(){
        this.setLayout(new GridLayout(numberOfVariables + 1, 1));
        int index = 0;
        for(String s : names){
            createChart(names.get(index), s, xLabel, yLabel, minTestScore, maxTestScore);
            index++;
        }

        //add Person Plot
        if(showTcc || showPersonInfo || showPersonSe){
            createChart("jmetrik-tcc-tif-tse", "Person Plot", "Theta", "Value", minTestScore, maxTestScore);
        }
    }

    private void createChart(String name, String title, String xLabel, String yLabel, double minScore, double maxScore){
        XYSeriesCollection dataset = new XYSeriesCollection();
        JFreeChart chart = ChartFactory.createXYLineChart(
                title,              // chart title
                xLabel,             // x axis label
                yLabel,             // y axis label
                dataset,            // data
                chartOrientation,   // chart orientation
                showLegend,         // include legend
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

//        this.addLocalEPSMenuItem(this, panel.getPopupMenu(), chart);//remove this line for public release versions

        chart.setPadding(new RectangleInsets(20.0,5.0,20.0,5.0));
        charts.put(name, chart);

        JPanel subPanel = new JPanel();//additional panel needed to prevent gridlayout from stretching graph
        subPanel.add(panel);
//        this.addJpgMenuItem(subPanel, panel.getPopupMenu());
        subPanel.setBackground(Color.WHITE);
        this.add(subPanel);

    }

//    /**
//     * This method uses the proprietary library EpsGraphics. It is only here
//     * for producing files for the jMetrik book. It will be disabled in
//     * public versions of the software.
//     *
//     * @param parent
//     * @param popMenu
//     * @param chart
//     */
//    private void addLocalEPSMenuItem(final Component parent, JPopupMenu popMenu, final JFreeChart chart){
//        JMenuItem mItem = new JMenuItem("Save as EPS...");
//        mItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JFileChooser chooser = new JFileChooser();
//                FileFilter filter1 = new SimpleFilter("eps", "EPS File (*.eps)");
//                chooser.addChoosableFileFilter(filter1);
//                int status = chooser.showSaveDialog(parent);
//                if(status == JFileChooser.APPROVE_OPTION){
//                    File f = chooser.getSelectedFile();
//                    String fileName = f.getAbsolutePath().toLowerCase();
//                    if(!fileName.endsWith("eps")) fileName += ".eps";
//
//                    try{
//                        EpsGraphics2D g = new EpsGraphics2D();
//                        g.scale(1.0, 1.0);//72dpi
//                        g.setColorDepth(EpsGraphics2D.GRAYSCALE);
//                        chart.draw(g,new Rectangle(500, 445));
//                        Writer out=new FileWriter(new File(fileName));
//                        out.write(g.toString());
//                        out.close();
//                    }catch(IOException ex){
//                        ex.printStackTrace();
//                    }
//                }
//
//            }
//        });
//        popMenu.addArgument(mItem);
//    }

}

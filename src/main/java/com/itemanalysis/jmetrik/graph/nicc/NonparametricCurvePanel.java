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

package com.itemanalysis.jmetrik.graph.nicc;

import com.itemanalysis.jmetrik.swing.GraphPanel;
import com.itemanalysis.jmetrik.swing.SimpleFilter;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class NonparametricCurvePanel extends GraphPanel {

    private NonparametricCurveCommand command = null;
    private TreeMap<String, JFreeChart> charts = null;
    private ArrayList<String> names = null;
    String title = "";
    String xlabel = "";
    String ylabel = "";
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public NonparametricCurvePanel(NonparametricCurveCommand command){
        this.command = command;
        charts = new TreeMap<String, JFreeChart>();
        processCommand();
        setGraphs();
    }

    public void updateDatasetFor(String name, double min, double max, XYDataset dataset){
        JFreeChart c = charts.get(name);
        XYPlot plot = (XYPlot)c.getPlot();
        plot.setDataset(dataset);
        setXYPlotRenderer(plot);

        ValueAxis axis = plot.getRangeAxis();
        axis.setLowerBound(min);
        axis.setUpperBound(max);
    }

    public void savePlots(String path)throws IOException{
        File dir = new File(path);
        if(!dir.exists()) dir.mkdirs();
        for(String s : charts.keySet()){
            JFreeChart c = charts.get(s);
            ChartUtilities.saveChartAsJPEG(new File(dir.getAbsolutePath()+"/"+s+".jpg"), c, width, height);
        }
    }

    private void processCommand(){
        try{
            names = command.getFreeOptionList("variables").getString();
            xlabel = command.getFreeOption("xvar").getString();
            ylabel = "Probability";
            if(command.getSelectOneOption("curves").isValueSelected("expected")) ylabel = "Expected Value";
            this.setLayout(new GridLayout(names.size()+1, 1));
        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }

    }

    private void setGraphs(){
        //add ICCs
        for(String s : names){
            createChart(s, s, xlabel, ylabel, 0, 1);
        }

        //add TCC
        createChart("tcc", "Test Characteristic Curve", xlabel, "True Score", 0.0, (double)names.size());
    }

    private void createChart(String name, String title, String xLabel, String yLabel, double minScore, double maxScore){
        XYSeriesCollection dataset = new XYSeriesCollection();
        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,             // chart title
                xLabel,            // x axis label
                yLabel,            // y axis label
                dataset,           // data
                chartOrientation,  // chart orientation
                showLegend,        // include legend
                true,              // tooltips
                false              // urls
        );

       if(showLegend){
           LegendTitle chartTitle = chart.getLegend();
           chartTitle.setPosition(legendPosition);
       }

        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

//        //can use this code to fix the number of tick marks on the y-axis
//        NumberFormat myFormatter = new DecimalFormat("#.0");
//        NumberAxis yaxis = (NumberAxis)plot.getRangeAxis();
//        yaxis.setTickUnit(new NumberTickUnit(.1, myFormatter));

        ChartPanel panel = new ChartPanel(chart);
        panel.getPopupMenu().addSeparator();
        panel.setPreferredSize(new Dimension(width, height));

//        this.addLocalEPSMenuItem(this, panel.getPopupMenu(), chart);//remove this line for public release versions

        chart.setPadding(new RectangleInsets(20.0,5.0,20.0,5.0));
        charts.put(name, chart);

        JPanel subPanel = new JPanel();//additional panel needed to prevent gridlayout from stretching graph
        subPanel.add(panel);
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

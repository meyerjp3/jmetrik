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

package com.itemanalysis.jmetrik.swing;

import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.io.*;

public class GraphPanel extends JPanel{

    protected JFreeChart chart = null;
    protected int width = 450;
    protected int height = 400;
    protected float lineWidth = 1.0f;
    protected float[][] lineStyle = null;
    protected boolean showLegend = true;
    protected boolean showMarkers = false;
    protected RectangleEdge legendPosition = RectangleEdge.BOTTOM;
    protected PlotOrientation chartOrientation = PlotOrientation.VERTICAL;


    /**
     * jMetrik line styles--old
     */
//    private float[][] lineStyle = new float[][]{
//        { 1.0f, 0.0f}, //solid
//        {10.0f, 5.0f}, //dashed
//        {10.0f, 5.0f, 2.0f, 5.0f}, //dash-dot
//        {10.0f, 5.0f, 2.0f, 5.0f, 2.0f, 5.0f}, //dash-two-dot
//        {10.0f, 5.0f, 2.0f, 5.0f, 2.0f, 5.0f, 2.0f, 5.0f}, //dash-three-dot
//        {20.0f, 5.0f}, //long-dash
//        {20.0f, 5.0f, 5.0f, 5.0f}, //long-dash-dot
//        {20.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f}, //long-dash-two-dot
//        {20.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f}, //long-dash-three-dot
//    };

    /**
     * Default jMetrik colors. First nine colors are overwritten by preferences.
     */
    private Color[] defaultColors = {
            Color.DARK_GRAY,        //"darkGray";  java.awt.Color[r=64,g=64,b=64]
            ChartColor.RED,         //"red";       java.awt.Color[r=255,g=0,b=0]
            ChartColor.DARK_BLUE,   //"#0000c0";   java.awt.Color[r=0,g=0,b=192]
            ChartColor.DARK_GREEN,  //"#00c000";   java.awt.Color[r=0,g=192,b=0]
            ChartColor.DARK_MAGENTA,//"#c000c0";   java.awt.Color[r=192,g=0,b=192]
            ChartColor.DARK_CYAN,   //"#00c0c0";   java.awt.Color[r=0,g=192,b=192]
            ChartColor.LIGHT_RED,   //"#ff4040";   java.awt.Color[r=255,g=64,b=64]
            ChartColor.LIGHT_BLUE,  //"#4040ff";   java.awt.Color[r=64,g=64,b=255]
            ChartColor.LIGHT_GREEN, //"#40ff40";   java.awt.Color[r=64,g=255,b=64]
            ChartColor.LIGHT_MAGENTA,
            ChartColor.LIGHT_CYAN,
            ChartColor.VERY_DARK_RED,
            ChartColor.VERY_DARK_BLUE,
            ChartColor.VERY_DARK_GREEN,
            ChartColor.VERY_DARK_YELLOW,
            ChartColor.VERY_DARK_MAGENTA,
            ChartColor.VERY_DARK_CYAN,
            ChartColor.VERY_LIGHT_RED,
            ChartColor.VERY_LIGHT_BLUE,
            ChartColor.VERY_LIGHT_GREEN,
            ChartColor.VERY_LIGHT_MAGENTA,
            ChartColor.VERY_LIGHT_CYAN
    };

    public GraphPanel(){
        initializePreferences();
    }


    /**
     * This method should be called after a chart dataset is updated. It
     * will iterate over all XYDatasets and provide the line color and lineStyle.
     * If it is called before a chart has a dataset, it will not have an effect.
     *
     * @param plot
     */
    public void setXYPlotRenderer(XYPlot plot){
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
        int n = plot.getSeriesCount();

        for(int i=0;i<n;i++){
            Stroke stroke = new BasicStroke(lineWidth,
                            BasicStroke.CAP_SQUARE,
                            BasicStroke.JOIN_MITER,
                            10.0f,
                            getLineStyle(i),
                            0.0f);
            renderer.setSeriesStroke(i, stroke);
            renderer.setSeriesPaint(i, getPaintColor(i));
        }
        renderer.setLegendLine(new Line2D.Double(0, 5, 40, 5));
        renderer.setBaseShapesFilled(false);
        renderer.setBaseShapesVisible(showMarkers);
        renderer.setDrawSeriesLineAsPath(true);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRenderer(renderer);
    }

    public void setXYPlotRendererWithPoints(XYPlot plot){
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer)plot.getRenderer();
        int n = plot.getSeriesCount();
        int half = (int)(n/2.0);

        //assume first half are series for lines and second half are series for points

        for(int i=0;i<n;i++){
            if(i<half){
                //Add lines
                Stroke stroke = new BasicStroke(lineWidth,
                            BasicStroke.CAP_SQUARE,
                            BasicStroke.JOIN_MITER,
                            10.0f,
                            getLineStyle(i),
                            0.0f);
                renderer.setSeriesStroke(i, stroke);
                renderer.setSeriesPaint(i, getPaintColor(i));
                renderer.setSeriesShapesFilled(i, false);
                renderer.setSeriesShapesVisible(i, showMarkers);
                renderer.setLegendLine(new Line2D.Double(0, 5, 40, 5));
                renderer.setDrawSeriesLineAsPath(true);

            }else{
                //Add points
                renderer.setSeriesLinesVisible(i, false);
                renderer.setSeriesShapesFilled(i, false);
                renderer.setSeriesShapesVisible(i, true);
                renderer.setSeriesPaint(i, getPaintColor(i-half));

                //This code will add points that are slightly transparent.
                //The plan is to change the size according to the number of values at each point.
//                renderer.setSeriesShape(i, new Ellipse2D.Double(-50/2,-50/2,50,50));
//                renderer.setSeriesShapesFilled(i, true);
//                renderer.setSeriesPaint(i, new Color(0,0,0,50));
            }
        }


        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRenderer(renderer);
    }

    public void setLineChart(String title, String subtitle, String xlabel, String ylabel){
        XYSeriesCollection dataset = new XYSeriesCollection();

        chart = ChartFactory.createXYLineChart(
                title,            // chart title
                xlabel,            // x axis label
                ylabel,            // y axis label
                dataset,                    // data
                chartOrientation,
                showLegend,              // include legend
                true,                            // tooltips
                false                            // urls
        );

        if(showLegend){
           LegendTitle chartTitle = chart.getLegend();
           chartTitle.setPosition(legendPosition);
        }

        if(subtitle!=null && !"".equals(subtitle)){
            TextTitle subtitle1 = new TextTitle(subtitle);
            chart.addSubtitle(subtitle1);
        }

        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

        //can use this code to modify charts for book. DO NOT USE FOR NORMAL FUNCTIONING
//        NumberAxis yaxis = (NumberAxis)plot.getRangeAxis();
//        yaxis.setTickUnit(new NumberTickUnit(.1));

        ChartPanel panel = new ChartPanel(chart);
        panel.getPopupMenu().addSeparator();
        this.addJpgMenuItem(this, panel.getPopupMenu());
//        this.addEPSMenuItem(this, panel.getPopupMenu());//remove this line for public release versions
        panel.setPreferredSize(new Dimension(width, height));

        chart.setPadding(new RectangleInsets(20.0,5.0,20.0,5.0));
        this.setBackground(Color.WHITE);
        this.add(panel);

    }

    /**
     * Reads program preferences to get line styles and colors
     */
    private void initializePreferences(){
        JmetrikPreferencesManager prefs = new JmetrikPreferencesManager();
        lineStyle = prefs.getLineStyles();
        Color[] color = prefs.getColors();
        for(int i=0;i<color.length;i++){
            defaultColors[i] = color[i];
        }

        showLegend = prefs.getShowLegend();
        showMarkers = prefs.getShowMarkers();
        legendPosition = prefs.getLegendPosition();
        chartOrientation = prefs.getChartOrientation();
        width = prefs.getChartWidth();
        height = prefs.getChartHeight();
        lineWidth = prefs.getChartLineWidth();
    }

    /**
     * Uses a wrapping array index to get the line lineStyle from the lineStyle array.
     * This is a helper method for getLineStyle.
     *
     * @param index
     * @return
     */
    private float[] getLineStyle(int index){
        int n = lineStyle.length;
        int newIndex = index;
        while(index<0){
            newIndex += n;
        }
        newIndex = newIndex % n;
        return lineStyle[newIndex];
    }

    /**
     * Uses a wrapping array index to get a color from the color array.
     *
     * @param index
     * @return
     */
    private Color getPaintColor(int index){
        int n = defaultColors.length;
        int newIndex = index;
        while(index<0){
            newIndex += n;
        }
        newIndex = newIndex % n;
        return defaultColors[newIndex];
    }

    public void saveAsJPEG(File f)throws IOException {
        ChartUtilities.saveChartAsJPEG(f, chart, width, height);
    }

    public void addJpgMenuItem(final Component parent, JPopupMenu popMenu){
        JMenuItem mItem = new JMenuItem("Save as JPG...");
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileFilter filter1 = new SimpleFilter("jpg", "JPG File (*.jpg)");
                chooser.addChoosableFileFilter(filter1);
                int status = chooser.showSaveDialog(parent);
                if(status == JFileChooser.APPROVE_OPTION){
                    File f = chooser.getSelectedFile();
                    try{
                        String fileName = f.getAbsolutePath().toLowerCase();
                        if(!fileName.endsWith("jpg")) fileName += ".jpg";
                        saveAsJPEG(new File(fileName));
                    }catch(IOException ex){
                        JOptionPane.showMessageDialog(
                                parent,
                                "IOException: Could not save file",
                                "IOException",
                                JOptionPane.ERROR_MESSAGE);
                    }

                }

            }
        });
        popMenu.add(mItem);
    }

//    public void addPDFMenuItem(final Component parent, JPopupMenu popMenu){
//        JMenuItem mItem = new JMenuItem("Save as PDF...");
//        mItem.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JFileChooser chooser = new JFileChooser();
//                FileFilter filter1 = new SimpleFilter("pdf", "PDF File (*.pdf)");
//                chooser.addChoosableFileFilter(filter1);
//                int status = chooser.showSaveDialog(parent);
//                if(status == JFileChooser.APPROVE_OPTION){
//                    File f = chooser.getSelectedFile();
//                    try{
//                        String fileName = f.getAbsolutePath().toLowerCase();
//                        if(!fileName.endsWith("pdf")) fileName += ".pdf";
//                        saveAsPDF(parent, new File(fileName));
//                    }catch(IOException ex){
//                        JOptionPane.showMessageDialog(
//                                parent,
//                                "IOException: Could not save file",
//                                "IOException",
//                                JOptionPane.ERROR_MESSAGE);
//                    }
//
//                }
//
//            }
//        });
//        popMenu.addArgument(mItem);
//    }

//    /**
//     * This method uses the proprietary library EpsGraphics. It is only here
//     * for producing files for the jMetrik book. It will be disabled in
//     * public versions of teh software.
//     *
//     * @param parent
//     * @param popMenu
//     */
//    protected void addEPSMenuItem(final Component parent, JPopupMenu popMenu){
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
//        popMenu.add(mItem);
//    }

}

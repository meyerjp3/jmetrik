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

package com.itemanalysis.jmetrik.graph.scatterplot;

import java.awt.*;

import com.itemanalysis.jmetrik.swing.GraphPanel;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class ScatterplotPanel extends GraphPanel {

    private ScatterplotCommand command = null;
    private JFreeChart chart = null;
    private String title = "";
    private String subtitle = "";
    private String xlabel = "";
    private String ylabel = "";
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public ScatterplotPanel(ScatterplotCommand command){
        this.command = command;
        processCommand();
    }

    public void updateDataset(XYSeriesCollection dataset){
        if(dataset.getSeriesCount()==1 || !showLegend){
            chart.removeLegend();
        }
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDataset(dataset);
        setXYPlotRenderer(plot);
    }

    public void updateDataset(DefaultXYDataset dataset){
        if(dataset.getSeriesCount()==1 || !showLegend){
            chart.removeLegend();
        }
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDataset(dataset);
        setXYPlotRenderer(plot);
    }

    private void processCommand(){
        try{
            title = command.getFreeOption("title").getString();
            subtitle = command.getFreeOption("subtitle").getString();
            xlabel = command.getFreeOption("xvar").getString();
            ylabel= command.getFreeOption("yvar").getString();
            showLegend = command.getSelectAllOption("options").isArgumentSelected("legend");
            showMarkers = true;//must be true otherwise chart will show not points
            width = command.getPairedOptionList("dimensions").getIntegerAt("width");
            height = command.getPairedOptionList("dimensions").getIntegerAt("height");
        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

    public void setGraph(){
        DefaultXYDataset dataset = new DefaultXYDataset();
        PlotOrientation orientation=PlotOrientation.VERTICAL;

        try{
            chart = ChartFactory.createScatterPlot(
                    title,      		// chart title
                    xlabel,     		// x axis label
                    ylabel,     		// y axis label
                    dataset,                  	// data
                    orientation,
                    showLegend,              // include legend
                    true,                     		// tooltips
                    false                     		// urls
            );

            if(subtitle!=null && !"".equals(subtitle)){
                TextTitle subtitle1 = new TextTitle(subtitle);
                chart.addSubtitle(subtitle1);
            }

            XYPlot plot = (XYPlot) chart.getPlot();
            plot.setNoDataMessage("NO DATA");
            plot.setDomainZeroBaselineVisible(false);
            plot.setRangeZeroBaselineVisible(false);
            plot.setBackgroundPaint(Color.WHITE);
            plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
            plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

            ChartPanel panel = new ChartPanel(chart);
            panel.getPopupMenu().addSeparator();
            this.addJpgMenuItem(this, panel.getPopupMenu());
            panel.setPreferredSize(new Dimension(width, height));

            chart.setPadding(new RectangleInsets(20.0,5.0,20.0,5.0));
            this.setBackground(Color.WHITE);
            this.add(panel);

        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
    }



}

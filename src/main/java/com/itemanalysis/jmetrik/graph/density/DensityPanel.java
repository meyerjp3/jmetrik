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

package com.itemanalysis.jmetrik.graph.density;

import com.itemanalysis.jmetrik.swing.GraphPanel;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

import java.awt.*;

public class DensityPanel extends GraphPanel {

    private DensityCommand command = null;
    private String title = "";
    private String subtitle = "";
    private String xlabel = "";
    private String ylabel = "";
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public DensityPanel(DensityCommand syntax){
        this.command = syntax;
        processCommand();
    }

    public void updateDataset(XYSeriesCollection dataset){
        if(showLegend && dataset.getSeriesCount()==1){
            chart.removeLegend();
        }
        XYPlot plot = (XYPlot)chart.getPlot();
        plot.setDataset(dataset);
        setXYPlotRenderer(plot);
    }

    private void processCommand(){
        try{
            title = command.getFreeOption("title").getString();
            xlabel = command.getFreeOption("variable").getString();
            ylabel = "Density";
            setLineChart(title, subtitle, xlabel, ylabel);

        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

}

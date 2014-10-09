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

package com.itemanalysis.jmetrik.graph.barchart;

import com.itemanalysis.jmetrik.swing.GraphPanel;
import com.itemanalysis.psychometrics.statistics.TwoWayTable;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.LayeredBarRenderer;
import org.jfree.chart.renderer.category.StackedBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.SortOrder;

import java.awt.*;
import java.util.Iterator;

public class BarChartPanel extends GraphPanel {

    private BarChartCommand command = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public BarChartPanel(BarChartCommand command){
        this.command=command;
    }

    public void updateDataset(TwoWayTable table){
        String groupingName="";
        try{
            boolean showFreq = command.getSelectOneOption("yaxis").isValueSelected("freq");
            if(command.getFreeOption("groupvar").hasValue()){
                groupingName=command.getFreeOption("groupvar").getString();
            }
            DefaultCategoryDataset dataset=new DefaultCategoryDataset();

            Iterator<Comparable<?>> rowIter = table.rowValuesIterator();
            Iterator<Comparable<?>> colIter = null;
            Comparable<?> r = null;
            Comparable<?> c = null;

            while(rowIter.hasNext()){
                r = rowIter.next();

                colIter = table.colValuesIterator();
                while(colIter.hasNext()){
                    c = colIter.next();
                    if(showFreq){
                        dataset.addValue(table.getCount(r,c), r.toString(), c.toString());
                    }else{
                        dataset.addValue(table.getCount(r,c)/table.getFreqSum(), (groupingName + " " + r.toString()), c.toString());
                    }
                }

            }
            CategoryPlot plot = (CategoryPlot)chart.getPlot();
            plot.setDataset(dataset);

        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }

    }

    public void setGraph()throws IllegalArgumentException{
        boolean hasGroupingVariable = false;
        if(command.getFreeOption("groupvar").hasValue()){
            hasGroupingVariable = true;
        }

        String name = command.getFreeOption("variable").getString();
        String xLabel = name;

        DefaultCategoryDataset dataset=new DefaultCategoryDataset();

        String yLabel="";
        if(command.getSelectOneOption("yaxis").isValueSelected("freq")){
            yLabel="Frequency";
        }else{
            yLabel="Percentage";
        }

        if(command.getSelectOneOption("layout").isValueSelected("stacked")){
            chart = ChartFactory.createStackedBarChart(
                    command.getFreeOption("title").getString(),
                    xLabel,
                    yLabel,
                    dataset,
                    chartOrientation,
                    hasGroupingVariable, //only show legend if has grouping variable
                    true,
                    false
            );
            CategoryPlot plot = (CategoryPlot) chart.getPlot();
            StackedBarRenderer renderer = (StackedBarRenderer)plot.getRenderer();
            renderer.setDrawBarOutline(false);
            renderer.setBaseItemLabelsVisible(true);
            renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator());

        }else if(command.getSelectOneOption("view").isValueSelected("3D")){
            chart = ChartFactory.createBarChart3D(
                    command.getFreeOption("title").getString(),
                    xLabel,
                    yLabel,
                    dataset,
                    chartOrientation,
                    hasGroupingVariable, //only show legend if has grouping variable
                    true,
                    false
            );
        }else{
            chart = ChartFactory.createBarChart(
                    command.getFreeOption("title").getString(),
                    xLabel,
                    yLabel,
                    dataset,
                    chartOrientation,
                    hasGroupingVariable, //only show legend if has grouping variable
                    true,
                    false
            );

        }

        String sub = "";
        if(command.getFreeOption("subtitle").getString()!=null){
            sub = command.getFreeOption("subtitle").getString();
        }
        TextTitle subtitle1 = new TextTitle(sub);
        chart.addSubtitle(subtitle1);

        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setDomainGridlinesVisible(true);

        if(command.getSelectOneOption("layout").isValueSelected("layered")){
            LayeredBarRenderer renderer = new LayeredBarRenderer();
//				renderer.setDrawBarOutline(false);
            plot.setRenderer(renderer);
            plot.setRowRenderingOrder(SortOrder.DESCENDING);
        }

        chart.setPadding(new RectangleInsets(20.0,5.0,20.0,5.0));

        ChartPanel panel = new ChartPanel(chart);
        panel.getPopupMenu().addSeparator();
        this.addJpgMenuItem(BarChartPanel.this, panel.getPopupMenu());

        panel.setPreferredSize(new Dimension(width, height));

        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
//                plot.setForegroundAlpha(0.80f);
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(true);
        renderer.setShadowVisible(false);

        this.setBackground(Color.WHITE);
        this.add(panel);

    }



}

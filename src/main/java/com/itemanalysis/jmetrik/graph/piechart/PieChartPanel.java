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

package com.itemanalysis.jmetrik.graph.piechart;

import com.itemanalysis.jmetrik.swing.GraphPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.util.Iterator;

import com.itemanalysis.psychometrics.statistics.TwoWayTable;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.MultiplePiePlot;
import org.jfree.chart.plot.PiePlot;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.ui.RectangleInsets;
import org.jfree.util.Rotation;
import org.jfree.util.TableOrder;

public class PieChartPanel extends GraphPanel {

    private PieChartCommand command = null;
    private JFreeChart chart = null;
    private boolean hasGroupVariable = false;
    private boolean explode = false;
    private double explodePercent = 0.0;
    private String explodeValue = "";
    private String chartTitle = "";
    private String chartSubtitle = "";
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public PieChartPanel(PieChartCommand command){
        this.command = command;
        processCommand();
    }

    public void updateDefaultCategoryDataset(TwoWayTable table){
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
                dataset.addValue(table.getCount(r,c), r.toString(), c.toString());
            }
        }
        MultiplePiePlot plot = (MultiplePiePlot)chart.getPlot();
        plot.setDataset(dataset);
    }

    public void updateDefaultPieDataset(TwoWayTable table){
        DefaultPieDataset dataset=new DefaultPieDataset();
        Iterator<Comparable<?>> rowIter = table.rowValuesIterator();
        Iterator<Comparable<?>> colIter = null;
        Comparable<?> r = null;
        Comparable<?> c = null;

        while(rowIter.hasNext()){
            r = rowIter.next();
            colIter = table.colValuesIterator();
            while(colIter.hasNext()){
                c = colIter.next();
                dataset.setValue(c.toString(), table.getCount(r,c));
            }
        }
        PiePlot plot = (PiePlot)chart.getPlot();
        plot.setDataset(dataset);
    }

    private void processCommand(){
        try{
            chartTitle = command.getFreeOption("title").getString();
            chartSubtitle = command.getFreeOption("subtitle").getString();
            explode = command.getPairedOptionList("explode").hasValue();
            explodeValue = "";
            explodePercent = 0;
            if(explode){
                explodeValue = command.getPairedOptionList("explode").getStringAt("section");
                int explodeAmount = command.getPairedOptionList("explode").getIntegerAt("amount");
                explodePercent = explodeAmount/100.0;
            }

            if(command.getFreeOption("groupvar").hasValue()){
                hasGroupVariable = true;
            }else{
                hasGroupVariable = false;
            }
        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }

    }

    public void setGraph(){
        if(hasGroupVariable){
            DefaultCategoryDataset piedat = new DefaultCategoryDataset();
            chart = ChartFactory.createMultiplePieChart(
                    chartTitle,
                    piedat,
                    TableOrder.BY_ROW,
                    showLegend,
                    true,
                    false
            );

            if(chartSubtitle!=null && !"".equals(chartSubtitle)){
                TextTitle subtitle1 = new TextTitle(chartSubtitle);
                chart.addSubtitle(subtitle1);
            }

            MultiplePiePlot plot = (MultiplePiePlot) chart.getPlot();
            JFreeChart subchart = plot.getPieChart();
            PiePlot p = (PiePlot) subchart.getPlot();
            p.setBackgroundPaint(Color.WHITE);
            p.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})"));
            if(explode) p.setExplodePercent(explodeValue, explodePercent);

            ChartPanel panel = new ChartPanel(chart);
            panel.setPreferredSize(new Dimension(width,height));

            chart.setPadding(new RectangleInsets(20.0,5.0,20.0,5.0));
            this.add(panel);
        }else{
            DefaultPieDataset piedat = new DefaultPieDataset();
            if(command.getSelectOneOption("view").isValueSelected("3D")){
                chart = ChartFactory.createPieChart3D(
                        chartTitle,
                        piedat,
                        showLegend,
                        true,
                        false
                );

                PiePlot3D plot = (PiePlot3D) chart.getPlot();
                plot.setStartAngle(290);
                plot.setDirection(Rotation.CLOCKWISE);
                plot.setForegroundAlpha(0.5f);
                plot.setNoDataMessage("No data to display");
                if(explode) plot.setExplodePercent(explodeValue, explodePercent);

            }else{
                chart = ChartFactory.createPieChart(
                        command.getFreeOption("title").getString(),
                        piedat,
                        showLegend,
                        true,
                        false
                );
            }

            if(chartSubtitle!=null && !"".equals(chartSubtitle)){
                TextTitle subtitle = new TextTitle(chartSubtitle);
                chart.addSubtitle(subtitle);
            }

            PiePlot plot = (PiePlot) chart.getPlot();
            plot.setLabelGap(0.02);
            plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})"));
            plot.setBackgroundPaint(Color.WHITE);
            if(explode) plot.setExplodePercent(explodeValue, explodePercent);

            ChartPanel panel = new ChartPanel(chart);
            panel.getPopupMenu().addSeparator();
            this.addJpgMenuItem(this, panel.getPopupMenu());
            panel.setPreferredSize(new Dimension(width, height));

            chart.setPadding(new RectangleInsets(5.0,5.0,5.0,5.0));
            this.setBackground(Color.WHITE);
            this.add(panel);
        }


    }

    public boolean hasGroupVariable(){
        return hasGroupVariable;
    }

}

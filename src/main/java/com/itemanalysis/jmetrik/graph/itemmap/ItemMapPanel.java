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

package com.itemanalysis.jmetrik.graph.itemmap;

import java.awt.*;
import java.util.List;

import com.itemanalysis.jmetrik.graph.histogram.HistogramChartDataset;
import com.itemanalysis.jmetrik.swing.GraphPanel;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.TextTitle;
import org.jfree.chart.urls.StandardXYURLGenerator;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.RectangleInsets;

public class ItemMapPanel extends GraphPanel {

    private ItemMapCommand command = null;
    private JFreeChart chart = null;
    private String chartTitle = "";
    private String chartSubtitle = "";
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public ItemMapPanel(ItemMapCommand command){
        this.command = command;
    }

    private void processCommand(){

        try{
            //get titles
            chartTitle = command.getFreeOption("title").getString();
            chartSubtitle = command.getFreeOption("subtitle").getString();
        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }

    }

    public void setGraph(){
        HistogramChartDataset personDataset=null;

        //create common x-axis
        NumberAxis domainAxis = new NumberAxis();
        domainAxis.setLabel("Logits");

        PlotOrientation itemMapOrientation = PlotOrientation.HORIZONTAL;

        //create histogram
        personDataset = new HistogramChartDataset();
        ValueAxis personRangeAxis = new NumberAxis("Person Density");
        if(itemMapOrientation==PlotOrientation.HORIZONTAL) personRangeAxis.setInverted(true);
        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        renderer.setURLGenerator(new StandardXYURLGenerator());
        renderer.setDrawBarOutline(true);
        renderer.setShadowVisible(false);
        XYPlot personPlot = new XYPlot(personDataset, null, personRangeAxis, renderer);
        personPlot.setOrientation(PlotOrientation.HORIZONTAL);

        //create scatterplot of item difficulty
        NumberAxis itemRangeAxis = new NumberAxis("Item Number");
        if(itemMapOrientation==PlotOrientation.VERTICAL){
            itemRangeAxis.setInverted(true);
        }

        XYItemRenderer itemRenderer = new ItemPointRenderer(true, true);
        itemRenderer.setSeriesPaint(1, Color.DARK_GRAY);
        itemRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
        itemRenderer.setURLGenerator(new StandardXYURLGenerator());
        XYPlot itemPlot = new XYPlot(null, null, itemRangeAxis, itemRenderer);
        itemPlot.setOrientation(PlotOrientation.HORIZONTAL);



        //combine the two charts
        CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(domainAxis);
        cplot.add(personPlot, 1);
        cplot.add(itemPlot, 4);
        cplot.setGap(8.0);
        cplot.setDomainGridlinePaint(Color.white);
        cplot.setDomainGridlinesVisible(true);
        cplot.setOrientation(itemMapOrientation);

        chart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, cplot, false);
        chart.setBackgroundPaint(Color.white);
        if(chartSubtitle!=null && !"".equals(chartSubtitle)){
            chart.addSubtitle(new TextTitle(chartSubtitle));
        }

        ChartPanel panel = new ChartPanel(chart);
        panel.getPopupMenu().addSeparator();
        this.addJpgMenuItem(this, panel.getPopupMenu());
        panel.setPreferredSize(new Dimension(width, height));

        chart.setPadding(new RectangleInsets(20.0,5.0,20.0,5.0));
        this.setBackground(Color.WHITE);
        this.add(panel);


    }

    private class ItemPointRenderer extends XYLineAndShapeRenderer{

        private final Color pointColor = Color.DARK_GRAY;

        public ItemPointRenderer(boolean lines, boolean shapes) {
            super(lines, shapes);
        }

        @Override
        public Paint getItemPaint(int row, int col) {
            return pointColor;
        }

    }

//    public void setGraph(){
//        HistogramChartDataset personDataset=null;
//        HistogramChartDataset itemData = null;
//
//        PlotOrientation itemMapOrientation = PlotOrientation.HORIZONTAL;
//
//        //create common x-axis
//        NumberAxis domainAxis = new NumberAxis();
//        domainAxis.setLabel("Logits");
//
//        //create histogram
//        personDataset = new HistogramChartDataset();
//        ValueAxis rangeAxis = new NumberAxis("Person Density");
//        if(itemMapOrientation==PlotOrientation.HORIZONTAL) rangeAxis.setInverted(true);
//        XYBarRenderer renderer = new XYBarRenderer();
//        renderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
//        renderer.setURLGenerator(new StandardXYURLGenerator());
//        renderer.setDrawBarOutline(true);
//        renderer.setShadowVisible(false);
//        XYPlot personPlot = new XYPlot(personDataset, null, rangeAxis, renderer);
//        personPlot.setOrientation(PlotOrientation.HORIZONTAL);
//
//        //create scatterplot of item difficulty
//        itemData = new HistogramChartDataset();
//        NumberAxis itemRangeAxis = new NumberAxis("Item Frequency");
//        if(itemMapOrientation==PlotOrientation.VERTICAL){
//            itemRangeAxis.setInverted(true);
//        }
//
//        XYBarRenderer itemRenderer = new XYBarRenderer();
//        itemRenderer.setBaseToolTipGenerator(new StandardXYToolTipGenerator());
//        itemRenderer.setURLGenerator(new StandardXYURLGenerator());
//        itemRenderer.setDrawBarOutline(true);
//        itemRenderer.setShadowVisible(false);
//        XYPlot itemPlot = new XYPlot(itemData, null, itemRangeAxis, itemRenderer);
//        itemPlot.setOrientation(PlotOrientation.HORIZONTAL);
//
//        //combine the two charts
//        CombinedDomainXYPlot cplot = new CombinedDomainXYPlot(domainAxis);
//        cplot.add(personPlot, 3);
//        cplot.add(itemPlot, 2);
//        cplot.setGap(8.0);
//        cplot.setDomainGridlinePaint(Color.white);
//        cplot.setDomainGridlinesVisible(true);
//        cplot.setOrientation(itemMapOrientation);
//
////            //next four lines are temp setting for book
////            //these four lines will create a histogram with white bars so it appears as just the bar outline
////            renderer.setBarPainter(new StandardXYBarPainter());
////            renderer.setSeriesPaint(0, Color.white);
////            itemRenderer.setBarPainter(new StandardXYBarPainter());
////            itemRenderer.setSeriesPaint(0, Color.white);
//
//        chart = new JFreeChart(chartTitle, JFreeChart.DEFAULT_TITLE_FONT, cplot, false);
//        chart.setBackgroundPaint(Color.white);
//        if(chartSubtitle!=null && !"".equals(chartSubtitle)){
//            chart.addSubtitle(new TextTitle(chartSubtitle));
//        }
//
//        ChartPanel panel = new ChartPanel(chart);
//        panel.getPopupMenu().addSeparator();
//        this.addJpgMenuItem(this, panel.getPopupMenu());
//        panel.setPreferredSize(new Dimension(width, height));
//
////            //temp setting for book
////            this.addLocalEPSMenuItem(this, panel.getPopupMenu(), chart);//remove this line for public release versions
//
//        chart.setPadding(new RectangleInsets(20.0,5.0,20.0,5.0));
//        this.setBackground(Color.WHITE);
//        this.add(panel);
//
//    }
//
//    public void updateItemDataSet(HistogramChartDataset data){
//        //set histogram data
//        CombinedDomainXYPlot cplot = (CombinedDomainXYPlot)chart.getPlot();
//        List plots = cplot.getSubplots();
//        XYPlot p = (XYPlot) plots.get(1);
//        if(data.getSeriesCount()>1) p.setForegroundAlpha(0.70f);
//        p.setDataset(1, data);
//    }

    public void updateItemDataSet2(XYSeriesCollection data){
        //set histogram data
        CombinedDomainXYPlot cplot = (CombinedDomainXYPlot)chart.getPlot();
        List plots = cplot.getSubplots();
        XYPlot p = (XYPlot) plots.get(1);
        p.setDataset(1, data);
    }

    public void updatePersonDataset(HistogramChartDataset data){
        //set histogram data
        CombinedDomainXYPlot cplot = (CombinedDomainXYPlot)chart.getPlot();
        List plots = cplot.getSubplots();
        XYPlot p = (XYPlot) plots.get(0);
        p.setDataset(0, data);
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
//                        chart.draw(g,new Rectangle(450, 400));
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

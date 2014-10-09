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

package com.itemanalysis.jmetrik.graph.histogram;

import com.itemanalysis.jmetrik.swing.GraphPanel;

import java.awt.*;

import org.apache.log4j.Logger;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.StandardXYBarPainter;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.ui.RectangleInsets;

public class HistogramPanel extends GraphPanel {

    private HistogramCommand command = null;
    private JFreeChart chart = null;
    private String chartTitle = "";
    private String chartSubtitle = "";
    private String xlabel = "";
    private String ylabel = "";
    private boolean hasGroupingVariable = false;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public HistogramPanel(HistogramCommand command){
        this.command = command;
        processCommand();
    }

    public void updateDataset(HistogramChartDataset data){
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDataset(data);
    }

    private void processCommand(){
        try{
            chartTitle = command.getFreeOption("title").getString();
            chartSubtitle = command.getFreeOption("subtitle").getString();

            if(command.getFreeOption("groupvar").hasValue()){
                hasGroupingVariable = true;
            }

            xlabel = command.getFreeOption("variable").getString();
            ylabel="";
            if(command.getSelectOneOption("yaxis").isValueSelected("freq")){
                ylabel = "Frequency";
            }else{
                ylabel = "Density";
            }


        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

    public void setGraph(){
        HistogramChartDataset dataset=null;
        dataset = new HistogramChartDataset();

        chart = HistogramChart.createHistogram(
                chartTitle,
                xlabel, //x-axis label
                ylabel, //y-axis label
                dataset,
                chartOrientation,
                hasGroupingVariable, //legend
                true, //tooltips
                false //urls
        );

        if(chartSubtitle!=null && !"".equals(chartSubtitle)){
            TextTitle subtitle1 = new TextTitle();
            chart.addSubtitle(subtitle1);
        }

        XYPlot plot = (XYPlot) chart.getPlot();
        if(hasGroupingVariable) plot.setForegroundAlpha(0.80f);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        XYBarRenderer renderer = (XYBarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(true);
        renderer.setShadowVisible(false);

        //next two lines are temp setting for book
        //these two lines will create a histogram with white bars so it appears as just the bar outline
//        renderer.setBarPainter(new StandardXYBarPainter());
//        renderer.setSeriesPaint(0, Color.white);

        ChartPanel panel = new ChartPanel(chart);
        panel.getPopupMenu().addSeparator();
        this.addJpgMenuItem(this, panel.getPopupMenu());
        panel.setPreferredSize(new Dimension(width, height));

//        //temp setting for book
//        this.addLocalEPSMenuItem(this, panel.getPopupMenu(), chart);//remove this line for public release versions

        chart.setPadding(new RectangleInsets(20.0,5.0,20.0,5.0));
        this.setBackground(Color.WHITE);
        this.add(panel);


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

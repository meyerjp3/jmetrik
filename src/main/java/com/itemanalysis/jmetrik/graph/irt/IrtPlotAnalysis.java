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

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.SwingWorker;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.distribution.UniformDistributionApproximation;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmPCM;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class IrtPlotAnalysis extends SwingWorker<IrtPlotPanel, Void> {

    private IrtPlotCommand command = null;
    private IrtPlotPanel irtPanel = null;
    private Throwable theException = null;
    private Connection conn = null;
    private int progressValue=0;
    private double maxProgress=100.0;
    private int lineNumber=0;
    private StopWatch sw = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    private DataTableName tableName = null;
    private ArrayList<String> variables = null;
    private boolean categoryProbability = true;
    private boolean plotIcc = true;
    private boolean plotItemInfo = false;
    private boolean plotTcc = true;
    private boolean plotTestInfo = false;
    private boolean plotTestStdError = false;
    private double min = -4.0;
    private double max = 4.0;
    private int points = 51;
    private double[] theta = null;
    private double[] tcc = null;
    private double[] tinfo = null;
    private DatabaseAccessObject dao = null;
    private boolean savePlots = false;
    private String savePath = "";

    private ArrayList<VariableChangeListener> variableChangeListeners = null;

    public IrtPlotAnalysis(Connection conn, DatabaseAccessObject dao, IrtPlotCommand command, IrtPlotPanel irtPanel){
        this.command = command;
        this.irtPanel = irtPanel;
        this.conn = conn;
        this.dao = dao;
        variables = new ArrayList<String>();
        variableChangeListeners = new ArrayList<VariableChangeListener>();

    }

    private void initializeProgress()throws SQLException {
        int nrow = dao.getRowCount(conn, tableName);
        maxProgress = (double)nrow;
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    public void summarize()throws SQLException{
        Statement stmt = null;
        ResultSet rs=null;
        double maxPossibleTestScore = 0.0;

        initializeProgress();

        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        select.addColumn(sqlTable, "*");
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs=stmt.executeQuery(select.toString());

        VariableName name = new VariableName("name");
        VariableName ncat = new VariableName("ncat");
        VariableName delta = new VariableName("bparam");
        VariableName tau = null;

        if(plotTcc) tcc = new double[theta.length];
        if(plotTestInfo || plotTestStdError) tinfo = new double[theta.length];

        String itemName = "";
        double difficulty = 0.0;
        double[] step = null;
        int nCat = 2;
        Double cats = 0.0;
        XYSeriesCollection collection = null;
        double maxItemScore = 1;

        ItemResponseModel irm = null;

        while(rs.next()){
            collection = new XYSeriesCollection();
            itemName = rs.getString(name.nameForDatabase());
            if(variables.contains(itemName)){
                cats = rs.getDouble(ncat.nameForDatabase());
                maxPossibleTestScore += cats-1.0;
                nCat = cats.intValue();
                difficulty = rs.getDouble(delta.nameForDatabase());
                if(nCat>2){
                    step = new double[nCat-1];
                    for(int i=0;i<nCat-1;i++){
                        tau = new VariableName("step" + (i+1));
                        step[i] = rs.getDouble(tau.nameForDatabase());
                    }
                    irm = new IrmPCM(difficulty, step, 1.0);
                }else{
                    irm = new Irm3PL(difficulty, 1.0);
                }



                //plot icc
                if(plotIcc){
                    if(nCat>2){
                        if(categoryProbability){
                            //plot all category probabilities for polytomous item
                            for(int i=0;i<nCat;i++){
                                collection.addSeries(getCategoryProbability(irm, i));
                            }
                        }else{
                            maxItemScore = (double)(nCat-1);
                            //plot expected value for polytomous item
                            collection.addSeries(getExpectedValue(irm));
                        }
                    }else{
                        //plot probability of a correct response for binary item
                        collection.addSeries(getCategoryProbability(irm, 1));
                    }
                }

                irtPanel.updateOrdinate(itemName, 0.0, maxItemScore);
                if(plotItemInfo){
                    collection.addSeries(getItemInformation(irm));
                    if(!plotIcc){
                        irtPanel.setOrdinateLabel(itemName, "Item Information");
                        irtPanel.setOrdinateAutoRange(itemName, true);
                    }
                }
                irtPanel.updateDataset(itemName, collection, collection.getSeriesCount()>1);


                if(plotTcc){
                    incrementTcc(irm);
                }

                if(plotTestInfo || plotTestStdError){
                    incrementTestInfo(irm);
                }

            }//end check for selected items
            updateProgress();
        }//end while
        rs.close();
        stmt.close();

        if(plotTcc || plotTestInfo || plotTestStdError) {
            collection = new XYSeriesCollection();
            if(plotTcc){
                addTcc(collection);
                irtPanel.updateOrdinate("jmetrik-tcc-tif-tse", 0.0, maxPossibleTestScore);
                irtPanel.setOrdinateLabel("jmetrik-tcc-tif-tse", "True Score");
            }else{
                irtPanel.setOrdinateAutoRange("jmetrik-tcc-tif-tse", true);
                if(plotTestStdError && !plotTestInfo){
                    irtPanel.setOrdinateLabel("jmetrik-tcc-tif-tse", "Standard Error");
                }
                if(plotTestInfo && !plotTestStdError ){
                    irtPanel.setOrdinateLabel("jmetrik-tcc-tif-tse", "Test Information");
                }
            }
            if(plotTestInfo) addTestInfo(collection);
            if(plotTestStdError) addTestStdError(collection);
            irtPanel.updateDataset("jmetrik-tcc-tif-tse", collection, collection.getSeriesCount()>1);


        }

    }

    private void addTcc(XYSeriesCollection collection){
        XYSeries tccSeries = new XYSeries("TCC");
        int index = 0;
        for(double t : theta){
            tccSeries.add(t, tcc[index]);
            index++;
        }
        collection.addSeries(tccSeries);
    }

    private void addTestInfo(XYSeriesCollection collection){
        XYSeries tifSeries = new XYSeries("TIF");
        int index = 0;
        for(double t : theta){
            tifSeries.add(t, tinfo[index]);
            index++;
        }
        collection.addSeries(tifSeries);
    }

    private void addTestStdError(XYSeriesCollection collection){
        XYSeries tseSeries = new XYSeries("TSE");
        int index = 0;
        for(double t : theta){
            tseSeries.add(t, 1.0/Math.sqrt(tinfo[index]));
            index++;
        }
        collection.addSeries(tseSeries);
    }

//    private XYSeries getCategoryProbability(double difficulty, double[] step, int category){
//        XYSeries series = new XYSeries(category);
//        double prob = 0.0;
//        for(double t : theta){
//            prob = rsm.value(t, difficulty, step, category);
//            series.addArgument(t, prob);
//        }
//        return series;
//    }

    private XYSeries getCategoryProbability(ItemResponseModel irm, int category){
        XYSeries series = new XYSeries(category);

        double prob = 0.0;
        for(double t : theta){
            prob = irm.probability(t, category);
            series.add(t, prob);
        }
        return series;
    }

//    private XYSeries getExpectedValue(double difficulty, double[] step){
//        XYSeries series = new XYSeries("Expected Value");
//        double value = 0.0;
//        for(double t : theta){
//            value = rsm.expectedValue(t, difficulty, step);
//            series.addArgument(t, value);
//        }
//        return series;
//    }

    private XYSeries getExpectedValue(ItemResponseModel irm){
        XYSeries series = new XYSeries("Expected Value");
        double value = 0.0;
        for(double t : theta){
            value = irm.expectedValue(t);
            series.add(t, value);
        }
        return series;
    }

//    private void incrementTcc(double difficulty, double[] step){
//        double value = 0.0;
//        int index = 0;
//        for(double t : theta){
//            value = rsm.expectedValue(t, difficulty, step);
//            tcc[index] += value;
//            index++;
//        }
//    }

    private void incrementTcc(ItemResponseModel irm){
        double value = 0.0;
        int index = 0;
        for(double t : theta){
            value = irm.expectedValue(t);
            tcc[index] += value;
            index++;
        }
    }

//    private XYSeries getItemInformation(double difficulty, double[] step){
//        XYSeries series = new XYSeries("Information");
//        double info = 0.0;
//        for(double t : theta){
//            info = rsm.denomInf(t, difficulty, step);
//            series.addArgument(t, info);
//        }
//        return series;
//    }

    private XYSeries getItemInformation(ItemResponseModel irm){
        XYSeries series = new XYSeries("Information");
        double info = 0.0;
        for(double t : theta){
            info = irm.itemInformationAt(t);
            series.add(t, info);
        }
        return series;
    }

//    private void incrementTestInfo(double difficulty, double[] step){
//        double value = 0.0;
//        int index = 0;
//        for(double t : theta){
//            value = rsm.denomInf(t, difficulty, step);
//            tinfo[index] += value;
//            index++;
//        }
//    }

    private void incrementTestInfo(ItemResponseModel irm){
        double value = 0.0;
        int index = 0;
        for(double t : theta){
            value = irm.itemInformationAt(t);
            tinfo[index] += value;
            index++;
        }
    }

    protected IrtPlotPanel doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Irt Plot...");
        this.firePropertyChange("progress-on", null, null);

        logger.info(command.paste());

        try{
            //database
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));

            //parse command
            variables = command.getFreeOptionList("variables").getString();
            categoryProbability = command.getSelectOneOption("type").isValueSelected("prob");
            plotIcc = command.getSelectAllOption("item").isArgumentSelected("icc");
            plotItemInfo = command.getSelectAllOption("item").isArgumentSelected("info");
            plotTcc = command.getSelectAllOption("person").isArgumentSelected("tcc");
            plotTestInfo = command.getSelectAllOption("person").isArgumentSelected("info");
            plotTestStdError = command.getSelectAllOption("person").isArgumentSelected("se");
            min = command.getPairedOptionList("xaxis").getDoubleAt("min").doubleValue();
            max = command.getPairedOptionList("xaxis").getDoubleAt("max").doubleValue();
            points = command.getPairedOptionList("xaxis").getIntegerAt("points").intValue();

            savePlots = command.getFreeOption("output").hasValue();
            if(savePlots) savePath = command.getFreeOption("output").getString();

//            deprecated class
//            ThetaDistribution thetaDist = new ThetaDistribution();
//            thetaDist.uniformPoints(points, min, max);
            UniformDistributionApproximation thetaDist = new UniformDistributionApproximation(min, max, points);
            theta = thetaDist.getPoints();
            thetaDist = null;

            summarize();
            if(savePlots){
                this.firePropertyChange("progress-ind-on", null, null);
                firePropertyChange("status", "", "Saving plots");
                irtPanel.savePlots(savePath);
            }

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible
        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return irtPanel;
    }

    //===============================================================================================================
    //Handle variable changes here
    //===============================================================================================================
    public synchronized void addVariableChangeListener(VariableChangeListener l){
        variableChangeListeners.add(l);
    }

    public synchronized void removeVariableChangeListener(VariableChangeListener l){
        variableChangeListeners.remove(l);
    }

    public synchronized  void removeAllVariableChangeListeners(){
        variableChangeListeners.clear();
    }

    public void fireVariableChanged(VariableChangeEvent event){
        System.out.println("TestSclingAnalysis: firing variable changed=" + event.getVariable().getName().toString());
        for(VariableChangeListener l : variableChangeListeners){
            l.variableChanged(event);
        }
    }
    //===============================================================================================================

    @Override
    protected void done(){
        try{
            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }
        }catch(Exception ex){
            logger.fatal(theException.getMessage(), theException);
            firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

}

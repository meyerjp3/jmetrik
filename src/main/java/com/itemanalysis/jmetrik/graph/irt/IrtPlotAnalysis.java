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

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import javax.swing.SwingWorker;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.stats.irt.linking.DbItemParameterSet;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.distribution.ContinuousDistributionApproximation;
import com.itemanalysis.psychometrics.distribution.UniformDistributionApproximation;
import com.itemanalysis.psychometrics.irt.estimation.IrtExaminee;
import com.itemanalysis.psychometrics.irt.estimation.ItemResponseVector;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.statistics.TwoWayTable;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
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
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");
    private DataTableName tableName = null;
    private ArrayList<String> variables = null;
    private boolean categoryProbability = true;
    private boolean plotIcc = true;
    private boolean plotItemInfo = false;
    private boolean plotTcc = true;
    private boolean plotTestInfo = false;
    private boolean plotTestStdError = false;
    private double[] theta = null;
    private double[] tcc = null;
    private double[] tinfo = null;
    private DatabaseAccessObject dao = null;
    private boolean savePlots = false;
    private String savePath = "";
    private ItemResponseModel[] itemResponseModels = null;
    private DataTableName responseTableName = null;
    private boolean hasResponseData = false;
    private ItemResponseVector[] responseVector = null;
    private LinkedHashMap<VariableName, ItemResponseModel> itemParameterSet = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;

    int nBins = 10;
    double[] eap = null;
    DescriptiveStatistics eapStats = new DescriptiveStatistics();
    TwoWayTable[] table = null;

    public IrtPlotAnalysis(Connection conn, DatabaseAccessObject dao, IrtPlotCommand command, IrtPlotPanel irtPanel){
        this.command = command;
        this.irtPanel = irtPanel;
        this.conn = conn;
        this.dao = dao;
        variables = new ArrayList<String>();
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void initializeProgress()throws SQLException {
        this.firePropertyChange("progress-on", null, null);
        int nrow = dao.getRowCount(conn, tableName);
        maxProgress = (double)nrow;
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    private void getItemResponseModels()throws SQLException{
        ArrayList<VariableName> selectedVariableNames = new ArrayList<VariableName>();
        for(String s : variables){
            selectedVariableNames.add(new VariableName(s));
        }

        DbItemParameterSet dbItemParameterSet = new DbItemParameterSet();
        itemParameterSet = dbItemParameterSet.getItemParameters(
                conn, tableName, selectedVariableNames, true
        );

        itemResponseModels = new ItemResponseModel[itemParameterSet.size()];

//        int j=0;
//        for(String s : itemParameterSet.keySet()){
//            itemResponseModels[j] = itemParameterSet.get(s);
//            j++;
//        }

        //In order of the item parameter table
        int j=0;
        for(String s : variables){
            itemResponseModels[j] = itemParameterSet.get(s);
            j++;
        }

    }

    //NOTE: The scaling constant 1 or 1.7 is only incorporated if it is contained in the item parameter table.
    //There is no default for the scaling constant that is set here.

    public void summarize()throws SQLException{
        initializeProgress();

        if(plotTcc) tcc = new double[theta.length];
        if(plotTestInfo || plotTestStdError) tinfo = new double[theta.length];

        int j=0;
        double maxPossibleTestScore = 0.0;
        double maxItemScore = 1;
        XYSeriesCollection collection = null;
        ItemResponseModel irm = null;
        for(VariableName v : itemParameterSet.keySet()){

            collection = new XYSeriesCollection();
            irm = itemParameterSet.get(v);
            maxPossibleTestScore += irm.getMaxScoreWeight();
            int ncat = irm.getNcat();

            //plot icc
            if(plotIcc){
                if(ncat>2){
                    if(categoryProbability){
                        //plot all category probabilities for polytomous item
                        for(int i=0;i<ncat;i++){
                            collection.addSeries(getCategoryProbability(irm, i));
                        }
                    }else{
                        //plot expected value for polytomous item
                        collection.addSeries(getExpectedValue(irm));
                        maxItemScore = irm.getMaxScoreWeight();
                    }
                }else{
                    //plot probability of a correct response for binary item
                    collection.addSeries(getCategoryProbability(irm, 1));
                }
            }

            irtPanel.updateOrdinate(v.toString(), 0.0, maxItemScore);
            if(plotItemInfo){
                collection.addSeries(getItemInformation(irm));
                if(!plotIcc){
                    irtPanel.setOrdinateLabel(v.toString(), "Item Information");
                    irtPanel.setOrdinateAutoRange(v.toString(), true);
                }
            }

            if(hasResponseData){
                addObservedPoints(j, collection);
                irtPanel.updateDatasetLinesAndPoints(v.toString(), collection, collection.getSeriesCount()>2);
            }else{
                irtPanel.updateDataset(v.toString(), collection, collection.getSeriesCount()>1);
            }


            if(plotTcc){
                incrementTcc(irm);
            }

            if(plotTestInfo || plotTestStdError){
                incrementTestInfo(irm);
            }


            j++;
        }

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

    private ArrayList<VariableAttributes> reorderAttributes(ArrayList<VariableAttributes> variableAttributes){
        ArrayList<VariableAttributes> orderedAttributes = new ArrayList<VariableAttributes>();

        outer:
        for(int j=0;j<itemResponseModels.length;j++){
            inner:
            for(VariableAttributes v : variableAttributes){
                if(itemResponseModels[j].getName().equals(v.getName())){
                    orderedAttributes.add(v);
                    break inner;
                }
            }
        }
        return orderedAttributes;
    }

    private void summarizeResponseData()throws SQLException{
        this.firePropertyChange("progress-ind-on", null, null);

        Statement stmt = null;
        ResultSet rs = null;

        //Summarize item response vectors
        try{
            int nrow = dao.getRowCount(conn, responseTableName);
            responseVector = new ItemResponseVector[nrow];

            eap = new double[nrow];
            IrtExaminee examinee = new IrtExaminee(itemResponseModels);

            //Selecte variables must be in same order as in the array of item response models
            VariableTableName variableTableName = new VariableTableName(responseTableName.toString());
            ArrayList<VariableAttributes> variableAttributes = reorderAttributes(dao.getSelectedVariables(conn, variableTableName, variables));


            //Query the db. Variables include the select items and the grouping variable is one is available.
            Table sqlTable = new Table(responseTableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();

            for(VariableAttributes v : variableAttributes){
                select.addColumn(sqlTable, v.getName().nameForDatabase());
            }
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            int i=0;
            int c = 0;
            int ncol = itemResponseModels.length;
            byte[] rv = null;
            Object response = null;
            ItemResponseVector iVec = null;
            while(rs.next()){
                c = 0;
                rv = new byte[ncol];

                for(VariableAttributes v : variableAttributes){
                    response = rs.getObject(v.getName().nameForDatabase());
                    if((response==null || response.equals("") || response.equals("NA"))){
                        rv[c] = -1;//code for omitted responses
                    }else{
                        rv[c] = (byte)v.getItemScoring().computeItemScore(response);
                    }
                    c++;
                }
                iVec = new ItemResponseVector(rv, 1.0);
                responseVector[i] = iVec;

                //Compute EAP estimate and increment descriptive statistics
                examinee.setResponseVector(responseVector[i]);
                eap[i] = examinee.eapEstimate(0, 1, -5, 5, 51);//TODO allow user to set the arguments
                eapStats.addValue(eap[i]);

                i++;
            }//end data summary

            condenseObservedResponses();

        }catch(SQLException ex){
            throw(ex);
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }
    }

    private void condenseObservedResponses(){
        double response = 0;
        double min = eapStats.getMin();
        double max = eapStats.getMax();
        double midPoint = 0;

        //initialize tables
        table = new TwoWayTable[itemResponseModels.length];
        for(int i=0;i<table.length;i++){
            table[i] = new TwoWayTable();
        }

        //Create two-way frequency tables
        for(int i=0;i<responseVector.length;i++){
            midPoint = findMidPoint(eap[i], nBins, min, max);

            for(int j=0;j<responseVector[i].getNumberOfItems();j++){
                response = responseVector[i].getResponseAt(j);
                if(response!=-1){
                    table[j].addValue(midPoint, Double.valueOf(response).byteValue());
                }
            }
        }

    }

    private void addObservedPoints(int itemIndex, XYSeriesCollection collection){

        TwoWayTable itemTable = table[itemIndex];
        Iterator<Comparable<?>> rowIter = itemTable.rowValuesIterator();
        Iterator<Comparable<?>> colIter = itemTable.colValuesIterator();
        double r;
        double prop;

        while(colIter.hasNext()){
            Comparable<?> ci = colIter.next();
            int c = ((Byte)ci).intValue();
            XYSeries series = new XYSeries(c+"p");

            if((itemResponseModels[itemIndex].getNcat()>2) ||
                    (itemResponseModels[itemIndex].getNcat()==2 && c==1) ){

                rowIter = itemTable.rowValuesIterator();

                while(rowIter.hasNext()){
                    Comparable<?> ri = rowIter.next();
                    r = (Double)ri;
                    prop = (double)itemTable.getCount(ri, ci)/(double)itemTable.getRowCount(ri);
                    series.add(r, prop);
                }
                collection.addSeries(series);

            }

        }
    }

    private double findMidPoint(double x, int nBins, double min, double max){
        double h = (max-min)/nBins;
        double upperBound;

        for(int i=0;i<nBins;i++){
            upperBound = min+(i+1)*h;
            if(x <= upperBound) return upperBound-h/2;
        }
        return max-h/2;
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

    private XYSeries getCategoryProbability(ItemResponseModel irm, int category){
        XYSeries series = new XYSeries(category);

        double prob = 0.0;
        for(double t : theta){
            prob = irm.probability(t, category);
            series.add(t, prob);
        }
        return series;
    }

    private XYSeries getExpectedValue(ItemResponseModel irm){
        XYSeries series = new XYSeries("Expected Value");
        double value = 0.0;
        for(double t : theta){
            value = irm.expectedValue(t);
            series.add(t, value);
        }
        return series;
    }

    private void incrementTcc(ItemResponseModel irm){
        double value = 0.0;
        int index = 0;
        for(double t : theta){
            value = irm.expectedValue(t);
            tcc[index] += value;
            index++;
        }
    }

    private XYSeries getItemInformation(ItemResponseModel irm){
        XYSeries series = new XYSeries("Information");
        double info = 0.0;
        for(double t : theta){
            info = irm.itemInformationAt(t);
            series.add(t, info);
        }
        return series;
    }

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
            double min = command.getPairedOptionList("xaxis").getDoubleAt("min").doubleValue();
            double max = command.getPairedOptionList("xaxis").getDoubleAt("max").doubleValue();
            int points = command.getPairedOptionList("xaxis").getIntegerAt("points").intValue();

            savePlots = command.getFreeOption("output").hasValue();
            if(savePlots) savePath = command.getFreeOption("output").getString();

            getItemResponseModels();

            hasResponseData = command.getPairedOptionList("response").hasValue();
            if(!categoryProbability) hasResponseData = false;//Do not add observed proportions when expected value selected
            if(hasResponseData){
                String tn = command.getPairedOptionList("response").getStringAt("table");
                responseTableName = new DataTableName(tn);
                nBins = command.getPairedOptionList("response").getIntegerAt("bins");
                summarizeResponseData();
            }

            UniformDistributionApproximation thetaDist = new UniformDistributionApproximation(min, max, points);
            theta = thetaDist.getPoints();

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
            scriptLogger.info(command.paste());
        }catch(Exception ex){
            logger.fatal(theException.getMessage(), theException);
            firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

}

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

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.distribution.UniformDistributionApproximation;
import com.itemanalysis.psychometrics.kernel.*;
import com.itemanalysis.psychometrics.measurement.KernelRegressionCategories;
import com.itemanalysis.psychometrics.measurement.KernelRegressionItem;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;

public class NonparametricCurveAnalysis extends SwingWorker<String, Void> {

    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private NonparametricCurveCommand command = null;
    private NonparametricCurvePanel nonparametricPanel = null;
    private TreeMap<VariableAttributes, KernelRegressionItem> focalRegression = null;
    private TreeMap<VariableAttributes, KernelRegressionItem> referenceRegression = null;
    private TreeMap<VariableAttributes, KernelRegressionCategories> categoryRegression = null;
    private TreeMap<VariableAttributes, KernelRegressionItem> kernelRegression = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private TreeMap<VariableAttributes, XYSeriesCollection> xySeriesMap = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    private VariableAttributes regressorVariable = null;
    private VariableAttributes groupByVariable = null;
    private boolean hasGroupVariable = false;
    private ArrayList<VariableAttributes> variables = null;
    private double maxProgress = 100.0;
    private double sampleSize = 0.0;
    private DataTableName tableName = null;
    private VariableTableName variableTableName = null;
    private int progressValue = 0;
    private int lineNumber = 0;
    private Throwable theException = null;
    private StopWatch sw = null;
    private String focalCode = "";
    private String referenceCode = "";
    private String savePath = "";
    private boolean savePlots = false;
    private int gridPoints = 51;
    boolean allCategories = false;
    private UniformDistributionApproximation uniformDistributionApproximation = null;
    private double bwAdjustment = 1.0;
    private NonparametricIccBandwidth bandwidth = null;
    private KernelFunction kernelFunction = null;

    public NonparametricCurveAnalysis(Connection conn, DatabaseAccessObject dao, NonparametricCurveCommand command, NonparametricCurvePanel nonparametricPanel){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.nonparametricPanel = nonparametricPanel;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void initializeProgressBar()throws SQLException{
        sampleSize = dao.getRowCount(conn, tableName);
        maxProgress = (double)sampleSize;
        maxProgress*=2.0;
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/ maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    private void initialize()throws SQLException, IllegalArgumentException{

        String db = command.getPairedOptionList("data").getStringAt("db");
        String table = command.getPairedOptionList("data").getStringAt("table");
        tableName = new DataTableName(table);
        variableTableName = new VariableTableName(table);

        String xvar = command.getFreeOption("xvar").getString();
        regressorVariable = dao.getVariableAttributes(conn, variableTableName, xvar);

        ArrayList<String> selectedVariables = command.getFreeOptionList("variables").getString();
        variables = dao.getSelectedVariables(conn, variableTableName, selectedVariables);

        allCategories = command.getSelectOneOption("curves").isValueSelected("all");
        savePlots = command.getFreeOption("output").hasValue();
        if(savePlots) savePath = command.getFreeOption("output").getString();

        if(command.getFreeOption("groupvar").hasValue()){
            String gvar = command.getFreeOption("groupvar").getString();
            groupByVariable = dao.getVariableAttributes(conn, variableTableName, gvar);
            hasGroupVariable = true;
            focalCode = command.getPairedOptionList("codes").getStringAt("focal");
            referenceCode = command.getPairedOptionList("codes").getStringAt("reference");
            allCategories = false;


            //convert focal and reference codes to double format is they are numbers
            double focDouble = 0;
            double refDouble = 0;
            try{
                focDouble = Double.parseDouble(focalCode);
                focalCode = Double.valueOf(focDouble).toString();
            }catch(NumberFormatException ex){
                //data is string
            }
            try{
                refDouble = Double.parseDouble(referenceCode);
                referenceCode = Double.valueOf(refDouble).toString();
            }catch(NumberFormatException ex){
                //data is string
            }

        }

        //initialize progress and compute sample size
        initializeProgressBar();

        //bandwidth adjustment factor
        bwAdjustment = command.getFreeOption("adjust").getDouble();

        //kernel function properties
        String kernelTypeString = command.getSelectOneOption("kernel").getSelectedArgument();
        KernelFactory kernelFactory = new KernelFactory(kernelTypeString);
        kernelFunction = kernelFactory.getKernelFunction();

        initializeGridPoints();

    }

    private void initializeGridPoints()throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        //connect to db
        try{
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(sqlTable, regressorVariable.getName().nameForDatabase());
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            Min min = new Min();
            Max max = new Max();
            Mean mean = new Mean();
            StandardDeviation sd = new StandardDeviation();

            double value = 0.0;
            while(rs.next()){
                value = rs.getDouble(regressorVariable.getName().nameForDatabase());
                if(!rs.wasNull()){
                    min.increment(value);
                    max.increment(value);
                    mean.increment(value);
                    sd.increment(value);
                }
                updateProgress();
            }
            rs.close();
            stmt.close();

            //evaluation points
            double sdv = sd.getResult();
            double mn = mean.getResult();
            double lower = mn-2.5*sdv;
            double upper = mn+2.5*sdv;
            bwAdjustment *= sdv;
            bandwidth = new NonparametricIccBandwidth(sampleSize, bwAdjustment);
            gridPoints = command.getFreeOption("gridpoints").getInteger();
//            uniformDistributionApproximation = new UniformDistributionApproximation(
//                    min.getResult(), max.getResult(), gridPoints);
            uniformDistributionApproximation = new UniformDistributionApproximation(
                    lower, upper, gridPoints);

        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }


    }

    public void evaluateDIF()throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        //create focal map
        focalRegression = new TreeMap<VariableAttributes, KernelRegressionItem>();
        for(VariableAttributes v : variables){
            KernelRegressionItem kItem = new KernelRegressionItem(v, kernelFunction, bandwidth, uniformDistributionApproximation);
            focalRegression.put(v, kItem);
        }

        //create reference map
        if(hasGroupVariable){
            referenceRegression = new TreeMap<VariableAttributes, KernelRegressionItem>();
            for(VariableAttributes v : variables){
                KernelRegressionItem kItem = new KernelRegressionItem(v, kernelFunction, bandwidth, uniformDistributionApproximation);
                referenceRegression.put(v, kItem);
            }
        }

        //determine whether group variable is double or not
        boolean groupVariableIsDouble = false;
        if(groupByVariable.getType().getDataType()== DataType.DOUBLE) groupVariableIsDouble = true;

        try{
            //connect to db
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(VariableAttributes v : variables){
                select.addColumn(sqlTable, v.getName().nameForDatabase());
            }
            select.addColumn(sqlTable, regressorVariable.getName().nameForDatabase());
            select.addColumn(sqlTable, groupByVariable.getName().nameForDatabase());

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            KernelRegressionItem kernelRegressionItem;
            Object itemResponse;
            Double score;
            Object tempGroup;
            String group;

            //analyze by groups
            while(rs.next()){
                tempGroup = rs.getObject(groupByVariable.getName().nameForDatabase());
                if(tempGroup==null){
                    group="";//will not be counted if does not match focal or reference code
                }else{
                    if(groupVariableIsDouble){
                        group = Double.valueOf((Double)tempGroup).toString();
                    }else{
                        group = ((String)tempGroup).trim();
                    }
                }

                //get independent variable value
                //omit examinees with missing data
                //examinees with missing group code omitted
                score = rs.getDouble(regressorVariable.getName().nameForDatabase());
                if(!rs.wasNull()){
                    if(focalCode.equals(group)){
                        for(VariableAttributes v : focalRegression.keySet()){
                            kernelRegressionItem = focalRegression.get(v);
                            itemResponse = rs.getObject(v.getName().nameForDatabase());
                            if(itemResponse!=null) kernelRegressionItem.increment(score, itemResponse);
                        }
                    }else if(referenceCode.equals(group)){
                        for(VariableAttributes v : referenceRegression.keySet()){
                            kernelRegressionItem = referenceRegression.get(v);
                            itemResponse = rs.getObject(v.getName().nameForDatabase());
                            if(itemResponse!=null) kernelRegressionItem.increment(score, itemResponse);
                        }
                    }
                }
                updateProgress();
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }




        this.firePropertyChange("progress-ind-on", null, null);
    }

    public void evaluateAll()throws SQLException{
        categoryRegression = new TreeMap<VariableAttributes, KernelRegressionCategories>();
        for(VariableAttributes v : variables){
            KernelRegressionCategories kCat = new KernelRegressionCategories(v, kernelFunction, bandwidth, uniformDistributionApproximation);
            categoryRegression.put(v, kCat);
        }

        //connect to db
        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        for(VariableAttributes v : variables){
            select.addColumn(sqlTable, v.getName().nameForDatabase());
        }
        select.addColumn(sqlTable, regressorVariable.getName().nameForDatabase());
        if(hasGroupVariable) select.addColumn(sqlTable, groupByVariable.getName().nameForDatabase());

        ResultSet rs = null;
        Statement stmt = null;

        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            KernelRegressionCategories kernelRegressionCategories;
            Object itemResponse;
            Double score;
            Object tempGroup;
            String group;

            while(rs.next()){
                //increment kernel regression objects
                //omit examinees with missing data
                score = rs.getDouble(regressorVariable.getName().nameForDatabase());
                if(!rs.wasNull()){
                    for(VariableAttributes v : categoryRegression.keySet()){
                        kernelRegressionCategories = categoryRegression.get(v);
                        itemResponse = rs.getObject(v.getName().nameForDatabase());
                        if(itemResponse!=null) kernelRegressionCategories.increment(score, itemResponse);
                    }
                }
                updateProgress();
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

        this.firePropertyChange("progress-ind-on", null, null);
    }

    public void evaluate() throws SQLException{
        kernelRegression = new TreeMap<VariableAttributes, KernelRegressionItem>();
        for(VariableAttributes v : variables){
            KernelRegressionItem kItem = new KernelRegressionItem(v, kernelFunction, bandwidth, uniformDistributionApproximation);
            kernelRegression.put(v, kItem);
        }

        ResultSet rs = null;
        Statement stmt = null;

        try{
            //connect to db
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(VariableAttributes v : variables){
                select.addColumn(sqlTable, v.getName().nameForDatabase());
            }
            select.addColumn(sqlTable, regressorVariable.getName().nameForDatabase());
            if(hasGroupVariable) select.addColumn(sqlTable, groupByVariable.getName().nameForDatabase());

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            KernelRegressionItem kernelRegressionItem;
            Object itemResponse;
            Double score;
            Object tempGroup;
            String group;

            while(rs.next()){
                //increment kernel regression objects
                //omit examinees with missing data
                score = rs.getDouble(regressorVariable.getName().nameForDatabase());
                if(!rs.wasNull()){
                    for(VariableAttributes v : kernelRegression.keySet()){
                        kernelRegressionItem = kernelRegression.get(v);
                        itemResponse = rs.getObject(v.getName().nameForDatabase());
                        if(itemResponse!=null) kernelRegressionItem.increment(score, itemResponse);
                    }
                }
                updateProgress();
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

        this.firePropertyChange("progress-ind-on", null, null);

    }

    /**
     * Called from done to run on EDT
     *
     * @throws IllegalArgumentException
     */
    private void publishDIFSeries()throws IllegalArgumentException{
        double[] focalTcc = new double[gridPoints];
        double[] referenceTcc = new double[gridPoints];
        double[] valueF, valueR;
        double[] points = uniformDistributionApproximation.getPoints();

        double tccMax = 0;
        double tccMin = 0;

        for(VariableAttributes v : focalRegression.keySet()){
            KernelRegressionItem kItemF, kItemR;
            XYSeries seriesF;
            XYSeries seriesR;

            //add lines to this collection repeat for each item
            XYSeriesCollection xyCollection = new XYSeriesCollection();

            //increment TCC for focal group, also create expected value series
            seriesF = new XYSeries("Focal Group");

            //add line for reference group
            seriesR = new XYSeries("Reference Group");

            kItemF = focalRegression.get(v);
            valueF = kItemF.getExpectedValues();
            tccMin+=kItemF.getMinimumPossibleScore();
            tccMax+=kItemF.getMaximumPossibleScore();

            kItemR = referenceRegression.get(v);
            valueR = kItemR.getExpectedValues();

            for(int i=0;i<focalTcc.length;i++){
                //increment focal group
                focalTcc[i] += valueF[i];
                seriesF.add(points[i], valueF[i]);

                //increment reference group
                referenceTcc[i] += valueR[i];
                seriesR.add(points[i], valueR[i]);
            }
            xyCollection.addSeries(seriesF);
            xyCollection.addSeries(seriesR);

            if(allCategories){
                nonparametricPanel.updateDatasetFor(v.getName().toString(), 0, 1, xyCollection);
            }else{
                nonparametricPanel.updateDatasetFor(
                    v.getName().toString(),
                    kItemF.getMinimumPossibleScore(),
                    kItemF.getMaximumPossibleScore(),
                    xyCollection);
            }



        }//end loop over items

        //add series for test characteristic curve
        XYSeriesCollection xyCollection = new XYSeriesCollection();
        XYSeries tccSeries1 = new XYSeries("Focal TCC");
        XYSeries tccSeries2 = new XYSeries("Reference TCC");
        for(int i=0;i<focalTcc.length;i++){
            tccSeries1.add(points[i], focalTcc[i]);
            tccSeries2.add(points[i], referenceTcc[i]);
        }
        xyCollection.addSeries(tccSeries1);
        xyCollection.addSeries(tccSeries2);

        nonparametricPanel.updateDatasetFor("tcc", tccMin, tccMax, xyCollection);

    }

    /**
     * Called from done on EDT
     *
     * @throws IllegalArgumentException
     */
    private void publishAllSeries()throws IllegalArgumentException{
        double[] tcc = new double[gridPoints];
        double[] values;
        double[] points = uniformDistributionApproximation.getPoints();

        double tccMin = 0;
        double tccMax = 0;

        for(VariableAttributes v : categoryRegression.keySet()){
            KernelRegressionCategories kCategories;
            XYSeries series;

            //add lines to this collection repeat for each item
            XYSeriesCollection xyCollection = new XYSeriesCollection();

            //increment TCC for focal group, also create expected value series
            series = new XYSeries("");
            kCategories = categoryRegression.get(v);
            tccMin+=kCategories.getMinimumPossibleScore();
            tccMax+=kCategories.getMaximumPossibleScore();
            values = kCategories.getExpectedValues();
            for(int i=0;i<tcc.length;i++){
                tcc[i] += values[i];
                series.add(points[i], values[i]);
            }

            XYSeries catSeries;
            //add line for every category
            TreeMap<Object, KernelRegression> kregMap = kCategories.getRegressionMap();
            for(Object o : kregMap.keySet()){
                catSeries = new XYSeries(o.toString() + "(" + kCategories.getScoreValue(o) + ")");
                values = kregMap.get(o).value();
                for(int i=0;i<points.length;i++) catSeries.add(points[i], values[i]);
                xyCollection.addSeries(catSeries);
            }

            if(allCategories){
                nonparametricPanel.updateDatasetFor(
                    v.getName().toString(), 0, 1, xyCollection);
            }else{
                nonparametricPanel.updateDatasetFor(
                    v.getName().toString(),
                    kCategories.getMinimumPossibleScore(),
                    kCategories.getMaximumPossibleScore(),
                    xyCollection);
            }


        }//end loop over items

        //add series for test characteristic curve
        XYSeriesCollection xyCollection = new XYSeriesCollection();
        XYSeries tccSeries1 = new XYSeries("TCC");
        for(int i=0;i<tcc.length;i++){
            tccSeries1.add(points[i], tcc[i]);
        }
        xyCollection.addSeries(tccSeries1);

        nonparametricPanel.updateDatasetFor(
                "tcc",
                tccMin,
                tccMax,
                xyCollection
        );


    }

    /**
     * Called from done so that is run from EDT
     *
     * @throws IllegalArgumentException
     */
    private void publishSeries()throws IllegalArgumentException{
        double[] tcc = new double[uniformDistributionApproximation.getNumberOfPoints()];
        double[] values;
        double[] points = uniformDistributionApproximation.getPoints();

        XYSeriesCollection collection = null;

        double tccMax = 0;
        double tccMin = 0;

        for(VariableAttributes v : kernelRegression.keySet()){
            KernelRegressionItem kItem;
            XYSeries seriesData = new XYSeries(v.getName().toString());
            collection = new XYSeriesCollection();

            kItem = kernelRegression.get(v);
            values = kItem.getExpectedValues();
            tccMin+=kItem.getMinimumPossibleScore();
            tccMax+=kItem.getMaximumPossibleScore();

            //increment tcc
            for(int i=0;i<tcc.length;i++){
                seriesData.add(points[i], values[i]);
                tcc[i] += values[i];
            }
            collection.addSeries(seriesData);

            nonparametricPanel.updateDatasetFor(
                    v.getName().toString(),
                    kItem.getMinimumPossibleScore(),
                    kItem.getMaximumPossibleScore(),
                    collection);


        }//end loop over items

        //add series for test characteristic curve
        XYSeriesCollection tccCollection = new XYSeriesCollection();
        XYSeries seriesData = new XYSeries("TCC");
        for(int i=0;i<tcc.length;i++){
            seriesData.add(points[i], tcc[i]);
        }
        tccCollection.addSeries(seriesData);
        nonparametricPanel.updateDatasetFor(
            "tcc",
            tccMin,
            tccMax,
            tccCollection);

    }

    @Override
    public String doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Curves...");
        this.firePropertyChange("progress-on", null, null);
        try{
            initialize();

            if(hasGroupVariable){
                evaluateDIF();
                publishDIFSeries();
            }else if(allCategories){
                evaluateAll();
                publishAllSeries();
            }else{
                evaluate();
                publishSeries();
            }
            if(savePlots) nonparametricPanel.savePlots(savePath);
        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return "Done";
    }


    @Override
    protected void done(){
        try{
            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }else{
                logger.info("NICC bandwidth = " + bandwidth.value() + "\n" +
                        "Bandwidth adjustment factor = " + bandwidth.getAdjustmentFactor());
            }
            scriptLogger.info(command.paste());
            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible
        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible
        }
    }


    //===============================================================================================================
    //Handle variable changes here
    //   -Dialogs will use these methods to add their variable listeners
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


}

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

package com.itemanalysis.jmetrik.stats.irt.linking;

import java.sql.*;
import java.util.*;
import javax.swing.SwingWorker;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.distribution.DistributionApproximation;
import com.itemanalysis.psychometrics.distribution.NormalDistributionApproximation;
import com.itemanalysis.psychometrics.distribution.UniformDistributionApproximation;
import com.itemanalysis.psychometrics.irt.equating.*;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.optimization.BOBYQAOptimizer;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.optim.*;
import org.apache.commons.math3.optim.nonlinear.scalar.GoalType;
import org.apache.commons.math3.optim.nonlinear.scalar.MultiStartMultivariateOptimizer;
import org.apache.commons.math3.optim.nonlinear.scalar.ObjectiveFunction;
import org.apache.commons.math3.random.*;
import org.apache.log4j.Logger;

public class IrtLinkingAnalysis extends SwingWorker<String, String> {

    private IrtLinkingCommand command = null;

    private Connection conn = null;

    private JmetrikTextFile textFile = null;

    private Throwable theException = null;

    private StopWatch sw = null;

    private LinkedHashMap<String, ItemResponseModel> irmX = null;

    private LinkedHashMap<String, ItemResponseModel> irmY = null;

    private ArrayList<LinkingItemPair> commonItems = null;

    private DistributionApproximation xDistribution = null;

    private DistributionApproximation yDistribution = null;

    private int bins = -1;

    private boolean biasSd = true;

//    private MeanMeanMethod mm = null;
//
//    private MeanSigmaMethod ms = null;
//
//    private HaebaraMethod hb = null;
//
//    private StockingLordMethod sl = null;

    private EquatingCriterionType criterionType = EquatingCriterionType.Q1Q2;

    private int numberOfCommonItems = 0;

    private TransformationMethod method = TransformationMethod.STOCKING_LORD;

    private double A = 1.0;

    private double B = 0.0;

    private DataTableName tableNameItemsX = null;

    private DataTableName tableNameItemsY = null;

    private DataTableName newItemTable = null;

    private DataTableName tableNamePersonsX = null;
    private VariableName thetaNameX = null;
    private VariableName weightNameX = null;
    private boolean hasWeightX = false;
    private VariableInfo newTheta = null;

    private DataTableName tableNamePersonsY = null;
    private VariableName thetaNameY = null;
    private VariableName weightNameY = null;
    private boolean hasWeightY = false;

    private int precision = 4;

    private boolean logisticScale = true;

    private boolean newItemTableCreated = false;

    private DatabaseAccessObject dao = null;
    private DatabaseName dbName = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private IrtScaleLinking irtScaleLinking = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");


    public IrtLinkingAnalysis(Connection conn, DatabaseAccessObject dao, IrtLinkingCommand command, JmetrikTextFile textFile){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.textFile = textFile;
        variableChangeListeners = new ArrayList<VariableChangeListener>();

    }

    private void getItemParameters() throws SQLException{
        DbItemParameterSet itemParameterSet = new DbItemParameterSet();
        irmX = itemParameterSet.getFormXItemParameters(conn, tableNameItemsX, commonItems, logisticScale);
        irmY = itemParameterSet.getFormYItemParameters(conn, tableNameItemsY, commonItems, logisticScale);
    }

    public void getThetaDistributions()throws IllegalArgumentException, SQLException{
        if(command.getSelectOneOption("distribution").isValueSelected("observed")){
            getObservedAbilityValues();
        }else if(command.getSelectOneOption("distribution").isValueSelected("histogram")){
            getAbilityHistograms();
        }else if(command.getSelectOneOption("distribution").isValueSelected("uniform")){
            int numPoints = command.getPairedOptionList("uniform").getIntegerAt("bins");
            int min = command.getPairedOptionList("uniform").getIntegerAt("min");
            int max = command.getPairedOptionList("uniform").getIntegerAt("max");
            xDistribution = new UniformDistributionApproximation(min, max, numPoints);
            yDistribution = new UniformDistributionApproximation(min, max, numPoints);
        }else{
            int numPoints = command.getPairedOptionList("normal").getIntegerAt("bins");
            double mean = command.getPairedOptionList("normal").getDoubleAt("mean");
            double sd = command.getPairedOptionList("normal").getDoubleAt("sd");
            double min = command.getPairedOptionList("normal").getDoubleAt("min");
            double max = command.getPairedOptionList("normal").getDoubleAt("max");
            xDistribution = new NormalDistributionApproximation(mean, sd, min, max, numPoints);
            yDistribution = new NormalDistributionApproximation(mean, sd, min, max, numPoints);
        }
    }

    private void getObservedAbilityValues()throws SQLException{
        DbThetaDistribution dist = new DbThetaDistribution();
        xDistribution = dist.getDistribution(conn, tableNamePersonsX, thetaNameX, weightNameX, hasWeightX);
        yDistribution = dist.getDistribution(conn, tableNamePersonsY, thetaNameY, weightNameY, hasWeightY);
    }

    private void getAbilityHistograms()throws SQLException{
        DbHistogram hist = new DbHistogram();
        xDistribution = hist.getHistogram(conn, tableNamePersonsX, thetaNameX, bins);
        yDistribution = hist.getHistogram(conn, tableNamePersonsY, thetaNameY, bins);
    }

    public void computeCoefficients()throws IllegalArgumentException{

        irtScaleLinking = new IrtScaleLinking(irmX, irmY, xDistribution, yDistribution);
        irtScaleLinking.setStockingLordCritionType(criterionType);
        irtScaleLinking.setHaebaraCritionType(criterionType);
        irtScaleLinking.setPrecision(6);
        irtScaleLinking.computeCoefficients();

//        mm = new MeanMeanMethod(irmX, irmY);
//        mm.setPrecision(precision);
//        ms = new MeanSigmaMethod(irmX, irmY, biasSd);
//        ms.setPrecision(precision);
//
//        double[] startValues = {0,1};
//
//        double[] initial = {ms.getIntercept(), ms.getScale()};
//        sl = new StockingLordMethod(irmX, irmY, xDistribution, yDistribution, criterionType);
//        sl.setPrecision(precision);
//
//        PointValuePair optimum = null;
//        int numIter = 0;
//        String minMethod = "Powell's BOBYQA";
//        int numIterpolationPoints = 2 * 2;//two dimensions A and B
//        BOBYQAOptimizer underlying = new BOBYQAOptimizer(numIterpolationPoints);
//        RandomGenerator g = new JDKRandomGenerator();
//        RandomVectorGenerator generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
//        MultiStartMultivariateOptimizer optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
//        optimum = optimizer.optimize(
//                new MaxEval(2000),
//                new ObjectiveFunction(sl),
//                GoalType.MINIMIZE,
//                SimpleBounds.unbounded(2),
//                new InitialGuess(startValues)
//        );
//        numIter = optimizer.getEvaluations();
//
//        double[] slCoefficients = optimum.getPoint();
//        sl.setIntercept(slCoefficients[0]);
//        sl.setScale(slCoefficients[1]);
//        logger.info("Stocking-Lord: " + minMethod + " optimization Fmin = " + optimum.getValue() + ", Iterations = " + numIter);
//
//        hb = new HaebaraMethod(irmX, irmY, xDistribution, yDistribution, criterionType);
//        hb.setPrecision(precision);
//
//        underlying = new BOBYQAOptimizer(numIterpolationPoints);
//        g = new JDKRandomGenerator();
//        generator = new UncorrelatedRandomVectorGenerator(2, new GaussianRandomGenerator(g));
//        optimizer = new MultiStartMultivariateOptimizer(underlying, 10, generator);
//        optimum = optimizer.optimize(
//                new MaxEval(2000),
//                new ObjectiveFunction(hb),
//                GoalType.MINIMIZE,
//                SimpleBounds.unbounded(2),
//                new InitialGuess(startValues));
//        numIter = optimizer.getEvaluations();
//
//        double[] hbCoefficients = optimum.getPoint();
//        hb.setIntercept(hbCoefficients[0]);
//        hb.setScale(hbCoefficients[1]);
//        logger.info("Haebara: " + minMethod + " optimization. Fmin = " + optimum.getValue() + ", Iterations = " + numIter);

        //Stocking-lord is default transformation method
        A = irtScaleLinking.getStockingLordMethod().getScale();
        B = irtScaleLinking.getStockingLordMethod().getIntercept();

        //use a different transformation method
        if(command.getSelectOneOption("method").isValueSelected("mm")){
            A = irtScaleLinking.getMeanMeanMethod().getScale();
            B = irtScaleLinking.getMeanMeanMethod().getIntercept();
        }else if(command.getSelectOneOption("method").isValueSelected("ms")){
            A = irtScaleLinking.getMeanSigmaMethod().getScale();
            B = irtScaleLinking.getMeanSigmaMethod().getIntercept();
        }else if(command.getSelectOneOption("method").isValueSelected("hb")){
            A = irtScaleLinking.getHaebaraMethod().getScale();
            B = irtScaleLinking.getHaebaraMethod().getIntercept();
        }

    }

    public void transformItems() throws SQLException{

        newItemTable = dao.getUniqueTableName(conn, newItemTable.toString());
        dao.copyTable(conn, tableNameItemsX, newItemTable);
        newItemTableCreated = true;

        Statement stmt = null;
        ResultSet rs = null;

        conn.setAutoCommit(false);//start transaction

        try{
            String query = "SELECT * FROM " + newItemTable.getNameForDatabase();
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery(query);

            ResultSetMetaData rsmd = rs.getMetaData();
            int ncol = rsmd.getColumnCount();
            ArrayList<VariableName> colNames = new ArrayList<VariableName>();
            VariableName tempName;
            String model = "L3";
            int ncat = 2;

            for(int i=0;i<ncol;i++){
                tempName = new VariableName(rsmd.getColumnName(i+1));
                colNames.add(tempName);
            }

            VariableName modelName = new VariableName("model");
            VariableName ncatName = new VariableName("ncat");//must be in item parameter table
            VariableName aparam = new VariableName("aparam");
            VariableName bparam = new VariableName("bparam");
            VariableName stepName;

            double a = 1.0;
            double b = 0.0;
            double c = 0.0;
            double step = 0.0;

            while(rs.next()){
                ncat = rs.getInt(ncatName.nameForDatabase());
                model = rs.getString(modelName.nameForDatabase());

                //difficulty
                if(!model.equals("GR") && !model.equals("PC1")){
                    b = rs.getDouble(bparam.nameForDatabase());
                    b = A*b + B;
                    rs.updateDouble(bparam.nameForDatabase(), b);
                }


                //discrimination
                if(colNames.contains(aparam)){
                    a = rs.getDouble(aparam.nameForDatabase());
                    a = a/A;
                    rs.updateDouble(aparam.nameForDatabase(), a);
                }

                if(ncat>2){
                    for(int i=1;i<ncat;i++){
                        stepName = new VariableName("step" + i);
                        step = rs.getDouble(stepName.nameForDatabase());
                        step = A*step + B;
                        rs.updateDouble(stepName.nameForDatabase(), step);
                    }
                }

                rs.updateRow();
            }

            String desc = dao.getTableDescription(conn, newItemTable);
            desc += "\n\nForm X item parameters transformed to the scale of Form Y. " +
                    "The transformation coefficients were Slope (A) = " + A + " Intercept (B) = " + B;
            dao.setTableDescription(conn, newItemTable, desc);

            conn.commit();
            conn.setAutoCommit(true);
        }catch(SQLException ex){
            conn.rollback();
            conn.setAutoCommit(true);
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    public void transformPersons()throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            String tName = "t_" + thetaNameX.toString();
            newTheta = new VariableInfo(tName, "Form X theta on Form Y scale", VariableType.NOT_ITEM, VariableType.DOUBLE, 0, "");
            dao.addColumnToDb(conn, tableNamePersonsX, newTheta);

            Table sqlTable = new Table(tableNamePersonsX.getNameForDatabase());
            SelectQuery query = new SelectQuery();
            query.addColumn(sqlTable, thetaNameX.nameForDatabase());
            query.addColumn(sqlTable, newTheta.getName().nameForDatabase());

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery(query.toString());
            double theta = 0.0;
            double thetaT = 0.0;
            while(rs.next()){
                theta = rs.getDouble(thetaNameX.nameForDatabase());
                if(!rs.wasNull()){
                    thetaT = A*theta+B;
                    rs.updateDouble(newTheta.getName().nameForDatabase(), thetaT);
                }
                rs.updateRow();
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }


    }

//    public void printEquatingCoefficients(){
//        StringBuilder sb = new StringBuilder();
//        Formatter f = new Formatter(sb);
//        String gapFormat1 = "%-" + Math.max(13, precision+4+5) + "s";
//        String gapFormat2 = "%-" + Math.max(9, precision+4+5) + "s";
//        String intFormat = "%" + Math.max(13, (precision+4))  + "." + precision + "f";
//        String sclFormat = "%" + Math.max(9, (precision+4))  + "." + precision + "f";
//        f.format("%n");
//        f.format("%60s", "               TRANSFORMATION COEFFICIENTS                 "); f.format("%n");
//        f.format("%60s", "           Form X (New Form) to Form Y (Old Form)          "); f.format("%n");
//        f.format("%60s", "============================================================"); f.format("%n");
//        f.format("%-18s", " Method");f.format(gapFormat2, "Slope (A)"); f.format("%5s"," "); f.format(gapFormat1, "Intercept (B)"); f.format("%5s"," "); f.format("%n");
//        f.format("%60s", "------------------------------------------------------------"); f.format("%n");
//        f.format("%-17s", " Mean/Mean");     f.format(sclFormat, mm.getScale());    f.format("%5s"," "); f.format(intFormat, mm.getIntercept());
//        f.format("%5s"," "); f.format("%n");
//        f.format("%-17s", " Mean/Sigma");    f.format(sclFormat, ms.getScale());    f.format("%5s"," "); f.format(intFormat, ms.getIntercept());
//        f.format("%5s"," "); f.format("%n");
//        f.format("%-17s", " Haebara");       f.format(sclFormat, hb.getScale());   f.format("%5s"," "); f.format(intFormat, hb.getIntercept());
//        f.format("%5s"," "); f.format("%n");
//        f.format("%-17s", " Stocking-Lord"); f.format(sclFormat, sl.getScale());    f.format("%5s"," "); f.format(intFormat, sl.getIntercept());
//        f.format("%5s"," "); f.format("%n");
//        f.format("%60s", "============================================================"); f.format("%n");
//        publish(f.toString());
//    }

    @Override
    protected void process(List<String> chunks){
        for(String s : chunks){
            textFile.append(s + "\n");
        }
    }

    public void publishHeader()throws IllegalArgumentException{
        StringBuilder header = new StringBuilder();
        Formatter f = new Formatter(header);
        String s1 = String.format("%1$tB %1$te, %1$tY  %tT", Calendar.getInstance());
        int len = 38+Double.valueOf(Math.floor(Double.valueOf(s1.length()).doubleValue()/2.0)).intValue();
        String dString = "";
        dString = command.getDataString();

        f.format("%49s", "IRT SCALE LINKING"); f.format("%n");
        f.format("%" + len + "s", s1); f.format("%n"); f.format("%n");
        f.format("%-" + dString.length() + "s", dString); f.format("%n");
        publish(f.toString());
    }

    public String timeStamp(){
        String complete = "Elapsed Time: " + sw.getElapsedTime();
        return complete;
    }

    public void processCommand()throws IllegalArgumentException{
        String xDbName = command.getPairedOptionList("xitem").getStringAt("db");
        String xTable = command.getPairedOptionList("xitem").getStringAt("table");
        tableNameItemsX = new DataTableName(xTable);

        dbName = new DatabaseName(xDbName);//assumes that same database is used for all parameter

        String yDbName = command.getPairedOptionList("yitem").getStringAt("db"); //same as dbName
        String yTable = command.getPairedOptionList("yitem").getStringAt("table");
        tableNameItemsY = new DataTableName(yTable);

        ArrayList<String> xyPairs = command.getFreeOptionList("xypairs").getString();
        LinkingItemPair pair = null;
        commonItems = new ArrayList<LinkingItemPair>();
        for(String s : xyPairs){
            pair = new LinkingItemPair(s);
            commonItems.add(pair);
        }
        numberOfCommonItems = commonItems.size();

        String xPersonTable = command.getPairedOptionList("xability").getStringAt("table");
        String xPersonTheta = command.getPairedOptionList("xability").getStringAt("theta");
        String xPersonWeight = command.getPairedOptionList("xability").getStringAt("weight");
        if(xPersonTable!=null && !xPersonTable.equals("null")){
            tableNamePersonsX = new DataTableName(xPersonTable);
            thetaNameX = new VariableName(xPersonTheta);
            weightNameX = new VariableName(xPersonWeight);
            if(!xPersonWeight.trim().equals("")){
                hasWeightX = true;
            }
        }

        String yPersonTable = command.getPairedOptionList("yability").getStringAt("table");
        String yPersonTheta = command.getPairedOptionList("yability").getStringAt("theta");
        String yPersonWeight = command.getPairedOptionList("yability").getStringAt("weight");
        if(yPersonTable!=null && !yPersonTable.equals("null")){
            tableNamePersonsY = new DataTableName(yPersonTable);
            thetaNameY = new VariableName(yPersonTheta);
            weightNameY = new VariableName(yPersonWeight);
            if(!yPersonWeight.trim().equals("")){
                hasWeightY = true;
            }
        }


        if( command.getFreeOption("bins").hasValue()){
            bins = command.getFreeOption("bins").getInteger();
        }

        biasSd = command.getSelectOneOption("popsd").isValueSelected("biased");
        String criterion = command.getSelectOneOption("criterion").getSelectedArgument();
        if(criterion.toLowerCase().equals("y")){
            criterionType = EquatingCriterionType.Q1;
        }else if(criterion.toLowerCase().equals("x")){
            criterionType = EquatingCriterionType.Q2;
        }

        precision = command.getFreeOption("precision").getInteger();
        logisticScale = command.getSelectOneOption("scale").isValueSelected("logistic");

        if(command.getSelectOneOption("method").isValueSelected("ms")){
            method = TransformationMethod.MEAN_SIGMA;
        }else if(command.getSelectOneOption("method").isValueSelected("mm")){
            method = TransformationMethod.MEAN_MEAN;
        }else if(command.getSelectOneOption("method").isValueSelected("hb")){
            method = TransformationMethod.HAEBARA;
        }else{
            method = TransformationMethod.STOCKING_LORD;
        }
    }

    protected String doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running IRT Scale Linking...");
        this.firePropertyChange("progress-ind-on", null, null);

        try{
            logger.info(command.paste());
            processCommand();
            publishHeader();
            getItemParameters();
            getThetaDistributions();
            computeCoefficients();

            CommonItemSummaryStatistics itemSummary = new CommonItemSummaryStatistics(irmX, irmY);
            publish(itemSummary.printItemSummary());
            publish(itemSummary.commonItemCorrelations());
            publish(itemSummary.robustZTest());
            publish(irtScaleLinking.toString());

//            printEquatingCoefficients();

            if(command.getSelectAllOption("transform").isArgumentSelected("items")){
                newItemTable = new DataTableName(tableNameItemsX.toString() + "_t");
                transformItems();
            }
            if(command.getSelectAllOption("transform").isArgumentSelected("persons") &&
                    (command.getSelectOneOption("distribution").isValueSelected("observed") ||
                            command.getSelectOneOption("distribution").isValueSelected("histogram"))){
                transformPersons();
            }

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible

        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException = t;
        }
        return timeStamp();

    }

    @Override
    protected void done(){
        try{
            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }else{
                if(newItemTableCreated){
                    this.firePropertyChange("table-added", "", newItemTable);//will add node to tree
                }
                if(newTheta!=null){
                    fireVariableChanged(new VariableChangeEvent(this, tableNamePersonsX, newTheta, VariableChangeType.VARIABLE_ADDED));
                }
            }
            textFile.addText(get());
            textFile.setCaretPosition(0);
        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
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

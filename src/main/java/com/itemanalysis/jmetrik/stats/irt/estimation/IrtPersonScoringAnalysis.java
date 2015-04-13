/*
 * Copyright (c) 2013 Patrick Meyer
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

package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.*;
import com.itemanalysis.psychometrics.distribution.UserSuppliedDistributionApproximation;
import com.itemanalysis.psychometrics.irt.estimation.IrtExaminee;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.statistics.StorelessDescriptiveStatistics;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class IrtPersonScoringAnalysis extends SwingWorker<String, String> {

    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private ArrayList<VariableAttributes> variables = null;
    private IrtPersonScoringCommand command = null;
    private Throwable theException = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private StopWatch sw = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private VariableTableName variableTableName = null;
    private DataTableName ipTable = null;
    private JmetrikTextFile tfa = null;
    private ItemResponseModel[] irm = null;
    private ArrayList<VariableName> selectedItems = null;
    private boolean useMle = false;
    private boolean useMap = false;
    private boolean useEap = false;
    private String nameBase = "";
    private boolean ignoreMissing = true;
    private double minValue = -6.0;
    private double maxValue = 6.0;
    private double priorMean = 0.0;
    private double priorSd = 1.0;
    private double adjust = 0.3;
    private int numPoints = 60;
    private int maxIter = 100;
    private double converge = 1e-5;
    private double maxProgress = 0;
    private VariableAttributes mleVar = null;
    private VariableAttributes mapVar = null;
    private VariableAttributes eapVar = null;
    private VariableAttributes mleVarSe = null;
    private VariableAttributes mapVarSe = null;
    private VariableAttributes eapVarSe = null;
    private StorelessDescriptiveStatistics mleStats = null;
    private StorelessDescriptiveStatistics mapStats = null;
    private StorelessDescriptiveStatistics eapStats = null;
    private int progressValue = 0;
    private int lineNumber = 0;
    private boolean logisticScale = true;

    private UserSuppliedDistributionApproximation distributionApproximation = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public IrtPersonScoringAnalysis(Connection conn, DatabaseAccessObject dao, IrtPersonScoringCommand command, JmetrikTextFile tfa){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.tfa = tfa;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void initializeProgress()throws SQLException {
        int nrow = dao.getRowCount(conn, tableName);
        maxProgress = (double)nrow;
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/ maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    private void processCommand() throws Exception{
        tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
        variableTableName = new VariableTableName(tableName.toString());
        ipTable = new DataTableName(command.getPairedOptionList("iptable").getStringAt("table"));

        if(command.getSelectAllOption("method").isArgumentSelected("mle"))useMle = true;
        if(command.getSelectAllOption("method").isArgumentSelected("map"))useMap = true;
        if(command.getSelectAllOption("method").isArgumentSelected("eap"))useEap = true;

        ArrayList<String> selectVariables = command.getFreeOptionList("variables").getString();
        variables = dao.getSelectedVariables(conn, variableTableName, selectVariables);
        selectedItems = new ArrayList<VariableName>();
        for(VariableAttributes v : variables){
            selectedItems.add(v.getName());
        }

        ignoreMissing = command.getSelectOneOption("missing").isValueSelected("ignore");
        logisticScale = command.getSelectOneOption("scale").isValueSelected("logistic");

        minValue = command.getPairedOptionList("bounds").getDoubleAt("min");
        maxValue = command.getPairedOptionList("bounds").getDoubleAt("max");
        if(useEap) numPoints = command.getFreeOption("numpoints").getInteger();
        if(useEap || useMap){
            priorMean = command.getPairedOptionList("normprior").getDoubleAt("mean");
            priorSd = command.getPairedOptionList("normprior").getDoubleAt("sd");
        }

        maxIter = command.getPairedOptionList("criteria").getIntegerAt("maxiter");
        converge = command.getPairedOptionList("criteria").getDoubleAt("converge");

        nameBase = command.getFreeOption("name").getString();

        if(useEap && command.getPairedOptionList("quad").hasValue()){
            DataTableName quadTable = new DataTableName(command.getPairedOptionList("quad").getStringAt("table"));
            VariableName pName = new VariableName(command.getPairedOptionList("quad").getStringAt("theta"));
            VariableName wName = new VariableName(command.getPairedOptionList("quad").getStringAt("weight"));
            setQuadrature(quadTable, pName, wName);
        }

    }

    private void setQuadrature(DataTableName quadTable, VariableName pName, VariableName wName)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        try{
            int numQuad = dao.getRowCount(conn, quadTable);
            double[] points = new double[numQuad];
            double[] weights = new double[numQuad];

            Table sqlTable = new Table(quadTable.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(sqlTable, pName.nameForDatabase());
            select.addColumn(sqlTable, wName.nameForDatabase());

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            int index = 0;
            while(rs.next()){
                points[index] = rs.getDouble(pName.nameForDatabase());
                weights[index] = rs.getDouble(wName.nameForDatabase());
                index++;
            }

            distributionApproximation = new UserSuppliedDistributionApproximation(points, weights);

        }catch(SQLException ex){
            distributionApproximation = null;
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }
    }

    private void createVariables() throws SQLException {

        int colCount = dao.getColumnCount(conn, tableName);

        if(useMle){
            mleVar = new VariableAttributes(nameBase + "_ml", "MLE Estimate", ItemType.NOT_ITEM, DataType.DOUBLE, colCount++, "");
            dao.addColumnToDb(conn, tableName, mleVar);
            mleVarSe = new VariableAttributes(nameBase + "_mlse", "MLE Estimate standard error", ItemType.NOT_ITEM, DataType.DOUBLE, colCount++, "");
            dao.addColumnToDb(conn, tableName, mleVarSe);
            mleStats = new StorelessDescriptiveStatistics();
        }
        if(useMap){
            mapVar = new VariableAttributes(nameBase + "_mp", "MAP Estimate", ItemType.NOT_ITEM, DataType.DOUBLE, colCount++, "");
            dao.addColumnToDb(conn, tableName, mapVar);
            mapVarSe = new VariableAttributes(nameBase + "_mpse", "MAP Estimate standard error", ItemType.NOT_ITEM, DataType.DOUBLE, colCount++, "");
            dao.addColumnToDb(conn, tableName, mapVarSe);
            mapStats = new StorelessDescriptiveStatistics();
        }
        if(useEap){
            eapVar = new VariableAttributes(nameBase + "_ep", "EAP Estimate", ItemType.NOT_ITEM, DataType.DOUBLE, colCount++, "");
            dao.addColumnToDb(conn, tableName, eapVar);
            eapVarSe = new VariableAttributes(nameBase + "_epse", "EAP Estimate standard error", ItemType.NOT_ITEM, DataType.DOUBLE, colCount++, "");
            dao.addColumnToDb(conn, tableName, eapVarSe);
            eapStats = new StorelessDescriptiveStatistics();
        }

    }

    private void computeScores()throws SQLException{
        this.firePropertyChange("progress-on", null, null);
        initializeProgress();

        Statement stmt = null;
        ResultSet rs=null;

        try{
            conn.setAutoCommit(false);//start transaction

            //connect to db
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(VariableAttributes v : variables){
                select.addColumn(sqlTable, v.getName().nameForDatabase());
            }
            if(useMle){
                select.addColumn(sqlTable, mleVar.getName().nameForDatabase());
                select.addColumn(sqlTable, mleVarSe.getName().nameForDatabase());
            }

            if(useMap){
                select.addColumn(sqlTable, mapVar.getName().nameForDatabase());
                select.addColumn(sqlTable, mapVarSe.getName().nameForDatabase());
            }
            if(useEap){
                select.addColumn(sqlTable, eapVar.getName().nameForDatabase());
                select.addColumn(sqlTable, eapVarSe.getName().nameForDatabase());
            }

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs=stmt.executeQuery(select.toString());

            Object response = null;
            byte responseScore = 0;
            byte[] responseVector = null;
//            int index = 0;
            int col = 0;

            IrtExaminee personScoring = new IrtExaminee(irm);
            int personIndex = 1000;

            while(rs.next()){
                col = 0;
//                index = 0;
//                personScoring.clearResponseVector();
                responseVector = new byte[variables.size()];
                for(VariableAttributes v : variables){//columns in data will be in same order as variables
                    response = rs.getObject(v.getName().nameForDatabase());
                    if((response==null || response.equals("") || response.equals("NA")) && ignoreMissing){
                        responseScore = (byte)-1;
//                        personScoring.increment((byte)-1);//-1 is code for omitted response and missing data
                    }else{
                        responseScore = (byte)v.getItemScoring().computeItemScore(response);
//                        personScoring.increment(responseScore);
                    }
                    responseVector[col] = responseScore;
                    col++;
//                    index++;
                }
                personScoring.setResponseVector(responseVector);

                double score = 0.0;
                double se = 0.0;

                if(useMle){
                    score = personScoring.maximumLikelihoodEstimate(minValue, maxValue);
                    se = personScoring.mleStandardErrorAt(score);

                    if(Double.isNaN(score) || Double.isInfinite(score)){
                        rs.updateNull(mleVar.getName().nameForDatabase());
                        rs.updateNull(mleVarSe.getName().nameForDatabase());
                    }else{
                        rs.updateDouble(mleVar.getName().nameForDatabase(), score);
                        rs.updateDouble(mleVarSe.getName().nameForDatabase(), se);
                        mleStats.increment(score);
                    }

                }

                if(useMap){
                    score = personScoring.mapEstimate(priorMean, priorSd, minValue, maxValue);
                    se = personScoring.mapStandardErrorAt(score);

                    if(Double.isNaN(score) || Double.isInfinite(score)){
                        rs.updateNull(mapVar.getName().nameForDatabase());
                        rs.updateNull(mapVarSe.getName().nameForDatabase());
                    }else{
                        rs.updateDouble(mapVar.getName().nameForDatabase(), score);
                        rs.updateDouble(mapVarSe.getName().nameForDatabase(), se);
                        mapStats.increment(score);
                    }

                }

                if(useEap){
                    if(distributionApproximation==null){
                        score = personScoring.eapEstimate(priorMean, priorSd, minValue, maxValue, numPoints);
                    }else{
                        score = personScoring.eapEstimate(distributionApproximation);
                    }
                    se = personScoring.eapStandardErrorAt(score);

                    if(Double.isNaN(score) || Double.isInfinite(score)){
                        rs.updateNull(eapVar.getName().nameForDatabase());
                        rs.updateNull(eapVarSe.getName().nameForDatabase());
                    }else{
                        rs.updateDouble(eapVar.getName().nameForDatabase(), score);
                        rs.updateDouble(eapVarSe.getName().nameForDatabase(), se);
                        eapStats.increment(score);
                    }

                }

                rs.updateRow();
                updateProgress();
            }

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

    @Override
    protected void process(List<String> chunks){
        for(String s : chunks){
            tfa.append(s + "\n");
        }
    }

    private void setItemParameters()throws SQLException{
        LinkedHashMap<String, ItemResponseModel> temp = dao.getItemParameterSet(conn, ipTable, selectedItems, logisticScale);
        irm = new ItemResponseModel[temp.size()];
        int index = 0;
        for(String s : temp.keySet()){
            irm[index] = temp.get(s);
            index++;
        }
    }

    private void publishHeader()throws IllegalArgumentException{
        StringBuilder header = new StringBuilder();
        Formatter f = new Formatter(header);
        String s1 = String.format("%1$tB %1$te, %1$tY  %tT", Calendar.getInstance());
        int len = 21+Double.valueOf(Math.floor(Double.valueOf(s1.length()).doubleValue()/2.0)).intValue();
        String dString = "";
        try{
            dString = command.getDataString();
        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
        int len2 = 21+Double.valueOf(Math.floor(Double.valueOf(dString.length()).doubleValue()/2.0)).intValue();

        f.format("%30s", "IRT PERSON SCORING"); f.format("%n");
        f.format("%" + len2 + "s", dString); f.format("%n");
        f.format("%" + len + "s", s1); f.format("%n");
        f.format("%n");
        publish(f.toString());
    }


    @Override
    public String doInBackground(){
        String s = "";
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running IRT Person Scoring...");
        this.firePropertyChange("progress-ind-on", null, null);
        logger.info(command.paste());

        try{
            processCommand();
            setItemParameters();
            createVariables();
            computeScores();

            publishHeader();
            s += "\n";
            if(useMle) s+= mleStats.toString(mleVar.getName().toString()) + "\n\n";
            if(useMap) s+= mapStats.toString(mapVar.getName().toString()) + "\n\n";
            if(useEap) s+= eapStats.toString(eapVar.getName().toString()) + "\n\n";

        }catch(Throwable t){
            theException=t;
        }

        firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
        firePropertyChange("progress-off", null, null); //make statusbar progress not visible
        return s;
    }

    @Override
    public void done(){
        try{

            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }else{
                tfa.addText(get());
                tfa.addText("Elapsed time: " + sw.getElapsedTime());
                tfa.setCaretPosition(0);

                if(mleVar!=null){
                    fireVariableChanged(new VariableChangeEvent(this, tableName, mleVar, VariableChangeType.VARIABLE_ADDED));
                    fireVariableChanged(new VariableChangeEvent(this, tableName, mleVarSe, VariableChangeType.VARIABLE_ADDED));
                }
                if(mapVar!=null){
                    fireVariableChanged(new VariableChangeEvent(this, tableName, mapVar, VariableChangeType.VARIABLE_ADDED));
                    fireVariableChanged(new VariableChangeEvent(this, tableName, mapVarSe, VariableChangeType.VARIABLE_ADDED));
                }
                if(eapVar!=null){
                    fireVariableChanged(new VariableChangeEvent(this, tableName, eapVar, VariableChangeType.VARIABLE_ADDED));
                    fireVariableChanged(new VariableChangeEvent(this, tableName, eapVarSe, VariableChangeType.VARIABLE_ADDED));
                }

            }



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
    //===============================================================================================================


}

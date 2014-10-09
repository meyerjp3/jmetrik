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

package com.itemanalysis.jmetrik.stats.transformation;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.scaling.DefaultLinearTransformation;
import com.itemanalysis.psychometrics.scaling.LinearTransformation;
import com.itemanalysis.psychometrics.scaling.ScoreBounds;
import com.itemanalysis.psychometrics.statistics.StorelessDescriptiveStatistics;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;

public class LinearTransformationAnalysis  extends SwingWorker<String, Void> {

    private LinearTransformationCommand command = null;
    private JmetrikTextFile textFile = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private Throwable theException = null;
    private StopWatch sw = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private VariableInfo selectedVariable = null;
    private VariableInfo addedVariableInfo = null;
    private int lineNumber=0;
    private int progressValue=0;
    private int columnNumber = 0;
    private double maxProgress=100.0;
    private int precision = 15;
    private ScoreBounds scaleScoreBounds = null;
    private double scaleMean = 0.0;
    private double scaleSd = 1.0;
    private double minPossibleScore = Double.NEGATIVE_INFINITY;
    private double maxPossibleScore = Double.POSITIVE_INFINITY;
    private boolean type1 = true;
    private StorelessDescriptiveStatistics descriptiveStatistics = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public LinearTransformationAnalysis(Connection conn, DatabaseAccessObject dao, LinearTransformationCommand command, JmetrikTextFile textFile){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.textFile = textFile;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
        descriptiveStatistics = new StorelessDescriptiveStatistics();
    }

    private void initializeProgress()throws SQLException{
        int nrow = dao.getRowCount(conn, tableName);
        maxProgress=2.0*(double)nrow;
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    private void processCommand()throws IllegalArgumentException, SQLException{
        precision = command.getPairedOptionList("constraints").getIntegerAt("precision").intValue();
        minPossibleScore = command.getPairedOptionList("constraints").getDoubleAt("min");
        maxPossibleScore = command.getPairedOptionList("constraints").getDoubleAt("max");

        if(command.getPairedOptionList("transform").hasAllArguments()){
            scaleMean = command.getPairedOptionList("transform").getDoubleAt("intercept");
            scaleSd = command.getPairedOptionList("transform").getDoubleAt("slope");
            type1 = true;
        }else{
            type1 = false;
        }

        int numberOfColumns = dao.getColumnCount(conn, tableName);
        columnNumber = numberOfColumns+1;

        String newVariableName = command.getFreeOption("name").getString();
        addedVariableInfo = new VariableInfo(
                newVariableName,
                "Linear transformation of " + selectedVariable.getName().toString(),
                VariableType.NOT_ITEM,
                VariableType.DOUBLE,
                columnNumber,
                "");

        initializeProgress();

    }

    public String transformScore()throws SQLException {
        Statement stmt = null;
        ResultSet rs=null;
        Double constrainedScore = null;

        try{
            //add variable to db
            dao.addColumnToDb(conn, tableName, addedVariableInfo);

            conn.setAutoCommit(false);//begin transaction

            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(sqlTable, selectedVariable.getName().nameForDatabase());
            select.addColumn(sqlTable, addedVariableInfo.getName().nameForDatabase());
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs=stmt.executeQuery(select.toString());

            this.firePropertyChange("message", "", "Transforming scores...");

            double origValue = 0.0;
            double transValue = 0.0;
            double z = 0.0;

            StandardDeviation sd = new StandardDeviation();
            Mean mean = new Mean();
            Min min = new Min();
            Max max = new Max();

            while(rs.next()){
                origValue = rs.getDouble(selectedVariable.getName().nameForDatabase());
                if(!rs.wasNull()){
                    sd.increment(origValue);
                    mean.increment(origValue);
                    min.increment(origValue);
                    max.increment(origValue);
                }
                updateProgress();
            }

            double meanValue = mean.getResult();
            double sdValue = sd.getResult();
            double minValue = min.getResult();
            double maxValue = max.getResult();
            double A = 1.0;
            double B = 0.0;

            rs.beforeFirst();

            while(rs.next()){
                origValue = rs.getDouble(selectedVariable.getName().nameForDatabase());
                if(!rs.wasNull()){
                    if(type1){
                        z = (origValue - meanValue)/sdValue;
                        transValue = scaleSd*z + scaleMean;
                        transValue = checkConstraints(transValue);
                    }else{
                        A = (maxPossibleScore-minPossibleScore)/(maxValue-minValue);
                        B = minPossibleScore - minValue*A;
                        transValue = origValue*A + B;
                        transValue = checkConstraints(transValue);
                    }

                    descriptiveStatistics.increment(transValue);

                    rs.updateDouble(addedVariableInfo.getName().nameForDatabase(), transValue);
                    rs.updateRow();
                }
                updateProgress();
            }

            conn.commit();
            conn.setAutoCommit(true);

            //create output
            DefaultLinearTransformation linearTransformation = new DefaultLinearTransformation();
            linearTransformation.setScale(A);
            linearTransformation.setIntercept(B);

            StringBuilder sb = new StringBuilder();
            Formatter f = new Formatter(sb);
            f.format(publishHeader());
            f.format(descriptiveStatistics.toString());
            f.format(linearTransformation.toString());
            f.format("%n");
            f.format("%n");
            return f.toString();

        }catch(SQLException ex){
            conn.rollback();
            conn.setAutoCommit(true);
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    private String publishHeader(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        String dString = "";
        String title = "LINEAR TRANSFORMATION";

        try{
            dString = command.getDataString();
        }catch(IllegalArgumentException ex){
            dString = "";
            throw new IllegalArgumentException(ex);
        }

        //create header
        int len1 = 25+Double.valueOf(Math.floor(Double.valueOf(title.length()).doubleValue()/2.0)).intValue();
        int len2 = 25+Double.valueOf(Math.floor(Double.valueOf(dString.length()).doubleValue()/2.0)).intValue();

        String date = String.format("%1$tB %1$te, %1$tY  %tT", Calendar.getInstance());
        int len3 = 25+Double.valueOf(Math.floor(Double.valueOf(date.length()).doubleValue()/2.0)).intValue();

        f.format("%" + len1 + "s", title); f.format("%n");
        f.format("%" + len2 + "s", dString); f.format("%n");
        f.format("%" + len3 + "s", date); f.format("%n");
        f.format("%n");
        f.format("%n");
        f.format("%n");

        return f.toString();
    }

    private Double checkConstraints(Double value){
        if(value<minPossibleScore){
            return Precision.round(minPossibleScore, precision);
        }else if(value>maxPossibleScore){
            return Precision.round(maxPossibleScore, precision);
        }else{
            return Precision.round(value, precision);
        }
    }

    @Override
    protected String doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Test Scaling...");
        this.firePropertyChange("progress-on", null, null);
        String results = "";
        try{
            logger.info(command.paste());
            //get variable info from db
            dbName = new DatabaseName(command.getPairedOptionList("data").getStringAt("db"));
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            VariableTableName variableTableName = new VariableTableName(tableName.toString());
            ArrayList<String> selectVariables = command.getFreeOptionList("variables").getString();
            ArrayList<VariableInfo> variables = dao.getSelectedVariables(conn, variableTableName, selectVariables);
            selectedVariable = variables.get(0);

            processCommand();
            results = transformScore();

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible

        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return results;
    }

    @Override
    protected void done(){
        try{
            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }else{
                textFile.addText(get());
                textFile.addText("Elapsed time: " + sw.getElapsedTime());
                textFile.setCaretPosition(0);
                fireVariableChanged(new VariableChangeEvent(this, tableName, addedVariableInfo, VariableChangeType.VARIABLE_ADDED));
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

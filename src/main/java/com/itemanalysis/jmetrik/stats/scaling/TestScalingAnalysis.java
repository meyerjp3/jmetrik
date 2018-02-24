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

package com.itemanalysis.jmetrik.stats.scaling;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import javax.swing.SwingWorker;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.polycor.CovarianceMatrix;
import com.itemanalysis.psychometrics.reliability.CoefficientAlpha;
import com.itemanalysis.psychometrics.scaling.*;
import com.itemanalysis.psychometrics.statistics.StorelessDescriptiveStatistics;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;

public class TestScalingAnalysis  extends SwingWorker<String, Void> {

    private TestScalingCommand command = null;
    private JmetrikTextFile textFile = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");
    private ArrayList<VariableAttributes> variables = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private DataTableName tableName = null;
    private Throwable theException = null;
    private StopWatch sw = null;
    private VariableAttributes addedVariableInfo = null;
    private ScoreBounds sumScoreBounds = null;
    private ScoreBounds scaleScoreBounds = null;
    private int precision = 2;
    private Double scaleMean = null;
    private Double scaleSd = null;
    private boolean rescale = false;
    private int lineNumber=0;
    private int progressValue=0;
    private double maxProgress=100.0;
    private String newVariableName = "";
    private int columnNumber = 0;
    private StorelessDescriptiveStatistics rawScoreDescriptives = null;
    private StorelessDescriptiveStatistics scaleScoreDescriptives = null;
    private LinearTransformation linearTransformation = null;
    private String title = "SUM SCORES";

    private int nrow = 0;
    private static int SUM_SCORE = 1;
    private static int MEAN_SCORE = 2;
    private static int PERCENTILE_RANK = 3;
    private static int KELLEY_SCORE = 4;
    private static int NORMALIZED_SCORE = 5;
    private int scoreType = SUM_SCORE;

    public TestScalingAnalysis(Connection conn, DatabaseAccessObject dao, TestScalingCommand command, JmetrikTextFile textFile){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.textFile = textFile;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void initializeProgress()throws SQLException{
        nrow = dao.getRowCount(conn, tableName);
        if(scoreType==PERCENTILE_RANK || scoreType==NORMALIZED_SCORE || scoreType==KELLEY_SCORE){
            maxProgress = 3.0*(double)nrow;//will loop over database three times
        }else{
            //sum score and mean score, not rescaled
            maxProgress=(double)nrow*2.0;
        }
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    private double[] computeRawScore(boolean meanScore)throws SQLException{
        this.firePropertyChange("message", "", "Computing sum scores...");
        Statement stmt = null;
        ResultSet rs=null;
        int numberOfItems = variables.size();
        double validItems = 0.0;
        double tempSum = 0.0;
        double[] sumScore = new double[nrow];
        rawScoreDescriptives = new StorelessDescriptiveStatistics();

        try{
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(VariableAttributes v : variables){
                select.addColumn(sqlTable, v.getName().nameForDatabase());
            }
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            int index = 0;
            double responseScore = 0.0;

            while(rs.next()){
                Object response = null;

                //loop over items to compute RawScore
                tempSum = 0.0;
                validItems = 0.0;
                for(VariableAttributes v : variables){
                    response = rs.getObject(v.getName().nameForDatabase());
                    responseScore = v.getItemScoring().computeItemScore(response);
                    if(!Double.isNaN(responseScore)){
                        tempSum += responseScore;
                        validItems++;
                    }

                }
                if(meanScore) tempSum /= validItems;
                sumScore[index] = tempSum;
                rawScoreDescriptives.increment(tempSum);
                index++;
                updateProgress();
            }

            return sumScore;

        }catch(SQLException ex){
            throw ex;
        }finally{
            stmt.close();
            rs.close();
        }

    }

    public String addRawScore(boolean meanScore)throws SQLException{
        Statement stmt = null;
        ResultSet rs=null;
        scaleScoreDescriptives = new StorelessDescriptiveStatistics();

        try{
            conn.setAutoCommit(false);
            double[] testScore = computeRawScore(meanScore);
            double transformedScore = 0.0;

            //apply linear transformation and score constraints
            if(rescale){
                linearTransformation = new DefaultLinearTransformation(
                        rawScoreDescriptives.getMean(),
                        scaleMean,
                        rawScoreDescriptives.getStandardDeviation(),
                        scaleSd);
            }else{
                linearTransformation = new DefaultLinearTransformation();
            }

            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(sqlTable, addedVariableInfo.getName().nameForDatabase());
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs=stmt.executeQuery(select.toString());

            //add score to database
            int index = 0;
            double tempScore = 0.0;
            while(rs.next()){
                tempScore = linearTransformation.transform(testScore[index]);
                tempScore = scaleScoreBounds.checkConstraints(tempScore);
                scaleScoreDescriptives.increment(tempScore);
                rs.updateDouble(
                        addedVariableInfo.getName().nameForDatabase(),
                        tempScore);
                rs.updateRow();
                updateProgress();
                index++;
            }

            //produce output
            StringBuilder sb = new StringBuilder();
            Formatter f = new Formatter(sb);
            f.format(publishHeader());

            if(scoreType==MEAN_SCORE){
                f.format(rawScoreDescriptives.toString("Mean Score Descriptive Statistics"));f.format("%n");
            }else{
                f.format(rawScoreDescriptives.toString("Sum Score Descriptive Statistics"));f.format("%n");
            }
            f.format("%n");f.format("%n");
            if(rescale){
                f.format(scaleScoreDescriptives.toString("Scale Score Descriptive Statistics"));
                f.format(linearTransformation.toString());f.format("%n");
                f.format("%n");
                f.format("%n");
            }

            return f.toString();

        }catch(SQLException ex){
            conn.rollback();
            throw ex;
        }finally{
            conn.commit();
            conn.setAutoCommit(true);
            if(stmt!=null) stmt.close();
            if(rs!=null) rs.close();
        }

    }

    public String computeKelleyScore()throws SQLException{

        this.firePropertyChange("message", "", "Computing Kelley scores...");
        Statement stmt = null;
        ResultSet rs=null;
        int numberOfItems = variables.size();
        double tempSum = 0.0;
        double[] sumScore = new double[nrow];
        CovarianceMatrix matrix = new CovarianceMatrix(variables);
        rawScoreDescriptives = new StorelessDescriptiveStatistics();
        scaleScoreDescriptives = new StorelessDescriptiveStatistics();

        try{
            conn.setAutoCommit(false);

            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(VariableAttributes v : variables){
                select.addColumn(sqlTable, v.getName().nameForDatabase());
            }
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            int personIndex = 0;
            int xIndex = 0;
            int yIndex = 0;
            double responseScore = 0.0;
            double responseScore2 = 0.0;
            double[] responseVector = new double[numberOfItems];

            Object response = null;
            Object response2 = null;

            //first loop computes sum score and covariance
            while(rs.next()){
                //loop over items to compute RawScore
                tempSum = 0.0;

                //compute sum score and create response vector
                xIndex = 0;
                for(VariableAttributes v : variables){
                    response = rs.getObject(v.getName().nameForDatabase());
                    responseScore = v.getItemScoring().computeItemScore(response);
                    responseVector[xIndex] = responseScore;
                    tempSum += responseScore;
                    xIndex++;
                }

                //increment covariance matrix for reliability computation
                for(int i=0;i<numberOfItems;i++){
                    for(int j=0;j<numberOfItems;j++){
                        matrix.increment(i, j, responseVector[i], responseVector[j]);
                    }
                }

                sumScore[personIndex] = tempSum;
                rawScoreDescriptives.increment(tempSum);
                personIndex++;
                updateProgress();
            }

            //compute reliability estimate
            CoefficientAlpha alpha = new CoefficientAlpha(matrix);
            KelleyRegressedScore kelley = new KelleyRegressedScore(rawScoreDescriptives.getMean(), alpha);

            //close statement and result set from first pass
            stmt.close();
            rs.close();

            select = new SelectQuery();
            select.addColumn(sqlTable, addedVariableInfo.getName().nameForDatabase());
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs=stmt.executeQuery(select.toString());

            //compute kelley score for obtaining descriptive statistics needed for linear transformation
            personIndex = 0;
            double tempScore = 0.0;
            while(rs.next()){
                tempScore = kelley.value(sumScore[personIndex]);
                scaleScoreDescriptives.increment(tempScore);
                personIndex++;
                updateProgress();
            }

            if(rescale){
                linearTransformation = new DefaultLinearTransformation(scaleScoreDescriptives.getMean(), scaleMean, scaleScoreDescriptives.getStandardDeviation(), scaleSd);
            }else{
                linearTransformation = new DefaultLinearTransformation();
            }

            //compute kelley score, apply linear transformation and constraints, add to database
            rs.beforeFirst();
            personIndex = 0;
            tempScore = 0.0;
            scaleScoreDescriptives.clear();
            while(rs.next()){
                tempScore = kelley.value(sumScore[personIndex], linearTransformation);
                tempScore = scaleScoreBounds.checkConstraints(tempScore);
                scaleScoreDescriptives.increment(tempScore);
                if(!Double.isNaN(tempScore)){
                    rs.updateDouble(addedVariableInfo.getName().nameForDatabase(), tempScore);
                    rs.updateRow();
                }
                personIndex++;
                updateProgress();
            }


            //create output
            ScoreTable table = new ScoreTable(sumScoreBounds, "Kelley", precision);
            table.kelleyScoreTable(kelley, linearTransformation, scaleScoreBounds, rescale);
            StringBuilder sb = new StringBuilder();
            Formatter f = new Formatter(sb);
            f.format(publishHeader());

            f.format("%-20s", "Coefficient alpha = " ); f.format("%1.2f", + alpha.value());
            f.format("%n");
            f.format("%n");
            f.format("%n");
            f.format(rawScoreDescriptives.toString("Sum Score Descriptive Statistics"));f.format("%n");
            f.format("%n");
            f.format("%n");
            f.format("%n");

            f.format(scaleScoreDescriptives.toString("Kelley Score Descriptive Statistics"));
            f.format("%n");
            f.format("%n");
            f.format("%n");

            f.format(table.toString());
            if(rescale){
                f.format(linearTransformation.toString());
                f.format("%n");
                f.format("%n");
            }

            return f.toString();


        }catch(SQLException ex){
            conn.rollback();
            throw ex;
        }finally{
            conn.commit();
            conn.setAutoCommit(true);
            if(stmt!=null) stmt.close();
            if(rs!=null) rs.close();
        }

    }


    public String computePercentileRankOrNormalizedScore()throws SQLException{
        if(scoreType==PERCENTILE_RANK){
            this.firePropertyChange("message", "", "Computing percentile ranks...");
        }else{
            this.firePropertyChange("message", "", "Computing normalized scores...");
        }

        Statement stmt = null;
        ResultSet rs=null;
        int numberOfItems = variables.size();
        scaleScoreDescriptives = new StorelessDescriptiveStatistics();

        PercentileRank percentileRank = new PercentileRank(
                Double.valueOf(Math.floor(sumScoreBounds.getMinPossibleScore()+0.5)).intValue(),
                Double.valueOf(Math.floor(sumScoreBounds.getMaxPossibleScore()+0.5)).intValue()
        );

        NormalizedScore normalizedScore = null;

        try{
            double[] rawScore = computeRawScore(false);

            for(int i=0;i<rawScore.length;i++){
                percentileRank.addValue(rawScore[i]);
                updateProgress();
            }

            //create percentile rank lookup table
            percentileRank.createLookupTable();

            conn.setAutoCommit(false);

            //update database
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(sqlTable, addedVariableInfo.getName().nameForDatabase());
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs=stmt.executeQuery(select.toString());

            int index = 0;
            double tempScore = 0.0;
            if(scoreType==PERCENTILE_RANK){
                while(rs.next()){
                    tempScore = percentileRank.getPercentileRankAt(Double.valueOf(rawScore[index]).intValue());
                    tempScore = scaleScoreBounds.checkPrecisionOnly(tempScore);
                    scaleScoreDescriptives.increment(tempScore);
                    rs.updateDouble(
                            addedVariableInfo.getName().nameForDatabase(),
                            tempScore);
                    rs.updateRow();
                    updateProgress();
                    index++;
                }
            }else if(scoreType==NORMALIZED_SCORE){
                if(rescale){
                    linearTransformation = new DefaultLinearTransformation(0.0, scaleMean, 1.0, scaleSd);
                }else{
                    linearTransformation = new DefaultLinearTransformation();
                }

                //compute normalized scores
                normalizedScore = new NormalizedScore();
                normalizedScore.createLookupTable(percentileRank, linearTransformation);
                scaleScoreDescriptives.clear();

                while(rs.next()){
                    tempScore = normalizedScore.getNormalizedScoreAt(rawScore[index]);
                    tempScore = scaleScoreBounds.checkConstraints(tempScore);
                    scaleScoreDescriptives.increment(tempScore);
                    rs.updateDouble(
                            addedVariableInfo.getName().nameForDatabase(),
                            tempScore);
                    rs.updateRow();
                    updateProgress();
                    index++;
                }
            }

            //produce output
            StringBuilder sb = new StringBuilder();
            Formatter f = new Formatter(sb);
            f.format(publishHeader());

            f.format(rawScoreDescriptives.toString("Sum Score Descriptive Statistics"));f.format("%n");
            f.format("%n");f.format("%n");

            if(scoreType==PERCENTILE_RANK){
                f.format(scaleScoreDescriptives.toString("Percentile Rank Descriptives"));f.format("%n");f.format("%n");f.format("%n");
            }else{
                f.format(scaleScoreDescriptives.toString("Normalized Score Descriptives"));f.format("%n");f.format("%n");f.format("%n");
            }

            if(scoreType==PERCENTILE_RANK){
                f.format(percentileRank.toString());
            }else{
                f.format(normalizedScore.printTable(precision));
            }
            if(rescale && scoreType!=PERCENTILE_RANK){
                f.format(linearTransformation.toString());
            }
            f.format("%n");
            f.format("%n");

            return f.toString();

        }catch(SQLException ex){
            conn.rollback();
            throw ex;
        }finally{
            conn.commit();
            conn.setAutoCommit(true);
            if(stmt!=null) stmt.close();
            if(rs!=null) rs.close();
        }

    }

    private String publishHeader(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        String dString = "";

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

    public String getResults()throws SQLException{
        if(scoreType==PERCENTILE_RANK){
            addedVariableInfo = new VariableAttributes(newVariableName, "Percentile Rank", ItemType.NOT_ITEM, DataType.DOUBLE, columnNumber++, "");
            dao.addColumnToDb(conn, tableName,  addedVariableInfo);
            title = "TEST SCALING: PERCENTILE RANKS";
            return computePercentileRankOrNormalizedScore();
        }else if(scoreType==KELLEY_SCORE){
            addedVariableInfo = new VariableAttributes(newVariableName, "Kelley Regressed Score", ItemType.NOT_ITEM, DataType.DOUBLE, columnNumber++, "");
            dao.addColumnToDb(conn, tableName,  addedVariableInfo);
            title = "TEST SCALING: KELLEY SCORES";
            return computeKelleyScore();
        }else if(scoreType==NORMALIZED_SCORE){
            addedVariableInfo = new VariableAttributes(newVariableName, "Normalized Score", ItemType.NOT_ITEM, DataType.DOUBLE, columnNumber++, "");
            dao.addColumnToDb(conn, tableName,  addedVariableInfo);
            title = "TEST SCALING: NORMALIZED SCORES";
            return computePercentileRankOrNormalizedScore();
        }else if(scoreType==MEAN_SCORE){
            addedVariableInfo = new VariableAttributes(newVariableName, "Average Score", ItemType.NOT_ITEM, DataType.DOUBLE, columnNumber++, "");
            dao.addColumnToDb(conn, tableName,  addedVariableInfo);
            title = "TEST SCALING: MEAN SCORES";
            return addRawScore(true);
        }else{
            addedVariableInfo = new VariableAttributes(newVariableName, "Sum Score", ItemType.NOT_ITEM, DataType.DOUBLE, columnNumber++, "");
            dao.addColumnToDb(conn, tableName,  addedVariableInfo);
            title = "TEST SCALING: SUM SCORES";
            return addRawScore(false);
        }
    }

    private void processCommand()throws IllegalArgumentException, SQLException{
        scaleScoreBounds = new ScoreBounds(
                command.getPairedOptionList("constraints").getIntegerAt("precision").intValue());
        scaleScoreBounds.setConstraints(
                command.getPairedOptionList("constraints").getDoubleAt("min"),
                command.getPairedOptionList("constraints").getDoubleAt("max"));

        if(command.getPairedOptionList("transform").getDoubleAt("mean")!=null &&
                command.getPairedOptionList("transform").getDoubleAt("sd")!=null){
            scaleMean = command.getPairedOptionList("transform").getDoubleAt("mean");
            scaleSd = command.getPairedOptionList("transform").getDoubleAt("sd");
            rescale = true;
        }

        if(command.getSelectOneOption("score").isValueSelected("prank")){
            scoreType = PERCENTILE_RANK;
        }else if(command.getSelectOneOption("score").isValueSelected("kelley")){
            scoreType = KELLEY_SCORE;
        }else if(command.getSelectOneOption("score").isValueSelected("normal")){
            scoreType = NORMALIZED_SCORE;
        }else if(command.getSelectOneOption("score").isValueSelected("mean")){
            scoreType = MEAN_SCORE;
        }else{
            scoreType = SUM_SCORE;
        }

        int numberOfColumns = dao.getColumnCount(conn, tableName);
        columnNumber = numberOfColumns+1;

        precision = command.getPairedOptionList("constraints").getIntegerAt("precision").intValue();
        newVariableName = command.getFreeOption("name").getString();

        initializeProgress();

    }

    protected String doInBackground() {
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Test Scaling...");
        this.firePropertyChange("progress-on", null, null);
        String results = "";
        try{
            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            VariableTableName variableTableName = new VariableTableName(tableName.toString());
            ArrayList<String> selectVariables = command.getFreeOptionList("variables").getString();
            variables = dao.getSelectedVariables(conn, variableTableName, selectVariables);

            sumScoreBounds = new ScoreBounds(variables, 6);
            processCommand();
            results = getResults();

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
                scriptLogger.info(command.paste());
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

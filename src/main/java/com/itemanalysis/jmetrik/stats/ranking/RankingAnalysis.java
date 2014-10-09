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

package com.itemanalysis.jmetrik.stats.ranking;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeSet;
import javax.swing.SwingWorker;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.statistics.NormalScores;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.stat.ranking.NaNStrategy;
import org.apache.commons.math3.stat.ranking.NaturalRanking;
import org.apache.commons.math3.stat.ranking.TiesStrategy;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.apache.log4j.Logger;

public class RankingAnalysis extends SwingWorker<String, Void> {

    private JmetrikTextFile textFile = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private RankingCommand command = null;
    private VariableInfo variable = null;
    private DataTableName tableName = null;
    private Throwable theException = null;
    private StopWatch sw = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private TiesStrategy tiesStrategy = TiesStrategy.MAXIMUM;
    private boolean ascending = true;
    private VariableInfo newVariable = null;
    private String newVariableType = "rank";
    private NormalScores normScore = null;
    private int numGroups = 0;
    private String newVariableName = "";

    /**
     * TreeSet to keep track of database records that have missing data.
     * Will use this to insert null values into db.
     */
    private TreeSet<Integer> missingIndex = null;

    private int lineNumber=0;
    private int progressValue=0;
    private double maxProgress=0.0;
    private boolean blom = false;
    private boolean tukey = false;
    private boolean vdw = false;
    private boolean rank = false;
    private boolean ntiles = false;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public RankingAnalysis(Connection conn, DatabaseAccessObject dao, RankingCommand command, JmetrikTextFile textFile){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.textFile = textFile;
        missingIndex = new TreeSet<Integer>();
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void initializeProgress()throws SQLException {
        int nrow = dao.getRowCount(conn, tableName);
        maxProgress = (double)nrow*2.0; //will loop over db twice
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    private ResizableDoubleArray getData()throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        ResizableDoubleArray data = new ResizableDoubleArray((int)(maxProgress/2.0));

        try{
            //connect to table to create data set to be ranked
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(sqlTable, variable.getName().nameForDatabase());
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            String vNameDb = variable.getName().nameForDatabase();

            double x = Double.NaN;
            int dbIndex = 0;//row position index for all records in db
            while(rs.next()){
                x = rs.getDouble(vNameDb);
                if(!rs.wasNull()){
                    if(ascending){
                        data.addElement(x);//ascending order
                    }else{
                        data.addElement(-x);//descending order
                    }

                }else{
                    missingIndex.add(dbIndex);
                }
                dbIndex++;
                updateProgress();
            }
            return data;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();

        }
    }

    public String compute()throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        try{
            //get data
            ResizableDoubleArray data = getData();

            //create columns - dao uses its own transaction
            int numberOfColumns = dao.getColumnCount(conn, tableName);
            int columnNumber = numberOfColumns+1;
            String newVariableLabel = "Rank";
            if(blom) newVariableLabel = "Blom Normal Score";
            if(tukey) newVariableLabel = "Tukey Normal Score";
            if(vdw) newVariableLabel = "van der Waerden Normal Score";
            if(ntiles) newVariableLabel = "Quantiles: " + numGroups + " groups";
            newVariable = new VariableInfo(newVariableName, newVariableLabel, VariableType.NOT_ITEM, VariableType.DOUBLE, columnNumber++, "");

            dao.addColumnToDb(conn, tableName, newVariable);

            //compute ranks
            NaturalRanking ranking = new NaturalRanking(NaNStrategy.REMOVED, tiesStrategy);
            double[] ranks = ranking.rank(data.getElements());

            //begin transaction
            conn.setAutoCommit(false);

            //connect to table and update values
            SelectQuery select = new SelectQuery();
            Table sqlTable = new Table(tableName.getNameForDatabase());
            select.addColumn(sqlTable, newVariable.getName().nameForDatabase());
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs=stmt.executeQuery(select.toString());

            int nRanks = ranks.length;
            int rankIndex = 0;//array index for ranks (missing values not included)
            int dbIndex = 0;//db row position for case (missing values included)
            double r = Double.NaN;
            boolean missing = false;
            String tempName = "";
            while(rs.next()){
                missing = missingIndex.contains(dbIndex);
                if(missing){
                    rs.updateNull(newVariable.getName().nameForDatabase());
                }else{
                    r = ranks[rankIndex];
                    if(blom){
                        rs.updateDouble(newVariable.getName().nameForDatabase(), normScore.blom(r, (double)nRanks));
                    }else if (tukey){
                        rs.updateDouble(newVariable.getName().nameForDatabase(), normScore.tukey(r, (double)nRanks));
                    }else if(vdw){
                        rs.updateDouble(newVariable.getName().nameForDatabase(), normScore.vanderWaerden(r, (double)nRanks));
                    }else if(ntiles){
                        rs.updateDouble(newVariable.getName().nameForDatabase(), getGroup(r, (double)nRanks, (double)numGroups));
                    }else{
                        rs.updateDouble(newVariable.getName().nameForDatabase(), r);
                    }
                    rankIndex++;
                }
                rs.updateRow();
                updateProgress();
                dbIndex++;
            }
            conn.commit();
            return "Ranks computed";
        }catch(SQLException ex){
            conn.rollback();
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
            conn.setAutoCommit(true);
        }

    }

    /**
     * From SAS documentation of PROC RANK statement. Returns a group value
     * ranging from 0 to numGroups - 1.
     *
     * @param rank rank of case.
     * @param n number of non-missing cases.
     * @param numGroups number of groups.
     * @return group for case.
     */
    public double getGroup(double rank, double n, double numGroups){
        return Math.floor(rank*numGroups/(n+1.0));
    }

    /**
     * Adds columns to database for storing ranks, ntiles, and normal scores.
     * A prefix is added to the variable name to indicate the type of variable.
     *
     * These prefixes are used to update the proper variables in the database (see compute()).
     *
     * @throws SQLException
     * @throws IllegalArgumentException
     */
//    public void addColumnsToDb()throws SQLException, IllegalArgumentException{
//
//        int numberOfColumns = dao.getColumnCount(conn, tableName);
//        int columnNumber = numberOfColumns+1;
//
//        String newVariableLabel = "Rank";
//        if(blom) newVariableLabel = "Blom Normal Score";
//        if(tukey) newVariableLabel = "Tukey Normal Score";
//        if(vdw) newVariableLabel = "van der Waerden Normal Score";
//        if(ntiles) newVariableLabel = "Quantiles: " + numGroups + " groups";
//
//        newVariable = new VariableInfo(newVariableName, newVariableLabel, VariableType.NOT_ITEM, VariableType.DOUBLE, columnNumber++, "");
//        dao.addColumnToDb(conn, tableName, newVariable);
//
//    }

    protected String doInBackground() {
        sw = new StopWatch();
        logger.info(command.paste());

        this.firePropertyChange("status", "", "Running Ranking...");
        this.firePropertyChange("progress-on", null, null);
        String results = "";
        try{

            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            VariableTableName variableTableName = new VariableTableName(tableName.toString());
            String selectVariable = command.getFreeOption("variable").getString();
            variable = dao.getVariableInfo(conn, variableTableName, selectVariable);

            newVariableName = command.getFreeOption("name").getString();

            initializeProgress();

            String ties = command.getSelectOneOption("ties").getSelectedArgument();
            if(ties.equals("sequential")){
                tiesStrategy = TiesStrategy.SEQUENTIAL;
            }else if(ties.equals("min")){
                tiesStrategy = TiesStrategy.MINIMUM;
            }else if(ties.equals("max")){
                tiesStrategy = TiesStrategy.MAXIMUM;
            }else if(ties.equals("average")){
                tiesStrategy = TiesStrategy.AVERAGE;
            }else if(ties.equals("random")){
                tiesStrategy = TiesStrategy.RANDOM;
            }

            String type = command.getSelectOneOption("type").getSelectedArgument();
            if("blom".equals(type)){
                blom = true;
            }else if("tukey".equals(type)){
                tukey = true;
            }else if("vdw".equals(type)){
                vdw = true;
            }else if("ntiles".equals(type)){
                ntiles = true;
                if(command.getFreeOption("ntiles").hasValue()){
                    numGroups = command.getFreeOption("ntiles").getInteger();
                }else{
                    rank = true;
                }
            }else{
                rank = true;
            }

            if(blom || tukey || vdw) normScore = new NormalScores();
            ascending = command.getSelectOneOption("order").isValueSelected("asc");

//            addColumnsToDb();
            results = compute();



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

                fireVariableChanged(new VariableChangeEvent(this, tableName, newVariable, VariableChangeType.VARIABLE_ADDED));

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

}

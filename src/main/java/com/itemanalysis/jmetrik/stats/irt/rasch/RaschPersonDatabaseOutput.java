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

package com.itemanalysis.jmetrik.stats.irt.rasch;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.irt.estimation.JointMaximumLikelihoodEstimation;
import com.itemanalysis.psychometrics.irt.estimation.RaschFitStatistics;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class RaschPersonDatabaseOutput {

    private JointMaximumLikelihoodEstimation jmle = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private ArrayList<VariableInfo> variables = null;
    private VariableInfo sumInfo = null;
    private VariableInfo validSumInfo = null;
    private VariableInfo thetaInfo = null;
    private VariableInfo thetaStdErrorInfo = null;
    private VariableInfo extremeInfo = null;
    private VariableInfo umsInfo = null;
    private VariableInfo zUmsInfo = null;
    private VariableInfo wmsInfo = null;
    private VariableInfo zWmsInfo = null;
    private boolean sumAdded = false;
    private boolean validSumAdded = false;
    private boolean thetaAdded = false;
    private boolean stdErrorAdded = false;
    private boolean extremeAdded = false;
    public boolean umsAdded = false;
    public boolean zumsAdded = false;
    public boolean wmsAdded = false;
    public boolean zwmsAdded = false;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public RaschPersonDatabaseOutput(Connection conn, DatabaseAccessObject dao, DatabaseName dbName, DataTableName tableName,
                                  ArrayList<VariableInfo> variables, JointMaximumLikelihoodEstimation jmle){
        this.conn = conn;
        this.dao = dao;
        this.dbName = dbName;
        this.tableName = tableName;
        this.variables = variables;
        this.jmle = jmle;
        variableChangeListeners = new ArrayList<VariableChangeListener>();

    }

    public void addEstimates()throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try{

            conn.setAutoCommit(false);//begin transaction

            addPersonEstimateColumnsToDb();

            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(VariableInfo v : variables){
                select.addColumn(sqlTable, v.getName().nameForDatabase());
            }
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs=stmt.executeQuery(select.toString());

            int i=0;
            double raw = 0.0;
            double validRaw = 0.0;
            double theta = 0.0;
            RaschFitStatistics personFit = null;
            while(rs.next()){
                raw = jmle.getSumScoreAt(i);
                validRaw = jmle.getValidSumScoreAt(i);
                rs.updateDouble(sumInfo.getName().nameForDatabase(), raw);//raw score for all items
                rs.updateDouble(validSumInfo.getName().nameForDatabase(), validRaw);//raw score for valid items only
                theta = jmle.getPersonEstimateAt(i);
                if(Double.isNaN(theta)){
                    rs.updateNull(thetaInfo.getName().nameForDatabase());
                    rs.updateNull(thetaStdErrorInfo.getName().nameForDatabase());
                }else{
                    rs.updateDouble(thetaInfo.getName().nameForDatabase(), theta);
                    rs.updateDouble(thetaStdErrorInfo.getName().nameForDatabase(), jmle.getPersonEstimateStdErrorAt(i));
                }

                int intEx = jmle.getExtremePersonAt(i);
                String strExtreme = "No";
                if(intEx==-1) strExtreme = "Minimum";
                if(intEx== 1) strExtreme = "Maximum";
                rs.updateString(extremeInfo.getName().nameForDatabase(), strExtreme);

                rs.updateRow();
                i++;
            }

        }catch(SQLException ex){
            conn.rollback();
            conn.setAutoCommit(true);
			throw new SQLException(ex);
        }finally{
            conn.commit();
            conn.setAutoCommit(true);
            if(stmt!=null) stmt.close();
            if(rs!=null) rs.close();
        }

    }

    public void addFitStatistics()throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{

            conn.setAutoCommit(false);//begin transaction

            addFitStatisticColumnsToDb();

            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(VariableInfo v : variables){
                select.addColumn(sqlTable, v.getName().nameForDatabase());
            }
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
			rs=stmt.executeQuery(select.toString());

            int i=0;
            double raw = 0.0;
            double validRaw = 0.0;
            double theta = 0.0;
            RaschFitStatistics personFit = null;
            while(rs.next()){
                personFit = jmle.getPersonFitStatisticsAt(i);
                rs.updateDouble(umsInfo.getName().nameForDatabase(), personFit.getUnweightedMeanSquare());
                rs.updateDouble(zUmsInfo.getName().nameForDatabase(), personFit.getStandardizedUnweightedMeanSquare());
                rs.updateDouble(wmsInfo.getName().nameForDatabase(), personFit.getWeightedMeanSquare());
                rs.updateDouble(zWmsInfo.getName().nameForDatabase(), personFit.getStandardizedWeightedMeanSquare());
                rs.updateRow();
                i++;
            }

        }catch(SQLException ex){
            conn.rollback();
            conn.setAutoCommit(true);
			throw new SQLException(ex);
        }finally{
            conn.commit();
            conn.setAutoCommit(true);
            if(stmt!=null) stmt.close();
            if(rs!=null) rs.close();
        }


    }

    private void addPersonEstimateColumnsToDb()throws SQLException{

        int numberOfColumns = dao.getColumnCount(conn, tableName);

        sumInfo = new VariableInfo("sum", "Examinee Sum Score", VariableType.NOT_ITEM, VariableType.DOUBLE, numberOfColumns++, "");
        validSumInfo = new VariableInfo("vsum", "Examinee Score Score for Nonextreme items", VariableType.NOT_ITEM, VariableType.DOUBLE, numberOfColumns++, "");
        thetaInfo = new VariableInfo("theta", "Examinee Proficiency", VariableType.NOT_ITEM, VariableType.DOUBLE, numberOfColumns++, "");
        thetaStdErrorInfo = new VariableInfo("StdErr", "Examinee Proficiency Standard Error", VariableType.NOT_ITEM, VariableType.DOUBLE, numberOfColumns++, "");
        extremeInfo = new VariableInfo("extreme", "Extreme Score", VariableType.NOT_ITEM, VariableType.STRING, numberOfColumns++, "");

        dao.addColumnToDb(conn, tableName, sumInfo);
        sumAdded = true;

        dao.addColumnToDb(conn, tableName, validSumInfo);
        validSumAdded = true;

        dao.addColumnToDb(conn, tableName, thetaInfo);
        thetaAdded = true;

        dao.addColumnToDb(conn, tableName, thetaStdErrorInfo);
        stdErrorAdded = true;

        dao.addColumnToDb(conn, tableName, extremeInfo);
        extremeAdded = true;

        //if no SQLException occurs - variables added to variableChanged object
        variables.add(sumInfo);
        variables.add(validSumInfo);
        variables.add(thetaInfo);
        variables.add(thetaStdErrorInfo);
        variables.add(extremeInfo);

    }

    private void addFitStatisticColumnsToDb()throws SQLException{

        int numberOfColumns = dao.getColumnCount(conn, tableName);
        wmsInfo = new VariableInfo("wms", "Weighted Mean Squares (INFIT)", VariableType.NOT_ITEM, VariableType.DOUBLE, numberOfColumns++, "");
        zWmsInfo = new VariableInfo("stdwms", "Standardized Weighted Mean Squares (INFIT)", VariableType.NOT_ITEM, VariableType.DOUBLE, numberOfColumns++, "");
        umsInfo = new VariableInfo("ums", "Unweighted Mean Squares (OUTFIT)", VariableType.NOT_ITEM, VariableType.DOUBLE, numberOfColumns++, "");
        zUmsInfo = new VariableInfo("stdums", "Standardized Unweighted Mean Squares (OUTFIT)", VariableType.NOT_ITEM, VariableType.DOUBLE, numberOfColumns++, "");

        dao.addColumnToDb(conn, tableName, wmsInfo);
        wmsAdded = true;

        dao.addColumnToDb(conn, tableName, zWmsInfo);
        zwmsAdded = true;

        dao.addColumnToDb(conn, tableName, umsInfo);
        umsAdded = true;

        dao.addColumnToDb(conn, tableName, zUmsInfo);
        zumsAdded = true;

        //if no SQLException occurs - variables added to variableChanged object
        variables.add(wmsInfo);
        variables.add(zWmsInfo);
        variables.add(umsInfo);
        variables.add(zUmsInfo);
    }

    public void updateGui(){
        if(sumAdded){
            fireVariableChanged(new VariableChangeEvent(this, tableName, sumInfo, VariableChangeType.VARIABLE_ADDED));
        }
        if(validSumAdded){
            fireVariableChanged(new VariableChangeEvent(this, tableName, validSumInfo, VariableChangeType.VARIABLE_ADDED));
        }
        if(thetaAdded){
            fireVariableChanged(new VariableChangeEvent(this, tableName, thetaInfo, VariableChangeType.VARIABLE_ADDED));
        }
        if(stdErrorAdded){
            fireVariableChanged(new VariableChangeEvent(this, tableName, thetaStdErrorInfo, VariableChangeType.VARIABLE_ADDED));
        }
        if(extremeAdded){
            fireVariableChanged(new VariableChangeEvent(this, tableName, extremeInfo, VariableChangeType.VARIABLE_ADDED));
        }
        if(umsAdded){
            fireVariableChanged(new VariableChangeEvent(this, tableName, umsInfo, VariableChangeType.VARIABLE_ADDED));
        }
        if(zumsAdded){
            fireVariableChanged(new VariableChangeEvent(this, tableName, zUmsInfo, VariableChangeType.VARIABLE_ADDED));
        }
        if(wmsAdded){
            fireVariableChanged(new VariableChangeEvent(this, tableName, wmsInfo, VariableChangeType.VARIABLE_ADDED));
        }
        if(zwmsAdded){
            fireVariableChanged(new VariableChangeEvent(this, tableName, zWmsInfo, VariableChangeType.VARIABLE_ADDED));
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

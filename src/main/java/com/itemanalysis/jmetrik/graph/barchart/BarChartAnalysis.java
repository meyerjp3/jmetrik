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

package com.itemanalysis.jmetrik.graph.barchart;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.statistics.TwoWayTable;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class BarChartAnalysis extends SwingWorker<BarChartPanel, Void> {

    private DatabaseAccessObject dao = null;

    private BarChartCommand command = null;

    private BarChartPanel barChartPanel = null;

    private Throwable theException = null;

    private Connection conn = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    public StopWatch sw = null;

    public boolean hasGroupingVariable = false;

    private VariableAttributes variable = null;

    private VariableAttributes groupVar = null;

    private DataTableName tableName = null;

    private int progressValue = 0;

    private double maxProgress = 100.0;

    private int lineNumber = 0;

    private ArrayList<VariableChangeListener> variableChangeListeners = null;


    public BarChartAnalysis(Connection conn, DatabaseAccessObject dao, BarChartCommand command, BarChartPanel barChartPanel){
        this.command = command;
        this.barChartPanel = barChartPanel;
        this.conn = conn;
        this.dao = dao;
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

    public TwoWayTable evaluate()throws SQLException {
        Statement stmt = null;
        ResultSet rs=null;
        initializeProgress();

        TwoWayTable table = new TwoWayTable();

        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        select.addColumn(sqlTable, variable.getName().nameForDatabase());
        if(hasGroupingVariable){
            select.addColumn(sqlTable, groupVar.getName().nameForDatabase());
        }
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs=stmt.executeQuery(select.toString());

        String value = null;
        boolean nullValue = false;
        String groupBy = null;
        boolean nullGroupBy = false;

        while(rs.next()){
            value = rs.getString(variable.getName().nameForDatabase());
            nullValue = rs.wasNull();

            if(hasGroupingVariable){
                groupBy = rs.getString(groupVar.getName().nameForDatabase());
                nullGroupBy = rs.wasNull();
                if(!nullValue && ! nullGroupBy){
                    table.addValue(groupBy.toString(), value.toString());
                }
            }else{
                if(!nullValue){
                    table.addValue("A",value.toString());
                }
            }
            updateProgress();
        }
        rs.close();
        stmt.close();
        return table;

    }

    protected BarChartPanel doInBackground() {
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Bar Chart...");
        this.firePropertyChange("progress-on", null, null);
        try{
            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            String selectVariable = command.getFreeOption("variable").getString();
            variable = dao.getVariableAttributes(conn, new VariableTableName(tableName.toString()), selectVariable);
            if(command.getFreeOption("groupvar").hasValue()){
                String groupByName=command.getFreeOption("groupvar").getString();
                groupVar = dao.getVariableAttributes(conn, new VariableTableName(tableName.toString()), groupByName);
                hasGroupingVariable = true;
            }

            barChartPanel.updateDataset(evaluate());

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible
        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return barChartPanel;
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
            scriptLogger.info(command.paste());
            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }
        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }

    }



}


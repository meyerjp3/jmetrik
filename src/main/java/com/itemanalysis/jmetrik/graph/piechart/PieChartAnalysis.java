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

package com.itemanalysis.jmetrik.graph.piechart;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.statistics.TwoWayTable;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;

public class PieChartAnalysis extends SwingWorker<PieChartPanel, Void> {

    private PieChartCommand command = null;
    private PieChartPanel pieChartPanel = null;
    private Throwable theException = null;
    private Connection conn = null;
    public StopWatch sw = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    private DataTableName tableName = null;
    private boolean hasGroupingVariable = false;
    private VariableInfo variable = null;
    private VariableInfo groupVar = null;
    private int progressValue = 0;
    private int lineNumber = 0;
    private double maxProgress = 100.0;
    private DatabaseAccessObject dao = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;

    public PieChartAnalysis(Connection conn, DatabaseAccessObject dao, PieChartCommand command, PieChartPanel pieChartPanel){
        this.command = command;
        this.pieChartPanel = pieChartPanel;
        this.conn = conn;
        this.dao = dao;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/ maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    public TwoWayTable evaluate()throws SQLException{
        Statement stmt = null;
        ResultSet rs=null;
        try{
            int nrow = 0;
            JmetrikPreferencesManager pref = new JmetrikPreferencesManager();
            String dbType = pref.getDatabaseType();
            if(DatabaseType.APACHE_DERBY.toString().equals(dbType)){
                JmetrikDatabaseFactory dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
                nrow = dao.getRowCount(conn, tableName);
            }else{
                //add other databases here when functionality is added
            }
            maxProgress = (double)nrow;

            TwoWayTable table = new TwoWayTable();

            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(sqlTable, variable.getName().nameForDatabase());
            if(hasGroupingVariable){
                select.addColumn(sqlTable, groupVar.getName().nameForDatabase());
            }
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            Object value = null;
            Object groupBy = null;
            while(rs.next()){
                value = rs.getObject(variable.getName().nameForDatabase());
                if(hasGroupingVariable){
                    groupBy = rs.getObject(groupVar.getName().nameForDatabase());
                    if(value!=null && groupBy != null){
                        table.addValue(groupBy.toString(), value.toString());
                    }else{
                        //don't addValue if missing grouBy variable or a value
                    }

                }else{
                    if(value!=null){
                        table.addValue("A",value.toString());
                    }else{
                        //don't addValue if missing a variable
                    }
                }
                updateProgress();
            }
            rs.close();
            stmt.close();
            return table;
        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex);
        }

    }

    protected PieChartPanel doInBackground() {
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Pie Chart...");
        this.firePropertyChange("progress-on", null, null);
        try{
            logger.info(command.paste());

            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            String selectVariable = command.getFreeOption("variable").getString();
            variable = dao.getVariableInfo(conn, new VariableTableName(tableName.toString()), selectVariable);
            if(command.getFreeOption("groupvar").hasValue()){
                String groupByName=command.getFreeOption("groupvar").getString();
                groupVar = dao.getVariableInfo(conn, new VariableTableName(tableName.toString()), groupByName);
                hasGroupingVariable = true;
            }

            if(pieChartPanel.hasGroupVariable()){
                pieChartPanel.updateDefaultCategoryDataset(evaluate());
            }else{
                pieChartPanel.updateDefaultPieDataset(evaluate());
            }
            conn.commit();
            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible

        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return pieChartPanel;
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
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }

    }

}

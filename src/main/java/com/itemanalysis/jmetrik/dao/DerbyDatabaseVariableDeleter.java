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

package com.itemanalysis.jmetrik.dao;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.DeleteVariableCommand;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DerbyDatabaseVariableDeleter extends SwingWorker<String, Void> implements DatabaseVariableDeleter {

    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private VariableTableName variableTableName = null;
    private DeleteVariableCommand command = null;
    private ArrayList<String> varList = null;
    private ArrayList<VariableAttributes> variables = null;
    private Throwable theException = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    public DerbyDatabaseVariableDeleter(Connection conn, DatabaseAccessObject dao, DeleteVariableCommand command){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void processCommand()throws IllegalArgumentException{
        dbName = new DatabaseName(command.getPairedOptionList("data").getStringAt("db"));
        tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
        variableTableName = new VariableTableName(tableName.toString());
        varList = command.getFreeOptionList("variables").getString();
    }

    public void deleteVariable() throws SQLException {
        try{
            conn.setAutoCommit(false);//start tansaction

            //get variable info from db
            variables = dao.getSelectedVariables(conn, variableTableName, varList);

            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            String sqlString = "";
            for(VariableAttributes v : variables){
                //remove column from data table
                sqlString = "ALTER TABLE " + tableName.getNameForDatabase() + " DROP COLUMN " + v.getName().nameForDatabase();
                stmt.execute(sqlString);

                //remove column from variable table
                sqlString = "DELETE FROM " + variableTableName.getNameForDatabase() + " WHERE variable = " + v.getName().quotedName();
                stmt.execute(sqlString);
            }
            conn.commit();//end transaction
            conn.setAutoCommit(true);
            stmt.close();
		}catch(SQLException ex){
            try{
                conn.rollback();//rollback and end transaction
                conn.setAutoCommit(true);
                throw new SQLException(ex);
            }catch(SQLException ex2){
                throw new SQLException(ex2);
            }
        }
    }

    public ArrayList<VariableAttributes> getDroppedColumns(){
        return variables;
    }

    @Override
    protected String doInBackground()throws Exception{
        firePropertyChange("status", "", "Deleting variables...");
        firePropertyChange("progress-ind-on", null, null);

        logger.info(command.paste());
        try{
            processCommand();
            deleteVariable();
        }catch(Exception ex){
            theException = ex;
        }

        return "";
    }

    @Override
    protected void done(){
        if(theException!=null){
            logger.fatal(theException.getMessage(), theException);
            firePropertyChange("error", "", "Error - Check log for details.");
        }else{

            for(VariableAttributes v : variables){
                fireVariableChanged(new VariableChangeEvent(this, tableName, v, VariableChangeType.VARIABLE_DELETED));
            }
            scriptLogger.info(command.paste());
            firePropertyChange("status", "", "Ready");
        }
        firePropertyChange("progress-off", null, null);
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

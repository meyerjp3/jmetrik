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
import com.itemanalysis.jmetrik.workspace.RenameVariableCommand;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DerbyDatabaseVariableRenamer extends SwingWorker<String, Void> implements DatabaseVariableRenamer {

    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private RenameVariableCommand command = null;
    private Throwable theException = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    VariableName oldName = null;
    VariableName newName = null;
    DatabaseName dbName = null;
    DataTableName tableName = null;
    VariableTableName variableTableName = null;
    VariableAttributes oldVariableInfo = null;
    VariableAttributes newVariableInfo = null;

    public DerbyDatabaseVariableRenamer(Connection conn, DatabaseAccessObject dao, RenameVariableCommand command){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void processCommand(){
        String old = command.getPairedOptionList("variable").getStringAt("oldname");
        String newNameString = command.getPairedOptionList("variable").getStringAt("newname");
        String db = command.getPairedOptionList("data").getStringAt("db");
        String tbl = command.getPairedOptionList("data").getStringAt("table");

        oldName = new VariableName(old);
        newName = new VariableName(newNameString);
        dbName = new DatabaseName(db);
        tableName = new DataTableName(tbl);
        variableTableName = new VariableTableName(tableName.toString());

    }

    public void renameVariable()throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        try{
            conn.setAutoCommit(false);

            //get old variable info from db
            oldVariableInfo = dao.getVariableAttributes(conn, variableTableName, oldName.toString());

            //rename variable in data table
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String queryString = "RENAME COLUMN " + tableName.getNameForDatabase() + "." +
                    oldName.nameForDatabase() + " TO " + newName.nameForDatabase();
            stmt.execute(queryString);

            //rename variable in variable table
            VariableName name = new VariableName("variable");
            queryString = "SELECT " + name.toString()  + " FROM " + variableTableName.getNameForDatabase() +
                    " WHERE " + name.toString() +" = " + "'" + oldName.toString() + "'";
            rs = stmt.executeQuery(queryString);

            rs.next();
            rs.updateString(name.toString(), newName.toString());
            rs.updateRow();

            conn.commit();//end transaction
            conn.setAutoCommit(true);
        }catch(SQLException ex){
            try{
                conn.rollback();//rollback and end transaction
                conn.setAutoCommit(true);
                throw new SQLException(ex);
            }catch(SQLException ex2){
                throw new SQLException(ex2);
            }
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
            conn.setAutoCommit(true);
        }
    }

    private void getNewVariableInfo()throws SQLException{
        newVariableInfo = dao.getVariableAttributes(conn, variableTableName, newName.toString());
    }

    @Override
    protected String doInBackground(){
        firePropertyChange("status", "", "Renaming variable...");
        firePropertyChange("progress-ind-on", null, null);

        logger.info(command.paste());

        try{
            processCommand();
            renameVariable();
            getNewVariableInfo();
        }catch(SQLException ex){
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
            fireVariableChanged(new VariableChangeEvent(this, tableName, newVariableInfo, oldVariableInfo, VariableChangeType.VARIABLE_RENAMED));
            firePropertyChange("status", "", "Ready");
            scriptLogger.info(command.paste());
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

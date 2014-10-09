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
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.DatabaseCommand;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DerbyDatabaseTableDeleter extends SwingWorker<String, Void> implements DatabaseTableDeleter{

    private Connection conn = null;

    private DatabaseCommand command = null;

    private DerbyDatabaseAccessObject dao = null;

    private Throwable theException = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public DerbyDatabaseTableDeleter(Connection conn, DatabaseAccessObject dao, DatabaseCommand command){
        this.conn = conn;
        this.dao = (DerbyDatabaseAccessObject)dao;
        this.command = command;
    }

    /**
     * One or more tables are deleted in a single transaction.
     *
     * @param tableName list of table names to be deleted from database
     * @throws IllegalArgumentException
     * @throws SQLException
     */
    public void deleteTable(ArrayList<String> tableName)throws IllegalArgumentException, SQLException{
        if(command.getSelectOneOption("action").isValueSelected("delete-table")){
            try{
                conn.setAutoCommit(false);
                DataTableName tempDataTableName = null;
                VariableTableName tempVariableTableName = null;

                Statement stmt = conn.createStatement();

                for(String s : tableName){
                    //drop data table from database
                    tempDataTableName = new DataTableName(s);
                    stmt.executeUpdate("DROP TABLE " + tempDataTableName.getNameForDatabase());

                    //drop variable table from database
                    tempVariableTableName = new VariableTableName(s);
                    stmt.executeUpdate("DROP TABLE " + tempVariableTableName.getNameForDatabase());

                    //drop row count form row count table
                    dao.dropRowCount(conn, tempDataTableName);

                    //fire property change
                    this.firePropertyChange("table-deleted", "", tempDataTableName);//remove node from tree

                }
                stmt.close();
                conn.commit();
            }catch(SQLException ex){
                conn.rollback();
                throw new SQLException(ex);
            }
            conn.setAutoCommit(false);
        }//end if
    }

    @Override
    protected String doInBackground()throws Exception{
        firePropertyChange("status", "", "Deleting table...");
        firePropertyChange("progress-ind-on", null, null);
        logger.info(command.paste());
        try{
            String name = command.getFreeOption("name").getString();
            ArrayList<String> tableName = command.getFreeOptionList("tables").getString();
            deleteTable(tableName);

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
            firePropertyChange("status", "", "Ready");
        }
        firePropertyChange("progress-off", null, null);
    }

}

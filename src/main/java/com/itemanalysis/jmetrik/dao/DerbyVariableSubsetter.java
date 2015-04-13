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
import com.itemanalysis.jmetrik.workspace.SubsetVariableCommand;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.tools.StopWatch;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;

public class DerbyVariableSubsetter extends SwingWorker<String,Void> implements DatabaseVariableSubsetter{

    private Connection conn = null;

    private SubsetVariableCommand command = null;

    private Throwable theException = null;

    private StopWatch timer = null;

    private ArrayList<VariableAttributes> variables = null;

    private DataTableName dataTableName = null;

    private VariableTableName variableTableName = null;

    private DataTableName newDataTableName = null;

    private VariableTableName newVariableTableName = null;

    private ArrayList<String> selectedVariables = null;

    private boolean tablesCreated = false;

    private boolean display = true;

    private boolean forceTableName = false;

    private String newTable = "";

    static Logger logger = Logger.getLogger("jmetrik-logger");

    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    public DerbyVariableSubsetter(Connection conn, SubsetVariableCommand command){
        this.conn = conn;
        this.command = command;
        variables = new ArrayList<VariableAttributes>();
    }


    public void parseCommand()throws IllegalArgumentException{
        logger.info(command.paste());
        String dn = command.getPairedOptionList("data").getStringAt("table");
        dataTableName = new DataTableName(dn);
        variableTableName = new VariableTableName(dn);
        newTable = command.getFreeOption("newtable").getString();
        newDataTableName = new DataTableName(newTable);
        newVariableTableName = new VariableTableName(newTable);
        selectedVariables = command.getFreeOptionList("variables").getString();
        display = command.getSelectAllOption("options").isArgumentSelected("display");
        forceTableName = command.getSelectAllOption("options").isArgumentSelected("force");
    }

    public void subsetVariables()throws SQLException {

        try{

            //get VariableInfo for selected variables
            JmetrikDatabaseFactory dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
            DatabaseAccessObject dao = dbFactory.getDatabaseAccessObject();
            variables = dao.getSelectedVariables(conn, variableTableName, selectedVariables);

            //get unique table name
            if(forceTableName){
                newDataTableName = dao.getUniqueTableName(conn, newTable);
                newVariableTableName = new VariableTableName(newDataTableName.toString());
            }else{
                newDataTableName = new DataTableName(newTable);
                newVariableTableName = new VariableTableName(newTable);
            }


            //start transaction
            conn.setAutoCommit(false);

            //create new variable table
            Statement stmt = conn.createStatement();
            String sqlString = "CREATE TABLE " + newVariableTableName.getNameForDatabase() +
                    " (" +
                    "VARIABLE VARCHAR(30)," +           //name
                    "VARGROUP VARCHAR(30)," +           //group
                    "SCORING VARCHAR(250)," +           //scoring
                    "ITEMTYPE SMALLINT," +              //item type
                    "DATATYPE SMALLINT," +              //data type
                    "LABEL VARCHAR(150)," +              //label
                    "OMITCODE VARCHAR(30)," +            //omit code
                    "NOTREACHEDCODE VARCHAR(30))";      //not reached code
            stmt.execute(sqlString);

            //Populate new variable table
            String updateString = "INSERT INTO " + newVariableTableName.getNameForDatabase() + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(updateString);
            for(VariableAttributes v : variables){
                pstmt.setString(1, v.getName().toString());                 //name
                pstmt.setString(2, v.getItemGroup());                        //subscale/group
                pstmt.setString(3, v.printOptionScoreKey());                //scoring

                ItemType it = v.getType().getItemType();
                int itemInt = it.toInt(it);
                DataType dt = v.getType().getDataType();
                int dataInt = dt.toInt(dt);

                pstmt.setInt(4, itemInt);                 //item type
                pstmt.setInt(5, dataInt);                 //data type
                pstmt.setString(6, v.getLabel().toString());                //label

                Object omit = v.getSpecialDataCodes().getOmittedCode();
                if(omit!=null && !omit.toString().trim().equals("")){
                    pstmt.setString(7, omit.toString().trim());                //omit code
                }else{
                    pstmt.setNull(7, Types.VARCHAR);                           //omit code initially set to null
                }

                Object nr = v.getSpecialDataCodes().getNotReachedCode();
                if(nr!=null && !nr.toString().trim().equals("")){
                    pstmt.setString(8, nr.toString().trim());                //not reached code
                }else{
                    pstmt.setNull(8, Types.VARCHAR);                         //not reached code initially set to null
                }

                pstmt.executeUpdate();
            }
            pstmt.close();

            //create new data table
            String newTableString = "CREATE TABLE " + newDataTableName.getNameForDatabase() + " AS SELECT ";
            Iterator<VariableAttributes> iter = variables.iterator();
            while(iter.hasNext()){
                newTableString += iter.next().getName().nameForDatabase();
                if(iter.hasNext()){
                    newTableString += ",";
                }
            }
            newTableString += " FROM " + dataTableName.getNameForDatabase() + " WITH NO DATA";
            stmt.execute(newTableString);

            //populate new table with selected cases
            newTableString = "INSERT INTO " + newDataTableName.getNameForDatabase() + " SELECT ";
            iter = variables.iterator();
            while(iter.hasNext()){
                newTableString += iter.next().getName().nameForDatabase();
                if(iter.hasNext()){
                    newTableString += ",";
                }
            }
            newTableString += " FROM " + dataTableName.getNameForDatabase();
            int updates = stmt.executeUpdate(newTableString);

            stmt.close();

            //set row count and table description in table
            String desc = "Subset of variables from " + dataTableName.toString();
            dao.setTableInformation(conn, newDataTableName, updates, desc);

            //close transaction
            conn.commit();
            conn.setAutoCommit(true);

            tablesCreated = true;

        }catch(SQLException ex){
            conn.rollback();
            conn.setAutoCommit(true);
            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex);
        }
    }

    protected String doInBackground() throws Exception{
        timer = new StopWatch();
        try{
            logger.info(command.paste());
            this.firePropertyChange("status", "", "Subsetting variables...");
            this.firePropertyChange("progress-ind-on", null, null);
            parseCommand();
            subsetVariables();
            logger.info("Subset complete: " + timer.getElapsedTime());
        }catch(Throwable t){
            theException=t;
        }
        return "";
    }

    protected void done(){
        if(tablesCreated) this.firePropertyChange("table-added", "", newDataTableName);//will add node to tree
        if(theException==null){
            this.firePropertyChange("status", "", "Ready");//will display status in statusBar
            if(display) this.firePropertyChange("import", "", newDataTableName);//will display data table in dialogs
            scriptLogger.info(command.paste());
        }else{
            logger.fatal(theException.getMessage(), theException);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
        firePropertyChange("progress-off", null, null);
    }

}

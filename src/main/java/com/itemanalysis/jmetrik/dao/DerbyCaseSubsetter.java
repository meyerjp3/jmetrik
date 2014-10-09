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
import com.itemanalysis.jmetrik.workspace.SubsetCasesCommand;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.tools.StopWatch;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;

public class DerbyCaseSubsetter extends SwingWorker<String,Void> implements DatabaseCaseSubsetter {

    private Connection conn = null;

    private SubsetCasesCommand command = null;

    private Throwable theException = null;

    private StopWatch timer = null;

    private ArrayList<VariableInfo> variables = null;

    private DataTableName dataTableName = null;

    private VariableTableName variableTableName = null;

    private DataTableName newDataTableName = null;

    private VariableTableName newVariableTableName = null;

    private boolean tablesCreated = false;

    private boolean display = true;

    private boolean forceTableName = false;

    private String whereString = "";

    private String newTable = "";

    static Logger logger = Logger.getLogger("jmetrik-logger");


    public DerbyCaseSubsetter(Connection conn, SubsetCasesCommand command){
        this.conn = conn;
        this.command = command;
        variables = new ArrayList<VariableInfo>();
    }

    public void parseCommand()throws IllegalArgumentException{
        logger.info(command.paste());
        String dn = command.getPairedOptionList("data").getStringAt("table");
        dataTableName = new DataTableName(dn);
        variableTableName = new VariableTableName(dn);
        newTable = command.getFreeOption("newtable").getString();
        whereString = command.getFreeOption("where").getString();
        display = command.getSelectAllOption("options").isArgumentSelected("display");
        forceTableName = command.getSelectAllOption("options").isArgumentSelected("force");
    }

    public void subsetCases()throws SQLException {
        PreparedStatement pstmt = null;
        Statement stmt = null;

        try{
            //get variable info for all variables in table
            JmetrikDatabaseFactory dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
            DatabaseAccessObject dao = dbFactory.getDatabaseAccessObject();
            variables = dao.getAllVariables(conn, variableTableName);

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
            stmt = conn.createStatement();
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
            pstmt = conn.prepareStatement(updateString);
            for(VariableInfo v : variables){
                pstmt.setString(1, v.getName().toString());                 //name
                pstmt.setString(2, v.getSubscale());                        //subscale/group
                pstmt.setString(3, v.printOptionScoreKey());                //scoring
                pstmt.setInt(4, v.getType().getItemType());                 //item type
                pstmt.setInt(5, v.getType().getDataType());                 //data type
                pstmt.setString(6, v.getLabel().toString());                //label

                Object omit = v.getOmitCode();
                if(omit!=null && !omit.toString().trim().equals("")){
                    pstmt.setString(7, omit.toString().trim());                //omit code
                }else{
                    pstmt.setNull(7, Types.VARCHAR);                           //omit code initially set to null
                }

                Object nr = v.getNotReachedCode();
                if(nr!=null && !nr.toString().trim().equals("")){
                    pstmt.setString(8, nr.toString().trim());                //not reached code
                }else{
                    pstmt.setNull(8, Types.VARCHAR);                         //not reached code initially set to null
                }

                pstmt.executeUpdate();
            }
            pstmt.close();

            //create new data table
            String newWhere = convertWhereStatement();
            String newTableString = "CREATE TABLE " + newDataTableName.getNameForDatabase() + " AS SELECT * FROM " +
                    dataTableName.getNameForDatabase() + " WITH NO DATA";//future releases of Derby will allow WITH DATA but it is not currently available
            stmt.execute(newTableString);

            //populate new table with selected cases
            newTableString = "INSERT INTO " + newDataTableName.getNameForDatabase() +
                    " SELECT * FROM " + dataTableName.getNameForDatabase() + " WHERE " + newWhere;

            int updates = stmt.executeUpdate(newTableString);

            stmt.close();

            //set row count and table description
            String desc = "Subset of " + dataTableName.toString() + " WHERE " + whereString;
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
        }finally{
            if(pstmt!=null) pstmt.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * jMetrik database column names start and end with an "x". This convention
     * prevents conflict with reserved words in a SQL query. The column names
     * are displayed in jMetrik without the leading and trailing "x". This method
     * converts the displayed column names in a string of text with the
     * internal column names so that the string can be used in a SQL query.
     *
     * @return
     */
    private String convertWhereStatement(){
        String newWhereString = "";
        String oldName = "";
        String newName = "";
        String nameOnly = "";

        //TODO add single quotes around values for string variables

        String[] temp = whereString.split("\\s+");

        for(String s : temp){
            nameOnly = s.replaceAll("\\(\\)", "").trim();
            for(VariableInfo v : variables){
                oldName = v.getName().toString();
                newName = v.getName().nameForDatabase();

                if(nameOnly.equals(oldName)){
                    s = s.replaceAll(oldName, newName);
                }
            }
            newWhereString += s + " ";
        }
        return newWhereString.trim();
    }

    protected String doInBackground() throws Exception{
        timer = new StopWatch();
        try{
            logger.info(command.paste());
            this.firePropertyChange("status", "", "Subsetting cases...");
            this.firePropertyChange("progress-ind-on", null, null);
            parseCommand();
            subsetCases();
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
        }else{
            logger.fatal(theException.getMessage(), theException);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
        firePropertyChange("progress-off", null, null);
    }


}

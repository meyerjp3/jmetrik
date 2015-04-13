/*
 * Copyright (c) 2011 Patrick Meyer
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

import au.com.bytecode.opencsv.CSVReader;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.utils.DerbyCSVWriter;
import com.itemanalysis.jmetrik.workspace.ImportCommand;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.tools.StopWatch;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.*;
import java.sql.*;
import java.util.ArrayList;

public class DerbyDelimitedFileImporter  extends SwingWorker<String,Void> implements DelimitedFileImporter {

    private Connection conn = null;

    private ImportCommand command = null;

    private String tableNameString = "";

    private DataTableName dataTableName = null;

    private VariableTableName variableTableName = null;

    private String fileName = "";

    private char delimiter = ',';

    private String description = "";

    private int numberOfVariables = 0;

    private boolean headerIncluded = true;

    private boolean display = true;

    private Throwable theException = null;

    private ArrayList<VariableAttributes> variables = null;

    private StopWatch timer = null;

    private boolean tablesCreated = false;

    private DerbyDatabaseAccessObject dao = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    public DerbyDelimitedFileImporter(Connection conn, ImportCommand command){
        this.conn = conn;
        this.command = command;
        variables = new ArrayList<VariableAttributes>();
        dao = new DerbyDatabaseAccessObject();
    }

    public void parseCommand()throws IllegalArgumentException{
        logger.info(command.paste());
        headerIncluded = command.getSelectOneOption("header").getSelectedArgument().equals("included");
        fileName = command.getFreeOption("file").getString();
        String dl = command.getSelectOneOption("delimiter").getSelectedArgument();
        delimiter = getDelimiter(dl);
        tableNameString = command.getPairedOptionList("data").getStringAt("table");
        display = command.getSelectAllOption("options").isArgumentSelected("display");
        description = command.getFreeOption("description").getString();
    }

    public char getDelimiter(String delimiterName){
        char d = ',';
        if(delimiterName.equals("tab")){
            d='\t';
        }else if(delimiterName.equals("semicolon")){
            d=';';
        }else if(delimiterName.equals("colon")){
            d=':';
        }else{
            d=',';
        }
        return d;
    }

    public void importDelimitedFile() throws IOException, SQLException{

        try{
            //get variable names from header or create them
            processHeader();

            //scan file to determine type of data in each column and eliminate white spaces
            File tempFile = writeTempFile(fileName);
            tempFile.deleteOnExit();

            String strDelimiter = String.valueOf(delimiter).toString();

            this.firePropertyChange("status", "", "Importing file...");

            //create tables
            DerbyDatabaseAccessObject dbDao = new DerbyDatabaseAccessObject();

            //get unique table name
            dataTableName = dbDao.getUniqueTableName(conn, tableNameString);
            variableTableName = new VariableTableName(dataTableName.toString());

            dbDao.createTables(conn, dataTableName, variableTableName, variables);

            //import data into data table
            CallableStatement cs = conn.prepareCall("CALL SYSCS_UTIL.SYSCS_IMPORT_DATA(?, ?, ?, ?, ?, ?, ?, ?, ?)");
            cs.setNull(1, Types.VARCHAR);
            cs.setString(2, dataTableName.getNameForDatabase());
            cs.setNull(3, Types.VARCHAR);
            cs.setNull(4, Types.VARCHAR);
            cs.setString(5, tempFile.getAbsolutePath());
            cs.setString(6, strDelimiter);
            cs.setNull(7, Types.CHAR);
            cs.setNull(8, Types.VARCHAR);
            cs.setInt(9, 1);
            cs.execute();
            conn.commit();
            cs.close();
            conn.setAutoCommit(true);//close transaction

            setTableInformation();

        }catch(SQLException ex){
            conn.rollback();
            conn.setAutoCommit(true);

            //delete tables and remove row count

            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex.getMessage());
        }catch(IOException ex){
            conn.rollback();
            conn.setAutoCommit(true);
            logger.fatal(ex.getMessage(), ex);
            throw new IOException(ex.getMessage());
        }

    }

    /**
     * Process header and create list of VariableInfo objects. If the file includes a header,
     * variable names are taken from the header. Otherwise, variable names are created.
     * The first row of the data file establishes the number of variables in the data.
     *
     * @return
     * @throws IOException
     */
    public String processHeader()throws IOException{
        StringBuilder sb = new StringBuilder();

        CSVReader reader = new CSVReader(new FileReader(fileName), delimiter);
        String[] header = reader.readNext();//first line will be header or data

        String tempVarName = "";
        VariableName varName = null;
        for(int i=0;i<header.length;i++){
            if(headerIncluded){
                tempVarName = header[i].trim().replaceAll("\"", "");
            }else{
                tempVarName = "Var" + (i+1);
            }

            varName = new VariableName(tempVarName);

            VariableAttributes var = new VariableAttributes(
                    varName.toString(),
                    "",
                    ItemType.NOT_ITEM,
                    DataType.DOUBLE,
                    (i+1),
                    "");

            if(var.getName().nameChanged() || !headerIncluded){
                sb.append(var.getName().printNameChangeInformation());
                sb.append("\n");
            }

            variables.add(var);

        }
        reader.close();

        numberOfVariables = variables.size();

        return sb.toString();
    }

    /**
     * Read file and determine the type of data in each field. Must be called prior to
     * writing the temporary file that will be imported so that doubles and strings
     * make correct use of quote characters.
     *
     * @param file
     * @throws IOException
     */
    public void scanFile(File file) throws IOException{

        CSVReader reader = new CSVReader(new FileReader(file), delimiter);

        int firstDimensionMismatch = 0;
        String[] line;
        String tempString = "";
        VariableAttributes tempVar;
        int ncol = 0;
        int count = 0;
        int colWidth = 50;

        try{
            while((line=reader.readNext())!=null){
                ncol = line.length;
                if(firstDimensionMismatch==0 && ncol!=numberOfVariables){
                    firstDimensionMismatch = (count+1);
                    logger.fatal("Import dimension mismatch: The number of columns in row " + firstDimensionMismatch + " of the data file " +
                            "does not match the number of variables in the first row.");
                }

                if(headerIncluded && count>0){
                    //start processing first line of data
                    for(int i=0;i<ncol;i++){
                        tempString = line[i].trim();

                        if("".equals(tempString)){
                            //White spaces found. Eliminate white spaces.
                            //IMPORTANT NOTE: Empty strings and NAs will be imported as empty strings and not as null values
                            //  when the database column type is String.
                            line[i] = tempString;
                        }else{
                            //no white spaces (i.e. no missing data)
                            try{
                                //default data type is double
                                Double.parseDouble(tempString);
                            }catch(NumberFormatException ex){
                                //string detected, change data type in VariableInfo
                                tempVar = variables.get(i);

                                //if number format exception is encountered, data type is set to string
                                tempVar.getType().setDataType(DataType.STRING);

                                //set varChar size to be between 50 and 255
                                colWidth = Math.max(tempVar.getVarcharSize(), tempString.length());
                                tempVar.setVarcharSize(Math.min(colWidth, 255));
                            }
                        }

                    }//end loop over columns

                }//end if

                count++;
            }

        }catch(IOException ex){
            throw ex;
        }finally {
            if(reader!=null) reader.close();
        }


    }

    /**
     * Determines the type of data in each column. It limits text data to a maximum of 255 characters.
     * It also creates a temp file that does not contains empty spaces. Eliminating empty spaces
     * is necessary for the Derby import. An empty space for a double throws an exception.
     *
     * @throws IOException
     */
    public File writeTempFile(String scanFileName)throws IOException{
        File tempFile = File.createTempFile("jmk-import-temp", ".txt");
        tempFile.deleteOnExit();

        scanFile(tempFile);

        CSVReader reader = new CSVReader(new FileReader(new File(scanFileName)), delimiter);
        DerbyCSVWriter writer = new DerbyCSVWriter(new FileWriter(tempFile), delimiter, variables);

        String[] line = null;
        int count = 0;
        int colWidth = 50;
        VariableAttributes tempVar = null;
        String tempString = "";
        int ncol = 0;

        int firstDimensionMismatch = 0;

        try{
            while((line=reader.readNext())!=null){
                ncol = line.length;

                if(firstDimensionMismatch==0 && ncol!=numberOfVariables){
                    firstDimensionMismatch = (count+1);
                    logger.fatal("Import dimension mismatch: The number of columns in row " + firstDimensionMismatch + " of the data file " +
                            "does not match the number of variables in the first row.");
                }

                if(headerIncluded && count==0){
                    //do not write header. Header has already been processed with processHeader().
                    //Derby CallableStatement cannot be used with a file that has names in the first row.
                }else if((headerIncluded && count>0) || !headerIncluded){
                    //start processing first line of data
                    for(int i=0;i<ncol;i++){
                        tempString = line[i].trim();

                        if("".equals(tempString) || "NA".equals(tempString)){
                            //White spaces found or missing data code NA found. Eliminate white spaces and missing data.
                            //IMPORTANT NOTE: Empty strings and NAs will be imported as empty strings and not as null values
                            //  when the database column type is String.
                            line[i] = "";
                        }else{
                            //no white spaces (i.e. no missing data)
                            try{
                                //default data type is double
                                Double.parseDouble(tempString);
                            }catch(NumberFormatException ex){
                                //string detected, change data type in VariableInfo
                                tempVar = variables.get(i);

                                //if number format exception is encountered, data type is set to string
                                tempVar.getType().setDataType(DataType.STRING);

                                //set varChar size to be between 50 and 255
                                colWidth = Math.max(tempVar.getVarcharSize(), tempString.length());
                                tempVar.setVarcharSize(Math.min(colWidth, 255));
                            }
                        }

                    }//end loop over columns

                    //write line to temp file
                    writer.writeNext(line);

                }

                count++;//line count

            }//end loop over file



            return tempFile;

        }catch(IOException ex){
            throw ex;
        }finally {
            if(reader!=null) reader.close();
            if(writer!=null) writer.close();
        }
    }


    private void setTableInformation()throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        try{
            String QUERY = "SELECT COUNT(*) FROM " + dataTableName.getNameForDatabase();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(QUERY);
            rs.next();
            int rowCount = rs.getInt(1);
            rs.close();

            if("".equals(description.trim())){
                description = "Import of " + fileName;
            }

            dao.setTableInformation(conn, dataTableName, rowCount, description);

        }catch(SQLException ex){
            throw(ex);
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    protected String doInBackground() throws Exception{
        timer = new StopWatch();
        try{
            this.firePropertyChange("status", "", "Scanning file...");
            this.firePropertyChange("progress-ind-on", null, null);
            parseCommand();
            importDelimitedFile();
            logger.info("Import complete: " + timer.getElapsedTime());
        }catch(Throwable t){
            theException=t;
        }
        return "";
    }

    protected void done(){
        if(tablesCreated) this.firePropertyChange("table-added", "", dataTableName);//will add node to tree
        if(theException==null){
            this.firePropertyChange("status", "", "Ready");//will display status in statusBar
            if(display) this.firePropertyChange("import", "", dataTableName);//will display data table in dialogs
            scriptLogger.info(command.paste());
        }else{
            logger.fatal(theException.getMessage(), theException);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
        firePropertyChange("progress-off", null, null);
    }


}

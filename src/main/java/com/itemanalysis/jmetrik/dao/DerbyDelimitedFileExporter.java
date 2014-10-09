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

import au.com.bytecode.opencsv.CSVWriter;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.ExportCommand;
import com.itemanalysis.jmetrik.workspace.JmetrikCsvWriter;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.tools.StopWatch;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DerbyDelimitedFileExporter extends SwingWorker<String,Void> implements DelimitedFileExporter{
    
    private Connection conn = null;
    
    private ExportCommand command = null;

    private StopWatch timer = null;

    private Throwable theException=null;

    private ArrayList<VariableInfo> variables = null;

    private boolean exportScoredData = false;

    private boolean useQuotes = false;

    private boolean header = true;

    private DataTableName tableName = null;

    private char delimiter = ',';

    private String outputFileName ="";

    static Logger logger = Logger.getLogger("jmetrik-logger");
    
    public DerbyDelimitedFileExporter(Connection conn, ExportCommand command){
        this.conn = conn;
        this.command = command;
        timer = new StopWatch();
    }
    
    public void parseCommand()throws IllegalArgumentException{
        //get variable info from db
        tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
        exportScoredData = command.getSelectAllOption("options").isArgumentSelected("scored");
        useQuotes = command.getSelectAllOption("options").isArgumentSelected("quotes");
        header = command.getSelectOneOption("header").isValueSelected("included");
        outputFileName = command.getFreeOption("file").getString();
        delimiter = getDelimiter(command.getSelectOneOption("delimiter").getSelectedArgument());
    }

    private void getVariables()throws SQLException{
        JmetrikDatabaseFactory dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
        DatabaseAccessObject dao = dbFactory.getDatabaseAccessObject();
        variables = dao.getAllVariables(conn, new VariableTableName(tableName.getTableName()));
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

    public void exportDelimitedFile()throws IOException, SQLException{
        try{
            JmetrikCsvWriter writer = null;
            if(useQuotes){
                writer = new JmetrikCsvWriter(new FileWriter(new File(outputFileName)), delimiter);
            }else{
                writer = new JmetrikCsvWriter(new FileWriter(new File(outputFileName)), delimiter, CSVWriter.NO_QUOTE_CHARACTER);
            }

            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName.getNameForDatabase());

            //write data
            if(header){
                writer.writeHeader(variables);
            }

            if(exportScoredData){
                writer.writeScoredDatabase(rs, variables);
            }else{
                writer.writeDatabase(rs, variables);
            }
            rs.close();
            stmt.close();
            writer.close();
        }catch(IOException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new IOException(ex);
        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex);
        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    protected String doInBackground()throws Exception{
        try{
            logger.info(command.paste());
            this.firePropertyChange("status", "", "Exporting data...");
            this.firePropertyChange("progress-ind-on", null, null);

            this.parseCommand();
            this.getVariables();
            this.exportDelimitedFile();
            logger.info("Export complete: " + timer.getElapsedTime());

        }catch(Throwable t){
            theException = t;
        }
        return "";
    }

    @Override
    protected void done(){
        if(theException==null){
            this.firePropertyChange("status", "", "Ready");//will display status in statusBar
        }else{
            logger.fatal(theException.getMessage(), theException);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
        firePropertyChange("progress-off", null, null);
    }



}

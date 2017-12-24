package com.itemanalysis.jmetrik.dao;

import au.com.bytecode.opencsv.CSVWriter;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.ExportSpssCommand;
import com.itemanalysis.jmetrik.workspace.JmetrikCsvWriter;
import com.itemanalysis.psychometrics.data.*;
import com.itemanalysis.psychometrics.tools.StopWatch;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

/**
 * Exports a database table as a IBM SPSS *.sav file. It requires the user to have
 * licensed copy of IBM SPSS. It works by creating a temporary CSV file. Then,
 * it uses the spssjavaplugin.jar to process SPSS commands for importing a CSV file.
 *
 *
 */
public class SpssFileExporter extends SwingWorker<String,Void>{

    private File spssJavaPlugin = null;

    private File spssSavFile = null;

    private boolean useQuotes = false;

    private DataTableName tableName = null;

    private Connection conn = null;

    private ExportSpssCommand command = null;

    private boolean exportScoredData = false;

    private StopWatch timer = null;

    private double maxProgress = 0;

    private int progressValue = 0;

    private Throwable theException = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    public SpssFileExporter(Connection conn, ExportSpssCommand command){
        this.conn = conn;
        this.command = command;
    }

    private void initializeProgress(int nrow) {
        maxProgress = (double)nrow;
    }

    private void updateProgress(int lineNumber){
        progressValue=(int)((100*((double)lineNumber+1.0))/ maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    private void parseCommand(){
        logger.info(command.paste());
        tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
        String fileName = command.getFreeOption("file").getString();
        spssSavFile = new File(fileName);

        exportScoredData = command.getSelectAllOption("options").isArgumentSelected("scored");

        String pathToPlugin = command.getFreeOption("pluginpath").getString();
        spssJavaPlugin = new File(pathToPlugin + "/" + "spssjavaplugin.jar");

    }

    private void exportToSpssFile()throws Exception{
        Statement stmt = null;
        ResultSet rs = null;
        JmetrikCsvWriter writer = null;
        Class statsUtil = null;

        try{
            //Check for SPSS plugin
            if(!spssJavaPlugin.exists()){
                theException = new FileNotFoundException("File not found: " + spssJavaPlugin.getAbsolutePath());
                return;
            }

            File tempFile = File.createTempFile("jmk-spss-export-temp", ".csv");
            tempFile.deleteOnExit();

            if(useQuotes){
                writer = new JmetrikCsvWriter(new FileWriter(tempFile), ',');
            }else{
                writer = new JmetrikCsvWriter(new FileWriter(tempFile), ',', CSVWriter.NO_QUOTE_CHARACTER);
            }

            //Get variable attrobutes
            JmetrikDatabaseFactory dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
            DatabaseAccessObject dao = dbFactory.getDatabaseAccessObject();
            ArrayList<VariableAttributes> variables = dao.getAllVariables(conn, new VariableTableName(tableName.getTableName()));

            //Create a result set for reading from database
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery("SELECT * FROM " + tableName.getNameForDatabase());

            if(exportScoredData){
                writer.writeScoredDatabase(rs, variables);
            }else{
                writer.writeDatabase(rs, variables);
            }
            writer.close();
            writer=null;

            //Start a new spss file
            //String spssCommand = getDataListString(variables);
            String spssCommand = "GET DATA\n" +
                    "/TYPE=TXT\n" +
                    "/FILE='" + tempFile.getAbsolutePath() + "'\n" +
                    "/DELIMITERS=','\n" +
                    "/FIRSTCASE = 1\n" +
                    "/VARIABLES = \n" +
                    getVariableListString(variables) + ".\n" +
                    "EXECUTE.";

            //load StatsUtil class and start spss session
            URLClassLoader child = new URLClassLoader (new URL[]{spssJavaPlugin.toURI().toURL()}, this.getClass().getClassLoader());
            statsUtil = Class.forName ("com.ibm.statistics.plugin.StatsUtil", true, child);
            Method startMethod = statsUtil.getDeclaredMethod ("start");
            startMethod.invoke(null);

            Method submitMethod = statsUtil.getDeclaredMethod ("submit", String.class);
            submitMethod.invoke(null, spssCommand);

            spssCommand = "VARIABLE LABELS\n" + getVariableLabelsString(variables) + ".";
            submitMethod.invoke(null, spssCommand);

            spssCommand = "SAVE OUTFILE=\"" + spssSavFile.getAbsolutePath() + "\".";
            submitMethod.invoke(null, spssCommand);


        }catch(InvocationTargetException ex){
            logger.fatal(ex.getMessage(), ex);
            throw ex;
        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            throw ex;
        }finally{
            if(null!=rs) rs.close();
            if(stmt!=null) stmt.close();
            if(null!=writer) writer.close();

            //Stop spss processor
            if(null!=statsUtil){
                Method stopMethod = statsUtil.getDeclaredMethod ("stop");
                Object stopResult = stopMethod.invoke(null);
            }
        }

    }

    private String getVariableListString(ArrayList<VariableAttributes> variables){
        StringBuilder sb = new StringBuilder();

        for(VariableAttributes v : variables){
            sb.append(v.getName().toString() + " AUTO\n");
        }
        return sb.toString();
    }

    private String getVariableLabelsString(ArrayList<VariableAttributes> variables){
        StringBuilder sb = new StringBuilder();

        for(VariableAttributes v : variables){
            sb.append(v.getName().toString() + " '" + v.getLabel().toString() + "'\n");
        }
        return sb.toString();
    }

    protected String doInBackground() throws Exception{
        timer = new StopWatch();
        try{
            this.firePropertyChange("status", "", "Exporting as SPSS file...");
            this.firePropertyChange("progress-ind-on", null, null);
            parseCommand();
            exportToSpssFile();
            logger.info("Export complete: " + timer.getElapsedTime());
        }catch(Throwable t){
            theException=t;
        }
        return "";
    }

    protected void done(){
        if(theException==null){
            this.firePropertyChange("status", "", "Ready");
            scriptLogger.info(command.paste());
        }else{
            logger.fatal(theException.getMessage(), theException);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
        firePropertyChange("progress-off", null, null);
    }


}

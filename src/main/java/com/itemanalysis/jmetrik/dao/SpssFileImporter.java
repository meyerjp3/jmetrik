package com.itemanalysis.jmetrik.dao;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.ImportSPSSCommand;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableLabel;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.tools.StopWatch;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;

/**
 * This class uses Java reflections to import an SPSS *.sav file into a derby database table.
 * It requires that the end user have a licensed copy of SPSS installed on their computer.
 * Reflections are needed to access the classes in the SPSS Java plugin (spssjavaplugin.jar).
 *
 * The spssjavaplugin.jar file is stored in a different location depending on the user's
 * operating system. The location of the plugin is provided in teh ImportSPSScommand by
 * the end user.
 *
 * On Mac OSX:
 * spssjavaplugin.jar is located in the bin directory under the Content directory in the
 * IBM SPSS Statistics application bundle.
 *
 * On Linux and UNIX:is located in the bin directory under the IBM SPSS Statistics installation directory
 * spssjavaplugin.jar
 *
 * Requirements:
 * 1. This program must be installed in the same location as jMetrik.
 * 2. This program requires that jMetrik be running with an open database. Data will be inported into the
 *    open database.
 *
 */
public class SpssFileImporter extends SwingWorker<String,Void> {

    private File spssJavaPlugin = null;

    private File spssSavFile = null;

    private Connection conn = null;

    private ImportSPSSCommand command = null;

    private String tableNameString = "";

    private String description = "";

    private boolean valueLabels = false;

    private DataTableName dataTableName = null;

    private VariableTableName variableTableName = null;

    private StopWatch timer = null;

    private boolean tablesCreated = false;

    private boolean display = true;

    private double maxProgress = 0;

    private int progressValue = 0;

    private Throwable theException = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");


    public SpssFileImporter(Connection conn, ImportSPSSCommand command){
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
        String fileName = command.getFreeOption("file").getString();
        spssSavFile = new File(fileName);

        String pathToPlugin = command.getFreeOption("pluginpath").getString();
        spssJavaPlugin = new File(pathToPlugin + "/" + "spssjavaplugin.jar");

        tableNameString = command.getPairedOptionList("data").getStringAt("table");
        display = command.getSelectAllOption("options").isArgumentSelected("display");
        description = command.getFreeOption("description").getString();
        if(null==description){
            description = "Import of " + spssSavFile.getAbsolutePath();
        }

        valueLabels = command.getSelectOneOption("use").isValueSelected("vlabels");

    }

    /**
     * Imports an SPSS file using teh original data values. Value labels are ignored.
     *
     * @throws Exception
     */
    private void importSpssFile()throws Exception{
        PreparedStatement pstmt = null;
        Class statsUtil = null;

        try{

            //Check for SPSS plugin
            if(!spssJavaPlugin.exists()){
                theException = new FileNotFoundException("File not found: " + spssJavaPlugin.getAbsolutePath());
                return;
            }

            URLClassLoader child = new URLClassLoader (new URL[]{spssJavaPlugin.toURI().toURL()}, this.getClass().getClassLoader());

            statsUtil = Class.forName ("com.ibm.statistics.plugin.StatsUtil", true, child);
            Method method = statsUtil.getDeclaredMethod ("start");
            Object result = method.invoke(null);

            //Get the spss file
            String spssCommand = "GET FILE='" + spssSavFile.getAbsolutePath() + "'.";
            method = statsUtil.getDeclaredMethod ("submit", String.class);
            result = method.invoke(null, spssCommand);

            Class spssCursor = Class.forName("com.ibm.statistics.plugin.Cursor", true, child);
            Constructor c = spssCursor.getConstructor(String.class, boolean.class); //new Cursor("r", true);
            Object cursorObject = c.newInstance("r", true);

            method = spssCursor.getDeclaredMethod("getCaseCount");
            int nrow = ((Integer)method.invoke(cursorObject, null)).intValue();

            method = spssCursor.getDeclaredMethod("getVariableCount");
            int ncol = ((Integer)method.invoke(cursorObject, null)).intValue();

            Method variableNameMethod = statsUtil.getDeclaredMethod ("getVariableName", int.class);
            Method variableLabelMethod = statsUtil.getDeclaredMethod ("getVariableLabel", int.class);
            Method variableTypeMethod = statsUtil.getDeclaredMethod ("getVariableType", int.class);

            initializeProgress(nrow);
            this.firePropertyChange("progress-on", null, null);


            //Convert variables
            VariableName vName = null;
            VariableLabel vLabel = null;
            DataType dataType = null;
            VariableAttributes tempAttributes = null;
            ArrayList<VariableAttributes> variables = new ArrayList<VariableAttributes>();

            Class caseClass = Class.forName("com.ibm.statistics.plugin.Case", true, child);
            Method getCellValueFormatMethod = caseClass.getDeclaredMethod ("getCellValueFormat", int.class);

            Method fetchCasesMethod = spssCursor.getDeclaredMethod("fetchCases", int.class);
            Object caseArray = Array.newInstance(caseClass, 1);
            Object caseArrayElement = null;

            //get first case to determine variable format
            caseArray = fetchCasesMethod.invoke(cursorObject, 1);
            caseArrayElement = Array.get(caseArray, 0);
            Enum cellValueFormat = null;

            for(int j=0;j<ncol;j++){

                //Variable name
                result = variableNameMethod.invoke(cursorObject, j);
                vName = new VariableName((String)result);

                //Variable label
                result = variableLabelMethod.invoke(cursorObject, j);
                vLabel = new VariableLabel((String)result);

                //Variable type (either numeric or string)
                cellValueFormat = (Enum)getCellValueFormatMethod.invoke(caseArrayElement, j);

                if("DOUBLE".equals(cellValueFormat.toString())){
                    dataType = DataType.DOUBLE;
                }else if("STRING".equals(cellValueFormat.toString())){
                    dataType = DataType.STRING;
                }else{
                    dataType = DataType.STRING;
                }

                tempAttributes = new VariableAttributes(vName, vLabel, dataType, j);
                variables.add(tempAttributes);

            }

            //reset the SPSS cursor
            Method resetCursorMethod = spssCursor.getDeclaredMethod("reset");
            resetCursorMethod.invoke(cursorObject, null);

            //Open database transaction
            conn.setAutoCommit(false);

            //Create database tables
            //get variable info for all variables in table
            JmetrikDatabaseFactory dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
            DatabaseAccessObject dbDao = dbFactory.getDatabaseAccessObject();

            //get unique table name
            dataTableName = dbDao.getUniqueTableName(conn, tableNameString);
            variableTableName = new VariableTableName(dataTableName.toString());

            //Create database tables
            dbDao.createTables(conn, dataTableName, variableTableName, variables);
            pstmt = conn.prepareStatement(getInsertString(ncol));

            //Convert data
            Method getDoubleCellValueMethod = caseClass.getDeclaredMethod ("getDoubleCellValue", int.class);
            Method getStringCellValueMethod = caseClass.getDeclaredMethod ("getStringCellValue", int.class);
            Method getDateCellValueMethod = caseClass.getDeclaredMethod ("getDateCellValue", int.class);

            Double numvar;
            String strvar;
            Calendar datevar = null;

            for(int i=0;i<nrow;i++){
                caseArray = fetchCasesMethod.invoke(cursorObject, 1);
                caseArrayElement = Array.get(caseArray, 0);

                for(int j=0;j<ncol;j++){
                    cellValueFormat = (Enum)getCellValueFormatMethod.invoke(caseArrayElement, j);

                    if("DOUBLE".equals(cellValueFormat.toString())){
                        numvar = (Double)getDoubleCellValueMethod.invoke(caseArrayElement, j);
                        if(null==numvar){
                            pstmt.setNull(j+1, Types.DOUBLE);
                        }else{
                            pstmt.setDouble(j+1, numvar);
                        }

                    }else if("STRING".equals(cellValueFormat.toString())){
                        strvar = (String)getStringCellValueMethod.invoke(caseArrayElement, j);
                        if(null==strvar){
                            pstmt.setNull(j+1, Types.VARCHAR);
                        }else{
                            pstmt.setString(j+1, strvar);
                        }

                    }else{
                        datevar = (Calendar)getDateCellValueMethod.invoke(caseArrayElement, j);
                        if(null==datevar){
                            pstmt.setNull(j+1, Types.VARCHAR);
                        }else{
                            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                            String date1 = format1.format(datevar.getTime());
                            pstmt.setString(j+1, date1);
                        }
                    }


                }

                pstmt.executeUpdate();
                updateProgress(i+1);
            }



            setTableInformation();


        }catch(Exception ex){
            conn.rollback();
            logger.fatal(ex.getMessage(), ex);
            throw new Exception(ex.getMessage());

        }finally{

            //Stop spss processor
            if(null!=statsUtil){
                Method stopMethod = statsUtil.getDeclaredMethod ("stop");
                Object stopResult = stopMethod.invoke(null);
            }

            if(null!=pstmt) pstmt.close();

            //Commit database and close transaction
            conn.commit();
            conn.setAutoCommit(true);
        }


    }

    /**
     * Imports an SPSS file and uses value labels for a variable when defined in the SPSS file.
     * Numeric and String variables are converted to value labels where defined. The result is a string variable.
     * Date variables are not converted to value labels even if they are defined in teh SPSS file.
     * @throws Exception
     */
    private void importSpssFileValueLabels()throws Exception{
        PreparedStatement pstmt = null;
        Class statsUtil = null;

        try{

            //Check for SPSS plugin
            if(!spssJavaPlugin.exists()){
                theException = new FileNotFoundException("File not found: " + spssJavaPlugin.getAbsolutePath());
                return;
            }

            URLClassLoader child = new URLClassLoader (new URL[]{spssJavaPlugin.toURI().toURL()}, this.getClass().getClassLoader());

            statsUtil = Class.forName ("com.ibm.statistics.plugin.StatsUtil", true, child);
            Method method = statsUtil.getDeclaredMethod ("start");
            Object result = method.invoke(null);

            //Get the spss file
            String spssCommand = "GET FILE='" + spssSavFile.getAbsolutePath() + "'.";
            method = statsUtil.getDeclaredMethod ("submit", String.class);
            result = method.invoke(null, spssCommand);

            Class spssCursor = Class.forName("com.ibm.statistics.plugin.Cursor", true, child);
            Constructor c = spssCursor.getConstructor(String.class, boolean.class); //new Cursor("r", true);
            Object cursorObject = c.newInstance("r", true);

            method = spssCursor.getDeclaredMethod("getCaseCount");
            int nrow = ((Integer)method.invoke(cursorObject, null)).intValue();

            method = spssCursor.getDeclaredMethod("getVariableCount");
            int ncol = ((Integer)method.invoke(cursorObject, null)).intValue();

            Method variableNameMethod = statsUtil.getDeclaredMethod ("getVariableName", int.class);
            Method variableLabelMethod = statsUtil.getDeclaredMethod ("getVariableLabel", int.class);

            initializeProgress(nrow);
            this.firePropertyChange("progress-on", null, null);


            //Convert variables
            VariableName vName = null;
            VariableLabel vLabel = null;
            DataType dataType = null;
            VariableAttributes tempAttributes = null;
            ArrayList<VariableAttributes> variables = new ArrayList<VariableAttributes>();

            Class caseClass = Class.forName("com.ibm.statistics.plugin.Case", true, child);
            Method getCellValueFormatMethod = caseClass.getDeclaredMethod ("getCellValueFormat", int.class);

            Method fetchCasesMethod = spssCursor.getDeclaredMethod("fetchCases", int.class);
            Object caseArray = Array.newInstance(caseClass, 1);
            Object caseArrayElement = null;

            Method getNumericValueLabelsMethod = spssCursor.getDeclaredMethod("getNumericValueLabels", int.class);
            Method getStringValueLabelsMethod = spssCursor.getDeclaredMethod("getStringValueLabels", int.class);

            //get first case to determine variable format
            caseArray = fetchCasesMethod.invoke(cursorObject, 1);
            caseArrayElement = Array.get(caseArray, 0);
            Enum cellValueFormat = null;

            Map<Double,String> numericLabelMap = null;
            Map<String,String> stringLabelMap = null;

            for(int j=0;j<ncol;j++){

                //Variable name
                result = variableNameMethod.invoke(cursorObject, j);
                vName = new VariableName((String)result);

                //Variable label
                result = variableLabelMethod.invoke(cursorObject, j);
                vLabel = new VariableLabel((String)result);

                //Variable type (either numeric or string)
                cellValueFormat = (Enum)getCellValueFormatMethod.invoke(caseArrayElement, j);

                if("DOUBLE".equals(cellValueFormat.toString())){
                    dataType = DataType.DOUBLE;

                    //If variable contains value labels, make it a string variable
                    numericLabelMap = (Map<Double,String>)getNumericValueLabelsMethod.invoke(cursorObject, j);
                    if(!numericLabelMap.isEmpty()) dataType = DataType.STRING;

                }else if("STRING".equals(cellValueFormat.toString())){
                    dataType = DataType.STRING;

                    Map<String,String> ttt = (Map<String,String>)getStringValueLabelsMethod.invoke(cursorObject, j);
                    if(ttt.isEmpty())System.out.println(vName);

                }else{
                    dataType = DataType.STRING;
                }

                tempAttributes = new VariableAttributes(vName, vLabel, dataType, j);
                variables.add(tempAttributes);

            }

            //reset the SPSS cursor
            Method resetCursorMethod = spssCursor.getDeclaredMethod("reset");
            resetCursorMethod.invoke(cursorObject, null);

            //Open database transaction
            conn.setAutoCommit(false);

            //Create database tables
            //get variable info for all variables in table
            JmetrikDatabaseFactory dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
            DatabaseAccessObject dbDao = dbFactory.getDatabaseAccessObject();

            //get unique table name
            dataTableName = dbDao.getUniqueTableName(conn, tableNameString);
            variableTableName = new VariableTableName(dataTableName.toString());

            //Create database tables
            dbDao.createTables(conn, dataTableName, variableTableName, variables);
            pstmt = conn.prepareStatement(getInsertString(ncol));

            //Convert data
            Method getDoubleCellValueMethod = caseClass.getDeclaredMethod ("getDoubleCellValue", int.class);
            Method getStringCellValueMethod = caseClass.getDeclaredMethod ("getStringCellValue", int.class);
            Method getDateCellValueMethod = caseClass.getDeclaredMethod ("getDateCellValue", int.class);

            Double numvar;
            String strvar;
            Calendar datevar = null;

            for(int i=0;i<nrow;i++){
                caseArray = fetchCasesMethod.invoke(cursorObject, 1);
                caseArrayElement = Array.get(caseArray, 0);

                for(int j=0;j<ncol;j++){
                    cellValueFormat = (Enum)getCellValueFormatMethod.invoke(caseArrayElement, j);

                    //Convert double variables that have value labels
                    if("DOUBLE".equals(cellValueFormat.toString())){
                        numericLabelMap = (Map<Double,String>)getNumericValueLabelsMethod.invoke(cursorObject, j);
                        numvar = (Double)getDoubleCellValueMethod.invoke(caseArrayElement, j);

                        if(null==numvar){
                            pstmt.setNull(j+1, Types.DOUBLE);
                        }else{
                            //save original value if value labels are not available for this variable
                            if(numericLabelMap.isEmpty()){
                                pstmt.setDouble(j+1, numvar);
                            }
                            //save value label
                            else{
                                String temp = numericLabelMap.get(numvar);
                                if(null==temp){
                                    //No associated label for this value. Keep original value.
                                    pstmt.setString(j+1, Double.valueOf(numvar).toString());
                                }else{
                                    //Get associated value label.
                                    pstmt.setString(j+1, numericLabelMap.get(numvar));
                                }

                            }

                        }

                    }else if("STRING".equals(cellValueFormat.toString())){
                        stringLabelMap = (Map<String,String>)getStringValueLabelsMethod.invoke(cursorObject, j);
                        strvar = ((String)getStringCellValueMethod.invoke(caseArrayElement, j)).trim();
                        if(null==strvar){
                            pstmt.setNull(j+1, Types.VARCHAR);
                        }else{
                            //save original value if value labels are not available for this variable
                            if(stringLabelMap.isEmpty()){
                                pstmt.setString(j+1, strvar);
                            }
                            //save value label
                            else{
                                String temp = stringLabelMap.get(strvar);
                                if(null==temp){
                                    //No associated label for this value. Keep original value.
                                    pstmt.setString(j+1, strvar);
                                }else{
                                    //Get associated value label.
                                    pstmt.setString(j+1, stringLabelMap.get(strvar));
                                }

                            }
                        }

                    }else{
                        datevar = (Calendar)getDateCellValueMethod.invoke(caseArrayElement, j);
                        if(null==datevar){
                            pstmt.setNull(j+1, Types.VARCHAR);
                        }else{
                            SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                            String date1 = format1.format(datevar.getTime());
                            pstmt.setString(j+1, date1);
                        }
                    }


                }

                pstmt.executeUpdate();
                updateProgress(i+1);
            }



            setTableInformation();


        }catch(InvocationTargetException ex){
            ex.printStackTrace();
        }
        catch(Exception ex){
            conn.rollback();
            logger.fatal(ex.getMessage(), ex);
            throw new Exception(ex.getMessage());

        }finally{

            //Stop spss processor
            if(null!=statsUtil){
                Method stopMethod = statsUtil.getDeclaredMethod ("stop");
                Object stopResult = stopMethod.invoke(null);
            }

            if(null!=pstmt) pstmt.close();

            //Commit database and close transaction
            conn.commit();
            conn.setAutoCommit(true);
        }


    }

    private String getInsertString(int ncol){

        String updateString = "INSERT INTO " + dataTableName.getNameForDatabase() + " VALUES(";
        for(int i=0;i<ncol;i++){
            updateString+= "?";
            if(i<ncol-1){
                updateString+=",";
            }else{
                updateString+=")";
            }
        }
        return updateString;
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
                System.out.println(spssSavFile.getAbsolutePath());
                description = "Import of " + spssSavFile.getAbsolutePath();
            }

            DerbyDatabaseAccessObject dao = dao = new DerbyDatabaseAccessObject();
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
            this.firePropertyChange("status", "", "Importing file...");
            this.firePropertyChange("progress-ind-on", null, null);
            parseCommand();

            if(valueLabels){
                importSpssFileValueLabels();
            }else{
                importSpssFile();
            }

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

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

package com.itemanalysis.jmetrik.stats.cmh;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import javax.swing.SwingWorker;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.cmh.CochranMantelHaenszel;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;


public class CmhAnalysis extends SwingWorker<String,Void> {

    private CmhCommand command = null;
    private TreeMap<Integer, CochranMantelHaenszel> cmhTreeMap = null;
    private JmetrikTextFile textFile = null;
    private Throwable theException = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private StopWatch sw = null;
    private double maxProgress = 0.0;
    private int progressValue=0;
    private int lineNumber=0;
    private VariableAttributes groupVar = null;
    private VariableAttributes matchVar = null;
    private String focalCode = null;
    private String referenceCode = null;
    private boolean etsDelta = false;
    private String dString = "";
    private ArrayList<VariableAttributes> variables = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    boolean tables = false;
    private DataTableName tableName = null;
    private String outputTableString = "";
    private String outputDb = "";
    private DataTableName outputTable = null;
    private boolean tableCreated = false;
    private boolean saveOutput = false;
    private boolean scoreAsZero = true;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    public CmhAnalysis(Connection conn, DatabaseAccessObject dao, CmhCommand command, JmetrikTextFile textFile){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.textFile = textFile;
        cmhTreeMap = new TreeMap<Integer, CochranMantelHaenszel>();
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/ maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    private void initialize()throws IllegalArgumentException{

        focalCode = command.getPairedOptionList("codes").getStringAt("focal");
        referenceCode = command.getPairedOptionList("codes").getStringAt("reference");

        //convert focal and reference codes to double format is they are numbers
        double focDouble = 0;
        double refDouble = 0;
        try{
            focDouble = Double.parseDouble(focalCode);
            focalCode = Double.valueOf(focDouble).toString();
        }catch(NumberFormatException ex){
            //data is string
        }
        try{
            refDouble = Double.parseDouble(referenceCode);
            referenceCode = Double.valueOf(refDouble).toString();
        }catch(NumberFormatException ex){
            //data is string
        }


        tables = command.getSelectAllOption("options").isArgumentSelected("tables");
        scoreAsZero = command.getSelectAllOption("options").isArgumentSelected("zero");
        etsDelta = command.getSelectOneOption("effectsize").isValueSelected("ets");
        dString = command.getDataString();

        saveOutput = command.getPairedOptionList("output").hasValue();
        if(saveOutput){
            outputDb = command.getPairedOptionList("output").getStringAt("db");
            outputTableString = command.getPairedOptionList("output").getStringAt("table");
        }

        //add items to tree map
        CochranMantelHaenszel tempItem;
        for(VariableAttributes v : variables){
            tempItem = cmhTreeMap.get(v.positionInDb());
            if(tempItem==null){
                tempItem = new CochranMantelHaenszel(focalCode, referenceCode, groupVar, v, etsDelta);
                cmhTreeMap.put(v.positionInDb(), tempItem);
            }
        }

    }

    public void summarize()throws IllegalArgumentException, SQLException{
        Object response;
        Object tempGroup;
        CochranMantelHaenszel tempItem;
        double matchingScore = 0.0;
        double itemScore = 0.0;

        Statement stmt;
        ResultSet rs;

        //set progress bar information
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

        //create query for all variables
        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        for(VariableAttributes v : variables){
            select.addColumn(sqlTable, v.getName().nameForDatabase());
        }
        select.addColumn(sqlTable, groupVar.getName().nameForDatabase());
        select.addColumn(sqlTable, matchVar.getName().nameForDatabase());

        //execute query
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs=stmt.executeQuery(select.toString());

        //loop over examinees
        while(rs.next()){

            //get value for group
            if(groupVar.getType().getDataType()== DataType.DOUBLE){
                tempGroup = rs.getDouble(groupVar.getName().nameForDatabase());
            }else{
                tempGroup = rs.getString(groupVar.getName().nameForDatabase());
            }

            //increment cmh object only if groupvar and matchvar != null
            if(!rs.wasNull()){
                //get matching variable score
                matchingScore = rs.getDouble(matchVar.getName().nameForDatabase());
                if(!rs.wasNull()){
                    //loop over items and increment dif analysis
                    for(VariableAttributes v : variables){
                        tempItem = cmhTreeMap.get(v.positionInDb());
                        response = rs.getObject(v.getName().nameForDatabase());
                        itemScore = v.getItemScoring().computeItemScore(response);
                        tempItem.increment(matchingScore, tempGroup.toString(), itemScore);
                    }
                }
            }

            updateProgress();

        }//end loop over examinees

        rs.close();
        stmt.close();

    }

    public String getResults(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        Set<Integer> keys = cmhTreeMap.keySet();
        Iterator<Integer> iter = keys.iterator();
        CochranMantelHaenszel temp = null;

        int i=0;
        int outputMidpoint = 37;
        String s1 = String.format("%1$tB %1$te, %1$tY  %tT", Calendar.getInstance());
        int len = outputMidpoint+Double.valueOf(Math.floor(Double.valueOf(s1.length()).doubleValue()/2.0)).intValue();
        int len2 = outputMidpoint+Double.valueOf(Math.floor(Double.valueOf(dString.length()).doubleValue()/2.0)).intValue();

        f.format("%43s", "DIF ANALYSIS"); f.format("%n");
        f.format("%" + len2 + "s", dString); f.format("%n");
        f.format("%" + len + "s", s1); f.format("%n");
        f.format("%-80s","================================================================================");
        f.format("%n");
        f.format("%n");

        while(iter.hasNext()){
            temp = cmhTreeMap.get(iter.next());
            if(tables){
                sb.append(temp.printTables()); f.format("%n"); f.format("%n");
            }
            if(i==0 || tables) f.format("%-120s", temp.printHeader());
            f.format("%-120s", temp.toString()); f.format("%n");
            i++;
        }
        f.format("%n");
        f.format("%n");

        f.format("%-25s",     "              Options ");f.format("%n");
        f.format("%-25s", "-----------------------------------");f.format("%n");
        f.format("%20s", "Matching Variable: "); f.format("%-20s", matchVar.getName()); f.format("%n");
        f.format("%20s", "DIF Group Variable: "); f.format("%-20s", groupVar.getName()); f.format("%n");
        f.format("%18s", "Focal Group Code: "); f.format("%-10s", focalCode); f.format("%n");
        f.format("%22s", "Reference Group Code: "); f.format("%-10s", referenceCode); f.format("%n");
        f.format("%n");

        return f.toString();
    }

    protected String doInBackground() {
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running DIF Analysis...");
        this.firePropertyChange("progress-on", null, null);
        try{
            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            VariableTableName variableTableName = new VariableTableName(tableName.toString());
            ArrayList<String> selectVariable = command.getFreeOptionList("variables").getString();
            variables = dao.getSelectedVariables(conn, variableTableName, selectVariable);

            //get grouping variable info
            if(command.getFreeOption("groupvar").hasValue()){
                String groupByName=command.getFreeOption("groupvar").getString();
                groupVar = dao.getVariableAttributes(conn, new VariableTableName(tableName.toString()), groupByName);
            }

            //get matching variable info
            String matchVarString = command.getFreeOption("matchvar").getString();
            matchVar = dao.getVariableAttributes(conn, variableTableName, matchVarString);

            initialize();
            summarize();

            if(saveOutput){
                firePropertyChange("status", "", "Saving output...");
                this.firePropertyChange("progress-ind-on", null, null);
                outputTable = dao.getUniqueTableName(conn, outputTableString);
                CmhOutputTable dbOutput = new CmhOutputTable(conn, tableName, outputTable);
                dbOutput.saveOutput(cmhTreeMap);
                tableCreated = true;
            }

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible

        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return getResults();
    }

    @Override
    protected void done(){
        try{
            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }else{
                if(tableCreated) this.firePropertyChange("table-added", "", outputTable);//will addArgument node to tree
                textFile.addText(get());
                textFile.addText("Elapsed time: " + sw.getElapsedTime());
                textFile.setCaretPosition(0);
                scriptLogger.info(command.paste());
            }

        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }

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

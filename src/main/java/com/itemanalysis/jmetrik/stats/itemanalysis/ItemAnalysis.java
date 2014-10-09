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

package com.itemanalysis.jmetrik.stats.itemanalysis;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.measurement.ClassicalItem;
import com.itemanalysis.psychometrics.measurement.TestSummary;
import com.itemanalysis.psychometrics.scaling.RawScore;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.stat.Frequency;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.TreeMap;

public class ItemAnalysis extends SwingWorker<String,Void> {

    private ItemAnalysisCommand command = null;
    private TreeMap<Integer, ClassicalItem> item = null;
    private JmetrikTextFile textFile = null;
    private Throwable theException = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private StopWatch sw = null;
    private TestSummary testSummary = null;
    private ArrayList<VariableInfo> variables = null;
    private boolean showItemStats = true;
    private boolean unbiased = false;
    private boolean pearsonCorrelation = true;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    private DataTableName tableName = null;
    private int progressValue = 0;
    private int lineNumber = 0;
    private double maxProgress = 100.0;
    private boolean saveOutput = false;
    private String outputTableString = "";
    private DataTableName outputTable = null;

    private boolean biasCorrection = false;
    private boolean allCategories = false;
    private boolean listwiseDeletion = false;
    private boolean showCsem = false;
    private ArrayList<Integer> cutScores = null;
    private int numberOfItems = 0;
    private boolean deletedReliability = false;
    private boolean tableCreated = false;

    public ItemAnalysis(Connection conn, DatabaseAccessObject dao, ItemAnalysisCommand command, JmetrikTextFile textFile){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.textFile = textFile;
        item = new TreeMap<Integer, ClassicalItem>();
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void initializeProgress()throws SQLException {
        int nrow = dao.getRowCount(conn, tableName);
        maxProgress = (double)nrow;
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/ maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    private void processCommand()throws IllegalArgumentException{

        numberOfItems = command.getFreeOptionList("variables").getNumberOfValues();
        deletedReliability = command.getSelectAllOption("options").isArgumentSelected("delrel");
        biasCorrection = command.getSelectAllOption("options").isArgumentSelected("spur");
        unbiased = command.getSelectAllOption("options").isArgumentSelected("unbiased");
        allCategories = command.getSelectAllOption("options").isArgumentSelected("all");
        listwiseDeletion = command.getSelectOneOption("missing").isValueSelected("listwise");
        showCsem = command.getSelectAllOption("options").isArgumentSelected("csem");
        showItemStats = command.getSelectAllOption("options").isArgumentSelected("istats");
        pearsonCorrelation = command.getSelectOneOption("correlation").isValueSelected("pearson");
        saveOutput = command.getPairedOptionList("output").hasValue();
        outputTableString = command.getPairedOptionList("output").getStringAt("table");

        if(command.getFreeOptionList("cut").getNumberOfValues()>0){
            cutScores = command.getFreeOptionList("cut").getInteger();
        }


    }

    public void summarize()throws IllegalArgumentException, SQLException {
        Statement stmt = null;
        ResultSet rs=null;
        int missingCount = 0;


        int numberOfSubscales = this.numberOfSubscales();
//        numberOfItems = command.getFreeOptionList("variables").getNumberOfValues();
//
//        deletedReliability = command.getSelectAllOption("options").isArgumentSelected("delrel");
//        biasCorrection = command.getSelectAllOption("options").isArgumentSelected("spur");
//        unbiased = command.getSelectAllOption("options").isArgumentSelected("unbiased");
//        allCategories = command.getSelectAllOption("options").isArgumentSelected("all");
//        listwiseDeletion = command.getSelectOneOption("missing").isArgumentSelected("listwise");
//        showCsem = command.getSelectAllOption("options").isArgumentSelected("csem");
//        showItemStats = command.getSelectAllOption("options").isArgumentSelected("istats");
//        pearsonCorrelation = command.getSelectOneOption("correlation").isArgumentSelected("pearson");

//        if(command.getFreeOptionList("cut").getNumberOfValues()>0){
//            cutScores = command.getFreeOptionList("cut").getInteger();
//        }

        try{
            //connect to db
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(VariableInfo v : variables){
                select.addColumn(sqlTable, v.getName().nameForDatabase());
            }
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            //create test summary object
            testSummary = new TestSummary(
                    numberOfItems,
                    numberOfSubscales,
                    cutScores,
                    variables,
                    unbiased,
                    deletedReliability,
                    showCsem);

            Object response = null;
            Double responseScore=null;
            RawScore rawScore = null;
            ClassicalItem tempItem = null;
            int[] resposneVectorIndex = null;
            Object[] responseVector = null;
            Double[] scoreVector = null;

            //loop over examinees
            while(rs.next()){

                //loop over items to compute RawScore
                rawScore = new RawScore(numberOfItems);

                missingCount = 0;
                for(VariableInfo v : variables){
                    tempItem = item.get(v.positionInDb());
                    if(tempItem==null){
                        tempItem = new ClassicalItem(v, biasCorrection, allCategories, pearsonCorrelation);
                        item.put(v.positionInDb(), tempItem);
                    }
                    response = rs.getObject(v.getName().nameForDatabase());

                    //count missing responses per examinee
                    if(response==null || response.equals("")){//FIXME need to allow a space " " or other special codes to be viewed as missing data
                        missingCount++;
                    }
                    responseScore = v.getItemScoring().computeItemScore(response, v.getType());
                    rawScore.increment(responseScore);
                    rawScore.incrementResponseVector(v.positionInDb(), response, responseScore);
                    rawScore.incrementSubScaleScore(v.getSubscale(), responseScore);

                }

                //only use complete cases if listwise deletion is specified
                //otherwise a missing item response is scored as 0

                if((listwiseDeletion && missingCount==0) || !listwiseDeletion){
//                    System.out.println("TEST: " + listwiseDeletion + " " + (missingCount==0) + " " + (listwiseDeletion && missingCount==0));
                    testSummary.increment(rawScore);

                    if(numberOfSubscales>1) testSummary.incrementPartTestReliability(rawScore);

                    //loop over items to compute item analysis
                    responseVector = rawScore.getResponseVector();
                    resposneVectorIndex = rawScore.getResponseVectorIndex();
                    scoreVector = rawScore.getScoreVector();

                    for(int i=0;i<responseVector.length;i++){
                        if(showItemStats){
                            item.get(resposneVectorIndex[i]).increment(rawScore, responseVector[i]);
                        }
                        for(int j=i;j<responseVector.length;j++){
                            testSummary.incrementReliability(i, j, scoreVector[i], scoreVector[j]);
                        }
                    }

                }
                updateProgress();
            }//end loop over examinees
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
            conn.setAutoCommit(true);
        }



    }

    public int numberOfSubscales(){
        Frequency table = new Frequency();
        for(VariableInfo v : variables){
            table.addValue(v.getSubscale());
        }
        return table.getUniqueCount();
    }

    public int numberOfItemsInSubScaleAt(String subscaleKey){
        int items = 0;
        for(VariableInfo v : variables){
            if(v.getSubscale().equals(subscaleKey)){
                items++;
            }
        }
        return items;
    }

    public String getResults()throws IllegalArgumentException{
        StringBuilder headerBuffer = new StringBuilder();
        Formatter f = new Formatter(headerBuffer);

        int outputMidpoint = 37;
        String s1 = String.format("%1$tB %1$te, %1$tY  %tT", Calendar.getInstance());
        int len = outputMidpoint+Double.valueOf(Math.floor(Double.valueOf(s1.length()).doubleValue()/2.0)).intValue();
        String dString = "";

        dString = command.getDataString();

        int len2 = outputMidpoint+Double.valueOf(Math.floor(Double.valueOf(dString.length()).doubleValue()/2.0)).intValue();

        f.format("%43s", "ITEM ANALYSIS"); f.format("%n");
        f.format("%" + len2 + "s", dString); f.format("%n");
        f.format("%" + len + "s", s1); f.format("%n");
        f.format("%-70s","======================================================================");
        f.format("%n");
        f.format("%n");

        StringBuilder buffer = new StringBuilder();
        buffer.append(f.toString());

        int counter=0;
        ClassicalItem temp ;
        if(showItemStats){
            for(Integer i : item.keySet()){
                temp = item.get(i);
                if(counter==0 || command.getSelectAllOption("options").isArgumentSelected("header")){
                    buffer.append(temp.printHeader());
                }
                buffer.append(temp.toString());

                if(command.getSelectAllOption("options").isArgumentSelected("all") ||
                        command.getSelectAllOption("options").isArgumentSelected("header")){
                    buffer.append("\n");
                }

                counter++;
            }
        }

        buffer.append(testSummary.print(unbiased));
        return buffer.toString();

    }

    protected String doInBackground() {
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Item Analysis...");
        this.firePropertyChange("progress-on", null, null);
        String results = "";
        logger.info(command.paste());
        try{
            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            VariableTableName variableTableName = new VariableTableName(tableName.toString());
            ArrayList<String> selectVariables = command.getFreeOptionList("variables").getString();
            variables = dao.getSelectedVariables(conn, variableTableName, selectVariables);

            processCommand();
            initializeProgress();
            summarize();
            results = getResults();

            if(saveOutput){
                firePropertyChange("status", "", "Saving output...");
                this.firePropertyChange("progress-ind-on", null, null);
                outputTable = dao.getUniqueTableName(conn, outputTableString);
                ItemAnalysisOutputTable dbOutput = new ItemAnalysisOutputTable(conn, tableName, outputTable);
                dbOutput.saveOutput(item, allCategories);
                tableCreated = true;
            }

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible
        }catch(Throwable t){
            theException=t;
        }
        return results;
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
            }
        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }

    }

    //===============================================================================================================
    //Handle variable changes here
    //   -Dialogs will use these methods to addArgument their variable listeners
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

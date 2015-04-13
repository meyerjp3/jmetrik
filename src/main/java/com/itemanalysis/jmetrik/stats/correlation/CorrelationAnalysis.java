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

package com.itemanalysis.jmetrik.stats.correlation;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.polycor.CovarianceMatrix;
import com.itemanalysis.psychometrics.polycor.MixedCorrelationMatrix;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;

public class CorrelationAnalysis extends SwingWorker<String,Void> {

    private CorrelationCommand command = null;

    private JmetrikTextFile textFile = null;

    private Throwable theException = null;

    private Connection conn = null;

    private DatabaseAccessObject dao = null;

    private StopWatch sw = null;

    private CovarianceMatrix covMat = null;

    private MixedCorrelationMatrix mixedCovMat = null;

    private boolean unbiased = true;

    private boolean listwise=true;

    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    private ArrayList<VariableAttributes> variables = null;

    private ArrayList<VariableChangeListener> variableChangeListeners = null;

    private String dString = "";

    private DataTableName tableName = null;

    private boolean mixed = false;

    private boolean stdError = false;

    private double maxProgress = 100.0;

    private int progressValue = 0;

    private int lineNumber = 0;

    public CorrelationAnalysis(Connection conn, DatabaseAccessObject dao, CorrelationCommand command, JmetrikTextFile textFile){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.textFile = textFile;
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

    public void summarize()throws IllegalArgumentException, SQLException {
        int missingDataForCase=0;
        int numberOfVariables = 0;
        Double[] responseVector;
        boolean ml = false;

        Statement stmt;
        ResultSet rs;

        dString = command.getDataString();
        numberOfVariables=variables.size();

        if(mixed){
            ml = command.getSelectOneOption("polychoric").isValueSelected("ml");
            mixedCovMat = new MixedCorrelationMatrix(variables, ml);
        }else{
            covMat = new CovarianceMatrix(variables);
        }

        responseVector = new Double[numberOfVariables];
        if(command.getSelectOneOption("missing").isValueSelected("listwise")){
            listwise=true;
        }else{
            listwise=false;
        }

        if(command.getSelectOneOption("estimator").isValueSelected("unbiased")){
            unbiased=true;
        }else{
            unbiased=false;
        }

        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        for(VariableAttributes v : variables){
            select.addColumn(sqlTable, v.getName().nameForDatabase());
        }
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs=stmt.executeQuery(select.toString());

        int index=0;
        double responseScore = 0.0;
        Object response;
        while(rs.next()){
            //check for missing values per case for listwise deletion
            index=0;
            missingDataForCase=0;

            for(VariableAttributes v : variables){
                response = rs.getObject(v.getName().nameForDatabase());
                if(listwise && (response==null || response.equals(""))) missingDataForCase++;

                if(v.getType().getItemType()== ItemType.NOT_ITEM){
                    responseVector[index] = (Double)response;
                }else{
                    responseScore = v.getItemScoring().computeItemScore(response);
                    responseVector[index] = responseScore;
                }
                index++;
            }

            if(listwise && missingDataForCase==0){
                for(int j=0;j<numberOfVariables;j++){
                    for(int k=j;k<numberOfVariables;k++){
                        if(mixed){
                            mixedCovMat.increment(j, k, responseVector[j].doubleValue(), responseVector[k].doubleValue());
                        }else{
                            covMat.increment(j, k, responseVector[j], responseVector[k]);
                        }
                    }
                }
            }else if(!listwise){
                for(int j=0;j<numberOfVariables;j++){
                    for(int k=j;k<numberOfVariables;k++){
                        if(mixed){
                            mixedCovMat.increment(j, k, responseVector[j].doubleValue(), responseVector[k].doubleValue());
                        }else{
                            if(responseVector[j]!=null && responseVector[k]!=null){
                                covMat.increment(j, k, responseVector[j], responseVector[k]);
                            }

                        }

                    }
                }
            }
            updateProgress();
        }
        rs.close();
        stmt.close();

    }

    public String printCorrelationMatrix(){
        StringBuilder sb = new StringBuilder();
        if(mixed){
            sb.append(mixedCovMat.printCorrelationMatrix(stdError));
        }else{
            sb.append(covMat.printCorrelationMatrix(unbiased, stdError));
            sb.append("\n");
            sb.append("\n");
            sb.append("\n");
            sb.append(covMat.printCovarianceMatrix(unbiased));
        }
        return sb.toString();
    }

    public String printPolychoricInformation(){
        if(!mixed) return "";
        StringBuilder sb = new StringBuilder();
        sb.append(mixedCovMat.printPolychoricThresholds());
        sb.append("\n");
        sb.append("\n");
        sb.append("\n");
        sb.append(mixedCovMat.printCorrelationTypes());
        return sb.toString();


    }

    public String getResults(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        int outputMidpoint = 35;
        String s1 = String.format("%1$tB %1$te, %1$tY  %tT", Calendar.getInstance());
        int len = outputMidpoint+Double.valueOf(Math.floor(Double.valueOf(s1.length()).doubleValue()/2.0)).intValue();
        int len2 = outputMidpoint+Double.valueOf(Math.floor(Double.valueOf(dString.length()).doubleValue()/2.0)).intValue();

        f.format("%45s", "CORRELATION ANALYSIS"); f.format("%n");
        f.format("%" + len2 + "s", dString); f.format("%n");
        f.format("%" + len + "s", s1); f.format("%n");
        f.format("%n");
        f.format("%n");

        sb.append(printCorrelationMatrix());
        f.format("%n");
        f.format("%n");

        if(mixed){
            sb.append(printPolychoricInformation());
        }

        f.format("%n");
//        if(listwise){
//            f.format("%10s", "N = " + covMat.listwiseSampleSize());
//        }
        f.format("%n");
        f.format("%n");
        f.format("%n");

        return sb.toString();
    }

    protected String doInBackground() {
        String results = "";
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Correlation...");
        this.firePropertyChange("progress-on", null, null);
        try{
            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            VariableTableName variableTableName = new VariableTableName(tableName.toString());
            ArrayList<String> selectVariables = command.getFreeOptionList("variables").getString();
            variables = dao.getSelectedVariables(conn, variableTableName, selectVariables);

            initializeProgress();

            mixed = command.getSelectOneOption("type").isValueSelected("mixed");
            stdError = command.getSelectAllOption("options").isArgumentSelected("stderror");

            summarize();
            if(mixed){
                this.firePropertyChange("progress-ind-on", null, null);
                this.firePropertyChange("message", "", "Maximizing Polychoric Likelihood...");
            } //make statusbar progress visible

            results = getResults();

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible

        }catch(Throwable t){
            theException=t;
        }
        return results;
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

    @Override
    protected void done(){
        try{
            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }
            textFile.addText(get());
            textFile.addText("Elapsed time: " + sw.getElapsedTime());
            textFile.setCaretPosition(0);
            scriptLogger.info(command.paste());
        }catch(Exception ex){
            logger.fatal(theException.getMessage(), theException);
            firePropertyChange("error", "", "Error - Check log for details.");
        }

    }

}

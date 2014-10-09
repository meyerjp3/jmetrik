/*
 * Copyright (c) 2013 Patrick Meyer
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

package com.itemanalysis.jmetrik.scoring;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.utils.Alphabet;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class BasicScoringAnalysis extends SwingWorker<String, Void>{

    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private BasicScoringCommand command = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private VariableTableName variableTableName = null;
    private ArrayList<String> key = null;
    private ArrayList<Integer> ncat = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private ArrayList<VariableInfo> variables = null;
    private Throwable theException = null;
    private String omitCode = "";
    private String notReachedCode = "";
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public BasicScoringAnalysis(Connection conn, DatabaseAccessObject dao, BasicScoringCommand command){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void processCommand(){
        dbName = new DatabaseName(command.getPairedOptionList("data").getStringAt("db"));
        tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
        variableTableName = new VariableTableName(tableName.toString());
        key = command.getFreeOptionList("key").getString();
        ncat = command.getFreeOptionList("ncat").getInteger();

        omitCode = command.getPairedOptionList("codes").getStringAt("omit");
        notReachedCode = command.getPairedOptionList("codes").getStringAt("nr");

    }

    /**
     * The score string is formatted as (A,B,C,D)(0,1,0,0). See DefaultItemScoring
     * for the regex and more information.
     *
     * @param key answer key may be a number or letter
     * @param ncat number of response options (number of categories)
     * @return
     */
    private double maxNumberOfResponseOptions(String key, int ncat){
        try{
            //key is an integer
            double k = Double.parseDouble(key);
            return Math.max(k, (double)ncat);
        }catch(NumberFormatException ex){
            //key is a string
            int pos = Alphabet.getNum(key)+1;
            return Math.max(pos, ncat);
        }
    }

    private String polytomousScoreString(String key, int ncat)throws NumberFormatException{
        boolean ascending = key.startsWith("+");
        int startValue = 1;
        int complement = ncat;

        if(!ascending){
            startValue = ncat;
            complement = 1;
        }

        if(key.length()>1){
            int iKey = Integer.parseInt(key.substring(1));//will throw NumberFormatException if key.substring(1) is a String.
            startValue = Math.abs(iKey);

            if(ascending){
                complement = ncat+startValue-1;
            }else{
                complement = startValue+1 - ncat;
                complement = Math.max(0, complement);//force non-negative numbers
            }
        }

        String options = "(";
        String score = "(";

        //key is an integer
        if(ascending){
            for(int i=0;i<ncat;i++){
                options += startValue + ",";
                score += startValue + ",";
                startValue++;
            }
        }else{
            for(int i=0;i<ncat;i++){
                options += Math.max(0, complement+i) + ",";
                score += startValue + ",";
                startValue--;
                startValue = Math.max(0, startValue);//prevent negative score values
            }
        }

        options = options.substring(0, options.lastIndexOf(","));
        options+=")";
        score = score.substring(0, score.lastIndexOf(","));
        score+=")";

        return options + score;
    }

    private String binaryScoreString(String key, int ncat)throws NumberFormatException{
        double maxCat = maxNumberOfResponseOptions(key, ncat);
        String options = "(";
        String score = "(";
        try{
            double iKey = Double.parseDouble(key);

            //key is a double
            for(int i=0;i<maxCat;i++){
                options += (i+1) + ",";
                if(iKey==(double)(i+1)){
                    score += 1 + ",";
                }else{
                    score += 0 + ",";
                }
            }
            options = options.substring(0, options.lastIndexOf(","));
            options+=")";
            score = score.substring(0, score.lastIndexOf(","));
            score+=")";

        }catch(NumberFormatException ex){
            //key is a string
            boolean lowerCase = Character.isLowerCase(key.charAt(0));
            String optionString = "";

            for(int i=0;i<maxCat;i++){
                optionString = Alphabet.getLetter(i+1, lowerCase);
                options +=  optionString + ",";
                if(key.equals(optionString)){
                    score += 1 + ",";
                }else{
                    score += 0 + ",";
                }

            }
            options = options.substring(0, options.lastIndexOf(","));
            options+=")";
            score = score.substring(0, score.lastIndexOf(","));
            score+=")";
        }

        return options + score;
    }

    private String getScoreString(String key, int ncat)throws NumberFormatException{
        String trimmedKey = key.trim();
        if("".equals(trimmedKey)){
            return "";//not an item
        }else if(trimmedKey.startsWith("+") || trimmedKey.startsWith("-")){
            return polytomousScoreString(key, ncat);
        }else{
            return binaryScoreString(key, ncat);
        }

    }

    /**
     * Converts input information into a scoring string for VariableInfo object.
     * Database is updated with the new scoring. The method assumes that the input
     * ArrayLists are in teh same order as the variables in the database table.
     *
     * @throws java.sql.SQLException
     */
    private void convertScoring()throws SQLException{
        variables = dao.getAllVariables(conn, variableTableName);

        //only use the smallest number of variables found in input
        int nvar = variables.size();
        int nkey = key.size();
        int nopt = ncat.size();

        nvar = Math.min(nvar, nkey);
        nvar = Math.min(nvar, nopt);

        String scoreString = "";
        VariableInfo tempVar;
        for(int i=0;i<nvar;i++){
            scoreString = getScoreString(key.get(i), ncat.get(i));
            tempVar = variables.get(i);
            tempVar.clearCategory();
            tempVar.clearSpecialDataCodes();
            tempVar.addAllCategories(scoreString);
            if(tempVar.getType().getItemType()!=VariableType.NOT_ITEM){
                if(omitCode!=null) tempVar.setOmitCode(omitCode);
                if(notReachedCode!=null) tempVar.setNotReachedCode(notReachedCode);
            }

        }

        //add item scoring to database
        dao.setVariableScoring(conn, variableTableName, variables);
        dao.setOmitAndNotReachedCode(conn, variableTableName, variables);

    }


    @Override
    protected String doInBackground()throws Exception{
        try{
            logger.info(command.paste());
            processCommand();
            convertScoring();
        }catch(Exception ex){
            theException = ex;
            throw ex;
        }

        return "";
    }

    @Override
    protected void done(){
        try{
            if(theException==null){
                firePropertyChange("table-updated", null, tableName);//updates display of data table
                for(VariableInfo v : variables){
                    //updates variables in dialogs
                    fireVariableChanged(new VariableChangeEvent(this, variableTableName, v, VariableChangeType.VARIABLE_MODIFIED));
                }
            }else{
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
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

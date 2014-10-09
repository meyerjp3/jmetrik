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

package com.itemanalysis.jmetrik.stats.frequency;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import com.itemanalysis.psychometrics.texttable.TextTableRow;
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
import java.util.*;

public class FrequencyAnalysis extends SwingWorker<String,String> {

    private FrequencyCommand command = null;

    private JmetrikTextFile textFile = null;

    private Throwable theException = null;

    private Connection conn = null;

    private StopWatch sw = null;

    private LinkedHashMap<VariableInfo, Frequency> frequencyTables = null;

    private ArrayList<VariableChangeListener> variableChangeListeners = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    private ArrayList<VariableInfo> variables = null;

    private DataTableName tableName = null;

    private int progressValue = 0;

    private int lineNumber = 0;

    private double maxProgress = 100.0;

    private DatabaseAccessObject dao = null;

    public FrequencyAnalysis(Connection conn, DatabaseAccessObject dao, FrequencyCommand command, JmetrikTextFile textFile){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.textFile = textFile;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
        variables = new ArrayList<VariableInfo>();
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

    public void summarize()throws SQLException {
        Statement stmt = null;
        ResultSet rs=null;

        frequencyTables = new LinkedHashMap<VariableInfo, Frequency>();
        Frequency temp = null;

        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        for(VariableInfo v : variables){
            select.addColumn(sqlTable, v.getName().nameForDatabase());
        }
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs=stmt.executeQuery(select.toString());

        String strValue = "";
        double dblValue = 0;
        while(rs.next()){
            for(VariableInfo v : variables){
                temp = frequencyTables.get(v);
                if(temp==null){
                    temp = new Frequency();
                    frequencyTables.put(v, temp);
                }
                if(v.getType().getDataType()== VariableType.STRING){
                    strValue = rs.getString(v.getName().nameForDatabase());
                    if(!rs.wasNull() && !"".equals(strValue)){
                        temp.addValue(strValue);
                    }
                }else{
                    dblValue = rs.getDouble(v.getName().nameForDatabase());
                    if(!rs.wasNull()){
                        temp.addValue(dblValue);
                    }
                }
            }
            updateProgress();
        }
        rs.close();
        stmt.close();

        for(VariableInfo v: frequencyTables.keySet()){
            publishTable(v);
        }

    }

    public void publishTable(VariableInfo v){
        TextTableColumnFormat[] cformats = new TextTableColumnFormat[6];
        cformats[0] = new TextTableColumnFormat();
        cformats[0].setStringFormat(11, TextTableColumnFormat.OutputAlignment.LEFT);
        cformats[1] = new TextTableColumnFormat();
        cformats[1].setIntFormat(10, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[2] = new TextTableColumnFormat();
        cformats[2].setDoubleFormat(10, 4, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[3] = new TextTableColumnFormat();
        cformats[3].setDoubleFormat(10, 4, TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[4] = new TextTableColumnFormat();
        cformats[4].setIntFormat(10,  TextTableColumnFormat.OutputAlignment.RIGHT);
        cformats[5] = new TextTableColumnFormat();
        cformats[5].setDoubleFormat(10, 4, TextTableColumnFormat.OutputAlignment.RIGHT);

        Frequency temp = frequencyTables.get(v);

        TextTable table = new TextTable();
        table.addAllColumnFormats(cformats, 4);
        table.getRowAt(0).addHeader(0, 6, v.getName().toString(), TextTablePosition.CENTER);
        table.getRowAt(1).addHorizontalRule(0, 6, "=");
        table.getRowAt(2).addHeader(0, 1, "Value", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(1, 1, "Frequency", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(2, 1, "Percent", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(3, 1, "Valid Pct.", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(4, 1, "Cum. Freq.", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(5, 1, "Cum. Pct.", TextTablePosition.CENTER);
        table.getRowAt(3).addHorizontalRule(0, 6, "-");

        Iterator<Comparable<?>> iter = temp.valuesIterator();

        int index=4;
        double pctSum = 0.0;
        long validSum = temp.getSumFreq();
        long miss = (long)(maxProgress - validSum);

        while(iter.hasNext()){
            table.addRow(new TextTableRow(cformats.length), cformats);

            Comparable<?> value = iter.next();
            table.addStringAt(index, 0, value.toString());
            table.addLongAt(index, 1, temp.getCount(value));
            table.addDoubleAt(index, 2, ((double)temp.getCount(value)/ maxProgress)*100);
            table.addDoubleAt(index, 3, temp.getPct(value)*100);
            table.addLongAt(index, 4, temp.getCumFreq(value));
            table.addDoubleAt(index, 5, temp.getCumPct(value)*100);
            index++;
            pctSum += (double)temp.getCount(value)/(double) maxProgress;
        }

        table.addRow(new TextTableRow(cformats.length), cformats);
        table.addStringAt(index, 0, "Valid Total");
        table.addLongAt(index, 1, validSum);
        table.addDoubleAt(index, 2, pctSum*100);
        table.addDoubleAt(index, 3, (double)validSum/(double)(maxProgress -miss)*100);
        index++;

        table.addRow(new TextTableRow(cformats.length), cformats);
        table.addStringAt(index, 0, "Missing");
        table.addLongAt(index, 1, miss);
        table.addDoubleAt(index, 2, ((double)miss/ maxProgress)*100);
        index++;

        table.addRow(new TextTableRow(cformats.length), cformats);
        table.addStringAt(index, 0, "Grand Total");
        table.addLongAt(index, 1, (long) maxProgress);
        table.addDoubleAt(index, 2, ((double)(miss+temp.getSumFreq())/ maxProgress)*100);
        index++;

        table.addRow(new TextTableRow(cformats.length), cformats);
        table.getRowAt(index).addHorizontalRule(0, 6, "=");
        publish(table.toString() + "\n");
    }

    public void publishHeader()throws IllegalArgumentException{
        StringBuilder header = new StringBuilder();
        Formatter f = new Formatter(header);
        String s1 = String.format("%1$tB %1$te, %1$tY  %tT", Calendar.getInstance());
        int len = 21+Double.valueOf(Math.floor(Double.valueOf(s1.length()).doubleValue()/2.0)).intValue();
        String dString = "";
        try{
            dString = command.getDataString();
        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
        int len2 = 21+Double.valueOf(Math.floor(Double.valueOf(dString.length()).doubleValue()/2.0)).intValue();

        f.format("%31s", "FREQUENCY ANALYSIS"); f.format("%n");
        f.format("%" + len2 + "s", dString); f.format("%n");
        f.format("%" + len + "s", s1); f.format("%n");
        f.format("%n");
        publish(f.toString());
    }

    public String timeStamp(){
        String complete = "Elapsed Time: " + sw.getElapsedTime();
        return complete;
    }

    @Override
    protected void process(List<String> chunks){
        for(String s : chunks){
            textFile.append(s + "\n");
        }
    }

    protected String doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Frequencies...");
        this.firePropertyChange("progress-on", null, null);
        try{
            logger.info(command.paste());

            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            VariableTableName variableTable = new VariableTableName(tableName.toString());
            ArrayList<String> selectVariables = command.getFreeOptionList("variables").getString();
            variables = dao.getSelectedVariables(conn, variableTable, selectVariables);

            initializeProgress();

            this.publishHeader();
            this.summarize();
            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible

        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return timeStamp();
    }

    @Override
    protected void done(){
        try{
            if(theException==null){
                textFile.addText(get());
                textFile.setCaretPosition(0);
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

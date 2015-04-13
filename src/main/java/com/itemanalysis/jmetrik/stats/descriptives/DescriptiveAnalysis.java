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

package com.itemanalysis.jmetrik.stats.descriptives;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.LinkedHashMap;
import java.util.List;
import javax.swing.SwingWorker;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.apache.log4j.Logger;

public class DescriptiveAnalysis extends SwingWorker<String,String> {

    private DescriptiveCommand command = null;

    private DatabaseAccessObject dao = null;

    private JmetrikTextFile textFile = null;

    private Throwable theException = null;

    private Connection conn = null;

    private StopWatch sw = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    private ArrayList<VariableAttributes> variables = null;

    private DataTableName tableName = null;

    private LinkedHashMap<VariableAttributes, DescriptiveStatistics> data = null;

    private ArrayList<VariableChangeListener> variableChangeListeners = null;

    private int progressValue = 0;

    private int lineNumber = 0;

    private double maxProgress = 100.0;

    public DescriptiveAnalysis(Connection conn, DatabaseAccessObject dao, DescriptiveCommand command, JmetrikTextFile textFile){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.textFile = textFile;
        data = new LinkedHashMap<VariableAttributes, DescriptiveStatistics>();
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

    public void summarize()throws SQLException{
        Statement stmt = null;
        ResultSet rs=null;

        DescriptiveStatistics temp = null;

        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        for(VariableAttributes v : variables){
            select.addColumn(sqlTable, v.getName().nameForDatabase());
        }
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs=stmt.executeQuery(select.toString());

        double value = Double.NaN;
        while(rs.next()){
            for(VariableAttributes v : variables){
                temp = data.get(v);
                if(temp==null){
                    temp = new DescriptiveStatistics();
                    data.put(v, temp);
                }

                //only increment for non null doubles
                value = rs.getDouble(v.getName().nameForDatabase());
                if(!rs.wasNull()){
                    temp.addValue(value);
                }
            }
            updateProgress();
        }

        rs.close();
        stmt.close();

        for(VariableAttributes v : data.keySet()){
            publishTable(v);
        }


    }

    public void publishTable(VariableAttributes v){
        TextTable table = null;
        TextTableColumnFormat[] cformats = new TextTableColumnFormat[2];
        cformats[0] = new TextTableColumnFormat();
        cformats[0].setStringFormat(15, TextTableColumnFormat.OutputAlignment.LEFT);
        cformats[1] = new TextTableColumnFormat();
        cformats[1].setDoubleFormat(10, 4, TextTableColumnFormat.OutputAlignment.RIGHT);

        DescriptiveStatistics temp = data.get(v);
        table = new TextTable();
        table.addAllColumnFormats(cformats, 17);
        table.getRowAt(0).addHeader(0, 2, v.getName().toString(), TextTablePosition.CENTER);
        table.getRowAt(1).addHorizontalRule(0, 2, "=");
        table.getRowAt(2).addHeader(0, 1, "Statistic", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(1, 1, "Value", TextTablePosition.CENTER);
        table.getRowAt(3).addHorizontalRule(0, 2, "-");

        table.addStringAt(4, 0, "N");
        table.addDoubleAt(4, 1, maxProgress);
        table.addStringAt(5, 0, "Valid N");
        table.addDoubleAt(5, 1, temp.getN());
        table.addStringAt(6, 0, "Min");
        table.addDoubleAt(6, 1, temp.getMin());
        table.addStringAt(7, 0, "Max");
        table.addDoubleAt(7, 1, temp.getMax());
        table.addStringAt(8, 0, "Mean");
        table.addDoubleAt(8, 1, temp.getMean());
        table.addStringAt(9, 0, "Std. Dev.");
        table.addDoubleAt(9, 1, temp.getStandardDeviation());
        table.addStringAt(10, 0, "Skewness");
        table.addDoubleAt(10, 1, temp.getSkewness());
        table.addStringAt(11, 0, "Kurtosis");
        table.addDoubleAt(11, 1, temp.getKurtosis());
        table.addStringAt(12, 0, "First Quartile");
        table.addDoubleAt(12, 1, temp.getPercentile(25));
        table.addStringAt(13, 0, "Median");
        table.addDoubleAt(13, 1, temp.getPercentile(50));
        table.addStringAt(14, 0, "Third Quartile");
        table.addDoubleAt(14, 1, temp.getPercentile(75));
        table.addStringAt(15, 0, "IQR");
        table.addDoubleAt(15, 1, temp.getPercentile(75)-temp.getPercentile(25));
        table.getRowAt(16).addHorizontalRule(0, 2, "=");

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

        f.format("%31s", "DESCRIPTIVE STATISTICS"); f.format("%n");
        f.format("%" + len2 + "s", dString); f.format("%n");
        f.format("%" + len + "s", s1); f.format("%n");
        f.format("%n");
        publish(f.toString());
    }

    @Override
    protected void process(List<String> chunks){
        for(String s : chunks){
            textFile.append(s + "\n");
        }
    }

    public String timeStamp(){
        String complete = "Elapsed Time: " + sw.getElapsedTime();
        return complete;
    }

    protected String doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Descriptives...");
        this.firePropertyChange("progress-on", null, null);
        try{
            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            VariableTableName variableTableName = new VariableTableName(tableName.toString());
            ArrayList<String> selectVariables = command.getFreeOptionList("variables").getString();
            variables = dao.getSelectedVariables(conn, variableTableName, selectVariables);

            initializeProgress();

            this.publishHeader();
            this.summarize();
            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible
        }catch(Throwable t){
            theException=t;
        }
        return timeStamp();
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
            textFile.setCaretPosition(0);
            scriptLogger.info(command.paste());
        }catch(Exception ex){
            logger.fatal(theException.getMessage(), theException);
            firePropertyChange("error", "", "Error - Check log for details.");
        }

    }

}

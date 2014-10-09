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

package com.itemanalysis.jmetrik.graph.scatterplot;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;

public class ScatterplotAnalysis extends SwingWorker<String, DefaultXYDataset> {

    private ScatterplotCommand command = null;

    private ScatterplotPanel scatterplotPanel = null;

    private Throwable theException = null;

    private Connection conn = null;

    public StopWatch sw = null;

    private DataTableName tableName = null;

    public boolean hasGroupingVariable = false;

    private VariableInfo xVar = null;

    private VariableInfo yVar = null;

    VariableInfo groupVar = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    private int progressValue = 0;

    private int lineNumber = 0;

    private double maxProgress = 100.0;

    private DatabaseAccessObject dao = null;

    private ArrayList<VariableChangeListener> variableChangeListeners = null;

    public ScatterplotAnalysis(Connection conn, DatabaseAccessObject dao, ScatterplotCommand command, ScatterplotPanel scatterplotPanel){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.scatterplotPanel = scatterplotPanel;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void initializeProgress()throws SQLException {
        int nrow = dao.getRowCount(conn, tableName);
        maxProgress = ((double)nrow);
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/maxProgress);
        setProgress(progressValue);
        lineNumber++;
    }

    public XYSeriesCollection summarize()throws SQLException{
        XYSeriesCollection dataset = new XYSeriesCollection();
        TreeMap<Object, XYSeries> seriesMap = new TreeMap<Object, XYSeries>();
        Double xValue, yValue;
        Statement stmt = null;
        ResultSet rs=null;
        Object groupValue = null;

        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        select.addColumn(sqlTable, xVar.getName().nameForDatabase());
        select.addColumn(sqlTable, yVar.getName().nameForDatabase());

        String name = "";
        if(hasGroupingVariable){
            select.addColumn(sqlTable, groupVar.getName().nameForDatabase());
            if(groupVar.getLabel()==null || groupVar.getLabel().toString().equals("")){
                name=groupVar.getName().toString();
            }else{
                name=groupVar.getLabel().toString();
            }
        }else{
            name = "Series 1";
        }

        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs=stmt.executeQuery(select.toString());

        XYSeries xyseries = null;

        while(rs.next()){
            if(hasGroupingVariable){
                groupValue = rs.getObject(groupVar.getName().nameForDatabase());
                if(rs.wasNull()) groupValue = "";//handle missing data
                xyseries = seriesMap.get(groupValue);
                if(xyseries == null){
                    xyseries = new XYSeries(groupValue.toString());
                    seriesMap.put(groupValue, xyseries);
                }
            }else{
                xyseries = seriesMap.get(name);
                if(xyseries == null){
                    xyseries = new XYSeries(name);
                    seriesMap.put(name, xyseries);
                }
            }
            xValue=(Double)rs.getObject(xVar.getName().nameForDatabase());
            yValue=(Double)rs.getObject(yVar.getName().nameForDatabase());

            if(xValue!=null && yValue!=null){
                xyseries.add(xValue, yValue);
            }

            updateProgress();
        }
        rs.close();
        stmt.close();

        for(Object o : seriesMap.keySet()){
            dataset.addSeries(seriesMap.get(o));
        }

        return dataset;

    }

    protected String doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Scatterplot...");
        this.firePropertyChange("progress-on", null, null);
        try{
            logger.info(command.paste());

            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            VariableTableName variableTableName = new VariableTableName(tableName.toString());

            initializeProgress();

            String xName = command.getFreeOption("xvar").getString();
            xVar = dao.getVariableInfo(conn, variableTableName, xName);
            String yName = command.getFreeOption("yvar").getString();
            yVar = dao.getVariableInfo(conn, variableTableName, yName);

            if(command.getFreeOption("groupvar").hasValue()){
                String gName=command.getFreeOption("groupvar").getString();
                groupVar = dao.getVariableInfo(conn, variableTableName, gName);
                hasGroupingVariable = true;
            }

            scatterplotPanel.updateDataset(summarize());
            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible

        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return "done";
    }

    //===============================================================================================================
    //Handle variable changes here
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
        System.out.println("TestSclingAnalysis: firing variable changed=" + event.getVariable().getName().toString());
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
        }catch(Exception ex){
            logger.fatal(theException.getMessage(), theException);
            firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

}

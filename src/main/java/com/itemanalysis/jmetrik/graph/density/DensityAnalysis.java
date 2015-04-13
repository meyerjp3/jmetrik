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

package com.itemanalysis.jmetrik.graph.density;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.distribution.UniformDistributionApproximation;
import com.itemanalysis.psychometrics.kernel.*;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;
import org.apache.commons.math3.util.ResizableDoubleArray;
import org.apache.log4j.Logger;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;

public class DensityAnalysis  extends SwingWorker<DensityPanel, Void> {

    private DensityCommand command = null;
    private DensityPanel densityPanel = null;
    private Throwable theException = null;
    private Connection conn = null;
    private static int KERNEL_POINTS = 500;
    public StopWatch sw = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");
    public boolean hasGroupingVariable = false;
    private VariableAttributes variable = null;
    private VariableAttributes groupVar = null;
    private DataTableName tableName = null;
    private int progressValue = 0;
    private int lineNumber = 0;
    private double maxProgress = 0.0;
    private DatabaseAccessObject dao = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;

    public DensityAnalysis( Connection conn, DatabaseAccessObject dao, DensityCommand command, DensityPanel densityPanel){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.densityPanel = densityPanel;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/ maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    public XYSeriesCollection summarize()throws SQLException, IllegalArgumentException{
        Statement stmt = null;
        ResultSet rs=null;
        TreeMap<String, ResizableDoubleArray> data = new TreeMap<String, ResizableDoubleArray>();

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




        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        select.addColumn(sqlTable, variable.getName().nameForDatabase());
        if(hasGroupingVariable) select.addColumn(sqlTable, groupVar.getName().nameForDatabase());
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs=stmt.executeQuery(select.toString());

        String conditionalName = "";
        ResizableDoubleArray cData = null;
        double value = Double.NaN;

        while(rs.next()){
            if(groupVar!=null){
                String groupName = rs.getString(groupVar.getName().nameForDatabase());
                if(rs.wasNull()){
                    groupName = "";
                }
                conditionalName = groupName;
            }else{
                conditionalName = "Series 1";
            }

            cData = data.get(conditionalName);
            if(cData==null){
                cData = new ResizableDoubleArray((int) maxProgress);
                data.put(conditionalName, cData);
            }
            value = rs.getDouble(variable.getName().nameForDatabase());
            if(!rs.wasNull()){
                cData.addElement(value);
            }
            updateProgress();
        }
        rs.close();
        stmt.close();

        String kType = command.getSelectOneOption("kernel").getSelectedArgument();
        double adjustment = command.getFreeOption("adjust").getDouble();
        KernelFactory kernelFactory = new KernelFactory(kType);

        KernelFunction kernelFunction = kernelFactory.getKernelFunction();
        Bandwidth bandwidth = null;
        KernelDensity density = null;
        UniformDistributionApproximation uniform = null;
        Min min = new Min();
        Max max = new Max();
        double[] x = null;

        this.firePropertyChange("progress-ind-on", null, null);

        XYSeriesCollection seriesCollection = new XYSeriesCollection();
        XYSeries series = null;
        for(String s : data.keySet()){
            series = new XYSeries(s);
            x = data.get(s).getElements();
            bandwidth = new ScottsBandwidth(x, adjustment);
            uniform = new UniformDistributionApproximation(min.evaluate(x), max.evaluate(x), KERNEL_POINTS);
            density = new KernelDensity(kernelFunction, bandwidth, uniform);

            double[] dens = density.evaluate(x);
            double[] points = density.getPoints();
            for(int i=0;i<dens.length;i++){
                series.add(points[i], dens[i]);
            }
            seriesCollection.addSeries(series);
        }
        return seriesCollection;

    }

    protected DensityPanel doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Density Analysis...");
        this.firePropertyChange("progress-on", null, null);
        try{
            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            String selectVariable = command.getFreeOption("variable").getString();
            variable = dao.getVariableAttributes(conn, new VariableTableName(tableName.toString()), selectVariable);
            if(command.getFreeOption("groupvar").hasValue()){
                String groupByName=command.getFreeOption("groupvar").getString();
                groupVar = dao.getVariableAttributes(conn, new VariableTableName(tableName.toString()), groupByName);
                hasGroupingVariable = true;
            }

            densityPanel.updateDataset(summarize());
            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible
        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }

        return densityPanel;
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
            scriptLogger.info(command.paste());
        }catch(Exception ex){
            logger.fatal(theException.getMessage(), theException);
            firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

}


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

package com.itemanalysis.jmetrik.graph.itemmap;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.SwingWorker;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.graph.histogram.HistogramChartDataset;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.histogram.BinCalculationType;
import com.itemanalysis.psychometrics.histogram.Histogram;
import com.itemanalysis.psychometrics.histogram.HistogramType;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;

public class ItemMapAnalysis extends SwingWorker<ItemMapPanel, Void> {

    private ItemMapCommand command = null;
    private ItemMapPanel itemMapPanel = null;
    private Connection conn = null;
    private StopWatch sw = null;
    private DataTableName tableName = null;
    private DataTableName itemTableName = null;
    private ArrayList<VariableInfo> variable = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    private int progressValue = 0;
    private int lineNumber = 0;
    private double maxProgress = 100.0;
    private Throwable theException = null;
    private DatabaseAccessObject dao = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;

    public ItemMapAnalysis(Connection conn, DatabaseAccessObject dao, ItemMapCommand command, ItemMapPanel itemMapPanel){
        this.command = command;
        this.itemMapPanel = itemMapPanel;
        this.conn = conn;
        this.dao = dao;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void initializeProgress()throws SQLException {
        int nrow = dao.getRowCount(conn, tableName);
        maxProgress = ((double)nrow)*2.0;//once for bin calculation and noce for filling bin

        nrow = dao.getRowCount(conn, itemTableName);
        maxProgress += ((double)nrow)*2.0;//once for bin calculation and noce for filling bin
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    private HistogramChartDataset summarizePersons()throws SQLException{
        HistogramChartDataset data = new HistogramChartDataset();
        Statement stmt = null;
        ResultSet rs=null;

        try{
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(VariableInfo v : variable){
                select.addColumn(sqlTable, v.getName().nameForDatabase());
            }
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            Histogram histogram = new Histogram(HistogramType.DENSITY, BinCalculationType.STURGES, true);
            String dbVarName = variable.get(0).getName().nameForDatabase();
            rs=stmt.executeQuery(select.toString());
            while(rs.next()){
                Double value = (Double)rs.getObject(dbVarName);
                if(value!=null){
                    histogram.increment(value.doubleValue());
                }
                updateProgress();
            }
            histogram.evaluate();//compute histogram
            data.addHistogram("Theta", histogram);
            return data;
        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex);
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }
    }

    private HistogramChartDataset summarizeItems()throws SQLException{
        Statement stmt = null;
        ResultSet rs=null;

        HistogramChartDataset data = new HistogramChartDataset();

        try{
            Table sqlTable = new Table(itemTableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(sqlTable, "*");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            VariableName nCatName = new VariableName("ncat");
            VariableName deltaName = new VariableName("bparam");
            VariableName tauName = null;
            int numCat = 2;
            String gName = "difficulty";
            rs=stmt.executeQuery(select.toString());

            //fill bins
            while(rs.next()){
                Double nCat = (Double)rs.getObject(nCatName.nameForDatabase());
                Double delta = (Double)rs.getObject(deltaName.nameForDatabase());
                Double tau = 0.0;
                Double step = 0.0;
                if(nCat!=null && delta!=null){
                    numCat = nCat.intValue();
                    if(numCat>2){
                        for(int i=1;i<numCat;i++){
                            gName = "step" + i;
                            //compute step difficulties and frequency of occurrence
                            tauName = new VariableName(gName);
                            tau = delta + (Double)rs.getObject(tauName.nameForDatabase());
                            step = delta + tau;

                            Histogram h = data.getHistogram(gName);
                            if(h==null){
                                h = new Histogram(HistogramType.FREQUENCY, BinCalculationType.STURGES, true);
                                data.addHistogram(gName, h);
                            }
                            h.increment(step);
                        }
                    }else{
                        Histogram h = data.getHistogram(gName);
                        if(h==null){
                            h = new Histogram(HistogramType.FREQUENCY, BinCalculationType.STURGES, true);
                            data.addHistogram(gName, h);
                        }
                        h.increment(delta);
                    }
                }
                updateProgress();
            }

            //Compute histogram
            Iterator<Comparable> iter = data.iterator();
            Histogram h = null;
            while(iter.hasNext()){
                data.getHistogram(iter.next()).evaluate();
            }

            return data;
        }catch(SQLException ex){
            throw(ex);
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }
    }

    protected ItemMapPanel doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Item Map...");
        this.firePropertyChange("progress-on", null, null);
        try{
            logger.info(command.paste());
            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            itemTableName = new DataTableName(command.getPairedOptionList("itemdata").getStringAt("table"));
            ArrayList<String> selectedVariables = new ArrayList<String>();
            selectedVariables.add(command.getFreeOption("variables").getString());

            VariableTableName varTable = new VariableTableName(tableName.toString());

            variable = dao.getSelectedVariables(conn, varTable, selectedVariables);

            initializeProgress();
            itemMapPanel.updatePersonDataset(summarizePersons());
            itemMapPanel.updateItemDataSet(summarizeItems());

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible

        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return itemMapPanel;
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

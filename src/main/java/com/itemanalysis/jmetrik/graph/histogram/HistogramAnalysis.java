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

package com.itemanalysis.jmetrik.graph.histogram;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.histogram.*;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.Column;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import com.itemanalysis.squiggle.criteria.MatchCriteria;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.TreeMap;
import javax.swing.SwingWorker;
import org.apache.log4j.Logger;

public class HistogramAnalysis extends SwingWorker<HistogramPanel, Void> {

    private HistogramCommand command = null;

    private DatabaseAccessObject dao = null;

    private HistogramPanel histogramPanel = null;

    private Throwable theException = null;

    private Connection conn = null;

    public StopWatch sw = null;

    private DataTableName tableName = null;

    private boolean hasGroupingVariable = false;

    private VariableInfo groupVar = null;

    private VariableInfo variable = null;

    private ArrayList<Object> groupbyValues = null;

    private int progressValue = 0;

    private int lineNumber = 0;

    private double maxProgress = 100.0;

    private TreeMap<Object, BinCalculation> binCalc = null;

    private ArrayList<VariableChangeListener> variableChangeListeners = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public HistogramAnalysis(Connection conn, DatabaseAccessObject dao, HistogramCommand command, HistogramPanel histogramPanel){
        this.command = command;
        this.histogramPanel = histogramPanel;
        this.conn = conn;
        this.dao = dao;
        groupbyValues = new ArrayList<Object>();
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    private void initializeProgressBar()throws SQLException{
        int nrow = 0;
        nrow = dao.getRowCount(conn, tableName);
        maxProgress = (double)nrow*2.0;//two loops over db
    }

    private void updateProgress(){
        progressValue=(int)((100*((double)lineNumber+1.0))/ maxProgress);
        setProgress(Math.max(0,Math.min(100,progressValue)));
        lineNumber++;
    }

    public void setGroupByValues()throws SQLException{
        Statement stmt = null;
        ResultSet rs=null;
        try{
            //get unique values of grouping variable
            if(hasGroupingVariable){
                stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
                SelectQuery select = new SelectQuery();
                select.setDistinct(true);
                Table table = new Table(tableName.getNameForDatabase());
                select.addColumn(table, groupVar.getName().nameForDatabase());
                rs=stmt.executeQuery(select.toString());
                Object g = null;
                while(rs.next()){
                    g = rs.getObject(groupVar.getName().nameForDatabase());
                    if(!rs.wasNull()){
                        groupbyValues.add(g);
                    }
                }
                rs.close();
                stmt.close();
            }else{
                groupbyValues.add("series1");
            }
        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex);
        }
    }

    /**
     * Loop over db to compute the number of bins needed to form the histogram.
     * Will loop over db a second time to compute histogram. This approach costs
     * time but it save memory.
     *
     * @throws SQLException
     * @throws IllegalArgumentException
     */
//    private void binCalculation()throws SQLException, IllegalArgumentException{
//        Statement stmt = null;
//        ResultSet rs=null;
//
//        try{
//            binCalc = new TreeMap<Object, BinCalculation>();
//
//            initializeProgressBar();
//
//            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//            SelectQuery select = new SelectQuery();
//            Table table = new Table(tableName.getNameForDatabase());
//            select.addColumn(table, variable.getName().nameForDatabase());
//            rs=stmt.executeQuery(select.toString());
//
//            for(Object o : groupbyValues){
//                //select all cases ind b with groupByValue of Object o
//                select = new SelectQuery();
//                table = new Table(tableName.getNameForDatabase());
//                select.addColumn(table, variable.getName().nameForDatabase());
//
//                //add where clause if groupby variable provided
//                if(hasGroupingVariable && groupVar !=null){
//                    select.addColumn(table, groupVar.getName().nameForDatabase());
//                    if(groupVar.getType().getDataType()== VariableType.DOUBLE){
//                        select.addCriteria((new MatchCriteria(
//                                new Column(table, groupVar.getName().nameForDatabase()),
//                                MatchCriteria.EQUALS,
//                                ((Double)o).doubleValue()
//                        )));
//                    }else{
//                        select.addCriteria((new MatchCriteria(
//                                new Column(table, groupVar.getName().nameForDatabase()),
//                                MatchCriteria.EQUALS,
//                                o.toString()
//                        )));
//                    }
//                }
//                rs=stmt.executeQuery(select.toString());
//
//                //loop over data and add value sto array list
//                double value = Double.NaN;
//                while(rs.next()){
//                    value=rs.getDouble(variable.getName().nameForDatabase());
//                    if(!rs.wasNull()){
//                        BinCalculation bc = binCalc.get(o);
//                        if(bc==null){
//                            bc = getBinCalculation();
//                            binCalc.put(o, bc);
//                        }
//                        bc.increment(value);
//                    }
//                    updateProgress();
//                }
//                rs.close();
//            }
//            stmt.close();
//        }catch(SQLException ex){
//            throw new SQLException(ex);
//        }catch(IllegalArgumentException ex){
//            throw new IllegalArgumentException(ex);
//        }
//
//    }
//
//    private BinCalculation getBinCalculation()throws IllegalArgumentException{
//        BinCalculation bc = null;
//        try{
//            if(command.getSelectOneOption("bintype").isValueSelected("sturges")){
//                bc = new SturgesBinCalculation();
//            }else if(command.getSelectOneOption("bintype").isValueSelected("scott")){
//                bc = new ScottBinCalculation();
//            }else if(command.getSelectOneOption("bintype").isValueSelected("fd")){
//                bc = new FreedmanDiaconisBinCalculation();
//            }
//            return bc;
//        }catch(IllegalArgumentException ex){
//            throw new IllegalArgumentException(ex);
//        }
//
//    }

    /**
     * Second loop over db to compute histogram
     *
     * @return
     * @throws SQLException
     * @throws IllegalArgumentException
     */
    public HistogramChartDataset evaluate()throws SQLException, IllegalArgumentException{
        Statement stmt = null;
        ResultSet rs=null;
        HistogramChartDataset data = new HistogramChartDataset();

        try{

            setGroupByValues();
            HistogramType histogramType = HistogramType.DENSITY;
            if(command.getSelectOneOption("yaxis").isValueSelected("freq")){
                histogramType = HistogramType.FREQUENCY;

            }

            Histogram histogram = null;

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            SelectQuery select = new SelectQuery();
            Table table = new Table(tableName.getNameForDatabase());
            select.addColumn(table, variable.getName().nameForDatabase());
            rs=stmt.executeQuery(select.toString());

            for(Object o : groupbyValues){
                //select all cases ind b with groupByValue of Object o
                select = new SelectQuery();
                table = new Table(tableName.getNameForDatabase());
                select.addColumn(table, variable.getName().nameForDatabase());

                //add where clause if groupby variable provided
                if(hasGroupingVariable && groupVar !=null){
                    select.addColumn(table, groupVar.getName().nameForDatabase());
                    if(groupVar.getType().getDataType()==VariableType.DOUBLE){
                        select.addCriteria((new MatchCriteria(
                                new Column(table, groupVar.getName().nameForDatabase()),
                                MatchCriteria.EQUALS,
                                ((Double)o).doubleValue()
                        )));
                    }else{
                        select.addCriteria((new MatchCriteria(
                                new Column(table, groupVar.getName().nameForDatabase()),
                                MatchCriteria.EQUALS,
                                o.toString()
                        )));
                    }
                }
                rs=stmt.executeQuery(select.toString());

                //loop over data and add value sto array list
                histogram = new Histogram(histogramType);
                double value = Double.NaN;
                while(rs.next()){
                    value=rs.getDouble(variable.getName().nameForDatabase());
                    if(!rs.wasNull()){
                        histogram.increment(value);
                    }
                    updateProgress();
                }

                histogram.evaluate();//do computations
                data.addHistogram(o.toString(), histogram);
            }

            //===========================================================OLd BELOW
//            setGroupByValues();
//            binCalculation();
//
//            Histogram.HistogramType histType = null;
//            if(command.getSelectOneOption("yaxis").isValueSelected("freq")){
//                histType = Histogram.HistogramType.FREQUENCY;
//            }else{
//                histType = Histogram.HistogramType.DENSITY;
//            }
//
//            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//            SelectQuery select = new SelectQuery();
//            Table table = new Table(tableName.getNameForDatabase());
//            select.addColumn(table, variable.getName().nameForDatabase());
//            rs=stmt.executeQuery(select.toString());
//
//            for(Object o : groupbyValues){
//                //select all cases ind b with groupByValue of Object o
//                select = new SelectQuery();
//                table = new Table(tableName.getNameForDatabase());
//                select.addColumn(table, variable.getName().nameForDatabase());
//
//                //add where clause if groupby variable provided
//                if(hasGroupingVariable && groupVar !=null){
//                    select.addColumn(table, groupVar.getName().nameForDatabase());
//                    if(groupVar.getType().getDataType()==VariableType.DOUBLE){
//                        select.addCriteria((new MatchCriteria(
//                                new Column(table, groupVar.getName().nameForDatabase()),
//                                MatchCriteria.EQUALS,
//                                ((Double)o).doubleValue()
//                        )));
//                    }else{
//                        select.addCriteria((new MatchCriteria(
//                                new Column(table, groupVar.getName().nameForDatabase()),
//                                MatchCriteria.EQUALS,
//                                o.toString()
//                        )));
//                    }
//                }
//                rs=stmt.executeQuery(select.toString());
//
//                //loop over data and add values to array list
//                Histogram histogram = new Histogram(binCalc.get(o), histType);
//                double value = Double.NaN;
//                while(rs.next()){
//                    value=rs.getDouble(variable.getName().nameForDatabase());
//                    if(!rs.wasNull()){
//                        histogram.increment(value);
//                    }
//                    updateProgress();
//                }
//                rs.close();
//                data.addHistogram(o.toString(), histogram);
//            }
//            stmt.close();

//            Iterator<Comparable> iter = data.iterator();
//            while(iter.hasNext()){
//                Histogram h = data.getHistogram(iter.next());
//                System.out.println(h.toString());
//            }

            return data;
        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex);
        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new IllegalArgumentException(ex);
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    protected HistogramPanel doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Histogram...");
        this.firePropertyChange("progress-on", null, null);
        try{
            logger.info(command.paste());
            //get variable info from db
            tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
            String selectVariable = command.getFreeOption("variable").getString();
            variable = dao.getVariableInfo(conn, new VariableTableName(tableName.toString()), selectVariable);
            if(command.getFreeOption("groupvar").hasValue()){
                String groupByName=command.getFreeOption("groupvar").getString();
                groupVar = dao.getVariableInfo(conn, new VariableTableName(tableName.toString()), groupByName);
                hasGroupingVariable = true;
            }

            histogramPanel.updateDataset(evaluate());
            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible

        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return histogramPanel;
    }

    @Override
    protected void done(){
        try{
            if(theException!=null){
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


}

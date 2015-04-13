/*
 * Copyright (c) 2011 Patrick Meyer
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

package com.itemanalysis.jmetrik.model;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.workspace.StatusNotifier;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;

import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.*;

/**
 * Model for viewing data. It loads pages of data in a separate thread.
 *
 * It is almost database independent. The updateData() method
 * includes a SelectQuery object that is sepcific to Apache Derby. It needs to be fixed.
 *
 * In Workspace.java, this model is reloaded anytime a variable is added or deleted.
 *
 *
 */
public class PagingDataModel extends AbstractTableModel implements StatusNotifier{

    /**
     * Number of rows of data to hold in memory
     */
    private int rowsPerPage = 800;
    private int halfRowsPerPage = rowsPerPage/2;
    
    private int activeOffset = 0;
    private int maxRow = rowsPerPage-1;

    /**
     * keep list of edited rows
     */
    private ArrayList<Integer> editedRows = null;
    
    private int absoluteNumberOfRows=0;//total number of rows in table
    private int absoluteNumberOfColumns=0;//total number of cols in query
    private Object[][] data = null;
    private Class[] colClasses = null;
    private VariableName[] variableNames = null;

    protected DatabaseAccessObject dao = null;
    
    protected Connection conn = null;
    protected DataTableName tableName = null;
    private ArrayList<PropertyChangeListener> propertyChangeListeners = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    //use thread pool to manage data loading
    private ThreadPoolExecutor threadPool = null;
    private int threadPoolSize = 1;
    private int threadPoolSizeMax = 1;
    private int maxQueueSize = 5000;
    private long threadKeepAliveTime = 1000;
    private final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(maxQueueSize);

    public PagingDataModel(Connection conn, DataTableName tableName, DatabaseAccessObject dao, ArrayList<PropertyChangeListener> propertyChangeListeners){
        this.conn = conn;
        this.tableName = tableName;
        this.dao = dao;
        this.propertyChangeListeners = propertyChangeListeners;        
        threadPool = new ThreadPoolExecutor(threadPoolSize, threadPoolSizeMax, threadKeepAliveTime, TimeUnit.SECONDS, queue);

        threadPool.prestartCoreThread();
        editedRows = new ArrayList<Integer>();
        initialize();
    }
    
    private void initialize(){
        try{
//            Table table = new Table(tableName.getNameForDatabase());
//            SelectQuery select = new SelectQuery();
//            select.addColumn(table, "*");
            absoluteNumberOfRows = dao.getRowCount(conn, tableName);//number of rows in db table

            //get number of rows in table -- very slow
//            String QUERY = "SELECT COUNT(*) FROM " + tableName.getNameForDatabase();
//            Statement stmt = conn.createStatement();
//            ResultSet rs = stmt.executeQuery(QUERY);
//            rs.next();
//            absoluteNumberOfRows = rs.getInt(1);

            halfRowsPerPage = rowsPerPage/2;

            absoluteNumberOfColumns = dao.getColumnCount(conn, tableName);
            data = new Object[rowsPerPage][absoluteNumberOfColumns];
            variableNames = dao.getColumnNames(conn, tableName);
            colClasses = dao.getColumnClass(conn, tableName);

            updateData(0, 0, true);//initial load

        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }        
    }

    @Override
    public Object getValueAt(int r, int c){
        checkForRow(r);
        return data[r- activeOffset][c];
    }

    @Override
    public void setValueAt(Object value, int r, int c){
        data[r- activeOffset][c] = value;
        editedRows.add(r- activeOffset);
    }

    @Override
    public boolean isCellEditable(int r, int c){
        return true;
    }

    @Override
    public int getRowCount(){
        return absoluteNumberOfRows;
    }

    @Override
    public int getColumnCount(){
        return absoluteNumberOfColumns;
    }
    
    private void checkForRow(int r){
        if(r < activeOffset || r > maxRow){
            //set new activeOffset and load more data
            int newOffset = Math.max(r-halfRowsPerPage, 0);//TODO could be a problem if scroll fast than load?
            updateData(activeOffset, newOffset, true);
        }
    }



    private void updateData(int saveOffset, int loadOffset, boolean loadData){

        SaveLoadDataProcess saveLoad = new SaveLoadDataProcess(saveOffset, loadOffset, loadData);

        try{
            int result = threadPool.submit(saveLoad).get(); //should block until thread completes work
//            this.firePropertyChange("status", "", "Ready");

//            int result = threadPool.submit(saveLoad).get();
            if(result==-1){
                //only save happened
//                this.firePropertyChange("status", "", "Data saved.");
            }else{
                //edits saved and new data loaded
                activeOffset = result;
                maxRow = activeOffset +rowsPerPage-1;
            }
        }catch(InterruptedException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

    public void saveData(){
        updateData(activeOffset, activeOffset, false);
    }

    public boolean dataEdited(){
        return !editedRows.isEmpty();
    }

    @Override
    public Class getColumnClass(int col){
        return colClasses[col];
    }
    
    @Override
    public String getColumnName(int col){
        if(col<variableNames.length)return variableNames[col].toString();
        return "Var" + col;
        
    }

    //===============================================================================================================
    //Process messages here
    //===============================================================================================================
    public synchronized void addPropertyChangeListener(PropertyChangeListener l){
        propertyChangeListeners.add(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l){
        propertyChangeListeners.remove(l);
    }

    public synchronized void firePropertyChange(String propertyName, Object oldValue, Object newValue){
        PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for(PropertyChangeListener l : propertyChangeListeners){
            l.propertyChange(e);
        }
    }

    //===============================================================================================================

    /**
     * Class that saves and loads data.
     * If no edits have been made to the data, then nothing will be saved.
     *
     */
    class SaveLoadDataProcess implements Callable<Integer>{
        private int saveOffset = 0;
        private int loadOffset = 0;
        private boolean loadData = false;

        public SaveLoadDataProcess(int saveOffset, int loadOffset, boolean loadData){
            this.saveOffset = saveOffset;
            this.loadOffset = loadOffset;
            this.loadData = loadData;
        }

        /**
         * Create tree map where rows contain integers of rows numbers using the
         * rows number of the target database. The values are the rows of
         * data that have been edited
         */
        private TreeMap<Integer, Object[]> configureData(){
            TreeMap<Integer, Object[]> dataToSave = new TreeMap<Integer, Object[]>();
            for(Integer i : editedRows){
                dataToSave.put(i+1, data[i]);//database start index at 1
            }
            return dataToSave;
        }

        public void saveData()throws Exception{
            Table table = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(table, "*");
            select.addOffset(saveOffset, rowsPerPage);//TODO this activeOffset and limit is specific to derby
            dao.saveData(conn, select, configureData());
            editedRows.clear();
        }

        public Integer loadData()throws Exception{
            Table table = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(table, "*");
            select.addOffset(loadOffset, rowsPerPage);//TODO this activeOffset and limit is specific to derby
            data = dao.getData(conn, select, rowsPerPage, absoluteNumberOfColumns);

            //display data for debugging
//            for(int i=0;i<data.length;i++){
//                for(int j=0;j<data[0].length; j++){
//                    System.out.print(data[i][j] + " ");
//                }
//                System.out.println();
//            }


            return loadOffset;
        }

        public Integer call()throws Exception{
            if(editedRows.size()>0) saveData();
            if(loadData) return loadData();
            return -1;
        }

    }

}

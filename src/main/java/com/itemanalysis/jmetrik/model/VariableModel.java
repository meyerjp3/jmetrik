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
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.*;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class VariableModel extends AbstractTableModel {

    private String[] columnNames = {"Variable", "Type", "Scoring", "Group", "Label", "Omit", "Not Reached"};
    private ArrayList<VariableInfo> variables = new ArrayList<VariableInfo>();
    private int numCols=columnNames.length,numRows=0;
    private int dataModified=0;
    private Connection conn = null;
    private DatabaseName dbName = null;
    private VariableTableName tableName = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    private DatabaseAccessObject dao = null;
    private ArrayList<PropertyChangeListener> propertyChangeListeners = null;
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private ArrayList<DatabaseSelectionListener> databaseChangeListeners = null;

    //use thread pool to manage data loading
    private ThreadPoolExecutor threadPool = null;
    private int threadPoolSize = 1;
    private int threadPoolSizeMax = 1;
    private int maxQueueSize = 50;
    private long threadKeepAliveTime = 10;
    private final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(maxQueueSize);

//    Columns in db
//    "VARIABLE VARCHAR(30)," + //name (1)
//    "VARGROUP VARCHAR(30)," + //group (2)
//    "SCORING VARCHAR(30)," +  //scoring (3)
//    "ITEMTYPE VARCHAR(30)," +    //item type (4)
//    "DATATYPE VARCHAR(30)," +    //data type (5)
//    "LABEL VARCHAR(50))";     //label (6)


    public VariableModel(Connection conn, DatabaseName dbName, VariableTableName tableName, DatabaseAccessObject dao, ArrayList<PropertyChangeListener> propertyChangeListeners){
        this.conn = conn;
        this.dbName = dbName;
        this.tableName=tableName;
        this.dao = dao;
        this.propertyChangeListeners = propertyChangeListeners;
        this.variableChangeListeners = new ArrayList<VariableChangeListener>();
        this.databaseChangeListeners = new ArrayList<DatabaseSelectionListener>();
        threadPool = new ThreadPoolExecutor(threadPoolSize, threadPoolSizeMax, threadKeepAliveTime, TimeUnit.SECONDS, queue);
        threadPool.prestartCoreThread();
        loadData();
    }

    private void loadData(){

        Runnable task = new SwingWorker<ArrayList<VariableInfo>, Void>(){
            
            //connect to db and populate newData
            protected ArrayList<VariableInfo> doInBackground()throws Exception{
                return dao.getAllVariables(conn, tableName);
            }

            protected void done(){
                try{
                    variables = get();
                    numRows = variables.size();
                    fireTableDataChanged();
                }catch(Exception ex){
                    logger.fatal(ex.getMessage(), ex);
                    this.firePropertyChange("message", "", "Error - Check log for details.");
                }

            }

        };
        threadPool.execute(task);
    }

    private void updateTestItemOrder(){
        int order = 0;
        for(VariableInfo v : variables){
            VariableType vType = v.getType();
            if(vType.getItemType()== VariableType.BINARY_ITEM ||
                    vType.getItemType()==VariableType.POLYTOMOUS_ITEM ||
                    vType.getItemType()==VariableType.CONTINUOUS_ITEM){
                order++;
                v.setTestItemOrder(order);
            }
        }
    }
    
    public String getTableName(){
        return tableName.getTableName();
    }

    public void saveData(){
        if(dataModified>0){
            try{
                Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName.getNameForDatabase());
                for(int i=0;i<variables.size();i++){
                    rs.absolute(i+1);
                    rs.updateObject(1, variables.get(i).getName().toString());          //name
                    rs.updateObject(2, variables.get(i).getSubscale());                 //group (i.e. subscale)
                    rs.updateObject(3, variables.get(i).printOptionScoreKey());         //scoring
                    rs.updateObject(4, variables.get(i).getType().getItemType());       //item type
                    rs.updateObject(5, variables.get(i).getType().getDataType());       //data type
                    rs.updateObject(6, variables.get(i).getLabel().toString());         //label
                    rs.updateRow();
                }
                rs.close();
                stmt.close();
                rs=null;
                stmt=null;
                dataModified = 0;
                this.firePropertyChange("message", "", "Variables saved");
            }catch(SQLException ex){
                logger.fatal(ex.getMessage(), ex);
                this.firePropertyChange("error", "", "Error - Check log for details.");
            }
        }
    }

    public Object getValueAt(int r, int c){
        VariableInfo varInfo = variables.get(r);
        if(c==0){
            return varInfo.getName().toString();
        }else if(c==1){//convert integer to string for display
            return varInfo.getType().getItemTypeString();
        }else if(c==2){
            return varInfo.printOptionScoreKey();
        }else if(c==3){
            return varInfo.getSubscale();
        }else if(c==4){
            return varInfo.getLabel().toString();
        }else if(c==5){
            return varInfo.getOmitCode();
        }else if(c==6){
            return varInfo.getNotReachedCode();
        }
        return null;//c is too big
    }

    //	columnNames = {"Variable", "Type", "scoring", "Group", "Label"};
    @Override
    public void setValueAt(Object value, int r, int c){
        //c==0 not editable
        VariableInfo varInfo = variables.get(r);

        String valueString = value.toString();

        if(c==1){
            
            if(!varInfo.getType().getItemTypeString().equals(valueString)){
                dataModified++;

                if(valueString.equals(VariableType.CONTINUOUS_ITEM_STRING) ) {
                    varInfo.clearCategory();
                    varInfo.getType().setItemType(valueString);
                }else{
                    varInfo.getType().setItemType(valueString);
                }
                updateTestItemOrder();

            }

        }else if(c==2){
            if(!varInfo.printOptionScoreKey().equals(valueString)){
                dataModified++;
                varInfo.clearCategory();
                if((valueString==null || value.toString().equals("")) && varInfo.getType().getItemType()!=VariableType.CONTINUOUS_ITEM){
                    setValueAt(VariableType.CONTINUOUS_ITEM_STRING, r, 1);
                }else{
                    varInfo.addAllCategories(value.toString());//Type changed in VarInfo here by call to addAllCategories()
                }
            }

        }else if(c==3){
            if(!varInfo.getSubscale().equals(valueString)){
                dataModified++;
                varInfo.setSubscale(valueString);
            }

        }else if(c==4){
            if(!varInfo.getLabel().toString().equals(valueString)){
                dataModified++;
                varInfo.setLabel(valueString);
            }

        }

        fireVariableChangeEvent(new VariableChangeEvent(this, tableName, varInfo, VariableChangeType.VARIABLE_MODIFIED));

    }

    public int getRowCount(){
        return numRows;
    }

    public int getColumnCount(){
        return columnNames.length;
    }

    @Override
    public String getColumnName(int c){
        return columnNames[c];
    }

    @Override
    public Class getColumnClass(int c){
        return String.class;
    }

    @Override
    public boolean isCellEditable(int row, int col){
        if(col==3 || col==4) return true;
        return false;
    }


    public ArrayList<VariableInfo> getVariables(){
        return variables;
    }

    public VariableInfo getVariableAt(int index){
        return variables.get(index);
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

    protected synchronized void firePropertyChange(String propertyName, Object oldValue, Object newValue){
        PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for(PropertyChangeListener l : propertyChangeListeners){
            l.propertyChange(e);
        }
    }
    //===============================================================================================================




    //===============================================================================================================
    //Handle variable changes here
    //   -Dialogs should listen for changes to variables
    //   -Dialogs will use these methods to add their variable listeners
    //===============================================================================================================
    public synchronized void addVariableChangeListener(VariableChangeListener l){
        variableChangeListeners.add(l);
    }

    public synchronized void removeVariableChangeListener(VariableChangeListener l){
        variableChangeListeners.remove(l);
    }

    public synchronized  void removeAllVariableChangeListeners(){
        for(VariableChangeListener l : variableChangeListeners){
            variableChangeListeners.remove(l);
        }
    }

    public synchronized void fireVariableChangeEvent(VariableChangeEvent e){
        for(VariableChangeListener l : variableChangeListeners){
            l.variableChanged(e);
        }
    }
    //===============================================================================================================




    //===============================================================================================================
    //Handle database changes here
    //   -Dialogs should listen for new table opened
    //   -Dialogs will use these methods to add their variable listeners
    //===============================================================================================================
    public synchronized void addDatabaseChangeListener(DatabaseSelectionListener l){
        databaseChangeListeners.add(l);
    }

    public synchronized void removeVariableChangeListener(DatabaseSelectionListener l){
        databaseChangeListeners.remove(l);
    }

    public synchronized  void removeAllDatabaseChangeListeners(){
        for(DatabaseSelectionListener l : databaseChangeListeners){
            databaseChangeListeners.remove(l);
        }
    }

    public synchronized void fireDatabaseChangeEvent(DatabaseSelectionEvent e){

        for(DatabaseSelectionListener l : databaseChangeListeners){
            l.databaseSelectionChanged(e);
        }
    }
    //===============================================================================================================


}

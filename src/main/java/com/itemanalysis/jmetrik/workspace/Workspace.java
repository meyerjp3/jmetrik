/*
 * Copyright 2011 Patrick Meyer
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
 *
 */
package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.commandbuilder.TextToCommand;
import com.itemanalysis.jmetrik.dao.*;
import com.itemanalysis.jmetrik.swing.DataTable;
import com.itemanalysis.jmetrik.swing.NumericCellRenderer;
import com.itemanalysis.jmetrik.model.*;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.VariableInfo;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;
import java.util.ServiceLoader;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Workspace implements StatusNotifier{

    private JmetrikDatabaseFactory dbFactory = null;
    private DatabaseAccessObject dao = null;
    private Connection conn = null;
    private JList workspaceList = null;
    private SortedListModel<DataTableName> tableListModel = null;
    private DataTable dataTable = null;
    private DataTable variableTable = null;
    private ArrayList<PropertyChangeListener> propertyChangeListeners = null;
    private DatabaseName currentDbName = null;
    private DataTableName currentDataTable = null;
    private VariableTableName currentVariableTable = null;
    private DatabaseConnectionURL databaseConnectionURL = null;
    private JmetrikPreferencesManager prefs = null;
    private JmetrikProcessFactory procFactory = null;
    private boolean databaseOpened = false;
    private JTabbedPane tabbedPane = null;
    private String dbHome = "";
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private ThreadPoolExecutor threadPool = null;
    private int threadPoolSize = 1;
    private int threadPoolSizeMax = 1;
    private int maxQueueSize = 5000;
    private long threadKeepAliveTime = 10;
    private final ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(maxQueueSize);
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public Workspace(final JList workspaceList, JTabbedPane tabbedPane, DataTable dataTable, DataTable variableTable){
        this.workspaceList = workspaceList;
        this.tabbedPane = tabbedPane;
        this.dataTable = dataTable;
        this.variableTable = variableTable;
        
        threadPool = new ThreadPoolExecutor(threadPoolSize, threadPoolSizeMax, threadKeepAliveTime, TimeUnit.SECONDS, queue);
        threadPool.prestartCoreThread();

        workspaceList.addListSelectionListener(new JmetrikListSelectionListener());
        tableListModel = new SortedListModel<DataTableName>();
        workspaceList.setModel(tableListModel);

        //set model for data table
        dataTable.setModel(new EmptyTableModel());
        dataTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        //set model for variable table
        variableTable.setModel(new EmptyVariableModel());
//        variableTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        //create property and variable listener lists
        propertyChangeListeners = new ArrayList<PropertyChangeListener>();
        variableChangeListeners = new ArrayList<VariableChangeListener>();
        
        //load properties
        loadProperties();
        procFactory = new JmetrikProcessFactory();

        
    }
    
    private void loadProperties(){
        prefs = new JmetrikPreferencesManager();
        for(PropertyChangeListener pcl : propertyChangeListeners){
            prefs.addPropertyChangeListener(pcl);
        }

        //set type of database according to properties
        String dbType = prefs.getDatabaseType();
        if(DatabaseType.APACHE_DERBY.toString().equals(dbType)){
            dao = new DerbyDatabaseAccessObject();
            dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
        }else if(DatabaseType.MYSQL.toString().equals(dbType)){
            //not yet implemented
        }else{
            //default is apache derby
            dao = new DerbyDatabaseAccessObject();
            dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
        }
        
//        System.out.println(System.getProperty("derby.system.home"));//returns null

        dbHome = prefs.getDatabaseHome();
        System.setProperty("derby.system.home", dbHome);
//        System.out.println(System.getProperty("derby.system.home"));

        int precision = prefs.getPrecision();
        dataTable.setDefaultRenderer(Double.class, new NumericCellRenderer(precision));

    }

    /**
     * Changes to jMetrik often involve changes to the underlying database. This
     * method checks a property file in the DatabaseHome folder to see if the
     * databases have been updated. If not, updates are made and the property
     * is changed.
     *
     *
     */
    private void updateDatabaseVersion(){

        //create new columns in jmk_table_rows table and all variable tables
        Properties p = new Properties();
        FileInputStream in = null;
        ResultSet rs = null;
        boolean mustUpdateDb = false;

        try{
            String dbName = dbHome + "/" + conn.getMetaData().getURL();
            dbName = dbName.replaceAll("[\\\\/:]+", ".");
            dbName = dbName.replaceAll("[.]+", ".");

            File f = new File(dbHome + "/jmetrik-db-version.props");
            if(!f.exists()) f.createNewFile();
            in = new FileInputStream(f);
            p.load(in);
            in.close();

            String currentVersion = p.getProperty(dbName, "jmk-db-not-found");
            if(currentVersion==null || "jmk-db-not-found".equals(currentVersion) || !"version3".equals(currentVersion)){
                logger.info("Updating database: " + dbName);
                dao.updateDatabasesVersion(conn);
            }

        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }catch(IOException ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }finally{
            try{
                if(rs!=null) rs.close();
                if(in!=null) in.close();
            }catch(Exception ex){
                logger.fatal(ex.getMessage(), ex);
                firePropertyChange("error", "", "Error - Check log for details.");
            }

        }
    }

    /**
     * Go to database home and construct a list of all databases
     *
     * @param list
     */
    public void setDatabaseListModel(final JList list){

        SwingWorker<SortedListModel<DatabaseName>, Void> task = new SwingWorker<SortedListModel<DatabaseName>, Void>(){
            
            protected SortedListModel<DatabaseName> doInBackground()throws Exception{
                firePropertyChange("status", "", "Getting database list...");
                firePropertyChange("progress-ind-on", null, null);
                return dao.getDatabaseListModel(prefs.getDatabaseHome());
            }

            protected void done(){
                try{
                    list.setModel(get());
                    firePropertyChange("status", "", "Ready");
                    firePropertyChange("progress-off", null, null);
                }catch(Exception ex){
                    logger.fatal(ex.getMessage(), ex);
                    firePropertyChange("error", "", "Error - Check log for details.");
                    firePropertyChange("progress-off", null, null);
                }
                
            }
        };

        for(PropertyChangeListener l : propertyChangeListeners){
            task.addPropertyChangeListener(l);
        }
        threadPool.execute(task);
    }

    /**
     * Create a list of database tables. For workspace list
     *
     */
    public void setTableListModel(){

        SwingWorker<SortedListModel<DataTableName>, Void> task = new SwingWorker<SortedListModel<DataTableName>, Void>(){

            protected SortedListModel<DataTableName> doInBackground()throws Exception{
                firePropertyChange("status", "", "Getting table list...");
                firePropertyChange("progress-ind-on", null, null);
                return dao.getTableListModel(conn);
            }

            protected void done(){
                try{
                    tableListModel = get();
                    workspaceList.setModel(tableListModel);
                    firePropertyChange("status", "", "Ready");
                    firePropertyChange("progress-off", null, null);
                }catch(Exception ex){
                    logger.fatal(ex.getMessage(), ex);
                    firePropertyChange("error", "", "Error - Check log for details.");
                    firePropertyChange("progress-off", null, null);
                }
            }
        };
        for(PropertyChangeListener l : propertyChangeListeners){
            task.addPropertyChangeListener(l);
        }
        threadPool.execute(task);
    }

    public String getDatabaseHome(){
        return dbHome;
    }
    
    public DatabaseName getDatabaseName(){
        return databaseConnectionURL.getName();
    }

    /**
     * Connect to database and populate database list in worker thread. Note that the
     * database must already exist.
     *
     *
     * @throws SQLException
     */
    public void openDatabase(String dbName)throws SQLException{
        if(conn!=null && !conn.isClosed()){
            //committ transactions and close connection to existing database
            conn.commit();
            conn.close();
        }

        this.databaseConnectionURL = dbFactory.getDatabaseConnectionURL();
        this.databaseConnectionURL.setDatabaseName(dbName);

        currentDbName = new DatabaseName(dbName);
        dataTable.setModel(new EmptyTableModel());
        variableTable.setModel(new EmptyVariableModel());

        conn = DriverManager.getConnection(databaseConnectionURL.getConnectionUrl());

        //check that database structure reflect the current version of software.
        updateDatabaseVersion();

        //populate table list model
        setTableListModel();
        databaseOpened = true;
        firePropertyChange("db-selection", "", currentDbName.toString());

    }

    /**
     * This method closes the connection to the database and resets the interface.
     * Closing is done in the thread pool to allow completion of existing tasks before closing.
     *
     * @throws java.sql.SQLException
     */
    public void closeDatabase(){
        firePropertyChange("status", "", "Closing workspace...");
        try{
            TableModel m = variableTable.getModel();
            if(m instanceof VariableModel){
                ((VariableModel)m).saveData();
            }

            //TODO add saving data table

            if(conn!=null && !conn.isClosed()){
                databaseConnectionURL.setProperty("shutdown", "true");
                DriverManager.getConnection(databaseConnectionURL.getConnectionUrl());
                databaseConnectionURL = null;
                conn.close();
                resetGUI();
            }

        }catch(SQLException ex){
            if((ex.getErrorCode() == 45000) && ("08006".equals(ex.getSQLState()))){
                //normal shutdown
                logger.info("Normal Derby Shutdown: " + this.getDatabaseName());
                resetGUI();
                databaseOpened = false;
            }else{
                logger.fatal(ex.getMessage(), ex);
                firePropertyChange("error", "", "Error - Check log for details.");
            }

        }finally{
            firePropertyChange("status", "", "Ready");
        }

    }

    private void resetGUI(){
        currentDbName = null;
        databaseConnectionURL = null;
        dataTable.setModel(new EmptyTableModel());
        variableTable.setModel(new EmptyVariableModel());
        tableListModel = new SortedListModel<DataTableName>();
        workspaceList.setModel(tableListModel);
        databaseOpened = false;
    }

    /**
     * Returns a connection to the db. The is just the base connection. Connections needed for
     * creating or shutting down a db are done elsewhere.
     *
     * @return
     * @throws SQLException
     */
    public Connection getConnection() {
        return conn;
    }

    public DataTableName getCurrentDataTable(){
        return currentDataTable;
    }
    
    public ArrayList<VariableInfo> getVariables(){
        ArrayList<VariableInfo> variables = null;
        try{
            variables = dao.getAllVariables(conn, currentVariableTable);
        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }
        return variables;
    }

    public boolean tableOpen(){
        return currentDataTable!=null;
    }
    
    public boolean tableSelectionChanged(String tableName){
        if(currentDataTable==null) return true;
        DataTableName dName = new DataTableName(tableName);
        return !currentDataTable.equals(dName);
    }
    
    public void saveTable(){

        //save edited data before changing table
        TableModel tempTable = dataTable.getModel();
        if(tempTable instanceof PagingDataModel){
            PagingDataModel pm = (PagingDataModel)tempTable; 
            pm.saveData();
        }

        //save variable table edits before changing table
        TableModel tempModel = variableTable.getModel();
        if(tempModel instanceof VariableModel){
            VariableModel vm =(VariableModel)tempModel;
            vm.saveData();
        }
        
    }

    /**
     * Opens table called tableName.
     *
     * @param tableName
     */
    public void reloadTable(DataTableName tableName){
        //load data model
        PagingDataModel dataModel = new PagingDataModel(conn, tableName, dao, propertyChangeListeners);
        dataTable.setModel(dataModel);
        currentDataTable = tableName;

        //Load variable model
        VariableTableName cVarTable = new VariableTableName(tableName.toString());
        VariableModel variableModel = new VariableModel(conn, currentDbName, cVarTable, dao, propertyChangeListeners);
        for(VariableChangeListener l : variableChangeListeners){
            variableModel.addVariableChangeListener(l);
        }
        variableTable.setModel(variableModel);
        currentVariableTable = cVarTable;

    }

    /**
     * Opens table if different from current one or current table is null.
     *
     * @param tableName
     */
    public void openTable(DataTableName tableName){

        //only open table if different from current one
        if(currentDataTable!=null && currentDataTable.equals(tableName)) return;

        DataTableName oldTableName = currentDataTable;

        saveTable();
        reloadTable(tableName);

        firePropertyChange("table-selection", oldTableName, tableName);
        firePropertyChange("status", "", "Ready");
    }

    public void loadMoreData(DataTableName tableName){
        PagingDataModel dataModel = new PagingDataModel(conn, tableName, dao, propertyChangeListeners);
        dataTable.setModel(dataModel);
    }
    
    public void openEmptyTable(){
        dataTable.setModel(new EmptyTableModel());
        currentDataTable = null;
        variableTable.setModel(new EmptyVariableModel());
        currentVariableTable = null;
        firePropertyChange("status", "", "Ready");
    }

    public boolean databaseOpened(){
        return databaseOpened;
    }

    /**
     * This method handles any action that must come before any process is run.
     * For example, data table and variable table information is saved before
     * any process is run.
     *
     * @param command
     */
    private void runPreProcess(Command command){

        //save edited data before changing table
        saveTable();

        //If deleting current database, must first close workspace.
        DatabaseCommand dbCommand = new DatabaseCommand();
        if(dbCommand.equals(command)){
            dbCommand = (DatabaseCommand)command;
            String action = dbCommand.getSelectOneOption("action").getSelectedArgument();
            String name = dbCommand.getFreeOption("name").getString();
            DatabaseName dbName = new DatabaseName(name);
            if("delete-db".equals(action)){
                if(this.getDatabaseName().toString().equals(dbName.getName())){
                    this.closeDatabase();
                }
            }else if("delete-table".equals(action)){
                ArrayList<String> nameList = command.getFreeOptionList("tables").getString();
                DataTableName tempName = null;
                for(String s : nameList){
                    tempName = new DataTableName(s);
                    if(currentDataTable!=null && currentDataTable.equals(tempName)){
                        this.openEmptyTable();
                    }
                }
            }
        }

    }

    public void runFromSyntax(String text){
        TextToCommand converter = new TextToCommand();
        String commandName = "";
        String commandSyntax = "";
        JmetrikCommandFactory commandFactory = new JmetrikCommandFactory();
        Command command = null;

        converter.convertToCommands(text);
        Iterator<String[]> iter = converter.iterator();
        String[] sa = null;
        int count = 0;
        while(iter.hasNext()){
            sa = iter.next();
            commandName = sa[0];
            commandSyntax = sa[1];

            command = commandFactory.getCommand(sa[0], sa[1]);
            if(command!=null) runProcess(command);

        }

    }

    /**
     * Run plugin process
     *
     * @param command
     * @param tabbedPane
     * @return true if process found
     */
    private boolean runPluginProcess(Command command, JTabbedPane tabbedPane){
        boolean processFound = false;

        //run plugin processes
        ServiceLoader<JmetrikProcess> loader = ServiceLoader.load(JmetrikProcess.class);
        for(JmetrikProcess proc : loader){
            if(proc.commandMatch(command)){
                processFound = true;
                proc.setCommand(command);
                proc.addVariableChangeListener(new VariableListener());
                proc.addPropertyChangeListener(new DatabaseChangeListener());
                for(PropertyChangeListener l : propertyChangeListeners){
                    proc.addPropertyChangeListener(l);
                }
                proc.runProcess(conn, dbFactory, tabbedPane, threadPool);
                break;
            }
        }

        return processFound;
    }

    /**
     * All actions that have a Command object are run from here.
     * This method runs any necessary pre process, then runs either a base process
     * or a plugin process.
     *
     * @param command
     */
    public void runProcess(Command command){
        
        runPreProcess(command);

        //run base processes here
        JmetrikProcess proc = procFactory.getProcess(command);

        if(proc==null){
            //No base process found. Attempt to run plugin.
            boolean pluginFound = runPluginProcess(command, tabbedPane);
            if(!pluginFound){
                logger.fatal("Process not found: " + command.getName());
                firePropertyChange("error", "", "Error - Check log for details.");
            }
        }else{
            proc.setCommand(command);

            //add variable change listeners to process and thereby the analysis object
            proc.addVariableChangeListener(new VariableListener());
            for(VariableChangeListener l : variableChangeListeners){
                proc.addVariableChangeListener(l);
            }

            //add property change listeners to process and thereby the analysis object
            proc.addPropertyChangeListener(new DatabaseChangeListener());
            for(PropertyChangeListener l : propertyChangeListeners){
                proc.addPropertyChangeListener(l);
            }

            //execute process
            proc.runProcess(conn, dbFactory, tabbedPane, threadPool);
        }

    }

    public JmetrikDatabaseFactory getDatabaseFactory(){
        return dbFactory;
    }


    //===============================================================================================================
    //Process messages here
    //  Note that SwingWorker classes also implement these methods. Just need to add list of
    //  propertyChangeListeners to SwingWorker classes. See importTable(...) for an example.
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
    //Handle variable changes here
    //   -Dialogs will use these methods to add their variable listeners
    //===============================================================================================================


    public synchronized void addVariableChangeListener(VariableChangeListener l){
        TableModel m = variableTable.getModel();
        if(m instanceof VariableModel){
            VariableModel vModel = (VariableModel)m;
            vModel.addVariableChangeListener(l);
        }
        variableChangeListeners.add(l);
    }

    public synchronized void removeVariableChangeListener(VariableChangeListener l){
        TableModel m = variableTable.getModel();
        if(m instanceof VariableModel){
            VariableModel vModel = (VariableModel)m;
            vModel.removeVariableChangeListener(l);
        }
        variableChangeListeners.remove(l);

    }

    public synchronized  void removeAllVariableChangeListeners(){
        TableModel m = variableTable.getModel();
        if(m instanceof VariableModel){
            VariableModel vModel = (VariableModel)m;
            vModel.removeAllVariableChangeListeners();
        }
        variableChangeListeners.clear();
    }

    /**
     * Call this method when ever database or variables are changed from within the Workspace object or
     * an analysis object.
     *
     * @param e
     */
    public synchronized void fireVariableChangeEvent(VariableChangeEvent e){
        for(VariableChangeListener l : variableChangeListeners){
            l.variableChanged(e);
        }
    }
    //===============================================================================================================

    class VariableListener implements VariableChangeListener{

        public void variableChanged(VariableChangeEvent e){
            VariableChangeType changeType = e.getChangeType();
            if(VariableChangeType.VARIABLE_ADDED==changeType || VariableChangeType.VARIABLE_DELETED==changeType ||
                    VariableChangeType.VARIABLE_RENAMED==changeType){
                DataTableName evtTableName = e.getTableName();
                if(evtTableName.equals(currentDataTable)){
                    //current table is reloaded any time a variable is added or deleted
                    reloadTable(e.getTableName());
                }
            }
        }

    }

    /**
     * Implementation of TreeSelectionListener interface
     */
    class JmetrikListSelectionListener implements ListSelectionListener{

        public void valueChanged(ListSelectionEvent e){
            DataTableName tableName = (DataTableName)workspaceList.getSelectedValue();
            if(tableName!=null){
                openTable(tableName);
            }
        }
    }

    class DatabaseChangeListener implements PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if("import".equals(propertyName)){
                //open table and set TreePath to selected table
                DataTableName tName = (DataTableName)e.getNewValue();
                tableListModel.addElement(tName);
                openTable(tName);
                workspaceList.setSelectedValue(tName, true);
            }else if("table-added".equals(propertyName)){
                DataTableName dataTableName = (DataTableName)e.getNewValue();
                tableListModel.addElement(dataTableName);
            }else if("table-deleted".equals(propertyName)){
                DataTableName dataTableName = (DataTableName)e.getNewValue();
                tableListModel.removeElement(dataTableName);
            }else if("table-updated".equals(propertyName)){
                DataTableName dataTableName = (DataTableName)e.getNewValue();
                if(currentDataTable!=null && currentDataTable.equals(dataTableName)){
                    reloadTable(dataTableName);
                }
            }
        }
    }



}

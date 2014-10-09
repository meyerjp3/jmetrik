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

package com.itemanalysis.jmetrik.gui;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.graph.barchart.BarChartProcess;
import com.itemanalysis.jmetrik.graph.histogram.HistogramProcess;
import com.itemanalysis.jmetrik.graph.irt.IrtPlotDialog;
import com.itemanalysis.jmetrik.graph.itemmap.ItemMapProcess;
import com.itemanalysis.jmetrik.graph.linechart.LineChartProcess;
import com.itemanalysis.jmetrik.graph.nicc.NonparametricCurveProcess;
import com.itemanalysis.jmetrik.graph.piechart.PieChartProcess;
import com.itemanalysis.jmetrik.graph.scatterplot.ScatterplotProcess;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.model.VariableModel;
import com.itemanalysis.jmetrik.scoring.BasicScoringProcess;
import com.itemanalysis.jmetrik.scoring.ScoringProcess;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.stats.cmh.CmhProcess;
import com.itemanalysis.jmetrik.stats.correlation.CorrelationProcess;
import com.itemanalysis.jmetrik.graph.density.DensityProcess;
import com.itemanalysis.jmetrik.stats.descriptives.DescriptiveProcess;
import com.itemanalysis.jmetrik.stats.frequency.FrequencyProcess;
import com.itemanalysis.jmetrik.stats.irt.equating.IrtEquatingProcess;
import com.itemanalysis.jmetrik.stats.irt.estimation.IrtPersonScoringProcess;
import com.itemanalysis.jmetrik.stats.irt.linking.IrtLinkingProcess;
import com.itemanalysis.jmetrik.stats.irt.rasch.RaschAnalysisProcess;
import com.itemanalysis.jmetrik.stats.itemanalysis.ItemAnalysisProcess;
import com.itemanalysis.jmetrik.stats.ranking.RankingProcess;
import com.itemanalysis.jmetrik.stats.scaling.TestScalingProcess;
import com.itemanalysis.jmetrik.stats.transformation.LinearTransformationProcess;
import com.itemanalysis.jmetrik.swing.*;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.statusbar.StatusBar;
import com.itemanalysis.jmetrik.utils.JmetrikFileUtils;
import com.itemanalysis.jmetrik.utils.PrintUtilities;
import com.itemanalysis.jmetrik.workspace.*;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableName;
import org.apache.commons.math3.util.Precision;
import org.apache.log4j.Logger;
import sun.print.DialogOwner;

import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.table.JTableHeader;
import javax.swing.text.DefaultEditorKit;
import java.awt.*;
import java.awt.event.*;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TreeMap;

public class Jmetrik extends JFrame{

    final static String APP_NAME = "jMetrik";
    final static String VERSION = "4.0.0";
    final static boolean BETA_VERSION = true;
    final static String RELEASE_DATE = "October 21, 2014";
    final static String COPYRIGHT_YEAR = "2009 - 2014";
    final static String AUTHOR = "J. Patrick Meyer";

    private DataTable dataTable = null;
    private DataTable variableTable = null;
    private StatusBar statusBar = null;
    private Color[] rowColors = null;
    private Workspace workspace = null;
    private JList workspaceList = null;
    private JTabbedPane tabbedPane = null;
    private JmetrikFileUtils fileUtils = null;
    private String importExportPath = ".";
    private PrintRequestAttributeSet attributes = null;
    public final static String JMETRIK_TEXT_FILE = "com.itemanalysis.jmetrik.swing.JmetrikTextFile";
    static Logger logger = Logger.getLogger("jmetrik-logger");

    //Dialog components
    private IrtPlotDialog irtPlotDialog = null;
    private TreeMap<String, JDialog> dialogs = new TreeMap<String, JDialog>();

    public Jmetrik(){
        super("jMetrik");
        setPreferredSize(new Dimension(1024,650));
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //properly close database if user closes window
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                if(workspace!=null){
                    workspace.closeDatabase();
                }
                System.exit(0);
            }
        });

        //add statusbar
        statusBar = new StatusBar(1024,30);
        statusBar.setBorder(new EmptyBorder(2,2,2,2));
        getContentPane().add(statusBar, BorderLayout.SOUTH);

        //start logging
        startLog();

        //left-right splitpane
        JSplitPane splitPane1 = new JSplitPane();
        splitPane1.setDividerLocation(200);

        //setup workspace list
        workspaceList = new JList();
        workspaceList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workspaceList.setModel(new SortedListModel<DataTableName>());
        workspaceList.addKeyListener(new DeleteKeyListener());
//        workspaceList.getInsets().set(5, 5, 5, 5);

        //add icon to list cell renderer
        String urlString = "/images/spreadsheet.png";
        URL url = this.getClass().getResource(urlString);
        final ImageIcon tableIcon = new ImageIcon(url, "Table");
        workspaceList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setIcon(tableIcon);
                return label;
            }
        });

        JScrollPane scrollPane1 = new JScrollPane(workspaceList);
        scrollPane1.setPreferredSize(new Dimension(200, 698));

        splitPane1.setLeftComponent(scrollPane1);

        //tabbed pane for top pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setTabPlacement(JTabbedPane.BOTTOM);

        //setup data table
        dataTable = new DataTable();
        dataTable.setRowHeight(18);

        //change size of table header and center text
        JTableHeader header = dataTable.getTableHeader();
        header.setDefaultRenderer(new TableHeaderCellRenderer());

        JScrollPane dataScrollPane = new JScrollPane(dataTable);
        tabbedPane.addTab("Data", dataScrollPane);

        //setup variable table
        variableTable = new DataTable();
        variableTable.setRowHeight(18);
        variableTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                if(e.getClickCount()==2){
                    JTable target = (JTable)e.getSource();
                    int row = target.getSelectedRow();
                    int col = target.getSelectedColumn();
                    if(col==0){
                        if(target.getModel() instanceof VariableModel){
                            VariableModel vModel = (VariableModel)target.getModel();
                            String s = (String)vModel.getValueAt(row, col);

                            DatabaseName db = workspace.getDatabaseName();
                            DataTableName table = workspace.getCurrentDataTable();

                            RenameVariableDialog renameVariableDialog = new RenameVariableDialog(Jmetrik.this, db, table, s);
                            renameVariableDialog.setVisible(true);

                            if(renameVariableDialog.canRun()){
                                RenameVariableCommand command = renameVariableDialog.getCommand();
                                workspace.runProcess(command);
                            }

                        }//end instanceof

                    }//end if col==0

                }//end if click count==2
            }//end mouse clicked
        });

        //change size of table header and center text
        JTableHeader variableHeader = variableTable.getTableHeader();
        variableHeader.setDefaultRenderer(new TableHeaderCellRenderer());
        variableHeader.setPreferredSize(new Dimension(30, 21));

        JScrollPane variableScrollPane = new JScrollPane(variableTable);
        tabbedPane.addTab("Variables", variableScrollPane);

        splitPane1.setRightComponent(tabbedPane);
        getContentPane().add(splitPane1, BorderLayout.CENTER);

        //add status bar listener - needed to display status message that are generated within this class
        this.addPropertyChangeListener(statusBar.getStatusListener());

        //add listener for displaying error messages - needed to display errors and exceptions
        this.addPropertyChangeListener(new ErrorOccurredPropertyChangeListener());

        //instantiate file utilities
        fileUtils = new JmetrikFileUtils();
        fileUtils.addPropertyChangeListener(statusBar.getStatusListener());
        
        //set import and export path to user's documents folder
        JFileChooser chooser = new JFileChooser();
        FileSystemView fw = chooser.getFileSystemView();
        importExportPath = fw.getDefaultDirectory().toString().replaceAll("\\\\", "/");

        //create and start a workspace
        startWorkspace();

        //create menu bar and tool bar
        this.setJMenuBar(createMenuBar());

        JToolBar toolBar = createToolBar();
        toolBar.setFloatable(false);
        toolBar.setRollover(true);
        getContentPane().add(toolBar,BorderLayout.PAGE_START);

        pack();

    }

    /**
     * Start logging. The log properties file must exist in a fixed location.
     * This location is teh same as that used for storing the application properties.
     * See also JmetrikPropertiesManager.java
     */
    private void startLog(){
        JmetrikPreferencesManager prefs = new JmetrikPreferencesManager();
        prefs.addPropertyChangeListener(statusBar.getStatusListener());
        prefs.loadLog();
    }

    private void closeWorkspace(){
        if(workspace!=null) workspace.closeDatabase();
        workspace = null;
    }

    private void startWorkspace(){
        if(workspace!=null) workspace.closeDatabase();
        workspace = new Workspace(workspaceList, tabbedPane, dataTable, variableTable);
        workspace.addPropertyChangeListener(new TableNameListener());
        workspace.addPropertyChangeListener(statusBar.getStatusListener());
        workspace.addPropertyChangeListener(new ErrorOccurredPropertyChangeListener());
    }
    
    private void openWorkspace(String dbName){
        if(workspace!=null){
            workspace.closeDatabase();
        }

        try{
            workspace.openDatabase(dbName);
            resetDialogs();
        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

    private JMenuBar createMenuBar(){
        final JMenuBar menuBar = new JMenuBar();
        JMenuItem mItem = null;
        String urlString;
        URL url;

        //============================================================================================
        // File Menu
        //============================================================================================
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic('f');
        menuBar.add(fileMenu);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-new.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconNew = new ImageIcon(url, "New");
        mItem = new JMenuItem(new NewTextFileAction("New", iconNew));
        fileMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-open.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconOpen = new ImageIcon(url, "Open");
        mItem = new JMenuItem(new OpenFileAction("Open...", iconOpen, new Integer(KeyEvent.VK_A)));
        fileMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-save.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconSave = new ImageIcon(url, "Save");
        mItem = new JMenuItem(new SaveAction("Save",iconSave,new Integer(KeyEvent.VK_S)));
        fileMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-save-as.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconSaveAs = new ImageIcon(url, "Save As");
        mItem = new JMenuItem(new SaveAsAction("Save As...",iconSaveAs));
        fileMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/status/folder-visiting.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconClose = new ImageIcon(url, "Close All Tabs");
        mItem = new JMenuItem(new CloseAllTabsAction("Close All Tabs...", iconClose, new Integer(KeyEvent.VK_C)));
        fileMenu.add(mItem);

        fileMenu.addSeparator();

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-print.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconPrint = new ImageIcon(url, "Print");
        mItem = new JMenuItem(new PrintAction("Print...",iconPrint));
        fileMenu.add(mItem);

        fileMenu.addSeparator();

//		exit menu item
        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/system-log-out.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconExit = new ImageIcon(url, "Exit");
        mItem = new JMenuItem(new ExitAction("Exit",iconExit));
        fileMenu.add(mItem);

        //============================================================================================
        // Edit Menu
        //============================================================================================
        JMenu editMenu = new JMenu("Edit");
        editMenu.setMnemonic(KeyEvent.VK_E);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-cut.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconCut = new ImageIcon(url, "Cut");
        mItem = new JMenuItem(new DefaultEditorKit.CutAction());
        mItem.setText("Cut");
        mItem.setIcon(iconCut);
        mItem.setMnemonic(KeyEvent.VK_X);
        editMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-copy.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconCopy = new ImageIcon(url, "Copy");
        mItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        mItem.setText("Copy");
        mItem.setIcon(iconCopy);
        mItem.setMnemonic(KeyEvent.VK_C);
        editMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-paste.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconPaste = new ImageIcon(url, "Paste");
        mItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        mItem.setText("Paste");
        mItem.setIcon(iconPaste);
        mItem.setMnemonic(KeyEvent.VK_V);
        editMenu.add(mItem);

        editMenu.addSeparator();

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-undo.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconUndo = new ImageIcon(url, "Undo");
        mItem = new JMenuItem(new UndoAction("Undo",iconUndo,new Integer(KeyEvent.VK_Z)));
        editMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-redo.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconRedo = new ImageIcon(url, "Redo");
        mItem = new JMenuItem(new RedoAction("Redo",iconRedo,new Integer(KeyEvent.VK_Y)));
        editMenu.add(mItem);

        editMenu.addSeparator();

        urlString = "/org/tango-project/tango-icon-theme/16x16/categories/preferences-system.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconView = new ImageIcon(url, "Preferences");
        mItem = new JMenuItem("Preferences");
        mItem.setIcon(iconView);
        mItem.setToolTipText("Edit jMetrik preferences");
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JmetrikPreferencesManager prefs = new JmetrikPreferencesManager();
                prefs.addPropertyChangeListener(new ErrorOccurredPropertyChangeListener());
                prefs.addPropertyChangeListener(statusBar.getStatusListener());
                JmetrikPreferencesDialog propDialog = new JmetrikPreferencesDialog(Jmetrik.this, prefs);
//                propDialog.loadPreferences();
                propDialog.setVisible(true);
            }
        });
        editMenu.setMnemonic('e');
        editMenu.add(mItem);

        menuBar.add(editMenu);

        //============================================================================================
        // Log Menu
        //============================================================================================
        JMenu logMenu = new JMenu("Log");

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-properties.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconLog = new ImageIcon(url, "View Log");
        mItem = new JMenuItem(new ViewLogAction("View Log",iconLog));
        logMenu.setMnemonic('l');
        logMenu.add(mItem);

        menuBar.add(logMenu);


        //============================================================================================
        // Manage Menu
        //============================================================================================
        JMenu manageMenu = new JMenu("Manage");
        manageMenu.setMnemonic('m');

        mItem = new JMenuItem("New Database...");//create db
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                NewDatabaseDialog newDatabaseDialog = new NewDatabaseDialog(Jmetrik.this);
                newDatabaseDialog.setVisible(true);
                if(newDatabaseDialog.canRun()){
                    if(workspace==null) {
//                        workspace = new Workspace(workspaceTree, tabbedPane, dataTable, variableTable);
                        workspace = new Workspace(workspaceList, tabbedPane, dataTable, variableTable);
                        workspace.addPropertyChangeListener(statusBar.getStatusListener());
                        workspace.addPropertyChangeListener(new ErrorOccurredPropertyChangeListener());
                    }
                    workspace.runProcess(newDatabaseDialog.getCommand());
//                    workspace.createDatabase(newDatabaseDialog.getCommand());
                }
            }
        });
        manageMenu.add(mItem);

        mItem = new JMenuItem("Open Database...");
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenDatabaseDialog openDbDialog = new OpenDatabaseDialog(Jmetrik.this, "Open");
                JList l = openDbDialog.getDatabaseList();
                workspace.setDatabaseListModel(l);
                openDbDialog.setVisible(true);
                if(openDbDialog.canRun()){
                    openWorkspace(openDbDialog.getDatabaseName());
                }
            }
        });
        manageMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-delete.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconDelete = new ImageIcon(url, "Delete");
        mItem = new JMenuItem("Delete Database...");
        mItem.setIcon(iconDelete);
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                OpenDatabaseDialog selectDialog = new OpenDatabaseDialog(Jmetrik.this, "Delete");
                JList l = selectDialog.getDatabaseList();
                workspace.setDatabaseListModel(l);
                selectDialog.setVisible(true);
                if(selectDialog.canRun()){
                    int answer = JOptionPane.showConfirmDialog(Jmetrik.this,
                            "Do you want to delete " + selectDialog.getDatabaseName() + " and all of its contents? \n" +
                                    "All data will be permanently deleted. You cannot undo this action.",
                            "Delete Database",
                            JOptionPane.WARNING_MESSAGE,
                            JOptionPane.YES_NO_OPTION);

                    if(answer==JOptionPane.YES_OPTION){
                        DatabaseCommand command = new DatabaseCommand();
                        DatabaseName dbName = new DatabaseName(selectDialog.getDatabaseName());
                        command.getFreeOption("name").add(dbName.getName());
                        command.getSelectOneOption("action").setSelected("delete-db");

                        DatabaseName currentDb = workspace.getDatabaseName();
                        if(currentDb.getName().equals(dbName.getName())){
                            JOptionPane.showMessageDialog(Jmetrik.this, "You cannot delete the current database.\n" +
                                    "Close the database before attempting to delete it.",
                                    "Database Delete Error",
                                    JOptionPane.WARNING_MESSAGE);
                        }else{
                            workspace.runProcess(command);
                        }


                    }

                }
            }
        });
        manageMenu.add(mItem);

        manageMenu.addSeparator();

        urlString = "/org/tango-project/tango-icon-theme/16x16/apps/accessories-text-editor.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconDesc = new ImageIcon(url, "Descriptions");
        mItem = new JMenuItem("Table Descriptions...");
        mItem.setIcon(iconDesc);
        mItem.addActionListener(new TableDescriptionActionListener());
        manageMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/list-add.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconImport = new ImageIcon(url, "Import");
        mItem = new JMenuItem("Import Data...");
        mItem.setIcon(iconImport);
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(workspace.databaseOpened()){
                    ImportDialog importDialog = new ImportDialog(Jmetrik.this, workspace.getDatabaseName(), importExportPath);
                    importDialog.setVisible(true);

                    if(importDialog.canRun()){
                        importExportPath = importDialog.getCurrentDirectory();
                        workspace.runProcess(importDialog.getCommand());
                    }
                }else{
                    JOptionPane.showMessageDialog(Jmetrik.this, "You must open a database before importing data.",
                            "No Open Database", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
        manageMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/format-indent-less.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconExport = new ImageIcon(url, "Export");
        mItem = new JMenuItem("Export Data...");
        mItem.setIcon(iconExport);
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                DataTableName tableName = (DataTableName)workspaceList.getSelectedValue();
                if(!workspace.databaseOpened()){
                    JOptionPane.showMessageDialog(Jmetrik.this, "You must open a database before exporting data.",
                            "No Open Database", JOptionPane.ERROR_MESSAGE);
                }else if(tableName==null){
                    JOptionPane.showMessageDialog(Jmetrik.this, "You must select a table in the workspace list. \n " +
                            "Select a table to continue the export.", "No Table Selected", JOptionPane.ERROR_MESSAGE);
                }else{
                    ExportDataDialog exportDialog = new ExportDataDialog(Jmetrik.this, workspace.getDatabaseName(), tableName, importExportPath);
                    if(exportDialog.canRun()){
                        importExportPath = exportDialog.getCurrentDirectory();
                        workspace.runProcess(exportDialog.getCommand());
                    }
                }
            }
        });
        manageMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-delete.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconDeleteTable = new ImageIcon(url, "Delete");
        mItem = new JMenuItem("Delete Table...");
        mItem.setIcon(iconDeleteTable);
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(workspace.databaseOpened()){
                    DeleteTableDialog deleteDialog = new DeleteTableDialog(Jmetrik.this, workspace.getDatabaseName(), (SortedListModel<DataTableName>)workspaceList.getModel());
                    deleteDialog.setVisible(true);
                    if(deleteDialog.canRun()){
                        int nSelected = deleteDialog.getNumberOfSelectedTables();
                        int answer = JOptionPane.NO_OPTION;
                        if(nSelected>1){
                            answer = JOptionPane.showConfirmDialog(Jmetrik.this,
                                    "Do you want to delete these " + nSelected + " tables? \n" +
                                    "All data will be permanently deleted. You cannot undo this action.",
                                    "Delete Database",
                                    JOptionPane.WARNING_MESSAGE,
                                    JOptionPane.YES_NO_OPTION);
                        }else{
                            ArrayList<DataTableName> dList = deleteDialog.getSelectedTables();
                            answer = JOptionPane.showConfirmDialog(Jmetrik.this,
                                    "Do you want to delete the table " + dList.get(0).getTableName() + "? \n" +
                                            "All data will be permanently deleted. You cannot undo this action.",
                                    "Delete Database",
                                    JOptionPane.WARNING_MESSAGE,
                                    JOptionPane.YES_NO_OPTION);
                        }
                        if(answer==JOptionPane.YES_OPTION){
                            workspace.runProcess(deleteDialog.getCommand());
                        }

                    }
                }else{
                    JOptionPane.showMessageDialog(Jmetrik.this, "You must open a database before deleting a table.",
                            "No Open Database", JOptionPane.ERROR_MESSAGE);
                }


            }
        });
        manageMenu.add(mItem);

        manageMenu.addSeparator();

        SubsetCasesProcess subsetCasesProcess = new SubsetCasesProcess();
        subsetCasesProcess.addMenuItem(Jmetrik.this, manageMenu, dialogs, workspace, workspaceList);

        SubsetVariablesProcess subsetVariablesProcess = new SubsetVariablesProcess();
        subsetVariablesProcess.addMenuItem(Jmetrik.this, manageMenu, dialogs, workspace, workspaceList);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-delete.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconDeleteVariables = new ImageIcon(url, "Delete Variables");
        mItem = new JMenuItem("Delete Variables...");
        mItem.setIcon(iconDeleteVariables);
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTableName tableName = (DataTableName)workspaceList.getSelectedValue();
                if(!workspace.databaseOpened()){
                    JOptionPane.showMessageDialog(Jmetrik.this, "You must open a database before subsetting data.",
                            "No Open Database", JOptionPane.ERROR_MESSAGE);
                }else if(tableName==null){
                    JOptionPane.showMessageDialog(Jmetrik.this, "You must select a table in the workspace list. \n " +
                            "Select a table to continue.", "No Table Selected", JOptionPane.ERROR_MESSAGE);
                }else if(workspace.tableOpen()){
                    DeleteVariableDialog deleteVariableDialog = new DeleteVariableDialog(
                            Jmetrik.this,
                            workspace.getDatabaseName(),
                            workspace.getCurrentDataTable(),
                            workspace.getVariables());
                    deleteVariableDialog.setVisible(true);
                    if(deleteVariableDialog.canRun()){
                        int nSelected = deleteVariableDialog.getNumberOfSelectedVariables();
                        int answer = JOptionPane.NO_OPTION;
                        if(nSelected>1){
                            answer = JOptionPane.showConfirmDialog(Jmetrik.this,
                                    "Do you want to delete these " + nSelected + " variables? \n" +
                                    "All data will be permanently deleted. You cannot undo this action.",
                                    "Delete Variables",
                                    JOptionPane.WARNING_MESSAGE,
                                    JOptionPane.YES_NO_OPTION);
                        }else{
                            VariableInfo v = deleteVariableDialog.getSelectedVariable();
                            answer = JOptionPane.showConfirmDialog(Jmetrik.this,
                                    "Do you want to delete the variable " + v.getName().toString() + "? \n" +
                                            "All data will be permanently deleted. You cannot undo this action.",
                                    "Delete Database",
                                    JOptionPane.WARNING_MESSAGE,
                                    JOptionPane.YES_NO_OPTION);
                        }
                        if(answer==JOptionPane.YES_OPTION){
                            workspace.runProcess(deleteVariableDialog.getCommand());
                        }
                    }
                }

            }
        });
        manageMenu.add(mItem);

        menuBar.add(manageMenu);


        //============================================================================================
        // Transform Menu
        //============================================================================================
        JMenu transformMenu = new JMenu("Transform");
        transformMenu.setMnemonic('t');

        BasicScoringProcess basicScoringProcess = new BasicScoringProcess();
        basicScoringProcess.addMenuItem(Jmetrik.this, transformMenu, dialogs, workspace, workspaceList);

        ScoringProcess scoringProcess = new ScoringProcess();
        scoringProcess.addMenuItem(Jmetrik.this, transformMenu, dialogs, workspace, workspaceList);

        transformMenu.addSeparator();

        RankingProcess rankingProcess = new RankingProcess();
        rankingProcess.addMenuItem(Jmetrik.this, transformMenu, dialogs, workspace, workspaceList);

        TestScalingProcess testScalingProcess = new TestScalingProcess();
        testScalingProcess.addMenuItem(Jmetrik.this, transformMenu, dialogs, workspace, workspaceList);

        LinearTransformationProcess linearTransformationProcess = new LinearTransformationProcess();
        linearTransformationProcess.addMenuItem(Jmetrik.this, transformMenu, dialogs, workspace, workspaceList);

        transformMenu.addSeparator();

        IrtLinkingProcess irtLinkingProcess = new IrtLinkingProcess();
        irtLinkingProcess.addMenuItem(Jmetrik.this, transformMenu, dialogs, workspace, workspaceList);

        IrtEquatingProcess irtEquatingProcess = new IrtEquatingProcess();
        irtEquatingProcess.addMenuItem(Jmetrik.this, transformMenu, dialogs, workspace, workspaceList);

        menuBar.add(transformMenu);


        //============================================================================================
        // Analyze Menu
        //============================================================================================
        JMenu analyzeMenu = new JMenu("Analyze");
        analyzeMenu.setMnemonic('a');

        FrequencyProcess frequencyProcess = new FrequencyProcess();
        frequencyProcess.addMenuItem(Jmetrik.this, analyzeMenu, dialogs, workspace, workspaceList);

        DescriptiveProcess descriptiveProcess = new DescriptiveProcess();
        descriptiveProcess.addMenuItem(Jmetrik.this, analyzeMenu, dialogs, workspace, workspaceList);

        CorrelationProcess correlationProcess = new CorrelationProcess();
        correlationProcess.addMenuItem(Jmetrik.this, analyzeMenu, dialogs, workspace, workspaceList);

        analyzeMenu.addSeparator();

        ItemAnalysisProcess itemAnalysisProcess = new ItemAnalysisProcess();
        itemAnalysisProcess.addMenuItem(Jmetrik.this, analyzeMenu, dialogs, workspace, workspaceList);

        CmhProcess cmhProcess = new CmhProcess();
        cmhProcess.addMenuItem(Jmetrik.this, analyzeMenu, dialogs, workspace, workspaceList);

        analyzeMenu.addSeparator();

        RaschAnalysisProcess raschAnalysisProcess = new RaschAnalysisProcess();
        raschAnalysisProcess.addMenuItem(Jmetrik.this, analyzeMenu, dialogs, workspace, workspaceList);

        mItem = new JMenuItem("Item Calibration (MMLE)...");
        mItem.setEnabled(false);
        analyzeMenu.add(mItem);

        IrtPersonScoringProcess irtPersonScoringProcess = new IrtPersonScoringProcess();
        irtPersonScoringProcess.addMenuItem(Jmetrik.this, analyzeMenu, dialogs, workspace, workspaceList);

        menuBar.add(analyzeMenu);


        //============================================================================================
        // Graph Menu
        //============================================================================================
        JMenu graphMenu = new JMenu("Graph");
        graphMenu.setMnemonic('g');

        BarChartProcess barchartProcess = new BarChartProcess();
        barchartProcess.addMenuItem(Jmetrik.this, graphMenu, dialogs, workspace, workspaceList);

        PieChartProcess piechartProcess = new PieChartProcess();
        piechartProcess.addMenuItem(Jmetrik.this, graphMenu, dialogs, workspace, workspaceList);

        graphMenu.addSeparator();

        HistogramProcess histogramProcess = new HistogramProcess();
        histogramProcess.addMenuItem(Jmetrik.this, graphMenu, dialogs, workspace, workspaceList);

        DensityProcess densityProcess= new DensityProcess();
        densityProcess.addMenuItem(Jmetrik.this, graphMenu, dialogs, workspace, workspaceList);

        LineChartProcess lineChartProcess = new LineChartProcess();
        lineChartProcess.addMenuItem(Jmetrik.this, graphMenu, dialogs, workspace, workspaceList);

        ScatterplotProcess scatterplotProcess = new ScatterplotProcess();
        scatterplotProcess.addMenuItem(Jmetrik.this, graphMenu, dialogs, workspace, workspaceList);

        graphMenu.addSeparator();

        NonparametricCurveProcess nonparametricCurveProcess = new NonparametricCurveProcess();
        nonparametricCurveProcess.addMenuItem(Jmetrik.this, graphMenu, dialogs, workspace, workspaceList);

        mItem = new JMenuItem("Irt Plot...");
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                DataTableName tableName = (DataTableName) workspaceList.getSelectedValue();
                if(tableName==null){
                    JOptionPane.showMessageDialog(Jmetrik.this, "You must open a database and select a table. \n " +
                            "Select a table to continue scoring.", "No Table Selected", JOptionPane.ERROR_MESSAGE);
                }else {
                    if(irtPlotDialog==null && workspace.tableOpen()){

                        //Note that starting this dialog is different because variables
                        //names must be obtained from the rows of a table.

                        DatabaseAccessObject dao = workspace.getDatabaseFactory().getDatabaseAccessObject();

                        try{
                            ArrayList<VariableInfo> tempVar = dao.getVariableInfoFromColumn(workspace.getConnection(), workspace.getCurrentDataTable(), new VariableName("name"));
                            irtPlotDialog = new IrtPlotDialog(Jmetrik.this, workspace.getDatabaseName(), tableName, tempVar);
                        }catch(SQLException ex){
                            logger.fatal(ex.getMessage(), ex);
                            firePropertyChange("error", "", "Error - Check log for details.");
                        }
                    }
                    if(irtPlotDialog!=null) irtPlotDialog.setVisible(true);
                }

                if(irtPlotDialog!=null && irtPlotDialog.canRun()){
                    workspace.runProcess(irtPlotDialog.getCommand());
                }
            }
        });
        graphMenu.add(mItem);

        ItemMapProcess itemMapProcess = new ItemMapProcess();
        itemMapProcess.addMenuItem(Jmetrik.this, graphMenu, dialogs, workspace, workspaceList);



        menuBar.add(graphMenu);

        //============================================================================================
        // Command Menu
        //============================================================================================

        JMenu commandMenu = new JMenu("Commands");
        commandMenu.setMnemonic('c');
        mItem = new JMenuItem("Run command");
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollPane pain = (JScrollPane)tabbedPane.getSelectedComponent();
                JViewport vp = pain.getViewport();
                Component c = vp.getComponent(0);
                if(c instanceof JmetrikTextFile){
                    JmetrikTab tempTab = (JmetrikTab)tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex());
                    JmetrikTextFile textFile = (JmetrikTextFile)c;
                    workspace.runFromSyntax(textFile.getText());
                }
            }
        });
        commandMenu.add(mItem);

        mItem = new JMenuItem("Stop command");
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //add something
            }
        });
        mItem.setEnabled(false);
        commandMenu.add(mItem);

        mItem = new JMenuItem("Command Reference...");
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //add something
            }
        });
        mItem.setEnabled(false);
        commandMenu.add(mItem);

        menuBar.add(commandMenu);

        //============================================================================================
        // Help Menu
        //============================================================================================
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic('h');
        mItem = new JMenuItem("Quick Start Guide");
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Desktop deskTop = Desktop.getDesktop();
                try{
                    URI uri = new URI("http://www.itemanalysis.com/quick-start-guide.php");
                    deskTop.browse(uri);
                }catch(URISyntaxException ex){
                    logger.fatal(ex.getMessage(), ex);
                    firePropertyChange("error", "", "Error - Check log for details.");
                }catch(IOException ex){
                    logger.fatal(ex.getMessage(), ex);
                    firePropertyChange("error", "", "Error - Check log for details.");
                }
            }
        });
        helpMenu.add(mItem);

        urlString = "/org/tango-project/tango-icon-theme/16x16/apps/help-browser.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconAbout = new ImageIcon(url, "About");
        mItem = new JMenuItem("About");
        mItem.setIcon(iconAbout);
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JmetrikAboutDialog aboutDialog = new JmetrikAboutDialog(Jmetrik.this,
                        APP_NAME, VERSION, AUTHOR, RELEASE_DATE, COPYRIGHT_YEAR, BETA_VERSION);
                aboutDialog.setVisible(true);
            }
        });
        helpMenu.add(mItem);

        menuBar.add(helpMenu);

        return menuBar;
    }

    private JToolBar createToolBar(){
        String urlString;
        URL url;
        final JToolBar tools = new JToolBar("jMetrik Tool Bar");
        JButton button;

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-new.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconNew = new ImageIcon(url, "New");
        button = new JButton(new NewTextFileAction("", iconNew));
        tools.add(button);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-open.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconOpen = new ImageIcon(url, "Open");
        button = new JButton(new OpenFileAction("",iconOpen));
        tools.add(button);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-save.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconSave = new ImageIcon(url, "Save As");
        button = new JButton(new SaveAction("",iconSave));
        tools.add(button);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-save-as.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconSaveAs = new ImageIcon(url, "Save As");
        button = new JButton(new SaveAsAction("",iconSaveAs));
        tools.add(button);

        tools.addSeparator();

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/document-print.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconPrint = new ImageIcon(url, "Print");
        button = new JButton(new PrintAction("",iconPrint));
        tools.add(button);

        tools.addSeparator();

//        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-cut.png";
//        url = this.getClass().getResource( urlString );
//        ImageIcon iconCut = new ImageIcon(url, "Cut");
//        button = new JButton(new DefaultEditorKit.CutAction());
//        button.setText("");
//        button.setIcon(iconCut);
//        tools.addArgument(button);
//
//        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-copy.png";
//        url = this.getClass().getResource( urlString );
//        ImageIcon iconCopy = new ImageIcon(url, "Copy");
//        button = new JButton(new DefaultEditorKit.CopyAction());
//        button.setText("");
//        button.setIcon(iconCopy);
//        tools.addArgument(button);
//
//        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-paste.png";
//        url = this.getClass().getResource( urlString );
//        ImageIcon iconPaste = new ImageIcon(url, "Paste");
//        button = new JButton(new DefaultEditorKit.PasteAction());
//        button.setIcon(iconPaste);
//        button.setText("");
//        tools.addArgument(button);
//
//        tools.addSeparator();

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-undo.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconUndo = new ImageIcon(url, "Undo");
        button = new JButton(new UndoAction("",iconUndo));
        tools.add(button);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-redo.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconRedo = new ImageIcon(url, "Redo");
        button = new JButton(new RedoAction("",iconRedo));
        tools.add(button);

        tools.addSeparator();

        urlString = "/org/tango-project/tango-icon-theme/16x16/apps/accessories-text-editor.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconDesc = new ImageIcon(url, "Descriptions");
        button = new JButton("", iconDesc);
        button.setToolTipText("View table descriptions");
        button.addActionListener(new TableDescriptionActionListener());
        tools.add(button);

        tools.addSeparator();

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/media-playback-start.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconStart = new ImageIcon(url, "Run Analysis from Syntax");
        button = new JButton(iconStart);
        button.setToolTipText("Run commands");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JScrollPane pain = (JScrollPane)tabbedPane.getSelectedComponent();
                JViewport vp = pain.getViewport();
                Component c = vp.getComponent(0);
                if(c instanceof JmetrikTextFile){
                    JmetrikTab tempTab = (JmetrikTab)tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex());
                    JmetrikTextFile textFile = (JmetrikTextFile)c;
                    workspace.runFromSyntax(textFile.getText());
                }
            }
        });
        tools.add(button);

        urlString = "/org/tango-project/tango-icon-theme/16x16/actions/media-playback-stop.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconStopThreads = new ImageIcon(url, "Stop Analysis");
//        button = new JButton(new StopAllThreadsAction("",iconStopThreads));
        button = new JButton(iconStopThreads);
        tools.add(button);

        tools.addSeparator();

        urlString = "/org/tango-project/tango-icon-theme/16x16/apps/utilities-system-monitor.png";
        url = this.getClass().getResource(urlString);
        ImageIcon iconStartMemoryMonitor = new ImageIcon(url, "Start Memory Monitor");
        button = new JButton(iconStartMemoryMonitor);
        button.setToolTipText("View memory allocation");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                double m = (double)Runtime.getRuntime().maxMemory();
                double f = (double)Runtime.getRuntime().freeMemory();
                double t = (double)Runtime.getRuntime().totalMemory();
                f = f/1048576.0;
                t = t/1048576.0;
                m = m/1048576.0;
                double amem = Precision.round(f/t*100.0, 2);

                JOptionPane.showMessageDialog(Jmetrik.this,
                        "Total memory available: " + Precision.round(m,2) + " MB\n" +
                        "Current allocation: " + Precision.round(t,2) + " MB\n" +
                        "Current amount free: " + Precision.round(f,2) + " MB (" + amem + "%)",
                        "JVM Memory Available",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        tools.add(button);

        urlString = "/org/tango-project/tango-icon-theme/16x16/status/folder-visiting.png";
        url = this.getClass().getResource( urlString );
        ImageIcon iconCloseAllTabs = new ImageIcon(url, "Close All");
        button = new JButton(new CloseAllTabsAction("",iconCloseAllTabs));
        tools.add(button);

        return tools;
    }
    
    public void addTab(String title, Component p){
        final Component P = p;
        tabbedPane.addTab(null, p);
        JmetrikTab jTab = new JmetrikTab(title);
        jTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int closeTabNumber = tabbedPane.indexOfComponent(P);
                JScrollPane sp = (JScrollPane)tabbedPane.getComponentAt(closeTabNumber);
                JViewport vp = sp.getViewport();

                if(vp.getComponent(0).getClass().getName().equals(JMETRIK_TEXT_FILE)){
                    int result = ((JmetrikTextFile)vp.getComponent(0)).promptToSave(Jmetrik.this);
                    if(result==JOptionPane.YES_OPTION || result==JOptionPane.NO_OPTION){
                        tabbedPane.removeTabAt(closeTabNumber);
                    }
                }
            }
        });
        tabbedPane.setTabComponentAt(tabbedPane.getTabCount()-1, jTab);
        tabbedPane.setSelectedIndex(tabbedPane.getTabCount()-1);

    }

    public static void main(String args[]){

        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                try {
                    Color SELECTED_COLOR = new Color(184, 204, 217);
                    Color BASE_COLOR = new Color(220,231,243, 50);
                    Color ALT_COLOR = new Color(220,231,243,115);

//                    Insets MENU_INSETS = new Insets(1,12,2,5);//default values
//                    Font MENU_FONT = new Font("SansSerif ", Font.PLAIN, 12);//default values



//                    UIManager.put("nimbusBase", BASE_COLOR);
                    UIManager.put("nimbusSelection", SELECTED_COLOR);
                    UIManager.put("nimbusSelectionBackground", SELECTED_COLOR);
                    UIManager.put("Menu[Enabled+Selected].backgroundPainter", SELECTED_COLOR);

                    //override defaults
                    for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                        if (info.getName().equals("Nimbus")) {
                            UIManager.setLookAndFeel(info.getClassName());
                            UIDefaults defaults = UIManager.getLookAndFeelDefaults();
                            defaults.put("Table.gridColor", Color.lightGray);
                            defaults.put("Table.disabled", false);
                            defaults.put("Table.showGrid", true);
                            defaults.put("Table.intercellSpacing", new Dimension(1, 1));
                            break;
                        }
                    }

//                    UIManager.put("TitledBorder.position", TitledBorder.TOP);



//                    UIManager.put("Table.showGrid", true);
//                    UIManager.put("Table.gridColor", Color.RED);
//                    UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
                }catch(UnsupportedLookAndFeelException ulafe){
//                    logger.fatal("Substance failed to set", ulafe);
                }catch(Exception ex){
//                    logger.fatal(ex.getMessage(), ex);
                }

                JFrame frame = new Jmetrik();

//				set window to maximum size but account for taskbar
                GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
                Rectangle rect = env.getMaximumWindowBounds();
                int width = Double.valueOf(rect.getWidth()-1.0).intValue();
                int height = Double.valueOf(rect.getHeight()-1.0).intValue();
                frame.setMaximizedBounds(new Rectangle(0,0,width, height));
                frame.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
                frame.pack();
//				frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                frame.setVisible(true);


                //check for updates to jmetrik
                ((Jmetrik)frame).checkForUpdates();
            }
        });

    }

    /**
     * Saving file happen in its own thread
     */
    private void save(JmetrikTextFile textFile, JmetrikTab tab){
        File f = null;
        if(!textFile.hasFile()){
            f = fileUtils.chooseFileToSave(Jmetrik.this, textFile.getFile());
            if(f!=null){
                Toolkit.getDefaultToolkit().beep();
                int question = JOptionPane.showConfirmDialog(Jmetrik.this,
                        f.getName() + " already fileExists. Do you want to overwrite it?",
                        "File Exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if(question==JOptionPane.YES_OPTION) {
                    textFile.fileSave(f, tab);
                }
            }
        }else{
            textFile.fileSave(tab);
        }
    }

    private void saveAs(JmetrikTextFile textFile, JmetrikTab tab){
        File f = fileUtils.chooseFileToSave(Jmetrik.this, textFile.getFile());
        if(f!=null){
            if(f.exists()){
                Toolkit.getDefaultToolkit().beep();
                int question = JOptionPane.showConfirmDialog(Jmetrik.this,
                        f.getName() + " already fileExists. Do you want to overwrite it?",
                        "File Exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);
                if(question==JOptionPane.YES_OPTION) {
                    textFile.fileSave(f, tab);
                }
            }else{
                textFile.fileSave(f, tab);
            }

        }
    }

    private void resetDialogs(){

        irtPlotDialog = null;

        for(String s : dialogs.keySet()){
            JDialog d = dialogs.get(s);
            d.dispose();
            d = null;
        }

        dialogs.clear();
    }

    private void checkForUpdates(){
        URL url = null;
        URLConnection urlConn = null;
        BufferedReader br = null;
        logger.info("Checking for updates...");

        try{
            url = new URL("http://www.itemanalysis.com/version/jmetrik-version.txt");
            urlConn = url.openConnection();

            urlConn.setDoInput(true);
            urlConn.setUseCaches(false);

            br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String s = "";
            String[] availableVersion = null;
            String[] currentVersion = VERSION.split("\\.");
            int needUpdate = 0;

            while((s=br.readLine())!=null){
                availableVersion = s.trim().split("\\.");
            }
            br.close();

            if(currentVersion.length < availableVersion.length){
                needUpdate++;
            }

            for(int i=0;i<currentVersion.length;i++){
                if(Integer.parseInt(currentVersion[i].trim())<Integer.parseInt(availableVersion[i].trim())){
                    needUpdate++;
                }
            }
            if(needUpdate>0){
                showUpdateResults(needUpdate>0);
                logger.info("jMetrik updates available. Please go to www.ItemAnalysis.com and download the new version.");
            }else{
                logger.info("No updates available. You have the most current version of jMetrik.");
            }

        }catch(MalformedURLException ex){
            logger.fatal("Could not access update information: MalformedURLException");
        }catch(IOException ex){
            logger.fatal("Could not access update information: IOException");
        }
    }

    private void showUpdateResults(boolean updateAvailable){
        String text = "";
        if(updateAvailable){
            text =   "<html><body>jMetrik Update Available. <br>" +
            "Go to  <a href=http://www.itemanalysis.com/jmetrik_download.php>http://www.itemanalysis.com/jmetrik-download.php</a><br>" +
            "and download the new version.<br>" +
            "</body></html>";
        }else{
            text =   "<html><body>No Update Available. <br>" +
            "You have the most current version of jMetrik. <br></body></html>";
        }

        final JEditorPane p = new JEditorPane("text/html", text);
        p.setEditable(false);
        p.addHyperlinkListener( new HyperlinkListener() {
        public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
                    Desktop deskTop = Desktop.getDesktop();
                    try{
                        URI uri = new URI("http://www.itemanalysis.com/jmetrik-download.php");
                        deskTop.browse(uri);
                    }catch(URISyntaxException ex){
                        logger.fatal(ex.getMessage(), ex);
                    }catch(IOException ex){
                        logger.fatal(ex.getMessage(), ex);
                    }
                }
            }
        });
        JOptionPane.showMessageDialog(Jmetrik.this, p, "jMetrik Update Status", JOptionPane.INFORMATION_MESSAGE);
    }

    //=================================================================================================================
    // Menu Actions
    //=================================================================================================================

    public class NewTextFileAction extends AbstractAction{
        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "New text file";

        public NewTextFileAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public NewTextFileAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public NewTextFileAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            newDocument();
        }

        public void newDocument(){
            String userHome = null;
            String name = "New File.txt";
            JmetrikTextFile textFile = new JmetrikTextFile();

            textFile.addPropertyChangeListener(statusBar.getStatusListener());
            JScrollPane p = new JScrollPane(textFile);
            p.setPreferredSize(new Dimension(730,550));
            addTab(name, p);



        }

    } // end NewAction

    public class OpenFileAction extends AbstractAction{
        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Open text file";

        public OpenFileAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public OpenFileAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public OpenFileAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            openDocument();
        }

        public void openDocument(){
            File f = fileUtils.chooseFileToOpen(Jmetrik.this);
            if(f!=null){
                JmetrikTextFile textFile = new JmetrikTextFile();
                textFile.addPropertyChangeListener(statusBar.getStatusListener());

                JScrollPane p = new JScrollPane(textFile);
                p.setPreferredSize(new Dimension(730,550));

                textFile.openFile(f);
                addTab(f.getName(), p);


            }
        }
    } // end OpenAction

    public class CloseAllTabsAction extends AbstractAction{
        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Close all tabs without saving";

        public CloseAllTabsAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public CloseAllTabsAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public CloseAllTabsAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){

            int count = tabbedPane.getTabCount();

            if(count>2){
                int result = JOptionPane.showConfirmDialog(Jmetrik.this,
                    "Close all tabs without saving?",
                    "Close All Tabs",
                    JOptionPane.YES_NO_OPTION);
                if(result==JOptionPane.YES_OPTION){
                    while(count>2){
                        tabbedPane.removeTabAt(count-1);
                        count = tabbedPane.getTabCount();
                    }
                }
            }


        }


    } // end Action

    public class SaveAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Save text file";

        public SaveAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public SaveAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public SaveAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            JScrollPane pain = (JScrollPane)tabbedPane.getSelectedComponent();
            JViewport vp = pain.getViewport();
            Component c = vp.getComponent(0);
            if(c instanceof JmetrikTextFile){
                JmetrikTab tempTab = (JmetrikTab)tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex());
                JmetrikTextFile textFile = (JmetrikTextFile)c;
                save(textFile, tempTab);
            }
        }
    }//end save action

    public class SaveAsAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Save As text file";


        public SaveAsAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public SaveAsAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public SaveAsAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            JScrollPane pain = (JScrollPane)tabbedPane.getSelectedComponent();
            JViewport vp = pain.getViewport();
            Component c = vp.getComponent(0);
            if(c instanceof JmetrikTextFile){
                JmetrikTab tempTab = (JmetrikTab)tabbedPane.getTabComponentAt(tabbedPane.getSelectedIndex());
                JmetrikTextFile textFile = (JmetrikTextFile)c;
                saveAs(textFile, tempTab);
            }
        }
    }//end SaveAsAction

    public class PrintAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Print text file";

        public PrintAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public PrintAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public PrintAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            if(!tabbedPane.getSelectedComponent().getClass().getName().equals("javax.swing.JPanel")){
                JScrollPane pain = (JScrollPane)tabbedPane.getSelectedComponent();
                JViewport vp = pain.getViewport();
                Component c = vp.getComponent(0);
                if(c instanceof JmetrikTextFile){
                    JmetrikTextFile textFile = (JmetrikTextFile)c;
                    PrintUtilities printer=new PrintUtilities(textFile);
                    try{
                        PrinterJob job = PrinterJob.getPrinterJob();
                        job.setPrintable(printer);
                        if(attributes==null){
                            attributes = new HashPrintRequestAttributeSet();
//                            attributes.addArgument(DialogTypeSelection.NATIVE);
                            attributes.add(new DialogOwner(Jmetrik.this));
                        }

                        boolean ok = job.printDialog(attributes);

                        if(ok) job.print(attributes);

                    }catch(PrinterException ex){
                        logger.fatal(ex.getMessage(), ex);
                        this.firePropertyChange("error", "", "Error - Check log for details.");
                    }
                    
                }
            }
        }

    }//end PrintAction

    public class ExitAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Exit";

        public ExitAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        putValue(MNEMONIC_KEY, mnemonic);
    }

    public ExitAction(String text, ImageIcon icon){
        super(text, icon);
        putValue(SHORT_DESCRIPTION, TOOL_TIP);
    }

        public ExitAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            if(workspace!=null) workspace.closeDatabase();
            System.exit(0);
        }

    }//end ExitAction
    
    
    public class ViewLogAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "View Log";

        public ViewLogAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public ViewLogAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public ViewLogAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            this.firePropertyChange("status", null, "Opening file...");

            JmetrikPreferencesManager prefs = new JmetrikPreferencesManager();
            String logHome = prefs.getLogHome();
            File f = new File(logHome + "/jmetrik-log");
            JmetrikTextFile textFile = new JmetrikTextFile();

            JScrollPane p = new JScrollPane(textFile);
            p.setPreferredSize(new Dimension(730,550));
            addTab(f.getName(), p);

            FileOpener fileOpener = new FileOpener(f, textFile);
            fileOpener.addPropertyChangeListener(statusBar.getStatusListener());
            fileOpener.addPropertyChangeListener(new ErrorOccurredPropertyChangeListener());
            fileOpener.execute();//open in independent thread
        }

    }//end View LogAction
    
    public class UndoAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Undo";

        public UndoAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public UndoAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public UndoAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            Component c = tabbedPane.getSelectedComponent();
            if(c.getClass().getName().equals("javax.swing.JScrollPane")){
                JScrollPane pain = (JScrollPane)c;
                JViewport vp = pain.getViewport();
                Component c2 = vp.getComponent(0);
                if(c2 instanceof JmetrikTextFile){
                    JmetrikTextFile tp = (JmetrikTextFile)vp.getComponent(0);
                    tp.undoText();
                }
            }

        }

    }//end UndoAction

    public class RedoAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Redo";

        public RedoAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public RedoAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public RedoAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
//			check to see if component is a TextfileArea which is in a JScrollPane
            Component c = tabbedPane.getSelectedComponent();
            if(c.getClass().getName().equals("javax.swing.JScrollPane")){
                JScrollPane pain = (JScrollPane)tabbedPane.getSelectedComponent();
                JViewport vp = pain.getViewport();
                Component c2 = vp.getComponent(0);
                if(c2 instanceof JmetrikTextFile){
                    JmetrikTextFile tp = (JmetrikTextFile)vp.getComponent(0);
                    tp.redoText();
                }

            }


        }

    }//end RedoAction

    class TableDescriptionActionListener implements ActionListener{
        public void actionPerformed(ActionEvent evt){
            if(workspace.databaseOpened()){
                    DatabaseAccessObject dao = workspace.getDatabaseFactory().getDatabaseAccessObject();
                    TableDescriptionDialog descriptionDialog = new TableDescriptionDialog(
                            Jmetrik.this,
                            workspace.getConnection(),
                            dao,
                            workspace.getDatabaseName(),
                            (SortedListModel<DataTableName>)workspaceList.getModel());
                    descriptionDialog.setVisible(true);
                }else{
                    JOptionPane.showMessageDialog(Jmetrik.this, "You must open a database before viewing table descriptions.",
                            "No Open Database", JOptionPane.ERROR_MESSAGE);
                }
        }
    }

    /**
     * Listener will reset all dialogs if table data has changed (i.e. table-updated)
     * or if a new table is selected in teh tree (i.e. table-selection). It
     * will also display the data table if teh selected table has changed.
     *
     * Dialogs are reset when table-updated is fired because variables need to
     * have the most up to date information such as any recently changed scoring.
     */
    class TableNameListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if("table-selection".equals(propertyName)){
                DataTableName newName = (DataTableName)e.getNewValue();
                DataTableName oldName = (DataTableName)e.getOldValue();
                if(!newName.equals(oldName)){
                    resetDialogs();
                    tabbedPane.setSelectedIndex(0);//show selected table in tabbed pane
                }
            }
            else if("table-updated".equals(propertyName)){
                DataTableName newName = (DataTableName)e.getNewValue();
                DataTableName oldName = (DataTableName)e.getOldValue();
                if(!newName.equals(oldName)){
                    resetDialogs();
                }
            }
        }
    }

    class ErrorOccurredPropertyChangeListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            if("error".equals(propertyName)){
                JOptionPane.showMessageDialog(Jmetrik.this,
                        "An error occurred. \n" +
                                "View the information in the Log file for details.\n" +
                                "Contact support@itemanalysis.com for help",
                        e.getNewValue().toString(),
                        JOptionPane.ERROR_MESSAGE);
                firePropertyChange("progress-off", null, null); //make statusbar progress not visible
            }
        }
    }

    public class DeleteKeyListener implements KeyListener{

        public void keyPressed(KeyEvent ke){
            if (ke.getKeyCode() == KeyEvent.VK_DELETE) {
                DataTableName tableName = (DataTableName)workspaceList.getSelectedValue();
                if(tableName!=null && workspace.databaseOpened()){
                    int answer = JOptionPane.showConfirmDialog(Jmetrik.this,
                                "Do you want to delete the table " + tableName.getTableName() + "? \n" +
                                        "All data will be permanently deleted. You cannot undo this action.",
                                "Delete Table",
                                JOptionPane.WARNING_MESSAGE,
                                JOptionPane.YES_NO_OPTION);
                    if(answer==JOptionPane.YES_OPTION){
                        DatabaseCommand dbCommand = new DatabaseCommand();
                        dbCommand.getFreeOption("name").add(workspace.getDatabaseName().toString());
                        dbCommand.getFreeOptionList("tables").addValue(tableName.toString());
                        dbCommand.getSelectOneOption("action").setSelected("delete-table");
                        workspace.runProcess(dbCommand);
                    }

                }
            }
        }

        public void keyReleased(KeyEvent ke){

        }

        public void keyTyped(KeyEvent ke){

        }

    }

}
package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTab;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.AbstractJmetrikProcess;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.Workspace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

public class IrtItemCalibrationProcess extends AbstractJmetrikProcess {

    private final String PROCESS_NAME = "IRT Item Calibration Process";

    private IrtItemCalibrationCommand command = null;

    public IrtItemCalibrationProcess(){
        command = new IrtItemCalibrationCommand();
    }

    public String getName(){
        return PROCESS_NAME;
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (IrtItemCalibrationCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){

        JMenuItem menuItem = new JMenuItem("IRT Item Calibration (MMLE)...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTableName tableName = (DataTableName)tableList.getSelectedValue();
                if(!workspace.databaseOpened()){
                    JOptionPane.showMessageDialog(parent,
                            "You must open a database and select a table before running an analysis.",
                            "No Open Database", JOptionPane.ERROR_MESSAGE);
                }else if(tableName==null){
                    JOptionPane.showMessageDialog(parent, "You must select a table before running an analysis." +
                            "Select a table to continue.", "No Table Selected", JOptionPane.ERROR_MESSAGE);
                }else if(workspace.tableOpen()){
                    IrtItemCalibrationDialog irtItemCalibrationDialog = (IrtItemCalibrationDialog)dialogs.get(PROCESS_NAME);
                    if(irtItemCalibrationDialog ==null){
                        irtItemCalibrationDialog = new IrtItemCalibrationDialog(parent,
                                workspace.getConnection(),
                                workspace.getDatabaseName(), tableName,
                                (SortedListModel<DataTableName>)tableList.getModel(),
                                workspace.getVariables());
                        dialogs.put(PROCESS_NAME, irtItemCalibrationDialog);
                        workspace.addVariableChangeListener(irtItemCalibrationDialog.getVariableChangedListener());
                    }
                    irtItemCalibrationDialog.setVisible(true);

                    if(irtItemCalibrationDialog.canRun()){
                        workspace.runProcess(irtItemCalibrationDialog.getCommand());
                    }
                }

            }
        });
        menu.add(menuItem);

    }

    public void runProcess(Connection conn, JmetrikDatabaseFactory dbFactory, final JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){

        //create the chart panel, scroll pane and add to the tabbed pane
        JmetrikTextFile textFile = new JmetrikTextFile();
        final JScrollPane scrollPane = new JScrollPane(textFile);
        tabbedPane.addTab(null, scrollPane);
        int tabCount = tabbedPane.getTabCount();

        //add tab close button listener
        JmetrikTab jTab = new JmetrikTab("irt"+ tabCount);
        jTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int closeTabNumber = tabbedPane.indexOfComponent(scrollPane);

                JScrollPane sp = (JScrollPane)tabbedPane.getComponentAt(closeTabNumber);
                JViewport vp = sp.getViewport();
                if(vp.getComponent(0).getClass().getName().equals(JMETRIK_TEXT_FILE)){
                    int result = ((JmetrikTextFile)vp.getComponent(0)).promptToSave(tabbedPane);
                    if(result==JOptionPane.YES_OPTION || result==JOptionPane.NO_OPTION){
                        tabbedPane.removeTabAt(closeTabNumber);
                    }
                }
            }
        });
        tabbedPane.setTabComponentAt(tabCount-1, jTab);
        tabbedPane.setSelectedIndex(tabCount-1);


        //instantiate and execute analysis
        IrtItemCalibrationAnalysis irtItemCalibrationAnalysis = new IrtItemCalibrationAnalysis(conn, dbFactory.getDatabaseAccessObject(), command, textFile);

        for(PropertyChangeListener pcl : propertyChangeListeners){
            textFile.addPropertyChangeListener(pcl);
            irtItemCalibrationAnalysis.addPropertyChangeListener(pcl);
        }

        for(VariableChangeListener vcl : variableChangeListeners){
            irtItemCalibrationAnalysis.addVariableChangeListener(vcl);
        }

        threadPool.execute(irtItemCalibrationAnalysis);

    }

}

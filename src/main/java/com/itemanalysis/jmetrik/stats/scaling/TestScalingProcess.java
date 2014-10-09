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

package com.itemanalysis.jmetrik.stats.scaling;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.stats.ranking.RankingAnalysis;
import com.itemanalysis.jmetrik.swing.JmetrikTab;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.swing.TextFileArea;
import com.itemanalysis.jmetrik.workspace.*;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

public class TestScalingProcess extends AbstractJmetrikProcess {

    private final String PROCESS_NAME = "Scaling Process";

    private TestScalingCommand command = null;

    public TestScalingProcess(){
        command = new TestScalingCommand();
    }

    public String getName(){
        return PROCESS_NAME;
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (TestScalingCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){

        JMenuItem menuItem = new JMenuItem("Test Scaling...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTableName tableName = (DataTableName)tableList.getSelectedValue();
                if(!workspace.databaseOpened()){
                    JOptionPane.showMessageDialog(parent,
                            "You must open a database and select a table before running an analysis.",
                            "No Open Database", JOptionPane.ERROR_MESSAGE);
                }else if(tableName==null){
                    JOptionPane.showMessageDialog(parent, "You must select a table before running an analysis.",
                            "No Table Selected", JOptionPane.ERROR_MESSAGE);
                }else if(workspace.tableOpen()){
                    TestScalingDialog testScalingDialog = (TestScalingDialog)dialogs.get(PROCESS_NAME);
                    if(testScalingDialog==null){
                        testScalingDialog = new TestScalingDialog(parent, workspace.getDatabaseName(), tableName, workspace.getVariables());
                        dialogs.put(PROCESS_NAME, testScalingDialog);
                        workspace.addVariableChangeListener(testScalingDialog.getVariableChangedListener());
                    }
                    testScalingDialog.setVisible(true);

                    if(testScalingDialog.canRun()){
                        workspace.runProcess(testScalingDialog.getCommand());
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
        JmetrikTab jTab = new JmetrikTab("scale"+ tabCount);
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

        //instantiate analysis
        TestScalingAnalysis testScalingAnalysis = new TestScalingAnalysis(conn, dbFactory.getDatabaseAccessObject(), command, textFile);

        //add property and variable change listeners
        for(PropertyChangeListener pcl : propertyChangeListeners){
            textFile.addPropertyChangeListener(pcl);
            testScalingAnalysis.addPropertyChangeListener(pcl);
        }

        for(VariableChangeListener vcl : variableChangeListeners){
            testScalingAnalysis.addVariableChangeListener(vcl);
        }

        //execute analysis in thread
        threadPool.execute(testScalingAnalysis);

    }

}

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

package com.itemanalysis.jmetrik.graph.nicc;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTab;
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

public class NonparametricCurveProcess extends AbstractJmetrikProcess {

    private final String PROCESS_NAME = "Nicc Process";

    private NonparametricCurveCommand command = null;

    public NonparametricCurveProcess(){
        command = new NonparametricCurveCommand();
    }

    public String getName(){
        return PROCESS_NAME;
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (NonparametricCurveCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){

        JMenuItem menuItem = new JMenuItem("Nonparametric Curves...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTableName tableName = (DataTableName)tableList.getSelectedValue();
                if(!workspace.databaseOpened()){
                    JOptionPane.showMessageDialog(parent,
                            "You must open a database and select a table before creating a graph.",
                            "No Open Database", JOptionPane.ERROR_MESSAGE);
                }else if(tableName==null){
                    JOptionPane.showMessageDialog(parent, "You must select a table before creating a graph.",
                            "No Table Selected", JOptionPane.ERROR_MESSAGE);
                }else if(workspace.tableOpen()){
                    NonparametricCurveDialog nonparametricCurveDialog = (NonparametricCurveDialog)dialogs.get(PROCESS_NAME);
                    if(nonparametricCurveDialog==null){
                        nonparametricCurveDialog = new NonparametricCurveDialog(parent, workspace.getDatabaseName(), tableName, workspace.getVariables());
                        dialogs.put(PROCESS_NAME, nonparametricCurveDialog);
                        workspace.addVariableChangeListener(nonparametricCurveDialog.getVariableChangedListener());
                    }
                    nonparametricCurveDialog.setVisible(true);

                    if(nonparametricCurveDialog.canRun()){
                        workspace.runProcess(nonparametricCurveDialog.getCommand());
                    }

                }
            }
        });

        menu.add(menuItem);

    }

    public void runProcess(Connection conn, JmetrikDatabaseFactory dbFactory, final JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){

        //create the chart panel, scroll pane and add to the tabbed pane
        NonparametricCurvePanel nonparametricCurvePanel = new NonparametricCurvePanel(command);
        final JScrollPane scrollPane = new JScrollPane(nonparametricCurvePanel);
        tabbedPane.addTab(null, scrollPane);
        int tabCount = tabbedPane.getTabCount();

        //add tab close button listener
        JmetrikTab jTab = new JmetrikTab("nicc"+ tabCount);
        jTab.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int closeTabNumber = tabbedPane.indexOfComponent(scrollPane);
                tabbedPane.removeTabAt(closeTabNumber);
            }
        });
        tabbedPane.setTabComponentAt(tabCount-1, jTab);
        tabbedPane.setSelectedIndex(tabCount-1);

        //instantiate and execute analysis
        NonparametricCurveAnalysis nonparametricCurveAnalysis = new NonparametricCurveAnalysis(
                conn,
                dbFactory.getDatabaseAccessObject(),
                command,
                nonparametricCurvePanel);

        for(PropertyChangeListener pcl : propertyChangeListeners){
            nonparametricCurveAnalysis.addPropertyChangeListener(pcl);
        }
        for(VariableChangeListener vcl : variableChangeListeners){
            nonparametricCurveAnalysis.addVariableChangeListener(vcl);
        }

        threadPool.execute(nonparametricCurveAnalysis);
    }

}

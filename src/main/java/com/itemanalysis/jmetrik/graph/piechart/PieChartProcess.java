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

package com.itemanalysis.jmetrik.graph.piechart;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.graph.barchart.BarChartAnalysis;
import com.itemanalysis.jmetrik.graph.barchart.BarChartCommand;
import com.itemanalysis.jmetrik.graph.barchart.BarChartPanel;
import com.itemanalysis.jmetrik.gui.Jmetrik;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTab;
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

public class PieChartProcess extends AbstractJmetrikProcess {

    private PieChartCommand command = null;

    private static final String PROCESS_NAME = "Piechart Process";

    public PieChartProcess(){
        command = new PieChartCommand();
    }

    public String getName(){
        return PROCESS_NAME;
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (PieChartCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){

        JMenuItem mItem = new JMenuItem("Pie Chart...");
        mItem.addActionListener(new ActionListener() {
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
                    PieChartDialog piechartDialog = (PieChartDialog)dialogs.get(PROCESS_NAME);
                    if(piechartDialog==null && workspace.tableOpen()){
                        piechartDialog = new PieChartDialog(parent, workspace.getDatabaseName(), tableName, workspace.getVariables());
                        dialogs.put(PROCESS_NAME, piechartDialog);
                        workspace.addVariableChangeListener(piechartDialog.getVariableChangedListener());
                    }
                    piechartDialog.setVisible(true);

                    if(piechartDialog.canRun()){
                        workspace.runProcess(piechartDialog.getCommand());
                    }

                }
            }
        });
        menu.add(mItem);

    }

    public void runProcess(Connection conn, JmetrikDatabaseFactory dbFactory, final JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){

        //create the chart panel, scroll pane and add to the tabbed pane
        PieChartPanel piechartPanel = new PieChartPanel(command);
        piechartPanel.setGraph();
        final JScrollPane scrollPane = new JScrollPane(piechartPanel);
        tabbedPane.addTab(null, scrollPane);
        int tabCount = tabbedPane.getTabCount();

        //add tab close button listener
        JmetrikTab jTab = new JmetrikTab("piechart"+ tabCount);
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
        PieChartAnalysis piechart = new PieChartAnalysis(conn, dbFactory.getDatabaseAccessObject(), command, piechartPanel);

        for(PropertyChangeListener pcl : propertyChangeListeners){
            piechart.addPropertyChangeListener(pcl);
        }
        for(VariableChangeListener vcl : variableChangeListeners){
            piechart.addVariableChangeListener(vcl);
        }
        threadPool.execute(piechart);
    }

}

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

package com.itemanalysis.jmetrik.stats.descriptives;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.stats.frequency.FrequencyAnalysis;
import com.itemanalysis.jmetrik.stats.frequency.FrequencyDialog;
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

public class DescriptiveProcess extends AbstractJmetrikProcess{

    private final String PROCESS_NAME = "Descriptive Process";

    private DescriptiveCommand command = null;

    public DescriptiveProcess(){
        command = new DescriptiveCommand();
    }

    public String getName(){
        return PROCESS_NAME;
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (DescriptiveCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){

        JMenuItem menuItem = new JMenuItem("Descriptives...");
        menuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DataTableName tableName = (DataTableName) tableList.getSelectedValue();
                if(!workspace.databaseOpened()){
                    JOptionPane.showMessageDialog(parent,
                            "You must open a database and select a table before running an analysis.",
                            "No Open Database", JOptionPane.ERROR_MESSAGE);
                }else if(tableName==null){
                    JOptionPane.showMessageDialog(parent, "You must select a table before running an analysis.",
                            "No Table Selected", JOptionPane.ERROR_MESSAGE);
                }else if(workspace.tableOpen()){
                    DescriptiveDialog descriptiveDialog = (DescriptiveDialog)dialogs.get(PROCESS_NAME);
                    if(descriptiveDialog==null){
                        descriptiveDialog = new DescriptiveDialog(parent, workspace.getDatabaseName(), tableName, workspace.getVariables());
                        dialogs.put(PROCESS_NAME, descriptiveDialog);
                        workspace.addVariableChangeListener(descriptiveDialog.getVariableChangedListener());
                    }
                    descriptiveDialog.setVisible(true);

                    if(descriptiveDialog.canRun()){
                        workspace.runProcess(descriptiveDialog.getCommand());
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
        JmetrikTab jTab = new JmetrikTab("desc"+ tabCount);
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
        DescriptiveAnalysis desriptive = new DescriptiveAnalysis(conn, dbFactory.getDatabaseAccessObject(), command, textFile);

        for(PropertyChangeListener pcl : propertyChangeListeners){
            textFile.addPropertyChangeListener(pcl);
            desriptive.addPropertyChangeListener(pcl);
        }
        for(VariableChangeListener vcl : variableChangeListeners){
            desriptive.addVariableChangeListener(vcl);
        }
        threadPool.execute(desriptive);

    }

}

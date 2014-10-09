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

package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.DerbyCaseSubsetter;
import com.itemanalysis.jmetrik.dao.DerbyVariableSubsetter;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.gui.Jmetrik;
import com.itemanalysis.jmetrik.gui.SubsetVariablesDialog;
import com.itemanalysis.jmetrik.sql.DataTableName;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

public class SubsetVariablesProcess implements JmetrikProcess {

    private SubsetVariableCommand command = null;

    private static final String PROCESS_NAME = "Subset Cases Process";

    private ArrayList<PropertyChangeListener> propertyChangeListeners = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public SubsetVariablesProcess(){
        command = new SubsetVariableCommand();
        propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    }

    public String getName(){
        return PROCESS_NAME;
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (SubsetVariableCommand)command;
    }

    public void addVariableChangeListener(VariableChangeListener listener){
        //not implemented for this command
    }

    public void removeVariableChangeListener(VariableChangeListener listener){
        //not implemented for this command
    }

    public void removeAllVariableChangeListeners(){
        //not implemented for this command
    }

    public void fireVariableChangeEvent(VariableChangeEvent evt){
        //not implemented for this command
    }

    public void addPropertyChangeListener(PropertyChangeListener listener){
        propertyChangeListeners.add(listener);
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){
        String urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-find.png";
        URL url = this.getClass().getResource( urlString );
        ImageIcon iconSelectVariables = new ImageIcon(url, "Subset Variables");
        JMenuItem mItem = new JMenuItem("Subset Variables...");
        mItem.setIcon(iconSelectVariables);
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                DataTableName tableName = (DataTableName)tableList.getSelectedValue();
                if(!workspace.databaseOpened()){
                    JOptionPane.showMessageDialog(parent, "You must open a database before subsetting data.",
                            "No Open Database", JOptionPane.ERROR_MESSAGE);
                }else if(tableName==null){
                    JOptionPane.showMessageDialog(parent, "You must select a table in the workspace list. \n " +
                            "Select a table to continue.", "No Table Selected", JOptionPane.ERROR_MESSAGE);
                }else if(workspace.tableOpen()){
                    SubsetVariablesDialog subsetVariablesDialog = (SubsetVariablesDialog)dialogs.get(PROCESS_NAME);
                    if(subsetVariablesDialog==null){
                        subsetVariablesDialog = new SubsetVariablesDialog(parent, workspace.getDatabaseName(), tableName, workspace.getVariables());
                        workspace.addVariableChangeListener(subsetVariablesDialog);
                    }
                    subsetVariablesDialog.setVisible(true);

                    if(subsetVariablesDialog.canRun()){
                        workspace.runProcess(subsetVariablesDialog.getCommand());
                    }

                }
            }
        });
        menu.add(mItem);
    }

    /**
     * Run the process.
     * Output is added as tab in tabbedPane. The process is run in as a SwingWorker thread
     * using the threadPool manager.
     *
     * The tabbedPane should be updated int he done() method of the SwingWorker thread. Any components added
     * to the tabbedPane should be created in the event dispatch thread prior to executing the
     * SwingWorker thread.
     *
     */
    public void runProcess(Connection conn, JmetrikDatabaseFactory dbFactory, JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){
        DerbyVariableSubsetter varSubsetter = new DerbyVariableSubsetter(conn, command);
        for(PropertyChangeListener pcl : propertyChangeListeners){
            varSubsetter.addPropertyChangeListener(pcl);
        }
        threadPool.execute(varSubsetter);
    }

}

/*
 * Copyright (c) 2013 Patrick Meyer
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

package com.itemanalysis.jmetrik.scoring;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.workspace.AbstractJmetrikProcess;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.Workspace;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.sql.Connection;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

public class BasicScoringProcess extends AbstractJmetrikProcess {

    private static final String PROCESS_NAME = "Basic Scoring Process";
    private BasicScoringCommand command = null;

    public BasicScoringProcess(){
        command = new BasicScoringCommand();
    }

    public String getName(){
        return PROCESS_NAME;
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (BasicScoringCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){
        String urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-find.png";
        URL url = this.getClass().getResource( urlString );
        ImageIcon iconScoring = new ImageIcon(url, "Basic Item Scoring...");
        JMenuItem mItem = new JMenuItem("Basic Item Scoring...");
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(workspace.databaseOpened() && workspace.tableOpen()){
                    BasicScoringDialog basicScoringDialog = new BasicScoringDialog(
                            parent,
                            workspace.getDatabaseName(),
                            workspace.getCurrentDataTable(),
                            workspace.getVariables());
                    basicScoringDialog.setVisible(true);
                    if(basicScoringDialog.canRun()){
                        workspace.runProcess(basicScoringDialog.getCommand());
                    }
                }else{
                    JOptionPane.showMessageDialog(parent, "You must open a database and select a table. \n " +
                            "Select a table to continue scoring.", "No Table Selected", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        menu.add(mItem);

    }

    public void runProcess(final Connection conn, final JmetrikDatabaseFactory dbFactory, JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){

        //instantiate and execute analysis
        BasicScoringAnalysis basicScoringAnalysis = new BasicScoringAnalysis(
                conn,
                dbFactory.getDatabaseAccessObject(),
                command);

        for(PropertyChangeListener pcl : propertyChangeListeners){
            basicScoringAnalysis.addPropertyChangeListener(pcl);
        }
        for(VariableChangeListener vcl : variableChangeListeners){
            basicScoringAnalysis.addVariableChangeListener(vcl);
        }
        threadPool.execute(basicScoringAnalysis);
    }



}

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

package com.itemanalysis.jmetrik.graph.irt;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.gui.Jmetrik;
import com.itemanalysis.jmetrik.swing.JmetrikTab;
import com.itemanalysis.jmetrik.workspace.*;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

public class IrtPlotProcess extends AbstractJmetrikProcess {

    private IrtPlotCommand command = null;

    public IrtPlotProcess(){
        command = new IrtPlotCommand();
    }

    public String getName(){
        return "IrtPlot Process";
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (IrtPlotCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){
        //menu item is hard coded in Jmetrik.java
    }

    public void runProcess(Connection conn, JmetrikDatabaseFactory dbFactory, final JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){

        //create the chart panel, scroll pane and add to the tabbed pane
        IrtPlotPanel irtPlotPanel = new IrtPlotPanel(command);
        irtPlotPanel.setGraph();
        final JScrollPane scrollPane = new JScrollPane(irtPlotPanel);
        tabbedPane.addTab(null, scrollPane);
        int tabCount = tabbedPane.getTabCount();

        //add tab close button listener
        JmetrikTab jTab = new JmetrikTab("irtplot"+ tabCount);
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
        IrtPlotAnalysis irtPlotAnalysis = new IrtPlotAnalysis(conn, dbFactory.getDatabaseAccessObject(), command, irtPlotPanel);

        for(PropertyChangeListener pcl : propertyChangeListeners){
            irtPlotAnalysis.addPropertyChangeListener(pcl);
        }
        for(VariableChangeListener vcl : variableChangeListeners){
            irtPlotAnalysis.addVariableChangeListener(vcl);
        }
        threadPool.execute(irtPlotAnalysis);
    }

}

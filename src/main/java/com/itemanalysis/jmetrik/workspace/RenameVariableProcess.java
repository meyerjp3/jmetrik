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

package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.DatabaseVariableRenamer;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

public class RenameVariableProcess extends AbstractJmetrikProcess {

    private RenameVariableCommand command = null;

    public RenameVariableProcess(){
        command = new RenameVariableCommand();
    }

    public String getName(){
        return "Delete Variable Process";
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (RenameVariableCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){

    }

    public void runProcess(Connection conn, JmetrikDatabaseFactory dbFactory, JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){
        final DatabaseVariableRenamer varRenamer = dbFactory.getDatabaseVariableRenamer(conn, dbFactory.getDatabaseAccessObject(), command);
        SwingWorker worker = (SwingWorker)varRenamer;
        for(PropertyChangeListener pcl : propertyChangeListeners){
            worker.addPropertyChangeListener(pcl);
        }

        for(VariableChangeListener vcl : variableChangeListeners){
            varRenamer.addVariableChangeListener(vcl);
        }

            threadPool.execute(worker);
    }

}

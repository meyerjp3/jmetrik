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
import com.itemanalysis.jmetrik.dao.DatabaseCreator;
import com.itemanalysis.jmetrik.dao.DatabaseDeleter;
import com.itemanalysis.jmetrik.dao.DatabaseTableDeleter;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.gui.Jmetrik;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

public class DatabaseProcess implements JmetrikProcess {
    
    private DatabaseCommand command = null;
    
    private ArrayList<PropertyChangeListener> propertyChangeListeners = null;
    
    public DatabaseProcess(){
        command = new DatabaseCommand();
        propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    }

    public String getName(){
        return "Database Process";
    }
    
    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }
    
    public void setCommand(Command command){
        this.command = (DatabaseCommand)command;
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

    }

    public void runProcess(Connection conn, JmetrikDatabaseFactory dbFactory, JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){
        if(command.getSelectOneOption("action").isValueSelected("create")){
            final DatabaseCreator dbCreator = dbFactory.getDatabaseCreator(command);
            SwingWorker worker = (SwingWorker)dbCreator;
            for(PropertyChangeListener pcl : propertyChangeListeners){
                worker.addPropertyChangeListener(pcl);
            }
            threadPool.execute(worker);
        }else if(command.getSelectOneOption("action").isValueSelected("delete-db")){
            final DatabaseDeleter dbDeleter = dbFactory.getDatabaseDeleter(command);
            SwingWorker worker = (SwingWorker)dbDeleter;
            for(PropertyChangeListener pcl : propertyChangeListeners){
                worker.addPropertyChangeListener(pcl);
            }
            threadPool.execute(worker);
        }else if(command.getSelectOneOption("action").isValueSelected("delete-table")){
            final DatabaseTableDeleter tableDeleter = dbFactory.getDatabaseTableDeleter(conn, dbFactory.getDatabaseAccessObject(), command);
            SwingWorker worker = (SwingWorker)tableDeleter;
            for(PropertyChangeListener pcl : propertyChangeListeners){
                worker.addPropertyChangeListener(pcl);
            }
            threadPool.execute(worker);
        }else if(command.getSelectOneOption("action").isValueSelected("open")){

        }else if(command.getSelectOneOption("action").isValueSelected("close")){

        }
    }

    
    
}

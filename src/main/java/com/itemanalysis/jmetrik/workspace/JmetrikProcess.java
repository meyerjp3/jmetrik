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
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.gui.Jmetrik;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.sun.corba.se.spi.orbutil.threadpool.ThreadPoolManager;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * This interface provides the framework for plugin capability.
 * Every plugin must implement this interface.
 *
 */
public interface JmetrikProcess {

    /**
     * Check to see that the process uses the Command
     * 
     * @param command
     * @return
     */
    public boolean commandMatch(Command command);

    /**
     * After a successful command match, set the command.
     *
     * @param command
     */
    public void setCommand(Command command);

    /**
     * Add listener for variable (i.e. database column) changes.
     * This method only needs to be implemented if the process changes a variable.
     *
     * @param listener
     */
    public void addVariableChangeListener(VariableChangeListener listener);

    /**
     * Remove listener.
     * This method only needs to be implemented if the process changes a variable.
     *
     * @param listener
     */
    public void removeVariableChangeListener(VariableChangeListener listener);

    /**
     * Remove al listeneres.
     * This method only needs to be implemented if the process changes a variable.
     */
    public void removeAllVariableChangeListeners();

    /**
     * Notify listeners that a variable has changed.
     * This method only needs to be implemented if the process changes a variable.
     *
     * @param evt
     */
    public void fireVariableChangeEvent(VariableChangeEvent evt);


    /**
     * This method will be implemented by extending the SwingWorker class.
     * Property changes include those that update the StatusBar, those
     * that update the database, etc.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener);

    public void addMenuItem(JFrame parent, JMenu menu, TreeMap<String, JDialog> dialogs, Workspace workspace, JList tableList);

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
    public void runProcess(Connection conn, JmetrikDatabaseFactory dbFactory, JTabbedPane tabbedPane, ThreadPoolExecutor threadPool);
    
    public String getName();


    
}

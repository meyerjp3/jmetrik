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

package com.itemanalysis.jmetrik.scoring;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.AbstractJmetrikProcess;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.jmetrik.workspace.Workspace;
import com.itemanalysis.psychometrics.data.VariableInfo;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

public class ScoringProcess extends AbstractJmetrikProcess {

    private ScoringCommand command = null;

    private static final String PROCESS_NAME = "Scoring Process";

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public ScoringProcess(){
        command = new ScoringCommand();
    }

    public String getName(){
        return PROCESS_NAME;
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (ScoringCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){

        String urlString = "/org/tango-project/tango-icon-theme/16x16/actions/edit-find.png";
        URL url = this.getClass().getResource( urlString );
        ImageIcon iconScoring = new ImageIcon(url, "Advanced Item Scoring...");
        JMenuItem mItem = new JMenuItem("Advanced Item Scoring...");
        mItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                DataTableName tableName = (DataTableName)tableList.getSelectedValue();
                if(tableName==null){
                    JOptionPane.showMessageDialog(parent, "You must select a table in the workspace tree. \n " +
                            "Select a table to continue scoring.", "No Table Selected", JOptionPane.ERROR_MESSAGE);
                }else if(workspace.tableOpen()){
                    ScoringToolDialog scoringToolDialog = (ScoringToolDialog)dialogs.get(PROCESS_NAME);
                    if(scoringToolDialog==null){
                        scoringToolDialog = new ScoringToolDialog(parent, workspace.getDatabaseName(), tableName, workspace.getVariables());
                    }
                    scoringToolDialog.setVisible(true);

                    if(scoringToolDialog.canRun()){
                        workspace.runProcess(scoringToolDialog.getCommand());
                    }

                }
            }
        });
        menu.add(mItem);
    }

    public void runProcess(final Connection conn, final JmetrikDatabaseFactory dbFactory, JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){

        SwingWorker<String, Void> task = new SwingWorker<String, Void>(){
            
            public Throwable theException = null;
            private String tableName = null;
            private VariableTableName variableTableName = null;
            private ArrayList<VariableInfo> variables = null;

            protected String doInBackground()throws Exception{
                firePropertyChange("status", "", "Setting variable scoring...");
                firePropertyChange("progress-ind-on", null, null);
                logger.info(command.paste());
                try{
                    DatabaseAccessObject dao = dbFactory.getDatabaseAccessObject();
                    dao.setVariableScoring(conn, command);
                    tableName = command.getPairedOptionList("data").getStringAt("table");
                    variableTableName = new VariableTableName(tableName);
                    variables = dao.getAllVariables(conn, variableTableName);
                }catch(Throwable t){
                    theException = t;
                }

                return tableName;
            }

            protected void done(){
                try{
                    if(theException==null){
                        firePropertyChange("status", "", "Ready");
                        DataTableName tableName = new DataTableName(get());
                        firePropertyChange("table-updated", null, tableName);//updates display of data table
                        for(VariableInfo v: variables){
                            //updates variables in dialogs
                            fireVariableChangeEvent(new VariableChangeEvent(this, variableTableName, v, VariableChangeType.VARIABLE_MODIFIED));
                        }
                    }else{
                        logger.fatal(theException.getMessage(), theException);
                        firePropertyChange("error", "", "Error - Check log for details.");
                    }
                }catch(Exception ex){
                    logger.fatal(ex.getMessage(), ex);
                    firePropertyChange("error", "", "Error - Check log for details.");
                }
                firePropertyChange("progress-off", null, null);

            }
        };

        for(PropertyChangeListener pcl : propertyChangeListeners){
            task.addPropertyChangeListener(pcl);
        }

        threadPool.execute(task);
    }

}

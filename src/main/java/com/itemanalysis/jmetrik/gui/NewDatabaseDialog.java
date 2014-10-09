/*
 * Copyright (c) 2011 Patrick Meyer
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

package com.itemanalysis.jmetrik.gui;


import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.DatabaseCommand;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class NewDatabaseDialog extends JDialog {

    private JPanel buttonPanel;
    private JButton cancelButton;
    private JPanel mainPanel;
    private JLabel nameLabel;
    private JTextField nameText;
    private JButton okButton;
    
    private DatabaseCommand command;
    private boolean canRun = false;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public NewDatabaseDialog(Jmetrik parent){
        super(parent, "Create New Database", true);
        initComponents();
        this.setLocationRelativeTo(parent);
    }

    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new JPanel();
        nameLabel = new JLabel();
        nameText = new JTextField();
        buttonPanel = new JPanel();
        cancelButton = new JButton(new CancelAction("Cancel"));
        okButton = new JButton(new OkAction("Create"));

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Create New Database");
        setLocationByPlatform(true);
        getContentPane().setLayout(new GridBagLayout());

        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new GridBagLayout());

        nameLabel.setText("Database Name:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        mainPanel.add(nameLabel, gridBagConstraints);

        nameText.setToolTipText("Name of database");
        nameText.setMinimumSize(new Dimension(300, 28));
        nameText.setPreferredSize(new Dimension(300, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        mainPanel.add(nameText, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        getContentPane().add(mainPanel, gridBagConstraints);

        buttonPanel.setBorder(BorderFactory.createEmptyBorder(1, 10, 10, 10));
        buttonPanel.setLayout(new GridBagLayout());

        //cancel button
        cancelButton.setMaximumSize(new Dimension(69, 28));
        cancelButton.setMinimumSize(new Dimension(69, 28));
        cancelButton.setPreferredSize(new Dimension(69, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 10, 5, 5);
        buttonPanel.add(cancelButton, gridBagConstraints);

        //OK button
        okButton.setMaximumSize(new Dimension(69, 28));
        okButton.setMinimumSize(new Dimension(69, 28));
        okButton.setPreferredSize(new Dimension(69, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        buttonPanel.add(okButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        getContentPane().add(buttonPanel, gridBagConstraints);

        pack();
    }

    public DatabaseCommand getCommand(){
        return command;
    }

    public boolean canRun(){
        return canRun;
    }

    public class OkAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Create Workspace";

        public OkAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public OkAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public OkAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            
            String name = nameText.getText().trim();
            
            if(name.equals("") ){
                JOptionPane.showMessageDialog(NewDatabaseDialog.this,
                        "Database name is required.",
                        "Incomplete Information",
                        JOptionPane.ERROR_MESSAGE);
            }else{
                try{
                    JmetrikPreferencesManager prefs = new JmetrikPreferencesManager();
                    int port = prefs.getDatabasePort();
                    
                    DatabaseName tName = new DatabaseName(name);
                    if(tName.nameChanged()){
                        JOptionPane.showMessageDialog(NewDatabaseDialog.this,
                                "Invalid characters removed from your database name.\n" +
                                        "The new name is " + tName.getName(),
                                "Database name changed",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                    
                    command = new DatabaseCommand();
                    command.getFreeOption("port").add(port);
                    command.getFreeOption("name").add(tName.getName());
                    command.getSelectOneOption("action").setSelected("create");
                    canRun = true;
                }catch(IllegalArgumentException ex){
                    JOptionPane.showMessageDialog(NewDatabaseDialog.this,
                            ex.getMessage(),
                            "Error - Illegal Argument Exception",
                            JOptionPane.ERROR_MESSAGE);
                }
                setVisible(false);
            }

            
        }

    }//end OkAction

    public class CancelAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Cancel";

        public CancelAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public CancelAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public CancelAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            setVisible(false);
        }


    }//end  Cancel Action


}

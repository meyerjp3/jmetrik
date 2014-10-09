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

package com.itemanalysis.jmetrik.gui;

import com.itemanalysis.jmetrik.sql.DatabaseName;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class OpenDatabaseDialog extends JDialog {

    private JPanel buttonPanel;
    private JPanel acessPanel;
    private JButton cancelButton;
    private JScrollPane databaseScrollPane;
    private JList databaseList;
    private JButton okButton;

    private boolean canRun = false;
    private DefaultListModel databaseListModel = null;
    private DatabaseName dbName = null;
    private String buttonName = "Select";
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public OpenDatabaseDialog(Jmetrik parent, String buttonName){
        super(parent, "Select Database", true);
        this.buttonName = buttonName;
        initComponents();
        this.setLocationRelativeTo(parent);
    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        buttonPanel = new JPanel();
        okButton = new JButton(new OkAction(buttonName));
        cancelButton = new JButton(new CancelAction("Cancel"));
        databaseScrollPane = new JScrollPane();
        databaseList = new JList();
        databaseList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        acessPanel = new JPanel();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle(buttonName + " Database");
        getContentPane().setLayout(new GridBagLayout());

        buttonPanel.setLayout(new GridBagLayout());

        //ok button
        okButton.setMaximumSize(new Dimension(65, 28));
        okButton.setMinimumSize(new Dimension(65, 28));
        okButton.setPreferredSize(new Dimension(65, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        buttonPanel.add(okButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        getContentPane().add(buttonPanel, gridBagConstraints);

        databaseScrollPane.setMinimumSize(new Dimension(300, 325));
        databaseScrollPane.setPreferredSize(new Dimension(300, 325));
        databaseScrollPane.setViewportView(databaseList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(databaseScrollPane, gridBagConstraints);

        acessPanel.setLayout(new GridBagLayout());

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        getContentPane().add(acessPanel, gridBagConstraints);

        pack();
    }// </editor-fold>

    public JList getDatabaseList(){
        return databaseList;
    }
    
    public boolean canRun(){
        return canRun;
    }
    
    public String getDatabaseName(){
        return dbName.toString();
    }


    public class OkAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Select database";

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

            Object firstSel = databaseList.getSelectedValue();

            if(firstSel==null){
                JOptionPane.showMessageDialog(OpenDatabaseDialog.this,
                        "You must select a database.",
                        "No database selected",
                        JOptionPane.ERROR_MESSAGE);
            }else{
                dbName = new DatabaseName(firstSel.toString());
                if(dbName.nameChanged()){
                    JOptionPane.showMessageDialog(OpenDatabaseDialog.this,
                            "Invalid characters removed from your database name.\n" +
                                    "The new name is " + dbName.getName(),
                            "Database name changed",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                canRun = true;
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
            canRun=false;
            setVisible(false);
        }


    }//end  Cancel Action

}

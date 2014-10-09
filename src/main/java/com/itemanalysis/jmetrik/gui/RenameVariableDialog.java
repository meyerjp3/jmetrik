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

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.RenameVariableCommand;
import com.itemanalysis.psychometrics.data.VariableName;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RenameVariableDialog extends JDialog {

    // Variables declaration - do not modify
    private JButton cancelButton;
    private JLabel newNameLabel;
    private JTextField newNameTextField;
    private JButton okButton;
    private JLabel oldNameLabel;
    private JTextField oldNameTextField;
    // End of variables declaration

    private String oldName = "";
    private boolean canRun = false;
    private RenameVariableCommand command = null;
    private DatabaseName db = null;
    private DataTableName tableName = null;

    public RenameVariableDialog(JFrame parent, DatabaseName db, DataTableName tableName, String oldName){
        super(parent, "Rename Variable", true);
        this.db = db;
        this.tableName = tableName;
        this.oldName = oldName;

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    }


    private void initComponents() {

        oldNameLabel = new JLabel();
        oldNameTextField = new JTextField();
        newNameLabel = new JLabel();
        newNameTextField = new JTextField();
        okButton = new JButton();
        cancelButton = new JButton();
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun = false;
                setVisible(false);
            }
        });

        oldNameLabel.setText("Old name:");

        oldNameTextField.setEditable(false);
        oldNameTextField.setMinimumSize(new Dimension(125, 28));
        oldNameTextField.setPreferredSize(new Dimension(125, 28));
        oldNameTextField.setText(oldName);

        newNameLabel.setText("New name:");

        newNameTextField.setMinimumSize(new Dimension(125, 28));
        newNameTextField.setPreferredSize(new Dimension(125, 28));

        okButton.setText("OK");
        okButton.addActionListener(new OkActionListener());
        okButton.setMaximumSize(new Dimension(72, 28));
        okButton.setMinimumSize(new Dimension(72, 28));
        okButton.setPreferredSize(new Dimension(72, 28));

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new Dimension(72, 28));
        cancelButton.setMinimumSize(new Dimension(72, 28));
        cancelButton.setPreferredSize(new Dimension(72, 28));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addComponent(oldNameLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(oldNameTextField, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newNameLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addComponent(newNameTextField, GroupLayout.PREFERRED_SIZE, 192, GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(oldNameLabel)
                    .addComponent(oldNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(newNameLabel)
                    .addComponent(newNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }

    public boolean canRun(){
        return canRun;
    }

    public RenameVariableCommand getCommand(){
        return command;
    }

    public class OkActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt){
            command = new RenameVariableCommand();

            String oldName = oldNameTextField.getText().trim();
            String newName = newNameTextField.getText().trim();

            if("".equals(newName)){
                JOptionPane.showMessageDialog(RenameVariableDialog.this,
                        "You must type a new name for the variable.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                canRun = false;
            }else{
                command.getPairedOptionList("data").addValue("db", db.toString());
                command.getPairedOptionList("data").addValue("table", tableName.toString());

                VariableName oldVariableName = new VariableName(oldName);
                command.getPairedOptionList("variable").addValue("oldname", oldVariableName.toString());

                VariableName newVariableName = new VariableName(newName);
                command.getPairedOptionList("variable").addValue("newname", newVariableName.toString());

                canRun = true;
                setVisible(false);
            }

        }
    }


}

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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TableNameDialog extends JDialog {

    // Variables declaration - do not modify
    private JButton cancelButton;
    private JButton okButton;
    private JLabel tableNameLabel;
    private JTextField tableNameTextField;
    // End of variables declaration

    private boolean canRun = false;
    private String tableName = "";

    public TableNameDialog(JDialog parent){
        super(parent, "New Table Name", true);
        initComponents();
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setResizable(false);
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        tableNameLabel = new JLabel();
        tableNameTextField = new JTextField();
        okButton = new JButton();
        cancelButton = new JButton();

        tableNameLabel.setText("Table Name");

        tableNameTextField.setMaximumSize(new Dimension(200, 28));
        tableNameTextField.setMinimumSize(new Dimension(200, 28));
        tableNameTextField.setPreferredSize(new Dimension(200, 28));

        okButton.setText("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(tableNameTextField.getText().trim().equals("")){
                    JOptionPane.showMessageDialog(TableNameDialog.this,
                            "You must type a name for the new table.",
                            "No Table Name Provided",
                            JOptionPane.ERROR_MESSAGE);
                }else{
                    tableName = tableNameTextField.getText().trim();
                    canRun = true;
                    setVisible(false);
                }
            }
        });
        okButton.setMaximumSize(new Dimension(70, 28));
        okButton.setMinimumSize(new Dimension(70, 28));
        okButton.setPreferredSize(new Dimension(70, 28));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        cancelButton.setMaximumSize(new Dimension(70, 28));
        cancelButton.setMinimumSize(new Dimension(70, 28));
        cancelButton.setPreferredSize(new Dimension(70, 28));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableNameLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(tableNameTextField, GroupLayout.PREFERRED_SIZE, 200, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(tableNameLabel)
                    .addComponent(tableNameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(18, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    public String getTableName(){
        return tableName;
    }

    public boolean canRun(){
        return canRun;
    }


}

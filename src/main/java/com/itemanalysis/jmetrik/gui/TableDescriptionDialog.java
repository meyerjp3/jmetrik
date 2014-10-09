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

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.SqlSafeTableName;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;

public class TableDescriptionDialog extends JDialog {

    // Variables declaration - do not modify
    private JButton cancelButton;
    private JButton submitButton;
    private JScrollPane dataScrollPane;
    private JScrollPane descriptionScrollPane;
    private JTextArea descriptionTextArea;
    private JButton okButton;
    private JList tableList;
    // End of variables declaration

    private DatabaseName dbName = null;
    private boolean canRun = false;
    private boolean unsavedChanges = false;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public TableDescriptionDialog(JFrame parent, Connection conn, DatabaseAccessObject dao,
                                  DatabaseName dbName, SortedListModel<DataTableName> listModel){
        super(parent, "Table Descriptions", true);

        this.conn = conn;
        this.dao = dao;
        this.dbName = dbName;
        tableList = new JList(listModel);
        tableList.addListSelectionListener(new JmetrikListSelectionListener());
        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        initComponents();
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setResizable(false);
    }

    private void initComponents() {

        dataScrollPane = new JScrollPane();
        descriptionScrollPane = new JScrollPane();
        descriptionTextArea = new JTextArea();
        submitButton = new JButton();
        cancelButton = new JButton();
        okButton = new JButton();

        dataScrollPane.setMaximumSize(new Dimension(200, 250));
        dataScrollPane.setMinimumSize(new Dimension(200, 250));
        dataScrollPane.setPreferredSize(new Dimension(200, 225));

        dataScrollPane.setViewportView(tableList);

        descriptionScrollPane.setMaximumSize(new Dimension(300, 225));
        descriptionScrollPane.setMinimumSize(new Dimension(300, 225));
        descriptionScrollPane.setPreferredSize(new Dimension(300, 225));

        descriptionTextArea.getDocument().addDocumentListener(new UpdateListener());
        descriptionTextArea.setColumns(20);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setRows(5);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionScrollPane.setViewportView(descriptionTextArea);

        submitButton.setText("Submit");
        submitButton.setToolTipText("Save changes.");
        submitButton.setEnabled(false);
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(unsavedChanges) saveChanges();
            }
        });
        submitButton.setMaximumSize(new Dimension(72, 28));
        submitButton.setMinimumSize(new Dimension(72, 28));
        submitButton.setPreferredSize(new Dimension(72, 28));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        cancelButton.setMaximumSize(new Dimension(72, 28));
        cancelButton.setMinimumSize(new Dimension(72, 28));
        cancelButton.setPreferredSize(new Dimension(72, 28));

        okButton.setText("OK");
        okButton.setToolTipText("Save changes and close dialog.");
        okButton.setEnabled(false);
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(unsavedChanges) saveChanges();
                setVisible(false);
            }
        });
        okButton.setMaximumSize(new Dimension(72, 28));
        okButton.setMinimumSize(new Dimension(72, 28));
        okButton.setPreferredSize(new Dimension(72, 28));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dataScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(submitButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(dataScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(submitButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>


    public void saveChanges(){
        try{
            String desc = descriptionTextArea.getText().trim();

            DataTableName tableName = (DataTableName)tableList.getSelectedValue();
            if(tableName!=null){
                dao.setTableDescription(conn, tableName, desc);
                submitButton.setEnabled(false);
                okButton.setEnabled(false);
                unsavedChanges = false;
            }

        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

    private void displayDescription(DataTableName tableName){
        try{
            String desc = dao.getTableDescription(conn, tableName);
            descriptionTextArea.setText(desc);
            submitButton.setEnabled(false);
            okButton.setEnabled(false);
        }catch(SQLException ex){
            descriptionTextArea.setText("");
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }

    }


    class UpdateListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e){
            unsavedChanges = true;
            okButton.setEnabled(true);
            submitButton.setEnabled(true);
        }

        public void removeUpdate(DocumentEvent e){
            unsavedChanges = true;
            okButton.setEnabled(true);
            submitButton.setEnabled(true);
        }

        public void changedUpdate(DocumentEvent e){
            unsavedChanges = true;
            okButton.setEnabled(true);
            submitButton.setEnabled(true);
        }

    }

    class JmetrikListSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e){
            DataTableName tableName = (DataTableName)tableList.getSelectedValue();
            if(tableName!=null){
                displayDescription(tableName);
            }
        }
    }

}

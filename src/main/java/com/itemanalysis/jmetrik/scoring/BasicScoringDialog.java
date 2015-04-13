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

package com.itemanalysis.jmetrik.scoring;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.swing.DataTable;
import com.itemanalysis.jmetrik.swing.TableHeaderCellRenderer;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class BasicScoringDialog extends JDialog {

    // Variables declaration - do not modify
    private JScrollPane jScrollPane1;
    private DataTable scoringTable;
    private JLabel notReachedLabel;
    private JTextField notReachedTextField;
    private JButton okButton;
    private JButton cancelButton;
    private JButton clearButton;
    private JLabel omitLabel;
    private JLabel missingLabel;
    private JTextField missingTextField;
    private JTextField omitTextField;
    // End of variables declaration

    private ArrayList<VariableAttributes> variables = null;
    private boolean canRun = false;
    private BasicScoringCommand command = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private BasicScoringTableModel tableModel = null;

    public BasicScoringDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList<VariableAttributes> variables){
        super(parent, "Basic Item Scoring", true);
        this.dbName = dbName;
        this.tableName = tableName;
        this.variables = variables;
        initComponents();

        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    private void initComponents() {
        jScrollPane1 = new JScrollPane();
        scoringTable = new DataTable();
        okButton = new JButton();
        cancelButton = new JButton();
        clearButton = new JButton();
        omitLabel = new JLabel();
        notReachedLabel = new JLabel();
        omitTextField = new JTextField();
        notReachedTextField = new JTextField();
        missingLabel = new JLabel();
        missingTextField = new JTextField();

        jScrollPane1.setPreferredSize(new Dimension(425, 100));

        tableModel = new BasicScoringTableModel(variables);
        int index = 0;
        for(VariableAttributes v : variables){
            if(v.getType().getItemType()== ItemType.BINARY_ITEM || v.getType().getItemType()== ItemType.POLYTOMOUS_ITEM){
                tableModel.setValueAt(v.getItemScoring().getAnswerKey(), 0, index);
                tableModel.setValueAt(v.getItemScoring().numberOfCategories(), 1, index);
            }
            index++;
        }
        index = 0;

        //set omit and not reached codes. Codes are the same for all variables. Obtain values from firt variable
        for(VariableAttributes v : variables){
            if(v.getType().getItemType()== ItemType.BINARY_ITEM || v.getType().getItemType()== ItemType.POLYTOMOUS_ITEM){
                Object omitCode = v.getSpecialDataCodes().getOmittedCode();
                Object nrCode = v.getSpecialDataCodes().getNotReachedCode();

                if(omitCode==null){
                    omitTextField.setText("");
                }else{
                    omitTextField.setText(omitCode.toString());
                }

                if(nrCode==null){
                    notReachedTextField.setText("");
                }else{
                    notReachedTextField.setText(nrCode.toString());
                }
                break;
            }
        }

        scoringTable.setModel(tableModel);
        JTableHeader header = scoringTable.getTableHeader();
        header.setDefaultRenderer(new TableHeaderCellRenderer());
        scoringTable.setRowHeight(18);
        scoringTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jScrollPane1.setViewportView(scoringTable);

        okButton.setText("OK");
        okButton.addActionListener(new OkActionListener());
        okButton.setMaximumSize(new Dimension(72, 28));
        okButton.setMinimumSize(new Dimension(72, 28));
        okButton.setPreferredSize(new Dimension(72, 28));

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun = false;
                setVisible(false);
            }
        });
        cancelButton.setMaximumSize(new Dimension(72, 28));
        cancelButton.setMinimumSize(new Dimension(72, 28));
        cancelButton.setPreferredSize(new Dimension(72, 28));

        clearButton.setText("Clear");
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.clearAll();
                tableModel.fireTableDataChanged();
                canRun = false;
            }
        });
        clearButton.setMaximumSize(new Dimension(72, 28));
        clearButton.setMinimumSize(new Dimension(72, 28));
        clearButton.setPreferredSize(new Dimension(72, 28));

        omitLabel.setText("Omit:");

        notReachedLabel.setText("Not Reached:");

        omitTextField.setToolTipText("");
        omitTextField.setMaximumSize(new Dimension(100, 28));
        omitTextField.setMinimumSize(new Dimension(100, 28));
        omitTextField.setPreferredSize(new Dimension(100, 28));

        notReachedTextField.setToolTipText("");
        notReachedTextField.setMaximumSize(new Dimension(100, 28));
        notReachedTextField.setMinimumSize(new Dimension(100, 28));
        notReachedTextField.setPreferredSize(new Dimension(100, 28));

        missingLabel.setText("Missing:");

        missingTextField.setToolTipText("");
        missingTextField.setMaximumSize(new Dimension(100, 28));
        missingTextField.setMinimumSize(new Dimension(100, 28));
        missingTextField.setPreferredSize(new Dimension(100, 28));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(10, 10, 10)
                        .addComponent(omitLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(omitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(notReachedLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(notReachedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(clearButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(notReachedLabel)
                        .addComponent(notReachedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(omitLabel)
                        .addComponent(omitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();

    }


//    private void initComponents() {
//
//        jScrollPane1 = new JScrollPane();
//        scoringTable = new DataTable();
//        okButton = new JButton();
//        cancelButton = new JButton();
//        clearButton = new JButton();
//        omitLabel = new JLabel();
//        notReachedLabel = new JLabel();
//        omitTextField = new JTextField();
//        notReachedTextField = new JTextField();
//
//        jScrollPane1.setPreferredSize(new Dimension(425, 100));
//
//        tableModel = new BasicScoringTableModel(variables);
//        int index = 0;
//        for(VariableInfo v : variables){
//            if(v.getType().getItemType()== VariableType.BINARY_ITEM || v.getType().getItemType()== VariableType.POLYTOMOUS_ITEM){
//                tableModel.setValueAt(v.getItemScoring().getAnswerKey(), 0, index);
//                tableModel.setValueAt(v.getItemScoring().numberOfCategories(), 1, index);
//            }
//            index++;
//        }
//        index = 0;
//
//        scoringTable.setModel(tableModel);
//        JTableHeader header = scoringTable.getTableHeader();
//        header.setDefaultRenderer(new TableHeaderCellRenderer());
//        scoringTable.setRowHeight(18);
//        scoringTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
//        jScrollPane1.setViewportView(scoringTable);
//
//        okButton.setText("OK");
//        okButton.addActionListener(new OkActionListener());
//        okButton.setMaximumSize(new Dimension(72, 28));
//        okButton.setMinimumSize(new Dimension(72, 28));
//        okButton.setPreferredSize(new Dimension(72, 28));
//
//        cancelButton.setText("Cancel");
//        cancelButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                canRun = false;
//                setVisible(false);
//            }
//        });
//        cancelButton.setMaximumSize(new Dimension(72, 28));
//        cancelButton.setMinimumSize(new Dimension(72, 28));
//        cancelButton.setPreferredSize(new Dimension(72, 28));
//
//        clearButton.setText("Clear");
//        clearButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                tableModel.clearAll();
//                tableModel.fireTableDataChanged();
//                canRun = false;
//            }
//        });
//        clearButton.setMaximumSize(new Dimension(72, 28));
//        clearButton.setMinimumSize(new Dimension(72, 28));
//        clearButton.setPreferredSize(new Dimension(72, 28));
//
//        omitLabel.setText("Omit code:");
//
//        notReachedLabel.setText("Not reached code:");
//
//        omitTextField.setToolTipText("");
//        omitTextField.setMaximumSize(new Dimension(100, 28));
//        omitTextField.setMinimumSize(new Dimension(100, 28));
//        omitTextField.setPreferredSize(new Dimension(100, 28));
//
//        notReachedTextField.setToolTipText("");
//        notReachedTextField.setMaximumSize(new Dimension(100, 28));
//        notReachedTextField.setMinimumSize(new Dimension(100, 28));
//        notReachedTextField.setPreferredSize(new Dimension(100, 28));
//
//        GroupLayout layout = new GroupLayout(getContentPane());
//        getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(layout.createSequentialGroup()
//                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 600, Short.MAX_VALUE)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                            .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addComponent(clearButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                    .addGroup(layout.createSequentialGroup()
//                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                            .addComponent(omitLabel)
//                            .addComponent(notReachedLabel))
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                            .addComponent(omitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addComponent(notReachedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                        .addGap(0, 0, Short.MAX_VALUE)))
//                .addContainerGap())
//        );
//        layout.setVerticalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(layout.createSequentialGroup()
//                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(clearButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                    .addComponent(jScrollPane1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(omitLabel)
//                    .addComponent(omitTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(notReachedLabel)
//                    .addComponent(notReachedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        pack();
//    }// </editor-fold>

    public boolean canRun(){
        return canRun;
    }

    public BasicScoringCommand getCommand(){
        return command;
    }

    public class OkActionListener implements ActionListener{

        public void actionPerformed(ActionEvent evt){

            command = new BasicScoringCommand();
            command.getPairedOptionList("data").addValue("db", dbName.toString());
            command.getPairedOptionList("data").addValue("table", tableName.toString());

            command.getFreeOptionList("key").addValue(tableModel.getKeyString());
            command.getFreeOptionList("ncat").addValue(tableModel.getNumberOfOptionsString());

            String omit = omitTextField.getText().trim();
            String notReached = notReachedTextField.getText().trim();
            command.getPairedOptionList("codes").addValue("omit", omit);
            command.getPairedOptionList("codes").addValue("nr", notReached);

            canRun = true;

            setVisible(false);

        }


    }

}

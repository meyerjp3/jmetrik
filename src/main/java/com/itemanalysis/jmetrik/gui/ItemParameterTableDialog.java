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
import com.itemanalysis.jmetrik.model.VariableListModel;
import com.itemanalysis.jmetrik.selector.MultipleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.data.VariableType;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class ItemParameterTableDialog extends JDialog {

    // Variables declaration - do not modify
    private JList tableList;
    private JScrollPane tableScrollPane;
    // End of variables declaration

    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private SortedListModel<DataTableName> tableListModel = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    private DataTableName currentTable = null;
    private MultipleSelectionPanel vsp = null;
    private boolean canRun = false;
    private VariableName nameColumn = null;


    public ItemParameterTableDialog(JFrame parent, Connection conn, DatabaseAccessObject dao, SortedListModel<DataTableName> tableListModel, String title){
        super(parent, title, true);
        this.conn = conn;
        this.dao = dao;
        this.tableListModel = tableListModel;

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    public ItemParameterTableDialog(JDialog parent, Connection conn, DatabaseAccessObject dao, SortedListModel<DataTableName> tableListModel, String title){
        super(parent, title, true);
        this.conn = conn;
        this.dao = dao;
        this.tableListModel = tableListModel;

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(HIDE_ON_CLOSE);
    }

    private void initComponents() {

        nameColumn = new VariableName("name");
        tableScrollPane = new JScrollPane();
        tableList = new JList();
        tableList.setModel(tableListModel);
        tableList.addListSelectionListener(new TableSelectionListener());
        vsp = new MultipleSelectionPanel();
        JButton b1 = vsp.getButton1();
        b1.setText("OK");
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        JButton b2 = vsp.getButton2();
        b2.setText("Cancel");
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        vsp.showButton3(false);
        vsp.showButton4(false);

        tableScrollPane.setPreferredSize(new Dimension(150, 242));
        tableScrollPane.setViewportView(tableList);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(tableScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(vsp, GroupLayout.DEFAULT_SIZE, 242, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }

    public void addSelectedListFilterType(VariableType variableType){
        vsp.addSelectedFilterType(variableType);
    }

    public void addUnSelectedListFilterType(VariableType variableType){
        vsp.addUnselectedFilterType(variableType);
    }

    public DatabaseName getDatabaseName() throws SQLException{
        String url = conn.getMetaData().getURL();
        return new DatabaseName(url);
    }

    public DataTableName getSelectedTable(){
        return currentTable;
    }

    public ArrayList<VariableInfo> getSelectedVariables(){
        if(currentTable==null) return null;

        ArrayList<VariableInfo> varInfo = new ArrayList<VariableInfo>();
        VariableInfo[] vInfo = vsp.getSelectedVariables();
        for(VariableInfo v : vInfo){
            varInfo.add(v);
        }
        return varInfo;
    }

    private void reset(){
        tableList.clearSelection();
        vsp.getUnselectedListModel().clear();
        vsp.getSelectedListModel().clear();
    }

    private void setVariables(){
        reset();
        VariableListModel vlm = vsp.getUnselectedListModel();

        try{
            ArrayList<VariableInfo> tempVar = dao.getVariableInfoFromColumn(conn, currentTable, nameColumn);

            for(VariableInfo v : tempVar){
                vlm.addElement(v);
            }

        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            JOptionPane.showMessageDialog(ItemParameterTableDialog.this,
                    "Selected table is probably not a table of item parameters.\n" +
                            "Select another table.",
                    "SQL Exception",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    private void openTable(DataTableName tableName){
        if(currentTable!=null && currentTable.equals(tableName)) return;
        VariableTableName variableTableName = new VariableTableName(tableName.toString());
        currentTable = tableName;
        setVariables();

    }

    public boolean canRun(){
        return currentTable!=null && vsp.hasSelection();
    }

    public class TableSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e){
            DataTableName tableName = (DataTableName) tableList.getSelectedValue();
            if(tableName!=null){
                openTable(tableName);
            }
        }
    }



}

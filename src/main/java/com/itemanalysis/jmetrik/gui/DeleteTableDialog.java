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

import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.DatabaseCommand;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class DeleteTableDialog extends JDialog{

    private static final long serialVersionUID = 1L;
    private DatabaseName dbName = null;
    private JList tableList = null;
    private boolean canRun = false;
    private DatabaseCommand command = null;
    private SortedListModel<DataTableName> listModel = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");


    public DeleteTableDialog(JFrame parent, DatabaseName dbName, SortedListModel<DataTableName> listModel){
        super(parent, "Delete Tables",true);
        this.dbName = dbName;
        this.listModel = listModel;
        tableList = new JList();
        tableList.setModel(listModel);
        tableList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        JScrollPane dataScroll = new JScrollPane(tableList);
        dataScroll.setPreferredSize(new Dimension(200,250));

        JPanel main = new JPanel();
        main.setPreferredSize(new Dimension(200,300));
        main.setLayout(new GridBagLayout());

        JPanel topPanel = new JPanel();
        topPanel.setPreferredSize(new Dimension(200,250));
        topPanel.add(dataScroll, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 4;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.BOTH;
        main.add(topPanel,c);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());

        JButton okButton = new JButton("OK");
        okButton.addActionListener(new RunActionListener());


        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun = false;
                setVisible(false);
            }
        });
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        buttonPanel.add(new JPanel(),c);
        c.gridx = 2;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        buttonPanel.add(okButton,c);
        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        buttonPanel.add(new JPanel(),c);
        c.gridx = 4;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        buttonPanel.add(cancelButton,c);
        c.gridx = 5;
        c.gridy = 0;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        buttonPanel.add(new JPanel(),c);


        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        main.add(buttonPanel,c);

        getContentPane().add(main, BorderLayout.CENTER);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);

    }

    public boolean canRun(){
        return canRun;
    }
    
    public DatabaseCommand getCommand(){
        return command;
    }
    
    public int getNumberOfSelectedTables(){
        int[] selectedIndices = tableList.getSelectedIndices();
        return selectedIndices.length;
    }

    public ArrayList<DataTableName> getSelectedTables(){

        ArrayList<DataTableName> selectedTables = new ArrayList<DataTableName>();
        int[] selection = tableList.getSelectedIndices();

        for(int i : selection) {
            selectedTables.add(listModel.getElementAt(i));
        }

        return selectedTables;
    }

    public class RunActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e){

            ArrayList<DataTableName> tableName = getSelectedTables();

            if(tableName.isEmpty()){
                JOptionPane.showMessageDialog(DeleteTableDialog.this,
                        "You must select a data table.",
                        "No data table selected",
                        JOptionPane.ERROR_MESSAGE);
            }else{
                command = new DatabaseCommand();
                command.getFreeOption("name").add(dbName.getName());
                command.getSelectOneOption("action").setSelected("delete-table");
                for(DataTableName dName : tableName){
                    command.getFreeOptionList("tables").addValue(dName.toString());
                }
                canRun = true;
                setVisible(false);
            }

        }


    }//end AboutAction

}

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
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SelectTableDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private DatabaseName dbName = null;
    private JList tableList = null;
    private boolean canRun = false;
    DataTableName tableName = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public SelectTableDialog(JDialog parent, DatabaseName dbName, SortedListModel<DataTableName> listModel){
        super(parent, "Select Table",true);
        this.dbName = dbName;
        tableList = new JList(listModel);
        tableList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

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
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableName = getSelectedTable();
                if(tableName==null){
                    JOptionPane.showMessageDialog(SelectTableDialog.this,
                            "You must select a data table.",
                            "No data table selected",
                            JOptionPane.ERROR_MESSAGE);
                }else{
                    canRun = true;
                    setVisible(false);
                }
            }
        });
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
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
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

    }

    public boolean canRun(){
        return canRun;
    }

    public DatabaseName getDatabaseName(){
        return dbName;
    }

    public DataTableName getTableName(){
        return tableName;
    }

    public int getNumberOfSelectedTables(){
        if(tableList.getSelectedValue()==null) return 0;
        return 1;
    }

    /**
     * Only Path with two levels are paths to data tables.
     * The last path component for a path with two levels is the
     * name of a data table.
     *
     * @return
     */
    public DataTableName getSelectedTable(){
        return (DataTableName)tableList.getSelectedValue();
    }
}

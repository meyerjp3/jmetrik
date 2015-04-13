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

package com.itemanalysis.jmetrik.stats.irt.rasch;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class RaschItemStartValuesDialog extends JDialog{

    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private DataTableName startTableName = null;
    private JTable table = null;
    private RaschStartValueTableModel model = null;
    private boolean hasValidTable = false;
    private JList tableList = null;
    private DataTableName currentTable = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public RaschItemStartValuesDialog(RaschDialog parent, Connection conn, DatabaseAccessObject dao, SortedListModel<DataTableName> tableListModel){
        super(parent, "Item Start Values", true);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        this.conn = conn;
        this.dao = dao;

        tableList = new JList(tableListModel);
        tableList.setName("tableList");
        tableList.addListSelectionListener(new TableSelectionListener());

        table = new JTable(new DefaultTableModel());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setPreferredSize(new Dimension(450,350));
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 6;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(getTreePanel(),c);

        c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 6;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(getTablePanel(),c);

        c.gridx = 0;
		c.gridy = 6;
		c.gridwidth = 6;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.SOUTH;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(getButtonPanel(),c);

        //add main panel to this
        getContentPane().add(mainPanel,BorderLayout.CENTER);
		pack();
		setResizable(false);
		setLocationRelativeTo(parent);
    }

    public final JScrollPane getTreePanel(){
        JScrollPane sp = new JScrollPane();
		sp.setPreferredSize(new Dimension(150,300));
		sp.getViewport().add(tableList);
		sp.setBorder(new TitledBorder("Table List"));
        return sp;
    }

    public final JScrollPane getTablePanel(){
        JScrollPane sp = new JScrollPane();
		sp.setPreferredSize(new Dimension(300,300));
		sp.getViewport().add(table);
		sp.setBorder(new TitledBorder("Start and Fixed Value Specification"));
        return sp;
    }

    public final JPanel getButtonPanel(){
        JPanel panel = new JPanel();
        panel.setLayout( new GridBagLayout());
        panel.setPreferredSize(new Dimension(400,50));
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JPanel(),c);

        JButton okButton = new JButton(new OkAction("OK"));
        c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(okButton,c);

        c.gridx = 6;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JPanel(),c);

        JButton cancelButton = new JButton(new OkAction("Cancel"));
        c.gridx = 7;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(cancelButton,c);

        c.gridx = 9;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.CENTER;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(new JPanel(),c);

        return panel;
    }

    public boolean hasValidTable(){
        return hasValidTable;
    }

    public Boolean hasStartValueNames(){
        RaschStartValueTableModel m = (RaschStartValueTableModel)table.getModel();
        return m.hasStartValueNames();
    }

    public Boolean hasFixedValueNames(){
        RaschStartValueTableModel m = (RaschStartValueTableModel)table.getModel();
        return m.hasFixedValueNames();
    }

    public ArrayList<VariableName> getFixedValueNames(){
        RaschStartValueTableModel m = (RaschStartValueTableModel)table.getModel();
        return m.getFixedValueNames();
    }

    public DatabaseName getStartDatabase(){
        return dbName;
    }

    public DataTableName getStartTable(){
        return startTableName;
    }

    private void setVariables(ArrayList<VariableAttributes> variables){
        model = new RaschStartValueTableModel(variables);
        table.setModel(model);
        model.fireTableDataChanged();
    }

    private void openTable(DataTableName tableName){
        try{
            if(currentTable!=null && currentTable.equals(tableName)) return;
            VariableTableName variableTableName = new VariableTableName(tableName.toString());
            ArrayList<VariableAttributes> v = dao.getAllVariables(conn, variableTableName);
            setVariables(v);
            currentTable = tableName;
        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            JOptionPane.showMessageDialog(RaschItemStartValuesDialog.this,
                    "Table could not be opened.",
                    "SQL Exception",
                    JOptionPane.ERROR_MESSAGE);
        }

    }

    public class OkAction extends AbstractAction{

		private static final long serialVersionUID = 1L;
		final static String TOOL_TIP = "Run analysis";

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
            //do something
            setVisible(false);
		}

	}//end RunAction

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

	}//end CancelAction

    public class TableSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e){
            DataTableName tableName = (DataTableName)tableList.getSelectedValue();
            if(tableName!=null){
                openTable(tableName);
            }
        }
    }


}


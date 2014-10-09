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

import com.itemanalysis.jmetrik.selector.MultipleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.DeleteVariableCommand;
import com.itemanalysis.psychometrics.data.VariableInfo;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class DeleteVariableDialog extends JDialog {

    private MultipleSelectionPanel vsp;
	private JPanel mainPanel, blankPanel;
	private ArrayList<VariableInfo> variables;
	boolean canRun=false;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private DeleteVariableCommand command = null;
    private int numberOfSelectedVariables = 0;
    private VariableInfo selectedVariable = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public DeleteVariableDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList <VariableInfo> variables){
        super(parent,"Delete Variables",true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		this.variables=variables;
        this.dbName = dbName;
        this.tableName = tableName;

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

		vsp = new MultipleSelectionPanel();
        vsp.setVariables(variables);

        JButton b1 = vsp.getButton1();
        b1.setText("Run");
        b1.addActionListener(new RunActionListener());

        JButton b2 = vsp.getButton2();
        b2.setText("Cancel");
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun=false;
			    setVisible(false);
            }
        });

        vsp.showButton3(false);
        vsp.showButton4(false);

		mainPanel=new JPanel();
//		mainPanel.setPreferredSize(new Dimension(340,250));
		mainPanel.setLayout(new GridBagLayout());

		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 3;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHEAST;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(vsp,c);

		getContentPane().add(mainPanel,BorderLayout.CENTER);
		pack();
		setResizable(false);
		setLocationRelativeTo(parent);

    }

    public DeleteVariableCommand getCommand(){
        return command;
    }

    public boolean canRun(){
        return canRun;
    }

    public int getNumberOfSelectedVariables(){
        return  numberOfSelectedVariables;
    }

    public VariableInfo getSelectedVariable(){
        return selectedVariable;
    }

    public class RunActionListener implements ActionListener{

		public void actionPerformed(ActionEvent e){

            VariableInfo[] v = vsp.getSelectedVariables();

            if(vsp.getSelectedVariables().length>0){
                numberOfSelectedVariables = v.length;
                selectedVariable = v[0];

                try{
                    command = new DeleteVariableCommand();
                    for(int i=0;i<v.length;i++){
                        command.getFreeOptionList("variables").addValue((v[i]).getName().toString());
                    }
                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", tableName.toString());
                    canRun=true;
                    setVisible(false);
                }catch(IllegalArgumentException ex){
                    logger.fatal(ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(DeleteVariableDialog.this,
                            ex.getMessage(),
                            "Syntax Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }

		}

	}//end RunAction


}

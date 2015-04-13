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

package com.itemanalysis.jmetrik.stats.frequency;

import com.itemanalysis.jmetrik.selector.MultipleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class FrequencyDialog extends JDialog {

    private static final long serialVersionUID = 1L;
    private MultipleSelectionPanel vsp;
    private ArrayList<VariableAttributes> varInfo;
    private boolean canRun = false;
    private FrequencyCommand command = null;
    private DatabaseName dbName = null;
    private DataTableName table = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public FrequencyDialog(JFrame parent, DatabaseName dbName, DataTableName table, ArrayList <VariableAttributes> variables){
        super(parent,"Frequency Analysis",true);
        this.dbName = dbName;
        this.table = table;

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        vsp = new MultipleSelectionPanel();
        vsp.addUnselectedFilterDataType(DataType.NO_DATATYPE_FILTER);
        vsp.setVariables(variables);
        vsp.showButton4(false);
        JButton b1 = vsp.getButton1();
        b1.setText("Run");
        b1.addActionListener(new RunActionListener());

        JButton b2 = vsp.getButton2();
        b2.setText("Cancel");
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun =false;
                setVisible(false);
            }
        });

        JButton b3 = vsp.getButton3();
        b3.setText("Clear");
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vsp.reset();
            }
        });

        getContentPane().add(vsp,BorderLayout.CENTER);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public boolean canRun(){
        return canRun;
    }

    public FrequencyCommand getCommand(){
        return command;
    }

    public VariableAttributes[] getSelectedVariables(){
        return vsp.getSelectedVariables();
    }

    public void removeVariable(int column){
        varInfo.remove(column);
    }

    public void addVariable(VariableAttributes variable){
        varInfo.add(variable);
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            Object[] v = vsp.getSelectedVariables();
            if(vsp.getSelectedVariables().length>0){
                try{
                    command = new FrequencyCommand();

                    for(int i=0;i<v.length;i++){
                        command.getFreeOptionList("variables").addValue(((VariableAttributes) v[i]).getName().toString());
                    }
                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", table.toString());
                    canRun =true;
                    setVisible(false);
                }catch(IllegalArgumentException ex){
                    logger.fatal(ex.getMessage(), ex);
                    firePropertyChange("error", "", "Error - Check log for details.");
                }
            }
        }

    }//end RunAction

}

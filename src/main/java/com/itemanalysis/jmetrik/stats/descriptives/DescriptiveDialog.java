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

package com.itemanalysis.jmetrik.stats.descriptives;

import javax.swing.*;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;

import com.itemanalysis.jmetrik.selector.MultipleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;

public class DescriptiveDialog extends JDialog {

    MultipleSelectionPanel vsp;
    JPanel mainPanel, optionPanel;
    JCheckBox minCheck, q1Check, meanCheck, medianCheck, q3Check, maxCheck, sdCheck, iqrCheck, skewnessCheck, kurtosisCheck;
    boolean canRun=false;
    DescriptiveCommand command = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    DatabaseName dbName = null;
    DataTableName tableName = null;
    private boolean min=true, q1=true, mean=true, med=true, q3=true, sd=true, iqr=true, max=true, skew=true, kurtosis=true;


    public DescriptiveDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList<VariableAttributes> variables){
        super(parent,"Descriptive Analysis",true);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

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

        //filter out strings
//        VariableType filterType1 = new VariableType(ItemType.BINARY_ITEM, DataType.STRING);
//        VariableType filterType2 = new VariableType(ItemType.POLYTOMOUS_ITEM, DataType.STRING);
//        VariableType filterType3 = new VariableType(ItemType.CONTINUOUS_ITEM, DataType.STRING);
//        VariableType filterType4 = new VariableType(ItemType.NOT_ITEM, DataType.STRING);
//        vsp.addUnselectedFilterType(filterType1);
//        vsp.addUnselectedFilterType(filterType2);
//        vsp.addUnselectedFilterType(filterType3);
//        vsp.addUnselectedFilterType(filterType4);
//        vsp.addSelectedFilterType(filterType1);
//        vsp.addSelectedFilterType(filterType2);
//        vsp.addSelectedFilterType(filterType3);
//        vsp.addSelectedFilterType(filterType4);

        vsp.addUnselectedFilterDataType(DataType.STRING);
        vsp.addSelectedFilterDataType(DataType.STRING);
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

        mainPanel=new JPanel();
//        mainPanel.setPreferredSize(new Dimension(340,375));
        mainPanel.setLayout(new GridBagLayout());

        optionPanel=new JPanel();
        optionPanel.setLayout(new GridLayout(5,2));
        optionPanel.setBorder(new TitledBorder("Statistics"));

        minCheck=new JCheckBox("Minimum");
        minCheck.setToolTipText("Minimum value");
        minCheck.setSelected(true);
        minCheck.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(minCheck.isSelected()) min=true;
                else min=false;
            }
        });
        optionPanel.add(minCheck);

        maxCheck=new JCheckBox("Max");
        maxCheck.setToolTipText("Maximum value");
        maxCheck.setSelected(true);
        maxCheck.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(maxCheck.isSelected()) max=true;
                else max=false;
            }
        });
        optionPanel.add(maxCheck);

        q1Check=new JCheckBox("First quartile");
        q1Check.setToolTipText("First quartile");
        q1Check.setSelected(true);
        q1Check.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(q1Check.isSelected()) q1=true;
                else q1=false;
            }
        });
        optionPanel.add(q1Check);

        q3Check=new JCheckBox("Third quartile");
        q3Check.setToolTipText("Third quartile");
        q3Check.setSelected(true);
        q3Check.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(q3Check.isSelected()) q3=true;
                else q3=false;
            }
        });
        optionPanel.add(q3Check);

        meanCheck=new JCheckBox("Mean");
        meanCheck.setToolTipText("Mean");
        meanCheck.setSelected(true);
        meanCheck.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(meanCheck.isSelected()) mean=true;
                else mean=false;
            }
        });
        optionPanel.add(meanCheck);

        medianCheck=new JCheckBox("Median");
        medianCheck.setToolTipText("Median");
        medianCheck.setSelected(true);
        medianCheck.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(medianCheck.isSelected()) med=true;
                else med=false;
            }
        });
        optionPanel.add(medianCheck);

        sdCheck=new JCheckBox("Standard deviation");
        sdCheck.setToolTipText("Standard deviation");
        sdCheck.setSelected(true);
        sdCheck.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(sdCheck.isSelected()) sd=true;
                else sd=false;
            }
        });
        optionPanel.add(sdCheck);

        iqrCheck=new JCheckBox("Interquartile range");
        iqrCheck.setToolTipText("Interquartile range");
        iqrCheck.setSelected(true);
        iqrCheck.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(iqrCheck.isSelected()) iqr=true;
                else iqr=false;
            }
        });
        optionPanel.add(iqrCheck);

        skewnessCheck=new JCheckBox("Skewness");
        skewnessCheck.setToolTipText("Skewness");
        skewnessCheck.setSelected(true);
        skewnessCheck.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(skewnessCheck.isSelected()) skew=true;
                else skew=false;
            }
        });
        optionPanel.add(skewnessCheck);

        kurtosisCheck=new JCheckBox("Kurtosis");
        kurtosisCheck.setToolTipText("Kurtosis");
        kurtosisCheck.setSelected(true);
        kurtosisCheck.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(kurtosisCheck.isSelected()) kurtosis=true;
                else kurtosis=false;
            }
        });
        optionPanel.add(kurtosisCheck);


        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 5;
        c.gridheight = 5;
        c.weightx = 5;
        c.weighty = 5;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(vsp,c);

        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 5;
        c.gridheight = 4;
        c.weightx = 5;
        c.weighty = 5;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(optionPanel,c);

        getContentPane().add(mainPanel,BorderLayout.CENTER);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    public VariableAttributes[] getSelectedVariables(){
        return vsp.getSelectedVariables();
    }

    public boolean canRun(){
        return canRun;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public DescriptiveCommand getCommand(){
        return command;
    }

    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            if(vsp.getSelectedVariables().length>0){
                try{
                    command = new DescriptiveCommand();
                    Object[] v = vsp.getSelectedVariables();
                    for(int i=0;i<v.length;i++){
                        command.getFreeOptionList("variables").addValue(((VariableAttributes) v[i]).getName().toString());
                    }
                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", tableName.toString());

                    command.getSelectAllOption("stats").setSelected("min", min);
                    command.getSelectAllOption("stats").setSelected("max", max);
                    command.getSelectAllOption("stats").setSelected("q1", q1);
                    command.getSelectAllOption("stats").setSelected("q3", q3);
                    command.getSelectAllOption("stats").setSelected("mean", mean);
                    command.getSelectAllOption("stats").setSelected("median", med);
                    command.getSelectAllOption("stats").setSelected("sd", sd);
                    command.getSelectAllOption("stats").setSelected("iqr", iqr);
                    command.getSelectAllOption("stats").setSelected("skew", skew);
                    command.getSelectAllOption("stats").setSelected("kurtosis", kurtosis);

                    canRun=true;
                    setVisible(false);
                }catch(IllegalArgumentException ex){
                    logger.fatal(ex.getMessage(), ex);
                    firePropertyChange("error", "", "Error - Check log for details.");
                }
            }
        }

    }//end RunAction

}

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

package com.itemanalysis.jmetrik.stats.correlation;

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
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.itemanalysis.jmetrik.selector.MultipleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;


public class CorrelationDialog extends JDialog{

    private static final long serialVersionUID = 1L;
    MultipleSelectionPanel vsp;
    JPanel mainPanel;
    boolean canRun =false;
    CorrelationCommand command = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    ButtonGroup missingGroup = null;
    ButtonGroup typeGroup = null;
    ButtonGroup estimatorGroup = null;
    ButtonGroup polychoricGroup = null;
    JCheckBox stdErrorBox = null;
    private boolean showStdError = false;

    public CorrelationDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList <VariableAttributes> variables){
        super(parent,"Correlation Analysis",true);
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
//        VariableType filterType1 = new VariableType(VariableType.BINARY_ITEM, VariableType.STRING);
//        VariableType filterType2 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.STRING);
//        VariableType filterType3 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.STRING);
//        VariableType filterType4 = new VariableType(VariableType.NOT_ITEM, VariableType.STRING);
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
//        mainPanel.setPreferredSize(new Dimension(340,450));
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


        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new GridBagLayout());
        bottomPanel.setBorder(new EmptyBorder(5,5,5,5));
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        bottomPanel.add(getTypePanel(),c);
        c.gridx = 1;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        bottomPanel.add(getPolychoricPanel(),c);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        bottomPanel.add(getOptionPanel(),c);
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        bottomPanel.add(getMissingPanel(),c);
        c.gridx = 1;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.BOTH;
        bottomPanel.add(getBiasedPanel(),c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 4;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.fill = GridBagConstraints.HORIZONTAL;
        mainPanel.add(bottomPanel,c);

        getContentPane().add(mainPanel,BorderLayout.CENTER);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private JPanel getMissingPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,1));
        panel.setBorder(new TitledBorder("Delete Missing Data"));
        missingGroup = new ButtonGroup();
        JRadioButton listwiseButton = new JRadioButton("Listwise");
        listwiseButton.setActionCommand("listwise");
        listwiseButton.setSelected(true);
        missingGroup.add(listwiseButton);
        panel.add(listwiseButton);

        JRadioButton pairwiseButton = new JRadioButton("Pairwise");
        pairwiseButton.setActionCommand("pairwise");
        missingGroup.add(pairwiseButton);
        panel.add(pairwiseButton);
        return panel;
    }

    private JPanel getTypePanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,1));
        panel.setBorder(new TitledBorder("Type of Correlation"));
        typeGroup = new ButtonGroup();
        JRadioButton pearsonButton = new JRadioButton("Pearson");
        pearsonButton.setActionCommand("pearson");
        pearsonButton.setSelected(true);
        typeGroup.add(pearsonButton);
        panel.add(pearsonButton);

        JRadioButton polychoricButton = new JRadioButton("Mixed");
        polychoricButton.setActionCommand("mixed");
        polychoricButton.setEnabled(false);
        typeGroup.add(polychoricButton);
        panel.add(polychoricButton);
        return panel;
    }

    private JPanel getPolychoricPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,1));
        panel.setBorder(new TitledBorder("Polychoric Estimation"));
        polychoricGroup = new ButtonGroup();
        JRadioButton twoStepButton = new JRadioButton("Two-step");
        twoStepButton.setActionCommand("twostep");
        twoStepButton.setEnabled(false);
        twoStepButton.setSelected(true);
        polychoricGroup.add(twoStepButton);
        panel.add(twoStepButton);

        JRadioButton mlButton = new JRadioButton("Maximum likelihood");
        mlButton.setActionCommand("ml");
        mlButton.setEnabled(false);
        polychoricGroup.add(mlButton);
        panel.add(mlButton);
        return panel;
    }

    private JPanel getBiasedPanel(){
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(2,1));
        panel.setBorder(new TitledBorder("Pearson Estimator"));
        estimatorGroup = new ButtonGroup();
        JRadioButton unbiasedButton = new JRadioButton("Unbiased");
        unbiasedButton.setActionCommand("unbiased");
        unbiasedButton.setSelected(true);
        estimatorGroup.add(unbiasedButton);
        panel.add(unbiasedButton);

        JRadioButton biasedButton = new JRadioButton("Biased");
        biasedButton.setActionCommand("biased");
        estimatorGroup.add(biasedButton);
        panel.add(biasedButton);
        return panel;
    }

    private JPanel getOptionPanel(){
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Options"));
        stdErrorBox = new JCheckBox("Show standard error");
        stdErrorBox.setToolTipText("compute and show correlation standard errors");
        stdErrorBox.setSelected(false);
        stdErrorBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                if(stdErrorBox.isSelected()) showStdError=true;
                else showStdError=false;
            }
        });
        panel.add(stdErrorBox, BorderLayout.WEST);
        return panel;
    }

    public boolean canRun(){
        return canRun;
    }

    public CorrelationCommand getCommand(){
        return command;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            if(vsp.getSelectedVariables().length<=1){
                JOptionPane.showMessageDialog(CorrelationDialog.this,
                        "You must select at least two variables.",
                        "Variable Selection Error",
                        JOptionPane.ERROR_MESSAGE);
            }else{
                try{
                    command = new CorrelationCommand();
                    Object[] v = vsp.getSelectedVariables();
                    for(int i=0;i<v.length;i++){
                        command.getFreeOptionList("variables").addValue(((VariableAttributes) v[i]).getName().toString());
                    }
                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", tableName.toString());

                    command.getSelectOneOption("missing").setSelected(missingGroup.getSelection().getActionCommand());
                    command.getSelectOneOption("type").setSelected(typeGroup.getSelection().getActionCommand());
                    command.getSelectOneOption("estimator").setSelected(estimatorGroup.getSelection().getActionCommand());
                    command.getSelectOneOption("polychoric").setSelected(polychoricGroup.getSelection().getActionCommand());

                    if(showStdError){
                        command.getSelectAllOption("options").setSelected("stderror", true);
                    }

                }catch(IllegalArgumentException ex){
                    JOptionPane.showMessageDialog(CorrelationDialog.this,
                            ex.getMessage(),
                            "Error - Send This Message with Help Request",
                            JOptionPane.ERROR_MESSAGE);
                }
                canRun =true;
                setVisible(false);
            }
        }

    }//end RunAction

}

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

package com.itemanalysis.jmetrik.stats.transformation;

import com.itemanalysis.jmetrik.selector.SingleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.data.VariableType;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class LinearTransformationDialog extends JDialog{

    // Variables declaration - do not modify
    private JPanel constraintPanel;
    private JLabel interceptLabel;
    private JTextField interceptTextField;
    private JLabel maxLabel;
    private JTextField maxTextField;
    private JLabel minLabel;
    private JTextField minTextField;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JLabel precisionLabel;
    private JTextField precisionTextField;
    private JLabel slopeLabel;
    private JTextField slopeTextField;
    private JPanel transformationPanel;
    // End of variables declaration

    private SingleSelectionPanel vsp = null;
    private boolean canRun = false;
    private LinearTransformationCommand command = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    static Logger logger = org.apache.log4j.Logger.getLogger("jmetrik-logger");

    public LinearTransformationDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList<VariableInfo> variables){
        super(parent, "Linear Transformation",true);

        this.dbName = dbName;
        this.tableName = tableName;

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        vsp = new SingleSelectionPanel();
        VariableType filterType1 = new VariableType(VariableType.BINARY_ITEM, VariableType.STRING);
        VariableType filterType2 = new VariableType(VariableType.BINARY_ITEM, VariableType.DOUBLE);
        VariableType filterType3 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.STRING);
        VariableType filterType4 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.DOUBLE);
        VariableType filterType5 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.STRING);
        VariableType filterType6 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.DOUBLE);
        VariableType filterType7 = new VariableType(VariableType.NOT_ITEM, VariableType.STRING);
        vsp.addUnselectedFilterType(filterType1);
        vsp.addUnselectedFilterType(filterType2);
        vsp.addUnselectedFilterType(filterType3);
        vsp.addUnselectedFilterType(filterType4);
        vsp.addUnselectedFilterType(filterType5);
        vsp.addUnselectedFilterType(filterType6);
        vsp.addUnselectedFilterType(filterType7);
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

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

    }

    private void initComponents() {
        transformationPanel = new JPanel();
        interceptLabel = new JLabel();
        slopeLabel = new JLabel();
        interceptTextField = new JTextField();
        slopeTextField = new JTextField();
        constraintPanel = new JPanel();
        minLabel = new JLabel();
        minTextField = new JTextField();
        maxLabel = new JLabel();
        maxTextField = new JTextField();
        precisionLabel = new JLabel();
        precisionTextField = new JTextField();
        nameLabel = new JLabel();
        nameTextField = new JTextField();

        transformationPanel.setBorder(BorderFactory.createTitledBorder("Linear Transformation"));

        interceptLabel.setText("Mean");

        slopeLabel.setText("Std. Dev.");

        interceptTextField.setMaximumSize(new Dimension(75, 28));
        interceptTextField.setMinimumSize(new Dimension(75, 28));
        interceptTextField.setPreferredSize(new Dimension(75, 28));

        slopeTextField.setMaximumSize(new Dimension(75, 28));
        slopeTextField.setMinimumSize(new Dimension(75, 28));
        slopeTextField.setPreferredSize(new Dimension(75, 28));

        GroupLayout transformationPanelLayout = new GroupLayout(transformationPanel);
        transformationPanel.setLayout(transformationPanelLayout);
        transformationPanelLayout.setHorizontalGroup(
            transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(transformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(slopeLabel)
                    .addComponent(interceptLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(interceptTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(slopeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        transformationPanelLayout.setVerticalGroup(
            transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(transformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(interceptLabel)
                    .addComponent(interceptTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(slopeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(slopeLabel))
                .addContainerGap(45, Short.MAX_VALUE))
        );

        constraintPanel.setBorder(BorderFactory.createTitledBorder("Constraints"));

        minLabel.setText("Minimum");

        minTextField.setMaximumSize(new Dimension(75, 28));
        minTextField.setMinimumSize(new Dimension(75, 28));
        minTextField.setPreferredSize(new Dimension(75, 28));

        maxLabel.setText("Maximum");

        maxTextField.setMaximumSize(new Dimension(75, 28));
        maxTextField.setMinimumSize(new Dimension(75, 28));
        maxTextField.setPreferredSize(new Dimension(75, 28));

        precisionLabel.setText("Precision");

        precisionTextField.setMaximumSize(new Dimension(75, 28));
        precisionTextField.setMinimumSize(new Dimension(75, 28));
        precisionTextField.setPreferredSize(new Dimension(75, 28));

        GroupLayout constraintPanelLayout = new GroupLayout(constraintPanel);
        constraintPanel.setLayout(constraintPanelLayout);
        constraintPanelLayout.setHorizontalGroup(
            constraintPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(constraintPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(constraintPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(precisionLabel)
                    .addComponent(maxLabel)
                    .addComponent(minLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(constraintPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(precisionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        constraintPanelLayout.setVerticalGroup(
            constraintPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(constraintPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(constraintPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(minLabel)
                    .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(constraintPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(constraintPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(precisionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(precisionLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nameLabel.setText("New Variable Name");
        nameLabel.setToolTipText("");

        nameTextField.setToolTipText("");

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, 253, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(transformationPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(constraintPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(constraintPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(transformationPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    public boolean canRun(){
        return canRun;
    }

    public LinearTransformationCommand getCommand(){
        return command;
    }

    public VariableInfo getSelectedVariables(){
        return vsp.getSelectedVariables();
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }


    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            VariableInfo v = vsp.getSelectedVariables();
            if(vsp.selectionMade()){
                try{
                    command = new LinearTransformationCommand();

                    command.getFreeOptionList("variables").addValue(v.getName().toString());
                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", tableName.toString());

                    String n = nameTextField.getText().trim();
                    if(n.equals("")) n = "score";
                    VariableName vName = new VariableName(n);
                    command.getFreeOption("name").add(vName.toString());

                    if(minTextField.getText().equals("")){
                        command.getPairedOptionList("constraints").addValue("min", Double.NEGATIVE_INFINITY);
                    }else{
                        command.getPairedOptionList("constraints").addValue("min", Double.parseDouble(minTextField.getText()));
                    }

                    if(maxTextField.getText().equals("")){
                        command.getPairedOptionList("constraints").addValue("max", Double.POSITIVE_INFINITY);
                    }else{
                        command.getPairedOptionList("constraints").addValue("max", Double.parseDouble(maxTextField.getText()));
                    }

                    if(precisionTextField.getText().equals("")){
                        command.getPairedOptionList("constraints").addValue("precision", 2);
                    }else{
                        command.getPairedOptionList("constraints").addValue("precision", Integer.parseInt(precisionTextField.getText()));
                    }

                    if(interceptTextField.getText().trim().equals("") && slopeTextField.getText().trim().equals("")){
                        if(minTextField.getText().trim().equals("") || maxTextField.getText().trim().equals("")){
                            JOptionPane.showMessageDialog(LinearTransformationDialog.this,
                                "If you do not specify the slope and intercept, you must specify \n" +
                                        "a minimum and maximum value for the linear transformation",
                                "Linear Transformation Error",
                                JOptionPane.ERROR_MESSAGE);
                            canRun=false;
                        }else{
                            canRun = true;
                        }

                    }else if((!interceptTextField.getText().trim().equals("") && slopeTextField.getText().trim().equals("")) ||
                            (interceptTextField.getText().trim().equals("") && !slopeTextField.getText().trim().equals(""))){
                        JOptionPane.showMessageDialog(LinearTransformationDialog.this,
                                "You must specify a slope and intercept for the linear transformation",
                                "Linear Transformation Error",
                                JOptionPane.ERROR_MESSAGE);
                        canRun = false;
                    }else{
                        Double m = Double.parseDouble(interceptTextField.getText().trim());
                        command.getPairedOptionList("transform").addValue("intercept", m);
                        m = Double.parseDouble(slopeTextField.getText().trim());
                        command.getPairedOptionList("transform").addValue("slope", m);
                        canRun=true;
                    }

                    if(canRun) setVisible(false);

                }catch(IllegalArgumentException ex){
                    logger.fatal(ex.getMessage(), ex);
                    firePropertyChange("error", "", "Error - Check log for details.");
                }
            }
        }

    }//end RunAction



}

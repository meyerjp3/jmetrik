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

package com.itemanalysis.jmetrik.stats.scaling;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.itemanalysis.jmetrik.selector.MultipleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import org.apache.log4j.Logger;

public class TestScalingDialog extends JDialog{

    // Variables declaration - do not modify
    private JPanel constraintPanel;
    private JLabel meanLabel;
    private JTextField meanTextField;
    private JComboBox scoreNameComboBox;
    private JLabel maxLabel;
    private JTextField maxTextField;
    private JLabel minLabel;
    private JTextField minTextField;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JLabel precisionLabel;
    private JTextField precisionTextField;
    private JPanel scorePanel;
    private JLabel stdDevLabel;
    private JTextField stdDevTextField;
    private JPanel transformationPanel;
    private JLabel typeLabel;
    // End of variables declaration

    private boolean canRun = false;
    private TestScalingCommand command = null;
    private MultipleSelectionPanel vsp = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private String[] scoreNames = {"Sum score", "Average score", "Kelley score", "Percentile rank", "Normalized score"};
    private HashMap<String, String> scoreNameConversion = null;
    private String scoreType = "sum";
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public TestScalingDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList <VariableAttributes> variables){
        super(parent,"Test Scaling",true);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.dbName = dbName;
        this.tableName = tableName;

        //HashMap for converting combobox text to command argument name
        scoreNameConversion = new HashMap<String, String>();
        scoreNameConversion.put(scoreNames[0], "sum");
        scoreNameConversion.put(scoreNames[1], "mean");
        scoreNameConversion.put(scoreNames[2], "kelley");
        scoreNameConversion.put(scoreNames[3], "prank");
        scoreNameConversion.put(scoreNames[4], "normal");

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        vsp = new MultipleSelectionPanel();

        //Filter out not item
//        VariableType filterType1 = new VariableType(ItemType.NOT_ITEM, DataType.STRING);
//        VariableType filterType4 = new VariableType(ItemType.NOT_ITEM, DataType.DOUBLE);
//        vsp.addUnselectedFilterType(filterType1);
//        vsp.addUnselectedFilterType(filterType4);
//        vsp.addSelectedFilterType(filterType1);
//        vsp.addSelectedFilterType(filterType4);

        vsp.addUnselectedFilterItemType(ItemType.NOT_ITEM);
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
                nameTextField.setText("");
                meanTextField.setText("");
                stdDevTextField.setText("");
                minTextField.setText("");
                maxTextField.setText("");
                precisionTextField.setText("");
            }
        });

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

    }


    private void initComponents() {
        scorePanel = new JPanel();
        nameLabel = new JLabel();
        nameTextField = new JTextField();
        typeLabel = new JLabel();
        scoreNameComboBox = new JComboBox();
        transformationPanel = new JPanel();
        meanLabel = new JLabel();
        stdDevLabel = new JLabel();
        meanTextField = new JTextField();
        stdDevTextField = new JTextField();
        constraintPanel = new JPanel();
        minLabel = new JLabel();
        minTextField = new JTextField();
        maxLabel = new JLabel();
        maxTextField = new JTextField();
        precisionLabel = new JLabel();
        precisionTextField = new JTextField();

        scorePanel.setBorder(BorderFactory.createTitledBorder("Score"));

        nameLabel.setText("Name");

        nameTextField.setMaximumSize(new Dimension(200, 28));
        nameTextField.setMinimumSize(new Dimension(200, 28));
        nameTextField.setPreferredSize(new Dimension(200, 28));

        typeLabel.setText("Type");

        scoreNameComboBox.setModel(new DefaultComboBoxModel(scoreNames));
        scoreNameComboBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String s = (String) cb.getSelectedItem();
                scoreType = scoreNameConversion.get(s);
            }
        });

        scoreNameComboBox.setMaximumSize(new Dimension(200, 28));
        scoreNameComboBox.setMinimumSize(new Dimension(200, 28));
        scoreNameComboBox.setPreferredSize(new Dimension(200, 28));

        GroupLayout scorePanelLayout = new GroupLayout(scorePanel);
        scorePanel.setLayout(scorePanelLayout);
        scorePanelLayout.setHorizontalGroup(
                scorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(scorePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(scorePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(typeLabel)
                                        .addComponent(nameLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(scorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(nameTextField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(scoreNameComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap(161, Short.MAX_VALUE))
        );
        scorePanelLayout.setVerticalGroup(
                scorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(scorePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(scorePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(nameLabel)
                                        .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(scorePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(scoreNameComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(typeLabel))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        transformationPanel.setBorder(BorderFactory.createTitledBorder("Linear Transformation"));

        meanLabel.setText("Mean");

        stdDevLabel.setText("Std. Dev.");

        meanTextField.setMaximumSize(new Dimension(75, 28));
        meanTextField.setMinimumSize(new Dimension(75, 28));
        meanTextField.setPreferredSize(new Dimension(75, 28));

        stdDevTextField.setMaximumSize(new Dimension(75, 28));
        stdDevTextField.setMinimumSize(new Dimension(75, 28));
        stdDevTextField.setPreferredSize(new Dimension(75, 28));

        GroupLayout transformationPanelLayout = new GroupLayout(transformationPanel);
        transformationPanel.setLayout(transformationPanelLayout);
        transformationPanelLayout.setHorizontalGroup(
            transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(transformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(stdDevLabel)
                    .addComponent(meanLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(stdDevTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(50, Short.MAX_VALUE))
        );
        transformationPanelLayout.setVerticalGroup(
            transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(transformationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(meanLabel)
                    .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(stdDevTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(stdDevLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(vsp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(scorePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(transformationPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(constraintPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scorePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(constraintPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(transformationPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>


    public boolean canRun(){
        return canRun;
    }

    public TestScalingCommand getCommand(){
        return command;
    }

    public VariableAttributes[] getSelectedVariables(){
        return vsp.getSelectedVariables();
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            VariableAttributes[] v = vsp.getSelectedVariables();
            if(v.length>0){
                try{
                    command = new TestScalingCommand();

                    for(int i=0;i<v.length;i++){
                        command.getFreeOptionList("variables").addValue((v[i]).getName().toString());
                    }
                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", tableName.toString());

                    String n = nameTextField.getText().trim();
                    if(n.equals("")) n = "score";
                    VariableName vName = new VariableName(n);
                    command.getFreeOption("name").add(vName.toString());
                    command.getSelectOneOption("score").setSelected(scoreType);

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

                    if((!meanTextField.getText().trim().equals("") && stdDevTextField.getText().trim().equals("")) ||
                            (meanTextField.getText().trim().equals("") && !stdDevTextField.getText().trim().equals(""))){
                        JOptionPane.showMessageDialog(TestScalingDialog.this,
                                "You must specify a mean and standard deviation \n for the linear transformation",
                                "Linear Transformation Error",
                                JOptionPane.ERROR_MESSAGE);
                    }else{
                        if(!meanTextField.getText().trim().equals("")){
                            Double m = Double.parseDouble(meanTextField.getText().trim());
                            command.getPairedOptionList("transform").addValue("mean", m);
                        }

                        if(!stdDevTextField.getText().trim().equals("")){
                            Double m = Double.parseDouble(stdDevTextField.getText().trim());
                            command.getPairedOptionList("transform").addValue("sd", m);
                        }
                    }

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

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

package com.itemanalysis.jmetrik.graph.irt;

import com.itemanalysis.jmetrik.gui.Jmetrik;
import com.itemanalysis.jmetrik.gui.SelectTableDialog;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.selector.MultipleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

public class IrtPlotDialog extends JDialog {

    private IrtPlotCommand command = null;
    private MultipleSelectionPanel vsp = null;
    private DatabaseName dbName = null;
    private DataTableName table = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    public boolean canRun = false;

    // Variables declaration - do not modify
    private JPanel axisPanel;
    private JRadioButton categoryRadioButton;
    private JPanel curveTypePanel;
    private JRadioButton expectedScoreRadioButton;
    private JCheckBox iccBox;
    private JCheckBox itemInfoBox;
    private JPanel itemPanel;
    private JPanel layoutPanel;
    private JCheckBox legendBox;
    private JLabel maxLabel;
    private JTextField maxTextField;
    private JLabel minLabel;
    private JTextField minTextField;
    private JPanel optionsPanel;
    private JCheckBox tccBox;
    private JCheckBox personSeBox;
    private JCheckBox personInfoBox;
    private JPanel personPanel;
    private JLabel pointsLabel;
    private JTextField pointsTextField;
    private JPanel responsePanel;
    private JLabel responseTableLebel;
    private JTextField responseTableTextField;
    private JButton selectTableButton;
    private JTabbedPane tabbedPane;
    private JLabel thinLabel;
    private JTextField thinTextField;
    private ButtonGroup typeGroup;
    // End of variables declaration
    private JFileChooser outputLocationChooser;
    private String outputPath = "";
    private SortedListModel listModel = null;
    private SelectTableDialog responseTableDialog = null;
    private boolean hasResponseData = false;

    /** Creates new form IrtPlotDialog */
    public IrtPlotDialog(Jmetrik parent, DatabaseName dbName, DataTableName table, ArrayList<VariableAttributes> variables, SortedListModel<DataTableName> listModel) {
        super(parent, "IRT Plot", true);
        this.listModel = listModel;
        this.dbName=dbName;
        this.table=table;
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

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

        JButton b4 = vsp.getButton3();
        b4.setText("Reset");
        b4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vsp.reset();
            }
        });

        JButton b3 = vsp.getButton4();
        b3.setText("Save");
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (outputLocationChooser == null) outputLocationChooser = new JFileChooser();
                outputLocationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                outputLocationChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                outputLocationChooser.setDialogTitle("Select Location");
                if (outputLocationChooser.showDialog(IrtPlotDialog.this, "OK") != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File f = outputLocationChooser.getSelectedFile();
                outputPath = f.getAbsolutePath().replaceAll("\\\\", "/");
            }
        });

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);

    }

     private void initComponents() {

            typeGroup = new ButtonGroup();
            tabbedPane = new JTabbedPane();
            optionsPanel = new JPanel();
            itemPanel = new JPanel();
            iccBox = new JCheckBox();
            itemInfoBox = new JCheckBox();
            legendBox = new JCheckBox();
            personPanel = new JPanel();
            tccBox = new JCheckBox();
            personInfoBox = new JCheckBox();
            personSeBox = new JCheckBox();
            curveTypePanel = new JPanel();
            categoryRadioButton = new JRadioButton();
            expectedScoreRadioButton = new JRadioButton();
            layoutPanel = new JPanel();
            axisPanel = new JPanel();
            minLabel = new JLabel();
            minTextField = new JTextField();
            maxTextField = new JTextField();
            maxLabel = new JLabel();
            pointsLabel = new JLabel();
            pointsTextField = new JTextField();
            responsePanel = new JPanel();
            responseTableLebel = new JLabel();
            responseTableTextField = new JTextField();
            selectTableButton = new JButton();
            thinLabel = new JLabel();
            thinTextField = new JTextField();

    //        setPreferredSize(new Dimension(434, 590));

            tabbedPane.setPreferredSize(new Dimension(400, 275));

            itemPanel.setBorder(BorderFactory.createTitledBorder("Item"));

            iccBox.setSelected(true);
            iccBox.setText("Characteristic curve");

            itemInfoBox.setText("Information function");

            legendBox.setSelected(true);
            legendBox.setText("Show legend");

            GroupLayout itemPanelLayout = new GroupLayout(itemPanel);
            itemPanel.setLayout(itemPanelLayout);
            itemPanelLayout.setHorizontalGroup(
                itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(iccBox, GroupLayout.PREFERRED_SIZE, 161, GroupLayout.PREFERRED_SIZE)
                .addComponent(itemInfoBox, GroupLayout.PREFERRED_SIZE, 161, GroupLayout.PREFERRED_SIZE)
                .addComponent(legendBox, GroupLayout.PREFERRED_SIZE, 161, GroupLayout.PREFERRED_SIZE)
            );
            itemPanelLayout.setVerticalGroup(
                itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(itemPanelLayout.createSequentialGroup()
                    .addComponent(iccBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(itemInfoBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(legendBox)
                    .addContainerGap())
            );

            personPanel.setBorder(BorderFactory.createTitledBorder("Person"));

            tccBox.setSelected(true);
            tccBox.setText("Characteristic curve");

            personInfoBox.setText("Information function");

            personSeBox.setText("Standard error");

            GroupLayout personPanelLayout = new GroupLayout(personPanel);
            personPanel.setLayout(personPanelLayout);
            personPanelLayout.setHorizontalGroup(
                personPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(personPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(personPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(personInfoBox)
                        .addComponent(personSeBox)
                        .addComponent(tccBox))
                    .addContainerGap(23, Short.MAX_VALUE))
            );
            personPanelLayout.setVerticalGroup(
                personPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(personPanelLayout.createSequentialGroup()
                    .addComponent(tccBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(personInfoBox)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(personSeBox)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            curveTypePanel.setBorder(BorderFactory.createTitledBorder("Curve Type"));

            typeGroup.add(categoryRadioButton);
            categoryRadioButton.setSelected(true);
            categoryRadioButton.setText("Category probability");
            categoryRadioButton.setActionCommand("prob");
            categoryRadioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(categoryRadioButton.isSelected()){
                        responseTableTextField.setEnabled(true);
                        thinTextField.setEnabled(true);
                        selectTableButton.setEnabled(true);
                    }
                }
            });

            typeGroup.add(expectedScoreRadioButton);
            expectedScoreRadioButton.setText("Expected score");
            expectedScoreRadioButton.setActionCommand("expected");
            expectedScoreRadioButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if(expectedScoreRadioButton.isSelected()){
                        responseTableTextField.setEnabled(false);
                        thinTextField.setEnabled(false);
                        selectTableButton.setEnabled(false);
                    }
                }
            });

            GroupLayout curveTypePanelLayout = new GroupLayout(curveTypePanel);
            curveTypePanel.setLayout(curveTypePanelLayout);
            curveTypePanelLayout.setHorizontalGroup(
                curveTypePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(curveTypePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(categoryRadioButton)
                    .addGap(18, 18, 18)
                    .addComponent(expectedScoreRadioButton)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            curveTypePanelLayout.setVerticalGroup(
                curveTypePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(curveTypePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(curveTypePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(categoryRadioButton)
                        .addComponent(expectedScoreRadioButton))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            GroupLayout optionsPanelLayout = new GroupLayout(optionsPanel);
            optionsPanel.setLayout(optionsPanelLayout);
            optionsPanelLayout.setHorizontalGroup(
                optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(optionsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(curveTypePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(optionsPanelLayout.createSequentialGroup()
                            .addComponent(itemPanel, GroupLayout.PREFERRED_SIZE, 164, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(personPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(51, Short.MAX_VALUE))
            );
            optionsPanelLayout.setVerticalGroup(
                optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(optionsPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(itemPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(personPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(curveTypePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(41, Short.MAX_VALUE))
            );

            tabbedPane.addTab("Lines", optionsPanel);

            axisPanel.setBorder(BorderFactory.createTitledBorder("X-axis"));

            minLabel.setText("Min");

            minTextField.setText("-5");
            minTextField.setMaximumSize(new Dimension(50, 28));
            minTextField.setMinimumSize(new Dimension(50, 28));
            minTextField.setPreferredSize(new Dimension(50, 28));

            maxTextField.setText("5");
            maxTextField.setMaximumSize(new Dimension(50, 28));
            maxTextField.setMinimumSize(new Dimension(50, 28));
            maxTextField.setPreferredSize(new Dimension(50, 28));

            maxLabel.setText("Max");

            pointsLabel.setText("Points");

            pointsTextField.setText("31");
            pointsTextField.setMaximumSize(new Dimension(50, 28));
            pointsTextField.setMinimumSize(new Dimension(50, 28));
            pointsTextField.setPreferredSize(new Dimension(50, 28));

            GroupLayout axisPanelLayout = new GroupLayout(axisPanel);
            axisPanel.setLayout(axisPanelLayout);
            axisPanelLayout.setHorizontalGroup(
                axisPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(axisPanelLayout.createSequentialGroup()
                    .addGap(29, 29, 29)
                    .addComponent(minLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(maxLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(pointsLabel)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(pointsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            axisPanelLayout.setVerticalGroup(
                axisPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(axisPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(axisPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(axisPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(pointsLabel)
                            .addComponent(pointsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(axisPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(minLabel)
                            .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxLabel)
                            .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            responsePanel.setBorder(BorderFactory.createTitledBorder("Observed Responses"));

            responseTableLebel.setText("Response table");

            responseTableTextField.setMaximumSize(new Dimension(150, 28));
            responseTableTextField.setMinimumSize(new Dimension(150, 28));
            responseTableTextField.setPreferredSize(new Dimension(150, 28));

            selectTableButton.setText("Select");
            selectTableButton.setMaximumSize(new Dimension(65, 28));
            selectTableButton.setMinimumSize(new Dimension(65, 28));
            selectTableButton.setPreferredSize(new Dimension(65, 28));
            selectTableButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    //TODO show table selection dialog
                    responseTableDialog = new SelectTableDialog(IrtPlotDialog.this, dbName, listModel);
                    responseTableDialog.setVisible(true);
                    if(responseTableDialog.canRun()){
                        responseTableTextField.setText(responseTableDialog.getTableName().toString());
                        hasResponseData = true;
                    }else{
                        hasResponseData = false;
                    }

                }
            });

            thinLabel.setText("Score increment");

            thinTextField.setText("1");
            thinTextField.setMaximumSize(new Dimension(50, 28));
            thinTextField.setMinimumSize(new Dimension(50, 28));
            thinTextField.setPreferredSize(new Dimension(50, 28));

            GroupLayout responsePanelLayout = new GroupLayout(responsePanel);
            responsePanel.setLayout(responsePanelLayout);
            responsePanelLayout.setHorizontalGroup(
                responsePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(responsePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(responsePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addComponent(thinLabel)
                        .addComponent(responseTableLebel))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addGroup(responsePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(responsePanelLayout.createSequentialGroup()
                            .addComponent(responseTableTextField, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(selectTableButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addComponent(thinTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            responsePanelLayout.setVerticalGroup(
                responsePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(responsePanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(responsePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(responseTableLebel)
                        .addComponent(responseTableTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(selectTableButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(responsePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(thinLabel)
                        .addComponent(thinTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            GroupLayout layoutPanelLayout = new GroupLayout(layoutPanel);
            layoutPanel.setLayout(layoutPanelLayout);
            layoutPanelLayout.setHorizontalGroup(
                layoutPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layoutPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layoutPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(responsePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(axisPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap(51, Short.MAX_VALUE))
            );
            layoutPanelLayout.setVerticalGroup(
                layoutPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layoutPanelLayout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(responsePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addComponent(axisPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            tabbedPane.addTab("Points", layoutPanel);

            GroupLayout layout = new GroupLayout(getContentPane());
            getContentPane().setLayout(layout);
            layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE))
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );
            layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE)
                    .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            );

            tabbedPane.getAccessibleContext().setAccessibleName("Lines");

            pack();
    }// </editor-fold>


//    private void initComponents() {
//        GridBagConstraints gridBagConstraints;
//
//        typeGroup = new ButtonGroup();
//        itemPanel = new JPanel();
//        iccBox = new JCheckBox();
//        itemInfoBox = new JCheckBox();
//        legendBox = new JCheckBox();
//        personPanel = new JPanel();
//        tccBox = new JCheckBox();
//        personInfoBox = new JCheckBox();
//        personSeBox = new JCheckBox();
//        typePanel = new JPanel();
//        probTypeButton = new JRadioButton();
//        expectedButton = new JRadioButton();
//        xaxisPanel = new JPanel();
//        xminLabel = new JLabel();
//        xMinText = new JTextField();
//        xMaxLabel = new JLabel();
//        xMaxText = new JTextField();
//        xPointsLabel = new JLabel();
//        pointsText = new JTextField();
//
//        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//        getContentPane().setLayout(new GridBagLayout());
//
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridwidth = 2;
//        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.insets = new Insets(5, 5, 0, 5);
//        getContentPane().add(vsp, gridBagConstraints);
//
//        itemPanel.setBorder(BorderFactory.createTitledBorder("Item"));
//        itemPanel.setLayout(new GridLayout(3, 1, 5, 5));
//
//        iccBox.setSelected(true);
//        iccBox.setText("Characteristic curve");
//        iccBox.setToolTipText("Item characteristic curve");
//        itemPanel.add(iccBox);
//
//        itemInfoBox.setText("Information function");
//        itemInfoBox.setToolTipText("Item information function");
//        itemPanel.add(itemInfoBox);
//
//        legendBox.setSelected(true);
//        legendBox.setText("Show legend");
//        legendBox.setToolTipText("Show legend on plot");
//        itemPanel.add(legendBox);
//
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.insets = new Insets(0, 5, 5, 2);
//        getContentPane().add(itemPanel, gridBagConstraints);
//
//        personPanel.setBorder(BorderFactory.createTitledBorder("Person"));
//        personPanel.setLayout(new GridLayout(3, 1, 5, 5));
//
//        tccBox.setSelected(true);
//        tccBox.setText("Characteristic curve");
//        tccBox.setToolTipText("Test characteristic curve");
//        personPanel.add(tccBox);
//
//        personInfoBox.setText("Information function");
//        personInfoBox.setToolTipText("Test information function");
//        personPanel.add(personInfoBox);
//
//        personSeBox.setText("Standard error");
//        personSeBox.setToolTipText("Person standard error");
//        personPanel.add(personSeBox);
//
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.insets = new Insets(0, 2, 5, 2);
//        getContentPane().add(personPanel, gridBagConstraints);
//
//        typePanel.setBorder(BorderFactory.createTitledBorder("Curve Type"));
//        typePanel.setLayout(new GridLayout(2, 1));
//
//        typeGroup.add(probTypeButton);
//        probTypeButton.setSelected(true);
//        probTypeButton.setText("Category probability");
//        probTypeButton.setToolTipText("Category probability curves");
//        probTypeButton.setActionCommand("prob");
//        typePanel.add(probTypeButton);
//
//        typeGroup.add(expectedButton);
//        expectedButton.setText("Expected score");
//        expectedButton.setToolTipText("Expected score curve");
//        expectedButton.setActionCommand("expected");
//        typePanel.add(expectedButton);
//
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.gridwidth = 1;
//        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
//        getContentPane().add(typePanel, gridBagConstraints);
//
//        xaxisPanel.setBorder(BorderFactory.createTitledBorder("X-axis"));
//        xaxisPanel.setLayout(new GridBagLayout());
//
//        xminLabel.setText("Min");
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
//        xaxisPanel.add(xminLabel, gridBagConstraints);
//        xminLabel.getAccessibleContext().setAccessibleName("Min: ");
//
//        xMinText.setText("-5.0");
//        xMinText.setMinimumSize(new Dimension(50, 25));
//        xMinText.setPreferredSize(new Dimension(50, 25));
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.gridwidth = 3;
//        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
//        xaxisPanel.add(xMinText, gridBagConstraints);
//
//        xMaxLabel.setText("Max");
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
//        xaxisPanel.add(xMaxLabel, gridBagConstraints);
//
//        xMaxText.setText("5.0");
//        xMaxText.setToolTipText("Maximum value");
//        xMaxText.setMinimumSize(new Dimension(50, 25));
//        xMaxText.setPreferredSize(new Dimension(50, 25));
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 1;
//        gridBagConstraints.gridwidth = 3;
//        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
//        xaxisPanel.add(xMaxText, gridBagConstraints);
//
//        xPointsLabel.setText("Points");
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
//        xaxisPanel.add(xPointsLabel, gridBagConstraints);
//
//        pointsText.setText("31");
//        pointsText.setToolTipText("Number of grid points");
//        pointsText.setMinimumSize(new Dimension(50, 25));
//        pointsText.setPreferredSize(new Dimension(50, 25));
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.gridwidth = 3;
//        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
//        xaxisPanel.add(pointsText, gridBagConstraints);
//
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 2;
//        gridBagConstraints.gridwidth = 1;
//        gridBagConstraints.fill = GridBagConstraints.BOTH;
//        gridBagConstraints.weightx = 1.0;
//        gridBagConstraints.insets = new Insets(0, 5, 5, 2);
//        getContentPane().add(xaxisPanel, gridBagConstraints);
//
//        pack();
//    }// </editor-fold>

    public boolean canRun(){
        return canRun;
    }

    public IrtPlotCommand getCommand(){
        return command;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e){
            try{
                command = new IrtPlotCommand();
                VariableAttributes[] vars = vsp.getSelectedVariables();
                if(vsp.hasSelection()){
                    for(VariableAttributes v : vars){
                        command.getFreeOptionList("variables").addValue(v.getName().toString());
                    }

                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", table.toString());

                    command.getSelectAllOption("item").setSelected("icc", iccBox.isSelected());
                    command.getSelectAllOption("item").setSelected("info", itemInfoBox.isSelected());

                    command.getSelectAllOption("person").setSelected("tcc", tccBox.isSelected());
                    command.getSelectAllOption("person").setSelected("info", personInfoBox.isSelected());
                    command.getSelectAllOption("person").setSelected("se", personSeBox.isSelected());


                    command.getSelectOneOption("type").setSelected(typeGroup.getSelection().getActionCommand());

                    command.getSelectAllOption("options").setSelected("legend", legendBox.isSelected());

                    command.getPairedOptionList("xaxis").addValue("min", minTextField.getText().trim());
                    command.getPairedOptionList("xaxis").addValue("max", maxTextField.getText().trim());
                    command.getPairedOptionList("xaxis").addValue("points", pointsTextField.getText().trim());

                    //get output directory
                    if(outputPath!=null && !"".equals(outputPath.trim())){
                        command.getFreeOption("output").add(outputPath.trim());
                    }

                    if(hasResponseData){
                        command.getPairedOptionList("response").addValue("table", responseTableTextField.getText().trim());
                        command.getPairedOptionList("response").addValue("thin", thinTextField.getText().trim());
                    }



                    canRun=true;
                    setVisible(false);
                }else{
                    JOptionPane.showMessageDialog(IrtPlotDialog.this,
                            "You must select variables for the plot",
                            "VariableSelection Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(IrtPlotDialog.this,
                        ex.getMessage(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }//end RunAction



}

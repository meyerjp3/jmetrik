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

package com.itemanalysis.jmetrik.graph.nicc;

import com.itemanalysis.jmetrik.selector.MultipleSelectionXYByGroupPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.ItemType;
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

public class NonparametricCurveDialog extends JDialog {

    // Variables declaration - do not modify
    private JLabel adjustmentLabel;
    private JTextField adjustmentTextField;
    private JRadioButton allOptionsRadioButton;
    private JRadioButton correctAnswerRadioButton;
    private JPanel curveDisplayPanel;
    private JPanel difPanel;
    private ButtonGroup displayButtonGroup;
    private JLabel focalLabel;
    private JTextField focalTextField;
    private JLabel gridPointsLabel;
    private JTextField gridPointsTextField;
    private JPanel kernelPanel;
    private JComboBox kernelTypeComboBox;
    private JLabel kernelTypeLabel;
    private JLabel referenceLabel;
    private JTextField referenceTextField;
    // End of variables declaration

    private MultipleSelectionXYByGroupPanel vsp = null;
    private boolean canRun = false;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private NonparametricCurveCommand command = null;
    private String[] kernelNames = {"Gaussian", "Epanechnikov", "Uniform", "Triangle", "Biweight", "Triweight", "Cosine"};
    private String kernelType = kernelNames[0];
    private JFileChooser outputLocationChooser = null;
    private String outputPath = "";
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public NonparametricCurveDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList<VariableAttributes> variables) {
        super(parent, "Nonparametric Characteristic Curves", true);
        this.dbName = dbName;
        this.tableName = tableName;

        vsp = new MultipleSelectionXYByGroupPanel();

        //filter out continuous items and non items
//        VariableType filterType1 = new VariableType(ItemType.NOT_ITEM, DataType.STRING);
//        VariableType filterType2 = new VariableType(ItemType.NOT_ITEM, DataType.DOUBLE);
//        VariableType filterType3 = new VariableType(ItemType.CONTINUOUS_ITEM, DataType.STRING);
//        VariableType filterType4 = new VariableType(ItemType.CONTINUOUS_ITEM, DataType.DOUBLE);
//        vsp.addSelectedFilterType(filterType1);
//        vsp.addSelectedFilterType(filterType2);
//        vsp.addSelectedFilterType(filterType3);
//        vsp.addSelectedFilterType(filterType4);
//        vsp.addGroupByFilterType(filterType3);
//        vsp.addGroupByFilterType(filterType4);

        vsp.addSelectedFilterItemType(ItemType.NOT_ITEM);
        vsp.addSelectedFilterItemType(ItemType.CONTINUOUS_ITEM);
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

        JButton b3 = vsp.getButton3();
        b3.setText("Save");
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (outputLocationChooser == null) outputLocationChooser = new JFileChooser();
                outputLocationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                outputLocationChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                outputLocationChooser.setDialogTitle("Select Location");
                if (outputLocationChooser.showDialog(NonparametricCurveDialog.this, "OK") != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File f = outputLocationChooser.getSelectedFile();
                outputPath = f.getAbsolutePath().replaceAll("\\\\", "/");

            }
        });

        JButton b4 = vsp.getButton4();
        b4.setText("Reset");
        b4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vsp.reset();
            }
        });


        initComponents();

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setResizable(false);

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

    }

    private void initComponents() {

        displayButtonGroup = new ButtonGroup();
        kernelPanel = new JPanel();
        kernelTypeLabel = new JLabel();
        kernelTypeComboBox = new JComboBox();
        adjustmentLabel = new JLabel();
        adjustmentTextField = new JTextField();
        gridPointsLabel = new JLabel();
        gridPointsTextField = new JTextField();
        curveDisplayPanel = new JPanel();
        allOptionsRadioButton = new JRadioButton();
        correctAnswerRadioButton = new JRadioButton();
        difPanel = new JPanel();
        focalLabel = new JLabel();
        referenceLabel = new JLabel();
        focalTextField = new JTextField();
        referenceTextField = new JTextField();

        kernelPanel.setBorder(BorderFactory.createTitledBorder("Kernel Options"));

        kernelTypeLabel.setText("Kernel type");

        kernelTypeComboBox.setMaximumSize(new Dimension(120, 28));
        kernelTypeComboBox.setMinimumSize(new Dimension(120, 25));
        kernelTypeComboBox.setPreferredSize(new Dimension(120, 25));
        kernelTypeComboBox.setModel(new DefaultComboBoxModel(kernelNames));
        kernelTypeComboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                JComboBox cb = (JComboBox)e.getSource();
                kernelType = (String)cb.getSelectedItem();
            }
        });

        adjustmentLabel.setText("Bandwidth adjustment");

        adjustmentTextField.setText("1");
        adjustmentTextField.setMaximumSize(new Dimension(50, 28));
        adjustmentTextField.setMinimumSize(new Dimension(50, 28));
        adjustmentTextField.setPreferredSize(new Dimension(50, 28));

        gridPointsLabel.setText("Grid points");

        gridPointsTextField.setText("51");
        gridPointsTextField.setMaximumSize(new Dimension(50, 28));
        gridPointsTextField.setMinimumSize(new Dimension(50, 28));
        gridPointsTextField.setPreferredSize(new Dimension(50, 28));

        GroupLayout kernelPanelLayout = new GroupLayout(kernelPanel);
        kernelPanel.setLayout(kernelPanelLayout);
        kernelPanelLayout.setHorizontalGroup(
            kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(kernelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(kernelPanelLayout.createSequentialGroup()
                        .addComponent(adjustmentLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(adjustmentTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(kernelPanelLayout.createSequentialGroup()
                        .addComponent(kernelTypeLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(kernelTypeComboBox, GroupLayout.PREFERRED_SIZE, 112, GroupLayout.PREFERRED_SIZE)))
                .addGap(12, 12, 12)
                .addComponent(gridPointsLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridPointsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        kernelPanelLayout.setVerticalGroup(
            kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(kernelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(kernelTypeLabel)
                    .addComponent(kernelTypeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(adjustmentLabel)
                    .addGroup(kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(gridPointsLabel)
                        .addComponent(gridPointsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(adjustmentTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        curveDisplayPanel.setBorder(BorderFactory.createTitledBorder("Display Curves for"));

        displayButtonGroup.add(allOptionsRadioButton);
        allOptionsRadioButton.setText("All options");
        allOptionsRadioButton.setActionCommand("all");

        displayButtonGroup.add(correctAnswerRadioButton);
        correctAnswerRadioButton.setText("Correct answer");
        correctAnswerRadioButton.setActionCommand("expected");
        correctAnswerRadioButton.setSelected(true);

        GroupLayout curveDisplayPanelLayout = new GroupLayout(curveDisplayPanel);
        curveDisplayPanel.setLayout(curveDisplayPanelLayout);
        curveDisplayPanelLayout.setHorizontalGroup(
            curveDisplayPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(curveDisplayPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(curveDisplayPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(allOptionsRadioButton)
                    .addComponent(correctAnswerRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        curveDisplayPanelLayout.setVerticalGroup(
            curveDisplayPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(curveDisplayPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(allOptionsRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(correctAnswerRadioButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        difPanel.setBorder(BorderFactory.createTitledBorder("DIF Group Codes"));

        focalLabel.setText("Focal");

        referenceLabel.setText("Reference");

        focalTextField.setMaximumSize(new Dimension(50, 28));
        focalTextField.setMinimumSize(new Dimension(50, 28));
        focalTextField.setPreferredSize(new Dimension(50, 28));

        referenceTextField.setMaximumSize(new Dimension(50, 28));
        referenceTextField.setMinimumSize(new Dimension(50, 28));
        referenceTextField.setPreferredSize(new Dimension(50, 28));

        GroupLayout difPanelLayout = new GroupLayout(difPanel);
        difPanel.setLayout(difPanelLayout);
        difPanelLayout.setHorizontalGroup(
            difPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(difPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(difPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(difPanelLayout.createSequentialGroup()
                        .addComponent(focalLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(focalTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(difPanelLayout.createSequentialGroup()
                        .addComponent(referenceLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(referenceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        difPanelLayout.setVerticalGroup(
            difPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(difPanelLayout.createSequentialGroup()
                .addGroup(difPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(focalLabel)
                    .addComponent(focalTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(difPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(referenceLabel)
                    .addComponent(referenceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(vsp, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                        .addGroup(layout.createSequentialGroup()
                            .addComponent(difPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(curveDisplayPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addComponent(kernelPanel, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(kernelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(curveDisplayPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(difPanel, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

//    private void initComponents() {
//
//        displayCurveButtonGroup = new ButtonGroup();
//        difCodesPanel = new JPanel();
//        focalLabel = new JLabel();
//        referenceTextField = new JTextField();
//        referenceLabel = new JLabel();
//        focalTextField = new JTextField();
//        curveDisplayPanel = new JPanel();
//        allOptionsRadioButton = new JRadioButton();
//        correctAnswerRadioButton = new JRadioButton();
//        kernelPanel = new JPanel();
//        bandwidthLabel = new JLabel();
//        bandwidthTextField = new JTextField();
//        gridPointsLabel = new JLabel();
//        gridPointsTextField = new JTextField();
//        kernelTypeLabel = new JLabel();
//        kernelTypeComboBox = new JComboBox();
//
//
//        chartSizePanel = new JPanel();
//        widthLabel = new JLabel();
//        widthTextField = new JTextField();
//        heightLabel = new JLabel();
//        heightTextField = new JTextField();
//
//        difCodesPanel.setBorder(BorderFactory.createTitledBorder("DIF Group Codes"));
//
//        focalLabel.setText("Focal");
//
//        referenceTextField.setPreferredSize(new Dimension(75, 28));
//
//        referenceLabel.setText("Reference");
//
//        focalTextField.setPreferredSize(new Dimension(75, 28));
//
//
//
//        curveDisplayPanel.setBorder(BorderFactory.createTitledBorder("Display Curves for"));
//
//        displayCurveButtonGroup.addArgument(allOptionsRadioButton);
//        allOptionsRadioButton.setText("All Options");
//        allOptionsRadioButton.setActionCommand("all");
//
//        displayCurveButtonGroup.addArgument(correctAnswerRadioButton);
//        correctAnswerRadioButton.setSelected(true);
//        correctAnswerRadioButton.setText("Correct Answer");
//        correctAnswerRadioButton.setActionCommand("expected");
//
//        GroupLayout curveDisplayPanelLayout = new GroupLayout(curveDisplayPanel);
//        curveDisplayPanel.setLayout(curveDisplayPanelLayout);
//        curveDisplayPanelLayout.setHorizontalGroup(
//                curveDisplayPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(curveDisplayPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(curveDisplayPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addComponent(allOptionsRadioButton)
//                                        .addComponent(correctAnswerRadioButton))
//                                .addContainerGap(23, Short.MAX_VALUE))
//        );
//        curveDisplayPanelLayout.setVerticalGroup(
//                curveDisplayPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(curveDisplayPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addComponent(allOptionsRadioButton)
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                .addComponent(correctAnswerRadioButton)
//                                .addContainerGap(52, Short.MAX_VALUE))
//        );
//
//        kernelPanel.setBorder(BorderFactory.createTitledBorder("Kernel Options"));
//
//        bandwidthLabel.setText("Bandwidth");
//
//        bandwidthTextField.setText("1");
//        bandwidthTextField.setMaximumSize(new Dimension(75, 28));
//        bandwidthTextField.setMinimumSize(new Dimension(75, 28));
//        bandwidthTextField.setPreferredSize(new Dimension(75, 28));
//
//        gridPointsLabel.setText("Grid Points");
//
//        gridPointsTextField.setText("51");
//        gridPointsTextField.setMaximumSize(new Dimension(75, 28));
//        gridPointsTextField.setMinimumSize(new Dimension(75, 28));
//        gridPointsTextField.setPreferredSize(new Dimension(75, 28));
//
//        kernelTypeLabel.setText("Kernel Type");
//
//        kernelTypeComboBox.setModel(new DefaultComboBoxModel(kernelNames));
//        kernelTypeComboBox.addActionListener(new ActionListener(){
//            public void actionPerformed(ActionEvent e){
//                JComboBox cb = (JComboBox)e.getSource();
//                kernelType = (String)cb.getSelectedItem();
//            }
//        });
//
//        GroupLayout kernelPanelLayout = new GroupLayout(kernelPanel);
//        kernelPanel.setLayout(kernelPanelLayout);
//        kernelPanelLayout.setHorizontalGroup(
//                kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(kernelPanelLayout.createSequentialGroup()
//                                .addGroup(kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addGroup(kernelPanelLayout.createSequentialGroup()
//                                                .addContainerGap()
//                                                .addComponent(kernelTypeLabel)
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addComponent(kernelTypeComboBox, GroupLayout.PREFERRED_SIZE, 123, GroupLayout.PREFERRED_SIZE))
//                                        .addGroup(kernelPanelLayout.createSequentialGroup()
//                                                .addGap(17, 17, 17)
//                                                .addGroup(kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                                                        .addGroup(kernelPanelLayout.createSequentialGroup()
//                                                                .addComponent(gridPointsLabel)
//                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                                .addComponent(gridPointsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                                        .addGroup(kernelPanelLayout.createSequentialGroup()
//                                                                .addComponent(bandwidthLabel)
//                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                                .addComponent(bandwidthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
//                                .addContainerGap())
//        );
//        kernelPanelLayout.setVerticalGroup(
//                kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(kernelPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(kernelTypeLabel)
//                                        .addComponent(kernelTypeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addGap(10, 10, 10)
//                                .addGroup(kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(bandwidthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addComponent(bandwidthLabel))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addGroup(kernelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(gridPointsLabel)
//                                        .addComponent(gridPointsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addContainerGap(5, Short.MAX_VALUE))
//        );
//
//        GroupLayout difCodesPanelLayout = new GroupLayout(difCodesPanel);
//        difCodesPanel.setLayout(difCodesPanelLayout);
//        difCodesPanelLayout.setHorizontalGroup(
//                difCodesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(difCodesPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(difCodesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addGroup(GroupLayout.Alignment.TRAILING, difCodesPanelLayout.createSequentialGroup()
//                                                .addComponent(focalLabel)
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addComponent(focalTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                        .addGroup(difCodesPanelLayout.createSequentialGroup()
//                                                .addComponent(referenceLabel)
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addComponent(referenceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                                .addContainerGap(65, Short.MAX_VALUE))
//        );
//        difCodesPanelLayout.setVerticalGroup(
//                difCodesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(difCodesPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(difCodesPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(focalTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addComponent(focalLabel))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addGroup(difCodesPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(referenceLabel)
//                                        .addComponent(referenceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        chartSizePanel.setBorder(BorderFactory.createTitledBorder("Chart Size"));
//
//        widthLabel.setText("Width");
//
//        widthTextField.setText("450");
//        widthTextField.setPreferredSize(new Dimension(75, 28));
//
//        heightLabel.setText("Height");
//
//        heightTextField.setText("400");
//        heightTextField.setPreferredSize(new Dimension(75, 28));
//
//        GroupLayout chartSizePanelLayout = new GroupLayout(chartSizePanel);
//        chartSizePanel.setLayout(chartSizePanelLayout);
//        chartSizePanelLayout.setHorizontalGroup(
//                chartSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(chartSizePanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(chartSizePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                                        .addComponent(widthLabel)
//                                        .addComponent(heightLabel))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addGroup(chartSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addComponent(widthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addComponent(heightTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        chartSizePanelLayout.setVerticalGroup(
//                chartSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(chartSizePanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(chartSizePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(widthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addComponent(widthLabel))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addGroup(chartSizePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(heightTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addComponent(heightLabel))
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        GroupLayout layout = new GroupLayout(getContentPane());
//        getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(
//                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(layout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addGroup(layout.createSequentialGroup()
//                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
//                                                        .addComponent(kernelPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                                        .addComponent(difCodesPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
//                                                        .addComponent(curveDisplayPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                                        .addComponent(chartSizePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
//                                .addContainerGap())
//        );
//        layout.setVerticalGroup(
//                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(layout.createSequentialGroup()
//                                .addContainerGap()
//                                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addComponent(curveDisplayPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                        .addComponent(kernelPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addComponent(difCodesPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addComponent(chartSizePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addContainerGap())
//        );
//
//        pack();
//    }// </editor-fold>


    public boolean canRun(){
        return canRun;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public NonparametricCurveCommand getCommand(){
        return command;
    }

    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){

            if(vsp.selectionMade() && vsp.hasIndependentVariable()){
                command = new NonparametricCurveCommand();

                //get variables
                VariableAttributes[] variables = vsp.getSelectedVariables();
                for(VariableAttributes v : variables){
                    command.getFreeOptionList("variables").addValue(v.getName().toString());
                }

                //get predictor variable
                VariableAttributes regressorVariable = vsp.getIndependentVariable();
                command.getFreeOption("xvar").add(regressorVariable.getName().toString());

                //get database and table
                command.getPairedOptionList("data").addValue("db", dbName.toString());
                command.getPairedOptionList("data").addValue("table", tableName.toString());

                //get type of kernel
                command.getSelectOneOption("kernel").setSelected(kernelType.toLowerCase());

                //get type of curves
                command.getSelectOneOption("curves").setSelected(displayButtonGroup.getSelection().getActionCommand());

                //get gridpoints for kernel regression
                String gp = gridPointsTextField.getText();
                if(!gp.equals("")){
                    command.getFreeOption("gridpoints").add(Integer.valueOf(gp));
                }

                //get bandwidth adjustment
                String bw = adjustmentTextField.getText();
                if(!bw.equals("")){
                    command.getFreeOption("adjust").add(Double.valueOf(bw));
                }

                //get output directory
                if(outputPath!=null && !"".equals(outputPath.trim())){
                    command.getFreeOption("output").add(outputPath.trim());
                }

                canRun = true;

                if(vsp.hasGroupingVariable()){
                    if(referenceTextField.getText().equals("") || focalTextField.getText().equals("")){
                        JOptionPane.showMessageDialog(NonparametricCurveDialog.this,
                                "You must specify a Focal Code and a Reference Code",
                                "No Group Codes Found",
                                JOptionPane.ERROR_MESSAGE);
                        canRun=false;
                    }else{
                        command.getPairedOptionList("codes").addValue("focal", focalTextField.getText().trim());
                        command.getPairedOptionList("codes").addValue("reference", referenceTextField.getText().trim());
                        VariableAttributes groupVar = vsp.getGroupByVariable();
                        command.getFreeOption("groupvar").add(groupVar.getName().toString());

                        canRun = true;
                    }
                }
                setVisible(false);
                outputPath = "";

            }

        }

    }//end RunAction


}

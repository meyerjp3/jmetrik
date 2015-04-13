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

package com.itemanalysis.jmetrik.stats.irt.linking;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.dao.DerbyDatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.gui.SelectTableDialog;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

public class IrtLinkingDialog extends JDialog {

    // Variables declaration - do not modify
    private JRadioButton biasedRadioButton;
    private JLabel binLabel;
    private JTextField binsTextField;
    private JButton cancelButton;
    private JRadioButton cgRadioButton;
    private JLabel convergeLabel;
    private JTextField convergeTextField;
    private ButtonGroup criterionButtonGroup;
    private JPanel criterionPanel;
    private ButtonGroup distributionButtonGroup;
    private JRadioButton formXCriterionRadioButton;
    private JButton formXItemButton;
    private JButton formXPersonButton;
    private JRadioButton formYCriterionRadioButton;
    private JButton formYItemButton;
    private JButton formYPersonButton;
    private JRadioButton haebaraRadioButton;
    private JRadioButton histogramRadioButton;
    private JPanel itemParameterPanel;
    private JCheckBox itemTransformCheckBox;
//    private JPanel equatingPanel;
    private JRadioButton bobyqaRadioButton;
    private JRadioButton logisticRadioButton;
    private JLabel maxLabel1;
    private JLabel maxLabel2;
    private JTextField maxTextField1;
    private JTextField maxTextField2;
    private JLabel meanLabel;
    private JTextField meanTextField;
    private JPanel methodPanel;
    private JLabel min1Label;
    private JLabel minLabel2;
    private JTextField minTextField1;
    private JTextField minTextField2;
    private JRadioButton mmRadioButton;
    private JRadioButton msRadioButton;
    private JLabel normalBinLabel;
    private JRadioButton normalDistributionRadioButton;
    private JRadioButton normalRadioButton;
    private JLabel noteLabel;
    private JRadioButton observedValuesRadioButton;
    private JButton okButton;
    private ButtonGroup optimizationGroup;
    private JPanel optimizationPanel;
    private JPanel parameterPanel;
    private JPanel personDistributionPanel;
    private JPanel personParameterPanel;
    private JCheckBox personTransformCheckBox;
    private JLabel pointLabel2;
    private JTextField pointsTextField1;
    private JTextField pointsTextField2;
    private JLabel precisionLabel;
    private JTextField precisionTextField;
    private JButton resetButton;
    private ButtonGroup scaleButtonGroup;
    private JPanel scalePanel;
    private ButtonGroup sdButtonGroup;
    private JLabel sdLabel;
    private JPanel sdPanel;
    private JTextField sdTextField;
    private JRadioButton slRadioButton;
    private JTabbedPane tabbedPane;
    private ButtonGroup transformButtonGroup;
    private JPanel transformPanel;
    private JPanel transformationPanel;
    private JRadioButton unbiasedRadioButton;
    private JRadioButton uniformRadioButton;
    private JRadioButton xyCriterionRadioButton;
    private JButton xyItemButton;
    // End of variables declaration

    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private boolean canRun = false;
    private IrtLinkingItemPairDialog itemPairDialog = null;
    private SelectTableDialog itemDialogX = null;
    private SelectTableDialog itemDialogY = null;
    private IrtLinkingThetaDialog thetaDialogX = null;
    private IrtLinkingThetaDialog thetaDialogY = null;
    private IrtLinkingCommand command = null;
    private DataTableName tableX = null;
    private DataTableName tableY = null;
    private DataTableName tableXtheta = null;
    private DataTableName tableYtheta = null;
    private static String FORMX = "Form X";
    private static String FORMY = "Form Y";
//    private boolean transformItems = false;
//    private boolean transformPersons = false;
    private DatabaseName dbName = null;
    private SortedListModel<DataTableName> tableListModel;
    static Logger logger = Logger.getLogger("jmetrik-logger");


    public IrtLinkingDialog(JFrame parent, Connection conn, DatabaseName dbName, SortedListModel<DataTableName> tableListModel){
        super(parent, "IRT Scale Linking", true);
        this.conn = conn;
        this.dbName = dbName;
        this.tableListModel = tableListModel;

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);

        //get type of database according to properties
        JmetrikPreferencesManager preferencesManager = new JmetrikPreferencesManager();
        String dbType = preferencesManager.getDatabaseType();
        JmetrikDatabaseFactory dbFactory;

        if(DatabaseType.APACHE_DERBY.toString().equals(dbType)){
            dao = new DerbyDatabaseAccessObject();
            dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
        }else if(DatabaseType.MYSQL.toString().equals(dbType)){
            //not yet implemented
        }else{
            //default is apache derby
            dao = new DerbyDatabaseAccessObject();
            dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
        }

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });


    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        distributionButtonGroup = new ButtonGroup();
        scaleButtonGroup = new ButtonGroup();
        sdButtonGroup = new ButtonGroup();
        transformButtonGroup = new ButtonGroup();
        optimizationGroup = new ButtonGroup();
        criterionButtonGroup = new ButtonGroup();
        tabbedPane = new JTabbedPane();
        parameterPanel = new JPanel();
        itemParameterPanel = new JPanel();
        formXItemButton = new JButton();
//        formXItemButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if(itemDialogX==null){
//                    itemDialogX = new IrtEquatingItemDialog(IrtEquatingDialog.this, conn, dao, tableListModel, FORMX);
//                }
//                itemDialogX.setVisible(true);
//
//                if(itemDialogX.canRun()){
//                    tableX = itemDialogX.getTableName();
//                }
//            }
//        });
        formXItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(itemDialogX==null){
                    itemDialogX = new SelectTableDialog(IrtLinkingDialog.this, dbName, tableListModel);
                    itemDialogX.setTitle("Form X Item Table");
                }
                itemDialogX.setVisible(true);

                if(itemDialogX.canRun()){
                    tableX = itemDialogX.getSelectedTable();
                }
            }
        });

        formYItemButton = new JButton();
//        formYItemButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if(itemDialogY==null){
//                    itemDialogY = new IrtEquatingItemDialog(IrtEquatingDialog.this, conn, dao, tableListModel, FORMY);
//                }
//                itemDialogY.setVisible(true);
//                if(itemDialogY.canRun()){
//                    tableY = itemDialogY.getTableName();
//                }
//            }
//        });
        formYItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(itemDialogY==null){
                    itemDialogY = new SelectTableDialog(IrtLinkingDialog.this, dbName, tableListModel);
                    itemDialogY.setTitle("Form Y Item Table");
                }
                itemDialogY.setVisible(true);

                if(itemDialogY.canRun()){
                    tableY = itemDialogY.getSelectedTable();
                }
            }
        });


        xyItemButton = new JButton();
        xyItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(itemDialogX!=null && itemDialogY!=null){
                    if(!itemDialogX.canRun() && !itemDialogY.canRun()){
                        JOptionPane.showMessageDialog(IrtLinkingDialog.this,
                                "You must select an item table for Form X and Form Y.",
                                "Insufficient Item Data",
                                JOptionPane.ERROR_MESSAGE);
                    }else{

                        try{
                            //check to see if selected tables are item parameter tables
                            isItemParameterTable(tableX);
                            isItemParameterTable(tableY);

                            //if table are item parameter tables, then start item pair dialog
                            if(itemPairDialog==null){
                                itemPairDialog = new IrtLinkingItemPairDialog(IrtLinkingDialog.this, conn, tableX, tableY);
                            }
                            itemPairDialog.setVisible(true);
                            if(itemPairDialog.canRun()){
                                //nothing here. Dialog is only building syntax. Not returning anything here.
                            }


                        }catch(SQLException ex){
                            logger.fatal(ex.getMessage(), ex);
                            JOptionPane.showMessageDialog(IrtLinkingDialog.this,
                                    "Selected table is probably not a table of item parameters.\n" +
                                            "Select another table.",
                                    "SQL Exception",
                                    JOptionPane.ERROR_MESSAGE);
                        }

                    }//end if
                }
            }
        });

        personDistributionPanel = new JPanel();
        observedValuesRadioButton = new JRadioButton();
        observedValuesRadioButton.setActionCommand("observed");
        observedValuesRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formXPersonButton.setEnabled(true);
                formYPersonButton.setEnabled(true);

                binsTextField.setEnabled(false);

                pointsTextField2.setEnabled(false);
                minTextField2.setEnabled(false);
                maxTextField2.setEnabled(false);

                pointsTextField1.setEnabled(false);
                minTextField1.setEnabled(false);
                maxTextField1.setEnabled(false);
                meanTextField.setEnabled(false);
                sdTextField.setEnabled(false);

            }
        });

        histogramRadioButton = new JRadioButton();
        histogramRadioButton.setActionCommand("histogram");
        histogramRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formXPersonButton.setEnabled(true);
                formYPersonButton.setEnabled(true);

                binsTextField.setEnabled(true);

                pointsTextField2.setEnabled(false);
                minTextField2.setEnabled(false);
                maxTextField2.setEnabled(false);

                pointsTextField1.setEnabled(false);
                minTextField1.setEnabled(false);
                maxTextField1.setEnabled(false);
                meanTextField.setEnabled(false);
                sdTextField.setEnabled(false);

            }
        });

        binLabel = new JLabel();
        binsTextField = new JTextField();
        normalDistributionRadioButton = new JRadioButton();
        normalDistributionRadioButton.setActionCommand("normal");
        normalDistributionRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formXPersonButton.setEnabled(false);
                formYPersonButton.setEnabled(false);

                binsTextField.setEnabled(false);

                pointsTextField2.setEnabled(false);
                minTextField2.setEnabled(false);
                maxTextField2.setEnabled(false);

                pointsTextField1.setEnabled(true);
                minTextField1.setEnabled(true);
                maxTextField1.setEnabled(true);
                meanTextField.setEnabled(true);
                sdTextField.setEnabled(true);

            }
        });

        meanLabel = new JLabel();
        meanTextField = new JTextField();
        min1Label = new JLabel();
        minTextField1 = new JTextField();
        maxLabel1 = new JLabel();
        normalBinLabel = new JLabel();
        pointsTextField1 = new JTextField();
        uniformRadioButton = new JRadioButton();
        uniformRadioButton.setActionCommand("uniform");
        uniformRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                formXPersonButton.setEnabled(false);
                formYPersonButton.setEnabled(false);

                binsTextField.setEnabled(false);

                pointsTextField2.setEnabled(true);
                minTextField2.setEnabled(true);
                maxTextField2.setEnabled(true);

                pointsTextField1.setEnabled(false);
                minTextField1.setEnabled(false);
                maxTextField1.setEnabled(false);
                meanTextField.setEnabled(false);
                sdTextField.setEnabled(false);

            }
        });

        pointLabel2 = new JLabel();
        pointsTextField2 = new JTextField();
        minLabel2 = new JLabel();
        minTextField2 = new JTextField();
        maxTextField1 = new JTextField();
        sdLabel = new JLabel();
        sdTextField = new JTextField();
        maxLabel2 = new JLabel();
        maxTextField2 = new JTextField();


        //initial radio button settings for the personDistributionPanel
        binsTextField.setEnabled(false);
        pointsTextField2.setEnabled(true);
        minTextField2.setEnabled(true);
        maxTextField2.setEnabled(true);
        pointsTextField1.setEnabled(false);
        minTextField1.setEnabled(false);
        maxTextField1.setEnabled(false);
        meanTextField.setEnabled(false);
        sdTextField.setEnabled(false);
        //end initial radio button values

        scalePanel = new JPanel();
        logisticRadioButton = new JRadioButton();
        logisticRadioButton.setActionCommand("logistic");
        scaleButtonGroup.add(logisticRadioButton);

        normalRadioButton = new JRadioButton();
        normalRadioButton.setActionCommand("normal");
        scaleButtonGroup.add(normalRadioButton);

        personParameterPanel = new JPanel();
        formXPersonButton = new JButton();
        formXPersonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(thetaDialogX==null){
                    thetaDialogX = new IrtLinkingThetaDialog(IrtLinkingDialog.this, conn, dao, tableListModel, FORMX);
                }
                thetaDialogX.setVisible(true);
            }
        });
        formXPersonButton.setEnabled(false);

        formYPersonButton = new JButton();
        formYPersonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(thetaDialogY==null){
                    thetaDialogY = new IrtLinkingThetaDialog(IrtLinkingDialog.this, conn, dao, tableListModel, FORMY);
                }
                thetaDialogY.setVisible(true);
            }
        });
        formYPersonButton.setEnabled(false);

        transformationPanel = new JPanel();
        methodPanel = new JPanel();
        msRadioButton = new JRadioButton();
        msRadioButton.setActionCommand("ms");

        mmRadioButton = new JRadioButton();
        mmRadioButton.setActionCommand("mm");

        haebaraRadioButton = new JRadioButton();
        haebaraRadioButton.setActionCommand("hb");

        slRadioButton = new JRadioButton();
        slRadioButton.setActionCommand("sl");

        transformPanel = new JPanel();
        itemTransformCheckBox = new JCheckBox();
        itemTransformCheckBox.setActionCommand("items");

        personTransformCheckBox = new JCheckBox();
        personTransformCheckBox.setActionCommand("persons");

        precisionLabel = new JLabel();
        precisionTextField = new JTextField();
        optimizationPanel = new JPanel();
        cgRadioButton = new JRadioButton();
        cgRadioButton.setActionCommand("cg");

        bobyqaRadioButton = new JRadioButton();
        bobyqaRadioButton.setActionCommand("lbfgs");

        convergeLabel = new JLabel();
        convergeTextField = new JTextField();
        criterionPanel = new JPanel();
        formXCriterionRadioButton = new JRadioButton();
        formXCriterionRadioButton.setActionCommand("x");


        formYCriterionRadioButton = new JRadioButton();
        formYCriterionRadioButton.setActionCommand("y");

        xyCriterionRadioButton = new JRadioButton();
        xyCriterionRadioButton.setActionCommand("xy");

        sdPanel = new JPanel();
        biasedRadioButton = new JRadioButton();
        biasedRadioButton.setActionCommand("biased");

        unbiasedRadioButton = new JRadioButton();
        unbiasedRadioButton.setActionCommand("unbiased");

        noteLabel = new JLabel();
//        equatingPanel = new JPanel();
        okButton = new JButton();
        okButton.setText("Run");
        okButton.addActionListener(new OkActionListener());
        okButton.setMaximumSize(new Dimension(69, 28));
        okButton.setMinimumSize(new Dimension(69, 28));
        okButton.setPreferredSize(new Dimension(69, 28));

        cancelButton = new JButton();
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun=false;
                setVisible(false);
            }
        });
        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new Dimension(69, 28));
        cancelButton.setMinimumSize(new Dimension(69, 28));
        cancelButton.setPreferredSize(new Dimension(69, 28));

        resetButton = new JButton();
        resetButton.setText("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetDialogs();
            }
        });
        resetButton.setMaximumSize(new Dimension(69, 28));
        resetButton.setMinimumSize(new Dimension(69, 28));
        resetButton.setPreferredSize(new Dimension(69, 28));

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        parameterPanel.setPreferredSize(new Dimension(445, 550));

        itemParameterPanel.setBorder(BorderFactory.createTitledBorder("Item Parameters"));
        itemParameterPanel.setPreferredSize(new Dimension(436, 171));

        formXItemButton.setText("Select Form X");

        formYItemButton.setText("Select Form Y");

        xyItemButton.setText("Select XY Pairs");

        GroupLayout itemParameterPanelLayout = new GroupLayout(itemParameterPanel);
        itemParameterPanel.setLayout(itemParameterPanelLayout);
        itemParameterPanelLayout.setHorizontalGroup(
                itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(itemParameterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(formXItemButton, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(formYItemButton, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(xyItemButton, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(24, Short.MAX_VALUE))
        );
        itemParameterPanelLayout.setVerticalGroup(
                itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(itemParameterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(formXItemButton)
                                .addGap(5, 5, 5)
                                .addComponent(formYItemButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(xyItemButton)
                                .addContainerGap(35, Short.MAX_VALUE))
        );

        personDistributionPanel.setBorder(BorderFactory.createTitledBorder("Person Distribution"));

        distributionButtonGroup.add(observedValuesRadioButton);
        observedValuesRadioButton.setText("Points and weights");

        distributionButtonGroup.add(histogramRadioButton);
        histogramRadioButton.setText("Histogram");

        binLabel.setText("Bins");

        binsTextField.setText("");

        distributionButtonGroup.add(normalDistributionRadioButton);
        normalDistributionRadioButton.setText("Normal");

        meanLabel.setText("Mean");

        meanTextField.setText("0");

        min1Label.setText("Min");

        minTextField1.setText("-4");

        maxLabel1.setText("Max");

        normalBinLabel.setText("Points");

        pointsTextField1.setText("25");

        distributionButtonGroup.add(uniformRadioButton);
        uniformRadioButton.setSelected(true);
        uniformRadioButton.setText("Uniform");

        pointLabel2.setText("Points");

        pointsTextField2.setText("25");

        minLabel2.setText("Min");

        minTextField2.setText("-4");

        maxTextField1.setText("4");

        sdLabel.setText("SD");

        sdTextField.setText("1");

        maxLabel2.setText("Max");

        maxTextField2.setText("4");

        GroupLayout personDistributionPanelLayout = new GroupLayout(personDistributionPanel);
        personDistributionPanel.setLayout(personDistributionPanelLayout);
        personDistributionPanelLayout.setHorizontalGroup(
                personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(personDistributionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(observedValuesRadioButton)
                                        .addGroup(personDistributionPanelLayout.createSequentialGroup()
                                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                        .addGroup(personDistributionPanelLayout.createSequentialGroup()
                                                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                                        .addGroup(GroupLayout.Alignment.LEADING, personDistributionPanelLayout.createSequentialGroup()
                                                                                .addComponent(normalDistributionRadioButton)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 35, Short.MAX_VALUE)
                                                                                .addComponent(normalBinLabel))
                                                                        .addGroup(GroupLayout.Alignment.LEADING, personDistributionPanelLayout.createSequentialGroup()
                                                                                .addComponent(uniformRadioButton)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                                                                                .addComponent(pointLabel2))
                                                                        .addGroup(GroupLayout.Alignment.LEADING, personDistributionPanelLayout.createSequentialGroup()
                                                                                .addComponent(histogramRadioButton)
                                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 31, Short.MAX_VALUE)
                                                                                .addComponent(binLabel)))
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(pointsTextField2)
                                                                        .addComponent(binsTextField, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 47, Short.MAX_VALUE)
                                                                        .addComponent(pointsTextField1, GroupLayout.Alignment.TRAILING))
                                                                .addGap(25, 25, 25)
                                                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                                        .addComponent(meanLabel)
                                                                        .addComponent(minLabel2)))
                                                        .addComponent(min1Label))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(minTextField2)
                                                        .addComponent(meanTextField)
                                                        .addComponent(minTextField1, GroupLayout.DEFAULT_SIZE, 49, Short.MAX_VALUE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, 14, Short.MAX_VALUE)
                                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                                        .addGroup(personDistributionPanelLayout.createSequentialGroup()
                                                                .addGap(12, 12, 12)
                                                                .addComponent(maxLabel1)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(maxTextField1, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(personDistributionPanelLayout.createSequentialGroup()
                                                                .addComponent(sdLabel)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(sdTextField, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE))
                                                        .addGroup(personDistributionPanelLayout.createSequentialGroup()
                                                                .addComponent(maxLabel2)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(maxTextField2, GroupLayout.PREFERRED_SIZE, 47, GroupLayout.PREFERRED_SIZE)))
                                                .addGap(2, 2, 2)))
                                .addGap(63, 63, 63))
        );
        personDistributionPanelLayout.setVerticalGroup(
                personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(personDistributionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(observedValuesRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(histogramRadioButton)
                                        .addComponent(binsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(binLabel))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(uniformRadioButton)
                                        .addComponent(pointLabel2)
                                        .addComponent(pointsTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(minLabel2)
                                        .addComponent(minTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(maxLabel2)
                                        .addComponent(maxTextField2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(normalDistributionRadioButton)
                                        .addComponent(normalBinLabel)
                                        .addComponent(pointsTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(meanLabel)
                                        .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(sdLabel)
                                        .addComponent(sdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(personDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(minTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(min1Label)
                                        .addComponent(maxLabel1)
                                        .addComponent(maxTextField1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        scalePanel.setBorder(BorderFactory.createTitledBorder("Default Scale"));

        logisticRadioButton.setSelected(true);
        logisticRadioButton.setText("Logistic (D = 1.0)");

        normalRadioButton.setText("Normal (D = 1.7)");

        GroupLayout scalePanelLayout = new GroupLayout(scalePanel);
        scalePanel.setLayout(scalePanelLayout);
        scalePanelLayout.setHorizontalGroup(
                scalePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(scalePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(scalePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(logisticRadioButton)
                                        .addComponent(normalRadioButton))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        scalePanelLayout.setVerticalGroup(
                scalePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(scalePanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(logisticRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(normalRadioButton)
                                .addContainerGap(70, Short.MAX_VALUE))
        );

//        scalePanel.setBorder(BorderFactory.createTitledBorder("Model Details"));
//
//        logisticRadioButton.setSelected(true);
//        logisticRadioButton.setText("Logistic (D = 1.0)");
//        normalRadioButton.setText("Normal (D = 1.7)");
//
//        modelDetailButton = new JButton();
//        modelDetailButton.setText("Select IRM");
//
//        GroupLayout scalePanelLayout = new GroupLayout(scalePanel);
//        scalePanel.setLayout(scalePanelLayout);
//        scalePanelLayout.setHorizontalGroup(
//                scalePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(scalePanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(scalePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addComponent(logisticRadioButton)
//                                        .addComponent(normalRadioButton)
//                                        .addComponent(modelDetailButton))
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        scalePanelLayout.setVerticalGroup(
//                scalePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(scalePanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addComponent(modelDetailButton)
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                .addComponent(logisticRadioButton)
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                .addComponent(normalRadioButton)
//                                .addContainerGap(36, Short.MAX_VALUE))
//        );

        personParameterPanel.setBorder(BorderFactory.createTitledBorder("Person Parameters"));

        formXPersonButton.setText("Select Form X");

        formYPersonButton.setText("Select Form Y");

        GroupLayout personParameterPanelLayout = new GroupLayout(personParameterPanel);
        personParameterPanel.setLayout(personParameterPanelLayout);
        personParameterPanelLayout.setHorizontalGroup(
                personParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(personParameterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(personParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(formXPersonButton)
                                        .addComponent(formYPersonButton))
                                .addContainerGap(20, Short.MAX_VALUE))
        );
        personParameterPanelLayout.setVerticalGroup(
                personParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(personParameterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(formXPersonButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(formYPersonButton)
                                .addContainerGap(63, Short.MAX_VALUE))
        );

        GroupLayout parameterPanelLayout = new GroupLayout(parameterPanel);
        parameterPanel.setLayout(parameterPanelLayout);
        parameterPanelLayout.setHorizontalGroup(
                parameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, parameterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(parameterPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(personDistributionPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(parameterPanelLayout.createSequentialGroup()
                                                .addComponent(itemParameterPanel, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(personParameterPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(scalePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGap(76, 76, 76))
        );
        parameterPanelLayout.setVerticalGroup(
                parameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(parameterPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(parameterPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(personParameterPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(scalePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(itemParameterPanel, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 153, GroupLayout.PREFERRED_SIZE))
                                .addGap(18, 18, 18)
                                .addComponent(personDistributionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(47, 47, 47))
        );

        tabbedPane.addTab("Parameters", parameterPanel);

        methodPanel.setBorder(BorderFactory.createTitledBorder("Method"));

        transformButtonGroup.add(msRadioButton);
        msRadioButton.setText("Mean/Sigma");

        transformButtonGroup.add(mmRadioButton);
        mmRadioButton.setText("Mean/Mean");

        transformButtonGroup.add(haebaraRadioButton);
        haebaraRadioButton.setText("Haebara");

        transformButtonGroup.add(slRadioButton);
        slRadioButton.setSelected(true);
        slRadioButton.setText("Stocking-Lord");

        GroupLayout methodPanelLayout = new GroupLayout(methodPanel);
        methodPanel.setLayout(methodPanelLayout);
        methodPanelLayout.setHorizontalGroup(
                methodPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(methodPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(methodPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(msRadioButton)
                                        .addComponent(mmRadioButton)
                                        .addComponent(haebaraRadioButton)
                                        .addComponent(slRadioButton))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        methodPanelLayout.setVerticalGroup(
                methodPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(methodPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(msRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(mmRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(haebaraRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(slRadioButton)
                                .addContainerGap(16, Short.MAX_VALUE))
        );

        transformPanel.setBorder(BorderFactory.createTitledBorder("Transform"));

        itemTransformCheckBox.setText("Item parameters");

        personTransformCheckBox.setText("Person parameters");

        precisionLabel.setText("Precision");

        precisionTextField.setText("2");

        GroupLayout transformPanelLayout = new GroupLayout(transformPanel);
        transformPanel.setLayout(transformPanelLayout);
        transformPanelLayout.setHorizontalGroup(
                transformPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(transformPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(transformPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(itemTransformCheckBox)
                                        .addComponent(personTransformCheckBox)
                                        .addGroup(transformPanelLayout.createSequentialGroup()
                                                .addComponent(precisionLabel)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(precisionTextField)))
                                .addContainerGap(13, Short.MAX_VALUE))
        );
        transformPanelLayout.setVerticalGroup(
                transformPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(transformPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(itemTransformCheckBox)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(personTransformCheckBox)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(transformPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(precisionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(precisionLabel))
                                .addContainerGap(41, Short.MAX_VALUE))
        );

        optimizationPanel.setBorder(BorderFactory.createTitledBorder("Optimization"));

        optimizationGroup.add(cgRadioButton);
        cgRadioButton.setText("Conjugate gradient");
        cgRadioButton.setActionCommand("cg");

        optimizationGroup.add(bobyqaRadioButton);
        bobyqaRadioButton.setText("Powell's BOBYQA");
        bobyqaRadioButton.setActionCommand("bobyqa");
        bobyqaRadioButton.setSelected(true);

        convergeLabel.setText("Converge");

        convergeTextField.setText("0.0005");

        GroupLayout optimizationPanelLayout = new GroupLayout(optimizationPanel);
        optimizationPanel.setLayout(optimizationPanelLayout);
        optimizationPanelLayout.setHorizontalGroup(
                optimizationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(optimizationPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(optimizationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(bobyqaRadioButton)
                                        .addComponent(cgRadioButton)
                                        .addGroup(optimizationPanelLayout.createSequentialGroup()
                                                .addComponent(convergeLabel)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(convergeTextField, GroupLayout.DEFAULT_SIZE, 68, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        optimizationPanelLayout.setVerticalGroup(
                optimizationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(optimizationPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(cgRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(bobyqaRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(optimizationPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(convergeLabel)
                                        .addComponent(convergeTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(41, Short.MAX_VALUE))
        );

        criterionPanel.setBorder(BorderFactory.createTitledBorder("Criterion Function Distribution"));

        criterionButtonGroup.add(formXCriterionRadioButton);
        formXCriterionRadioButton.setText("Form X (Forward)");

        criterionButtonGroup.add(formYCriterionRadioButton);
        formYCriterionRadioButton.setText("Form Y (Backward)");

        criterionButtonGroup.add(xyCriterionRadioButton);
        xyCriterionRadioButton.setSelected(true);
        xyCriterionRadioButton.setText("Form X & Y (Symmetric)");

        GroupLayout criterionPanelLayout = new GroupLayout(criterionPanel);
        criterionPanel.setLayout(criterionPanelLayout);
        criterionPanelLayout.setHorizontalGroup(
                criterionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(criterionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(criterionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(formXCriterionRadioButton)
                                        .addComponent(formYCriterionRadioButton)
                                        .addComponent(xyCriterionRadioButton))
                                .addContainerGap(118, Short.MAX_VALUE))
        );
        criterionPanelLayout.setVerticalGroup(
                criterionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(criterionPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(formXCriterionRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(formYCriterionRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(xyCriterionRadioButton)
                                .addContainerGap(18, Short.MAX_VALUE))
        );

        sdPanel.setBorder(BorderFactory.createTitledBorder("Standard Deviation"));

        sdButtonGroup.add(biasedRadioButton);
        biasedRadioButton.setSelected(true);
        biasedRadioButton.setText("Biased (N)");

        sdButtonGroup.add(unbiasedRadioButton);
        unbiasedRadioButton.setText("Unbiased (N-1)");

        GroupLayout sdPanelLayout = new GroupLayout(sdPanel);
        sdPanel.setLayout(sdPanelLayout);
        sdPanelLayout.setHorizontalGroup(
                sdPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(sdPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(sdPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(biasedRadioButton)
                                        .addComponent(unbiasedRadioButton))
                                .addContainerGap(36, Short.MAX_VALUE))
        );
        sdPanelLayout.setVerticalGroup(
                sdPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(sdPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(biasedRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(unbiasedRadioButton)
                                .addContainerGap(44, Short.MAX_VALUE))
        );

        noteLabel.setText("Note: Form X (new form) is transformed to the scale of Form Y (old form).");

        GroupLayout transformationPanelLayout = new GroupLayout(transformationPanel);
        transformationPanel.setLayout(transformationPanelLayout);
        transformationPanelLayout.setHorizontalGroup(
                transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(transformationPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(transformationPanelLayout.createSequentialGroup()
                                                .addGap(10, 10, 10)
                                                .addComponent(noteLabel)
                                                .addContainerGap())
                                        .addGroup(transformationPanelLayout.createSequentialGroup()
                                                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(criterionPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(GroupLayout.Alignment.LEADING, transformationPanelLayout.createSequentialGroup()
                                                                .addComponent(methodPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(transformPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(sdPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(optimizationPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addContainerGap(87, Short.MAX_VALUE))))
        );
        transformationPanelLayout.setVerticalGroup(
                transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(transformationPanelLayout.createSequentialGroup()
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(optimizationPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(transformPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(methodPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGap(12, 12, 12)
                                .addGroup(transformationPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(sdPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(criterionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(noteLabel)
                                .addGap(76, 76, 76))
        );

        tabbedPane.addTab("Transformation", transformationPanel);

//        GroupLayout jPanel1Layout = new GroupLayout(equatingPanel);
//        equatingPanel.setLayout(jPanel1Layout);
//        jPanel1Layout.setHorizontalGroup(
//                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGap(0, 527, Short.MAX_VALUE)
//        );
//        jPanel1Layout.setVerticalGroup(
//                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGap(0, 402, Short.MAX_VALUE)
//        );
//
//        tabbedPane.addTab("Equating", equatingPanel);



        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(171, 171, 171)
                                                .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 532, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 430, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addGap(16, 16, 16))
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {cancelButton, okButton, resetButton});

        pack();
    }// </editor-fold>


//    private void cancelButtonActionPerformed(ActionEvent evt) {
//        canRun=false;
//        setVisible(false);
//    }

//    private void itemBoxActionPerformed(ActionEvent evt) {
//        if(itemBox.isSelected()) transformItems=true;
//        else transformItems=false;
//    }

//    private void personBoxActionPerformed(ActionEvent evt) {
//        if(personTransformCheckBox.isSelected()) transformPersons=true;
//        else transformPersons=false;
//    }

    public void isItemParameterTable(DataTableName tableName)throws SQLException{
        VariableName nameColumn = new VariableName("name");
        ArrayList<VariableAttributes> tempVar = dao.getVariableAttributesFromColumn(conn, tableName, nameColumn);
    }

    public boolean canRun(){
        return canRun;
    }

    public IrtLinkingCommand getCommand(){
        return command;
    }

    public void resetDialogs(){
        if(itemPairDialog!=null){
            itemPairDialog.dispose();
            itemPairDialog=null;
        }
        if(itemDialogX!=null){
            itemDialogX.dispose();
            itemDialogX=null;
        }
        if(itemDialogY!=null){
            itemDialogY.dispose();
            itemDialogY=null;
        }
        if(thetaDialogX!=null){
            thetaDialogX.dispose();
            thetaDialogX=null;
        }
        if(thetaDialogY!=null){
            thetaDialogY.dispose();
            thetaDialogY=null;
        }
        uniformRadioButton.setSelected(true);
        logisticRadioButton.setSelected(true);
        slRadioButton.setSelected(true);
        xyCriterionRadioButton.setSelected(true);

        itemTransformCheckBox.setSelected(false);
        personTransformCheckBox.setSelected(false);
        biasedRadioButton.setSelected(true);
        precisionTextField.setText("2");
        convergeTextField.setText("0.0005");
        binsTextField.setText("");
        binsTextField.setEnabled(false);

        formXPersonButton.setEnabled(false);
        formYPersonButton.setEnabled(false);

        bobyqaRadioButton.setSelected(true);

        //uniform distribution
        pointsTextField2.setEnabled(true);
        pointsTextField2.setText("25");
        minTextField2.setEnabled(true);
        minTextField2.setText("-4");
        maxTextField2.setEnabled(true);
        maxTextField2.setText("4");

        //normal distribution
        pointsTextField1.setEnabled(false);
        pointsTextField1.setText("25");
        minTextField1.setEnabled(false);
        minTextField1.setText("-4");
        maxTextField1.setEnabled(false);
        maxTextField1.setText("4");
        sdTextField.setEnabled(false);
        sdTextField.setText("1");
        meanTextField.setEnabled(false);
        meanTextField.setText("0");
        cgRadioButton.setSelected(false);


    }


    public class OkActionListener implements ActionListener {

        public void actionPerformed(ActionEvent evt){
            try{
                command = new IrtLinkingCommand();
                boolean hasItem = false;
                boolean hasItemPair = false;
                boolean hasItemX = false;
                boolean hasItemY = false;
                boolean hasThetaX = false;
                boolean hasThetaY = false;
                boolean hasObserved = false;
                boolean hasUniform = false;
                boolean hasNormal = false;
                boolean hasDistribution  = false;
                boolean hasBins = false;

                if(itemDialogX!=null && itemDialogX.canRun()){
                    command.getPairedOptionList("xitem").addValue("db", dbName.toString());
                    command.getPairedOptionList("xitem").addValue("table", tableX.toString());
                    hasItemX = true;
                }else{
                    JOptionPane.showMessageDialog(IrtLinkingDialog.this,
                            "Be sure you have selected Form X item parameters.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                if(itemDialogY!=null && itemDialogY.canRun()){
                    command.getPairedOptionList("yitem").addValue("db", dbName.toString());
                    command.getPairedOptionList("yitem").addValue("table", tableY.toString());
                    hasItemY = true;
                }else{
                    JOptionPane.showMessageDialog(IrtLinkingDialog.this,
                            "Be sure you have selected Form Y item parameters.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

                hasItem = (hasItemX && hasItemY);

                String distributionAction = distributionButtonGroup.getSelection().getActionCommand();
                command.getSelectOneOption("distribution").setSelected(distributionAction);

                if(distributionAction.equals("observed") || distributionAction.equals("histogram")){
                    if(thetaDialogX!=null && thetaDialogX.canRun()){
                        tableXtheta = thetaDialogX.getTableName();
                        command.getPairedOptionList("xability").addValue("db", dbName.toString());
                        command.getPairedOptionList("xability").addValue("table", tableXtheta.toString());
                        command.getPairedOptionList("xability").addValue("theta", thetaDialogX.getTheta().getName().toString());
                        if(thetaDialogX.hasWeight()){
                            command.getPairedOptionList("xability").addValue("weight", thetaDialogX.getWeight().getName().toString());
                        }else{
                            command.getPairedOptionList("xability").addValue("weight", "");
                        }
                        hasThetaX = true;
                    }else{
                        JOptionPane.showMessageDialog(IrtLinkingDialog.this,
                                "Be sure you have selected a Form X person parameter.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    if(thetaDialogY!=null && thetaDialogY.canRun()){
                        tableYtheta = thetaDialogY.getTableName();
                        command.getPairedOptionList("yability").addValue("db", dbName.toString());
                        command.getPairedOptionList("yability").addValue("table", tableYtheta.toString());
                        command.getPairedOptionList("yability").addValue("theta", thetaDialogY.getTheta().getName().toString());
                        if(thetaDialogY.hasWeight()){
                            command.getPairedOptionList("yability").addValue("weight", thetaDialogY.getWeight().getName().toString());
                        }else{
                            command.getPairedOptionList("yability").addValue("weight", "");
                        }

                        hasThetaY = true;
                    }else{
                        JOptionPane.showMessageDialog(IrtLinkingDialog.this,
                                "Be sure you have selected a Form Y person parameter.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    hasObserved = (hasThetaX && hasThetaY);

                    if(distributionAction.equals("histogram")){
                        if(binsTextField.getText().trim().equals("")){
                            command.getSelectOneOption("binmethod").setSelected("sturges");
                        }else{
                            int bins = Math.abs(Integer.parseInt(binsTextField.getText().trim()));
                            command.getFreeOption("bins").add(bins);
                        }
                        hasBins = true;
                    }else if(distributionAction.equals("observed")){
                        command.getSelectOneOption("binmethod").setSelected("all");
                        hasBins = true;
                    }
                }else if(distributionAction.equals("uniform")){
                    int min = Integer.parseInt(minTextField2.getText().trim());
                    int max = Integer.parseInt(maxTextField2.getText().trim());
                    int numPoints = Integer.parseInt(pointsTextField2.getText().trim());
                    command.getPairedOptionList("uniform").addValue("min", min);
                    command.getPairedOptionList("uniform").addValue("max", max);
                    command.getPairedOptionList("uniform").addValue("bins", numPoints);
                    hasUniform = true;
                }else{
                    //normal distribution
                    double mean = Double.parseDouble(meanTextField.getText().trim());
                    double sd = Double.parseDouble(sdTextField.getText().trim());
                    int numPoints = Integer.parseInt(pointsTextField1.getText().trim());
                    double min = Double.parseDouble(minTextField1.getText().trim());
                    double max = Double.parseDouble(maxTextField1.getText().trim());
                    command.getPairedOptionList("normal").addValue("mean", mean);
                    command.getPairedOptionList("normal").addValue("sd", sd);
                    command.getPairedOptionList("normal").addValue("bins", numPoints);
                    command.getPairedOptionList("normal").addValue("min", min);
                    command.getPairedOptionList("normal").addValue("max", max);
                    hasNormal = true;
                }

                hasDistribution = ((hasObserved && hasBins) || hasUniform || hasNormal);

                if(itemPairDialog!=null && itemPairDialog.canRun()){
                    Object[] obj = itemPairDialog.getSelectedPairs();
                    for(int i=0;i<obj.length;i++){
                        String xyp = ((LinkingItemPair)obj[i]).commandString();
                        command.getFreeOptionList("xypairs").addValue(xyp);
                    }
                    hasItemPair = true;
                }else{
                    JOptionPane.showMessageDialog(IrtLinkingDialog.this,
                            "Be sure you have selected Form X and Form Y item pairs.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

                int precision = 4;
                if(!precisionTextField.getText().trim().equals("")){
                    precision = Integer.parseInt(precisionTextField.getText().trim());
                }

                command.getSelectOneOption("popsd").setSelected(sdButtonGroup.getSelection().getActionCommand());
                command.getSelectOneOption("scale").setSelected(scaleButtonGroup.getSelection().getActionCommand());
                command.getSelectOneOption("criterion").setSelected(criterionButtonGroup.getSelection().getActionCommand());
                command.getSelectOneOption("method").setSelected(transformButtonGroup.getSelection().getActionCommand());

                if(personTransformCheckBox.isSelected()){
                    command.getSelectAllOption("transform").setSelected("persons", true);
                }else{
                    command.getSelectAllOption("transform").setSelected("persons", false);
                }

                if(itemTransformCheckBox.isSelected()){
                    command.getSelectAllOption("transform").setSelected("items", true);
                }else{
                    command.getSelectAllOption("transform").setSelected("items", false);
                }

                command.getFreeOption("precision").add(precision);

                canRun = (hasItem && hasItemPair && hasDistribution);
                if(canRun){
                    setVisible(false);
                }

            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(IrtLinkingDialog.this,
                        ex.getMessage(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }


    }



}

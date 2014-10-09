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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import javax.swing.JOptionPane;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.dao.DerbyDatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import org.apache.log4j.Logger;

public class IrtLinkingDialogOld extends JDialog{

    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private boolean canRun = false;
    private IrtLinkingItemPairDialog itemPairDialog = null;
    private IrtLinkingItemDialog itemDialogX = null;
    private IrtLinkingItemDialog itemDialogY = null;
    private IrtLinkingThetaDialog thetaDialogX = null;
    private IrtLinkingThetaDialog thetaDialogY = null;
    private IrtLinkingCommand command = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    private DataTableName tableX = null;
    private DataTableName tableY = null;
    private DataTableName tableXtheta = null;
    private DataTableName tableYtheta = null;
    private static String FORMX = "Form X";
    private static String FORMY = "Form Y";
    private boolean transformItems = false;
    private boolean transformPersons = false;
    private DatabaseName dbName = null;
    private SortedListModel<DataTableName> tableListModel;


    // Variables declaration - do not modify
    private JRadioButton biasedButton;
    private JLabel binLabel;
    private JTextField binsField;
    private JPanel buttonPanel;
    private JButton cancelButton;
    private ButtonGroup criterionGroup;
    private JPanel criterionPanel;
    private ButtonGroup distributionGroup;
    private JPanel distributionPanel;
    private JRadioButton formXCriterionButton;
    private JButton formXItemButton;
    private JTextField formXItemField;
    private JButton formXPersonButton;
    private JTextField formXPersonField;
    private JLabel formXPersonLabel;
    private JRadioButton formXYDistributionButton;
    private JRadioButton formYDistributionButton;
    private JButton formYItemButton;
    private JTextField formYItemField;
    private JButton formYPersonButton;
    private JTextField formYPersonField;
    private JLabel formYPersonLabel;
    private JRadioButton hbButton;
    private JRadioButton histogramButton;
    private JCheckBox itemBox;
    private JPanel itemPanel;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JRadioButton logisticButton;
    private JTextField maxField;
    private JLabel maxLabel;
    private JTextField meanField;
    private JLabel meanLabel;
    private JPanel methodPanel;
    private JTextField minField;
    private JLabel minLabel;
    private JRadioButton mmButton;
    private JRadioButton msButton;
    private JRadioButton normalButton;
    private JRadioButton normalOgiveButton;
    private JRadioButton observedButton;
    private JButton okButton;
    private JCheckBox personBox;
    private JPanel personPanel;
    private JTextField precisionField;
    private JLabel precisionLabel;
    private JButton resetButton;
    private ButtonGroup scaleGroup;
    private JPanel scalePanel;
    private JTextField sdField;
    private ButtonGroup sdGroup;
    private JLabel sdLabel;
    private JPanel sdPanel;
    private JRadioButton slButton;
    private JPanel transformPanel;
    private ButtonGroup transformationGroup;
    private JRadioButton unbiasedButton;
    private JRadioButton uniformButton;
    private JTextField xyItemField;
    private JButton xyPairButton;
    // End of variables declaration

    /** Creates new form IrtEquatingDialogOld */
    public IrtLinkingDialogOld(JFrame parent, Connection conn, DatabaseName dbName, SortedListModel<DataTableName> tableListModel) {
        super(parent, "IRT Equating", true);
        this.conn = conn;
        this.dbName = dbName;
        this.tableListModel = tableListModel;

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);

        //set type of database according to properties
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

        binsField.setEditable(false);
        meanField.setEditable(false);
        sdField.setEditable(false);
        minField.setEditable(false);
        maxField.setEditable(false);
        formXPersonButton.setEnabled(true);
        formYPersonButton.setEnabled(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        distributionGroup = new ButtonGroup();
        criterionGroup = new ButtonGroup();
        scaleGroup = new ButtonGroup();
        sdGroup = new ButtonGroup();
        transformationGroup = new ButtonGroup();
        itemPanel = new JPanel();
        jLabel1 = new JLabel();
        formXItemButton = new JButton();
        jLabel2 = new JLabel();
        formYItemButton = new JButton();
        jLabel3 = new JLabel();
        xyPairButton = new JButton();
        formXItemField = new JTextField();
        formYItemField = new JTextField();
        xyItemField = new JTextField();
        distributionPanel = new JPanel();
        observedButton = new JRadioButton();
        histogramButton = new JRadioButton();
        binsField = new JTextField();
        normalButton = new JRadioButton();
        meanField = new JTextField();
        meanLabel = new JLabel();
        sdLabel = new JLabel();
        sdField = new JTextField();
        binLabel = new JLabel();
        uniformButton = new JRadioButton();
        minLabel = new JLabel();
        minField = new JTextField();
        maxLabel = new JLabel();
        maxField = new JTextField();
        personPanel = new JPanel();
        formXPersonLabel = new JLabel();
        formXPersonButton = new JButton();
        formYPersonLabel = new JLabel();
        formYPersonButton = new JButton();
        formXPersonField = new JTextField();
        formYPersonField = new JTextField();
        sdPanel = new JPanel();
        biasedButton = new JRadioButton();
        unbiasedButton = new JRadioButton();
        criterionPanel = new JPanel();
        formXCriterionButton = new JRadioButton();
        formYDistributionButton = new JRadioButton();
        formXYDistributionButton = new JRadioButton();
        scalePanel = new JPanel();
        logisticButton = new JRadioButton();
        normalOgiveButton = new JRadioButton();
        buttonPanel = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();
        resetButton = new JButton();
        methodPanel = new JPanel();
        msButton = new JRadioButton();
        mmButton = new JRadioButton();
        hbButton = new JRadioButton();
        slButton = new JRadioButton();
        transformPanel = new JPanel();
        itemBox = new JCheckBox();
        personBox = new JCheckBox();
        precisionLabel = new JLabel();
        precisionField = new JTextField();

        setTitle("IRT Equating");
        setModal(true);
        setResizable(false);
        getContentPane().setLayout(new GridBagLayout());

        itemPanel.setBorder(BorderFactory.createTitledBorder("Item Parameters"));
        itemPanel.setPreferredSize(new Dimension(300, 140));
        itemPanel.setLayout(new GridBagLayout());

        jLabel1.setText("Form X");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        itemPanel.add(jLabel1, gridBagConstraints);

        formXItemButton.setText("Select");
        formXItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(itemDialogX==null){
                    itemDialogX = new IrtLinkingItemDialog(IrtLinkingDialogOld.this, conn, dao, tableListModel, FORMX);
                }
                itemDialogX.setVisible(true);

                if(itemDialogX.canRun()){
                    tableX = itemDialogX.getTableName();
                    formXItemField.setText(tableX.toString());
                }
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        itemPanel.add(formXItemButton, gridBagConstraints);

        jLabel2.setText("Form Y");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        itemPanel.add(jLabel2, gridBagConstraints);

        formYItemButton.setText("Select");
        formYItemButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(itemDialogY==null){
                    itemDialogY = new IrtLinkingItemDialog(IrtLinkingDialogOld.this, conn, dao, tableListModel, FORMY);
                }
                itemDialogY.setVisible(true);
                if(itemDialogY.canRun()){
                    tableY = itemDialogY.getTableName();
                    formYItemField.setText(tableY.toString());
                }
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        itemPanel.add(formYItemButton, gridBagConstraints);

        jLabel3.setText("XY Pairs");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 5;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        itemPanel.add(jLabel3, gridBagConstraints);

        xyPairButton.setText("Select");
        xyPairButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(itemDialogX!=null && itemDialogY!=null){
                    if(itemDialogX.getNumberOfParameters()!=itemDialogY.getNumberOfParameters()){
                        JOptionPane.showMessageDialog(IrtLinkingDialogOld.this,
                                "You have mismatched IRT models for Form X and Form Y.\n"
                                        + "Make sure you have the same number of model parameters for each form.",
                                "Parameter Mismatch",
                                JOptionPane.ERROR_MESSAGE);
                    }else{
                        if(itemPairDialog==null){
                            itemPairDialog = new IrtLinkingItemPairDialog(IrtLinkingDialogOld.this, conn, tableX, tableY);
//                        itemDialogX.addTableSelectionListener(itemPairDialog);
//                        itemDialogY.addTableSelectionListener(itemPairDialog);
                        }
                        itemPairDialog.setVisible(true);
                        if(itemPairDialog.canRun()){
                            xyItemField.setText(itemPairDialog.numberOfPairs() + " item pairs created");
                        }
                    }
                }

            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        itemPanel.add(xyPairButton, gridBagConstraints);

        formXItemField.setColumns(30);
        formXItemField.setEditable(false);
        formXItemField.setToolTipText("Form X item parameter data");
        formXItemField.setPreferredSize(new Dimension(100, 25));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        itemPanel.add(formXItemField, gridBagConstraints);

        formYItemField.setEditable(false);
        formYItemField.setToolTipText("Form Y item data");
        formYItemField.setPreferredSize(new Dimension(100, 25));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        itemPanel.add(formYItemField, gridBagConstraints);

        xyItemField.setEditable(false);
        xyItemField.setToolTipText("Number of common items");
        xyItemField.setPreferredSize(new Dimension(100, 25));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        itemPanel.add(xyItemField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(itemPanel, gridBagConstraints);

        distributionPanel.setBorder(BorderFactory.createTitledBorder("Person Distribution"));
        distributionPanel.setPreferredSize(new Dimension(300, 140));
        distributionPanel.setLayout(new GridBagLayout());

        distributionGroup.add(observedButton);
        observedButton.setSelected(true);
        observedButton.setText("Observed Values");
        observedButton.setToolTipText("Observed values");
        observedButton.setActionCommand("observed");
        observedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                observedButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(observedButton, gridBagConstraints);

        distributionGroup.add(histogramButton);
        histogramButton.setText("Histogram");
        histogramButton.setToolTipText("Histogram of observed values");
        histogramButton.setActionCommand("histogram");
        histogramButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                histogramButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(histogramButton, gridBagConstraints);

        binsField.setColumns(10);
        binsField.setText(" ");
        binsField.setToolTipText("Number of histogram bins");
        binsField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                binsFieldActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(binsField, gridBagConstraints);

        distributionGroup.add(normalButton);
        normalButton.setText("Normal");
        normalButton.setToolTipText("Normal distribution");
        normalButton.setActionCommand("normal");
        normalButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                normalButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(normalButton, gridBagConstraints);

        meanField.setColumns(10);
        meanField.setText("0");
        meanField.setToolTipText("Distribution mean");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(meanField, gridBagConstraints);

        meanLabel.setText("Mean");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(meanLabel, gridBagConstraints);

        sdLabel.setText("SD");
        sdLabel.setToolTipText("Standard Deviation");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(sdLabel, gridBagConstraints);

        sdField.setColumns(10);
        sdField.setText("1");
        sdField.setToolTipText("Distribution standard deviation");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(sdField, gridBagConstraints);

        binLabel.setText("Bins");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(binLabel, gridBagConstraints);

        distributionGroup.add(uniformButton);
        uniformButton.setText("Uniform");
        uniformButton.setToolTipText("Uniform Distribution");
        uniformButton.setActionCommand("uniform");
        uniformButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                uniformButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(uniformButton, gridBagConstraints);

        minLabel.setText("Min");
        minLabel.setToolTipText("Minimum Value");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(minLabel, gridBagConstraints);

        minField.setColumns(10);
        minField.setText("-4");
        minField.setToolTipText("Minimum value");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(minField, gridBagConstraints);

        maxLabel.setText("Max");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 14;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(maxLabel, gridBagConstraints);

        maxField.setColumns(10);
        maxField.setText("4");
        maxField.setToolTipText("Maximum value");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 16;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 8;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = GridBagConstraints.WEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        distributionPanel.add(maxField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(distributionPanel, gridBagConstraints);

        personPanel.setBorder(BorderFactory.createTitledBorder("Person Data"));
        personPanel.setPreferredSize(new Dimension(300, 100));
        personPanel.setLayout(new GridBagLayout());

        formXPersonLabel.setText("Form X");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        personPanel.add(formXPersonLabel, gridBagConstraints);

        formXPersonButton.setText("Select");
        formXPersonButton.setToolTipText("Select Form X person data");
        formXPersonButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(thetaDialogX==null){
                    thetaDialogX = new IrtLinkingThetaDialog(IrtLinkingDialogOld.this, conn, dao, tableListModel, FORMX);
//                    thetaDialogX.addTableSelectionListener(this);
                }
                thetaDialogX.setVisible(true);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        personPanel.add(formXPersonButton, gridBagConstraints);

        formYPersonLabel.setText("Form Y");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        personPanel.add(formYPersonLabel, gridBagConstraints);

        formYPersonButton.setText("Select");
        formYPersonButton.setToolTipText("Select Form Y person data");
        formYPersonButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                if(thetaDialogY==null){
                    thetaDialogY = new IrtLinkingThetaDialog(IrtLinkingDialogOld.this, conn, dao, tableListModel, FORMX);
//                    thetaDialogY.addTableSelectionListener(this);
                }
                thetaDialogY.setVisible(true);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        personPanel.add(formYPersonButton, gridBagConstraints);

        formXPersonField.setEditable(false);
        formXPersonField.setToolTipText("Form x person parameter data");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        personPanel.add(formXPersonField, gridBagConstraints);

        formYPersonField.setEditable(false);
        formYPersonField.setToolTipText("Form y person parameter data");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        personPanel.add(formYPersonField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.8;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(personPanel, gridBagConstraints);

        sdPanel.setBorder(BorderFactory.createTitledBorder("Standard Deviation"));
        sdPanel.setPreferredSize(new Dimension(150, 100));
        sdPanel.setLayout(new GridLayout(2, 1));

        sdGroup.add(biasedButton);
        biasedButton.setSelected(true);
        biasedButton.setText("Biased");
        biasedButton.setToolTipText("Uses N in denominator");
        biasedButton.setActionCommand("biased");
        sdPanel.add(biasedButton);

        sdGroup.add(unbiasedButton);
        unbiasedButton.setText("Unbiased");
        unbiasedButton.setToolTipText("Uses N-1 in denominator");
        unbiasedButton.setActionCommand("unbiased");
        sdPanel.add(unbiasedButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        getContentPane().add(sdPanel, gridBagConstraints);

        criterionPanel.setBorder(BorderFactory.createTitledBorder("Criterion Function"));
        criterionPanel.setPreferredSize(new Dimension(150, 120));
        criterionPanel.setLayout(new GridLayout(3, 1, 5, 0));

        criterionGroup.add(formXCriterionButton);
        formXCriterionButton.setText("Form X Distribution");
        formXCriterionButton.setToolTipText("Distribution of theta for Form X");
        formXCriterionButton.setActionCommand("x");
        criterionPanel.add(formXCriterionButton);

        criterionGroup.add(formYDistributionButton);
        formYDistributionButton.setText("Form Y Distribution");
        formYDistributionButton.setToolTipText("Distribution of theta for Form Y");
        formYDistributionButton.setActionCommand("y");
        criterionPanel.add(formYDistributionButton);

        criterionGroup.add(formXYDistributionButton);
        formXYDistributionButton.setSelected(true);
        formXYDistributionButton.setText("Form X and Form Y");
        formXYDistributionButton.setToolTipText("Distribution of theta for Form X and Form Y (Symmetric)");
        formXYDistributionButton.setActionCommand("xy");
        criterionPanel.add(formXYDistributionButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        getContentPane().add(criterionPanel, gridBagConstraints);

        scalePanel.setBorder(BorderFactory.createTitledBorder("Scale"));
        scalePanel.setPreferredSize(new Dimension(150, 120));
        scalePanel.setLayout(new GridLayout(2, 1));

        scaleGroup.add(logisticButton);
        logisticButton.setSelected(true);
        logisticButton.setText("Logistic");
        logisticButton.setToolTipText("Use logistic scale (D = 1.0)");
        logisticButton.setActionCommand("logistic");
        scalePanel.add(logisticButton);

        scaleGroup.add(normalOgiveButton);
        normalOgiveButton.setText("Normal Ogive");
        normalOgiveButton.setToolTipText("Use normal ogive scale (D = 1.7)");
        normalOgiveButton.setActionCommand("normal");
        scalePanel.add(normalOgiveButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        getContentPane().add(scalePanel, gridBagConstraints);

        buttonPanel.setPreferredSize(new Dimension(550, 25));
        buttonPanel.setLayout(new GridBagLayout());

        okButton.setText("OK");
        okButton.setToolTipText("OK");
        okButton.setMaximumSize(new Dimension(65, 23));
        okButton.setMinimumSize(new Dimension(65, 23));
        okButton.setPreferredSize(new Dimension(65, 23));
        okButton.addActionListener(new OkActionListener());
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setToolTipText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        buttonPanel.add(cancelButton, gridBagConstraints);

        resetButton.setText("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetDialogs();
            }
        });
        resetButton.setMaximumSize(new Dimension(65, 23));
        resetButton.setMinimumSize(new Dimension(65, 23));
        resetButton.setPreferredSize(new Dimension(65, 23));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        buttonPanel.add(resetButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 6;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(buttonPanel, gridBagConstraints);

        methodPanel.setBorder(BorderFactory.createTitledBorder("Transformation Method"));
        methodPanel.setLayout(new GridLayout(2, 2));

        transformationGroup.add(msButton);
        msButton.setText("Mean/Sigma");
        msButton.setToolTipText("Mean/Sigma transformation method");
        msButton.setActionCommand("ms");
        methodPanel.add(msButton);

        transformationGroup.add(mmButton);
        mmButton.setText("Mean/Mean");
        mmButton.setToolTipText("Mean/Mean transformation method");
        mmButton.setActionCommand("mm");
        methodPanel.add(mmButton);

        transformationGroup.add(hbButton);
        hbButton.setText("Haebara");
        hbButton.setToolTipText("Haebara transformation method");
        hbButton.setActionCommand("hb");
        methodPanel.add(hbButton);

        transformationGroup.add(slButton);
        slButton.setSelected(true);
        slButton.setText("Stocking-Lord");
        slButton.setToolTipText("Stocking-Lord transformation method");
        slButton.setActionCommand("sl");
        methodPanel.add(slButton);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        getContentPane().add(methodPanel, gridBagConstraints);

        transformPanel.setBorder(BorderFactory.createTitledBorder("Transform"));
        transformPanel.setLayout(new GridBagLayout());

        itemBox.setText("Item Parameters");
        itemBox.setToolTipText("Tansform Form X item parameters");
        itemBox.setActionCommand("item");
        itemBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                itemBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        transformPanel.add(itemBox, gridBagConstraints);

        personBox.setText("Person Parameters");
        personBox.setToolTipText("Transform Form X person parameters");
        personBox.setActionCommand("person");
        personBox.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                personBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        transformPanel.add(personBox, gridBagConstraints);

        precisionLabel.setText("Precision");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        transformPanel.add(precisionLabel, gridBagConstraints);

        precisionField.setColumns(10);
        precisionField.setText("4");
        precisionField.setToolTipText("Number of decimal places in scaling coefficients");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        transformPanel.add(precisionField, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        getContentPane().add(transformPanel, gridBagConstraints);

        pack();
    }// </editor-fold>

    private void observedButtonActionPerformed(ActionEvent evt) {
        binsField.setEditable(false);
        meanField.setEditable(false);
        sdField.setEditable(false);
        minField.setEditable(false);
        maxField.setEditable(false);
        formXPersonButton.setEnabled(true);
        formYPersonButton.setEnabled(true);
    }

    private void histogramButtonActionPerformed(ActionEvent evt) {
        binsField.setEditable(true);
        meanField.setEditable(false);
        sdField.setEditable(false);
        minField.setEditable(false);
        maxField.setEditable(false);
        formXPersonButton.setEnabled(true);
        formYPersonButton.setEnabled(true);
    }

    private void normalButtonActionPerformed(ActionEvent evt) {
        binsField.setEditable(false);
        meanField.setEditable(true);
        sdField.setEditable(true);
        minField.setEditable(false);
        maxField.setEditable(false);
        formXPersonButton.setEnabled(false);
        formYPersonButton.setEnabled(false);
    }

    private void uniformButtonActionPerformed(ActionEvent evt) {
        binsField.setEditable(false);
        meanField.setEditable(false);
        sdField.setEditable(false);
        minField.setEditable(true);
        maxField.setEditable(true);
        formXPersonButton.setEnabled(false);
        formYPersonButton.setEnabled(false);
    }

    private void formYItemButtonActionPerformed(ActionEvent evt) {

    }

    private void formXPersonButtonActionPerformed(ActionEvent evt) {

    }

    private void formYPersonButtonActionPerformed(ActionEvent evt) {

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
                    command.getPairedOptionList("xitem").addValue("difficulty", itemDialogX.getDifficultyVariable().getName().toString());
                    command.getPairedOptionList("xitem").addValue("discrimination", itemDialogX.getDiscriminationVariable().getName().toString());
                    command.getPairedOptionList("xitem").addValue("guessing", itemDialogX.getGuessingVariable().getName().toString());
                    hasItemX = true;
                }else{
                    JOptionPane.showMessageDialog(IrtLinkingDialogOld.this,
                            "Be sure you have selected Form X item parameters.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                if(itemDialogY!=null && itemDialogY.canRun()){
                    command.getPairedOptionList("yitem").addValue("table", tableY.toString());
                    command.getPairedOptionList("yitem").addValue("difficulty", itemDialogY.getDifficultyVariable().toString());
                    command.getPairedOptionList("yitem").addValue("discrimination", itemDialogY.getDiscriminationVariable().toString());
                    command.getPairedOptionList("yitem").addValue("guessing", itemDialogY.getGuessingVariable().toString());
                    hasItemY = true;
                }else{
                    JOptionPane.showMessageDialog(IrtLinkingDialogOld.this,
                            "Be sure you have selected Form Y item parameters.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

                hasItem = (hasItemX && hasItemY);

                String distributionAction = distributionGroup.getSelection().getActionCommand();
                command.getSelectOneOption("distribution").setSelected(distributionAction);

                if(distributionAction.equals("observed") || distributionAction.equals("histogram")){
                    if(thetaDialogX!=null && thetaDialogX.canRun()){
                        command.getPairedOptionList("xability").addValue("table", tableXtheta.toString());
                        command.getPairedOptionList("xability").addValue("theta", thetaDialogX.getTheta().getName().toString());
                        command.getPairedOptionList("xability").addValue("weight", thetaDialogX.getWeight().getName().toString());
                        hasThetaX = true;
                    }else{
                        JOptionPane.showMessageDialog(IrtLinkingDialogOld.this,
                                "Be sure you have selected a Form X person parameter.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                    if(thetaDialogY!=null && thetaDialogY.canRun()){
                        command.getPairedOptionList("yability").addValue("table", tableYtheta.toString());
                        command.getPairedOptionList("yability").addValue("theta", thetaDialogY.getTheta().getName().toString());
                        command.getPairedOptionList("yability").addValue("weight", thetaDialogY.getWeight().getName().toString());
                        hasThetaY = true;
                    }else{
                        JOptionPane.showMessageDialog(IrtLinkingDialogOld.this,
                                "Be sure you have selected a Form Y person parameter.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                    hasObserved = (hasThetaX && hasThetaY);

                    if(distributionAction.equals("histogram")){
                        if(binsField.getText().trim().equals("")){
                            command.getSelectOneOption("binmethod").setSelected("sturges");
                        }else{
                            int bins = Math.abs(Integer.parseInt(binsField.getText().trim()));
                            command.getFreeOption("bins").add(bins);
                        }
                        hasBins = true;
                    }else if(distributionAction.equals("observed")){
                        command.getSelectOneOption("binmethod").setSelected("all");
                        hasBins = true;
                    }
                }else if(distributionAction.equals("uniform")){
                    int min = Integer.parseInt(minField.getText().trim());
                    int max = Integer.parseInt(maxField.getText().trim());
                    command.getPairedOptionList("uniform").addValue("min", min);
                    command.getPairedOptionList("uniform").addValue("max", max);
                    command.getPairedOptionList("uniform").addValue("bins", 10);//FIXME give user options to change this value
                    hasUniform = true;
                }else{
                    //normal distribution
                    double mean = Double.parseDouble(meanField.getText().trim());
                    double sd = Double.parseDouble(sdField.getText().trim());
                    command.getPairedOptionList("normal").addValue("mean", mean);
                    command.getPairedOptionList("normal").addValue("sd", sd);
                    command.getPairedOptionList("normal").addValue("bins", 10);//FIXME give user options to change this value
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
                    JOptionPane.showMessageDialog(IrtLinkingDialogOld.this,
                            "Be sure you have selected Form X and Form Y item pairs.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

                int precision = 4;
                if(!precisionField.getText().trim().equals("")){
                    precision = Integer.parseInt(precisionField.getText().trim());
                }

                command.getSelectOneOption("popsd").setSelected(sdGroup.getSelection().getActionCommand());
                command.getSelectOneOption("scale").setSelected(scaleGroup.getSelection().getActionCommand());
                command.getSelectOneOption("criterion").setSelected(criterionGroup.getSelection().getActionCommand());
                command.getSelectOneOption("method").setSelected(transformationGroup.getSelection().getActionCommand());
                command.getSelectAllOption("transform").setSelected("items", transformItems);
                command.getSelectAllOption("transform").setSelected("persons", transformPersons);
                command.getFreeOption("precision").add(precision);


                canRun = (hasItem && hasItemPair && hasDistribution);
                if(canRun){
                    setVisible(false);
                }

            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(IrtLinkingDialogOld.this,
                        ex.getMessage(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }


    }

    private void binsFieldActionPerformed(ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void cancelButtonActionPerformed(ActionEvent evt) {
        canRun=false;
        setVisible(false);
    }

    private void itemBoxActionPerformed(ActionEvent evt) {
        if(itemBox.isSelected()) transformItems=true;
        else transformItems=false;
    }

    private void personBoxActionPerformed(ActionEvent evt) {
        if(personBox.isSelected()) transformPersons=true;
        else transformPersons=false;
    }

    public boolean canRun(){
        return canRun;
    }

    public IrtLinkingCommand getCommand(){
        return command;
    }

    public void resetDialogs(){
        itemPairDialog.dispose();
        itemPairDialog=null;
        itemDialogX.dispose();
        itemDialogX=null;
        itemDialogY.dispose();
        itemDialogY=null;
        thetaDialogX.dispose();
        thetaDialogX=null;
        thetaDialogY.dispose();
        thetaDialogY=null;
    }


//    public class TableSelectionListener implements ListSelectionListener{
//        public void tableChanged(ListSelectionEvent e){
//            if(e.itemList()){
//                if(e.getId().equals("Form X")){
//                    tableX = e.getTableName();
//                    formXItemField.setText(tableX.toString());
//                }else{
//                    tableY = e.getTableName();
//                    formYItemField.setText(tableY.toString());
//                }
//            }else{
//                if(e.getId().equals("Form X")){
//                    tableXtheta = e.getTableName();
//                    formXPersonField.setText(tableXtheta.toString());
//                }else{
//                    tableYtheta = e.getTableName();
//                    formYPersonField.setText(tableYtheta.toString());
//                }
//            }
//    }
//
//
//
//    }



}

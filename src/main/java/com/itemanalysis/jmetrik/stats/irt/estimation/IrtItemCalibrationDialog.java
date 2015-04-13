package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.commandbuilder.MegaOption;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.model.VariableListFilter;
import com.itemanalysis.jmetrik.model.VariableListModel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.ArrayList;

public class IrtItemCalibrationDialog extends JDialog {

    // Variables declaration - do not modify
    private JLabel baseGroupLabel;
    private JTextField baseGroupTextField;
    private JRadioButton bfgsRadioButton;
    private JPanel convergencePanel;
    private JLabel countLabel;
    private JLabel distributionOutputLabel;
    private JTextField distributionOutputTextField;
    private JButton fixedValuesButton;
    private JLabel groupByLabel;
    private JTextField groupByTextField;
    private JRadioButton ignoreRadioButton;
    private ButtonGroup itemFitButtonGroup;
    private JPanel itemFitPanel;
    private JPanel itemOptionPanel;
    private JPanel itemPanel;
    private JButton okButton;
    private JButton cancelButton;
    private JLabel jLabel1;
    private JPanel buttonPanel;
    private JPanel latentDistributionPanel;
    private JLabel maxIterScoreLabel;
    private JTextField maxIterScoreTextField;
    private JLabel maxIterationLabel;
    private JTextField maxIterationTextField;
    private JLabel maxLabel;
    private JLabel maxScoreLabel;
    private JTextField maxScoreTextField;
    private JTextField maxTextField;
    private JLabel meanScoreLabel;
    private JTextField meanTextField;
    private JLabel minLabel;
    private JLabel minScoreLabel;
    private JTextField minScoreTextField;
    private JTextField minTextField;
    private JTextField mincountTextField;
    private ButtonGroup missingDataButtonGroup;
    private JPanel missingDataPanel;
    private ButtonGroup optimizerButtonGroup;
    private JPanel optimizerPanel;
    private JPanel optionsPanel;
    private JLabel outputLabel;
    private JTextField outputTextField;
    private JPanel personPanel;
    private JPanel personScorePanel;
    private JPanel personScoringTab;
    private JTextField pointTextField;
    private JLabel pointsLabel;
    private JLabel pointsScoreLabel;
    private JTextField pointsScoreTextField;
    private JLabel residualLabel;
    private JTextField residualTextField;
    private JRadioButton scoreAsZeroRadioButton;
    private JLabel scoreTypeLabel;
    private JComboBox scoringcomboBox;
    private JLabel sdScoreLabel;
    private JTextField sdTextField;
    private JButton selectGroupButton;
    private JButton selectItemsButton;
    private JTabbedPane tabbedPane;
    private JLabel toeranceLabel;
    private JLabel toleranceScoreLabel;
    private JTextField toleranceScoreTextField;
    private JTextField toleranceTextField;
    private JComboBox typeComboBox;
    private JLabel typeLabel;
    private JRadioButton uncminRadioButton;
    private JLabel variableNameScoreLabel;
    private JTextField variableNameScoreTextField;
    private JPanel fixedPanel;
    private JLabel selectFixedLabel;
    private JButton selectFixedButton;
    private JTextField selectFixedTextField;
    // End of variables declaration

    private Connection conn = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private SortedListModel<DataTableName> tableListModel = null;
    private VariableListModel variableListModel = null;
    private boolean canRun = false;
    private IrtItemCalibrationCommand command = null;
    private ItemGroupWizzard wizzard = null;

    private String[] latentDistributionTypeString = { "Normal", "Gauss-Hermite"};
    private String[] latentDistributionCode = {"normal", "GH"};

    public IrtItemCalibrationDialog(JFrame parent, Connection conn, DatabaseName dbName, DataTableName tableName,
                                    SortedListModel<DataTableName> tableListModel, ArrayList<VariableAttributes> variables){

        super(parent, "IRT Item Calibration", true);

        this.conn = conn;
        this.dbName = dbName;
        this.tableName = tableName;
        this.tableListModel = tableListModel;

        VariableListFilter listFilter = new VariableListFilter();
        listFilter.addFilteredItemType(ItemType.NOT_ITEM);
        variableListModel = new VariableListModel(listFilter);
        for(VariableAttributes v : variables){
            variableListModel.addElement(v);
        }

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

    }

    private void initComponents() {
        GridBagConstraints gridBagConstraints;

        optimizerButtonGroup = new ButtonGroup();
        missingDataButtonGroup = new ButtonGroup();
        tabbedPane = new JTabbedPane();
        itemPanel = new JPanel();
        itemOptionPanel = new JPanel();
        selectItemsButton = new JButton();
        fixedValuesButton = new JButton();
        outputLabel = new JLabel();
        outputTextField = new JTextField();
        selectFixedLabel = new JLabel();
        selectFixedTextField = new JTextField();
        itemFitPanel = new JPanel();
        mincountTextField = new JTextField();
        countLabel = new JLabel();
        personPanel = new JPanel();
        latentDistributionPanel = new JPanel();
        typeLabel = new JLabel();
        typeComboBox = new JComboBox();
        pointsLabel = new JLabel();
        pointTextField = new JTextField();
        minLabel = new JLabel();
        minTextField = new JTextField();
        maxLabel = new JLabel();
        maxTextField = new JTextField();
        groupByLabel = new JLabel();
        groupByTextField = new JTextField();
        selectGroupButton = new JButton();
        distributionOutputLabel = new JLabel();
        distributionOutputTextField = new JTextField();
        baseGroupLabel = new JLabel();
        baseGroupTextField = new JTextField();
        personScoringTab = new JPanel();
        personScorePanel = new JPanel();
        scoreTypeLabel = new JLabel();
        scoringcomboBox = new JComboBox();
        minScoreLabel = new JLabel();
        minScoreTextField = new JTextField();
        maxScoreLabel = new JLabel();
        maxScoreTextField = new JTextField();
        residualLabel = new JLabel();
        residualTextField = new JTextField();
        meanScoreLabel = new JLabel();
        meanTextField = new JTextField();
        sdScoreLabel = new JLabel();
        sdTextField = new JTextField();
        pointsScoreLabel = new JLabel();
        pointsScoreTextField = new JTextField();
        maxIterScoreLabel = new JLabel();
        maxIterScoreTextField = new JTextField();
        toleranceScoreLabel = new JLabel();
        toleranceScoreTextField = new JTextField();
        variableNameScoreLabel = new JLabel();
        variableNameScoreTextField = new JTextField();
        optionsPanel = new JPanel();
        convergencePanel = new JPanel();
        toeranceLabel = new JLabel();
        toleranceTextField = new JTextField();
        maxIterationLabel = new JLabel();
        maxIterationTextField = new JTextField();
        missingDataPanel = new JPanel();
        ignoreRadioButton = new JRadioButton();
        scoreAsZeroRadioButton = new JRadioButton();
        optimizerPanel = new JPanel();
        uncminRadioButton = new JRadioButton();
        bfgsRadioButton = new JRadioButton();
        buttonPanel = new JPanel();
        okButton = new JButton();
        cancelButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        itemOptionPanel.setBorder(BorderFactory.createTitledBorder("Item Options"));
        itemOptionPanel.setPreferredSize(new Dimension(456, 116));

        selectItemsButton.setText("Select Items");
        selectItemsButton.setMaximumSize(new Dimension(125, 28));
        selectItemsButton.setMinimumSize(new Dimension(125, 28));
        selectItemsButton.setPreferredSize(new Dimension(125, 28));
        selectItemsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (wizzard == null) wizzard = new ItemGroupWizzard(IrtItemCalibrationDialog.this, variableListModel);
                wizzard.setVisible(true);
            }
        });

        fixedValuesButton.setText("Fixed Values");
        fixedValuesButton.setEnabled(false);
        fixedValuesButton.setMaximumSize(new Dimension(125, 28));
        fixedValuesButton.setMinimumSize(new Dimension(125, 28));
        fixedValuesButton.setPreferredSize(new Dimension(125, 28));

        outputLabel.setText("Output table");

        outputTextField.setMaximumSize(new Dimension(160, 28));
        outputTextField.setMinimumSize(new Dimension(160, 28));
        outputTextField.setPreferredSize(new Dimension(160, 28));

        selectFixedLabel.setText("Fixed Table");

        selectFixedTextField.setMinimumSize(new Dimension(125, 28));
        selectFixedTextField.setPreferredSize(new Dimension(125, 28));
        selectFixedTextField.setEnabled(false);

        GroupLayout itemOptionPanelLayout = new GroupLayout(itemOptionPanel);
        itemOptionPanel.setLayout(itemOptionPanelLayout);
        itemOptionPanelLayout.setHorizontalGroup(
            itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemOptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(selectItemsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(fixedValuesButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(outputLabel)
                    .addComponent(selectFixedLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(outputTextField, GroupLayout.DEFAULT_SIZE, 206, Short.MAX_VALUE)
                    .addComponent(selectFixedTextField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        itemOptionPanelLayout.setVerticalGroup(
            itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemOptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(outputLabel)
                        .addComponent(outputTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(selectItemsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(fixedValuesButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(selectFixedLabel))
                    .addComponent(selectFixedTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );

        itemFitPanel.setBorder(BorderFactory.createTitledBorder("Item FIt"));

        mincountTextField.setText("1");
        mincountTextField.setMaximumSize(new Dimension(50, 28));
        mincountTextField.setMinimumSize(new Dimension(50, 28));
        mincountTextField.setPreferredSize(new Dimension(50, 28));

        countLabel.setText("Min average expected count");

        GroupLayout itemFitPanelLayout = new GroupLayout(itemFitPanel);
        itemFitPanel.setLayout(itemFitPanelLayout);
        itemFitPanelLayout.setHorizontalGroup(
            itemFitPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemFitPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(countLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(mincountTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        itemFitPanelLayout.setVerticalGroup(
            itemFitPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemFitPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemFitPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(mincountTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(countLabel))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout itemPanelLayout = new GroupLayout(itemPanel);
        itemPanel.setLayout(itemPanelLayout);
        itemPanelLayout.setHorizontalGroup(
            itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(itemOptionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(itemPanelLayout.createSequentialGroup()
                        .addComponent(itemFitPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        itemPanelLayout.setVerticalGroup(
            itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(itemOptionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(itemFitPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Item Calibration", itemPanel);

        latentDistributionPanel.setBorder(BorderFactory.createTitledBorder("Latent Distribution Options"));

        typeLabel.setText("Type");

        typeComboBox.setModel(new DefaultComboBoxModel(new String[] { "Normal", "Gauss-Hermite", "Estimate Nonparametric" }));
        typeComboBox.setMaximumSize(new Dimension(125, 28));
        typeComboBox.setMinimumSize(new Dimension(125, 28));
        typeComboBox.setPreferredSize(new Dimension(125, 28));
        typeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) typeComboBox.getSelectedItem();
                if ("Gauss-Hermite".equals(s)) {
                    pointTextField.setEnabled(true);
                    minTextField.setEnabled(false);
                    maxTextField.setEnabled(false);
                } else {
                    pointTextField.setEnabled(true);
                    minTextField.setEnabled(true);
                    maxTextField.setEnabled(true);
                }
            }
        });

        pointsLabel.setText("Points");

        pointTextField.setText("40");
        pointTextField.setMaximumSize(new Dimension(50, 28));
        pointTextField.setMinimumSize(new Dimension(50, 28));
        pointTextField.setPreferredSize(new Dimension(50, 28));

        minLabel.setText("Min value");

        minTextField.setText("-4.0");
        minTextField.setMaximumSize(new Dimension(50, 28));
        minTextField.setMinimumSize(new Dimension(50, 28));
        minTextField.setPreferredSize(new Dimension(50, 28));

        maxLabel.setText("Max value");

        maxTextField.setText("4.0");
        maxTextField.setMaximumSize(new Dimension(50, 28));
        maxTextField.setMinimumSize(new Dimension(50, 28));
        maxTextField.setPreferredSize(new Dimension(50, 28));

        groupByLabel.setText("Group by");

        groupByTextField.setEnabled(false);
        groupByTextField.setMaximumSize(new Dimension(150, 28));
        groupByTextField.setMinimumSize(new Dimension(150, 28));
        groupByTextField.setPreferredSize(new Dimension(150, 28));

        selectGroupButton.setText("Select");
        selectGroupButton.setEnabled(false);
        selectGroupButton.setMaximumSize(new Dimension(75, 28));
        selectGroupButton.setMinimumSize(new Dimension(75, 28));
        selectGroupButton.setPreferredSize(new Dimension(75, 28));

        distributionOutputLabel.setText("Output table");

        distributionOutputTextField.setMaximumSize(new Dimension(150, 28));
        distributionOutputTextField.setMinimumSize(new Dimension(150, 28));
        distributionOutputTextField.setPreferredSize(new Dimension(150, 28));

        baseGroupLabel.setText("Base group");

        baseGroupTextField.setEnabled(false);
        baseGroupTextField.setMaximumSize(new Dimension(50, 28));
        baseGroupTextField.setMinimumSize(new Dimension(50, 28));
        baseGroupTextField.setPreferredSize(new Dimension(50, 28));

        GroupLayout latentDistributionPanelLayout = new GroupLayout(latentDistributionPanel);
        latentDistributionPanel.setLayout(latentDistributionPanelLayout);
        latentDistributionPanelLayout.setHorizontalGroup(
            latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                        .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                            .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                .addGroup(GroupLayout.Alignment.LEADING, latentDistributionPanelLayout.createSequentialGroup()
                                    .addGap(22, 22, 22)
                                    .addComponent(typeLabel)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(typeComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                                    .addComponent(minLabel)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                    .addComponent(maxLabel)
                                    .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(pointsLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pointTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(GroupLayout.Alignment.LEADING, latentDistributionPanelLayout.createSequentialGroup()
                            .addComponent(distributionOutputLabel)
                            .addGap(90, 90, 90)))
                    .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                        .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                                .addComponent(groupByLabel)
                                .addGap(21, 21, 21)
                                .addComponent(groupByTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selectGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGroup(GroupLayout.Alignment.TRAILING, latentDistributionPanelLayout.createSequentialGroup()
                                .addGap(65, 65, 65)
                                .addComponent(distributionOutputTextField, GroupLayout.PREFERRED_SIZE, 231, GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(baseGroupLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(baseGroupTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(12, Short.MAX_VALUE))
        );
        latentDistributionPanelLayout.setVerticalGroup(
            latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(typeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(pointsLabel)
                        .addComponent(pointTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(maxLabel)
                        .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(minLabel)
                        .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(groupByLabel)
                    .addComponent(groupByTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(baseGroupLabel)
                    .addComponent(baseGroupTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(distributionOutputLabel)
                    .addComponent(distributionOutputTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout personPanelLayout = new GroupLayout(personPanel);
        personPanel.setLayout(personPanelLayout);
        personPanelLayout.setHorizontalGroup(
            personPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(personPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(latentDistributionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        personPanelLayout.setVerticalGroup(
            personPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(personPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(latentDistributionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Latent Distribution", personPanel);

        personScorePanel.setBorder(BorderFactory.createTitledBorder("Person Scoring Options"));

        scoreTypeLabel.setText("Score type");

        scoringcomboBox.setModel(new DefaultComboBoxModel(new String[] { "EAP", "MAP", "MLE" }));
        scoringcomboBox.setMaximumSize(new Dimension(65, 28));
        scoringcomboBox.setMinimumSize(new Dimension(65, 28));
        scoringcomboBox.setPreferredSize(new Dimension(65, 28));
        scoringcomboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = (String) scoringcomboBox.getSelectedItem();
                if ("MLE".equals(s)) {
                    meanTextField.setEnabled(false);
                    sdTextField.setEnabled(false);
                    toleranceScoreTextField.setEnabled(true);
                    maxIterScoreTextField.setEnabled(true);
                } else if ("MAP".equals(s)) {
                    meanTextField.setEnabled(true);
                    sdTextField.setEnabled(true);
                    toleranceScoreTextField.setEnabled(true);
                    maxIterScoreTextField.setEnabled(true);
                } else {
                    meanTextField.setEnabled(true);
                    sdTextField.setEnabled(true);
                    toleranceScoreTextField.setEnabled(false);
                    maxIterScoreTextField.setEnabled(false);
                }
            }
        });

        minScoreLabel.setText("Min");

        minScoreTextField.setText("-6.0");
        minScoreTextField.setMaximumSize(new Dimension(50, 28));
        minScoreTextField.setMinimumSize(new Dimension(50, 28));
        minScoreTextField.setPreferredSize(new Dimension(50, 28));

        maxScoreLabel.setText("Max");

        maxScoreTextField.setText("6.0");
        maxScoreTextField.setMaximumSize(new Dimension(50, 28));
        maxScoreTextField.setMinimumSize(new Dimension(50, 28));
        maxScoreTextField.setPreferredSize(new Dimension(50, 28));

        residualLabel.setText("Residual table");

        residualTextField.setMaximumSize(new Dimension(155, 28));
        residualTextField.setMinimumSize(new Dimension(155, 28));
        residualTextField.setPreferredSize(new Dimension(155, 28));

        meanScoreLabel.setText("Mean");

        meanTextField.setText("0");
        meanTextField.setMaximumSize(new Dimension(50, 28));
        meanTextField.setMinimumSize(new Dimension(50, 28));
        meanTextField.setPreferredSize(new Dimension(50, 28));

        sdScoreLabel.setText("SD");

        sdTextField.setText("1");
        sdTextField.setMaximumSize(new Dimension(50, 28));
        sdTextField.setMinimumSize(new Dimension(50, 28));
        sdTextField.setPreferredSize(new Dimension(50, 28));

        pointsScoreLabel.setText("Points");

        pointsScoreTextField.setText("60");
        pointsScoreTextField.setMaximumSize(new Dimension(50, 28));
        pointsScoreTextField.setMinimumSize(new Dimension(50, 28));
        pointsScoreTextField.setPreferredSize(new Dimension(50, 28));

        maxIterScoreLabel.setText("Max iterations");

        maxIterScoreTextField.setText("100");
        maxIterScoreTextField.setEnabled(false);
        maxIterScoreTextField.setMaximumSize(new Dimension(60, 28));
        maxIterScoreTextField.setMinimumSize(new Dimension(60, 28));
        maxIterScoreTextField.setPreferredSize(new Dimension(60, 28));

        toleranceScoreLabel.setText("Tolerance");

        toleranceScoreTextField.setText("1e-10");
        toleranceScoreTextField.setEnabled(false);
        toleranceScoreTextField.setMaximumSize(new Dimension(60, 28));
        toleranceScoreTextField.setMinimumSize(new Dimension(60, 28));
        toleranceScoreTextField.setPreferredSize(new Dimension(60, 28));

        variableNameScoreLabel.setText("Variable name");

        variableNameScoreTextField.setMaximumSize(new Dimension(155, 28));
        variableNameScoreTextField.setMinimumSize(new Dimension(155, 28));
        variableNameScoreTextField.setPreferredSize(new Dimension(155, 28));

        GroupLayout personScorePanelLayout = new GroupLayout(personScorePanel);
        personScorePanel.setLayout(personScorePanelLayout);
        personScorePanelLayout.setHorizontalGroup(
            personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(personScorePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(personScorePanelLayout.createSequentialGroup()
                        .addComponent(variableNameScoreLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(variableNameScoreTextField, GroupLayout.PREFERRED_SIZE, 186, GroupLayout.PREFERRED_SIZE))
                    .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                        .addGroup(GroupLayout.Alignment.LEADING, personScorePanelLayout.createSequentialGroup()
                            .addGap(25, 25, 25)
                            .addComponent(scoreTypeLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(scoringcomboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(maxIterScoreLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(maxIterScoreTextField, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(toleranceScoreLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(toleranceScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGroup(GroupLayout.Alignment.LEADING, personScorePanelLayout.createSequentialGroup()
                            .addComponent(meanScoreLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(sdScoreLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(sdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(minScoreLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(minScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(maxScoreLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(maxScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                            .addComponent(pointsScoreLabel)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(pointsScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addGroup(personScorePanelLayout.createSequentialGroup()
                        .addComponent(residualLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(residualTextField, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        personScorePanelLayout.setVerticalGroup(
            personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(personScorePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(variableNameScoreLabel)
                    .addComponent(variableNameScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(scoreTypeLabel)
                    .addComponent(scoringcomboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxIterScoreLabel)
                    .addComponent(maxIterScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(toleranceScoreLabel)
                    .addComponent(toleranceScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(meanScoreLabel)
                    .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(sdScoreLabel)
                    .addComponent(sdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(minScoreLabel)
                    .addComponent(minScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(maxScoreLabel)
                    .addComponent(maxScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(pointsScoreLabel)
                    .addComponent(pointsScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(residualLabel)
                    .addComponent(residualTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout personScoringTabLayout = new GroupLayout(personScoringTab);
        personScoringTab.setLayout(personScoringTabLayout);
        personScoringTabLayout.setHorizontalGroup(
            personScoringTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(personScoringTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(personScorePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        personScoringTabLayout.setVerticalGroup(
            personScoringTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(personScoringTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(personScorePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Person Scoring", personScoringTab);

        convergencePanel.setBorder(BorderFactory.createTitledBorder("Convergence Criteria"));

        toeranceLabel.setText("Tolerance");

        toleranceTextField.setText("0.001");
        toleranceTextField.setMaximumSize(new Dimension(75, 28));
        toleranceTextField.setMinimumSize(new Dimension(75, 28));
        toleranceTextField.setPreferredSize(new Dimension(75, 28));

        maxIterationLabel.setText("Max iterations");

        maxIterationTextField.setText("250");
        maxIterationTextField.setMaximumSize(new Dimension(75, 28));
        maxIterationTextField.setMinimumSize(new Dimension(75, 28));
        maxIterationTextField.setPreferredSize(new Dimension(75, 28));

        GroupLayout convergencePanelLayout = new GroupLayout(convergencePanel);
        convergencePanel.setLayout(convergencePanelLayout);
        convergencePanelLayout.setHorizontalGroup(
            convergencePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(convergencePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(convergencePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(convergencePanelLayout.createSequentialGroup()
                        .addComponent(toeranceLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(toleranceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(convergencePanelLayout.createSequentialGroup()
                        .addComponent(maxIterationLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxIterationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        convergencePanelLayout.setVerticalGroup(
            convergencePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(convergencePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(convergencePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(toeranceLabel)
                    .addComponent(toleranceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(convergencePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(maxIterationLabel)
                    .addComponent(maxIterationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        missingDataPanel.setBorder(BorderFactory.createTitledBorder("Missing Data"));

        missingDataButtonGroup.add(ignoreRadioButton);
        ignoreRadioButton.setSelected(true);
        ignoreRadioButton.setText("Ignore");
        ignoreRadioButton.setActionCommand("ignore");

        missingDataButtonGroup.add(scoreAsZeroRadioButton);
        scoreAsZeroRadioButton.setText("Score as zero");
        scoreAsZeroRadioButton.setActionCommand("zero");

        GroupLayout missingDataPanelLayout = new GroupLayout(missingDataPanel);
        missingDataPanel.setLayout(missingDataPanelLayout);
        missingDataPanelLayout.setHorizontalGroup(
            missingDataPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(missingDataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(missingDataPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(ignoreRadioButton)
                    .addComponent(scoreAsZeroRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        missingDataPanelLayout.setVerticalGroup(
            missingDataPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(missingDataPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ignoreRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scoreAsZeroRadioButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        optimizerPanel.setBorder(BorderFactory.createTitledBorder("Optimizer"));

        optimizerButtonGroup.add(uncminRadioButton);
        uncminRadioButton.setSelected(true);
        uncminRadioButton.setText("UNCMIN");
        uncminRadioButton.setActionCommand("uncmin");

        optimizerButtonGroup.add(bfgsRadioButton);
        bfgsRadioButton.setText("BFGS");
        bfgsRadioButton.setActionCommand("bfgs");

        GroupLayout optimizerPanelLayout = new GroupLayout(optimizerPanel);
        optimizerPanel.setLayout(optimizerPanelLayout);
        optimizerPanelLayout.setHorizontalGroup(
            optimizerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optimizerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optimizerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(uncminRadioButton)
                    .addComponent(bfgsRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        optimizerPanelLayout.setVerticalGroup(
            optimizerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optimizerPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(uncminRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(bfgsRadioButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout optionsPanelLayout = new GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(convergencePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(missingDataPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optimizerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(71, Short.MAX_VALUE))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                    .addComponent(optimizerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(missingDataPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(convergencePanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(104, Short.MAX_VALUE))
        );

        tabbedPane.addTab("Options", optionsPanel);

        buttonPanel.setMinimumSize(new Dimension(471, 40));
        buttonPanel.setPreferredSize(new Dimension(471, 40));
        buttonPanel.setLayout(new GridBagLayout());

        okButton.setText("OK");
        okButton.setMaximumSize(new Dimension(72, 28));
        okButton.setMinimumSize(new Dimension(72, 28));
        okButton.setPreferredSize(new Dimension(72, 28));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setCommand();
//                System.out.println(command.paste());
                setVisible(false);
            }
        });


        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.ipadx = 1;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.4;
        gridBagConstraints.insets = new Insets(11, 204, 11, 0);
        buttonPanel.add(okButton, gridBagConstraints);

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new Dimension(72, 28));
        cancelButton.setMinimumSize(new Dimension(72, 28));
        cancelButton.setPreferredSize(new Dimension(72, 28));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun = false;
                setVisible(false);
            }
        });

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new Insets(11, 6, 11, 118);
        buttonPanel.add(cancelButton, gridBagConstraints);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(20, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 250, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }

//    private void initComponents() {
//        GridBagConstraints gridBagConstraints;
//
//        optimizerButtonGroup = new ButtonGroup();
//        missingDataButtonGroup = new ButtonGroup();
//        itemFitButtonGroup = new ButtonGroup();
//        tabbedPane = new JTabbedPane();
//        itemPanel = new JPanel();
//        itemOptionPanel = new JPanel();
//        selectItemsButton = new JButton();
//        fixedValuesButton = new JButton();
//        outputLabel = new JLabel();
//        outputTextField = new JTextField();
//        itemFitPanel = new JPanel();
//        mincountTextField = new JTextField();
//        countLabel = new JLabel();
//        fitPlotCheckBox = new JCheckBox();
//        jLabel1 = new JLabel();
//        saveFitButton = new JButton();
//        saveFitPlotTextField = new JTextField();
//        personPanel = new JPanel();
//        latentDistributionPanel = new JPanel();
//        typeLabel = new JLabel();
//        typeComboBox = new JComboBox();
//        pointsLabel = new JLabel();
//        pointTextField = new JTextField();
//        minLabel = new JLabel();
//        minTextField = new JTextField();
//        maxLabel = new JLabel();
//        maxTextField = new JTextField();
//        groupByLabel = new JLabel();
//        groupByTextField = new JTextField();
//        selectGroupButton = new JButton();
//        distributionOutputLabel = new JLabel();
//        distributionOutputTextField = new JTextField();
//        baseGroupLabel = new JLabel();
//        baseGroupTextField = new JTextField();
//        personScoringTab = new JPanel();
//        personScorePanel = new JPanel();
//        scoreTypeLabel = new JLabel();
//        scoringcomboBox = new JComboBox();
//        minScoreLabel = new JLabel();
//        minScoreTextField = new JTextField();
//        maxScoreLabel = new JLabel();
//        maxScoreTextField = new JTextField();
//        residualLabel = new JLabel();
//        residualTextField = new JTextField();
//        meanScoreLabel = new JLabel();
//        meanTextField = new JTextField();
//        sdScoreLabel = new JLabel();
//        sdTextField = new JTextField();
//        pointsScoreLabel = new JLabel();
//        pointsScoreTextField = new JTextField();
//        maxIterScoreLabel = new JLabel();
//        maxIterScoreTextField = new JTextField();
//        toleranceScoreLabel = new JLabel();
//        toleranceScoreTextField = new JTextField();
//        variableNameScoreLabel = new JLabel();
//        variableNameScoreTextField = new JTextField();
//        optionsPanel = new JPanel();
//        convergencePanel = new JPanel();
//        toeranceLabel = new JLabel();
//        toleranceTextField = new JTextField();
//        maxIterationLabel = new JLabel();
//        maxIterationTextField = new JTextField();
//        missingDataPanel = new JPanel();
//        ignoreRadioButton = new JRadioButton();
//        scoreAsZeroRadioButton = new JRadioButton();
//        optimizerPanel = new JPanel();
//        uncminRadioButton = new JRadioButton();
//        bfgsRadioButton = new JRadioButton();
//        buttonPanel = new JPanel();
//        okButton = new JButton();
//        cancelButton = new JButton();
//
//        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
//
//        itemOptionPanel.setBorder(BorderFactory.createTitledBorder("Item Options"));
//
//        selectItemsButton.setText("Select Items");
//        selectItemsButton.setMaximumSize(new Dimension(125, 28));
//        selectItemsButton.setMinimumSize(new Dimension(125, 28));
//        selectItemsButton.setPreferredSize(new Dimension(125, 28));
//        selectItemsButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (wizzard == null) wizzard = new ItemGroupWizzard(IrtItemCalibrationDialog.this, variableListModel);
//                wizzard.setVisible(true);
//            }
//        });
//
//        fixedValuesButton.setText("Fixed Values");
//        fixedValuesButton.setEnabled(false);
//        fixedValuesButton.setMaximumSize(new Dimension(125, 28));
//        fixedValuesButton.setMinimumSize(new Dimension(125, 28));
//        fixedValuesButton.setPreferredSize(new Dimension(125, 28));
//
//        outputLabel.setText("Output table");
//
//        outputTextField.setMaximumSize(new Dimension(160, 28));
//        outputTextField.setMinimumSize(new Dimension(160, 28));
//        outputTextField.setPreferredSize(new Dimension(160, 28));
//
//        GroupLayout itemOptionPanelLayout = new GroupLayout(itemOptionPanel);
//        itemOptionPanel.setLayout(itemOptionPanelLayout);
//        itemOptionPanelLayout.setHorizontalGroup(
//                itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(itemOptionPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addGroup(itemOptionPanelLayout.createSequentialGroup()
//                                                .addComponent(selectItemsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                                .addGap(18, 18, 18)
//                                                .addComponent(outputLabel)
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addComponent(outputTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                        .addComponent(fixedValuesButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        itemOptionPanelLayout.setVerticalGroup(
//                itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(itemOptionPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(selectItemsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addGroup(itemOptionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                                .addComponent(outputLabel)
//                                                .addComponent(outputTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addComponent(fixedValuesButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        itemFitPanel.setBorder(BorderFactory.createTitledBorder("Item FIt"));
//
//        mincountTextField.setText("1");
//        mincountTextField.setMaximumSize(new Dimension(50, 28));
//        mincountTextField.setMinimumSize(new Dimension(50, 28));
//        mincountTextField.setPreferredSize(new Dimension(50, 28));
//
//        countLabel.setText("Min expected count");
//
//        fitPlotCheckBox.setText("Show fit plots");
//        fitPlotCheckBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (fitPlotCheckBox.isSelected()) {
//                    showFitPlots = true;
//                    saveFitButton.setEnabled(true);
//                    saveFitPlotTextField.setEnabled(true);
//                } else {
//                    showFitPlots = false;
//                    saveFitButton.setEnabled(false);
//                    saveFitPlotTextField.setEnabled(false);
//                }
//            }
//        });
//
//        jLabel1.setText("Save");
//
//        saveFitButton.setText("Browse");
//        saveFitButton.setMaximumSize(new Dimension(70, 28));
//        saveFitButton.setMinimumSize(new Dimension(70, 28));
//        saveFitButton.setPreferredSize(new Dimension(70, 28));
//        saveFitButton.setEnabled(false);
//        saveFitButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                JFileChooser fileChooser = new JFileChooser();
//                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//                fileChooser.setDialogTitle("Choose fit plot location");
//                if (fileChooser.showOpenDialog(IrtItemCalibrationDialog.this) == JFileChooser.APPROVE_OPTION) {
//                    saveFitPlotTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
//                }
//            }
//        });
//
//        saveFitPlotTextField.setMaximumSize(new Dimension(225, 28));
//        saveFitPlotTextField.setMinimumSize(new Dimension(225, 28));
//        saveFitPlotTextField.setPreferredSize(new Dimension(225, 28));
//        saveFitPlotTextField.setEnabled(false);
//
//        GroupLayout itemFitPanelLayout = new GroupLayout(itemFitPanel);
//        itemFitPanel.setLayout(itemFitPanelLayout);
//        itemFitPanelLayout.setHorizontalGroup(
//                itemFitPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(itemFitPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(itemFitPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addGroup(itemFitPanelLayout.createSequentialGroup()
//                                                .addComponent(countLabel)
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addComponent(mincountTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                        .addGroup(itemFitPanelLayout.createSequentialGroup()
//                                                .addComponent(fitPlotCheckBox)
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                                .addComponent(jLabel1)
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addComponent(saveFitPlotTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addComponent(saveFitButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        itemFitPanelLayout.setVerticalGroup(
//                itemFitPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(itemFitPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(itemFitPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(mincountTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addComponent(countLabel))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                .addGroup(itemFitPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(fitPlotCheckBox)
//                                        .addGroup(itemFitPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                                .addComponent(jLabel1)
//                                                .addComponent(saveFitButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                                .addComponent(saveFitPlotTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        GroupLayout itemPanelLayout = new GroupLayout(itemPanel);
//        itemPanel.setLayout(itemPanelLayout);
//        itemPanelLayout.setHorizontalGroup(
//                itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(itemPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
//                                        .addComponent(itemOptionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                        .addComponent(itemFitPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        itemPanelLayout.setVerticalGroup(
//                itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(itemPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addComponent(itemOptionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addComponent(itemFitPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        tabbedPane.addTab("Item Calibration", itemPanel);
//
//        latentDistributionPanel.setBorder(BorderFactory.createTitledBorder("Latent Distribution Options"));
//
//        typeLabel.setText("Type");
//
//        typeComboBox.setModel(new DefaultComboBoxModel(new String[]{"Normal", "Gauss-Hermite", "Estimate Nonparametric"}));
//        typeComboBox.setMaximumSize(new Dimension(125, 28));
//        typeComboBox.setMinimumSize(new Dimension(125, 28));
//        typeComboBox.setPreferredSize(new Dimension(125, 28));
//        typeComboBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String s = (String) typeComboBox.getSelectedItem();
//                if ("Gauss-Hermite".equals(s)) {
//                    pointTextField.setEnabled(true);
//                    minTextField.setEnabled(false);
//                    maxTextField.setEnabled(false);
//                } else {
//                    pointTextField.setEnabled(true);
//                    minTextField.setEnabled(true);
//                    maxTextField.setEnabled(true);
//                }
//            }
//        });
//
//        pointsLabel.setText("Points");
//
//        pointTextField.setText("40");
//        pointTextField.setMaximumSize(new Dimension(50, 28));
//        pointTextField.setMinimumSize(new Dimension(50, 28));
//        pointTextField.setPreferredSize(new Dimension(50, 28));
//
//        minLabel.setText("Min value");
//
//        minTextField.setText("-4.0");
//        minTextField.setMaximumSize(new Dimension(50, 28));
//        minTextField.setMinimumSize(new Dimension(50, 28));
//        minTextField.setPreferredSize(new Dimension(50, 28));
//
//        maxLabel.setText("Max value");
//
//        maxTextField.setText("4.0");
//        maxTextField.setMaximumSize(new Dimension(50, 28));
//        maxTextField.setMinimumSize(new Dimension(50, 28));
//        maxTextField.setPreferredSize(new Dimension(50, 28));
//
//        groupByLabel.setText("Group by");
//
//        groupByTextField.setEnabled(false);
//        groupByTextField.setMaximumSize(new Dimension(150, 28));
//        groupByTextField.setMinimumSize(new Dimension(150, 28));
//        groupByTextField.setPreferredSize(new Dimension(150, 28));
//
//        selectGroupButton.setText("Select");
//        selectGroupButton.setEnabled(false);
//        selectGroupButton.setMaximumSize(new Dimension(75, 28));
//        selectGroupButton.setMinimumSize(new Dimension(75, 28));
//        selectGroupButton.setPreferredSize(new Dimension(75, 28));
//
//        distributionOutputLabel.setText("Output table");
//
//        distributionOutputTextField.setMaximumSize(new Dimension(150, 28));
//        distributionOutputTextField.setMinimumSize(new Dimension(150, 28));
//        distributionOutputTextField.setPreferredSize(new Dimension(150, 28));
//
//        baseGroupLabel.setText("Base group");
//
//        baseGroupTextField.setEnabled(false);
//        baseGroupTextField.setMaximumSize(new Dimension(50, 28));
//        baseGroupTextField.setMinimumSize(new Dimension(50, 28));
//        baseGroupTextField.setPreferredSize(new Dimension(50, 28));
//
//        GroupLayout latentDistributionPanelLayout = new GroupLayout(latentDistributionPanel);
//        latentDistributionPanel.setLayout(latentDistributionPanelLayout);
//        latentDistributionPanelLayout.setHorizontalGroup(
//                latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(latentDistributionPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                                                .addGroup(latentDistributionPanelLayout.createSequentialGroup()
//                                                        .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
//                                                                .addGroup(GroupLayout.Alignment.LEADING, latentDistributionPanelLayout.createSequentialGroup()
//                                                                        .addGap(22, 22, 22)
//                                                                        .addComponent(typeLabel)
//                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                                        .addComponent(typeComboBox, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                                                                .addGroup(latentDistributionPanelLayout.createSequentialGroup()
//                                                                        .addComponent(minLabel)
//                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                                        .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                                                        .addComponent(maxLabel)
//                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                                        .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                                        .addComponent(pointsLabel)
//                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                        .addComponent(pointTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                                .addGroup(GroupLayout.Alignment.LEADING, latentDistributionPanelLayout.createSequentialGroup()
//                                                        .addComponent(distributionOutputLabel)
//                                                        .addGap(90, 90, 90)))
//                                        .addGroup(latentDistributionPanelLayout.createSequentialGroup()
//                                                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                                                        .addComponent(distributionOutputTextField, GroupLayout.PREFERRED_SIZE, 150, GroupLayout.PREFERRED_SIZE)
//                                                        .addGroup(GroupLayout.Alignment.LEADING, latentDistributionPanelLayout.createSequentialGroup()
//                                                                .addComponent(groupByLabel)
//                                                                .addGap(21, 21, 21)
//                                                                .addComponent(groupByTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addComponent(selectGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                                .addGap(18, 18, 18)
//                                                .addComponent(baseGroupLabel)
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addComponent(baseGroupTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                                .addContainerGap(14, Short.MAX_VALUE))
//        );
//        latentDistributionPanelLayout.setVerticalGroup(
//                latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(latentDistributionPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(typeLabel)
//                                        .addComponent(typeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addGap(11, 11, 11)
//                                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                                        .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                                .addComponent(pointsLabel)
//                                                .addComponent(pointTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                        .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                                .addComponent(maxLabel)
//                                                .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                        .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                                .addComponent(minLabel)
//                                                .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(groupByLabel)
//                                        .addComponent(groupByTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addComponent(selectGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                        .addComponent(baseGroupLabel)
//                                        .addComponent(baseGroupTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(distributionOutputLabel)
//                                        .addComponent(distributionOutputTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        GroupLayout personPanelLayout = new GroupLayout(personPanel);
//        personPanel.setLayout(personPanelLayout);
//        personPanelLayout.setHorizontalGroup(
//                personPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(personPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addComponent(latentDistributionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                .addContainerGap())
//        );
//        personPanelLayout.setVerticalGroup(
//                personPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(personPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addComponent(latentDistributionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                .addContainerGap(51, Short.MAX_VALUE))
//        );
//
//        tabbedPane.addTab("Latent Distribution", personPanel);
//
//        personScorePanel.setBorder(BorderFactory.createTitledBorder("Person Scoring Options"));
//
//        scoreTypeLabel.setText("Score type");
//
//        scoringcomboBox.setModel(new DefaultComboBoxModel(new String[]{"EAP", "MAP", "MLE"}));
//        scoringcomboBox.setMaximumSize(new Dimension(65, 28));
//        scoringcomboBox.setMinimumSize(new Dimension(65, 28));
//        scoringcomboBox.setPreferredSize(new Dimension(65, 28));
//        scoringcomboBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                String s = (String) scoringcomboBox.getSelectedItem();
//                if ("MLE".equals(s)) {
//                    meanTextField.setEnabled(false);
//                    sdTextField.setEnabled(false);
//                    toleranceScoreTextField.setEnabled(true);
//                    maxIterScoreTextField.setEnabled(true);
//                } else if ("MAP".equals(s)) {
//                    meanTextField.setEnabled(true);
//                    sdTextField.setEnabled(true);
//                    toleranceScoreTextField.setEnabled(true);
//                    maxIterScoreTextField.setEnabled(true);
//                } else {
//                    meanTextField.setEnabled(true);
//                    sdTextField.setEnabled(true);
//                    toleranceScoreTextField.setEnabled(false);
//                    maxIterScoreTextField.setEnabled(false);
//                }
//            }
//        });
//
//        minScoreLabel.setText("Min");
//
//        minScoreTextField.setText("-6.0");
//        minScoreTextField.setMaximumSize(new Dimension(50, 28));
//        minScoreTextField.setMinimumSize(new Dimension(50, 28));
//        minScoreTextField.setPreferredSize(new Dimension(50, 28));
//
//        maxScoreLabel.setText("Max");
//
//        maxScoreTextField.setText("6.0");
//        maxScoreTextField.setMaximumSize(new Dimension(50, 28));
//        maxScoreTextField.setMinimumSize(new Dimension(50, 28));
//        maxScoreTextField.setPreferredSize(new Dimension(50, 28));
//
//        residualLabel.setText("Residual table");
//
//        residualTextField.setMaximumSize(new Dimension(155, 28));
//        residualTextField.setMinimumSize(new Dimension(155, 28));
//        residualTextField.setPreferredSize(new Dimension(155, 28));
//        residualTextField.setEnabled(false);
//
//        meanScoreLabel.setText("Mean");
//
//        meanTextField.setText("0");
//        meanTextField.setMaximumSize(new Dimension(50, 28));
//        meanTextField.setMinimumSize(new Dimension(50, 28));
//        meanTextField.setPreferredSize(new Dimension(50, 28));
//
//        sdScoreLabel.setText("SD");
//
//        sdTextField.setText("1");
//        sdTextField.setMaximumSize(new Dimension(50, 28));
//        sdTextField.setMinimumSize(new Dimension(50, 28));
//        sdTextField.setPreferredSize(new Dimension(50, 28));
//
//        pointsScoreLabel.setText("Points");
//
//        pointsScoreTextField.setText("60");
//        pointsScoreTextField.setMaximumSize(new Dimension(50, 28));
//        pointsScoreTextField.setMinimumSize(new Dimension(50, 28));
//        pointsScoreTextField.setPreferredSize(new Dimension(50, 28));
//
//        maxIterScoreLabel.setText("Max iterations");
//
//        maxIterScoreTextField.setText("100");
//        maxIterScoreTextField.setEnabled(false);
//        maxIterScoreTextField.setMaximumSize(new Dimension(60, 28));
//        maxIterScoreTextField.setMinimumSize(new Dimension(60, 28));
//        maxIterScoreTextField.setPreferredSize(new Dimension(60, 28));
//
//        toleranceScoreLabel.setText("Tolerance");
//
//        toleranceScoreTextField.setText("0.00005");
//        toleranceScoreTextField.setEnabled(false);
//        toleranceScoreTextField.setMaximumSize(new Dimension(60, 28));
//        toleranceScoreTextField.setMinimumSize(new Dimension(60, 28));
//        toleranceScoreTextField.setPreferredSize(new Dimension(60, 28));
//
//        variableNameScoreLabel.setText("Variable name");
//
//        variableNameScoreTextField.setMaximumSize(new Dimension(155, 28));
//        variableNameScoreTextField.setMinimumSize(new Dimension(155, 28));
//        variableNameScoreTextField.setPreferredSize(new Dimension(155, 28));
//        variableNameScoreTextField.getDocument().addDocumentListener(new DocumentListener() {
//            @Override
//            public void insertUpdate(DocumentEvent e) {
//                if(variableNameScoreTextField.getText().trim().length()>0){
//                    residualTextField.setEnabled(true);
//                }else{
//                    residualTextField.setEnabled(false);
//                }
//            }
//
//            @Override
//            public void removeUpdate(DocumentEvent e) {
//                if(variableNameScoreTextField.getText().trim().length()>0){
//                    residualTextField.setEnabled(true);
//                }else{
//                    residualTextField.setEnabled(false);
//                }
//            }
//
//            @Override
//            public void changedUpdate(DocumentEvent e) {
//                if(variableNameScoreTextField.getText().trim().length()>0){
//                    residualTextField.setEnabled(true);
//                }else{
//                    residualTextField.setEnabled(false);
//                }
//            }
//        });
//
//        GroupLayout personScorePanelLayout = new GroupLayout(personScorePanel);
//        personScorePanel.setLayout(personScorePanelLayout);
//        personScorePanelLayout.setHorizontalGroup(
//            personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(personScorePanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(personScorePanelLayout.createSequentialGroup()
//                        .addComponent(residualLabel)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(residualTextField, GroupLayout.PREFERRED_SIZE, 185, GroupLayout.PREFERRED_SIZE))
//                    .addGroup(personScorePanelLayout.createSequentialGroup()
//                        .addComponent(variableNameScoreLabel)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(variableNameScoreTextField, GroupLayout.PREFERRED_SIZE, 186, GroupLayout.PREFERRED_SIZE))
//                    .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
//                        .addGroup(GroupLayout.Alignment.LEADING, personScorePanelLayout.createSequentialGroup()
//                            .addGap(25, 25, 25)
//                            .addComponent(scoreTypeLabel)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                            .addComponent(scoringcomboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                            .addComponent(maxIterScoreLabel)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                            .addComponent(maxIterScoreTextField, GroupLayout.PREFERRED_SIZE, 50, GroupLayout.PREFERRED_SIZE)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                            .addComponent(toleranceScoreLabel)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                            .addComponent(toleranceScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                        .addGroup(GroupLayout.Alignment.LEADING, personScorePanelLayout.createSequentialGroup()
//                            .addComponent(meanScoreLabel)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                            .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                            .addComponent(sdScoreLabel)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                            .addComponent(sdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                            .addComponent(minScoreLabel)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                            .addComponent(minScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                            .addComponent(maxScoreLabel)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                            .addComponent(maxScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                            .addComponent(pointsScoreLabel)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                            .addComponent(pointsScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
//                .addContainerGap(22, Short.MAX_VALUE))
//        );
//        personScorePanelLayout.setVerticalGroup(
//            personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(personScorePanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(variableNameScoreLabel)
//                    .addComponent(variableNameScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(scoreTypeLabel)
//                    .addComponent(scoringcomboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(maxIterScoreLabel)
//                    .addComponent(maxIterScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(toleranceScoreLabel)
//                    .addComponent(toleranceScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(meanScoreLabel)
//                    .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(sdScoreLabel)
//                    .addComponent(sdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(minScoreLabel)
//                    .addComponent(minScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(maxScoreLabel)
//                    .addComponent(maxScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(pointsScoreLabel)
//                    .addComponent(pointsScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addGap(18, 18, 18)
//                .addGroup(personScorePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(residualLabel)
//                    .addComponent(residualTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        GroupLayout personScoringTabLayout = new GroupLayout(personScoringTab);
//        personScoringTab.setLayout(personScoringTabLayout);
//        personScoringTabLayout.setHorizontalGroup(
//            personScoringTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(personScoringTabLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(personScorePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addContainerGap())
//        );
//        personScoringTabLayout.setVerticalGroup(
//            personScoringTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(personScoringTabLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(personScorePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addContainerGap(44, Short.MAX_VALUE))
//        );
//
//        tabbedPane.addTab("Person Scoring", personScoringTab);
//
//        convergencePanel.setBorder(BorderFactory.createTitledBorder("Convergence Criteria"));
//
//        toeranceLabel.setText("Tolerance");
//
//        toleranceTextField.setText("0.001");
//        toleranceTextField.setMaximumSize(new Dimension(75, 28));
//        toleranceTextField.setMinimumSize(new Dimension(75, 28));
//        toleranceTextField.setPreferredSize(new Dimension(75, 28));
//
//        maxIterationLabel.setText("Max iterations");
//
//        maxIterationTextField.setText("250");
//        maxIterationTextField.setMaximumSize(new Dimension(75, 28));
//        maxIterationTextField.setMinimumSize(new Dimension(75, 28));
//        maxIterationTextField.setPreferredSize(new Dimension(75, 28));
//
//        GroupLayout convergencePanelLayout = new GroupLayout(convergencePanel);
//        convergencePanel.setLayout(convergencePanelLayout);
//        convergencePanelLayout.setHorizontalGroup(
//            convergencePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(convergencePanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(convergencePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                    .addGroup(convergencePanelLayout.createSequentialGroup()
//                        .addComponent(toeranceLabel)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(toleranceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                    .addGroup(convergencePanelLayout.createSequentialGroup()
//                        .addComponent(maxIterationLabel)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(maxIterationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        convergencePanelLayout.setVerticalGroup(
//            convergencePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(convergencePanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(convergencePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(toeranceLabel)
//                    .addComponent(toleranceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(convergencePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(maxIterationLabel)
//                    .addComponent(maxIterationTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        missingDataPanel.setBorder(BorderFactory.createTitledBorder("Missing Data"));
//
//        missingDataButtonGroup.add(ignoreRadioButton);
//        ignoreRadioButton.setSelected(true);
//        ignoreRadioButton.setText("Ignore");
//        ignoreRadioButton.setActionCommand("ignore");
//
//        missingDataButtonGroup.add(scoreAsZeroRadioButton);
//        scoreAsZeroRadioButton.setText("Score as zero");
//        scoreAsZeroRadioButton.setActionCommand("zero");
//
//        GroupLayout missingDataPanelLayout = new GroupLayout(missingDataPanel);
//        missingDataPanel.setLayout(missingDataPanelLayout);
//        missingDataPanelLayout.setHorizontalGroup(
//            missingDataPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(missingDataPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(missingDataPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addComponent(ignoreRadioButton)
//                    .addComponent(scoreAsZeroRadioButton))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        missingDataPanelLayout.setVerticalGroup(
//            missingDataPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(missingDataPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(ignoreRadioButton)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                .addComponent(scoreAsZeroRadioButton)
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        optimizerPanel.setBorder(BorderFactory.createTitledBorder("Optimizer"));
//
//        optimizerButtonGroup.add(uncminRadioButton);
//        uncminRadioButton.setSelected(true);
//        uncminRadioButton.setText("UNCMIN");
//        uncminRadioButton.setActionCommand("uncmin");
//
//        optimizerButtonGroup.add(bfgsRadioButton);
//        bfgsRadioButton.setText("BFGS");
//        bfgsRadioButton.setActionCommand("bfgs");
//
//        GroupLayout optimizerPanelLayout = new GroupLayout(optimizerPanel);
//        optimizerPanel.setLayout(optimizerPanelLayout);
//        optimizerPanelLayout.setHorizontalGroup(
//            optimizerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(optimizerPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(optimizerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addComponent(uncminRadioButton)
//                    .addComponent(bfgsRadioButton))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        optimizerPanelLayout.setVerticalGroup(
//            optimizerPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(optimizerPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(uncminRadioButton)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                .addComponent(bfgsRadioButton)
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        GroupLayout optionsPanelLayout = new GroupLayout(optionsPanel);
//        optionsPanel.setLayout(optionsPanelLayout);
//        optionsPanelLayout.setHorizontalGroup(
//            optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(optionsPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(convergencePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(missingDataPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(optimizerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addContainerGap(73, Short.MAX_VALUE))
//        );
//        optionsPanelLayout.setVerticalGroup(
//            optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(optionsPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
//                    .addComponent(optimizerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                    .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
//                        .addComponent(missingDataPanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                        .addComponent(convergencePanel, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
//                .addContainerGap(134, Short.MAX_VALUE))
//        );
//
//        tabbedPane.addTab("Options", optionsPanel);
//
//        buttonPanel.setMinimumSize(new Dimension(471, 40));
//        buttonPanel.setPreferredSize(new Dimension(471, 40));
//        buttonPanel.setLayout(new GridBagLayout());
//
//        okButton.setText("OK");
//        okButton.setMaximumSize(new Dimension(72, 28));
//        okButton.setMinimumSize(new Dimension(72, 28));
//        okButton.setPreferredSize(new Dimension(72, 28));
//        okButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                setCommand();
////                System.out.println(command.paste());
//                setVisible(false);
//            }
//        });
//
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 0;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
//        gridBagConstraints.weightx = 0.5;
//        gridBagConstraints.insets = new Insets(11, 192, 11, 0);
//        buttonPanel.add(okButton, gridBagConstraints);
//
//        cancelButton.setText("Cancel");
//        cancelButton.setMaximumSize(new Dimension(72, 28));
//        cancelButton.setMinimumSize(new Dimension(72, 28));
//        cancelButton.setPreferredSize(new Dimension(72, 28));
//        cancelButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                canRun = false;
//                setVisible(false);
//            }
//        });
//
//        gridBagConstraints = new GridBagConstraints();
//        gridBagConstraints.gridx = 1;
//        gridBagConstraints.gridy = 0;
//        gridBagConstraints.anchor = GridBagConstraints.NORTHWEST;
//        gridBagConstraints.insets = new Insets(11, 6, 11, 134);
//        buttonPanel.add(cancelButton, gridBagConstraints);
//
//        GroupLayout layout = new GroupLayout(getContentPane());
//        getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addComponent(tabbedPane)
//                    .addGroup(layout.createSequentialGroup()
//                        .addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                        .addGap(0, 0, Short.MAX_VALUE)))
//                .addContainerGap())
//        );
//        layout.setVerticalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(tabbedPane, GroupLayout.PREFERRED_SIZE, 280, GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addContainerGap())
//        );
//
//        pack();
//    }

    public boolean canRun(){
        return canRun;
    }

    private void setCommand(){

        command = new IrtItemCalibrationCommand();
        wizzard.updateCommand(command);

        MegaOption option = null;

        //Data information
        option = command.getOption("data");
        option.addValueAt("db", dbName.toString());
        option.addValueAt("table", tableName.toString());

        //Convergence information
        option = command.getOption("converge");
        double tol = 0.001;
        try{
            tol = Double.parseDouble(toleranceTextField.getText().trim());
        }catch(NumberFormatException ex){
            tol = 0.001;
        }
        option.addValueAt("tol", Double.valueOf(tol).toString());

        int maxIter = 250;
        try{
            maxIter = Integer.parseInt(maxIterationTextField.getText().trim());
        }catch(NumberFormatException ex){
            maxIter = 250;
        }
        option.addValueAt("maxiter", Integer.valueOf(maxIter).toString());

        //Optimizer
        option = command.getOption("optim");
        option.addValue(optimizerButtonGroup.getSelection().getActionCommand());

        //Missing data
        option = command.getOption("missing");
        option.addValue(missingDataButtonGroup.getSelection().getActionCommand());

        //Item fit
        option = command.getOption("itemfit");
        String t = mincountTextField.getText().trim();
        if("".equals(t)) {
            option.addValueAt("mincell", "1");
        }else{
            option.addValueAt("mincell", t);
        }

        //Save  output
        int outputCount = 0;
        DataTableName distribution = null;
        String temp = distributionOutputTextField.getText().trim();
        if(!"".equals(temp)){
            distribution = new DataTableName(temp);
            outputCount++;
        }

        DataTableName itemOutput = null;
        temp = outputTextField.getText().trim();
        if(!"".equals(temp)){
            itemOutput = new DataTableName(temp);
            outputCount++;
        }

        DataTableName residualOutput = null;
        temp = residualTextField.getText().trim();
        if(!"".equals(temp)){
            residualOutput = new DataTableName(temp);
            outputCount++;
        }

        if(outputCount>0){
            option = command.getOption("output");
            option.addValueAt("db", dbName.toString());
            if(itemOutput!=null) option.addValueAt("item", itemOutput.toString());
            if(distribution!=null) option.addValueAt("latent", distribution.toString());
            if(residualOutput!=null) option.addValueAt("residual", residualOutput.toString());
        }

        //Latent distribution
        option = command.getOption("latent");
        option.addValueAt("name", latentDistributionCode[typeComboBox.getSelectedIndex()]);

        double min = -4.0;
        try{
            min = Double.parseDouble(minTextField.getText().trim());
        }catch(NumberFormatException ex){
            min = -4.0;
        }
        option.addValueAt("min", Double.valueOf(min).toString());

        double max = 4.0;
        try{
            max = Double.parseDouble(maxTextField.getText().trim());
        }catch(NumberFormatException ex){
            max = 4.0;
        }
        option.addValueAt("max", Double.valueOf(max).toString());

        int points = 40;
        try{
            points = Integer.parseInt(pointTextField.getText().trim());
        }catch(NumberFormatException ex){
            points = 40;
        }
        option.addValueAt("points", Integer.valueOf(points).toString());

        //Number of item groups
        option = command.getOption("groups");
        option.addValue(Integer.valueOf(wizzard.getNumberOfGroups()).toString());

        //Person scoring
        temp = variableNameScoreTextField.getText().trim();
        String scoreType = "";
        if(!"".equals(temp)){
            option = command.getOption("scoring");
            option.addValueAt("name", temp);
            scoreType = (String)scoringcomboBox.getSelectedItem();
            option.addValueAt("type", scoreType);

            if("MLE".equals(scoreType)){
                option.addValueAt("min", minScoreTextField.getText().trim());
                option.addValueAt("max", maxScoreTextField.getText().trim());
                option.addValueAt("tol", toleranceScoreTextField.getText().trim());
                option.addValueAt("maxiter", maxIterScoreTextField.getText().trim());
            }else if("MAP".equals(scoreType)){
                option.addValueAt("mean", meanTextField.getText().trim());
                option.addValueAt("sd", sdTextField.getText().trim());
                option.addValueAt("min", minScoreTextField.getText().trim());
                option.addValueAt("max", maxScoreTextField.getText().trim());
                option.addValueAt("tol", toleranceScoreTextField.getText().trim());
                option.addValueAt("maxiter", maxIterScoreTextField.getText().trim());
            }else{
                //MAP
                option.addValueAt("mean", meanTextField.getText().trim());
                option.addValueAt("sd", sdTextField.getText().trim());
                option.addValueAt("min", minScoreTextField.getText().trim());
                option.addValueAt("max", maxScoreTextField.getText().trim());
                option.addValueAt("points", pointsScoreTextField.getText().trim());
            }
        }

        //For debugging
//        System.out.println(command.paste());

        //Fixed values
        //TODO add fixed values from database informaiton


        canRun = true;//TODO check for required arguments
    }

    public Command getCommand(){
        return command;
    }

    public VariableChangeListener getVariableChangedListener(){
        return new VariableChangeListener() {
            @Override
            public void variableChanged(VariableChangeEvent evt) {
                //TODO modify itemList
            }
        };
    }


}

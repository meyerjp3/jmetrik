package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.commandbuilder.MegaOption;
import com.itemanalysis.jmetrik.model.VariableListModel;
import com.itemanalysis.psychometrics.data.VariableInfo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;

public class ItemGroupConfigurationPanel extends JPanel{

    // Variables declaration - do not modify
    private JCheckBox difficultyFixedCheckBox;
    private JButton difficultyPriorButton;
    private JLabel difficultyPriorLabel;
    private JTextField difficultyPriorTextField;
    private JLabel difficultyStartLabel;
    private JTextField difficultyStartTextField;
    private JCheckBox discriminationFixedCheckBox;
    private JButton discriminationPriorButton;
    private JLabel discriminationPriorLabel;
    private JTextField discriminationPriorTextField;
    private JLabel discriminationStartLabel;
    private JTextField discriminationStartTextField;
    private JCheckBox guessingFixedCheckBox;
    private JButton guessingPriorButton;
    private JLabel guessingPriorLabel;
    private JTextField guessingPriorTextField;
    private JLabel guessingStartLabel;
    private JTextField guessingStartTextField;
    private JList<VariableInfo> itemList;
    private JScrollPane itemScrollPane;
    private JLabel modelLabel;
    private JPanel modelPanel;
    private JComboBox modelcomboBox;
    private JPanel priorPanel;
    private JComboBox scaleComboBox;
    private JLabel scalingLabel;
    private JCheckBox slippingFixedCheckBox;
    private JButton slippingPriorButton;
    private JLabel slippingPriorLabel;
    private JTextField slippingPriorTextField;
    private JLabel slippingStartLabel;
    private JTextField slippingStartTextField;
    private JPanel startPanel;
    private JCheckBox steFixedCheckBox;
    private JTextField steStartTextField;
    private JButton stepPriorButton;
    private JLabel stepPriorLabel;
    private JTextField stepPriorTextField;
    private JLabel stepStartLabel;
    // End of variables declaration



    //Order of entries must match those in itemResponseModelCodes
    private final String[] itemResponseModelStringArray = new String[] {
                "4 Parameter Logistic Model (L4)",
                "3 Parameter Logistic Model (L3)",
                "2 Parameter Logistic Model (L2)",
                "Rasch Model (L1)",
                "Generalized Partial Credit Mode (PC1)",
                "Partial Credit Model (PC4)"
    };

    //Order of entry must match those in itemResponseModelStringArray
    private final String[] itemResponseModelCodes = new String[] {
                "L4",
                "L3",
                "L2",
                "L1",
                "PC1",
                "PC4"
    };

    private final String[] scalingText = {"Logistic (D = 1.0)", "Normal (D = 1.7)"};

    private JDialog parent = null;
    private VariableListModel variableListModel = null;
    private ArrayList<VariableInfo> selectedVariables = null;
    private int ncat = 0;
    private double scalingConstant = 1.0;
    private boolean fixedDiscrimination = false;
    private boolean fixedDifficulty = false;
    private boolean fixedGuessing = false;
    private boolean fixedSlipping = false;
    private boolean fixedStep = false;

    private ArrayList<VariableInfo> selectedVariableMasterList = null;

    public ItemGroupConfigurationPanel(JDialog parent, VariableListModel variableListModel, ArrayList<VariableInfo> selectedVariableMasterList){
        this.parent = parent;
        this.variableListModel = variableListModel;
        this.selectedVariableMasterList = selectedVariableMasterList;
        initComponents();
        selectedVariables = new ArrayList<VariableInfo>();
    }


    private void initComponents() {

        itemScrollPane = new JScrollPane();
        itemList = new JList<VariableInfo>();
        priorPanel = new JPanel();
        discriminationPriorTextField = new JTextField();
        discriminationPriorLabel = new JLabel();
        discriminationPriorButton = new JButton();
        difficultyPriorLabel = new JLabel();
        difficultyPriorTextField = new JTextField();
        difficultyPriorButton = new JButton();
        guessingPriorLabel = new JLabel();
        guessingPriorTextField = new JTextField();
        guessingPriorButton = new JButton();
        slippingPriorLabel = new JLabel();
        slippingPriorTextField = new JTextField();
        slippingPriorButton = new JButton();
        stepPriorLabel = new JLabel();
        stepPriorTextField = new JTextField();
        stepPriorButton = new JButton();
        startPanel = new JPanel();
        discriminationStartLabel = new JLabel();
        discriminationStartTextField = new JTextField();
        discriminationFixedCheckBox = new JCheckBox();
        difficultyStartLabel = new JLabel();
        difficultyStartTextField = new JTextField();
        difficultyFixedCheckBox = new JCheckBox();
        guessingStartLabel = new JLabel();
        guessingStartTextField = new JTextField();
        guessingFixedCheckBox = new JCheckBox();
        slippingStartLabel = new JLabel();
        slippingStartTextField = new JTextField();
        slippingFixedCheckBox = new JCheckBox();
        stepStartLabel = new JLabel();
        steStartTextField = new JTextField();
        steFixedCheckBox = new JCheckBox();
        modelPanel = new JPanel();
        modelLabel = new JLabel();
        modelcomboBox = new JComboBox();
        scalingLabel = new JLabel();
        scaleComboBox = new JComboBox();

        setMaximumSize(new Dimension(760, 310));
        setMinimumSize(new Dimension(760, 310));
        setName("");
        setPreferredSize(new Dimension(760, 310));

        itemScrollPane.setBorder(BorderFactory.createTitledBorder(""));
        itemScrollPane.setMinimumSize(new Dimension(137, 281));
        itemScrollPane.setPreferredSize(new Dimension(137, 281));

        itemList.setModel(variableListModel);
        itemList.setCellRenderer(new SelectedItemListCellRenderer());
        itemList.setLayoutOrientation(JList.VERTICAL);
        itemScrollPane.setViewportView(itemList);


        priorPanel.setBorder(BorderFactory.createTitledBorder("Priors"));
        priorPanel.setMinimumSize(new Dimension(300, 215));

        discriminationPriorTextField.setMaximumSize(new Dimension(100, 25));
        discriminationPriorTextField.setMinimumSize(new Dimension(100, 25));
        discriminationPriorTextField.setPreferredSize(new Dimension(100, 28));

        discriminationPriorLabel.setText("Discrimination");

        discriminationPriorButton.setText("Choose");
        discriminationPriorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IrtItemPriorDialog priorDialog = new IrtItemPriorDialog(
                        parent,
                        discriminationPriorTextField.getText().trim());
                priorDialog.setVisible(true);

                discriminationPriorTextField.setText(priorDialog.getPriorString());
                discriminationPriorTextField.setCaretPosition(0);

            }
        });

        difficultyPriorLabel.setText("Difficulty");

        difficultyPriorTextField.setMaximumSize(new Dimension(100, 25));
        difficultyPriorTextField.setMinimumSize(new Dimension(100, 25));
        difficultyPriorTextField.setPreferredSize(new Dimension(100, 28));

        difficultyPriorButton.setText("Choose");
        difficultyPriorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IrtItemPriorDialog priorDialog = new IrtItemPriorDialog(
                        parent,
                        difficultyPriorTextField.getText().trim());
                priorDialog.setVisible(true);

                difficultyPriorTextField.setText(priorDialog.getPriorString());
                difficultyPriorTextField.setCaretPosition(0);

            }
        });

        guessingPriorLabel.setText("Guessing");

        guessingPriorTextField.setMaximumSize(new Dimension(100, 25));
        guessingPriorTextField.setMinimumSize(new Dimension(100, 25));
        guessingPriorTextField.setPreferredSize(new Dimension(100, 28));

        guessingPriorButton.setText("Choose");
        guessingPriorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IrtItemPriorDialog priorDialog = new IrtItemPriorDialog(
                        parent,
                        guessingPriorTextField.getText().trim());
                priorDialog.setVisible(true);

                guessingPriorTextField.setText(priorDialog.getPriorString());
                guessingPriorTextField.setCaretPosition(0);

            }
        });

        slippingPriorLabel.setText("Slipping");

        slippingPriorTextField.setMaximumSize(new Dimension(100, 25));
        slippingPriorTextField.setMinimumSize(new Dimension(100, 25));
        slippingPriorTextField.setPreferredSize(new Dimension(100, 28));
        slippingPriorTextField.setEnabled(false);

        slippingPriorButton.setText("Choose");
        slippingPriorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IrtItemPriorDialog priorDialog = new IrtItemPriorDialog(
                        parent,
                        slippingPriorTextField.getText().trim());
                priorDialog.setVisible(true);

                slippingPriorTextField.setText(priorDialog.getPriorString());
                slippingPriorTextField.setCaretPosition(0);

            }
        });

        stepPriorLabel.setText("Step/Threshold");

        stepPriorTextField.setMaximumSize(new Dimension(100, 25));
        stepPriorTextField.setMinimumSize(new Dimension(100, 25));
        stepPriorTextField.setPreferredSize(new Dimension(100, 28));
        stepPriorTextField.setEnabled(false);

        stepPriorButton.setText("Choose");
        stepPriorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IrtItemPriorDialog priorDialog = new IrtItemPriorDialog(
                        parent,
                        stepPriorTextField.getText().trim());
                priorDialog.setVisible(true);

                stepPriorTextField.setText(priorDialog.getPriorString());
                stepPriorTextField.setCaretPosition(0);

            }
        });

        GroupLayout priorPanelLayout = new GroupLayout(priorPanel);
        priorPanel.setLayout(priorPanelLayout);
        priorPanelLayout.setHorizontalGroup(
            priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(priorPanelLayout.createSequentialGroup()
                .addContainerGap(26, Short.MAX_VALUE)
                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(GroupLayout.Alignment.TRAILING, priorPanelLayout.createSequentialGroup()
                        .addComponent(discriminationPriorLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(discriminationPriorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(discriminationPriorButton))
                    .addGroup(GroupLayout.Alignment.TRAILING, priorPanelLayout.createSequentialGroup()
                        .addComponent(difficultyPriorLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(difficultyPriorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(difficultyPriorButton))
                    .addGroup(GroupLayout.Alignment.TRAILING, priorPanelLayout.createSequentialGroup()
                        .addComponent(guessingPriorLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guessingPriorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guessingPriorButton))
                    .addGroup(GroupLayout.Alignment.TRAILING, priorPanelLayout.createSequentialGroup()
                        .addComponent(slippingPriorLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(slippingPriorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(slippingPriorButton))
                    .addGroup(GroupLayout.Alignment.TRAILING, priorPanelLayout.createSequentialGroup()
                        .addComponent(stepPriorLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stepPriorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(stepPriorButton)))
                .addContainerGap())
        );
        priorPanelLayout.setVerticalGroup(
            priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(priorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(discriminationPriorLabel)
                    .addComponent(discriminationPriorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(discriminationPriorButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(difficultyPriorLabel)
                    .addComponent(difficultyPriorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(difficultyPriorButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(guessingPriorLabel)
                    .addComponent(guessingPriorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(guessingPriorButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(slippingPriorLabel)
                    .addComponent(slippingPriorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(slippingPriorButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(stepPriorLabel)
                    .addComponent(stepPriorTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(stepPriorButton))
                .addContainerGap(17, Short.MAX_VALUE))
        );

        startPanel.setBorder(BorderFactory.createTitledBorder("Start Values"));
        startPanel.setMinimumSize(new Dimension(290, 215));
        startPanel.setPreferredSize(new Dimension(290, 215));

        discriminationStartLabel.setText("Discrimination");
        discriminationStartLabel.setToolTipText("");

        discriminationStartTextField.setText("");
        discriminationStartTextField.setMaximumSize(new Dimension(100, 25));
        discriminationStartTextField.setMinimumSize(new Dimension(100, 25));
        discriminationStartTextField.setPreferredSize(new Dimension(100, 28));

        discriminationFixedCheckBox.setText("Fixed");
        discriminationFixedCheckBox.setToolTipText("");
        discriminationFixedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(discriminationFixedCheckBox.isSelected()){
                    fixedDiscrimination = true;
                }else{
                    fixedDiscrimination = false;
                }
            }
        });

        difficultyStartLabel.setText("Difficulty");
        difficultyStartLabel.setToolTipText("");

        difficultyStartTextField.setText("");
        difficultyStartTextField.setMaximumSize(new Dimension(100, 25));
        difficultyStartTextField.setMinimumSize(new Dimension(100, 25));
        difficultyStartTextField.setPreferredSize(new Dimension(100, 28));

        difficultyFixedCheckBox.setText("Fixed");
        difficultyFixedCheckBox.setToolTipText("");
        difficultyFixedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(difficultyFixedCheckBox.isSelected()){
                    fixedDifficulty = true;
                }else{
                    fixedDifficulty = false;
                }
            }
        });

        guessingStartLabel.setText("Guessing");
        guessingStartLabel.setToolTipText("");

        guessingStartTextField.setText("");
        guessingStartTextField.setMaximumSize(new Dimension(100, 25));
        guessingStartTextField.setMinimumSize(new Dimension(100, 25));
        guessingStartTextField.setPreferredSize(new Dimension(100, 28));

        guessingFixedCheckBox.setText("Fixed");
        guessingFixedCheckBox.setToolTipText("");
        guessingFixedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(guessingFixedCheckBox.isSelected()){
                    fixedGuessing = true;
                }else{
                    fixedGuessing = false;
                }
            }
        });

        slippingStartLabel.setText("Slipping");
        slippingStartLabel.setToolTipText("");
        slippingFixedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(slippingFixedCheckBox.isSelected()){
                    fixedSlipping = true;
                }else{
                    fixedSlipping = false;
                }
            }
        });

        slippingStartTextField.setText("");
        slippingStartTextField.setMaximumSize(new Dimension(100, 25));
        slippingStartTextField.setMinimumSize(new Dimension(100, 25));
        slippingStartTextField.setPreferredSize(new Dimension(100, 28));

        slippingFixedCheckBox.setText("Fixed");
        slippingFixedCheckBox.setToolTipText("");

        stepStartLabel.setText("Step/Threshold");
        stepStartLabel.setToolTipText("");

        steStartTextField.setText("");
        steStartTextField.setMaximumSize(new Dimension(100, 25));
        steStartTextField.setMinimumSize(new Dimension(100, 25));
        steStartTextField.setPreferredSize(new Dimension(100, 28));
        steStartTextField.setEnabled(false);

        steFixedCheckBox.setText("Fixed");
        steFixedCheckBox.setToolTipText("");
        steFixedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(steFixedCheckBox.isSelected()){
                    fixedStep = true;
                }else{
                    fixedStep = false;
                }
            }
        });

        GroupLayout startPanelLayout = new GroupLayout(startPanel);
        startPanel.setLayout(startPanelLayout);
        startPanelLayout.setHorizontalGroup(
            startPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(startPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(startPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(startPanelLayout.createSequentialGroup()
                        .addComponent(discriminationStartLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(discriminationStartTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(discriminationFixedCheckBox))
                    .addGroup(startPanelLayout.createSequentialGroup()
                        .addComponent(difficultyStartLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(difficultyStartTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(difficultyFixedCheckBox))
                    .addGroup(startPanelLayout.createSequentialGroup()
                        .addComponent(slippingStartLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(slippingStartTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(slippingFixedCheckBox))
                    .addGroup(startPanelLayout.createSequentialGroup()
                        .addComponent(guessingStartLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guessingStartTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(guessingFixedCheckBox))
                    .addGroup(startPanelLayout.createSequentialGroup()
                        .addComponent(stepStartLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(steStartTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(steFixedCheckBox)))
                .addContainerGap(38, Short.MAX_VALUE))
        );
        startPanelLayout.setVerticalGroup(
            startPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(startPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(startPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(discriminationStartLabel)
                    .addComponent(discriminationStartTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(discriminationFixedCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(startPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(difficultyStartLabel)
                    .addComponent(difficultyStartTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(difficultyFixedCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(startPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(guessingStartLabel)
                    .addComponent(guessingStartTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(guessingFixedCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(startPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(slippingStartLabel)
                    .addComponent(slippingStartTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(slippingFixedCheckBox))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(startPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(stepStartLabel)
                    .addComponent(steStartTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(steFixedCheckBox))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        modelPanel.setBorder(BorderFactory.createTitledBorder(""));
        modelPanel.setPreferredSize(new Dimension(606, 60));

        modelLabel.setText("Model");

        modelcomboBox.setModel(new DefaultComboBoxModel(itemResponseModelStringArray));
        modelcomboBox.setSelectedItem(itemResponseModelStringArray[1]);
        modelcomboBox.setMinimumSize(new Dimension(214, 28));
        modelcomboBox.setPreferredSize(new Dimension(214, 28));
        modelcomboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox)e.getSource();
                String selectedItem = (String)comboBox.getSelectedItem();
                if(itemResponseModelStringArray[0].equals(selectedItem)){
                    discriminationPriorTextField.setEnabled(true);
                    difficultyPriorTextField.setEnabled(true);
                    guessingPriorTextField.setEnabled(true);
                    slippingPriorTextField.setEnabled(true);
                    stepPriorTextField.setEnabled(false);
                    steStartTextField.setEnabled(false);
                    difficultyStartTextField.setEnabled(true);
                    guessingStartTextField.setEnabled(true);
                    slippingStartTextField.setEnabled(true);


                    discriminationPriorButton.setEnabled(true);
                    difficultyPriorButton.setEnabled(true);
                    guessingPriorButton.setEnabled(true);
                    slippingPriorButton.setEnabled(true);
                    stepPriorButton.setEnabled(false);

                }else if(itemResponseModelStringArray[1].equals(selectedItem)){
                    discriminationPriorTextField.setEnabled(true);
                    difficultyPriorTextField.setEnabled(true);
                    guessingPriorTextField.setEnabled(true);
                    slippingPriorTextField.setEnabled(false);
                    stepPriorTextField.setEnabled(false);
                    steStartTextField.setEnabled(false);
                    difficultyStartTextField.setEnabled(true);
                    guessingStartTextField.setEnabled(true);
                    slippingStartTextField.setEnabled(true);

                    discriminationPriorButton.setEnabled(true);
                    difficultyPriorButton.setEnabled(true);
                    guessingPriorButton.setEnabled(true);
                    slippingPriorButton.setEnabled(false);
                    stepPriorButton.setEnabled(false);

                }else if(itemResponseModelStringArray[2].equals(selectedItem)){
                    discriminationPriorTextField.setEnabled(true);
                    difficultyPriorTextField.setEnabled(true);
                    guessingPriorTextField.setEnabled(false);
                    slippingPriorTextField.setEnabled(false);
                    stepPriorTextField.setEnabled(false);
                    steStartTextField.setEnabled(false);
                    difficultyStartTextField.setEnabled(true);
                    guessingStartTextField.setEnabled(true);
                    slippingStartTextField.setEnabled(true);

                    discriminationPriorButton.setEnabled(true);
                    difficultyPriorButton.setEnabled(true);
                    guessingPriorButton.setEnabled(false);
                    slippingPriorButton.setEnabled(false);
                    stepPriorButton.setEnabled(false);

                }else if(itemResponseModelStringArray[3].equals(selectedItem)){
                    discriminationPriorTextField.setEnabled(false);
                    difficultyPriorTextField.setEnabled(true);
                    guessingPriorTextField.setEnabled(false);
                    slippingPriorTextField.setEnabled(false);
                    stepPriorTextField.setEnabled(false);
                    steStartTextField.setEnabled(false);
                    difficultyStartTextField.setEnabled(true);
                    guessingStartTextField.setEnabled(true);
                    slippingStartTextField.setEnabled(true);

                    discriminationPriorButton.setEnabled(false);
                    difficultyPriorButton.setEnabled(true);
                    guessingPriorButton.setEnabled(false);
                    slippingPriorButton.setEnabled(false);
                    stepPriorButton.setEnabled(false);

                }else if(itemResponseModelStringArray[4].equals(selectedItem)){
                    discriminationPriorTextField.setEnabled(true);
                    difficultyPriorTextField.setEnabled(false);
                    guessingPriorTextField.setEnabled(false);
                    slippingPriorTextField.setEnabled(false);
                    stepPriorTextField.setEnabled(true);
                    steStartTextField.setEnabled(true);
                    difficultyStartTextField.setEnabled(false);
                    guessingStartTextField.setEnabled(false);
                    slippingStartTextField.setEnabled(false);

                    discriminationPriorButton.setEnabled(true);
                    difficultyPriorButton.setEnabled(false);
                    guessingPriorButton.setEnabled(false);
                    slippingPriorButton.setEnabled(false);
                    stepPriorButton.setEnabled(true);

                }else if(itemResponseModelStringArray[5].equals(selectedItem)){
                    discriminationPriorTextField.setEnabled(false);
                    difficultyPriorTextField.setEnabled(false);
                    guessingPriorTextField.setEnabled(false);
                    slippingPriorTextField.setEnabled(false);
                    stepPriorTextField.setEnabled(true);
                    steStartTextField.setEnabled(true);
                    difficultyStartTextField.setEnabled(false);
                    guessingStartTextField.setEnabled(false);
                    slippingStartTextField.setEnabled(false);

                    discriminationPriorButton.setEnabled(false);
                    difficultyPriorButton.setEnabled(false);
                    guessingPriorButton.setEnabled(false);
                    slippingPriorButton.setEnabled(false);
                    stepPriorButton.setEnabled(true);

                }
            }
        });

        scalingLabel.setText("Scaling constant");

        scaleComboBox.setModel(new DefaultComboBoxModel(scalingText));
        scaleComboBox.setSelectedItem(scalingText);
        scaleComboBox.setMinimumSize(new Dimension(150, 28));
        scaleComboBox.setPreferredSize(new Dimension(150, 28));
        scaleComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox)e.getSource();
                String selectedItem = (String)comboBox.getSelectedItem();

                if(scalingText[0].equals(selectedItem)){
                    scalingConstant = 1.0;
                }else{
                    scalingConstant = 1.7;
                }

            }
        });

        GroupLayout modelPanelLayout = new GroupLayout(modelPanel);
        modelPanel.setLayout(modelPanelLayout);
        modelPanelLayout.setHorizontalGroup(
            modelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(modelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(modelLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(modelcomboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(16, 16, 16)
                .addComponent(scalingLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scaleComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        modelPanelLayout.setVerticalGroup(
            modelPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(modelPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(modelPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(modelLabel)
                    .addComponent(modelcomboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(scalingLabel)
                    .addComponent(scaleComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(itemScrollPane, GroupLayout.PREFERRED_SIZE, 137, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(priorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(modelPanel, GroupLayout.DEFAULT_SIZE, 596, Short.MAX_VALUE))
                .addContainerGap(36, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(itemScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(modelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(priorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(startPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(18, Short.MAX_VALUE))
        );
    }// </editor-fold>

    public boolean hasSameNumberOfCategories(){
        int[] selectedIndices = itemList.getSelectedIndices();

        VariableInfo v = null;
        for(int i=0;i<selectedIndices.length;i++){
            v = variableListModel.getElementAt(selectedIndices[i]);
            if(i==0){
                ncat = v.getItemScoring().numberOfScoreLevels();
            }else{
                if(ncat!=v.getItemScoring().numberOfScoreLevels()){
                    JOptionPane.showMessageDialog(parent,
                            "Different item scores found. All items \n" +
                            "in the group must have the same scoring.",
                            "Item Selection Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<VariableInfo> getSelectedVariables(){
        return selectedVariables;
    }

    public void resetPanel(){
        for(VariableInfo v : selectedVariables){
            if(selectedVariableMasterList.contains(v)) selectedVariableMasterList.remove(v);
        }

        itemList.clearSelection();
        selectedVariables.clear();
        modelcomboBox.setSelectedItem(itemResponseModelStringArray[1]);
        scaleComboBox.setSelectedItem(scalingText[0]);
        discriminationPriorTextField.setText("");
        difficultyPriorTextField.setText("");
        guessingPriorTextField.setText("");
        slippingPriorTextField.setText("");
        stepPriorTextField.setText("");
        slippingPriorTextField.setEnabled(false);
        stepPriorTextField.setEnabled(false);

        discriminationStartTextField.setText("");
        difficultyStartTextField.setText("");
        guessingStartTextField.setText("");
        slippingStartTextField.setText("");
        steStartTextField.setText("");
        steStartTextField.setEnabled(false);

        discriminationFixedCheckBox.setSelected(false);
        difficultyFixedCheckBox.setSelected(false);
        guessingFixedCheckBox.setSelected(false);
        slippingFixedCheckBox.setSelected(false);
        steFixedCheckBox.setSelected(false);


    }

    public JList getItemList(){
        return itemList;
    }

//    public void setSelectedVariables(){
//        int[] selectedIndices = itemList.getSelectedIndices();
//        VariableInfo v = null;
//        for(int i = 0; i<selectedIndices.length;i++){
//            v = variableListModel.getElementAt(selectedIndices[i]);
//            selectedVariables.add(v);
//
//            if(!selectedVariableMasterList.contains(v)){
//                selectedVariableMasterList.add(v);
//            }
//
//        }
//    }

    public boolean setOption(MegaOption option){

        //Get selected items
        int[] selectedIndices = itemList.getSelectedIndices();

        if(selectedIndices.length==0) return false;//No items selected therefore no option to set

        //Set selected items (required)
        VariableInfo v = null;
        for(int i = 0; i<selectedIndices.length;i++){
            v = variableListModel.getElementAt(selectedIndices[i]);
            if(i==0) ncat = v.getItemScoring().numberOfScoreLevels();//number of categories set by first item in group
            selectedVariables.add(v);
            option.addValueAt("variables", v.getName().toString());
        }

        //Set model name (required)
        int selectedItem = modelcomboBox.getSelectedIndex();
        String model = itemResponseModelCodes[selectedItem];
        option.addValueAt("model", model);

        //Scaling constant (required)
        option.addValueAt("scale", Double.valueOf(scalingConstant).toString());

        //Number of score levels (required)
        option.addValueAt("ncat", Integer.valueOf(ncat).toString());

        //Set priors
        String[] prior = null;

        String aprior = discriminationPriorTextField.getText().trim();
        if(!"".equals(aprior)){
            prior = aprior.split(",");
            for(int i=0;i<prior.length;i++){
                option.addValueAt("aprior", prior[i]);
            }

        }

        String bprior = difficultyPriorTextField.getText().trim();
        if(!"".equals(bprior)){
            prior = bprior.split(",");
            for(int i=0;i<prior.length;i++){
                option.addValueAt("bprior", prior[i]);
            }
        }

        String cprior = guessingPriorTextField.getText().trim();
        if(!"".equals(cprior)){
            prior = cprior.split(",");
            for(int i=0;i<prior.length;i++){
                option.addValueAt("cprior", prior[i]);
            }
        }

        String uprior = slippingPriorTextField.getText().trim();
        if(!"".equals(uprior)){
            prior = uprior.split(",");
            for(int i=0;i<prior.length;i++){
                option.addValueAt("uprior", prior[i]);
            }
        }

        String stprior = stepPriorTextField.getText().trim();
        if(!"".equals(stprior)){
            prior = stprior.split(",");
            for(int i=0;i<prior.length;i++){
                option.addValueAt("stprior", prior[i]);
            }
        }

        //Set fixed values
        if(fixedDiscrimination) option.addValueAt("fixed", "aparam");
        if(fixedDifficulty) option.addValueAt("fixed", "bparam");
        if(fixedGuessing) option.addValueAt("fixed", "cparam");
        if(fixedSlipping) option.addValueAt("fixed", "uparam");;
        if(fixedStep) option.addValueAt("fixed", "stparam");

        //Start values
        if(!("".equals(discriminationStartTextField.getText().trim()) && "".equals(difficultyStartTextField.getText().trim()) &&
           "".equals(guessingStartTextField.getText().trim()) && "".equals(slippingStartTextField.getText().trim()) &&
           "".equals(steStartTextField.getText().trim())) ){

            //Set discrimination start value for all models that can use it (4PL, 3PL, 2PL, 1PL, GPCM)
            if(itemResponseModelCodes[0].equals(model) || itemResponseModelCodes[1].equals(model) ||
                itemResponseModelCodes[2].equals(model) || itemResponseModelCodes[3].equals(model) ||
                    itemResponseModelCodes[4].equals(model)){

                String astart = discriminationStartTextField.getText().trim();
                if("".equals(astart)) astart = "1.0";
                option.addValueAt("start", astart);

            }

            //Set difficulty, guessing, and slipping start values for all models that can use them (4PL, 3PL, 2PL, 1PL)
            if(itemResponseModelCodes[0].equals(model) || itemResponseModelCodes[1].equals(model) ||
                itemResponseModelCodes[2].equals(model) || itemResponseModelCodes[3].equals(model)){

                String bstart = difficultyStartTextField.getText().trim();
                if("".equals(bstart)) bstart = "0.0";
                option.addValueAt("start", bstart);

                String cstart = guessingStartTextField.getText().trim();
                if("".equals(cstart)) cstart = "0.0";
                option.addValueAt("start", cstart);

                String ustart = slippingStartTextField.getText().trim();
                if("".equals(ustart)) ustart = "1.0";
                option.addValueAt("start", ustart);

            }

            //Set step parameter start values for GPCM and PCM
            if(itemResponseModelCodes[4].equals(model) || itemResponseModelCodes[5].equals(model)) {

                String ststart = steStartTextField.getText().trim();
                String[] st = null;
                if ("".equals(ststart)) {
                    st = new String[ncat];
                    for (int k = 0; k < ncat; k++) {
                        st[k] = "0.0";
                    }
                } else {
                    st = ststart.split(",");
                }

                for (int k = 0; k < ncat; k++) {
                    option.addValueAt("start", st[k]);
                }
            }

        }//End start values

        return true;

    }

    class SelectedItemListCellRenderer extends DefaultListCellRenderer{

        public SelectedItemListCellRenderer(){

        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus){
            Font labelFont = UIManager.getFont("Label.font");
            JLabel label = (JLabel)super.getListCellRendererComponent(list,value,index,isSelected,cellHasFocus);
            VariableInfo v = (VariableInfo)value;
            if(selectedVariableMasterList.contains(v)){
                label.setFont(labelFont.deriveFont(Font.BOLD)) ;
            }else{
                label.setFont(labelFont.deriveFont(Font.PLAIN));
            }
            return this;
        }


    }

}

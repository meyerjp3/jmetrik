package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.commandbuilder.MegaOption;
import com.itemanalysis.jmetrik.model.VariableListModel;
import com.itemanalysis.psychometrics.data.VariableAttributes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class IrtItemConfigurationDialog extends JDialog {

    // Variables declaration - do not modify
    private JButton cancelButton;
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
    private JList itemList;
    private JScrollPane itemScrollPane;
    private JLabel modelLabel;
    private JPanel modelPanel;
    private JComboBox modelcomboBox;
    private JButton okButton;
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
                "4 Parameter Logistic Model (PL4)",
                "3 Parameter Logistic Model (PL3)",
                "2 Parameter Logistic Model (PL2)",
                "Rasch Model",
                "Generalized Partial Credit Mode (GPCM)",
                "Partial Credit Model (PCM2)"
    };

    //Order of entry must match those in itemResponseModelStringArray
    private final String[] itemResponseModelCodes = new String[] {
                "PL4",
                "PL3",
                "PL2",
                "PL1",
                "GPCM",
                "PCM2"
    };

    private VariableListModel variableListModel = null;
    private ArrayList<VariableAttributes> selectedVariables = null;
    private int ncat = 0;
    private double scalingConstant = 1.0;
    private boolean fixedDiscrimination = false;
    private boolean fixedDifficulty = false;
    private boolean fixedGuessing = false;
    private boolean fixedSlipping = false;
    private boolean fixedStep = false;


    public IrtItemConfigurationDialog(JDialog parent, VariableListModel variableListModel){
        super(parent, "IRT Item Configuration", true);

        this.variableListModel = variableListModel;

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);


    }

    private void initComponents() {

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
        okButton = new JButton();
        cancelButton = new JButton();
        itemScrollPane = new JScrollPane();
        itemList = new JList();

        //Set certain text fields and buttons enabled according to the default of using a 3PL model.
        //Enabled text fields and buttons will change according to selection in modelcomboBox.
        discriminationPriorTextField.setEnabled(true);
        difficultyPriorTextField.setEnabled(true);
        guessingPriorTextField.setEnabled(true);
        slippingPriorTextField.setEnabled(false);
        stepPriorTextField.setEnabled(false);
        discriminationPriorButton.setEnabled(true);
        difficultyPriorButton.setEnabled(true);
        guessingPriorButton.setEnabled(true);
        slippingPriorButton.setEnabled(false);
        stepPriorButton.setEnabled(false);
        //End setting enabled

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Item Configuration");
        setMaximumSize(new Dimension(770, 360));
        setMinimumSize(new Dimension(770, 360));
        setPreferredSize(new Dimension(770, 360));
        setResizable(false);

        priorPanel.setBorder(BorderFactory.createTitledBorder("Priors"));
        priorPanel.setMinimumSize(new Dimension(300, 215));
        priorPanel.setPreferredSize(new Dimension(300, 215));

        discriminationPriorTextField.setMaximumSize(new Dimension(100, 25));
        discriminationPriorTextField.setMinimumSize(new Dimension(100, 25));
        discriminationPriorTextField.setPreferredSize(new Dimension(100, 28));

        discriminationPriorLabel.setText("Discrimination");

        discriminationPriorButton.setText("Choose");
        discriminationPriorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IrtItemPriorDialog priorDialog = new IrtItemPriorDialog(
                        IrtItemConfigurationDialog.this,
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
                        IrtItemConfigurationDialog.this,
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
                        IrtItemConfigurationDialog.this,
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

        slippingPriorButton.setText("Choose");
        slippingPriorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IrtItemPriorDialog priorDialog = new IrtItemPriorDialog(
                        IrtItemConfigurationDialog.this,
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

        stepPriorButton.setText("Choose");
        stepPriorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IrtItemPriorDialog priorDialog = new IrtItemPriorDialog(
                        IrtItemConfigurationDialog.this,
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
        startPanel.setMinimumSize(new Dimension(295, 215));
        startPanel.setPreferredSize(new Dimension(295, 215));

        discriminationStartLabel.setText("Discrimination");
        discriminationStartLabel.setToolTipText("");

        discriminationStartTextField.setText("1.0");
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

        difficultyStartTextField.setText("0.0");
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

        guessingStartTextField.setText("0.0");
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

        slippingStartTextField.setText("0.95");
        slippingStartTextField.setMaximumSize(new Dimension(100, 25));
        slippingStartTextField.setMinimumSize(new Dimension(100, 25));
        slippingStartTextField.setPreferredSize(new Dimension(100, 28));

        slippingFixedCheckBox.setText("Fixed");
        slippingFixedCheckBox.setToolTipText("");
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

        stepStartLabel.setText("Step/Threshold");
        stepStartLabel.setToolTipText("");

        steStartTextField.setText("0.0");
        steStartTextField.setMaximumSize(new Dimension(100, 25));
        steStartTextField.setMinimumSize(new Dimension(100, 25));
        steStartTextField.setPreferredSize(new Dimension(100, 28));

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
                .addContainerGap(43, Short.MAX_VALUE))
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
                .addContainerGap(17, Short.MAX_VALUE))
        );

        modelPanel.setBorder(BorderFactory.createTitledBorder(""));
        modelPanel.setPreferredSize(new Dimension(606, 60));

        modelLabel.setText("Model");

        modelcomboBox.setModel(new DefaultComboBoxModel(itemResponseModelStringArray));
        modelcomboBox.setMinimumSize(new Dimension(214, 28));
        modelcomboBox.setPreferredSize(new Dimension(214, 28));
        modelcomboBox.setSelectedItem(itemResponseModelStringArray[1]);
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

                    discriminationPriorButton.setEnabled(false);
                    difficultyPriorButton.setEnabled(false);
                    guessingPriorButton.setEnabled(false);
                    slippingPriorButton.setEnabled(false);
                    stepPriorButton.setEnabled(true);

                }
            }
        });

        scalingLabel.setText("Scaling constant");

        scaleComboBox.setModel(new DefaultComboBoxModel(new String[] { "Logistic (D = 1.0)", "Normal (D = 1.7)" }));
        scaleComboBox.setMinimumSize(new Dimension(150, 28));
        scaleComboBox.setPreferredSize(new Dimension(150, 28));
        scaleComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox comboBox = (JComboBox)e.getSource();
                String selectedItem = (String)comboBox.getSelectedItem();

                if("Normal (D = 1.7)".equals(selectedItem)){
                    scalingConstant = 1.7;
                }else{
                    scalingConstant = 1.0;
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

        okButton.setText("OK");
        okButton.setMaximumSize(new Dimension(75, 28));
        okButton.setMinimumSize(new Dimension(75, 28));
        okButton.setPreferredSize(new Dimension(75, 28));

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new Dimension(75, 28));
        cancelButton.setMinimumSize(new Dimension(75, 28));
        cancelButton.setPreferredSize(new Dimension(75, 28));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        itemScrollPane.setBorder(BorderFactory.createTitledBorder(""));
        itemScrollPane.setMinimumSize(new Dimension(137, 281));
        itemScrollPane.setPreferredSize(new Dimension(137, 281));

        itemList.setModel(variableListModel);
        itemList.setLayoutOrientation(JList.VERTICAL);
        itemScrollPane.setViewportView(itemList);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(itemScrollPane, GroupLayout.PREFERRED_SIZE, 137, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addComponent(priorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(startPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(modelPanel, GroupLayout.DEFAULT_SIZE, 601, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(modelPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(startPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(priorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addComponent(itemScrollPane, GroupLayout.PREFERRED_SIZE, 281, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }

    public boolean checkForSameNumberOfCategories(){
        int[] selectedIndices = itemList.getSelectedIndices();

        VariableAttributes v = null;
        double maxItemScore = 0;
        double previousMaxItemScore = 0;
        for(int i : selectedIndices){
            v = variableListModel.getElementAt(i);
            if(i==0){
                ncat = v.getItemScoring().numberOfScoreLevels();
            }else{
                if(ncat!=v.getItemScoring().numberOfScoreLevels()){
                    JOptionPane.showMessageDialog(IrtItemConfigurationDialog.this,
                            "Selected items must have the same scoring.",
                            "Item Selection Error",
                            JOptionPane.ERROR_MESSAGE);
                    return false;
                }
            }
        }
        return true;
    }

    public ArrayList<VariableAttributes> getSelectedVariables(){
        return selectedVariables;
    }



    public class OkActionListener implements ActionListener{

        public void actionPerformed(ActionEvent evt){

            IrtItemCalibrationCommand command = new IrtItemCalibrationCommand();
            MegaOption option = command.getOption("group");

            selectedVariables = new ArrayList<VariableAttributes>();

            //Process selected items
            int[] selectedIndices = itemList.getSelectedIndices();
            String variableString = "(";
            VariableAttributes v = null;
            for(int i = 0; i<selectedIndices.length;i++){
                v = variableListModel.getElementAt(i);
                selectedVariables.add(v);
                variableString += v.getName().toString();
                if(i<selectedIndices.length-1) variableString += ", ";
            }
            variableString += ")";

            //Set selected items (required)
            option.addValueAt("variables", variableString);

            //Set model name (required)
            int selectedItem = modelcomboBox.getSelectedIndex();
            String model = itemResponseModelCodes[selectedItem];
            option.addValueAt("model", model);

            //Number of score levels (required)
            option.addValueAt("ncat", Integer.valueOf(ncat).toString());

            //Scaling constant (required)
            option.addValueAt("scale", Double.valueOf(scalingConstant).toString());

            //Set priors
            String aprior = discriminationPriorTextField.getText().trim();
            if(!"".equals(aprior)) option.addValueAt("aprior", "("+aprior+")");

            String bprior = difficultyPriorTextField.getText().trim();
            if(!"".equals(bprior)) option.addValueAt("bprior", "("+bprior+")");

            String cprior = guessingPriorTextField.getText().trim();
            if(!"".equals(cprior)) option.addValueAt("cprior", "("+cprior+")");

            String uprior = slippingPriorTextField.getText().trim();
            if(!"".equals(uprior)) option.addValueAt("uprior", "("+uprior+")");

            String stprior = stepPriorTextField.getText().trim();
            if(!"".equals(stprior)) option.addValueAt("stprior", "("+stprior+")");

            //Set fixed values
            String fixedOptions = "(";
            int count = 0;
            ArrayList<String> fixedString = new ArrayList<String>(5);
            if(fixedDiscrimination) fixedString.add("aparam");
            if(fixedDifficulty) fixedString.add("bparam");
            if(fixedGuessing) fixedString.add("cparam");
            if(fixedSlipping) fixedString.add("uparam");
            if(fixedStep) fixedString.add("stparam");

            for(int i=0;i<fixedString.size();i++){
                fixedOptions += fixedString.get(i);
                if(i<fixedString.size()-1) fixedOptions += ", ";
            }
            fixedOptions += ")";

            //Start values
            //TODO stopped here

            //TODO will need to know the group number


        }

    }

}

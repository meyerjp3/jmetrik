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
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.ArrayList;

public class IrtItemCalibrationDialog extends JDialog {

    // Variables declaration - do not modify
    private JRadioButton bfgsRadioButton;
    private JButton cancelButton;
    private JPanel convergencePanel;
    private JButton fixedValuesButton;
    private JLabel groupByLabel;
    private JTextField groupByTextField;
    private JRadioButton ignoreRadioButton;
    private JPanel itemPanel;
    private JPanel latentDistributionPanel;
    private JLabel maxIterationLabel;
    private JTextField maxIterationTextField;
    private JLabel maxLabel;
    private JTextField maxTextField;
    private JLabel minLabel;
    private JTextField minTextField;
    private ButtonGroup missingDataButtonGroup;
    private JPanel missingDataPanel;
    private JButton okButton;
    private ButtonGroup optimizerButtonGroup;
    private JPanel optimizerPanel;
    private JLabel outputLabel;
    private JTextField outputTextField;
    private JTextField pointTextField;
    private JLabel pointsLabel;
    private JRadioButton scoreAsZeroRadioButton;
    private JButton selectGroupButton;
    private JButton selectItemsButton;
    private JLabel toeranceLabel;
    private JTextField toleranceTextField;
    private JComboBox typeComboBox;
    private JLabel typeLabel;
    private JRadioButton uncminRadioButton;
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
                                    SortedListModel<DataTableName> tableListModel, ArrayList<VariableInfo> variables){

        super(parent, "IRT Item Calibration", true);

        this.conn = conn;
        this.dbName = dbName;
        this.tableName = tableName;
        this.tableListModel = tableListModel;


        //Add variables to list model and filter out nonitems
        VariableType filterType1 = new VariableType(VariableType.NOT_ITEM, VariableType.STRING);
        VariableType filterType2 = new VariableType(VariableType.NOT_ITEM, VariableType.DOUBLE);
        VariableListFilter listFilter = new VariableListFilter();
        listFilter.addFilteredType(filterType1);
        listFilter.addFilteredType(filterType2);
        variableListModel = new VariableListModel(listFilter);
        for(VariableInfo v : variables){
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

    public void initComponents(){
        optimizerButtonGroup = new ButtonGroup();
        missingDataButtonGroup = new ButtonGroup();
        itemPanel = new JPanel();
        selectItemsButton = new JButton();
        fixedValuesButton = new JButton();
        outputLabel = new JLabel();
        outputTextField = new JTextField();
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
        okButton = new JButton();
        cancelButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("IRT Item Calibration");

        itemPanel.setBorder(BorderFactory.createTitledBorder("Items"));

        selectItemsButton.setText("Select Items");
        selectItemsButton.setMaximumSize(new Dimension(125, 28));
        selectItemsButton.setMinimumSize(new Dimension(125, 28));
        selectItemsButton.setPreferredSize(new Dimension(125, 28));
        selectItemsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(wizzard==null) wizzard = new ItemGroupWizzard(IrtItemCalibrationDialog.this, variableListModel);
                wizzard.setVisible(true);
            }
        });

        fixedValuesButton.setText("Fixed Values");
        fixedValuesButton.setMaximumSize(new Dimension(125, 28));
        fixedValuesButton.setMinimumSize(new Dimension(125, 28));
        fixedValuesButton.setPreferredSize(new Dimension(125, 28));

        outputLabel.setText("Output table");

        outputTextField.setMaximumSize(new Dimension(150, 28));
        outputTextField.setMinimumSize(new Dimension(150, 28));
        outputTextField.setPreferredSize(new Dimension(150, 28));

        GroupLayout itemPanelLayout = new GroupLayout(itemPanel);
        itemPanel.setLayout(itemPanelLayout);
        itemPanelLayout.setHorizontalGroup(
            itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(itemPanelLayout.createSequentialGroup()
                        .addComponent(outputLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(outputTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addComponent(selectItemsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(fixedValuesButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        itemPanelLayout.setVerticalGroup(
            itemPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(selectItemsButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(fixedValuesButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(itemPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(outputLabel)
                    .addComponent(outputTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        latentDistributionPanel.setBorder(BorderFactory.createTitledBorder("Latent Distribution"));

        typeLabel.setText("Type");

        typeComboBox.setModel(new DefaultComboBoxModel(latentDistributionTypeString));
        typeComboBox.setMaximumSize(new Dimension(125, 28));
        typeComboBox.setMinimumSize(new Dimension(125, 28));
        typeComboBox.setPreferredSize(new Dimension(125, 28));

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

        GroupLayout latentDistributionPanelLayout = new GroupLayout(latentDistributionPanel);
        latentDistributionPanel.setLayout(latentDistributionPanelLayout);
        latentDistributionPanelLayout.setHorizontalGroup(
            latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                        .addComponent(minLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(maxLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                        .addComponent(groupByLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(groupByTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(typeLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(typeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addComponent(pointsLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pointTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        latentDistributionPanelLayout.setVerticalGroup(
            latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(latentDistributionPanelLayout.createSequentialGroup()
                .addGap(6, 6, 6)
                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(typeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(pointsLabel)
                    .addComponent(pointTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(11, 11, 11)
                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(maxLabel)
                    .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(minLabel)
                    .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(latentDistributionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(groupByLabel)
                    .addComponent(groupByTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
                .addContainerGap(28, Short.MAX_VALUE))
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
                .addContainerGap(95, Short.MAX_VALUE))
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

        okButton.setText("OK");
        okButton.setMaximumSize(new Dimension(75, 28));
        okButton.setMinimumSize(new Dimension(75, 28));
        okButton.setPreferredSize(new Dimension(75, 28));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setCommand();
//                System.out.println(command.paste());
                setVisible(false);
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new Dimension(75, 28));
        cancelButton.setMinimumSize(new Dimension(75, 28));
        cancelButton.setPreferredSize(new Dimension(75, 28));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun = false;
                setVisible(false);
            }
        });

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(itemPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(latentDistributionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(convergencePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(optimizerPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(missingDataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(itemPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(latentDistributionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(convergencePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(optimizerPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(missingDataPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }

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

        //Item output
        if(!"".equals(outputTextField.getText().trim())){
            option = command.getOption("itemout");
            option.addValueAt("db", dbName.toString());
            DataTableName outName = new DataTableName(outputTextField.getText().trim());
            option.addValueAt("table", outName.toString());
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

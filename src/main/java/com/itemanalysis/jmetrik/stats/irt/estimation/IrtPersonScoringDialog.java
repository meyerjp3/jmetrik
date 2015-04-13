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

package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.dao.DerbyDatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.gui.SelectTableDialog;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.selector.MultipleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.stats.irt.linking.IrtLinkingThetaDialog;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.ItemType;
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
import java.util.ArrayList;

public class IrtPersonScoringDialog extends JDialog{

    // Variables declaration - do not modify
    private JLabel convergenceLabel;
    private JTextField convergenceTextField;
    private JCheckBox eapCheckbox;
    private JPanel estimationTab;
    private JRadioButton ignoreRadioButton;
    private JRadioButton incorrectRadioButton;
    private JButton itemParameterButton;
    private JPanel itemParameterPanel;
    private JTextField itemParameterTextField;
    private JPanel scalePanel;
    private JRadioButton normalRadioButton;
    private JRadioButton logisticScaleRadioButton;
    private JCheckBox mapCheckBox;
    private JLabel maxIterLabel;
    private JTextField maxIterTextField;
    private JLabel maxLabel;
    private JTextField maxTextField;
    private JLabel meanLabel;
    private JTextField meanTextField;
    private JPanel methodPanel;
    private JLabel minLabel;
    private JTextField minTextField;
    private ButtonGroup missingButtonGroup;
    private JPanel missingDataPanel;
    private JCheckBox mleCheckBox;
    private JLabel nameLabel;
    private JTextField nameTextField;
    private JPanel normalPriorPanel;
    private JLabel numPointsLabel;
    private JTextField numPointsTextField;
    private JPanel optionsPanel;
    private JPanel optionsTab;
    private JPanel priorTab;
    private JButton quadratureButton;
    private JPanel quadraturePanel;
    private JTextField quadratureTextField;
    private ButtonGroup scaleButtonGroup;
    private JTabbedPane scoringTabbedPane;
    private JLabel sdLabel;
    private JTextField sdTextField;
    // End of variables declaration

    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private SortedListModel<DataTableName> tableListModel = null;
    private boolean canRun = false;
    private IrtPersonScoringCommand command = null;
    private MultipleSelectionPanel vsp = null;
    private SelectTableDialog itemParameterDialog = null;
    private DataTableName itemParameterTable = null;
    private IrtLinkingThetaDialog quadratureDialog = null;
    private boolean useMle = true;
    private boolean useMap = false;
    private boolean useEap = false;
    private boolean useQuadrature = false;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public IrtPersonScoringDialog(JFrame parent, Connection conn, DatabaseName dbName, DataTableName tableName, SortedListModel<DataTableName> tableListModel,
                       ArrayList <VariableAttributes> variables){
        super(parent, "IRT Person Scoring", true);
        this.conn = conn;
        this.dbName = dbName;
        this.tableName = tableName;
        this.tableListModel = tableListModel;

        vsp=new MultipleSelectionPanel();

        //filter out nonitems
//        VariableType filterType1 = new VariableType(ItemType.NOT_ITEM, DataType.STRING);
//        VariableType filterType2 = new VariableType(ItemType.NOT_ITEM, DataType.DOUBLE);
//        vsp.addUnselectedFilterType(filterType1);
//        vsp.addUnselectedFilterType(filterType2);
//        vsp.addSelectedFilterType(filterType1);
//        vsp.addSelectedFilterType(filterType2);

        vsp.addUnselectedFilterItemType(ItemType.NOT_ITEM);
        vsp.addSelectedFilterItemType(ItemType.NOT_ITEM);
        vsp.setVariables(variables);

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
                resetDialog();
            }
        });

        vsp.showButton4(false);


        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

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

    private void initComponents() {

        missingButtonGroup = new ButtonGroup();
        scaleButtonGroup = new ButtonGroup();
        scoringTabbedPane = new JTabbedPane();
        estimationTab = new JPanel();
        methodPanel = new JPanel();
        eapCheckbox = new JCheckBox();
        mapCheckBox = new JCheckBox();
        mleCheckBox = new JCheckBox();
        missingDataPanel = new JPanel();
        ignoreRadioButton = new JRadioButton();
        incorrectRadioButton = new JRadioButton();
        itemParameterPanel = new JPanel();
        itemParameterTextField = new JTextField();
        itemParameterButton = new JButton();
        optionsTab = new JPanel();
        optionsPanel = new JPanel();
        maxIterLabel = new JLabel();
        maxIterTextField = new JTextField();
        convergenceLabel = new JLabel();
        convergenceTextField = new JTextField();
        nameLabel = new JLabel();
        nameTextField = new JTextField();
        scalePanel = new JPanel();
        logisticScaleRadioButton = new JRadioButton();
        normalRadioButton = new JRadioButton();
        priorTab = new JPanel();
        normalPriorPanel = new JPanel();
        sdLabel = new JLabel();
        sdTextField = new JTextField();
        meanLabel = new JLabel();
        meanTextField = new JTextField();
        maxLabel = new JLabel();
        maxTextField = new JTextField();
        minLabel = new JLabel();
        minTextField = new JTextField();
        numPointsLabel = new JLabel();
        numPointsTextField = new JTextField();
        quadraturePanel = new JPanel();
        quadratureTextField = new JTextField();
        quadratureButton = new JButton();

        scoringTabbedPane.setPreferredSize(new Dimension(414, 275));

        methodPanel.setBorder(BorderFactory.createTitledBorder("Method"));
        methodPanel.setLayout(new GridLayout(3, 1, 5, 5));

        eapCheckbox.setText("Expected a posteriori");
        eapCheckbox.setToolTipText("");
        eapCheckbox.setActionCommand("eap");
        eapCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (eapCheckbox.isSelected()) {
                    useEap = true;
                } else {
                    useEap = false;
                }
                setDisplay();
            }
        });
        methodPanel.add(eapCheckbox);

        mapCheckBox.setText("Maximum a posteriori");
        mapCheckBox.setActionCommand("map");
        mapCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (mapCheckBox.isSelected()) {
                    useMap = true;
                } else {
                    useMap = false;
                }
                setDisplay();
            }
        });
        methodPanel.add(mapCheckBox);

        mleCheckBox.setSelected(true);
        mleCheckBox.setText("Maximum likelihood");
        mleCheckBox.setActionCommand("mle");
        mleCheckBox.addActionListener(
                new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (mleCheckBox.isSelected()) {
                            useMle = true;
                        } else {
                            useMle = false;
                        }
                        setDisplay();
                    }
                }
        );
        methodPanel.add(mleCheckBox);

        missingDataPanel.setBorder(BorderFactory.createTitledBorder("Missing Data"));
        missingDataPanel.setLayout(new GridLayout(2, 1, 5, 5));

        missingButtonGroup.add(ignoreRadioButton);
        ignoreRadioButton.setSelected(true);
        ignoreRadioButton.setText("Ignore");
        ignoreRadioButton.setActionCommand("ignore");
        missingDataPanel.add(ignoreRadioButton);

        missingButtonGroup.add(incorrectRadioButton);
        incorrectRadioButton.setText("Score as incorrect");
        incorrectRadioButton.setActionCommand("zero");
        missingDataPanel.add(incorrectRadioButton);

        itemParameterPanel.setBorder(BorderFactory.createTitledBorder("Item Parameter Table"));

        itemParameterTextField.setPreferredSize(new Dimension(250, 28));

        itemParameterButton.setText("Select");
        itemParameterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(itemParameterDialog==null){
                    itemParameterDialog = new SelectTableDialog(IrtPersonScoringDialog.this, dbName, tableListModel);
                    itemParameterDialog.setTitle("Item Parameter Table");
                }
                itemParameterDialog.setVisible(true);

                if(itemParameterDialog.canRun()){
                    itemParameterTable = itemParameterDialog.getSelectedTable();
                    itemParameterTextField.setText(itemParameterTable.toString());
                }
            }
        });
        itemParameterButton.setMaximumSize(new Dimension(69, 28));
        itemParameterButton.setMinimumSize(new Dimension(69, 28));
        itemParameterButton.setPreferredSize(new Dimension(69, 28));

        GroupLayout itemParameterPanelLayout = new GroupLayout(itemParameterPanel);
        itemParameterPanel.setLayout(itemParameterPanelLayout);
        itemParameterPanelLayout.setHorizontalGroup(
            itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemParameterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(itemParameterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(itemParameterButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        itemParameterPanelLayout.setVerticalGroup(
            itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemParameterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(itemParameterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(itemParameterButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout estimationTabLayout = new GroupLayout(estimationTab);
        estimationTab.setLayout(estimationTabLayout);
        estimationTabLayout.setHorizontalGroup(
            estimationTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(estimationTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(estimationTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(itemParameterPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(estimationTabLayout.createSequentialGroup()
                        .addComponent(methodPanel, GroupLayout.DEFAULT_SIZE, 201, Short.MAX_VALUE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(missingDataPanel, GroupLayout.PREFERRED_SIZE, 183, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        estimationTabLayout.setVerticalGroup(
            estimationTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(estimationTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(estimationTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(methodPanel, GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                    .addComponent(missingDataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(itemParameterPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(42, Short.MAX_VALUE))
        );

        scoringTabbedPane.addTab("Estimation", estimationTab);

        optionsPanel.setBorder(BorderFactory.createTitledBorder("MLE and MAP Options"));
        optionsPanel.setMinimumSize(new Dimension(215, 165));
        optionsPanel.setPreferredSize(new Dimension(215, 165));

        maxIterLabel.setText("Max iterations");

        maxIterTextField.setText("100");
        maxIterTextField.setPreferredSize(new Dimension(75, 28));

        convergenceLabel.setText("Convergence");

        convergenceTextField.setText("1e-5");
        convergenceTextField.setPreferredSize(new Dimension(75, 28));

        GroupLayout optionsPanelLayout = new GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGap(8, 8, 8)
                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(convergenceLabel)
                    .addComponent(maxIterLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                    .addComponent(convergenceTextField, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(maxIterTextField, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(0, 48, Short.MAX_VALUE))
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(maxIterLabel)
                    .addComponent(maxIterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(convergenceLabel)
                    .addComponent(convergenceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        nameLabel.setText("New variable name");

        nameTextField.setMinimumSize(new Dimension(250, 28));
        nameTextField.setPreferredSize(new Dimension(250, 28));

        scalePanel.setBorder(BorderFactory.createTitledBorder("Default Scale"));

        scaleButtonGroup.add(logisticScaleRadioButton);
        logisticScaleRadioButton.setSelected(true);
        logisticScaleRadioButton.setText("Logistic (D=1.0)");
        logisticScaleRadioButton.setActionCommand("logistic");

        scaleButtonGroup.add(normalRadioButton);
        normalRadioButton.setText("Normal (D=1.7)");
        normalRadioButton.setActionCommand("normal");

        GroupLayout scalePanelLayout = new GroupLayout(scalePanel);
        scalePanel.setLayout(scalePanelLayout);
        scalePanelLayout.setHorizontalGroup(
            scalePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(scalePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(scalePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(logisticScaleRadioButton)
                    .addComponent(normalRadioButton))
                .addContainerGap(48, Short.MAX_VALUE))
        );
        scalePanelLayout.setVerticalGroup(
            scalePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(scalePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(logisticScaleRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(normalRadioButton)
                .addContainerGap(36, Short.MAX_VALUE))
        );

        GroupLayout optionsTabLayout = new GroupLayout(optionsTab);
        optionsTab.setLayout(optionsTabLayout);
        optionsTabLayout.setHorizontalGroup(
            optionsTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(optionsTabLayout.createSequentialGroup()
                        .addComponent(nameLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(optionsTabLayout.createSequentialGroup()
                        .addComponent(optionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(scalePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        optionsTabLayout.setVerticalGroup(
            optionsTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, optionsTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionsTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(optionsTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(scalePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(optionsPanel, GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(85, Short.MAX_VALUE))
        );

        scoringTabbedPane.addTab("Options", optionsTab);

        normalPriorPanel.setBorder(BorderFactory.createTitledBorder("Normal Prior"));

        sdLabel.setText("Std. deviation");

        sdTextField.setText("1.0");
        sdTextField.setEnabled(false);
        sdTextField.setPreferredSize(new Dimension(100, 28));

        meanLabel.setText("Mean");

        meanTextField.setText("0.0");
        meanTextField.setEnabled(false);
        meanTextField.setPreferredSize(new Dimension(100, 28));

        maxLabel.setText("Maximum");

        maxTextField.setText("6.0");
        maxTextField.setPreferredSize(new Dimension(100, 28));

        minLabel.setText("Minimum");

        minTextField.setText("-6.0");
        minTextField.setPreferredSize(new Dimension(100, 28));

        numPointsLabel.setText("Points");

        numPointsTextField.setText("60");
        numPointsTextField.setEnabled(false);
        numPointsTextField.setPreferredSize(new Dimension(100, 28));

        GroupLayout normalPriorPanelLayout = new GroupLayout(normalPriorPanel);
        normalPriorPanel.setLayout(normalPriorPanelLayout);
        normalPriorPanelLayout.setHorizontalGroup(
            normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(normalPriorPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(normalPriorPanelLayout.createSequentialGroup()
                        .addComponent(numPointsLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(numPointsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(normalPriorPanelLayout.createSequentialGroup()
                        .addComponent(minLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(normalPriorPanelLayout.createSequentialGroup()
                        .addComponent(maxLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(sdLabel)
                    .addComponent(meanLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(sdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
        );
        normalPriorPanelLayout.setVerticalGroup(
            normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(normalPriorPanelLayout.createSequentialGroup()
                .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(normalPriorPanelLayout.createSequentialGroup()
                        .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(minLabel)
                            .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(maxLabel)))
                    .addGroup(normalPriorPanelLayout.createSequentialGroup()
                        .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(meanLabel))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(sdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(sdLabel))))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(numPointsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(numPointsLabel)))
        );

        quadraturePanel.setBorder(BorderFactory.createTitledBorder("Quadrature Table (Optional)"));

        quadratureTextField.setPreferredSize(new Dimension(250, 28));

        quadratureButton.setText("Select");
        quadratureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(quadratureDialog==null){
                    quadratureDialog = new IrtLinkingThetaDialog(IrtPersonScoringDialog.this, conn, dao, tableListModel, "Person Scoring");
                }
                quadratureDialog.setVisible(true);
                if(quadratureDialog.canRun()){
                    String text = quadratureDialog.getTableName().toString();
                    text += " (" + quadratureDialog.getTheta().getName() + ", " + quadratureDialog.getWeight().getName() + " )";
                    quadratureTextField.setText(text);
                }
            }
        });
        quadratureButton.setMaximumSize(new Dimension(69, 28));
        quadratureButton.setMinimumSize(new Dimension(69, 28));
        quadratureButton.setPreferredSize(new Dimension(69, 28));

        GroupLayout quadraturePanelLayout = new GroupLayout(quadraturePanel);
        quadraturePanel.setLayout(quadraturePanelLayout);
        quadraturePanelLayout.setHorizontalGroup(
            quadraturePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(quadraturePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(quadratureTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(quadratureButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(28, Short.MAX_VALUE))
        );
        quadraturePanelLayout.setVerticalGroup(
            quadraturePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(quadraturePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(quadraturePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(quadratureTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(quadratureButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout priorTabLayout = new GroupLayout(priorTab);
        priorTab.setLayout(priorTabLayout);
        priorTabLayout.setHorizontalGroup(
            priorTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(priorTabLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(priorTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(quadraturePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(normalPriorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(25, Short.MAX_VALUE))
        );
        priorTabLayout.setVerticalGroup(
            priorTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(priorTabLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(normalPriorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(quadraturePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(38, Short.MAX_VALUE))
        );

        scoringTabbedPane.addTab("Prior", priorTab);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(scoringTabbedPane, GroupLayout.PREFERRED_SIZE, 415, GroupLayout.PREFERRED_SIZE)
                    .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scoringTabbedPane, GroupLayout.PREFERRED_SIZE, 275, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

//    private void initComponents() {
//
//        missingButtonGroup = new ButtonGroup();
//        scaleButtonGroup = new ButtonGroup();
//        scoringTabbedPane = new JTabbedPane();
//        estimationTab = new JPanel();
//        methodPanel = new JPanel();
//        mleCheckBox = new JCheckBox();
//        mapCheckBox = new JCheckBox();
//        eapCheckbox = new JCheckBox();
//        pcfCheckBox = new JCheckBox();
//        missingDataPanel = new JPanel();
//        ignoreRadioButton = new JRadioButton();
//        incorrectRadioButton = new JRadioButton();
//        itemParameterPanel = new JPanel();
//        itemParameterTextField = new JTextField();
//        itemParameterButton = new JButton();
//        optionsTab = new JPanel();
//        optionsPanel = new JPanel();
//        maxIterLabel = new JLabel();
//        maxIterTextField = new JTextField();
//        convergenceLabel = new JLabel();
//        convergenceTextField = new JTextField();
//        adjustmentLabel = new JLabel();
//        adjustmentTextField = new JTextField();
//        nameLabel = new JLabel();
//        nameTextField = new JTextField();
//        scalePanel = new JPanel();
//        logisticScaleRadioButton = new JRadioButton();
//        normalRadioButton = new JRadioButton();
//        priorTab = new JPanel();
//        normalPriorPanel = new JPanel();
//        sdLabel = new JLabel();
//        sdTextField = new JTextField();
//        meanLabel = new JLabel();
//        meanTextField = new JTextField();
//        maxLabel = new JLabel();
//        maxTextField = new JTextField();
//        minLabel = new JLabel();
//        minTextField = new JTextField();
//        numPointsLabel = new JLabel();
//        numPointsTextField = new JTextField();
//        quadraturePanel = new JPanel();
//        quadratureTextField = new JTextField();
//        quadratureButton = new JButton();
//
//        scoringTabbedPane.setPreferredSize(new Dimension(414, 275));
//
//        methodPanel.setBorder(BorderFactory.createTitledBorder("Method"));
//        methodPanel.setLayout(new GridLayout(4, 1, 5, 5));
//
//        mleCheckBox.setSelected(true);
//        mleCheckBox.setText("Maximum likelihood");
//        mleCheckBox.setActionCommand("mle");
//        mleCheckBox.addActionListener(
//                new ActionListener() {
//                    @Override
//                    public void actionPerformed(ActionEvent e) {
//                        if (mleCheckBox.isSelected()) {
//                            useMle = true;
//                        } else {
//                            useMle = false;
//                        }
//                        setDisplay();
//                    }
//                }
//        );
//        methodPanel.addArgument(mleCheckBox);
//
//
//        mapCheckBox.setText("Maximum a posteriori");
//        mapCheckBox.setActionCommand("map");
//        mapCheckBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (mapCheckBox.isSelected()) {
//                    useMap = true;
//                } else {
//                    useMap = false;
//                }
//                setDisplay();
//            }
//        });
//        methodPanel.addArgument(mapCheckBox);
//
//        eapCheckbox.setText("Expected a posteriori");
//        eapCheckbox.setToolTipText("");
//        eapCheckbox.setActionCommand("eap");
//        eapCheckbox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (eapCheckbox.isSelected()) {
//                    useEap = true;
//                } else {
//                    useEap = false;
//                }
//                setDisplay();
//            }
//        });
//        methodPanel.addArgument(eapCheckbox);
//
//        pcfCheckBox.setText("Proportional curve fitting");
//        pcfCheckBox.setActionCommand("pcf");
//        pcfCheckBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if (pcfCheckBox.isSelected()) {
//                    usePcf = true;
//                } else {
//                    usePcf = false;
//                }
//                setDisplay();
//            }
//        });
//        methodPanel.addArgument(pcfCheckBox);
//
//        missingDataPanel.setBorder(BorderFactory.createTitledBorder("Missing Data"));
//        missingDataPanel.setLayout(new GridLayout(2, 1, 5, 5));
//
//        missingButtonGroup.addArgument(ignoreRadioButton);
//        ignoreRadioButton.setSelected(true);
//        ignoreRadioButton.setText("Ignore");
//        ignoreRadioButton.setActionCommand("ignore");
//        missingDataPanel.addArgument(ignoreRadioButton);
//
//        missingButtonGroup.addArgument(incorrectRadioButton);
//        incorrectRadioButton.setText("Score as incorrect");
//        incorrectRadioButton.setActionCommand("zero");
//        missingDataPanel.addArgument(incorrectRadioButton);
//
//        itemParameterPanel.setBorder(BorderFactory.createTitledBorder("Item Parameter Table"));
//
//        itemParameterTextField.setPreferredSize(new Dimension(250, 28));
//
//        itemParameterButton.setText("Select");
//        itemParameterButton.setMaximumSize(new Dimension(69, 28));
//        itemParameterButton.setMinimumSize(new Dimension(69, 28));
//        itemParameterButton.setPreferredSize(new Dimension(69, 28));
//        itemParameterButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if(itemParameterDialog==null){
//                    itemParameterDialog = new SelectTableDialog(IrtPersonScoringDialog.this, dbName, tableListModel);
//                    itemParameterDialog.setTitle("Item Parameter Table");
//                }
//                itemParameterDialog.setVisible(true);
//
//                if(itemParameterDialog.canRun()){
//                    itemParameterTable = itemParameterDialog.getSelectedTable();
//                    itemParameterTextField.setText(itemParameterTable.toString());
//                }
//            }
//        });
//
//        GroupLayout itemParameterPanelLayout = new GroupLayout(itemParameterPanel);
//        itemParameterPanel.setLayout(itemParameterPanelLayout);
//        itemParameterPanelLayout.setHorizontalGroup(
//                itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(itemParameterPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addComponent(itemParameterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addComponent(itemParameterButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                .addContainerGap(43, Short.MAX_VALUE))
//        );
//        itemParameterPanelLayout.setVerticalGroup(
//            itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(itemParameterPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(itemParameterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(itemParameterButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        GroupLayout estimationTabLayout = new GroupLayout(estimationTab);
//        estimationTab.setLayout(estimationTabLayout);
//        estimationTabLayout.setHorizontalGroup(
//                estimationTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(estimationTabLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(estimationTabLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                                        .addComponent(itemParameterPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                        .addGroup(estimationTabLayout.createSequentialGroup()
//                                                .addComponent(methodPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                                .addComponent(missingDataPanel, GroupLayout.PREFERRED_SIZE, 183, GroupLayout.PREFERRED_SIZE)))
//                                .addContainerGap())
//        );
//        estimationTabLayout.setVerticalGroup(
//                estimationTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(estimationTabLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(estimationTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
//                                        .addComponent(methodPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                        .addComponent(missingDataPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addComponent(itemParameterPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                                .addContainerGap(27, Short.MAX_VALUE))
//        );
//
//        scoringTabbedPane.addTab("Estimation", estimationTab);
//
//        optionsPanel.setBorder(BorderFactory.createTitledBorder("MLE, MAP, and PCF Options"));
//        optionsPanel.setMinimumSize(new Dimension(215, 165));
//        optionsPanel.setPreferredSize(new Dimension(215, 165));
//
//        maxIterLabel.setText("Max iterations");
//
//        maxIterTextField.setText("100");
//        maxIterTextField.setPreferredSize(new Dimension(75, 28));
//
//        convergenceLabel.setText("Convergence");
//
//        convergenceTextField.setText("1e-10");
//        convergenceTextField.setPreferredSize(new Dimension(75, 28));
//
//        adjustmentLabel.setText("PCF adjustment");
//
//        adjustmentTextField.setText("0.03");
//        adjustmentTextField.setPreferredSize(new Dimension(75, 28));
//
//        GroupLayout optionsPanelLayout = new GroupLayout(optionsPanel);
//        optionsPanel.setLayout(optionsPanelLayout);
//        optionsPanelLayout.setHorizontalGroup(
//                optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(optionsPanelLayout.createSequentialGroup()
//                                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                                        .addComponent(adjustmentLabel)
//                                        .addComponent(convergenceLabel)
//                                        .addComponent(maxIterLabel))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
//                                        .addComponent(convergenceTextField, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                        .addComponent(adjustmentTextField, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                                        .addComponent(maxIterTextField, GroupLayout.Alignment.LEADING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addGap(0, 48, Short.MAX_VALUE))
//        );
//        optionsPanelLayout.setVerticalGroup(
//                optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                        .addGroup(optionsPanelLayout.createSequentialGroup()
//                                .addContainerGap()
//                                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(maxIterLabel)
//                                        .addComponent(maxIterTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(convergenceLabel)
//                                        .addComponent(convergenceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                                .addGroup(optionsPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                                        .addComponent(adjustmentLabel)
//                                        .addComponent(adjustmentTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                                .addContainerGap(25, Short.MAX_VALUE))
//        );
//
//        nameLabel.setText("New Variable Name");
//
//        nameTextField.setMinimumSize(new Dimension(250, 28));
//        nameTextField.setPreferredSize(new Dimension(250, 28));
//
//        scalePanel.setBorder(BorderFactory.createTitledBorder("Default Scale"));
//
//        scaleButtonGroup.addArgument(logisticScaleRadioButton);
//        logisticScaleRadioButton.setSelected(true);
//        logisticScaleRadioButton.setText("Logistic (D=1.0)");
//        logisticScaleRadioButton.setActionCommand("logistic");
//
//        scaleButtonGroup.addArgument(normalRadioButton);
//        normalRadioButton.setText("Normal (D=1.7)");
//        normalRadioButton.setActionCommand("normal");
//
//        GroupLayout jPanel1Layout = new GroupLayout(scalePanel);
//        scalePanel.setLayout(jPanel1Layout);
//        jPanel1Layout.setHorizontalGroup(
//            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(jPanel1Layout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addComponent(logisticScaleRadioButton)
//                    .addComponent(normalRadioButton))
//                .addContainerGap(48, Short.MAX_VALUE))
//        );
//        jPanel1Layout.setVerticalGroup(
//            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(jPanel1Layout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(logisticScaleRadioButton)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(normalRadioButton)
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        GroupLayout optionsTabLayout = new GroupLayout(optionsTab);
//        optionsTab.setLayout(optionsTabLayout);
//        optionsTabLayout.setHorizontalGroup(
//            optionsTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(optionsTabLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(optionsTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(optionsTabLayout.createSequentialGroup()
//                        .addComponent(nameLabel)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                        .addGap(0, 0, Short.MAX_VALUE))
//                    .addGroup(optionsTabLayout.createSequentialGroup()
//                        .addComponent(optionsPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(scalePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
//                .addContainerGap())
//        );
//        optionsTabLayout.setVerticalGroup(
//            optionsTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(GroupLayout.Alignment.TRAILING, optionsTabLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(optionsTabLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(nameLabel)
//                    .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                .addGroup(optionsTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
//                    .addComponent(scalePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                    .addComponent(optionsPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                .addContainerGap(32, Short.MAX_VALUE))
//        );
//
//        scoringTabbedPane.addTab("Options", optionsTab);
//
//        normalPriorPanel.setBorder(BorderFactory.createTitledBorder("Normal Prior"));
//
//        sdLabel.setText("Std. deviation");
//
//        sdTextField.setText("1.0");
//        sdTextField.setEnabled(false);
//        sdTextField.setPreferredSize(new Dimension(100, 28));
//
//        meanLabel.setText("Mean");
//
//        meanTextField.setText("0.0");
//        meanTextField.setEnabled(false);
//        meanTextField.setPreferredSize(new Dimension(100, 28));
//
//        maxLabel.setText("Maximum");
//
//        maxTextField.setText("6.0");
//        maxTextField.setPreferredSize(new Dimension(100, 28));
//
//        minLabel.setText("Minimum");
//
//        minTextField.setText("-6.0");
//        minTextField.setPreferredSize(new Dimension(100, 28));
//
//        numPointsLabel.setText("Points");
//
//        numPointsTextField.setText("60");
//        numPointsTextField.setEnabled(false);
//        numPointsTextField.setPreferredSize(new Dimension(100, 28));
//
//        GroupLayout normalPriorPanelLayout = new GroupLayout(normalPriorPanel);
//        normalPriorPanel.setLayout(normalPriorPanelLayout);
//        normalPriorPanelLayout.setHorizontalGroup(
//            normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(normalPriorPanelLayout.createSequentialGroup()
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                    .addGroup(normalPriorPanelLayout.createSequentialGroup()
//                        .addComponent(numPointsLabel)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(numPointsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                    .addGroup(normalPriorPanelLayout.createSequentialGroup()
//                        .addComponent(minLabel)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                    .addGroup(normalPriorPanelLayout.createSequentialGroup()
//                        .addComponent(maxLabel)
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                    .addComponent(sdLabel)
//                    .addComponent(meanLabel))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(sdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
//        );
//        normalPriorPanelLayout.setVerticalGroup(
//            normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(normalPriorPanelLayout.createSequentialGroup()
//                .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addGroup(normalPriorPanelLayout.createSequentialGroup()
//                        .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                            .addComponent(minLabel)
//                            .addComponent(minTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                            .addComponent(maxTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addComponent(maxLabel)))
//                    .addGroup(normalPriorPanelLayout.createSequentialGroup()
//                        .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                            .addComponent(meanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addComponent(meanLabel))
//                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                        .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                            .addComponent(sdTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addComponent(sdLabel))))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addGroup(normalPriorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(numPointsTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(numPointsLabel)))
//        );
//
//        quadraturePanel.setBorder(BorderFactory.createTitledBorder("Quadrature Table (optional)"));
//
//        quadratureTextField.setPreferredSize(new Dimension(250, 28));
//        quadratureTextField.setEnabled(false);
//
//        quadratureButton.setText("Select");
//        quadratureButton.setMaximumSize(new Dimension(69, 28));
//        quadratureButton.setMinimumSize(new Dimension(69, 28));
//        quadratureButton.setPreferredSize(new Dimension(69, 28));
//        quadratureButton.setEnabled(false);
//        quadratureButton.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if(quadratureDialog==null){
//                    quadratureDialog = new IrtLinkingThetaDialog(IrtPersonScoringDialog.this, conn, dao, tableListModel, "Person Scoring");
//                }
//                quadratureDialog.setVisible(true);
//                if(quadratureDialog.canRun()){
//                    String text = quadratureDialog.getTableName().toString();
//                    text += " (" + quadratureDialog.getTheta().getName() + ", " + quadratureDialog.getWeight().getName() + " )";
//                    quadratureTextField.setText(text);
//                }
//            }
//        });
//
//        GroupLayout quadraturePanelLayout = new GroupLayout(quadraturePanel);
//        quadraturePanel.setLayout(quadraturePanelLayout);
//        quadraturePanelLayout.setHorizontalGroup(
//            quadraturePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(quadraturePanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(quadratureTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(quadratureButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addContainerGap(28, Short.MAX_VALUE))
//        );
//        quadraturePanelLayout.setVerticalGroup(
//            quadraturePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(quadraturePanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(quadraturePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(quadratureTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(quadratureButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        GroupLayout priorTabLayout = new GroupLayout(priorTab);
//        priorTab.setLayout(priorTabLayout);
//        priorTabLayout.setHorizontalGroup(
//            priorTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(priorTabLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(priorTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
//                    .addComponent(quadraturePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                    .addComponent(normalPriorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                .addContainerGap(25, Short.MAX_VALUE))
//        );
//        priorTabLayout.setVerticalGroup(
//            priorTabLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(priorTabLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(normalPriorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(quadraturePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addContainerGap(38, Short.MAX_VALUE))
//        );
//
//        scoringTabbedPane.addTab("Prior", priorTab);
//
//        GroupLayout layout = new GroupLayout(getContentPane());
//        getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
//                    .addComponent(scoringTabbedPane, GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
//                    .addComponent(vsp, GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        layout.setVerticalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addGap(7, 7, 7)
//                .addComponent(scoringTabbedPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addContainerGap())
//        );
//
//        pack();
//    }// </editor-fold>

    public boolean canRun(){
        return canRun;
    }

    public IrtPersonScoringCommand getCommand(){
        return command;
    }

    private void setDisplay(){
        meanTextField.setEnabled(false);
        sdTextField.setEnabled(false);
        numPointsTextField.setEnabled(false);
        quadratureTextField.setEnabled(false);
        quadratureButton.setEnabled(false);

        if(useMap || useEap){
            meanTextField.setEnabled(true);
            sdTextField.setEnabled(true);
        }

        if(useEap){
            numPointsTextField.setEnabled(true);
            quadratureTextField.setEnabled(true);
            quadratureButton.setEnabled(true);
        }
    }

    private void resetDialog(){
        mleCheckBox.setSelected(true);
        useMle = true;
        eapCheckbox.setSelected(false);
        useEap = false;
        mapCheckBox.setSelected(false);
        useMap = false;
        itemParameterTextField.setText("");
        quadratureTextField.setText("");
        if(quadratureDialog!=null){
            quadratureDialog.dispose();
            quadratureDialog=null;
        }
        convergenceTextField.setText("1e-10");
        maxIterTextField.setText("100");
        ignoreRadioButton.setSelected(true);
        logisticScaleRadioButton.setSelected(true);
        minTextField.setText("-6.0");
        maxTextField.setText("6.0");
        meanTextField.setText("0.0");
        meanTextField.setEnabled(false);
        numPointsTextField.setText("60");
        numPointsTextField.setEnabled(false);
        sdTextField.setText("1.0");
        sdTextField.setEnabled(false);
        nameTextField.setText("");
        canRun = false;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener {
        public void actionPerformed(ActionEvent evt){
            try{
                command = new IrtPersonScoringCommand();
                boolean allInput = true;

                if(vsp.getSelectedVariables().length<=1){
                    JOptionPane.showMessageDialog(IrtPersonScoringDialog.this,
                            "You must select at least two variables.",
                            "Variable Selection Error",
                            JOptionPane.ERROR_MESSAGE);
                    allInput = false;
                }

                //data information--------------------------------------------------------------------------------------
                Object[] v = vsp.getSelectedVariables();
                for(int i=0;i<v.length;i++){
                    command.getFreeOptionList("variables").addValue(((VariableAttributes) v[i]).getName().toString());
                }
                command.getPairedOptionList("data").addValue("db", dbName.toString());
                command.getPairedOptionList("data").addValue("table", tableName.toString());

                //item parameter table----------------------------------------------------------------------------------
                if("".equals(itemParameterTextField.getText().trim()) && allInput==true){
                    JOptionPane.showMessageDialog(IrtPersonScoringDialog.this,
                            "You must select an item parameter table.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    allInput = false;
                }else{
                    DataTableName ipTable = new DataTableName(itemParameterTextField.getText().trim());
                    command.getPairedOptionList("iptable").addValue("db", dbName.toString());
                    command.getPairedOptionList("iptable").addValue("table", ipTable.toString());
                }

                //estimation method-------------------------------------------------------------------------------------
                command.getSelectAllOption("method").setSelected("mle", useMle);
                command.getSelectAllOption("method").setSelected("map", useMap);
                command.getSelectAllOption("method").setSelected("eap", useEap);

                //missing data------------------------------------------------------------------------------------------
                command.getSelectOneOption("missing").setSelected(missingButtonGroup.getSelection().getActionCommand());

                //default scale-----------------------------------------------------------------------------------------
                command.getSelectOneOption("scale").setSelected(scaleButtonGroup.getSelection().getActionCommand());

                //bounds------------------------------------------------------------------------------------------------
                double min = -6.0;
                double max = 6.0;
                String minString = minTextField.getText().trim();
                if(!"".equals(minString)){
                    min = Double.parseDouble(minString);
                }

                String maxString = maxTextField.getText().trim();
                if(!"".equals(maxString)){
                    max = Double.parseDouble(maxString);
                }

                if(max<min){
                    double temp = max;
                    max = min;
                    min = temp;
                }

                command.getPairedOptionList("bounds").addValue("min", min);
                command.getPairedOptionList("bounds").addValue("max", max);

                //prior-------------------------------------------------------------------------------------------------
                if(useEap || useMap){
                    double mean = 0.0;
                    double sd = 1.0;
                    String m = meanTextField.getText().trim();
                    if(!"".equals(m)){
                        mean = Double.parseDouble(m);
                    }

                    String s = sdTextField.getText().trim();
                    if(!"".equals(s)){
                        sd = Double.parseDouble(s);
                    }

                    command.getPairedOptionList("normprior").addValue("mean", mean);
                    command.getPairedOptionList("normprior").addValue("sd", sd);

                }

                if(useEap){
                    int numPoints = 60;
                    String np = numPointsTextField.getText().trim();
                    if(!"".equals(np)){
                        numPoints = Integer.parseInt(np);
                    }
                    command.getFreeOption("numpoints").add(numPoints);


                    //quadrature information
                    if(quadratureDialog!=null){
                        if(quadratureDialog.canRun() && quadratureDialog.hasWeight()){//checks that user provided point and weight variable
                            DataTableName quadTable = quadratureDialog.getTableName();
                            command.getPairedOptionList("quad").addValue("db", dbName.toString());
                            command.getPairedOptionList("quad").addValue("table", quadTable.toString());
                            command.getPairedOptionList("quad").addValue("theta", quadratureDialog.getTheta().getName().toString());
                            command.getPairedOptionList("quad").addValue("weight", quadratureDialog.getWeight().getName().toString());
                            useQuadrature = true;
                        }
                    }

                }

                //convergence criteria----------------------------------------------------------------------------------
                int maxIter = 100;
                double conv = 1e-5;
                String mi = maxIterTextField.getText().trim();
                String cn = convergenceTextField.getText().trim();

                if(!"".equals(mi)){
                    maxIter = Integer.parseInt(mi);
                }

                if(!"".equals(cn)){
                    conv = Double.parseDouble(mi);
                    if(conv<0 || conv > 1.0){
                        conv = 1e-5;
                    }
                }
                command.getPairedOptionList("criteria").addValue("converge", conv);
                command.getPairedOptionList("criteria").addValue("maxiter", maxIter);

                //new variable name-------------------------------------------------------------------------------------
                String newName = nameTextField.getText().trim();
                if("".equals(newName) && allInput==true){
                    JOptionPane.showMessageDialog(IrtPersonScoringDialog.this,
                            "You must provide a new variable name.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                    allInput = false;
                }else{
                    VariableName vName = new VariableName(newName);
                    command.getFreeOption("name").add(vName.toString());
                }

                if(allInput){
                    canRun = true;
                    setVisible(false);
                }else{
                    canRun = false;
                }




            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(IrtPersonScoringDialog.this,
                        ex.getMessage(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }



    }


}

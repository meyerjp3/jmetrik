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

package com.itemanalysis.jmetrik.stats.irt.rasch;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.dao.DerbyDatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.gui.ItemParameterTableDialog;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.selector.MultipleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.ArrayList;

public class RaschDialog extends JDialog{

    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private MultipleSelectionPanel vsp = null;
    private JPanel mainPanel;
	private boolean canRun = false;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    private RaschCommand command = null;

    private boolean uconbias = false;
    private boolean saveItem = false;
    private boolean showStart = false;
    private boolean savePersonEst = false;
    private boolean savePersonFit = false;
    private boolean saveResiduals = false;

    private boolean ignoreMissingData = true;
    private boolean pcaResidual = false;
    private boolean itemCentering = true;

//    private ButtonGroup missingDataGroup = null;
//    private ButtonGroup centeringButtonGroup = null;

    private JTextField itemOutputText = null;
    private JTextField globalConvergenceText = null;
    private JTextField globalMaxIterText = null;
    private JTextField globalMeanText = null;
    private JTextField globalScaleText = null;
    private JTextField globalPrecisionText = null;
    private JTextField extremeScoreText = null;
    private JTextField residualText = null;

    private JTabbedPane tabs = null;

    private DefaultTreeModel treeModel = null;
    private ItemParameterTableDialog startDialog = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;

    private SortedListModel<DataTableName> tableListModel = null;

    public RaschDialog(JFrame parent, Connection conn, DatabaseName dbName, DataTableName tableName, SortedListModel<DataTableName> tableListModel,
                       ArrayList <VariableAttributes> variables){
        super(parent,"Rasch Models",true);
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        this.dbName = dbName;
        this.tableName = tableName;
        this.conn = conn;
        this.tableListModel = tableListModel;

//        centeringButtonGroup = new ButtonGroup();

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

        mainPanel=new JPanel();
		mainPanel=new JPanel();
		mainPanel.setLayout(new GridBagLayout());

//		selectionPanel= new JPanel();
//		selectionPanel.setLayout(new GridBagLayout());
//		selectionPanel.setPreferredSize(new Dimension(350,200));

		vsp=new MultipleSelectionPanel();

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

        vsp.showButton3(false);

        JButton b4 = vsp.getButton4();
        b4.setText("Clear");
        b4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vsp.reset();
            }
        });

		GridBagConstraints c = new GridBagConstraints();

//        create variable selection panel
//		c.gridx = 0;
//		c.gridy = 0;
//		c.gridwidth = 6;
//		c.gridheight = 5;
//		c.weightx = 2;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.CENTER;
//		c.fill = GridBagConstraints.BOTH;
//		selectionPanel.addArgument(vsp,c);

        tabs = new JTabbedPane();
        tabs.setPreferredSize(new Dimension(350, 260));
        tabs.addTab("Global", getGlobalPanel());
        tabs.addTab("Item", getItemPanel());
        tabs.addTab("Person", getPersonPanel());
        tabs.setSelectedIndex(0);

        //      ================================================
//		add components to main panel
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 6;
		c.gridheight = 2;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(vsp,c);

//        add final options panel
        c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 6;
		c.gridheight = 3;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
		mainPanel.add(tabs,c);

        //add main panel to this
        getContentPane().add(mainPanel,BorderLayout.CENTER);
		pack();
		setResizable(false);
		setLocationRelativeTo(parent);

    }

    public final JPanel getItemPanel(){
        JPanel panel = new JPanel();
//        panel.setPreferredSize(new Dimension(350, 250));
        panel.setLayout(new GridLayout(2,1));
        panel.add(getItemOptionsPanel());
        panel.add(getItemEstimationPanel());
        return panel;
    }

    public final JPanel getItemOptionsPanel(){
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Item Options"));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        final JCheckBox uconBox = new JCheckBox("Correct UCON bias");
        uconBox.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent evt){
               if(uconBox.isSelected()){
                   uconbias = true;
               }else{
                   uconbias = false;
               }
           }

        });
        uconBox.setSelected(false);
        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(uconBox,c);

//        final JRadioButton centerItemsRadioButton = new JRadioButton("Center on items");
//        centerItemsRadioButton.setActionCommand("items");
//        centerItemsRadioButton.setSelected(true);
//        centeringButtonGroup.add(centerItemsRadioButton);
//        c.gridx = 3;
//        c.gridy = 0;
//        c.gridwidth = 6;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        panel.add(centerItemsRadioButton,c);

        final JCheckBox showStartBox = new JCheckBox("Show start values");
        showStartBox.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent evt){
               if(showStartBox.isSelected()){
                   showStart=true;
               }else{
                   showStart=false;
               }
           }

        });
        showStartBox.setSelected(false);
        c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 6;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(showStartBox,c);

        itemOutputText = new JTextField(25);
        itemOutputText.setText("");
        itemOutputText.setEnabled(false);
        final JCheckBox saveItemEstBox = new JCheckBox("Save item estimates");
        saveItemEstBox.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent evt){
               if(saveItemEstBox.isSelected()){
                   itemOutputText.setEnabled(true);
                   saveItem = true;
               }else{
                   itemOutputText.setEnabled(false);
                   saveItem = false;
               }
           }
        });


        c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(saveItemEstBox,c);
        c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 4;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(itemOutputText,c);

        return panel;
    }

    public final JPanel getItemEstimationPanel(){
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Item Estimation"));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel startLabel = new JLabel("Fixed parameter values ");
        JButton startValueButton = new JButton(new ItemStartAction("Select"));
        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(startLabel,c);
        c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(startValueButton,c);

//        JLabel newtonLabel = new JLabel("Max Newton-Rhapson Updates: ");
//        itemMaxNewtonText = new JTextField(25);
//        itemMaxNewtonText.setText("10");
//        c.gridx = 0;
//		c.gridy = 1;
//		c.gridwidth = 4;
//		c.gridheight = 1;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		panel.addArgument(newtonLabel,c);
//        c.gridx = 4;
//		c.gridy = 1;
//		c.gridwidth = 2;
//		c.gridheight = 1;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		panel.addArgument(itemMaxNewtonText,c);
//
//        JLabel convergenceLabel = new JLabel("Newton-Rhapson Convergence Criterion: ");
//        itemConvergenceText = new JTextField(25);
//        itemConvergenceText.setText("0.01");
//        c.gridx = 0;
//		c.gridy = 2;
//		c.gridwidth = 4;
//		c.gridheight = 1;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		panel.addArgument(convergenceLabel,c);
//        c.gridx = 4;
//		c.gridy = 2;
//		c.gridwidth = 2;
//		c.gridheight = 1;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		panel.addArgument(itemConvergenceText,c);

        return panel;

    }

    public final JPanel getPersonPanel(){
        JPanel panel = new JPanel();
//        panel.setPreferredSize(new Dimension(350, 250));
        panel.setLayout(new GridLayout(2,1));
        panel.add(getPersonOptionsPanel());
        panel.add(getPersonEstimationPanel());
        return panel;
    }

    public final JPanel getPersonOptionsPanel(){
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Person Options"));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();


        final JCheckBox savePersonEstBox = new JCheckBox("Save person estimates");
        savePersonEstBox.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent evt){
               if(savePersonEstBox.isSelected()){
                   savePersonEst = true;
               }else{
                   savePersonEst = false;
               }
           }

        });
        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(savePersonEstBox, c);

//        final JRadioButton centerPersonsRadioButton = new JRadioButton("Center on persons");
//        centerPersonsRadioButton.setActionCommand("persons");
//        centeringButtonGroup.add(centerPersonsRadioButton);
//        c.gridx = 3;
//        c.gridy = 0;
//        c.gridwidth = 3;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        panel.add(centerPersonsRadioButton, c);

        final JCheckBox savePersonFitBox = new JCheckBox("Save person fit statistics");
        savePersonFitBox.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent evt){
               if(savePersonFitBox.isSelected()){
                   savePersonFit = true;
               }else{
                   savePersonFit = false;
               }
           }

        });
        c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 6;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(savePersonFitBox, c);

        residualText = new JTextField(25);
        residualText.setText("");
        residualText.setEnabled(false);
        final JCheckBox saveResidualBox = new JCheckBox("Save residuals");
        saveResidualBox.addActionListener(new ActionListener(){
           public void actionPerformed(ActionEvent evt){
               if(saveResidualBox.isSelected()){
                   residualText.setEnabled(true);
                   saveResiduals = true;
               }else{
                   residualText.setEnabled(false);
                   saveResiduals = false;
               }
           }
        });


        c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(saveResidualBox,c);
        c.gridx = 2;
		c.gridy = 2;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 4;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(residualText,c);


        return panel;
    }

    public final JPanel getPersonEstimationPanel(){
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Person Estimation"));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();


        JLabel startLabel = new JLabel("Fixed parameter values ");
        JButton startValueButton = new JButton(new PersonStartAction("Select"));
        startValueButton.setEnabled(false);
        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 4;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(startLabel,c);
        c.gridx = 4;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(startValueButton,c);

//        JLabel newtonLabel = new JLabel("Max Newton-Rhapson Updates: ");
//        personMaxNewtonText = new JTextField(25);
//        personMaxNewtonText.setText("10");
//        c.gridx = 0;
//		c.gridy = 1;
//		c.gridwidth = 4;
//		c.gridheight = 1;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		panel.addArgument(newtonLabel,c);
//        c.gridx = 4;
//		c.gridy = 1;
//		c.gridwidth = 2;
//		c.gridheight = 1;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		panel.addArgument(personMaxNewtonText,c);
//
//        JLabel convergenceLabel = new JLabel("Newton-Rhapson Convergence Criterion: ");
//        personConvergenceText = new JTextField(25);
//        personConvergenceText.setText("0.01");
//        c.gridx = 0;
//		c.gridy = 2;
//		c.gridwidth = 4;
//		c.gridheight = 1;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		panel.addArgument(convergenceLabel,c);
//        c.gridx = 4;
//		c.gridy = 2;
//		c.gridwidth = 2;
//		c.gridheight = 1;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		panel.addArgument(personConvergenceText,c);

        return panel;


    }

    public final JPanel getGlobalPanel(){
        JPanel panel = new JPanel();
//        panel.setPreferredSize(new Dimension(350, 250));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 6;
		c.gridheight = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
        panel.add(getGlobalEstimationPanel(), c);

        c.gridx = 0;
		c.gridy = 4;
		c.gridwidth = 3;
		c.gridheight = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
        panel.add(getMissingDataPanel(), c);

        c.gridx = 3;
		c.gridy = 4;
		c.gridwidth = 3;
		c.gridheight = 4;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.BOTH;
        panel.add(getLinearTransformationPanel(), c);

        return panel;
    }

    public final JPanel getGlobalEstimationPanel(){
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Global Estimation"));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel maxIterLabel = new JLabel("Max iterations ");
        globalMaxIterText = new JTextField(25);
        globalMaxIterText.setText("150");
        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(maxIterLabel,c);
        c.gridx = 3;
		c.gridy = 0;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(globalMaxIterText,c);

        JLabel convergenceLabel = new JLabel("Convergence criterion ");
        globalConvergenceText = new JTextField(25);
        globalConvergenceText.setText("0.005");
        c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(convergenceLabel,c);
        c.gridx = 3;
		c.gridy = 1;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(globalConvergenceText,c);

        JLabel extremeAdjustmentLabel = new JLabel("Extreme score adjustment ");
        extremeScoreText = new JTextField(25);
        extremeScoreText.setText("0.3");
        c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(extremeAdjustmentLabel,c);
        c.gridx = 3;
		c.gridy = 2;
		c.gridwidth = 3;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(extremeScoreText,c);

        return panel;
    }

    public JPanel getMissingDataPanel(){
//        JPanel panel = new JPanel();
//        panel.setBorder(new TitledBorder("Missing Data"));
//        panel.setLayout(new GridBagLayout());
//        GridBagConstraints c = new GridBagConstraints();
//
//        missingDataGroup = new ButtonGroup();
//        JRadioButton ignoreButton = new JRadioButton("Ignore");
//        ignoreButton.setSelected(true);
//        ignoreButton.setActionCommand("ignore");
//        missingDataGroup.add(ignoreButton);
//        c.gridx = 0;
//		c.gridy = 0;
//		c.gridwidth = 4;
//		c.gridheight = 1;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		panel.add(ignoreButton,c);
//
//        JRadioButton zeroButton = new JRadioButton("Score as zero");
//        zeroButton.setActionCommand("zero");
//        missingDataGroup.add(zeroButton);
//        c.gridx = 0;
//		c.gridy = 1;
//		c.gridwidth = 4;
//		c.gridheight = 1;
//		c.weightx = 1;
//		c.weighty = 1;
//		c.anchor = GridBagConstraints.NORTHWEST;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		panel.add(zeroButton,c);
//        return panel;


        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Options"));
        panel.setLayout(new GridLayout(3, 1));

        final JCheckBox ignoreCheckBox = new JCheckBox("Ignore missing data");
        ignoreCheckBox.setSelected(true);
        ignoreCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(ignoreCheckBox.isSelected()){
                    ignoreMissingData = true;
                }else{
                    ignoreMissingData = false;
                }
            }
        });
        panel.add(ignoreCheckBox);

        final JCheckBox pcaCheckBox = new JCheckBox("PCA of Std Residuals");
        pcaCheckBox.setSelected(false);
        pcaCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(pcaCheckBox.isSelected()){
                    pcaResidual = true;
                }else{
                    pcaResidual = false;
                }
            }
        });
        panel.add(pcaCheckBox);

        final JCheckBox itemCenterCheckBox = new JCheckBox("Center on items");
        itemCenterCheckBox.setSelected(true);
        itemCenterCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(itemCenterCheckBox.isSelected()){
                    itemCentering = true;
                }else{
                    itemCentering = false;
                }
            }
        });
        panel.add(itemCenterCheckBox);
        return panel;



    }

    public JPanel getLinearTransformationPanel(){
        JPanel panel = new JPanel();
        panel.setBorder(new TitledBorder("Linear Transformation"));
        panel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JLabel meanLabel = new JLabel("Mean ");
        globalMeanText = new JTextField(25);
        globalMeanText.setText("0");
        c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(meanLabel,c);
        c.gridx = 2;
		c.gridy = 0;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(globalMeanText,c);

        JLabel scaleLabel = new JLabel("Scale ");
        globalScaleText = new JTextField(25);
        globalScaleText.setText("1");
        c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(scaleLabel,c);
        c.gridx = 2;
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(globalScaleText,c);
        JLabel precisionLabel = new JLabel("Precision ");
        globalPrecisionText = new JTextField(25);
        globalPrecisionText.setText("4");
        c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(precisionLabel,c);
        c.gridx = 2;
		c.gridy = 3;
		c.gridwidth = 2;
		c.gridheight = 1;
		c.weightx = 1;
		c.weighty = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		panel.add(globalPrecisionText,c);

        return panel;

    }

    public boolean canRun(){
        return canRun;
    }

    public RaschCommand getCommand(){
        return command;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener{

		public void actionPerformed(ActionEvent e){
            if(vsp.getSelectedVariables().length<=1){
				JOptionPane.showMessageDialog(RaschDialog.this,
						"You must select at least two variables.",
						"Variable Selection Error",
						JOptionPane.ERROR_MESSAGE);
			}else if(saveItem && itemOutputText.getText().trim().equals("")){
                JOptionPane.showMessageDialog(RaschDialog.this,
						"You must provide a name for the item output table.",
						"Item Output Error",
						JOptionPane.ERROR_MESSAGE);
            }else if(saveResiduals && residualText.getText().trim().equals("")){
                JOptionPane.showMessageDialog(RaschDialog.this,
						"You must provide a name for the residual output table.",
						"Item Output Error",
						JOptionPane.ERROR_MESSAGE);
            }

            else{
                try{
                    command = new RaschCommand();

                    //data information--------------------------------------------------------------------------------------
                    Object[] v = vsp.getSelectedVariables();
                    for(int i=0;i<v.length;i++){
                        command.getFreeOptionList("variables").addValue(((VariableAttributes) v[i]).getName().toString());
                    }
                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", tableName.toString());

                    //item options--------------------------------------------------------------------------------------
                    command.getSelectAllOption("item").setSelected("start", showStart);
                    command.getSelectAllOption("item").setSelected("uconbias", uconbias);
                    command.getSelectAllOption("item").setSelected("isave", saveItem);

                    if(itemOutputText.getText().trim().equals("")){
                        //show error
                    }else{
                        String temp = itemOutputText.getText().trim();
                        DataTableName itemTable = new DataTableName(temp);
                        command.getFreeOption("itemout").add(itemTable.toString());
                    }

                    if(residualText.getText().trim().equals("")){
                        //show error
                    }else{
                        String temp = residualText.getText().trim();
                        DataTableName residTable = new DataTableName(temp);
                        command.getFreeOption("residout").add(residTable.toString());
                    }

                    if(startDialog!=null && startDialog.canRun()){
                        ArrayList<VariableAttributes> n = startDialog.getSelectedVariables();
                        for(int i=0;i<n.size();i++){
                            command.getFreeOptionList("ifixed").addValue(n.get(i).toString());
                        }
                        command.getPairedOptionList("iptable").addValue("db", dbName.toString());
                        command.getPairedOptionList("iptable").addValue("table", startDialog.getSelectedTable().toString());
                    }

                    //person options--------------------------------------------------------------------------------------
                    command.getSelectAllOption("person").setSelected("psave", savePersonEst);
                    command.getSelectAllOption("person").setSelected("pfit", savePersonFit);
                    command.getSelectAllOption("person").setSelected("rsave", saveResiduals);

                    //estimation options--------------------------------------------------------------------------------------
//                    command.getSelectOneOption("missing").setSelected(missingDataGroup.getSelection().getActionCommand());
//                    command.getSelectOneOption("center").setSelected(centeringButtonGroup.getSelection().getActionCommand());

                    if(ignoreMissingData){
                        command.getSelectOneOption("missing").setSelected("ignore");
                    }else{
                        command.getSelectOneOption("missing").setSelected("zero");
                    }

                    if(itemCentering){
                        command.getSelectOneOption("center").setSelected("items");
                    }else{
                        command.getSelectOneOption("center").setSelected("persons");
                    }

                    if(pcaResidual){
                        command.getSelectOneOption("pca").setSelected("yes");
                    }else{
                        command.getSelectOneOption("pca").setSelected("no");
                    }

                    //global update
                    if(globalMaxIterText.getText().trim().equals("")){
                        command.getPairedOptionList("gupdate").addValue("maxiter", 150);
                    }else{
                        int m = Integer.parseInt(globalMaxIterText.getText().trim());
                        command.getPairedOptionList("gupdate").addValue("maxiter", m);
                    }

                    if(globalConvergenceText.getText().trim().equals("")){
                        command.getPairedOptionList("gupdate").addValue("converge", 0.005);
                    }else{
                        Double m = Double.parseDouble(globalConvergenceText.getText().trim());
                        command.getPairedOptionList("gupdate").addValue("converge", m);
                    }

                    if(globalMeanText.getText().trim().equals("")){
                        command.getPairedOptionList("transform").addValue("intercept", 0.0);
                    }else{
                        command.getPairedOptionList("transform").addValue("intercept",
                                Double.parseDouble(globalMeanText.getText().trim()));
                    }

                    if(globalScaleText.getText().trim().equals("")){
                        command.getPairedOptionList("transform").addValue("scale", 1.0);
                    }else{
                        command.getPairedOptionList("transform").addValue("scale",
                                Double.parseDouble(globalScaleText.getText().trim()));
                    }

                    if(globalPrecisionText.getText().trim().equals("")){
                        command.getPairedOptionList("transform").addValue("precision", 2);
                    }else{
                        command.getPairedOptionList("transform").addValue("precision",
                                Integer.parseInt(globalPrecisionText.getText().trim()));
                    }

                    if(extremeScoreText.getText().trim().equals("")){
                        command.getFreeOption("adjust").add(0.3);
                    }else{
                        command.getFreeOption("adjust").add(
                                Double.parseDouble(extremeScoreText.getText().trim()));
                    }

                }catch(IllegalArgumentException ex){
                    logger.fatal(ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(RaschDialog.this,
                            ex.getMessage(),
                            "Syntax Error",
                            JOptionPane.ERROR_MESSAGE);
                }
				canRun =true;
				setVisible(false);
			}
		}

	}//end RunAction

    public class ItemStartAction extends AbstractAction{

		private static final long serialVersionUID = 1L;
		final static String TOOL_TIP = "Cancel";

		public ItemStartAction(String text, ImageIcon icon, Integer mnemonic){
			super(text, icon);
			putValue(SHORT_DESCRIPTION, TOOL_TIP);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public ItemStartAction(String text, ImageIcon icon){
			super(text, icon);
			putValue(SHORT_DESCRIPTION, TOOL_TIP);
		}

		public ItemStartAction(String text){
			super(text);
			putValue(SHORT_DESCRIPTION, TOOL_TIP);
		}

		public void actionPerformed(ActionEvent e){
            //display item start value dialog
            if(startDialog == null){
//                startDialog = new RaschItemStartValuesDialog(RaschDialog.this, conn, dao, tableListModel);
                startDialog = new ItemParameterTableDialog(RaschDialog.this, conn, dao, tableListModel, "Select Fixed Items");
            }
            startDialog.setVisible(true);

		}

	}//end

    public class PersonStartAction extends AbstractAction{

		private static final long serialVersionUID = 1L;
		final static String TOOL_TIP = "Cancel";

		public PersonStartAction(String text, ImageIcon icon, Integer mnemonic){
			super(text, icon);
			putValue(SHORT_DESCRIPTION, TOOL_TIP);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		public PersonStartAction(String text, ImageIcon icon){
			super(text, icon);
			putValue(SHORT_DESCRIPTION, TOOL_TIP);
		}

		public PersonStartAction(String text){
			super(text);
			putValue(SHORT_DESCRIPTION, TOOL_TIP);
		}

		public void actionPerformed(ActionEvent e){
            //display person start value dialog
		}

	}//end

}


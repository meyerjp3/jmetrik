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

package com.itemanalysis.jmetrik.stats.irt.equating;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.dao.DerbyDatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.gui.ItemParameterTableDialog;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.stats.irt.linking.IrtLinkingThetaDialog;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.util.ArrayList;

public class IrtEquatingDialog extends JDialog {

    // Variables declaration - do not modify
    private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton formXItemButton;
    private JButton formXPersonButton;
    private JButton formYItemButton;
    private JButton formYPersonButton;
    private JPanel itemParamPanel;
    private JRadioButton logisticRadioButton;
    private ButtonGroup methodButtonGroup;
    private JPanel methodPanel;
    private JRadioButton normalRadioButton;
    private JRadioButton observedScoreRadioButton;
    private JButton okButton;
    private JPanel outputPanel;
    private JTextField outputTextField;
    private JPanel personParamPanel;
    private JButton resetButton;
    private ButtonGroup scaleButtonGroup;
    private JPanel scalePanel;
    private JLabel tableNameLabel;
    private JRadioButton trueScoreRadioButton;
    // End of variables declaration

//    private SelectTableDialog itemDialogX = null;
//    private SelectTableDialog itemDialogY = null;
    private ItemParameterTableDialog itemDialogX = null;
    private ItemParameterTableDialog itemDialogY = null;
    private IrtLinkingThetaDialog thetaDialogX = null;
    private IrtLinkingThetaDialog thetaDialogY = null;
    private DataTableName tableX = null;
    private DataTableName tableY = null;
    private DataTableName tableXtheta = null;
    private DataTableName tableYtheta = null;
    private static String FORMX = "Form X";
    private static String FORMY = "Form Y";

    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private DatabaseName dbName = null;
    private SortedListModel<DataTableName> tableListModel = null;
    private boolean canRun = false;
    private IrtEquatingCommand command = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public IrtEquatingDialog(JFrame parent, Connection conn, DatabaseName dbName, SortedListModel<DataTableName> tableListModel){
        super(parent, "IRT Score Equating", true);
        this.conn = conn;
        this.dbName = dbName;
        this.tableListModel = tableListModel;

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);

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

        scaleButtonGroup = new ButtonGroup();
        methodButtonGroup = new ButtonGroup();
        itemParamPanel = new JPanel();
        formXItemButton = new JButton();
        formXItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(itemDialogX==null){
                    itemDialogX = new ItemParameterTableDialog(IrtEquatingDialog.this, conn, dao, tableListModel, "Select Form X Items");
//                    itemDialogX = new SelectTableDialog(IrtEquatingDialog.this, dbName, tableListModel);
                }
                itemDialogX.setVisible(true);

//                if(itemDialogX.canRun()){
//                    tableX = itemDialogX.getSelectedTable();
//                }
            }
        });

        formYItemButton = new JButton();
        formYItemButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(itemDialogY==null){
                    itemDialogY = new ItemParameterTableDialog(IrtEquatingDialog.this, conn, dao, tableListModel, "Select Form Y Items");
//                    itemDialogY = new SelectTableDialog(IrtEquatingDialog.this, dbName, tableListModel);
                }
                itemDialogY.setVisible(true);

//                if(itemDialogY.canRun()){
//                    tableY = itemDialogY.getSelectedTable();
//                }
            }
        });

        personParamPanel = new JPanel();
        formXPersonButton = new JButton();
        formXPersonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(thetaDialogX==null){
                    thetaDialogX = new IrtLinkingThetaDialog(IrtEquatingDialog.this, conn, dao, tableListModel, FORMX);
                }
                thetaDialogX.setVisible(true);
            }
        });
        formXPersonButton.setText("Select Form X");
        formXPersonButton.setEnabled(false);

        formYPersonButton = new JButton();
        formYPersonButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(thetaDialogY==null){
                    thetaDialogY = new IrtLinkingThetaDialog(IrtEquatingDialog.this, conn, dao, tableListModel, FORMX);
                }
                thetaDialogY.setVisible(true);
            }
        });
        formYPersonButton.setText("Select Form Y");
        formYPersonButton.setEnabled(false);

        scalePanel = new JPanel();
        logisticRadioButton = new JRadioButton();
        logisticRadioButton.setActionCommand("logistic");

        normalRadioButton = new JRadioButton();
        normalRadioButton.setActionCommand("normal");

        methodPanel = new JPanel();
        trueScoreRadioButton = new JRadioButton();
        trueScoreRadioButton.setActionCommand("true");

        observedScoreRadioButton = new JRadioButton();
        observedScoreRadioButton.setActionCommand("observed");
        outputPanel = new JPanel();
        tableNameLabel = new JLabel();
        outputTextField = new JTextField();
        buttonPanel = new JPanel();
        okButton = new JButton();
        okButton.addActionListener(new OkActionListener());

        cancelButton = new JButton();
        resetButton = new JButton();
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                reset();
            }
        });

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("IRT Score Equating");

        itemParamPanel.setBorder(BorderFactory.createTitledBorder("Item Parameters"));

        formXItemButton.setText("Select Form X");

        formYItemButton.setText("Select Form Y");

        GroupLayout itemParamPanelLayout = new GroupLayout(itemParamPanel);
        itemParamPanel.setLayout(itemParamPanelLayout);
        itemParamPanelLayout.setHorizontalGroup(
            itemParamPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemParamPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemParamPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(formXItemButton)
                    .addComponent(formYItemButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        itemParamPanelLayout.setVerticalGroup(
            itemParamPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemParamPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(formXItemButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formYItemButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        personParamPanel.setBorder(BorderFactory.createTitledBorder("Person Parameters"));

        GroupLayout personParamPanelLayout = new GroupLayout(personParamPanel);
        personParamPanel.setLayout(personParamPanelLayout);
        personParamPanelLayout.setHorizontalGroup(
            personParamPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(personParamPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(personParamPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(formXPersonButton)
                    .addComponent(formYPersonButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        personParamPanelLayout.setVerticalGroup(
            personParamPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(personParamPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(formXPersonButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(formYPersonButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        scalePanel.setBorder(BorderFactory.createTitledBorder("Default Scale"));

        scaleButtonGroup.add(logisticRadioButton);
        logisticRadioButton.setSelected(true);
        logisticRadioButton.setText("Logistic (D = 1.0)");

        scaleButtonGroup.add(normalRadioButton);
        normalRadioButton.setText("Normal (D = 1.7)");
        normalRadioButton.setToolTipText("");

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
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        methodPanel.setBorder(BorderFactory.createTitledBorder("Equating Method"));

        methodButtonGroup.add(trueScoreRadioButton);
        trueScoreRadioButton.setSelected(true);
        trueScoreRadioButton.setText("True score");

        methodButtonGroup.add(observedScoreRadioButton);
        observedScoreRadioButton.setText("Observed score");
        observedScoreRadioButton.setEnabled(false);

        GroupLayout methodPanelLayout = new GroupLayout(methodPanel);
        methodPanel.setLayout(methodPanelLayout);
        methodPanelLayout.setHorizontalGroup(
            methodPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(methodPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(methodPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(trueScoreRadioButton)
                    .addComponent(observedScoreRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        methodPanelLayout.setVerticalGroup(
            methodPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(methodPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(trueScoreRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(observedScoreRadioButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        outputPanel.setBorder(BorderFactory.createTitledBorder("Output Table"));

        tableNameLabel.setText("Table Name ");

        GroupLayout outputPanelLayout = new GroupLayout(outputPanel);
        outputPanel.setLayout(outputPanelLayout);
        outputPanelLayout.setHorizontalGroup(
            outputPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(outputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableNameLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputTextField)
                .addContainerGap())
        );
        outputPanelLayout.setVerticalGroup(
            outputPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(outputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(outputPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(tableNameLabel)
                    .addComponent(outputTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        okButton.setText("Run");
        okButton.setMaximumSize(new Dimension(72, 28));
        okButton.setMinimumSize(new Dimension(72, 28));
        okButton.setPreferredSize(new Dimension(72, 28));
        buttonPanel.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun = false;
                setVisible(false);
            }
        });
        cancelButton.setMaximumSize(new Dimension(72, 28));
        cancelButton.setMinimumSize(new Dimension(72, 28));
        cancelButton.setPreferredSize(new Dimension(72, 28));
        buttonPanel.add(cancelButton);

        resetButton.setText("Reset");
        resetButton.setMaximumSize(new Dimension(72, 28));
        resetButton.setMinimumSize(new Dimension(72, 28));
        resetButton.setPreferredSize(new Dimension(72, 28));
        buttonPanel.add(resetButton);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                            .addComponent(methodPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(itemParamPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(personParamPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scalePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(outputPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(personParamPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(itemParamPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scalePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(methodPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(outputPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    public boolean canRun(){
        return canRun;
    }

    public IrtEquatingCommand getCommand(){
        return command;
    }

    public void reset(){
        itemDialogX = null;
        itemDialogY = null;
        thetaDialogX = null;
        thetaDialogY = null;

        trueScoreRadioButton.setSelected(true);
        logisticRadioButton.setSelected(true);
        outputTextField.setText("");

    }


    public class OkActionListener implements ActionListener {


        public void actionPerformed(ActionEvent evt){
            command = new IrtEquatingCommand();

            try{
                boolean hasItem = false;
                boolean hasItemX = false;
                boolean hasItemY = false;
                boolean hasThetaX = false;
                boolean hasThetaY = false;
                boolean hasObserved = false;

                if(itemDialogX!=null && itemDialogX.canRun()){
                    tableX = itemDialogX.getSelectedTable();
                    command.getPairedOptionList("xitem").addValue("db", dbName.toString());
                    command.getPairedOptionList("xitem").addValue("table", tableX.toString());

                    ArrayList<VariableAttributes> varInfo = itemDialogX.getSelectedVariables();
                    for(VariableAttributes v: varInfo){
                        command.getFreeOptionList("xvar").addValue(v.getName().toString());
                    }

                    hasItemX = true;
                }else{
                    JOptionPane.showMessageDialog(IrtEquatingDialog.this,
                            "Be sure you have selected Form X items.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
                if(itemDialogY!=null && itemDialogY.canRun()){
                    tableY = itemDialogY.getSelectedTable();
                    command.getPairedOptionList("yitem").addValue("db", dbName.toString());
                    command.getPairedOptionList("yitem").addValue("table", tableY.toString());

                    ArrayList<VariableAttributes> varInfo = itemDialogY.getSelectedVariables();
                    for(VariableAttributes v: varInfo){
                        command.getFreeOptionList("yvar").addValue(v.getName().toString());
                    }

                    hasItemY = true;
                }else{
                    JOptionPane.showMessageDialog(IrtEquatingDialog.this,
                            "Be sure you have selected Form Y items.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }

                hasItem = (hasItemX && hasItemY);


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
//                        JOptionPane.showMessageDialog(IrtEquatingDialog.this,
//                                "Be sure you have selected a Form X person parameter.",
//                                "Error",
//                                JOptionPane.ERROR_MESSAGE);
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
//                        JOptionPane.showMessageDialog(IrtEquatingDialog.this,
//                                "Be sure you have selected a Form Y person parameter.",
//                                "Error",
//                                JOptionPane.ERROR_MESSAGE);
                    }

                    hasObserved = (hasThetaX && hasThetaY);

                command.getSelectOneOption("scale").setSelected(scaleButtonGroup.getSelection().getActionCommand());
                command.getSelectOneOption("method").setSelected(methodButtonGroup.getSelection().getActionCommand());

                String oTable = outputTextField.getText().trim();

                if("".equals(oTable)){
                    //output table will not be created
                }else{
                    DataTableName dbTableName = new DataTableName(oTable);
                    command.getPairedOptionList("output").addValue("db", dbName.toString());
                    command.getPairedOptionList("output").addValue("table", dbTableName.toString());
                }

                canRun = (hasItem);
                if(canRun){
                    setVisible(false);
//                    System.out.println(command.paste());
                }


            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(IrtEquatingDialog.this,
                        ex.getMessage(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }


}

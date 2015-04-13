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

package com.itemanalysis.jmetrik.stats.cmh;

import com.itemanalysis.jmetrik.gui.TableNameDialog;
import com.itemanalysis.jmetrik.selector.MultipleSelectionXYByGroupPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class CmhDialog extends JDialog {

    // Variables declaration - do not modify
    private JPanel difCodePanel;
    private ButtonGroup effectSizeButtonGroup;
    private JPanel effectSizePanel;
    private JRadioButton etsDeltaRadioButton;
    private JLabel focalLabel;
    private JTextField focalTextField;
    private JCheckBox frequencyTablesCheckBox;
    private JCheckBox scoreZeroCheckBox;
    private JRadioButton oddsRatioRadioButton;
    private JPanel optionPanel;
    private JLabel referenceLabel;
    private JTextField referenceTextField;
    // End of variables declaration

    private MultipleSelectionXYByGroupPanel vsp = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private boolean canRun = false;
    private CmhCommand command = null;
    private boolean showFrequencyTables = false;
    private boolean scoreAsZero = true;
    private TableNameDialog tableNameDialog = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");


    public CmhDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList<VariableAttributes> variables){
        super(parent,"Cochran-Mantel-Haenszel DIF",true);
        this.dbName = dbName;
        this.tableName = tableName;

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        vsp = new MultipleSelectionXYByGroupPanel();
        vsp.setIndependentVariableLabel("Matching Variable");
        vsp.setPreferredSize(new Dimension(410, 272));
        vsp.setMinimumSize(new Dimension(410,272));
        vsp.setMaximumSize(new Dimension(410,272));

        //filter out nonitems from selected list
//        VariableType filterType1 = new VariableType(VariableType.NOT_ITEM, VariableType.STRING);
//        VariableType filterType2 = new VariableType(VariableType.NOT_ITEM, VariableType.DOUBLE);
//        vsp.addSelectedFilterType(filterType1);
//        vsp.addSelectedFilterType(filterType2);

        vsp.addSelectedFilterItemType(ItemType.NOT_ITEM);

        //filter out items from groupby list
//        VariableType filterType3 = new VariableType(VariableType.BINARY_ITEM, VariableType.STRING);
//        VariableType filterType4 = new VariableType(VariableType.BINARY_ITEM, VariableType.DOUBLE);
//        VariableType filterType5 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.STRING);
//        VariableType filterType6 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.DOUBLE);
//        VariableType filterType7 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.STRING);
//        VariableType filterType8 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.DOUBLE);
//        vsp.addGroupByFilterType(filterType3);
//        vsp.addGroupByFilterType(filterType4);
//        vsp.addGroupByFilterType(filterType5);
//        vsp.addGroupByFilterType(filterType6);
//        vsp.addGroupByFilterType(filterType7);
//        vsp.addGroupByFilterType(filterType8);

        vsp.addGroupByFilterItemType(ItemType.BINARY_ITEM);
        vsp.addGroupByFilterItemType(ItemType.POLYTOMOUS_ITEM);
        vsp.addGroupByFilterItemType(ItemType.CONTINUOUS_ITEM);

        //filter out strings and items from matching variable
//        VariableType filterType9 = new VariableType(VariableType.NOT_ITEM, VariableType.STRING);
//        vsp.addIndependentVariableFilterType(filterType3);
//        vsp.addIndependentVariableFilterType(filterType4);
//        vsp.addIndependentVariableFilterType(filterType5);
//        vsp.addIndependentVariableFilterType(filterType6);
//        vsp.addIndependentVariableFilterType(filterType7);
//        vsp.addIndependentVariableFilterType(filterType8);
//        vsp.addIndependentVariableFilterType(filterType9);

        vsp.addIndependentVariableFilterDataType(DataType.STRING);
        vsp.addIndependentVariableFilterItemType(ItemType.BINARY_ITEM);
        vsp.addIndependentVariableFilterItemType(ItemType.POLYTOMOUS_ITEM);
        vsp.addIndependentVariableFilterItemType(ItemType.CONTINUOUS_ITEM);

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
        b3.setText("Save");
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(tableNameDialog==null) tableNameDialog = new TableNameDialog(CmhDialog.this);
                tableNameDialog.setVisible(true);
            }
        });

        JButton b4 = vsp.getButton4();
        b4.setText("Clear");
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

    }

    private void initComponents() {

        effectSizeButtonGroup = new ButtonGroup();
        difCodePanel = new JPanel();
        referenceLabel = new JLabel();
        focalLabel = new JLabel();
        focalTextField = new JTextField();
        referenceTextField = new JTextField();
        effectSizePanel = new JPanel();
        oddsRatioRadioButton = new JRadioButton();
        etsDeltaRadioButton = new JRadioButton();
        optionPanel = new JPanel();
        frequencyTablesCheckBox = new JCheckBox();
        scoreZeroCheckBox = new JCheckBox();

        difCodePanel.setBorder(BorderFactory.createTitledBorder("DIF Group Codes"));

        referenceLabel.setText("Reference");

        focalLabel.setText("Focal");

        focalTextField.setMaximumSize(new Dimension(100, 28));
        focalTextField.setMinimumSize(new Dimension(100, 28));
        focalTextField.setPreferredSize(new Dimension(100, 28));

        referenceTextField.setMaximumSize(new Dimension(100, 28));
        referenceTextField.setMinimumSize(new Dimension(100, 28));
        referenceTextField.setPreferredSize(new Dimension(100, 28));

        GroupLayout difCodePanelLayout = new GroupLayout(difCodePanel);
        difCodePanel.setLayout(difCodePanelLayout);
        difCodePanelLayout.setHorizontalGroup(
            difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(difCodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(referenceLabel)
                    .addComponent(focalLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(focalTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(referenceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(23, Short.MAX_VALUE))
        );
        difCodePanelLayout.setVerticalGroup(
            difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(difCodePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(focalLabel)
                    .addComponent(focalTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3)
                .addGroup(difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(referenceLabel)
                    .addComponent(referenceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(13, Short.MAX_VALUE))
        );

        effectSizePanel.setBorder(BorderFactory.createTitledBorder("Binary Item Effect Size"));

        effectSizeButtonGroup.add(oddsRatioRadioButton);
        oddsRatioRadioButton.setSelected(true);
        oddsRatioRadioButton.setText("Common odds ratio");
        oddsRatioRadioButton.setActionCommand("odds");

        effectSizeButtonGroup.add(etsDeltaRadioButton);
        etsDeltaRadioButton.setText("ETS delta");
        etsDeltaRadioButton.setActionCommand("ets");

        GroupLayout effectSizePanelLayout = new GroupLayout(effectSizePanel);
        effectSizePanel.setLayout(effectSizePanelLayout);
        effectSizePanelLayout.setHorizontalGroup(
            effectSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(effectSizePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(effectSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(oddsRatioRadioButton)
                    .addComponent(etsDeltaRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        effectSizePanelLayout.setVerticalGroup(
            effectSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(effectSizePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(oddsRatioRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(etsDeltaRadioButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        optionPanel.setBorder(BorderFactory.createTitledBorder("Options"));

        frequencyTablesCheckBox.setText("Show frequency tables");
        frequencyTablesCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(frequencyTablesCheckBox.isSelected()){
                    showFrequencyTables = true;
                }else{
                    showFrequencyTables = false;
                }
            }
        });

        scoreZeroCheckBox.setSelected(true);
        scoreZeroCheckBox.setText("Score as zero");
        scoreZeroCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(scoreZeroCheckBox.isSelected()){
                    scoreAsZero = true;
                }else{
                    scoreAsZero = false;
                }
            }
        });

        GroupLayout optionPanelLayout = new GroupLayout(optionPanel);
        optionPanel.setLayout(optionPanelLayout);
        optionPanelLayout.setHorizontalGroup(
            optionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(frequencyTablesCheckBox)
                .addGap(18, 18, 18)
                .addComponent(scoreZeroCheckBox)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        optionPanelLayout.setVerticalGroup(
            optionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(frequencyTablesCheckBox)
                    .addComponent(scoreZeroCheckBox))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(difCodePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(effectSizePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(optionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(difCodePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(effectSizePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
//    private void initComponents() {
//
//        effectSizeButtonGroup = new ButtonGroup();
//        difCodePanel = new JPanel();
//        referenceLabel = new JLabel();
//        focalLabel = new JLabel();
//        focalTextField = new JTextField();
//        referenceTextField = new JTextField();
//        effectSizePanel = new JPanel();
//        oddsRatioRadioButton = new JRadioButton();
//        oddsRatioRadioButton.setActionCommand("odds");
//        etsDeltaRadioButton = new JRadioButton();
//        etsDeltaRadioButton.setActionCommand("ets");
//        optionPanel = new JPanel();
//        frequencyTablesCheckBox = new JCheckBox();
//
//        difCodePanel.setBorder(BorderFactory.createTitledBorder("DIF Group Codes"));
//
//        referenceLabel.setText("Reference");
//
//        focalLabel.setText("Focal");
//
//        focalTextField.setMaximumSize(new Dimension(100, 28));
//        focalTextField.setMinimumSize(new Dimension(100, 28));
//        focalTextField.setPreferredSize(new Dimension(100, 28));
//
//        referenceTextField.setMaximumSize(new Dimension(100, 28));
//        referenceTextField.setMinimumSize(new Dimension(100, 28));
//        referenceTextField.setPreferredSize(new Dimension(100, 28));
//
//        GroupLayout difCodePanelLayout = new GroupLayout(difCodePanel);
//        difCodePanel.setLayout(difCodePanelLayout);
//        difCodePanelLayout.setHorizontalGroup(
//            difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(difCodePanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
//                    .addComponent(referenceLabel)
//                    .addComponent(focalLabel))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addGroup(difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addComponent(focalTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                    .addComponent(referenceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addContainerGap(23, Short.MAX_VALUE))
//        );
//        difCodePanelLayout.setVerticalGroup(
//            difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(difCodePanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(focalLabel)
//                    .addComponent(focalTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addGap(3, 3, 3)
//                .addGroup(difCodePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
//                    .addComponent(referenceLabel)
//                    .addComponent(referenceTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        effectSizePanel.setBorder(BorderFactory.createTitledBorder("Binary Item Effect Size"));
//
//        effectSizeButtonGroup.addArgument(oddsRatioRadioButton);
//        oddsRatioRadioButton.setSelected(true);
//        oddsRatioRadioButton.setText("Common odds ratio");
//
//        effectSizeButtonGroup.addArgument(etsDeltaRadioButton);
//        etsDeltaRadioButton.setText("ETS delta");
//
//        GroupLayout effectSizePanelLayout = new GroupLayout(effectSizePanel);
//        effectSizePanel.setLayout(effectSizePanelLayout);
//        effectSizePanelLayout.setHorizontalGroup(
//            effectSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(effectSizePanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(effectSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//                    .addComponent(oddsRatioRadioButton)
//                    .addComponent(etsDeltaRadioButton))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        effectSizePanelLayout.setVerticalGroup(
//            effectSizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(effectSizePanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(oddsRatioRadioButton)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                .addComponent(etsDeltaRadioButton)
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        optionPanel.setBorder(BorderFactory.createTitledBorder("Options"));
//
//        frequencyTablesCheckBox.setText("Show frequency tables");
//        frequencyTablesCheckBox.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                if(frequencyTablesCheckBox.isSelected()){
//                    showFrequencyTables = true;
//                }else{
//                    showFrequencyTables = false;
//                }
//            }
//        });
//
//        GroupLayout optionPanelLayout = new GroupLayout(optionPanel);
//        optionPanel.setLayout(optionPanelLayout);
//        optionPanelLayout.setHorizontalGroup(
//            optionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(optionPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(frequencyTablesCheckBox)
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        optionPanelLayout.setVerticalGroup(
//            optionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(optionPanelLayout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(frequencyTablesCheckBox)
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        GroupLayout layout = new GroupLayout(getContentPane());
//        getContentPane().setLayout(layout);
//        layout.setHorizontalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
//                    .addComponent(vsp, GroupLayout.PREFERRED_SIZE, 400, GroupLayout.PREFERRED_SIZE)
//                    .addGroup(layout.createSequentialGroup()
//                            .addComponent(difCodePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                            .addComponent(effectSizePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                    .addComponent(optionPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//        layout.setVerticalGroup(
//            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
//            .addGroup(layout.createSequentialGroup()
//                .addContainerGap()
//                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
//                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
//                    .addComponent(difCodePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
//                    .addComponent(effectSizePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
//                .addComponent(optionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
//                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//        );
//
//        pack();
//
//    }// </editor-fold>



    public boolean canRun(){
        return canRun;
    }

    public CmhCommand getCommand(){
        return command;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }


    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            if(vsp.getSelectedVariables().length>0){
                if(!vsp.hasGroupingVariable()){
                    JOptionPane.showMessageDialog(CmhDialog.this,
                            "You must select a DIF group variable.",
                            "No Dif Group Selected",
                            JOptionPane.ERROR_MESSAGE);
                    canRun=false;
                }else if(!vsp.hasIndependentVariable()){
                    JOptionPane.showMessageDialog(CmhDialog.this,
                            "You must select a matching variable",
                            "No Matching Variable Selected",
                            JOptionPane.ERROR_MESSAGE);
                    canRun=false;
                }else if(referenceTextField.getText().equals("") && focalTextField.getText().equals("")){
                    JOptionPane.showMessageDialog(CmhDialog.this,
                            "You must specify a Focal Code and a Reference Code",
                            "No Group Codes Found",
                            JOptionPane.ERROR_MESSAGE);
                    canRun=false;
                }else if(focalTextField.getText().equals("")){
                    JOptionPane.showMessageDialog(CmhDialog.this,
                            "You must specify a Focal Code",
                            "No Focal Code Found",
                            JOptionPane.ERROR_MESSAGE);
                    canRun=false;
                }else if(referenceTextField.getText().equals("")){
                    JOptionPane.showMessageDialog(CmhDialog.this,
                            "You must specify a Reference Code",
                            "No Reference Code Found",
                            JOptionPane.ERROR_MESSAGE);
                    canRun=false;
                }else{

                    try{
                        command = new CmhCommand();
                        Object[] v = vsp.getSelectedVariables();
                        for(int i=0;i<v.length;i++){
                            command.getFreeOptionList("variables").addValue(((VariableAttributes) v[i]).getName().toString());
                        }
                        command.getPairedOptionList("data").addValue("db", dbName.toString());
                        command.getPairedOptionList("data").addValue("table", tableName.toString());

                        VariableAttributes groupVar = vsp.getGroupByVariable();
                        command.getFreeOption("groupvar").add(groupVar.getName().toString());

                        VariableAttributes matchVar = vsp.getIndependentVariable();
                        command.getFreeOption("matchvar").add(matchVar.getName().toString());

                        command.getSelectOneOption("effectsize").setSelected(effectSizeButtonGroup.getSelection().getActionCommand());

                        command.getPairedOptionList("codes").addValue("focal", focalTextField.getText().trim());
                        command.getPairedOptionList("codes").addValue("reference", referenceTextField.getText().trim());
                        command.getSelectAllOption("options").setSelected("tables", showFrequencyTables);
                        command.getSelectAllOption("options").setSelected("zero", scoreAsZero);

                        command.getSelectAllOption("options").setSelected("noprint", false);//FIXME add option to dialog

                        if(tableNameDialog!=null && tableNameDialog.canRun()){
                            command.getPairedOptionList("output").addValue("db", dbName.toString());
                            command.getPairedOptionList("output").addValue("table", tableNameDialog.getTableName());
                        }

                    }catch(IllegalArgumentException ex){
                        logger.fatal(ex.getMessage(), ex);
                        firePropertyChange("error", "", "Error - Check log for details.");
                    }

                    canRun=true;
                    setVisible(false);
                }
            }
        }

    }//end RunAction



}

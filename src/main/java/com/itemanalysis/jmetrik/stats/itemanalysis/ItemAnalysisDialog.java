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

package com.itemanalysis.jmetrik.stats.itemanalysis;

import com.itemanalysis.jmetrik.gui.TableNameDialog;
import com.itemanalysis.jmetrik.selector.MultipleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class ItemAnalysisDialog extends JDialog {

    // Variables declaration - do not modify
    private JCheckBox allOptionsCheckBox;
    private ButtonGroup correlationButtonGroup;
    private JPanel correlationPanel;
    private JCheckBox csemCheckBox;
    private JTextField cutScoreTextField;
    private JPanel cutscorePanel;
    private JCheckBox itemDeletedCheckBox;
    private JCheckBox itemStatisticsCheckBox;
    private JCheckBox listwiseCheckBox;
    private JPanel optionPanel;
    private JRadioButton pearsonRadioButton;
    private JRadioButton polyserialRadioButton;
    private JCheckBox showHeadersCheckBox;
    private JCheckBox spuriousCheckBox;
    private JCheckBox unbiasedCheckBox;
    // End of variables declaration

    private MultipleSelectionPanel vsp;
    private boolean canRun =false;
    private boolean spur = true;
    private boolean header = false;
    private boolean all = true;
    private boolean scores = false;
    private boolean delrel = false;
    private boolean listwise=false;
    private boolean istats=true;
    private boolean csem=false;
    private boolean unbiased=false;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private ItemAnalysisCommand command = null;
    private TableNameDialog tableNameDialog = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public ItemAnalysisDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList<VariableInfo> variables){
        super(parent, "Item Analysis",true);
        this.dbName = dbName;
        this.tableName = tableName;

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        vsp = new MultipleSelectionPanel();

        //filter out nonitems
        VariableType filterType1 = new VariableType(VariableType.NOT_ITEM, VariableType.STRING);
        VariableType filterType2 = new VariableType(VariableType.NOT_ITEM, VariableType.DOUBLE);
        vsp.addUnselectedFilterType(filterType1);
        vsp.addUnselectedFilterType(filterType2);
        vsp.addSelectedFilterType(filterType1);
        vsp.addSelectedFilterType(filterType2);
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
                if(tableNameDialog==null) tableNameDialog = new TableNameDialog(ItemAnalysisDialog.this);
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


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        correlationButtonGroup = new ButtonGroup();
        optionPanel = new JPanel();
        itemStatisticsCheckBox = new JCheckBox();
        spuriousCheckBox = new JCheckBox();
        itemDeletedCheckBox = new JCheckBox();
        csemCheckBox = new JCheckBox();
        allOptionsCheckBox = new JCheckBox();
        showHeadersCheckBox = new JCheckBox();
        listwiseCheckBox = new JCheckBox();
        unbiasedCheckBox = new JCheckBox();
        cutscorePanel = new JPanel();
        cutScoreTextField = new JTextField();
        correlationPanel = new JPanel();
        pearsonRadioButton = new JRadioButton();
        polyserialRadioButton = new JRadioButton();

        optionPanel.setBorder(BorderFactory.createTitledBorder("Options"));
        optionPanel.setMinimumSize(new Dimension(400, 130));
        optionPanel.setPreferredSize(new Dimension(400, 123));
        optionPanel.setLayout(new GridLayout(4, 2, 0, 6));

        itemStatisticsCheckBox.setSelected(true);
        itemStatisticsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(itemDeletedCheckBox.isSelected()){
                    istats = true;
                }else{
                    istats = false;
                }
            }
        });
        itemStatisticsCheckBox.setText("Compute item statistics");
        optionPanel.add(itemStatisticsCheckBox);

        spuriousCheckBox.setSelected(true);
        spuriousCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(spuriousCheckBox.isSelected()){
                    spur = true;
                }else{
                    spur = false;
                }
            }
        });
        spuriousCheckBox.setText("Correct for spuriousness");
        optionPanel.add(spuriousCheckBox);

        itemDeletedCheckBox.setText("Item deleted reliability");
        itemDeletedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(itemDeletedCheckBox.isSelected()){
                    delrel = true;
                }else{
                    delrel = false;
                }
            }
        });
        optionPanel.add(itemDeletedCheckBox);

        csemCheckBox.setText("CSEM");
        csemCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(csemCheckBox.isSelected()){
                    csem = true;
                }else{
                    csem = false;
                }
            }
        });
        csemCheckBox.setToolTipText("Conditoinal standard error of measurement");
        optionPanel.add(csemCheckBox);

        allOptionsCheckBox.setSelected(true);
        allOptionsCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(allOptionsCheckBox.isSelected()){
                    all = true;
                }else{
                    all = false;
                }
            }
        });
        allOptionsCheckBox.setText("All response options");
        optionPanel.add(allOptionsCheckBox);

        showHeadersCheckBox.setText("Show headers");
        showHeadersCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(showHeadersCheckBox.isSelected()){
                    header = true;
                }else{
                    header = false;
                }
            }
        });
        optionPanel.add(showHeadersCheckBox);

        listwiseCheckBox.setText("Listwise deletion");
        listwiseCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(listwiseCheckBox.isSelected()){
                    listwise = true;
                }else{
                    listwise = false;
                }
            }
        });
        optionPanel.add(listwiseCheckBox);

        unbiasedCheckBox.setText("Unbiased covariance");
        unbiasedCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(unbiasedCheckBox.isSelected()){
                    unbiased = true;
                }else{
                    unbiased = false;
                }
            }
        });
        unbiasedCheckBox.setToolTipText("Use N-1 in denominator of covariances for reliability estimate");
        optionPanel.add(unbiasedCheckBox);

        cutscorePanel.setBorder(BorderFactory.createTitledBorder("Cut Score(s)"));
        cutscorePanel.setToolTipText("Space delimited list of cut scores");
        cutscorePanel.setPreferredSize(new Dimension(195, 73));

        cutScoreTextField.setMaximumSize(new Dimension(150, 28));
        cutScoreTextField.setMinimumSize(new Dimension(150, 28));
        cutScoreTextField.setPreferredSize(new Dimension(150, 28));

        GroupLayout cutscorePanelLayout = new GroupLayout(cutscorePanel);
        cutscorePanel.setLayout(cutscorePanelLayout);
        cutscorePanelLayout.setHorizontalGroup(
            cutscorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(cutscorePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cutScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        cutscorePanelLayout.setVerticalGroup(
            cutscorePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(cutscorePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cutScoreTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        correlationPanel.setBorder(BorderFactory.createTitledBorder("Item-total Correlation Type"));
        correlationPanel.setPreferredSize(new Dimension(190, 86));

        correlationButtonGroup.add(pearsonRadioButton);
        pearsonRadioButton.setSelected(true);
        pearsonRadioButton.setActionCommand("pearson");
        pearsonRadioButton.setText("Pearson correlation");

        correlationButtonGroup.add(polyserialRadioButton);
        polyserialRadioButton.setText("Polyserial correlation");
        polyserialRadioButton.setActionCommand("polyserial");

        GroupLayout correlationPanelLayout = new GroupLayout(correlationPanel);
        correlationPanel.setLayout(correlationPanelLayout);
        correlationPanelLayout.setHorizontalGroup(
            correlationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(correlationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(correlationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(pearsonRadioButton)
                    .addComponent(polyserialRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        correlationPanelLayout.setVerticalGroup(
            correlationPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(correlationPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pearsonRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(polyserialRadioButton)
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
                            .addComponent(cutscorePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(correlationPanel, GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE))
                    .addComponent(optionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(optionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(correlationPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(cutscorePanel, GroupLayout.PREFERRED_SIZE, 86, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    public boolean canRun(){
        return canRun;
    }

    public ItemAnalysisCommand getCommand(){
        return command;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            Object[] v = vsp.getSelectedVariables();
            if(vsp.getSelectedVariables().length>0){
                try{
                    command = new ItemAnalysisCommand();

                    for(int i=0;i<v.length;i++){
                        command.getFreeOptionList("variables").addValue(((VariableInfo) v[i]).getName().toString());
                    }

                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", tableName.toString());

                    command.getSelectAllOption("options").setSelected("spur", spur);
                    command.getSelectAllOption("options").setSelected("header", header);
                    command.getSelectAllOption("options").setSelected("all", all);
                    command.getSelectAllOption("options").setSelected("scores", scores);
                    command.getSelectAllOption("options").setSelected("delrel", delrel);
                    command.getSelectAllOption("options").setSelected("csem", csem);
                    command.getSelectAllOption("options").setSelected("istats", istats);
                    command.getSelectAllOption("options").setSelected("unbiased", unbiased);

                    command.getSelectOneOption("correlation").setSelected(correlationButtonGroup.getSelection().getActionCommand());

                    if(listwise){
                        command.getSelectOneOption("missing").setSelected("listwise");
                    }else{
                        command.getSelectOneOption("missing").setSelected("zero");
                    }

                    if(tableNameDialog!=null && tableNameDialog.canRun()){
                        command.getPairedOptionList("output").addValue("db", dbName.toString());
                        command.getPairedOptionList("output").addValue("table", tableNameDialog.getTableName());
                    }


                    if(!cutScoreTextField.getText().equals("")){
                        String[] cs = cutScoreTextField.getText().trim().split("\\s+");
                        for(int i=0;i<cs.length;i++){
                            command.getFreeOptionList("cut").addValue(new Integer(cs[i]));
                        }
                    }
                    canRun =true;
                    setVisible(false);
                }catch(IllegalArgumentException ex){
                    logger.fatal(ex.getMessage(), ex);
                    firePropertyChange("error", "", "Error - Check log for details.");
                }
            }
        }

    }//end RunAction

}

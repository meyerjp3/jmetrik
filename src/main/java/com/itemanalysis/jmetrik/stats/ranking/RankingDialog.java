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

package com.itemanalysis.jmetrik.stats.ranking;

import javax.swing.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.ButtonGroup;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import com.itemanalysis.jmetrik.selector.SingleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import org.apache.log4j.Logger;

public class RankingDialog extends JDialog {

    // Variables declaration - do not modify
    private JRadioButton ascendingRadioButton;
    private JRadioButton descendingRadioButton;
    private JTextField groupTextField;
    private JComboBox groupsComboBox;
    private JTextField nameTextField;
    private JLabel newVariableLabel;
    private ButtonGroup orderButtonGroup;
    private JPanel orderPanel;
    private JPanel rankPanel;
    private JLabel scoreLabel;
    private JComboBox tiesComboBox;
    private JPanel tiesPanel;
    // End of variables declaration

    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private RankingCommand command = null;
    private SingleSelectionPanel vsp = null;
    private boolean canRun=false;
    private String tiesName = "max";
    private String[] tiesNameArray = {"Sequential", "Min", "Average", "Max", "Random"};
    private String[] scoreNameArray = {"Rank", "Quartiles", "Deciles", "Percentile ranks", "Ntiles",
            "Blom normal score", "Tukey normal score", "van der Waerden NS"};
    private int ntiles = 100;
    private String scoreName = "rank";
    static Logger logger = Logger.getLogger("jmetrik-logger");
    private HashMap<String, String> tiesMap = null;
    private HashMap<String, String> scoreNameMap = null;

    public RankingDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList <VariableAttributes> variables){
        super(parent,"Rank Values",true);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.dbName = dbName;
        this.tableName = tableName;

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        vsp = new SingleSelectionPanel();

        //filter out strings from unselected list and selected list
//        VariableType filterType1 = new VariableType(ItemType.BINARY_ITEM, DataType.STRING);
//        VariableType filterType2 = new VariableType(ItemType.POLYTOMOUS_ITEM, DataType.STRING);
//        VariableType filterType3 = new VariableType(ItemType.CONTINUOUS_ITEM, DataType.STRING);
//        VariableType filterType4 = new VariableType(ItemType.NOT_ITEM, DataType.STRING);
//        vsp.addUnselectedFilterType(filterType1);
//        vsp.addUnselectedFilterType(filterType2);
//        vsp.addUnselectedFilterType(filterType3);
//        vsp.addUnselectedFilterType(filterType4);
//        vsp.addSelectedFilterType(filterType1);
//        vsp.addSelectedFilterType(filterType2);
//        vsp.addSelectedFilterType(filterType3);
//        vsp.addSelectedFilterType(filterType4);

        vsp.addUnselectedFilterDataType(DataType.STRING);
        vsp.addSelectedFilterDataType(DataType.STRING);

        vsp.setVariables(variables);
        vsp.showButton4(false);

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
            }
        });

        initComponents();

        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        orderButtonGroup = new ButtonGroup();
        rankPanel = new JPanel();
        newVariableLabel = new JLabel();
        nameTextField = new JTextField();
        scoreLabel = new JLabel();
        groupsComboBox = new JComboBox();
        groupTextField = new JTextField();
        tiesPanel = new JPanel();
        tiesComboBox = new JComboBox();
        orderPanel = new JPanel();
        ascendingRadioButton = new JRadioButton();
        descendingRadioButton = new JRadioButton();

        rankPanel.setBorder(BorderFactory.createTitledBorder("Rank Variable"));

        newVariableLabel.setText("New Variable Name");

        nameTextField.setMaximumSize(new Dimension(200, 28));
        nameTextField.setMinimumSize(new Dimension(200, 28));
        nameTextField.setPreferredSize(new Dimension(200, 28));

        scoreLabel.setText("Score Type");

        groupsComboBox.setModel(new DefaultComboBoxModel(scoreNameArray));
        groupsComboBox.setSelectedItem("Rank");
        groupsComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String s = (String)cb.getSelectedItem();
                if("Quartiles".equals(s)){
                    ntiles = 4;
                    scoreName = "ntiles";
                    groupTextField.setEnabled(false);
                    groupTextField.setText("4");
                }else if("Deciles".equals(s)){
                    ntiles = 10;
                    scoreName = "ntiles";
                    groupTextField.setEnabled(false);
                    groupTextField.setText("10");
                }else if("Percentile Ranks".equals(s)){
                    ntiles = 100;
                    scoreName = "ntiles";
                    groupTextField.setEnabled(false);
                    groupTextField.setText("100");
                }else if("Ntiles".equals(s)){
                    ntiles = 0;
                    scoreName = "ntiles";
                    groupTextField.setEnabled(true);
                    groupTextField.setText("");
                    groupTextField.requestFocus();
                }else if("Blom Normal Score".equals(s)){
                    ntiles = -1;
                    scoreName = "blom";
                    groupTextField.setEnabled(false);
                    groupTextField.setText("");
                }else if("Tukey Normal Score".equals(s)){
                    ntiles = -1;
                    scoreName = "tukey";
                    groupTextField.setEnabled(false);
                    groupTextField.setText("");
                }else if("van der Waerden NS".equals(s)){
                    ntiles = -1;
                    scoreName = "vdw";
                    groupTextField.setEnabled(false);
                    groupTextField.setText("");
                }else{
                    ntiles = -1;
                    scoreName = "rank";
                    groupTextField.setEnabled(false);
                    groupTextField.setText("");
                }

            }
        });
        groupsComboBox.setMaximumSize(new Dimension(200, 28));
        groupsComboBox.setMinimumSize(new Dimension(200, 28));
        groupsComboBox.setPreferredSize(new Dimension(200, 28));

        groupTextField.setEnabled(false);
        groupTextField.setMaximumSize(new Dimension(50, 28));
        groupTextField.setMinimumSize(new Dimension(50, 28));
        groupTextField.setPreferredSize(new Dimension(50, 28));

        GroupLayout rankPanelLayout = new GroupLayout(rankPanel);
        rankPanel.setLayout(rankPanelLayout);
        rankPanelLayout.setHorizontalGroup(
            rankPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(rankPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rankPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addComponent(scoreLabel)
                    .addComponent(newVariableLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rankPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(rankPanelLayout.createSequentialGroup()
                        .addComponent(groupsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(groupTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(40, Short.MAX_VALUE))
        );
        rankPanelLayout.setVerticalGroup(
            rankPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(rankPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(rankPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(newVariableLabel)
                    .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(rankPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(scoreLabel)
                    .addComponent(groupsComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(groupTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tiesPanel.setBorder(BorderFactory.createTitledBorder("Ties Method"));

        tiesComboBox.setModel(new DefaultComboBoxModel(tiesNameArray));
        tiesComboBox.setSelectedItem("Max");
        tiesComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox)e.getSource();
                String s = (String)cb.getSelectedItem();
                tiesName = s.toLowerCase();
            }
        });
        tiesComboBox.setMaximumSize(new Dimension(200, 28));
        tiesComboBox.setMinimumSize(new Dimension(200, 28));
        tiesComboBox.setPreferredSize(new Dimension(200, 28));

        GroupLayout tiesPanelLayout = new GroupLayout(tiesPanel);
        tiesPanel.setLayout(tiesPanelLayout);
        tiesPanelLayout.setHorizontalGroup(
            tiesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(tiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tiesComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        tiesPanelLayout.setVerticalGroup(
            tiesPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(tiesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tiesComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        orderPanel.setBorder(BorderFactory.createTitledBorder("Order"));

        orderButtonGroup.add(ascendingRadioButton);
        ascendingRadioButton.setSelected(true);
        ascendingRadioButton.setText("Ascending");
        ascendingRadioButton.setActionCommand("asc");

        orderButtonGroup.add(descendingRadioButton);
        descendingRadioButton.setText("Descending");
        descendingRadioButton.setActionCommand("desc");

        GroupLayout orderPanelLayout = new GroupLayout(orderPanel);
        orderPanel.setLayout(orderPanelLayout);
        orderPanelLayout.setHorizontalGroup(
            orderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(orderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(orderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(ascendingRadioButton)
                    .addComponent(descendingRadioButton))
                .addContainerGap(77, Short.MAX_VALUE))
        );
        orderPanelLayout.setVerticalGroup(
            orderPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(orderPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ascendingRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(descendingRadioButton)
                .addContainerGap(20, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tiesPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                        .addComponent(vsp, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(rankPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(rankPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(tiesPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(orderPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>


    public boolean canRun(){
        return canRun;
    }

    public RankingCommand getCommand(){
        return command;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            try{
                command = new RankingCommand();
                VariableAttributes v = vsp.getSelectedVariables();

                command.getFreeOption("variable").add(v.getName().toString());
                command.getFreeOption("label").add(v.getLabel().toString());

                command.getPairedOptionList("data").addValue("db", dbName.toString());
                command.getPairedOptionList("data").addValue("table", tableName.toString());

                command.getSelectOneOption("ties").setSelected(tiesName);
                command.getSelectOneOption("type").setSelected(scoreName);
                command.getSelectOneOption("order").setSelected(orderButtonGroup.getSelection().getActionCommand());

                if(ntiles==0){
                    if(groupTextField.getText().trim().equals("")){
                        JOptionPane.showMessageDialog(RankingDialog.this,
                                "You must specify a the number of quantiles \n " +
                                        "when chossing Ntiles",
                                "Ranking Error",
                                JOptionPane.ERROR_MESSAGE);
                        canRun = false;
                    }else{
                        int numGroups = Math.abs(Integer.parseInt(groupTextField.getText().trim()));
                        numGroups = Math.max(0, Math.min(100, numGroups));
                        command.getFreeOption("ntiles").add(numGroups);
                        canRun = true;
                    }

                }else if(ntiles > 0){
                    //user typed number of groups
                    command.getFreeOption("ntiles").add(ntiles);
                    canRun = true;
                }else{
                    //normal score selected
                    canRun = true;
                }

                String n = nameTextField.getText().trim();
                if(n.equals("")) n = "rank";
                VariableName vName = new VariableName(n);
                command.getFreeOption("name").add(vName.toString());

                if(canRun) setVisible(false);
            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                firePropertyChange("error", "", "Error - Check log for details.");
            }

        }

    }//end RunAction


}

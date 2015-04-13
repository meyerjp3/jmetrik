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

package com.itemanalysis.jmetrik.graph.histogram;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.*;

import com.itemanalysis.jmetrik.selector.SingleSelectionByGroupPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.swing.ChartTitlesDialog;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;

public class HistogramDialog extends JDialog {

    private SingleSelectionByGroupPanel vsp = null;
    private String chartTitle = "";
    private String chartSubtitle = "";
    boolean canRun =false;
    private HistogramCommand command = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    // Variables declaration - do not modify
    private ButtonGroup binwidthGroup;
    private JRadioButton densityRadioButton;
    private JRadioButton frequencyRadioButton;
    private JPanel yaxisPanel;
    private JPanel binwidthPanel;
    private JRadioButton freedmanDiaconisRadioButton;
    private JRadioButton scottRadioButton;
    private JRadioButton sturgesRadioButton;
    private ButtonGroup yaxisButtonGroup;
    // End of variables declaration

    public HistogramDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList <VariableAttributes> variables){
        super(parent,"Histogram",true);
        this.dbName=dbName;
        this.tableName=tableName;
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        vsp = new SingleSelectionByGroupPanel();
        initComponents();

        //filter out strings
//        VariableType filterType1 = new VariableType(ItemType.BINARY_ITEM, DataType.STRING);
//        VariableType filterType2 = new VariableType(ItemType.POLYTOMOUS_ITEM, DataType.STRING);
//        VariableType filterType3 = new VariableType(ItemType.CONTINUOUS_ITEM, DataType.STRING);
//        VariableType filterType4 = new VariableType(ItemType.NOT_ITEM, DataType.STRING);
//        vsp.addSelectedFilterType(filterType1);
//        vsp.addSelectedFilterType(filterType2);
//        vsp.addSelectedFilterType(filterType3);
//        vsp.addSelectedFilterType(filterType4);

        vsp.addSelectedFilterDataType(DataType.STRING);
        vsp.setVariables(variables);

        JButton b1 = vsp.getButton1();
        b1.setText("Run");
        b1.addActionListener(new RunActionListener());

        JButton b2 = vsp.getButton2();
        b2.setText("Cancel");
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun=false;
                setVisible(false);
            }
        });

        JButton b3 = vsp.getButton3();
        b3.setText("Titles");
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VariableAttributes v = vsp.getSelectedVariable();
                if((v!=null)) chartTitle = v.getName().toString();
                ChartTitlesDialog titlesDialog = new ChartTitlesDialog(HistogramDialog.this, chartTitle, chartSubtitle);
                titlesDialog.setVisible(true);
                chartTitle = titlesDialog.getChartTitle();
                chartSubtitle = titlesDialog.getChartSubtitle();
            }
        });

        JButton b4 = vsp.getButton4();
        b4.setText("Reset");
        b4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vsp.reset();
                chartTitle = "";
                chartSubtitle = "";
                densityRadioButton.setSelected(true);
                sturgesRadioButton.setSelected(true);
            }
        });

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        setResizable(false);
        setLocationRelativeTo(parent);

    }

    private void initComponents() {

        yaxisButtonGroup = new ButtonGroup();
        binwidthGroup = new ButtonGroup();
        yaxisPanel = new JPanel();
        frequencyRadioButton = new JRadioButton();
        densityRadioButton = new JRadioButton();
        binwidthPanel = new JPanel();
        sturgesRadioButton = new JRadioButton();
        scottRadioButton = new JRadioButton();
        freedmanDiaconisRadioButton = new JRadioButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        vsp.setMaximumSize(new Dimension(400, 272));
        vsp.setMinimumSize(new Dimension(400, 272));
        vsp.setPreferredSize(new Dimension(400, 272));

        yaxisPanel.setBorder(BorderFactory.createTitledBorder("Y-axis"));

        yaxisButtonGroup.add(frequencyRadioButton);
        frequencyRadioButton.setText("Frequency");
        frequencyRadioButton.setActionCommand("freq");

        yaxisButtonGroup.add(densityRadioButton);
        densityRadioButton.setSelected(true);
        densityRadioButton.setText("Density");
        densityRadioButton.setActionCommand("density");

        GroupLayout jPanel2Layout = new GroupLayout(yaxisPanel);
        yaxisPanel.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(frequencyRadioButton)
                                        .addComponent(densityRadioButton))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(frequencyRadioButton)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(densityRadioButton)
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        binwidthPanel.setBorder(BorderFactory.createTitledBorder("Binwidth Type"));

        binwidthGroup.add(sturgesRadioButton);
        sturgesRadioButton.setSelected(true);
        sturgesRadioButton.setText("Sturges");
        sturgesRadioButton.setActionCommand("sturges");

        binwidthGroup.add(scottRadioButton);
        scottRadioButton.setText("Scott");
        scottRadioButton.setActionCommand("scott");

        binwidthGroup.add(freedmanDiaconisRadioButton);
        freedmanDiaconisRadioButton.setText("Freedman-Diaconis");
        freedmanDiaconisRadioButton.setActionCommand("fd");

        GroupLayout jPanel3Layout = new GroupLayout(binwidthPanel);
        binwidthPanel.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(sturgesRadioButton)
                        .addGap(18, 18, 18)
                        .addComponent(freedmanDiaconisRadioButton))
                    .addComponent(scottRadioButton))
                .addContainerGap(11, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sturgesRadioButton)
                    .addComponent(freedmanDiaconisRadioButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(scottRadioButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(yaxisPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(binwidthPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(binwidthPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(yaxisPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    public boolean canRun(){
        return canRun;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public HistogramCommand getCommand(){
        return command;
    }

    public class RunActionListener implements ActionListener {

        public void actionPerformed(ActionEvent e){

            try{
                if(vsp.selectionMade()){
                    command = new HistogramCommand();
                    VariableAttributes v = vsp.getSelectedVariable();
                    command.getFreeOption("variable").add(v.getName().toString());

                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", tableName.toString());

                    command.getSelectOneOption("yaxis").setSelected(yaxisButtonGroup.getSelection().getActionCommand());
                    command.getSelectOneOption("bintype").setSelected(binwidthGroup.getSelection().getActionCommand());

                    if("".equals(chartTitle)) chartTitle = v.getName().toString();
                    command.getFreeOption("title").add(chartTitle);
                    chartTitle = "";

                    if(!"".equals(chartSubtitle)) command.getFreeOption("subtitle").add(chartSubtitle);
                    chartSubtitle = "";

                    if(vsp.hasGroupingVariable()){
                        VariableAttributes groupVar = vsp.getGroupByVariable();
                        command.getFreeOption("groupvar").add(groupVar.getName().toString());
                    }

                    canRun =true;
                    setVisible(false);

                }else{
                    JOptionPane.showMessageDialog(HistogramDialog.this,
                            "You must select a variable.",
                            "Variable Selection Error",
                            JOptionPane.ERROR_MESSAGE);
                }

            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(HistogramDialog.this,
                        ex.getMessage(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        }

    }//end RunAction

}

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

package com.itemanalysis.jmetrik.graph.density;

import com.itemanalysis.jmetrik.selector.SingleSelectionByGroupPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.swing.ChartTitlesDialog;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class DensityDialog extends JDialog {

    private SingleSelectionByGroupPanel vsp = null;
    boolean canRun = false;
    private String kernelType="gaussian";
    private String chartTitle = "";
    private String chartSubtitle = "";
    private DensityCommand command = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    // Variables declaration - do not modify
    private JLabel bandwidthLabel;
    private JTextField bandwidthTextField;
    private JPanel optionPanel;
    private JComboBox typeComboBox;
    private JLabel typeLabel;
    // End of variables declaration

    public DensityDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList<VariableAttributes> variables){
        super(parent,"Kernel Density",true);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.dbName=dbName;
        this.tableName=tableName;

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        vsp = new SingleSelectionByGroupPanel();

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
                ChartTitlesDialog chartTitlesDialog = new ChartTitlesDialog(DensityDialog.this, chartTitle, chartSubtitle);
                chartTitlesDialog.setVisible(true);
                chartTitle = chartTitlesDialog.getChartTitle();
                chartSubtitle = chartTitlesDialog.getChartSubtitle();
            }
        });

        JButton b4 = vsp.getButton4();
        b4.setText("Reset");
        b4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                vsp.reset();
                bandwidthTextField.setText("1");
                typeComboBox.setSelectedItem("Gaussian");
            }
        });

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);

    }

    private void initComponents() {

        optionPanel = new JPanel();
        bandwidthLabel = new JLabel();
        bandwidthTextField = new JTextField();
        typeLabel = new JLabel();
        typeComboBox = new JComboBox();

        vsp.setMaximumSize(new Dimension(400, 272));
        vsp.setMinimumSize(new Dimension(400, 272));
        vsp.setPreferredSize(new Dimension(400, 272));

        optionPanel.setBorder(BorderFactory.createTitledBorder("Kernel Density Options"));

        bandwidthLabel.setText("Bandwidth adjustment");

        bandwidthTextField.setText("1");
        bandwidthTextField.setMaximumSize(new Dimension(50, 28));
        bandwidthTextField.setMinimumSize(new Dimension(50, 28));
        bandwidthTextField.setPreferredSize(new Dimension(50, 28));

        typeLabel.setText("Kernel type");

        typeComboBox.setModel(new DefaultComboBoxModel(new String[] { "Gaussian", "Epanechnikov", "Uniform", "Triangle", "Biweight", "Triweight", "Cosine" }));
        typeComboBox.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                JComboBox cb = (JComboBox)e.getSource();
                kernelType = (String)cb.getSelectedItem();
            }
        });

        GroupLayout optionPanelLayout = new GroupLayout(optionPanel);
        optionPanel.setLayout(optionPanelLayout);
        optionPanelLayout.setHorizontalGroup(
            optionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(optionPanelLayout.createSequentialGroup()
                        .addComponent(typeLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(typeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(optionPanelLayout.createSequentialGroup()
                        .addComponent(bandwidthLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bandwidthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        optionPanelLayout.setVerticalGroup(
            optionPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(optionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(optionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(typeComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(optionPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(bandwidthLabel)
                    .addComponent(bandwidthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(optionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(optionPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    public boolean canRun(){
        return canRun;
    }

    public DensityCommand getCommand(){
        return command;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){

            try{
                command = new DensityCommand();
                VariableAttributes v = vsp.getSelectedVariable();
                command.getFreeOption("variable").add(v.getName().toString());

                command.getPairedOptionList("data").addValue("db", dbName.toString());
                command.getPairedOptionList("data").addValue("table", tableName.toString());

                if("".equals(chartTitle)) chartTitle = v.getName().toString();
                command.getFreeOption("title").add(chartTitle);
                chartTitle = "";

                if(!"".equals(chartSubtitle)) command.getFreeOption("subtitle").add(chartSubtitle);
                chartSubtitle = "";

                command.getSelectOneOption("kernel").setSelected(kernelType.toLowerCase());

                String bw = bandwidthTextField.getText().trim();
                if(!bw.equals("")){
                    command.getFreeOption("adjust").add(Double.valueOf(bw));
                }


                if(vsp.hasGroupingVariable()){
                    VariableAttributes groupVar = vsp.getGroupByVariable();
                    command.getFreeOption("groupvar").add(groupVar.getName().toString());
                }

            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(DensityDialog.this,
                        ex.getMessage(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            if(vsp.selectionMade()){
                canRun =true;
                setVisible(false);
            }
        }

    }//end RunAction

}


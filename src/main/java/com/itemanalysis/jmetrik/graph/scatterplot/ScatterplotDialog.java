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

package com.itemanalysis.jmetrik.graph.scatterplot;

import com.itemanalysis.jmetrik.selector.SelectionXYByGroupPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.swing.ChartTitlesDialog;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

public class ScatterplotDialog extends JDialog{

    private SelectionXYByGroupPanel vsp;
    private String chartTitle = "";
    private String chartSubtitle = "";
    private boolean canRun=false;
    private boolean showLegend=true, showMarkers=true;
    private ScatterplotCommand command = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public ScatterplotDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList<VariableInfo> variables) {
        super(parent,"Scatterplot",true);
        this.dbName=dbName;
        this.tableName=tableName;

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        vsp = new SelectionXYByGroupPanel();
        //filter out strings
        VariableType filterType1 = new VariableType(VariableType.BINARY_ITEM, VariableType.STRING);
        VariableType filterType2 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.STRING);
        VariableType filterType3 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.STRING);
        VariableType filterType4 = new VariableType(VariableType.NOT_ITEM, VariableType.STRING);
        vsp.addSelectedXFilterType(filterType1);
        vsp.addSelectedXFilterType(filterType2);
        vsp.addSelectedXFilterType(filterType3);
        vsp.addSelectedXFilterType(filterType4);
        vsp.addSelectedYFilterType(filterType1);
        vsp.addSelectedYFilterType(filterType2);
        vsp.addSelectedYFilterType(filterType3);
        vsp.addSelectedYFilterType(filterType4);
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
                ChartTitlesDialog chartTitlesDialog = new ChartTitlesDialog(ScatterplotDialog.this, chartTitle, chartSubtitle);
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
            }
        });

        initComponents();

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        initComponents();
    }

    private void initComponents() {

        vsp.setMaximumSize(new Dimension(414, 272));
        vsp.setMinimumSize(new Dimension(414, 272));
        vsp.setPreferredSize(new Dimension(414, 272));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    public boolean canRun(){
        return canRun;
    }

    public ScatterplotCommand getCommand(){
        return command;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            try{
                command = new ScatterplotCommand();
                if(vsp.selectionMade()){
                    VariableInfo xVar = vsp.getSelectedXVariable();
                    VariableInfo yVar = vsp.getSelectedYVariable();
                    command.getFreeOption("xvar").add(xVar.getName().toString());
                    command.getFreeOption("yvar").add(yVar.getName().toString());

                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", tableName.toString());

                    if(vsp.hasGroupingVariable()){
                        command.getFreeOption("groupvar").add(
                                vsp.getGroupByVariable().getName().toString());
                    }

                    command.getSelectAllOption("options").setSelected("legend", showLegend);
                    command.getSelectAllOption("options").setSelected("markers", showMarkers);
                    command.getFreeOption("title").add(chartTitle);
                    if(!"".equals(chartSubtitle)) command.getFreeOption("subtitle").add(chartSubtitle);

                    canRun=true;
                    setVisible(false);
                }else{
                    JOptionPane.showMessageDialog(ScatterplotDialog.this,
                            "You must select variables for the X- and Y-axis",
                            "Variable Selection Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(ScatterplotDialog.this,
                        ex.getMessage(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }

    }//end RunAction

}

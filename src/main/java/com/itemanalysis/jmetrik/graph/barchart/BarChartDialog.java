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

package com.itemanalysis.jmetrik.graph.barchart;

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
import com.itemanalysis.psychometrics.data.VariableAttributes;
import org.apache.log4j.Logger;

public class BarChartDialog extends JDialog {

    private SingleSelectionByGroupPanel vsp;
    private DatabaseName dbName = null;
    private DataTableName table = null;
    boolean canRun=false;
    private String chartTitle = "";
    private String chartSubtitle = "";
    private BarChartCommand command = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    // Variables declaration - do not modify
    private JRadioButton frequencyRadioButton;
    private ButtonGroup groupingButtonGroup;
    private JPanel groupingPanel;
    private JRadioButton layeredRadioButton;
    private JRadioButton percentageRadioButton;
    private JRadioButton sidebysideRadioButton;
    private JRadioButton stackedRadioButton;
    private JRadioButton view2DRadioButton;
    private JRadioButton view3DRadioButton;
    private ButtonGroup viewButtonGroup;
    private JPanel viewPanel;
    private ButtonGroup yaxisButtonGroup;
    private JPanel yaxisPanel;
    // End of variables declaration

    public BarChartDialog(JFrame parent, DatabaseName dbName, DataTableName table, ArrayList <VariableAttributes> variables){
        super(parent,"Bar Chart",true);
        this.dbName=dbName;
        this.table=table;
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

        vsp = new SingleSelectionByGroupPanel();
        vsp.setVariables(variables);

        JButton b1 = vsp.getButton1();
        b1.setText("Run");
        b1.addActionListener(new RunActionListener());

        JButton b2 = vsp.getButton2();
        b2.setText("Cancel");
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun = false;
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
                ChartTitlesDialog chartTitlesDialog = new ChartTitlesDialog(BarChartDialog.this, chartTitle, chartSubtitle);
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
                percentageRadioButton.setSelected(true);
                sidebysideRadioButton.setSelected(true);
                view2DRadioButton.setSelected(true);
                chartTitle = "";
                chartSubtitle = "";
            }
        });

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        initComponents();
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);

    }

    private void initComponents() {
        groupingButtonGroup = new ButtonGroup();
        yaxisButtonGroup = new ButtonGroup();
        viewButtonGroup = new ButtonGroup();

        yaxisPanel = new JPanel();
        frequencyRadioButton = new JRadioButton();
        percentageRadioButton = new JRadioButton();
        groupingPanel = new JPanel();
        sidebysideRadioButton = new JRadioButton();
        layeredRadioButton = new JRadioButton();
        stackedRadioButton = new JRadioButton();
        viewPanel = new JPanel();
        view2DRadioButton = new JRadioButton();
        view3DRadioButton = new JRadioButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        vsp.setMaximumSize(new Dimension(400, 272));
        vsp.setMinimumSize(new Dimension(400, 272));
        vsp.setPreferredSize(new Dimension(400, 272));

        yaxisPanel.setBorder(BorderFactory.createTitledBorder("Y-axis"));

        yaxisButtonGroup.add(frequencyRadioButton);
        frequencyRadioButton.setText("Frequency");
        frequencyRadioButton.setActionCommand("freq");

        yaxisButtonGroup.add(percentageRadioButton);
        percentageRadioButton.setSelected(true);
        percentageRadioButton.setText("Percentage");
        percentageRadioButton.setActionCommand("percentage");

        GroupLayout yaxisPanelLayout = new GroupLayout(yaxisPanel);
        yaxisPanel.setLayout(yaxisPanelLayout);
        yaxisPanelLayout.setHorizontalGroup(
            yaxisPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(yaxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(yaxisPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(frequencyRadioButton)
                    .addComponent(percentageRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        yaxisPanelLayout.setVerticalGroup(
            yaxisPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(yaxisPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(frequencyRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(percentageRadioButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        groupingPanel.setBorder(BorderFactory.createTitledBorder("Grouping"));

        groupingButtonGroup.add(sidebysideRadioButton);
        sidebysideRadioButton.setSelected(true);
        sidebysideRadioButton.setText("Side-by-side");
        sidebysideRadioButton.setActionCommand("sidebyside");

        groupingButtonGroup.add(layeredRadioButton);
        layeredRadioButton.setText("Layered");
        layeredRadioButton.setActionCommand("layered");

        groupingButtonGroup.add(stackedRadioButton);
        stackedRadioButton.setText("Stacked");
        stackedRadioButton.setActionCommand("stacked");

        GroupLayout groupingPanelLayout = new GroupLayout(groupingPanel);
        groupingPanel.setLayout(groupingPanelLayout);
        groupingPanelLayout.setHorizontalGroup(
            groupingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(groupingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(groupingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(sidebysideRadioButton)
                    .addComponent(layeredRadioButton)
                    .addComponent(stackedRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        groupingPanelLayout.setVerticalGroup(
            groupingPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(groupingPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sidebysideRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(layeredRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(stackedRadioButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        viewPanel.setBorder(BorderFactory.createTitledBorder("View"));

        viewButtonGroup.add(view2DRadioButton);
        view2DRadioButton.setSelected(true);
        view2DRadioButton.setText(" 2D Effect");
        view2DRadioButton.setActionCommand("2D");

        viewButtonGroup.add(view3DRadioButton);
        view3DRadioButton.setText("3D Effect");
        view3DRadioButton.setActionCommand("3D");

        GroupLayout viewPanelLayout = new GroupLayout(viewPanel);
        viewPanel.setLayout(viewPanelLayout);
        viewPanelLayout.setHorizontalGroup(
            viewPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(viewPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(view2DRadioButton)
                    .addComponent(view3DRadioButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        viewPanelLayout.setVerticalGroup(
            viewPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(viewPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(view2DRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(view3DRadioButton)
                .addContainerGap(33, Short.MAX_VALUE))
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
                        .addComponent(groupingPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(yaxisPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(viewPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(groupingPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(yaxisPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(viewPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
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

    public BarChartCommand getCommand(){
        return command;
    }

    public class RunActionListener implements ActionListener{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Run analysis";
        private boolean ready = false;

        public void actionPerformed(ActionEvent e){

            if(vsp.selectionMade()){
                ready=true;
            }

            if(ready){
                try{
                    command = new BarChartCommand();
                    VariableAttributes v = vsp.getSelectedVariable();
                    command.getFreeOption("variable").add(v.getName().toString());

                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", table.toString());

                    command.getSelectOneOption("yaxis").setSelected(yaxisButtonGroup.getSelection().getActionCommand());
                    command.getSelectOneOption("view").setSelected(viewButtonGroup.getSelection().getActionCommand());
                    command.getSelectOneOption("layout").setSelected(groupingButtonGroup.getSelection().getActionCommand());

                    if("".equals(chartTitle)) chartTitle = v.getName().toString();
                    command.getFreeOption("title").add(chartTitle);
                    chartTitle = "";//reset to avoid keeping old name when dialog reopened

                    if(!"".equals(chartSubtitle)) command.getFreeOption("subtitle").add(chartSubtitle);
                    chartSubtitle = ""; //reset to avoid keeping old name when dialog reopened

                    if(vsp.hasGroupingVariable()){
                        VariableAttributes groupVar = vsp.getGroupByVariable();
                        command.getFreeOption("groupvar").add(groupVar.getName().toString());
                    }

                    canRun=true;
                    setVisible(false);

                }catch(IllegalArgumentException ex){
                    logger.fatal(ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(BarChartDialog.this,
                            ex.getMessage(),
                            "Syntax Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }

    }//end RunAction


}

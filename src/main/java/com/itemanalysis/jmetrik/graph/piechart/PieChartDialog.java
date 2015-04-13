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

package com.itemanalysis.jmetrik.graph.piechart;


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

public class PieChartDialog extends JDialog{

    // Variables declaration - do not modify
    private JLabel amountLabel;
    private JSlider amountSlider;
    private JPanel explodePanel;
    private JLabel sectionLabel;
    private JTextField sectionTextField;
    private JRadioButton view2DRadioButton;
    private JRadioButton view3DRadioButton;
    private ButtonGroup viewButtonGroup;
    private JPanel viewPanel;
    // End of variables declaration

    private PieChartCommand command = null;
    private DatabaseName dbName = null;
    private DataTableName table = null;
    private String chartTitle = "";
    private String chartSubtitle = "";
    private SingleSelectionByGroupPanel vsp;
    private boolean canRun=false;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public PieChartDialog(JFrame parent, DatabaseName dbName, DataTableName table, ArrayList <VariableAttributes> variables){
        super(parent,"Pie Chart",true);
        this.dbName=dbName;
        this.table=table;

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
                ChartTitlesDialog chartTitlesDialog = new ChartTitlesDialog(PieChartDialog.this, chartTitle, chartSubtitle);
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
                sectionTextField.setText("");
                amountSlider.setValue(30);
                chartTitle = "";
                chartSubtitle = "";
                view2DRadioButton.setSelected(true);
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

        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

    }

    private void initComponents() {

        viewButtonGroup = new ButtonGroup();
        viewPanel = new JPanel();
        view2DRadioButton = new JRadioButton();
        view3DRadioButton = new JRadioButton();
        explodePanel = new JPanel();
        sectionLabel = new JLabel();
        sectionTextField = new JTextField();
        amountLabel = new JLabel();
        amountSlider = new JSlider();

        vsp.setMinimumSize(new Dimension(400, 272));
        vsp.setMaximumSize(new Dimension(400,272));
        vsp.setPreferredSize(new Dimension(400, 272));

        viewPanel.setBorder(BorderFactory.createTitledBorder("View"));

        viewButtonGroup.add(view2DRadioButton);
        view2DRadioButton.setSelected(true);
        view2DRadioButton.setText("2D Effect");
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
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        explodePanel.setBorder(BorderFactory.createTitledBorder("Explode Section"));

        sectionLabel.setText("Section value");

        sectionTextField.setMaximumSize(new Dimension(150, 28));
        sectionTextField.setMinimumSize(new Dimension(150, 28));
        sectionTextField.setPreferredSize(new Dimension(150, 28));

        amountLabel.setText("Amount");

        amountSlider.setValue(30);
        amountSlider.setMaximumSize(new Dimension(150, 23));
        amountSlider.setMinimumSize(new Dimension(150, 23));
        amountSlider.setPreferredSize(new Dimension(150, 23));

        GroupLayout jPanel1Layout = new GroupLayout(explodePanel);
        explodePanel.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(amountLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(amountSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(sectionLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sectionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(sectionLabel)
                    .addComponent(sectionTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(amountLabel)
                    .addComponent(amountSlider, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
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
                        .addComponent(viewPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(explodePanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(explodePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(viewPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pack();
    }// </editor-fold>

    public boolean canRun(){
        return canRun;
    }

    public PieChartCommand getCommand(){
        return command;
    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public class RunActionListener implements ActionListener{

        final static String TOOL_TIP = "Run analysis";

        public void actionPerformed(ActionEvent e){
            try{
                if(vsp.selectionMade()){
                    command = new PieChartCommand();
                    VariableAttributes v = vsp.getSelectedVariable();
                    command.getFreeOption("variable").add(v.getName().toString());
                    command.getPairedOptionList("data").addValue("db", dbName.toString());
                    command.getPairedOptionList("data").addValue("table", table.toString());

                    command.getSelectOneOption("view").setSelected(viewButtonGroup.getSelection().getActionCommand());

                    if("".equals(chartTitle)) chartTitle = v.getName().toString();
                    command.getFreeOption("title").add(chartTitle);
                    chartTitle = "";

                    if(!"".equals(chartSubtitle)) command.getFreeOption("subtitle").add(chartSubtitle);
                    chartSubtitle = "";

                    if(vsp.hasGroupingVariable()){
                        VariableAttributes groupVar = vsp.getGroupByVariable();
                        command.getFreeOption("groupvar").add(groupVar.getName().toString());
                    }

                    String explodeSection = sectionTextField.getText().trim();
                    int explodeAmount = amountSlider.getValue();
                    if(!"".equals(explodeSection) && explodeAmount>0){
                        command.getPairedOptionList("explode").addValue("section", explodeSection);
                        command.getPairedOptionList("explode").addValue("amount", explodeAmount);
                    }

                }
            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(PieChartDialog.this,
                        ex.getMessage(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            canRun=true;
            setVisible(false);
        }

    }//end RunAction

    public class CancelAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Cancel";

        public CancelAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public CancelAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public CancelAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
//			manage analysis here
            canRun=false;
            setVisible(false);
        }

    }//end CancelAction

}

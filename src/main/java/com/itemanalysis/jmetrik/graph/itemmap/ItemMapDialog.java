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

package com.itemanalysis.jmetrik.graph.itemmap;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import javax.swing.*;

import com.itemanalysis.jmetrik.gui.SelectTableDialog;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.selector.SingleSelectionPanel;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.swing.ChartTitlesDialog;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import org.apache.log4j.Logger;

public class ItemMapDialog extends JDialog{

    private ArrayList <VariableInfo> variables;
    private SingleSelectionPanel vsp;
    private String chartTitle = "";
    private String chartSubtitle = "";
    private boolean canRun =false;
    private ItemMapCommand command = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private DataTableName itemTable = null;
    private boolean itemDataSelected = false;
    private SelectTableDialog itemParameterDialog = null;
    private JList tableList = null;
    private SortedListModel listModel = null;

    // Variables declaration - do not modify
    private JPanel itemParameterPanel;
    private JButton selectTableButton;
    private JLabel tableLabel;
    private JTextField tableTextField;
    // End of variables declaration

    public ItemMapDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList<VariableInfo> variables, SortedListModel<DataTableName> listModel){
        super(parent, "Item Map", true);
        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        this.dbName = dbName;
        this.tableName = tableName;
        this.listModel = listModel;

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        tableList = new JList(listModel);

        vsp=new SingleSelectionPanel();

        //filter out binary, polytomous, continuous items and strings
        VariableType filterType1 = new VariableType(VariableType.BINARY_ITEM, VariableType.STRING);
        VariableType filterType2 = new VariableType(VariableType.BINARY_ITEM, VariableType.DOUBLE);
        VariableType filterType3 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.STRING);
        VariableType filterType4 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.DOUBLE);
        VariableType filterType5 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.STRING);
        VariableType filterType6 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.DOUBLE);
        VariableType filterType7 = new VariableType(VariableType.NOT_ITEM, VariableType.STRING);
        vsp.addUnselectedFilterType(filterType1);
        vsp.addUnselectedFilterType(filterType2);
        vsp.addUnselectedFilterType(filterType3);
        vsp.addUnselectedFilterType(filterType4);
        vsp.addUnselectedFilterType(filterType5);
        vsp.addUnselectedFilterType(filterType6);
        vsp.addUnselectedFilterType(filterType7);
        vsp.addSelectedFilterType(filterType1);
        vsp.addSelectedFilterType(filterType2);
        vsp.addSelectedFilterType(filterType3);
        vsp.addSelectedFilterType(filterType4);
        vsp.addSelectedFilterType(filterType5);
        vsp.addSelectedFilterType(filterType6);
        vsp.addSelectedFilterType(filterType7);
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
                ChartTitlesDialog chartTitlesDialog = new ChartTitlesDialog(ItemMapDialog.this, chartTitle, chartSubtitle);
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

        setResizable(false);
        setLocationRelativeTo(parent);

    }

    private void initComponents() {

        itemParameterPanel = new JPanel();
        tableLabel = new JLabel();
        tableTextField = new JTextField();
        selectTableButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        vsp.setMaximumSize(new Dimension(400, 272));
        vsp.setMinimumSize(new Dimension(400, 272));
        vsp.setPreferredSize(new Dimension(400, 272));

        itemParameterPanel.setBorder(BorderFactory.createTitledBorder("Item Parameter Table"));

        tableLabel.setText("Table");

        tableTextField.setMaximumSize(new Dimension(200, 28));
        tableTextField.setMinimumSize(new Dimension(200, 28));
        tableTextField.setPreferredSize(new Dimension(200, 28));

        selectTableButton.setText("Select");
        selectTableButton.setMaximumSize(new Dimension(72, 28));
        selectTableButton.setMinimumSize(new Dimension(72, 28));
        selectTableButton.setPreferredSize(new Dimension(72, 28));
        selectTableButton.addActionListener(new SelectItemsActionListener());

        GroupLayout itemParameterPanelLayout = new GroupLayout(itemParameterPanel);
        itemParameterPanel.setLayout(itemParameterPanelLayout);
        itemParameterPanelLayout.setHorizontalGroup(
            itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemParameterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tableLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tableTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(selectTableButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        itemParameterPanelLayout.setVerticalGroup(
            itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(itemParameterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(itemParameterPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(tableLabel)
                    .addComponent(tableTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(selectTableButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
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
                    .addComponent(itemParameterPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(vsp, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(itemParameterPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

//    public ItemMapDialog(JFrame parent, DatabaseName dbName, DataTableName tableName, ArrayList<VariableInfo> variables, SortedListModel<DataTableName> listModel){
//        super(parent, "Item Map", true);
//        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
//        this.dbName = dbName;
//        this.tableName = tableName;
//        this.listModel = listModel;
//
//        //prevent running an analysis when window close button is clicked
//        this.addWindowListener(new WindowAdapter(){
//            @Override
//            public void windowClosing(WindowEvent e){
//                canRun = false;
//            }
//        });
//
//        tableList = new JList(listModel);
//
//        mainPanel=new JPanel();
////        mainPanel.setPreferredSize(new Dimension(335,460));
//        mainPanel.setLayout(new GridBagLayout());
//
////        selectionPanel= new JPanel();
////        selectionPanel.setLayout(new GridBagLayout());
//
//        vsp=new SingleSelectionPanel();
//
//        //filter out binary, polytomous, continuous items and strings
//        VariableType filterType1 = new VariableType(VariableType.BINARY_ITEM, VariableType.STRING);
//        VariableType filterType2 = new VariableType(VariableType.BINARY_ITEM, VariableType.DOUBLE);
//        VariableType filterType3 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.STRING);
//        VariableType filterType4 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.DOUBLE);
//        VariableType filterType5 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.STRING);
//        VariableType filterType6 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.DOUBLE);
//        VariableType filterType7 = new VariableType(VariableType.NOT_ITEM, VariableType.STRING);
//        vsp.addUnselectedFilterType(filterType1);
//        vsp.addUnselectedFilterType(filterType2);
//        vsp.addUnselectedFilterType(filterType3);
//        vsp.addUnselectedFilterType(filterType4);
//        vsp.addUnselectedFilterType(filterType5);
//        vsp.addUnselectedFilterType(filterType6);
//        vsp.addUnselectedFilterType(filterType7);
//        vsp.addSelectedFilterType(filterType1);
//        vsp.addSelectedFilterType(filterType2);
//        vsp.addSelectedFilterType(filterType3);
//        vsp.addSelectedFilterType(filterType4);
//        vsp.addSelectedFilterType(filterType5);
//        vsp.addSelectedFilterType(filterType6);
//        vsp.addSelectedFilterType(filterType7);
//        vsp.setVariables(variables);
//
//        vsp.showButton4(false);
//
//        JButton b1 = vsp.getButton1();
//        b1.setText("Run");
//        b1.addActionListener(new RunActionListener());
//
//        JButton b2 = vsp.getButton2();
//        b2.setText("Cancel");
//        b2.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//            canRun=false;
//            setVisible(false);
//            }
//        });
//
//        JButton b3 = vsp.getButton3();
//        b3.setText("Reset");
//        b3.addActionListener(new ActionListener() {
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                vsp.reset();
//            }
//        });
//
//        GridBagConstraints c = new GridBagConstraints();
//
////        c.gridx = 0;
////        c.gridy = 0;
////        c.gridwidth = 5;
////        c.gridheight = 1;
////        c.weightx = 2;
////        c.weighty = 1;
////        c.anchor = GridBagConstraints.NORTHWEST;
////        c.fill = GridBagConstraints.BOTH;
////        selectionPanel.addArgument(vsp,c);
//
//        //add components to main panel
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridwidth = 5;
//        c.gridheight = 5;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.fill = GridBagConstraints.BOTH;
//        mainPanel.addArgument(vsp,c);
//        c.gridx = 0;
//        c.gridy = 5;
//        c.gridwidth = 5;
//        c.gridheight = 2;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        mainPanel.addArgument(getTitlePanel(),c);
//
//        c.gridx = 0;
//        c.gridy = 7;
//        c.gridwidth = 5;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        mainPanel.addArgument(getParameterPanel(),c);
//
//        c.gridx = 0;
//        c.gridy = 8;
//        c.gridwidth = 2;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        mainPanel.addArgument(getOptionsPanel(),c);
//
//        c.gridx = 2;
//        c.gridy = 8;
//        c.gridwidth = 3;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.NORTHWEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        mainPanel.addArgument(getSizePanel(),c);
//
//        getContentPane().addArgument(mainPanel, BorderLayout.CENTER);
//        pack();
//        setResizable(false);
//        setLocationRelativeTo(parent);
//    }
//
//    private JPanel getTitlePanel(){
//        JPanel panel = new JPanel();
//        panel.setBorder(new EmptyBorder(5,5,5,5));
//        GridBagConstraints c = new GridBagConstraints();
//        JLabel titleLabel = new JLabel("Title ");
//        titleLabel.setPreferredSize(new Dimension(10,35));
//        titleField = new JTextField(30);
//        titleField.setText("");
//        JLabel subtitleLabel = new JLabel("Subtitle ");
//        subtitleLabel.setPreferredSize(new Dimension(10,35));
//        subtitleField = new JTextField(30);
//        subtitleField.setText("");
//        panel.setLayout(new GridBagLayout());
//        panel.setPreferredSize(new Dimension(300,70));
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.EAST;
//        c.fill = GridBagConstraints.NONE;
//        panel.addArgument(titleLabel,c);
//        c.gridx = 1;
//        c.gridy = 0;
//        c.gridwidth = 9;
//        c.gridheight = 1;
//        c.weightx = 9;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        panel.addArgument(titleField,c);
//        c.gridx = 0;
//        c.gridy = 1;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.EAST;
//        c.fill = GridBagConstraints.NONE;
//        panel.addArgument(subtitleLabel,c);
//        c.gridx = 1;
//        c.gridy = 1;
//        c.gridwidth = 9;
//        c.gridheight = 1;
//        c.weightx = 9;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        panel.addArgument(subtitleField,c);
//        return panel;
//    }
//
//    private JPanel getParameterPanel(){
//        JPanel panel = new JPanel();
//        panel.setBorder(new TitledBorder("Item Parameter Table"));
//        panel.setLayout(new GridBagLayout());
//        panel.setPreferredSize(new Dimension(325, 70));
//        GridBagConstraints c = new GridBagConstraints();
//        parametersField = new JTextField(30);
//        parametersField.setEditable(false);
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridwidth = 6;
//        c.gridheight = 1;
//        c.weightx = 6;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        panel.addArgument(parametersField,c);
//
//        JButton paramButton = new JButton("Select");
//        paramButton.addActionListener(new SelectItemsActionListener());
//        c.gridx = 6;
//        c.gridy = 0;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.EAST;
//        c.fill = GridBagConstraints.NONE;
//        panel.addArgument(paramButton,c);
//
//        return panel;
//    }
//
//    private JPanel getOptionsPanel(){
//        JPanel panel = new JPanel();
//        panel.setBorder(new TitledBorder("Orientation"));
//        panel.setLayout(new GridLayout(2,1));
//        panel.setPreferredSize(new Dimension(125,100));
//        orientationGroup = new ButtonGroup();
//        JRadioButton verticalButton = new JRadioButton("Vertical");
//        verticalButton.setActionCommand("vertical");
//        verticalButton.setSelected(true);
//        orientationGroup.addArgument(verticalButton);
//        panel.addArgument(verticalButton);
//
//        JRadioButton horizontalButton = new JRadioButton("Horizontal");
//        horizontalButton.setActionCommand("horizontal");
//        orientationGroup.addArgument(horizontalButton);
//        panel.addArgument(horizontalButton);
//
//        return panel;
//    }
//
//    private JPanel getSizePanel(){
//        JPanel sizePanel = new JPanel();
//        sizePanel.setPreferredSize(new Dimension(200,100));
//        sizePanel.setBorder(new TitledBorder("Chart Size"));
//        sizePanel.setToolTipText("Size of item map");
//        sizePanel.setLayout(new GridBagLayout());
//        JLabel widthLabel = new JLabel("Width ");
//        widthField = new JTextField(10);
//        widthField.setText("500");
//        JLabel heightLabel = new JLabel("Height ");
//        heightField = new JTextField(10);
//        heightField.setText("450");
//        GridBagConstraints c = new GridBagConstraints();
//        c.gridx = 0;
//        c.gridy = 0;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.EAST;
//        c.fill = GridBagConstraints.NONE;
//        sizePanel.addArgument(widthLabel,c);
//        c.gridx = 1;
//        c.gridy = 0;
//        c.gridwidth = 4;
//        c.gridheight = 1;
//        c.weightx = 4;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        sizePanel.addArgument(widthField,c);
//        c.gridx = 0;
//        c.gridy = 1;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.EAST;
//        c.fill = GridBagConstraints.NONE;
//        sizePanel.addArgument(heightLabel,c);
//        c.gridx = 1;
//        c.gridy = 1;
//        c.gridwidth = 4;
//        c.gridheight = 1;
//        c.weightx = 4;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.WEST;
//        c.fill = GridBagConstraints.HORIZONTAL;
//        sizePanel.addArgument(heightField,c);
//
//        return sizePanel;
//    }

    public VariableChangeListener getVariableChangedListener(){
        return vsp.getVariableChangedListener();
    }

    public boolean canRun(){
        return canRun;
    }

    public ItemMapCommand getCommand(){
        return command;
    }

    public class SelectItemsActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            //display item start value dialog
            if(itemParameterDialog == null){
                itemParameterDialog = new SelectTableDialog(ItemMapDialog.this, dbName, listModel);
            }
            itemParameterDialog.setVisible(true);
            if(itemParameterDialog.canRun()){
                tableTextField.setText(itemParameterDialog.getTableName().toString());
                itemTable = itemParameterDialog.getTableName();
                itemDataSelected = true;
            }
        }

    }//end

    public class RunActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){
            try{
                command = new ItemMapCommand();
                VariableInfo v = vsp.getSelectedVariables();
                command.getFreeOption("variables").add(v.getName().toString());
                command.getPairedOptionList("data").addValue("db", dbName.toString());
                command.getPairedOptionList("data").addValue("table", tableName.toString());
                command.getPairedOptionList("itemdata").addValue("table", itemTable.toString());
                command.getFreeOption("title").add(chartTitle);
                if(!"".equals(chartSubtitle)) command.getFreeOption("subtitle").add(chartSubtitle);

            }catch(IllegalArgumentException ex){
                logger.fatal(ex.getMessage(), ex);
                JOptionPane.showMessageDialog(ItemMapDialog.this,
                        ex.getMessage(),
                        "Syntax Error",
                        JOptionPane.ERROR_MESSAGE);
            }

            if(!itemDataSelected){
                JOptionPane.showMessageDialog(ItemMapDialog.this,
                        "You must select an item parameter table.",
                        "No Item Data Selected",
                        JOptionPane.ERROR_MESSAGE);
                canRun =false;
            }

            if(itemDataSelected && vsp.selectionMade()){
                canRun =true;
                setVisible(false);
            }
        }

    }//end RunAction


}

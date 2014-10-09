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

package com.itemanalysis.jmetrik.selector;

import com.itemanalysis.jmetrik.model.VariableListFilter;
import com.itemanalysis.jmetrik.model.VariableListModel;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.ArrayList;

public class MultipleSelectionXYByGroupPanel extends JPanel implements VariableChangeListener{


    // Variables declaration - do not modify
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JButton selectedGroupButton;
    private JTextField groupByTextField;
    private JLabel groupbyLabel;
    private JButton independentVariableButton;
    private JLabel independentVariableLabel;
    private JTextField independentVariableTextField;
    private JButton selectAllButton;
    private JButton selectButton;
    private JList selectedVariableList;
    private JScrollPane selectedVariableScrollPane;
    private JList unselectedVariableList;
    private JScrollPane variableScrollPane;
    // End of variables declaration

    private VariableListModel unselectedListModel;
    private VariableListModel selectedListModel;
    private VariableListFilter unselectedVariableFilter;
    private VariableListFilter selectedVariableFilter;
    private VariableListFilter groupByVariableFilter;
    private VariableListFilter independentVariableFilter;
    private VariableInfo groupByVariable;
    private VariableInfo independentVariable;

    private boolean selectVariables = true;
    private boolean selectGroupVariable = true;
    private boolean selectIndependentVariable = true;

    public MultipleSelectionXYByGroupPanel(){

        //create list filter and list model
        unselectedVariableFilter = new VariableListFilter();
        unselectedListModel = new VariableListModel(unselectedVariableFilter);

        //create list filter and list model
        selectedVariableFilter = new VariableListFilter();
        selectedListModel = new VariableListModel(selectedVariableFilter);

        //create list filter
        groupByVariableFilter = new VariableListFilter();

        //create list filter
        independentVariableFilter = new VariableListFilter();

        initComponents();

    }

    private void initComponents() {

        selectButton = new JButton();
        selectAllButton = new JButton();
        selectedVariableScrollPane = new JScrollPane();
        selectedVariableList = new JList();
        selectedVariableList.setName("selectedVariableList");
        selectedVariableList.addFocusListener(new ListFocusListener());

        variableScrollPane = new JScrollPane();

        unselectedVariableList = new JList();
        unselectedVariableList.setName("unselectedVariableList");
        unselectedVariableList.addFocusListener(new ListFocusListener());

        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        button4 = new JButton();
        independentVariableLabel = new JLabel();

        independentVariableTextField = new JTextField();
        independentVariableTextField.setName("independentVariableField");
        independentVariableTextField.addFocusListener(new ListFocusListener());

        independentVariableButton = new JButton();
        groupbyLabel = new JLabel();
        groupByTextField = new JTextField();
        groupByTextField.setName("groupVariableField");
        groupByTextField.addFocusListener(new ListFocusListener());

        selectedGroupButton = new JButton();

        setMinimumSize(new Dimension(414, 272));
        setPreferredSize(new Dimension(412, 272));

        selectButton.setText(">");
        selectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(selectVariables){
                    //move selected variable to selectedList
                    int[] selected = unselectedVariableList.getSelectedIndices();
                    for(int i=0;i<selected.length;i++){
                        selectedListModel.addElement(unselectedListModel.getElementAt(selected[i]));
                    }
                    for(int i=0;i<selectedListModel.getSize();i++){
                        unselectedListModel.removeElement(selectedListModel.getElementAt(i));
                    }
                    unselectedVariableList.clearSelection();
                }else{
                    //move selectedVariables to unselected list
                    int[] selected = selectedVariableList.getSelectedIndices();
                    for(int i=0;i<selected.length;i++){
                        unselectedListModel.addElement(selectedListModel.getElementAt(selected[i]));
                    }
                    for(int i=0; i <unselectedListModel.getSize();i++){
                        selectedListModel.removeElement(unselectedListModel.getElementAt(i));
                    }
                    selectedVariableList.clearSelection();
                }
            }
        });
        selectButton.setMaximumSize(new Dimension(49, 28));
        selectButton.setMinimumSize(new Dimension(49, 28));
        selectButton.setPreferredSize(new Dimension(49, 28));

        selectAllButton.setText(">>");
        selectAllButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(selectVariables){
                    //move all variables to selectedList
                    selectedListModel.addAll(unselectedListModel.getAll());
                    unselectedListModel.removeAll(selectedListModel.getAll());
                }else{
                    //move all variables to unselected list
                    unselectedListModel.addAll(selectedListModel.getAll());
                    selectedListModel.removeAll(unselectedListModel.getAll());
                }
            }
        });
        selectAllButton.setMaximumSize(new Dimension(49, 28));
        selectAllButton.setMinimumSize(new Dimension(49, 28));
        selectAllButton.setPreferredSize(new Dimension(49, 28));

        selectedVariableScrollPane.setMinimumSize(new Dimension(125, 260));
        selectedVariableScrollPane.setPreferredSize(new Dimension(125, 250));

        selectedVariableList.setModel(selectedListModel);
        selectedVariableScrollPane.setViewportView(selectedVariableList);

        variableScrollPane.setMinimumSize(new Dimension(125, 250));
        variableScrollPane.setPreferredSize(new Dimension(125, 250));

        unselectedVariableList.setModel(unselectedListModel);
        variableScrollPane.setViewportView(unselectedVariableList);

        button1.setText("B1");
        button1.setMaximumSize(new Dimension(72, 28));
        button1.setMinimumSize(new Dimension(72, 28));
        button1.setPreferredSize(new Dimension(72, 28));

        button2.setText("B2");
        button2.setMaximumSize(new Dimension(72, 28));
        button2.setMinimumSize(new Dimension(72, 28));
        button2.setPreferredSize(new Dimension(72, 28));

        button3.setText("B3");
        button3.setMaximumSize(new Dimension(72, 28));
        button3.setMinimumSize(new Dimension(72, 28));
        button3.setPreferredSize(new Dimension(72, 28));

        button4.setText("B4");
        button4.setMaximumSize(new Dimension(72, 28));
        button4.setMinimumSize(new Dimension(72, 28));
        button4.setPreferredSize(new Dimension(72, 28));

        independentVariableLabel.setText("Independent Variable");

        independentVariableTextField.setMaximumSize(new Dimension(125, 28));
        independentVariableTextField.setMinimumSize(new Dimension(125, 28));
        independentVariableTextField.setPreferredSize(new Dimension(128, 28));

        independentVariableButton.setText(">");
        independentVariableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(selectIndependentVariable){
                    //Check to see if variable passes filter. If so, then move it to selected list
                    int[] selected = unselectedVariableList.getSelectedIndices();
                    VariableInfo temp = unselectedListModel.getElementAt(selected[0]);//only use first selection
                    if(independentVariableFilter.passThroughFilter(temp)){

                        independentVariable = temp;
                        unselectedListModel.removeElement(independentVariable);
                        unselectedVariableList.clearSelection();
                        independentVariableTextField.setText(independentVariable.getName().toString());
                    }else{
                        temp = null;
                        independentVariable = null;
                    }

                }else{
                    //move selectedVariables to unselected list
                    if(independentVariable!=null){
                        unselectedListModel.addElement(independentVariable);
                        independentVariable = null;
                        independentVariableTextField.setText("");
                    }
                }
            }
        });
        independentVariableButton.setMaximumSize(new Dimension(49, 28));
        independentVariableButton.setMinimumSize(new Dimension(49, 28));
        independentVariableButton.setPreferredSize(new Dimension(49, 28));

        groupbyLabel.setText("Group by");

        groupByTextField.setMaximumSize(new Dimension(125, 28));
        groupByTextField.setMinimumSize(new Dimension(125, 28));
        groupByTextField.setPreferredSize(new Dimension(128, 28));

        selectedGroupButton.setText(">");
        selectedGroupButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(selectGroupVariable){
                    //Check to see if variable passes filter. If so, then move it to selected list
                    int[] selected = unselectedVariableList.getSelectedIndices();
                    VariableInfo temp = unselectedListModel.getElementAt(selected[0]);//only use first selection
                    if(groupByVariableFilter.passThroughFilter(temp)){

                        groupByVariable = temp;
                        unselectedListModel.removeElement(groupByVariable);
                        unselectedVariableList.clearSelection();
                        groupByTextField.setText(groupByVariable.getName().toString());
                    }else{
                        temp = null;
                        groupByVariable = null;
                    }

                }else{
                    //move selectedVariables to unselected list
                    if(groupByVariable!=null){
                        unselectedListModel.addElement(groupByVariable);
                        groupByVariable = null;
                        groupByTextField.setText("");
                    }
                }
            }
        });
        selectedGroupButton.setMaximumSize(new Dimension(49, 28));
        selectedGroupButton.setMinimumSize(new Dimension(49, 28));
        selectedGroupButton.setPreferredSize(new Dimension(49, 28));

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(variableScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(independentVariableButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addComponent(selectButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(selectAllButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(selectedGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(independentVariableLabel)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(independentVariableTextField, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                                        .addComponent(selectedVariableScrollPane, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                                        .addComponent(button4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(button1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(button3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(button2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                        .addComponent(groupbyLabel)
                                        .addComponent(groupByTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(13, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(variableScrollPane, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                                        .addComponent(selectedVariableScrollPane, GroupLayout.Alignment.LEADING, 0, 0, Short.MAX_VALUE)
                                                                        .addGroup(GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                                                .addGap(2, 2, 2)
                                                                                .addComponent(button1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(2, 2, 2)
                                                                                .addComponent(button2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(2, 2, 2)
                                                                                .addComponent(button3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                                .addGap(2, 2, 2)
                                                                                .addComponent(button4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(independentVariableLabel))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(7, 7, 7)
                                                                .addComponent(selectButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(selectAllButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(independentVariableButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(independentVariableTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(groupbyLabel)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                                        .addComponent(groupByTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(selectedGroupButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                                .addContainerGap())
        );
    }// </editor-fold>

    public void setVariables(ArrayList<VariableInfo> variables){
        reset();
        unselectedListModel.clear();

        for(VariableInfo v : variables){
            unselectedListModel.addElement(v);
        }

    }

    public void reset(){
        unselectedListModel.addAll(selectedListModel.getAll());
        selectedListModel.clear();

        if(groupByVariable!=null){
            unselectedListModel.addElement(groupByVariable);
            groupByVariable = null;
        }
        groupByTextField.setText("");
        selectedGroupButton.setEnabled(true);

        if(independentVariable!=null){
            unselectedListModel.addElement(independentVariable);
            independentVariable = null;
        }
        independentVariableTextField.setText("");
        independentVariableButton.setEnabled(true);

    }

    public void addUnselectedFilterType(VariableType t){
        unselectedVariableFilter.addFilteredType(t);
    }

    public void addSelectedFilterType(VariableType t){
        selectedVariableFilter.addFilteredType(t);
    }

    public void addGroupByFilterType(VariableType t){
        groupByVariableFilter.addFilteredType(t);
    }

    public void addIndependentVariableFilterType(VariableType t){
        independentVariableFilter.addFilteredType(t);
    }

    public JButton getButton1(){
        return button1;
    }

    public JButton getButton2(){
        return button2;
    }

    public JButton getButton3(){
        return button3;
    }

    public JButton getButton4(){
        return button4;
    }

    public void showButton1(boolean show){
        button1.setVisible(show);
    }

    public void showButton2(boolean show){
        button2.setVisible(show);
    }

    public void showButton3(boolean show){
        button3.setVisible(show);
    }

    public void showButton4(boolean show){
        button4.setVisible(show);
    }

    public boolean selectionMade(){
        return selectedListModel.getSize()>0;
    }

    public boolean hasIndependentVariable(){
        return independentVariable!=null;
    }

    /**
     *
     * @return array of selected variables.
     */
    public VariableInfo[] getSelectedVariables(){
        VariableInfo[] selected = selectedListModel.getAll();
        return selected;
    }

    public VariableInfo getGroupByVariable(){
        return groupByVariable;
    }

    public VariableInfo getIndependentVariable(){
        return independentVariable;
    }

    public boolean hasGroupingVariable(){
        return groupByVariable!=null;
    }

    public VariableChangeListener getVariableChangedListener(){
        return this;
    }

    public void variableChanged(VariableChangeEvent e){

        if(e.getChangeType()== VariableChangeType.VARIABLE_DELETED){
            VariableInfo varInfo = e.getVariable();
            unselectedListModel.removeElement(varInfo);
            selectedListModel.removeElement(varInfo);
            if(groupByVariable!=null && groupByVariable.equals(varInfo)){
                groupByVariable=null;
            }
        }else if(e.getChangeType()==VariableChangeType.VARIABLE_ADDED){
            VariableInfo varInfo = e.getVariable();
            unselectedListModel.addElement(varInfo);
        }else if(e.getChangeType()==VariableChangeType.VARIABLE_MODIFIED){
            VariableInfo varInfo = e.getVariable();
            //do not use selectedListModel.replaceElement(v) because of need to filter variable
            if(selectedListModel.removeElement(varInfo)){
                selectedListModel.addElement(varInfo); //will force filtering of modified variable
            }else if(unselectedListModel.removeElement(varInfo)){
                //do not use variableListModel.replaceElement(v) because of need to filter variable
                unselectedListModel.addElement(varInfo); //will force filtering of modified variable
            }else if(independentVariable.equals(varInfo)){
                if(independentVariableFilter.passThroughFilter(independentVariable)){
                    independentVariable=varInfo;
                }else{
                    unselectedListModel.addElement(varInfo);//force filtering
                }
            }else if(groupByVariable.equals(varInfo)){
                if(groupByVariableFilter.passThroughFilter(groupByVariable)){
                    groupByVariable=varInfo;
                }else{
                    unselectedListModel.addElement(varInfo);//force filtering
                }
            }else{
                if(unselectedListModel.contains(varInfo)){
                    //do not use variableListModel.replaceElement(v) because of need to filter variable
                    unselectedListModel.removeElement(varInfo);
                    unselectedListModel.addElement(varInfo); //will force filtering of modified variable
                }
            }
        }else if(e.getChangeType()==VariableChangeType.VARIABLE_RENAMED){
            VariableInfo oldVariable = e.getOldVariable();
            //do not use selectedListModel.replaceElement(v) because of need to filter variable
            if(selectedListModel.removeElement(oldVariable)){
                selectedListModel.addElement(e.getVariable()); //will force filtering of modified variable
            }else if(unselectedListModel.removeElement(oldVariable)){
                //do not use variableListModel.replaceElement(v) because of need to filter variable
                unselectedListModel.addElement(e.getVariable()); //will force filtering of modified variable
            }else if(independentVariable.equals(oldVariable)){
                if(independentVariableFilter.passThroughFilter(independentVariable)){
                    independentVariable=e.getVariable();
                }else{
                    unselectedListModel.addElement(e.getVariable());//force filtering
                }
            }else if(groupByVariable.equals(oldVariable)){
                if(groupByVariableFilter.passThroughFilter(groupByVariable)){
                    groupByVariable=e.getVariable();
                }else{
                    unselectedListModel.addElement(e.getVariable());//force filtering
                }
            }else{
                if(unselectedListModel.contains(oldVariable)){
                    //do not use variableListModel.replaceElement(v) because of need to filter variable
                    unselectedListModel.removeElement(oldVariable);
                    unselectedListModel.addElement(e.getVariable()); //will force filtering of modified variable
                }
            }
        }
    }

    public void setUnselectedListCellRenderer(DefaultListCellRenderer renderer){
        unselectedVariableList.setCellRenderer(renderer);
    }

    public void setSelectedVariableListCellRenderer(DefaultListCellRenderer renderer){
        selectedVariableList.setCellRenderer(renderer);
    }

    public void repaintLists(){
        unselectedVariableList.repaint();
        selectedVariableList.repaint();
    }

    public void setIndependentVariableLabel(String text){
        independentVariableLabel.setText(text);
    }


    public class ListFocusListener implements FocusListener {

        public void focusGained(FocusEvent e){
            String compName = e.getComponent().getName();
            if(compName!=null){
                if("unselectedVariableList".equals(compName)){
                    if(groupByVariable!=null) selectedGroupButton.setEnabled(false);
                    if(independentVariable!=null) independentVariableButton.setEnabled(false);
                    selectButton.setText(">");
                    selectButton.setToolTipText("Select variable");
                    selectAllButton.setText(">>");
                    selectAllButton.setToolTipText("Select all variables");
                    selectVariables = true;

                    selectedGroupButton.setText(">");
                    selectedGroupButton.setToolTipText("Select group variable");
                    selectGroupVariable = true;

                    independentVariableButton.setText(">");
                    independentVariableButton.setToolTipText("Select group variable");
                    selectIndependentVariable = true;

                }

                if("selectedVariableList".equals(compName)){
                    selectButton.setText("<");
                    selectButton.setToolTipText("Deselect variable");
                    selectAllButton.setText("<<");
                    selectAllButton.setToolTipText("Deselect all variables");
                    selectVariables = false;
                }

                if("groupVariableField".equals(compName)){
                    selectedGroupButton.setEnabled(true);
                    selectedGroupButton.setText("<");
                    selectedGroupButton.setToolTipText("Deselect group variable");
                    groupByTextField.selectAll();
                    selectGroupVariable = false;
                }

                if("independentVariableField".equals(compName)){
                    independentVariableButton.setEnabled(true);
                    independentVariableButton.setText("<");
                    independentVariableButton.setToolTipText("Deselect group variable");
                    independentVariableTextField.selectAll();
                    selectIndependentVariable = false;
                }

            }
        }

        public void focusLost(FocusEvent e){
            //do nothing
        }

    }


}

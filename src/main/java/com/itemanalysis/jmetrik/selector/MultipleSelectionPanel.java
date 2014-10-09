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


/**
 * This components is used in jMetrik dialogs. It allows users to select and
 * deselect variables. It includes four buttons. The users can add ActionListeners to
 * the buttons and user the getButton1(), getButton2(), etc. methods to set the
 * visibility, text, and ActionListeners for each button. The user can add
 * ListCellRenderers to the unselectedVariableList and the selectedVariableList.
 * The user should call getSelectedVariables() to get an array of VariableInfo
 * objects of teh selected variables.
 *
 *
 */
public class MultipleSelectionPanel extends JPanel implements VariableChangeListener{

    // Variables declaration - do not modify
    private JButton button1;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JButton selectAllButton;
    private JButton selectButton;
    private JList selectedVariableList;
    private JScrollPane selectedVariableScrollPane;
    private JList unselectedVariableList;
    private JScrollPane unselectedVariableScrollPane;
    private VariableListModel unselectedListModel;
    private VariableListModel selectedListModel;
    private VariableListFilter unselectedVariableFilter;
    private VariableListFilter selectedVariableFilter;

    /**
     * If true, variables moved from unselectedVariableList to selectedList.
     * If false, variables moved from selectedList to unselectedVariableList.
            */
    private boolean selectVariables = true;

    /**
     * This constructor is used when no filtering is applied to the lists.
     *
     */
    public MultipleSelectionPanel(){

        //create list filter and list model
        unselectedVariableFilter = new VariableListFilter();
        unselectedListModel = new VariableListModel(unselectedVariableFilter);


        //create list filter and list model
        selectedVariableFilter = new VariableListFilter();
        selectedListModel = new VariableListModel(selectedVariableFilter);

        initComponents();

    }

    private void initComponents() {

        selectButton = new JButton();
        selectAllButton = new JButton();
        selectedVariableScrollPane = new JScrollPane();
        selectedVariableList = new JList();
        selectedVariableList.setName("selectedVariableList");
        unselectedVariableScrollPane = new JScrollPane();
        unselectedVariableList = new JList();
        unselectedVariableList.setName("unselectedVariableList");
        button1 = new JButton();
        button2 = new JButton();
        button3 = new JButton();
        button4 = new JButton();

        setMinimumSize(new Dimension(414, 272));

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

//        selectButton.setMaximumSize(new Dimension(49, 25));
//        selectButton.setMinimumSize(new Dimension(49, 25));
//        selectButton.setPreferredSize(new Dimension(49, 25));

//        selectAllButton.setText(">>");
//        selectAllButton.setMaximumSize(new Dimension(49, 25));
//        selectAllButton.setMinimumSize(new Dimension(49, 25));
//        selectAllButton.setPreferredSize(new Dimension(49, 25));

        selectedVariableScrollPane.setMinimumSize(new Dimension(125, 250));
        selectedVariableScrollPane.setPreferredSize(new Dimension(125, 250));

        selectedVariableList.setModel(selectedListModel);
        selectedVariableList.setName("selectedList");
        selectedVariableList.addFocusListener(new ListFocusListener());
        selectedVariableScrollPane.setViewportView(selectedVariableList);

        unselectedVariableScrollPane.setMinimumSize(new Dimension(125, 250));
        unselectedVariableScrollPane.setPreferredSize(new Dimension(125, 250));

        unselectedVariableList.setModel(unselectedListModel);
        unselectedVariableList.setName("unselectedVariableList");
        unselectedVariableList.addFocusListener(new ListFocusListener());
        unselectedVariableScrollPane.setViewportView(unselectedVariableList);

        button1.setText("B1");
//        button1.setMaximumSize(new Dimension(70, 25));
//        button1.setMinimumSize(new Dimension(70, 25));
//        button1.setPreferredSize(new Dimension(70, 25));

        button2.setText("B2");
//        button2.setMaximumSize(new Dimension(70, 25));
//        button2.setMinimumSize(new Dimension(70, 25));
//        button2.setPreferredSize(new Dimension(70, 25));

        button3.setText("B3");
//        button3.setMaximumSize(new Dimension(70, 25));
//        button3.setMinimumSize(new Dimension(70, 25));
//        button3.setPreferredSize(new Dimension(70, 25));

        button4.setText("B4");

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(unselectedVariableScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(selectButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, 49)
                                        .addComponent(selectAllButton, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, 49))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selectedVariableScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                        .addComponent(button4, GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                                        .addComponent(button1, GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                                        .addComponent(button3, GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE)
                                        .addComponent(button2, GroupLayout.DEFAULT_SIZE, 69, Short.MAX_VALUE))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
//                                .addContainerGap(13, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(103, 103, 103)
                                                .addComponent(selectButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                .addGap(2, 2, 2)
                                                .addComponent(selectAllButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(selectedVariableScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(unselectedVariableScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(2, 2, 2)
                                                                .addComponent(button1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(2, 2, 2)
                                                                .addComponent(button2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(2, 2, 2)
                                                                .addComponent(button3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                                                .addGap(2, 2, 2)
                                                                .addComponent(button4)))))
                                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
    }

    public void addUnselectedFilterType(VariableType t){
        unselectedVariableFilter.addFilteredType(t);
    }

    public void addSelectedFilterType(VariableType t){
        selectedVariableFilter.addFilteredType(t);
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

    public boolean hasSelection(){
        return selectedListModel.getSize()>0;
    }

    public JButton getButton4(){
        return button4;
    }

    public boolean selectionMade(){
        return selectedListModel.getSize()>0;
    }

    public VariableListModel getUnselectedListModel(){
        return unselectedListModel;
    }

    public VariableListModel getSelectedListModel(){
        return selectedListModel;
    }

    /**
     *
     * @return array of selected variables.
     */
    public VariableInfo[] getSelectedVariables(){
        VariableInfo[] selected = selectedListModel.getAll();
        return selected;
    }

    public VariableChangeListener getVariableChangedListener(){
        return this;
    }

    public void variableChanged(VariableChangeEvent e){

        if(e.getChangeType()== VariableChangeType.VARIABLE_DELETED){
            VariableInfo varInfo = e.getVariable();
            unselectedListModel.removeElement(varInfo);
            selectedListModel.removeElement(varInfo);
        }else if(e.getChangeType()==VariableChangeType.VARIABLE_ADDED){
            VariableInfo varInfo = e.getVariable();
            unselectedListModel.addElement(varInfo);
        }else if(e.getChangeType()==VariableChangeType.VARIABLE_MODIFIED){
            VariableInfo varInfo = e.getVariable();
            //do not use selectedListModel.replaceElement(v) because of need to filter variable
            if(selectedListModel.removeElement(varInfo)){
                selectedListModel.addElement(varInfo); //will force filtering of modified variable
            }else{
                //do not use variableListModel.replaceElement(v) because of need to filter variable
                unselectedListModel.removeElement(varInfo);
                unselectedListModel.addElement(varInfo); //will force filtering of modified variable
            }
        }else if(e.getChangeType()==VariableChangeType.VARIABLE_RENAMED){
            VariableInfo oldVarInfo = e.getOldVariable();
            if(selectedListModel.removeElement(oldVarInfo)){
                selectedListModel.addElement(e.getVariable());
            }else{
                unselectedListModel.removeElement(oldVarInfo);
                unselectedListModel.addElement(e.getVariable());
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

    public class ListFocusListener implements FocusListener {

        public void focusGained(FocusEvent e){
            String compName = e.getComponent().getName();
            if(compName!=null){
                if("unselectedVariableList".equals(compName)){
                    selectButton.setText(">");
                    selectButton.setToolTipText("Select variable");
                    selectAllButton.setText(">>");
                    selectAllButton.setToolTipText("Select all variables");
                    selectVariables = true;
                }

                if("selectedList".equals(compName)){
                    selectButton.setText("<");
                    selectButton.setToolTipText("Deselect variable");
                    selectAllButton.setText("<<");
                    selectAllButton.setToolTipText("Deselect all variables");
                    selectVariables = false;
                }

            }
        }

        public void focusLost(FocusEvent e){
            //do nothing
        }
    }


}

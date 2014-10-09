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

package com.itemanalysis.jmetrik.graph.nicc;

import com.itemanalysis.jmetrik.selector.SingleSelectionPanel;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class RegressorVariableDialog extends JDialog {

    private boolean canRun = false;

    private VariableInfo selectedVariable = null;

    private SingleSelectionPanel singleSelectionPanel = null;

    public RegressorVariableDialog(JFrame parent, ArrayList<VariableInfo> variables){
        super(parent, "Select Independent Variable", true);

        singleSelectionPanel = new SingleSelectionPanel();

        //filter out strings from unselected list
        VariableType filterType1 = new VariableType(VariableType.BINARY_ITEM, VariableType.STRING);
        VariableType filterType2 = new VariableType(VariableType.POLYTOMOUS_ITEM, VariableType.STRING);
        VariableType filterType3 = new VariableType(VariableType.CONTINUOUS_ITEM, VariableType.STRING);
        VariableType filterType4 = new VariableType(VariableType.NOT_ITEM, VariableType.STRING);
        singleSelectionPanel.addUnselectedFilterType(filterType1);
        singleSelectionPanel.addUnselectedFilterType(filterType2);
        singleSelectionPanel.addUnselectedFilterType(filterType3);
        singleSelectionPanel.addUnselectedFilterType(filterType4);
        singleSelectionPanel.addSelectedFilterType(filterType1);
        singleSelectionPanel.addSelectedFilterType(filterType2);
        singleSelectionPanel.addSelectedFilterType(filterType3);
        singleSelectionPanel.addSelectedFilterType(filterType4);

        singleSelectionPanel.setVariables(variables);
        singleSelectionPanel.showButton4(false);
        singleSelectionPanel.showButton3(false);

        JButton b1 = singleSelectionPanel.getButton1();
        b1.setText("OK");
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(singleSelectionPanel.selectionMade()){
                    selectedVariable = singleSelectionPanel.getSelectedVariables();
                    canRun = true;
                    setVisible(false);
                }else{
                    JOptionPane.showMessageDialog(RegressorVariableDialog.this,
                            "You must select a matching variable",
                            "No Matching Variable Selected",
                            JOptionPane.ERROR_MESSAGE);
                    canRun = false;
                }
            }
        });

        JButton b2 = singleSelectionPanel.getButton2();
        b2.setText("Cancel");
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        getContentPane().add(singleSelectionPanel);

        pack();

        setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
        setLocationRelativeTo(parent);
        setResizable(false);



    }

    public VariableInfo getIndependentVariable(){
        return selectedVariable;
    }

    public boolean canRun(){
        return canRun;
    }

}

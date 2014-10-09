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

import com.itemanalysis.jmetrik.gui.Jmetrik;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class TestDialog extends JDialog {


    public TestDialog(Jmetrik parent){

        ArrayList<VariableInfo> variables = new ArrayList<VariableInfo>();

        MultipleSelectionByGroupPanel panel = new MultipleSelectionByGroupPanel();
        VariableType filterType = new VariableType(VariableType.NO_FILTER, VariableType.NO_FILTER);
        panel.addUnselectedFilterType(filterType);
        panel.addSelectedFilterType(filterType);
        panel.addGroupByFilterType(filterType);
        panel.setVariables(variables);

        getContentPane().add(panel, BorderLayout.CENTER);
        pack();
        setResizable(false);
        setLocationRelativeTo(parent);

    }


}

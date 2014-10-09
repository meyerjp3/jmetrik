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

package com.itemanalysis.jmetrik.swing;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.util.Formatter;

public class NumericCellEditor extends AbstractCellEditor implements TableCellEditor{

    private JComponent component = new JTextField();
    
    private int precision = 4;
    
    private Formatter f = new Formatter();

    public NumericCellEditor(int precision){
        this.precision = precision;
    }


    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int rowIndex, int vColIndex) {

        if(isSelected){

        };

        Double d = (Double)value;
        if(value==null){
            ((JTextField)component).setText("");
        }else{
            ((JTextField)component).setText(String.format(" %."+precision+"f", d));
        }


        return component;

    }
    
    public Object getCellEditorValue(){
        return ((JTextField)component).getText();
    }

    public boolean stopCellEditing(){
        String s = (String)getCellEditorValue();

//        if(!isValid(s)){
//
//            return false;
//        }
        return super.stopCellEditing();
    }




}

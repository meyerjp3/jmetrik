/*
 * Copyright (c) 2013 Patrick Meyer
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

package com.itemanalysis.jmetrik.scoring;

import com.itemanalysis.psychometrics.data.VariableAttributes;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class BasicScoringTableModel  extends AbstractTableModel {

    private ArrayList<VariableAttributes> variables = null;
    private Object[][] data = null;
    int ncol = 0;

    public BasicScoringTableModel(ArrayList<VariableAttributes> variables){
        this.variables = variables;
        ncol = variables.size();
        data = new Object[2][variables.size()];
    }

    @Override
    public Object getValueAt(int row, int col){
        return data[row][col];
    }

    @Override
    public void setValueAt(Object value, int row, int col){
        data[row][col] = value;
    }

    @Override
    public int getColumnCount(){
        return ncol;
    }

    @Override
    public int getRowCount(){
        return 2;
    }

    @Override
    public boolean isCellEditable(int r, int c){
        return true;
    }

    @Override
    public String getColumnName(int col){
        if(col<variables.size())return variables.get(col).getName().toString();
        return "Var" + col;
    }

    public String getKeyString(){
        String key = "";
        for(int i=0;i<ncol;i++){
            if(data[0][i]==null || "".equals(data[0][i].toString().trim()) ||
                    data[1][i]==null  || "".equals(data[1][i].toString().trim())){
                key += " ";
            }else{
                key += data[0][i];
            }
            key += ",";
        }
        key = key.substring(0, key.lastIndexOf(","));
        return key;
    }

    public String getNumberOfOptionsString(){
        String options = "";
        for(int i=0;i<ncol;i++){
            if(data[0][i]==null || "".equals(data[0][i].toString().trim()) ||
                    data[1][i]==null  || "".equals(data[1][i].toString().trim())){
                options += "0";
            }else{
                options += data[1][i];
            }
            options += ",";
        }
        options = options.substring(0, options.lastIndexOf(","));
        return options;
    }

    public void clearAll(){
        for(int i=0;i<2;i++){
            for(int j=0;j<ncol;j++){
                data[i][j] = null;
            }
        }
    }


}

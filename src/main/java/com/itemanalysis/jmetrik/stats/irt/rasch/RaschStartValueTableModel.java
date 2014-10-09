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

package com.itemanalysis.jmetrik.stats.irt.rasch;

import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableName;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;

public class RaschStartValueTableModel extends AbstractTableModel {

    private String[] columnNames = {"Variable", "Fixed Value"};

    private ArrayList<VariableInfo> variables = null;

    private Boolean[] fixedParams = null;

    public RaschStartValueTableModel(ArrayList<VariableInfo> variables){
        this.variables = variables;
        fixedParams = new Boolean[variables.size()];
        initializeData();
    }

    public final void initializeData(){
        for(int i=0;i<fixedParams.length;i++){
            fixedParams[i]=Boolean.FALSE;
        }
    }

    public Object getValueAt(int r, int c){
        if(c==0){
            return variables.get(r).toString();
        }else{
            return fixedParams[r];
        }
    }

    public Boolean hasStartValueNames(){
        for(int i=0;i<fixedParams.length;i++){
            if(((Boolean)fixedParams[i])){
                return true;
            }
        }
        return false;
    }

    public ArrayList<VariableName> getFixedValueNames(){
        ArrayList<VariableName> start = new ArrayList<VariableName>();
        int i = 0;
        for(VariableInfo v : variables){
            if(fixedParams[i]){
                start.add(v.getName());
            }
            i++;
        }
        return start;
    }

    public boolean hasFixedValueNames(){
        for(int i=0;i<fixedParams.length;i++){
            if(fixedParams[i]) return true;
        }
        return false;
    }

    @Override
	public void setValueAt(Object value, int r, int c){
        if(c>0){
            fixedParams[r]=Boolean.valueOf(value.toString());
            fireTableCellUpdated(r, c);
        }
    }

    public int getColumnCount(){
		return columnNames.length;
	}

    public int getRowCount(){
        return variables.size();
    }

	public String getColumnName(int c){
		return columnNames[c];
	}

    public Class getColumnClass(int c){
        return getValueAt(0, c).getClass();
	}

    public boolean isCellEditable(int r, int c){
        if(c==0) return false;
        return true;
    }

}


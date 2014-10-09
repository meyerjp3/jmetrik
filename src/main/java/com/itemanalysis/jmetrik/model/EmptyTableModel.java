/*
 * Copyright (c) 2011 Patrick Meyer
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

package com.itemanalysis.jmetrik.model;

import javax.swing.table.AbstractTableModel;

public class EmptyTableModel extends AbstractTableModel{

	private static final long serialVersionUID = 1L;

	public EmptyTableModel(){

	}

	public String getColumnName(int c){
		return "Var" + (c+1);
	}

	public int getRowCount(){
		return 100;
	}

	public int getColumnCount(){
		return 50;
	}

	public Object getValueAt(int r, int c){
		return null;
	}

	public boolean isCellEditable(int nRow, int nCol){
		return false;
	}

	@SuppressWarnings("unchecked")
	public Class getColumnClass(int c) {
		return String.class;
	}

}
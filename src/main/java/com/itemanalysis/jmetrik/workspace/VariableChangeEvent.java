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

package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.VariableAttributes;

import java.util.EventObject;

public class VariableChangeEvent extends EventObject {

    private DataTableName tableName = null;

    private VariableAttributes variable = null;

    private VariableAttributes oldVariableInfo = null;

    private VariableChangeType changeType = VariableChangeType.VARIABLE_MODIFIED;

    public VariableChangeEvent(Object source, DataTableName tableName, VariableAttributes variable, VariableChangeType changeType){
        super(source);
        this.tableName = tableName;
        this.variable = variable;
        this.changeType = changeType;
    }

    public VariableChangeEvent(Object source, VariableTableName tableName, VariableAttributes variable, VariableChangeType changeType){
        super(source);
        this.tableName = new DataTableName(tableName.toString());
        this.variable = variable;
        this.changeType = changeType;
    }

    public VariableChangeEvent(Object source, DataTableName tableName, VariableAttributes variable, VariableAttributes oldVariable, VariableChangeType changeType){
        super(source);
        this.tableName = tableName;
        this.variable = variable;
        this.oldVariableInfo = oldVariable;
        this.changeType = changeType;
    }

    public VariableChangeEvent(Object source, VariableTableName tableName, VariableAttributes variable, VariableAttributes oldVariable, VariableChangeType changeType){
        super(source);
        this.tableName = new DataTableName(tableName.toString());
        this.variable = variable;
        this.oldVariableInfo = oldVariableInfo;
        this.changeType = changeType;
    }

    public DataTableName getTableName(){
        return tableName;
    }

    public VariableAttributes getVariable(){
        return variable;
    }

    public VariableAttributes getOldVariable(){
        return oldVariableInfo;
    }

    public VariableChangeType getChangeType(){
        return changeType;
    }

}

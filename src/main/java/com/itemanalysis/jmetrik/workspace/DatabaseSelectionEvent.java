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

package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.VariableInfo;

import java.util.ArrayList;

public class DatabaseSelectionEvent {

    private DatabaseName dbName = null;

    private DataTableName tableName = null;

    private ArrayList<VariableInfo> variables = null;

    public DatabaseSelectionEvent(Object source, DatabaseName dbName, DataTableName tableName, ArrayList<VariableInfo> variables){
        this.dbName = dbName;
        this.tableName = tableName;
        this.variables = variables;
    }

    public DatabaseSelectionEvent(Object source, DatabaseName dbName, VariableTableName tableName, ArrayList<VariableInfo> variables){
        this.dbName = dbName;
        this.tableName = new DataTableName(tableName.toString());
        this.variables = variables;
    }

    public DatabaseSelectionEvent(Object source, String dbName, String tableName, ArrayList<VariableInfo> variables){
        this.dbName = new DatabaseName(dbName);
        this.tableName = new DataTableName(tableName);
        this.variables = variables;
    }

    public DatabaseName getDatabaseName(){
        return dbName;
    }

    public DataTableName getTableName(){
        return tableName;
    }

    public ArrayList<VariableInfo> getVariables(){
        return variables;
    }

}

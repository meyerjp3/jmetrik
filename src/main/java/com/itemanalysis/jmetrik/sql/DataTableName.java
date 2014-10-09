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

package com.itemanalysis.jmetrik.sql;

/**
 * This class creates a table name that is safe to use for SQL statements.
 * All punctuation and space are removed from the name. A prefix of
 * "tbl" is added to all data tables for use in the CachedDataModel.
 * A prefix of "vtbl" is added to all variable tables for use in the
 * DataKeyTableModel.
 *
 *
 */
public class DataTableName extends AbstractSqlSafeTableName implements Comparable<DataTableName>{
    
    public DataTableName(String originalName){
        super(originalName);
    }

    /**
     * Return String value needed by the database. Application users are unaware fo this value.
     *
     * @return
     */
    public String toString(){
        return displayName();
    }
    
    public String getTableName(){
        return displayName();
    }
    
    public String getTableNameForDatabase(){
        return "TBL" + newTableName;
    }

    public String getNameForDatabase(){
        return "TBL" + newTableName;
    }

    /**
     * This string is the value shown to application users.
     *
     * @return
     */
    public String displayName(){
        String s = "";
        if(newTableName.startsWith("TBL")){
            s = newTableName.substring(0, "TBL".length());
        }else if(newTableName.startsWith("VTBL")){
            s = newTableName.substring(0, "VTBL".length());
        }else{
            s = newTableName;
        }
        return s;
    }

    @Override
    public boolean equals(Object o){
        return (o instanceof DataTableName) && (this.compareTo((DataTableName)o)==0);
    }

    @Override
    public int hashCode(){
        return this.getTableName().hashCode();
    }

    public int compareTo(DataTableName o){
        return this.getTableName().compareTo(o.getTableName());
    }


}


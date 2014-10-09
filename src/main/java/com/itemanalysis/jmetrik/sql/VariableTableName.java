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

public class VariableTableName extends AbstractSqlSafeTableName implements Comparable<VariableTableName>{

    public VariableTableName(String tableName){
        super(tableName);
    }

    public String getTableName(){
        return displayName();
    }

    public String getTableNameForDatabase(){
        return "TBL" + newTableName;
    }

    /**
     * Return String value needed by the database. Application users are unaware fo this value.
     *
     * @return
     */
    public String toString(){
        return displayName();
    }

    public String getNameForDatabase(){
        return "VTBL" + newTableName;
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

    public int compareTo(VariableTableName o){
        return this.getTableName().compareTo(o.getTableName());
    }

}

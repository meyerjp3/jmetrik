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
 * Takes a string and removes special characters and punctuation. This change is
 * done to avoid invalid SQL statements. Classes that extend this class
 * add prefixes to the name to avoid problems with SQL reserved words.
 */
public abstract class AbstractSqlSafeTableName implements SqlSafeTableName {

    public String originalTableName = "";

    public String newTableName = "";

    public AbstractSqlSafeTableName(String originalName){
        this.originalTableName = originalName.trim().toUpperCase();
        newTableName = checkName(this.originalTableName);
    }

    public final String checkName(String originalName){
        String a = originalName.trim().toUpperCase().replaceAll("\\s+", "");//table names are always upper case (i.e. case-insensitive)
//        String b = a.replaceAll("\\p{Punct}+", ""); //revoed all punctuation - removed August 2012
        String b = a.replaceAll("\\W", "");//only keep letters, numbers, and underscore - added August 2012
        String c = "";
        if(b.startsWith("TBL")){
            c = b.substring("TBL".length(), b.length());
        }else if(b.startsWith("VTBL")){
            c = b.substring("VTBL".length(), b.length());
        }else{
            c = b;
        }

        //maximum length is 128 characters in Apache Derby
        //limit non-prefixed names (i.e. without TBL or VTBL) to 120 characters
        c = c.substring(0,Math.min(c.length(),120));

        return c;
    }

    public String printNameChange(){
        String s = "";
        s += originalTableName + " changed to ";
        s += newTableName;
        return s;
    }

    public boolean nameChanged(){
        return !originalTableName.equals(newTableName);
    }



}

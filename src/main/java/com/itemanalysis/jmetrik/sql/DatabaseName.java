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

package com.itemanalysis.jmetrik.sql;

public class DatabaseName implements Comparable<DatabaseName>{

    public String originalName = "";

    public String newName = "";
    
    public DatabaseName(String originalName){
        this.originalName = originalName;
        newName = checkName(originalName);
    }

    public final String checkName(String originalName){
        String a = originalName.trim().toLowerCase().replaceAll("\\s+", "");//database names are always lower case (i.e. case-insensitive)
        String b = a.replaceAll("\\p{Punct}+", "");

        //maximum length of database name is 240 characters - this choice is arbitrary
        String c = b.substring(0,Math.min(b.length(),240));

        return c;
    }

    public boolean nameChanged(){
        if(!originalName.toLowerCase().equals(newName)){
            return true;
        }
        return false;
    }
    
    public String getName(){
        return newName;
    }
    
    public String getOriginalName(){
        return originalName;
    }

    public int compareTo(DatabaseName o){
        return this.getName().compareTo(o.getName());
    }

    @Override
    public String toString(){
        return newName;
    }
    
}

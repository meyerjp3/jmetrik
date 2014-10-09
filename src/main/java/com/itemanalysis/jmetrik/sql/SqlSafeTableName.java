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
 * Interface for an object that represents database table names.
 * 
 */
public interface SqlSafeTableName {

    public String checkName(String name);
    
    public String getTableName();
    
    public String getTableNameForDatabase();

    public String displayName();

    public String getNameForDatabase();

    public String printNameChange();

    public boolean nameChanged();

}

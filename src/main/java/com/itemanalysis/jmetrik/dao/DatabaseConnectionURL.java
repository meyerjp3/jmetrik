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

package com.itemanalysis.jmetrik.dao;

import com.itemanalysis.jmetrik.sql.DatabaseName;

/**
 * The connection URL for various types of databases (e.g. Apache Derby, MySQL) differ in
 * meaningful ways. This interface provides the general contract for all connection URLs.
 * It is an interface for database specific URLs (implemented as DAOs). there should
 * be a specific implementation of this interface for each type of database.
 */
public interface DatabaseConnectionURL {

    /**
     * Name of database. Required.
     */
    public void setDatabaseName(String name);

    /**
     * Name of database. Required.
     */
    public void setDatabaseName(DatabaseName name);

    /**
     * @param host URL or absolute path to database. Required.
     */
    public void setHost(String host);

    /**
     * Mainly for Microsoft SQL Server. Optional parameter.
     */
    public void setInstance(String instance);

    /**
     * Needed for remote connections.
     */
    public void setPort(String port);
    
    public void setUsernameAndPassword(String username, String password);

    /**
     * MySQL and MicrosoftSQL call these properties. Apache Derby calls them attributes.
     * @param property
     * @param value
     */
    public void setProperty(String property, String value);
    
    public void removeProperty(String property);

    public String getConnectionUrl();

    public String getPathAndName();
    
    public String getPath();
    
    public DatabaseName getName();

    public void clearProperties();

}

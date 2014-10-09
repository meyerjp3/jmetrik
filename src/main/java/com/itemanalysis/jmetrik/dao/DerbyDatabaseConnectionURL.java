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

import java.util.HashMap;

/**
 * Apache Derby specific implementation of the DatabaseConnectionURL interface.
 *
 */
public class DerbyDatabaseConnectionURL implements DatabaseConnectionURL {

    private String base = "jdbc:derby:";

    private DatabaseName dbName = null;

    private String host = "";

    private String instance = "";

    private String port = "";
    
    private String username = "";
    
    private String password = "";//NOT encrypted!

    private HashMap<String, String> props = null;

    public DerbyDatabaseConnectionURL(){
        props = new HashMap<String, String>();
    }
    
    public void setDatabaseName(String dbName){
        this.dbName = new DatabaseName(dbName.trim());
    }

    public void setDatabaseName(DatabaseName dbName){
        this.dbName = dbName;
    }

    public void setHost(String host){
        this.host = host.replaceAll("\\\\", "/");
    }
    
    public void setUsernameAndPassword(String username, String password){
        this.username = username;
        this.password = password;
    }

    public void setInstance(String instance){
        //Not needed for Derby.
    }

    public void setPort(String port){
        this.port = port;
    }

    /**
     * MySQL and MicrosoftSQL call these properties. Apache Derby calls them attributes.
     * @param property
     * @param value
     */
    public void setProperty(String property, String value){
        props.put(property, value);
    }
    
    public void removeProperty(String property){
        props.remove(property);
    }

    public String getConnectionUrl(){
        StringBuilder url = new StringBuilder();
        url.append(base);
//        url.append(host);
//        url.append("/");
        url.append(dbName);
        if(!username.equals("") && !password.equals("")){
            url.append(";user=" + username);
            url.append(";password=" + password);
        }
        for(String s : props.keySet()){
            url.append(";");
            url.append(s);
            url.append("=");
            url.append(props.get(s));
        }
        return url.toString();
    }

    public String getPathAndName(){
        return host + "/" + dbName;
    }
    
    public String getPath(){
        return host;
    }
    
    public DatabaseName getName(){
        return dbName;
    }
    
    public void clearProperties(){
        for(String s : props.keySet()){
            props.remove(s);
        }
    }

}

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
import com.itemanalysis.jmetrik.workspace.DatabaseCommand;
import com.itemanalysis.jmetrik.workspace.JmetrikPassword;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.psychometrics.data.VariableName;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;

public class DerbyDatabaseCreator extends SwingWorker<String, Void> implements DatabaseCreator {

    private DatabaseCommand command = null;

    private DatabaseName dbName = null;

    private Throwable theException = null;
    
    static Logger logger = Logger.getLogger("jmetrik-logger");    

    public DerbyDatabaseCreator(DatabaseCommand command){
        this.command = command;
    }

    public DerbyDatabaseConnectionURL createDatabase()throws SQLException, IOException, IllegalArgumentException {
        DerbyDatabaseConnectionURL connURL = null;
        Connection conn = null;
        Statement stmt = null;
        FileInputStream in = null;
        FileOutputStream out = null;

        if(command.getSelectOneOption("action").isValueSelected("create")){
            connURL = new DerbyDatabaseConnectionURL();
            String name = command.getFreeOption("name").getString();

            dbName = new DatabaseName(name);

            try{
                connURL.setDatabaseName(dbName.getName());
                connURL.setProperty("create", "true");
                conn = DriverManager.getConnection(connURL.getConnectionUrl());

                checkAuthorizationProperties(conn, name);
                connURL.removeProperty("create");

                //create shadow database for storing the number of rows in each table
                //this table is mainly used for initializing progress bars
                VariableName tableName = new VariableName("tablename");
                VariableName rowCount = new VariableName("rowcount");
                VariableName tableDescription = new VariableName("description");

                String sqlString = "CREATE TABLE JMKTBLROWS (" +
                        tableName.nameForDatabase() + " VARCHAR(255), " +
                        rowCount.nameForDatabase() + " INT, " +
                        tableDescription.nameForDatabase() + " VARCHAR(1000))";
                stmt = conn.createStatement();
                stmt.execute(sqlString);

                //create properties file in the database folder to indicate the current version of the database
                JmetrikPreferencesManager prefs = new JmetrikPreferencesManager();
                String dbHome = prefs.getDatabaseHome();
                Properties p = new Properties();
                File f = new File(dbHome + "/jmetrik-db-version.props");
                if(!f.exists()) f.createNewFile();
                in = new FileInputStream(f);
                p.load(in);
                in.close();

                String dbName = dbHome + "/" + conn.getMetaData().getURL();
                dbName = dbName.replaceAll("[\\\\/:]+", ".");
                dbName = dbName.replaceAll("[.]+", ".");
                p.setProperty(dbName, "version3");

                out = new FileOutputStream(f);
                p.store(out, "#DO NOT MODIFY - JMETRIK CONFIGURATION FILE - DO NOT MODIFY");
                out.close();

            }catch(SQLException ex){
                throw(ex);
            }catch(IOException ex){
                throw(ex);
            }finally {
                if(stmt!=null) stmt.close();
                if(conn!=null) conn.close();
                if(in!=null) in.close();
                if(out!=null) out.close();
            }



            
        }
        return connURL;
    }

    private void enableUserAuthentication(String username, String password, Connection conn)throws SQLException{

        String setProperty = "CALL SYSCS_UTIL.SYSCS_SET_DATABASE_PROPERTY(";
        String close = ")";
        String auth = setProperty + "'derby.connection.requireAuthentication', 'true'" + close;
        String provider = setProperty + "'derby.authentication.provider', 'BUILTIN'" + close;
        String access = setProperty + "'derby.database.defaultConnectionMode', 'noAccess'" + close;
        String user1 = setProperty + "'derby.user." + username + "', '" + password + "'" + close;
        String user2 = setProperty + "'derby.user.root', 'jmetrik'" + close;
        String rw = setProperty + "'derby.database.fullAccessUsers', '" + username + ",root'" + close;
        String dbprops = setProperty + "'derby.database.propertiesOnly', 'true'" + close;
        
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(auth);
        stmt.executeUpdate(provider);
        stmt.executeUpdate(access);
        stmt.executeUpdate(user1);
        stmt.executeUpdate(user2);
        stmt.executeUpdate(rw);

        //caution
//        stmt.executeUpdate(dbprops);
        
        stmt.close();
        
    }
    
    private void checkAuthorizationProperties(Connection conn, String dbName)throws SQLException{
        Statement stmt = conn.createStatement();
        String results = "Database created: " + dbName + " \n";
        String getProperty = "VALUES SYSCS_UTIL.SYSCS_GET_DATABASE_PROPERTY(";
        String close = ")";
        ResultSet rs = stmt.executeQuery(getProperty + "'derby.connection.requireAuthentication'" + close);
        rs.next();
        results += "Value of requireAuthentication is " +  rs.getString(1) + "\n";

        rs = stmt.executeQuery(getProperty +  "'derby.database.defaultConnectionMode'" + close);
        rs.next();
        results += "Value of defaultConnectionMode is " +  rs.getString(1) + "\n";

        rs = stmt.executeQuery(getProperty + "'derby.database.fullAccessUsers'" + close);
        rs.next();
        results += "Value of fullAccessUsers is " +  rs.getString(1) + "\n";
        rs.close();
        stmt.close();

        logger.info(results);

    }
    
    private void setAuthorizationProperties(String username, String pw)throws IOException{
        JmetrikPreferencesManager prefs = new JmetrikPreferencesManager();
        String dbAuthHome = prefs.getDatabaseHome();
        String dbAuthName = prefs.getDatabaseAuthenticationName();
        
        Properties p = new Properties();
        File f = new File(dbAuthHome + "/" + dbAuthName);
        FileInputStream in = new FileInputStream(f);
        p.load(in);
        in.close();

        JmetrikPassword password = new JmetrikPassword();
        p.setProperty(username, password.encodePassword(pw));//set db username and password
        p.setProperty(dbName.getName(), username);//set username for db

        FileOutputStream out = new FileOutputStream(f);
        p.store(out, "#DO NOT MODIFY - JMETRIK CONFIGURATION FILE - DO NOT MODIFY");
        out.close();
    }

    @Override
    protected String doInBackground()throws Exception{
        firePropertyChange("status", "", "Creating database...");
        firePropertyChange("progress-ind-on", null, null);
        logger.info(command.paste());
        try{
            this.createDatabase();
        }catch(Exception ex){
            theException = ex;
        }
        return "";
    }

    @Override
    protected void done(){
        try{
            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }else{
                firePropertyChange("status", "", "Ready");
            }
            firePropertyChange("progress-off", null, null);
        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
            firePropertyChange("progress-off", null, null);
        }
    }

}

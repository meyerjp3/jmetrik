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

import com.itemanalysis.jmetrik.workspace.*;

import java.sql.Connection;

public class JmetrikDatabaseFactory {

    private DatabaseType dbType = null;

    public JmetrikDatabaseFactory(DatabaseType dbType){
        this.dbType = dbType;
    }

    /**
     * Returns a class for implementing specific database URLs.
     *
     * @return
     */
    public DatabaseConnectionURL getDatabaseConnectionURL(){
        switch (dbType) {
            case APACHE_DERBY:
                return new DerbyDatabaseConnectionURL();
            case MYSQL:
                break;
            case MICROSOFT_SQL:
                break;
        }
        return new DerbyDatabaseConnectionURL();//default
    }

    /**
     * Returns a SwingWorker class for importing data
     *
     * @param conn
     * @param command
     * @return
     */
    public DelimitedFileImporter getDelimitedFileImporter(Connection conn, ImportCommand command){
        switch (dbType) {
            case APACHE_DERBY:
                return new DerbyDelimitedFileImporter(conn, command);
            case MYSQL:
                break;
            case MICROSOFT_SQL:
                break;
        }
        return new DerbyDelimitedFileImporter(conn, command);//default is APACHE_DERBY database
    }

    public DelimitedFileExporter getDelimitedFileExporter(Connection conn, ExportCommand command){
        switch (dbType) {
            case APACHE_DERBY:
                return new DerbyDelimitedFileExporter(conn, command);
            case MYSQL:
                break;
            case MICROSOFT_SQL:
                break;
        }
        return new DerbyDelimitedFileExporter(conn, command);//default is APACHE_DERBY database
    }

    /**
     * Database creators will probably only exist for Apache Derby.
     * MySQL and other databases will likely already exist.
     *
     * @return
     */
    public DatabaseCreator getDatabaseCreator(DatabaseCommand command){
        switch (dbType) {
            case APACHE_DERBY:
                return new DerbyDatabaseCreator(command);
            case MYSQL:
                break;
            case MICROSOFT_SQL:
                break;
        }
        return new DerbyDatabaseCreator(command);//default is APACHE_DERBY database
    }

    /**
     * Return  a DatabaseDeleter for a given database system.
     *
     * @param command
     * @return
     */
    public DatabaseDeleter getDatabaseDeleter(DatabaseCommand command){
        switch (dbType) {
            case APACHE_DERBY:
                return new DerbyDatabaseDeleter(command);
            case MYSQL:
                break;
            case MICROSOFT_SQL:
                break;
        }
        return new DerbyDatabaseDeleter(command);//default is APACHE_DERBY database
    }

    /**
     * Return  database access object.
     *
     */
    public DatabaseAccessObject getDatabaseAccessObject(){
        switch (dbType) {
            case APACHE_DERBY:
                return new DerbyDatabaseAccessObject();
            case MYSQL:
                break;
            case MICROSOFT_SQL:
                break;
        }
        return new DerbyDatabaseAccessObject();//default is APACHE_DERBY database
    }

    public DatabaseTableDeleter getDatabaseTableDeleter(Connection conn, DatabaseAccessObject dao, DatabaseCommand command){
        switch (dbType) {
            case APACHE_DERBY:
                return new DerbyDatabaseTableDeleter(conn, dao, command);
            case MYSQL:
                break;
            case MICROSOFT_SQL:
                break;
        }
        return new DerbyDatabaseTableDeleter(conn, dao, command);//default is APACHE_DERBY database
    }

    public DatabaseVariableDeleter getDatabaseVariableDeleter(Connection conn, DatabaseAccessObject dao, DeleteVariableCommand command){
        switch (dbType) {
            case APACHE_DERBY:
                return new DerbyDatabaseVariableDeleter(conn, dao, command);
            case MYSQL:
                break;
            case MICROSOFT_SQL:
                break;
        }
        return new DerbyDatabaseVariableDeleter(conn, dao, command);//default is APACHE_DERBY database
    }

    public DatabaseVariableRenamer getDatabaseVariableRenamer(Connection conn, DatabaseAccessObject dao, RenameVariableCommand command){
        switch (dbType) {
            case APACHE_DERBY:
                return new DerbyDatabaseVariableRenamer(conn, dao, command);
            case MYSQL:
                break;
            case MICROSOFT_SQL:
                break;
        }
        return new DerbyDatabaseVariableRenamer(conn, dao, command);//default is APACHE_DERBY database
    }

}

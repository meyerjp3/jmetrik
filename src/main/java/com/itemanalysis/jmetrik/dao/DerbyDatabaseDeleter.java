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

package com.itemanalysis.jmetrik.dao;

import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.DatabaseCommand;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

public class DerbyDatabaseDeleter extends SwingWorker<String, Void> implements DatabaseDeleter{

    private DatabaseCommand command = null;

    private DatabaseName dbName = null;

    private Throwable theException = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public DerbyDatabaseDeleter(DatabaseCommand command){
        this.command = command;
    }

    public void deleteDatabase()throws SQLException, IOException, IllegalArgumentException{
        DerbyDatabaseConnectionURL connURL = null;

        if(command.getSelectOneOption("action").isValueSelected("delete-db")){

            connURL = new DerbyDatabaseConnectionURL();
            String name = command.getFreeOption("name").getString();

            dbName = new DatabaseName(name);

            firePropertyChange("workspace-close", "", dbName);//will close workspace

            //TODO check authorization - only allow deletion if authorized

            JmetrikPreferencesManager prefs = new JmetrikPreferencesManager();
            String path = prefs.getDatabaseHome();
            path += "/" + name;

            firePropertyChange("status", "", "Deleting database: " + name );
            boolean result = deleteFolder(new File(path));


            //TODO remove authorization properties for this database


        }
    }

    /**
     *
     * Dangerous method. It is declared final to prevent from being overridden
     * and used for destructive purposes.
     *
     * @param dir
     * @return
     */
    private boolean deleteFolder(File dir){
        if(dir.exists()){
            if(dir.isDirectory()){
                String[] children = dir.list();
                for (int i=0; i<children.length; i++) {
                    boolean success = deleteFolder(new File(dir, children[i]));
                    if (!success) {
                        return false;
                    }
                }
            }
            // The directory is empty so delete it
            return dir.delete();
        }
        return false;
    }

    @Override
    protected String doInBackground()throws Exception{
        firePropertyChange("status", "", "Deleting database...");
        firePropertyChange("progress-ind-on", null, null);
        logger.info(command.paste());
        try{
            deleteDatabase();
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

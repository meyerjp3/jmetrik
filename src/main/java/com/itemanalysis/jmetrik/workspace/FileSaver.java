/*
 * Copyright (c) 2013 Patrick Meyer
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

package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.swing.JmetrikTab;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

public class FileSaver extends SwingWorker<String, Void> {

    private File file = null;
    private JmetrikTextFile textFile;
    private JmetrikTab tab = null;
    private Throwable theException = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public FileSaver(File file, JmetrikTextFile textFile){
        this.file = file;
        this.textFile = textFile;
    }

    public FileSaver(File file, JmetrikTextFile textFile, JmetrikTab tab){
        this.file = file;
        this.textFile = textFile;
        this.tab = tab;
    }

    @Override
    protected String doInBackground(){
        firePropertyChange("status", "", "Saving file...");
        firePropertyChange("progress-ind-on", null, null);

        try{
            if(!file.exists()) file.createNewFile();
            BufferedWriter bWrite=new BufferedWriter(new FileWriter(file));
            bWrite.write(textFile.getText());
            bWrite.close();
        }catch(Exception ex){
            theException = ex;
        }

        return "";

    }

    @Override
    protected void done(){
        if(theException!=null){
            logger.fatal(theException);
            firePropertyChange("error", "", "Error - Check log for details.");
        }else{
            if(tab!=null) tab.setTitle(file.getName());
            firePropertyChange("file-saved", "", "Saved");
            firePropertyChange("status", "", "Ready");
        }
        firePropertyChange("progress-off", null, null);

    }

}

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

package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FileOpener extends SwingWorker<StringBuffer, Void>{
    
    private File file = null;
    private JmetrikTextFile textFile;
    private Throwable theException = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public FileOpener(String fileName, JmetrikTextFile textFile){
        this(new File(fileName), textFile);
    }
    
    public FileOpener(File file, JmetrikTextFile textFile){
        this.file = file;
        this.textFile = textFile;
    }

    public StringBuffer readTextFile()throws IOException {
        StringBuffer text = new StringBuffer();
        String s="";

        try{
            BufferedReader bRead = new BufferedReader(new FileReader(file));
            while((s = bRead.readLine()) != null){
                text.append(s);
                text.append("\n");
            }
            bRead.close();
        }catch(IOException ex){
            ex.printStackTrace();
            throw new IOException(ex);
        }
        return text;
    }

    protected StringBuffer doInBackground() throws Exception{
        firePropertyChange("status", "", "Opening file...");
        firePropertyChange("progress-ind-on", null, null);
        StringBuffer s = null;
        try{
            s=readTextFile();
        }catch(Throwable t){
            theException=t;
        }
        return s;
    }

    @Override
    protected void done(){
        try{
            if(theException!=null){
                logger.fatal(theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }else{
                StringBuffer s = get();
                textFile.setText(s.toString());
                textFile.setCaretPosition(0);
                firePropertyChange("status", "", "Ready");
            }
            firePropertyChange("progress-off", null, null);
        }catch(Exception ex){
            logger.fatal(theException);
            firePropertyChange("error", "", "Error - Check log for details.");
            firePropertyChange("progress-off", null, null);
        }
    }
    
}

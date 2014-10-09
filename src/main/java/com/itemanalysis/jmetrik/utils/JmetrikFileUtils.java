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

package com.itemanalysis.jmetrik.utils;

import com.itemanalysis.jmetrik.swing.JmetrikTab;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.swing.SimpleFilter;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;

public class JmetrikFileUtils {
    
    private JFileChooser saveChooser = null;
    
    private JFileChooser openChooser = null;

    private SimpleFilter csvFilter = null;
    
    private SimpleFilter txtFilter = null;

    private ArrayList<PropertyChangeListener> propertyChangeListeners = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");
    
    public JmetrikFileUtils(){
        txtFilter = new SimpleFilter("txt", "Text Files (*.txt)");
        csvFilter = new SimpleFilter("csv", "CSV Files (*.csv)");
        propertyChangeListeners = new ArrayList<PropertyChangeListener>();
    }
    
    public File chooseFileToSave(Component parent, File directory){
        if(saveChooser==null) saveChooser = new JFileChooser();
        saveChooser.setCurrentDirectory(directory);
        saveChooser.setSelectedFile(directory);
        saveChooser.setFileFilter(txtFilter);
        saveChooser.addChoosableFileFilter(csvFilter);
        saveChooser.addChoosableFileFilter(txtFilter);
        saveChooser.setAcceptAllFileFilterUsed(false);
        saveChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        saveChooser.setDialogTitle("Save File");

        if(saveChooser.showDialog(parent, "Save File") != JFileChooser.APPROVE_OPTION) return null;

        String path = saveChooser.getSelectedFile().getAbsolutePath();
        String fileName = saveChooser.getSelectedFile().getName();
        String fileType = "";

        if(!fileName.endsWith(".txt") && !fileName.endsWith(".csv")){
            path+=".txt";
        }
        File f=new File(path);
        return f;
    }
    
    public File chooseFileToOpen(Component parent){
        if(openChooser==null) openChooser = new JFileChooser();
        openChooser.setFileFilter(txtFilter);
        openChooser.addChoosableFileFilter(csvFilter);
        openChooser.addChoosableFileFilter(txtFilter);
        openChooser.setAcceptAllFileFilterUsed(true);
        openChooser.setDialogType(JFileChooser.OPEN_DIALOG);
        openChooser.setDialogTitle("Open File");

        if(openChooser.showDialog(parent, "Open File") != JFileChooser.APPROVE_OPTION) return null;
        File f=openChooser.getSelectedFile();
        return f;
    }
    
//    public boolean saveExistingFile(Component parent, File f){
//        if(f.fileExists()){
//            Toolkit.getDefaultToolkit().beep();
//            int question = JOptionPane.showConfirmDialog(parent,
//                    f.getName() + " already fileExists. Do you want to overwrite it?",
//                    "File Exists",
//                    JOptionPane.YES_NO_OPTION,
//                    JOptionPane.QUESTION_MESSAGE);
//            if(question==JOptionPane.YES_OPTION) {
//                return true;
//            }else{
//                return false;
//            }
//        }
//        return true;
//    }

//    public void save(final JmetrikTextFile textFile, final File f){
//        Runnable task = new SwingWorker<String, Void>(){
//            protected String doInBackground()throws Exception{
//                textFile.fileSaveAs(f);
//                return "";
//            }
//
//            protected void done(){
//                try{
//                    get();
//                }catch(Exception ex){
//                    logger.fatal(ex.getMessage(), ex);
//                    this.firePropertyChange("error", "", "Error - Check log for details.");
//                }
//            }
//        };
//        task.run();
//    }
//
//    public void save(final JmetrikTextFile textFile, final File f, final JmetrikTab tab){
//        Runnable task = new SwingWorker<String, Void>(){
//            protected String doInBackground()throws Exception{
//                textFile.fileSaveAs(f);
//                return "";
//            }
//
//            protected void done(){
//                try{
//                    get();
//                    tab.setTitle(textFile.getFileName());
//                }catch(Exception ex){
//                    logger.fatal(ex.getMessage(), ex);
//                    this.firePropertyChange("error", "", "Error - Check log for details.");
//                }
//            }
//        };
//        task.run();
//    }

    //===============================================================================================================
    //Process messages here
    //  Note that SwingWorker classes also implement these methods. Just need to add list of
    //  propertyChangeListeners to SwingWorker classes. See importTable(...) for an example.
    //===============================================================================================================
    public synchronized void addPropertyChangeListener(PropertyChangeListener l){
        propertyChangeListeners.add(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l){
        propertyChangeListeners.remove(l);
    }

    public synchronized void firePropertyChange(String propertyName, Object oldValue, Object newValue){
        PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for(PropertyChangeListener l : propertyChangeListeners){
            l.propertyChange(e);
        }
    }
    
    
}

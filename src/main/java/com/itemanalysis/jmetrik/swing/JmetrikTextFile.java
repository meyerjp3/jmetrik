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

package com.itemanalysis.jmetrik.swing;

import com.itemanalysis.jmetrik.utils.JmetrikFileUtils;
import com.itemanalysis.jmetrik.workspace.FileOpener;
import com.itemanalysis.jmetrik.workspace.FileSaver;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.Formatter;

public class JmetrikTextFile extends JTextArea{

    protected File f = null;
    final UndoManager undoer;
    private Document doc;
    private JmetrikPreferencesManager p = null;
    private boolean unsavedChanges = false;
    private static final String USER_HOME = System.getProperty("user.home").replaceAll("\\\\", "/");
    static Logger logger = Logger.getLogger("jmetrik-logger");

    protected Action undoAction, redoAction;

    public JmetrikTextFile(){
        setBorder(new EmptyBorder(5,5,5,5));
        setBackground(Color.WHITE);
        initializeFont();

        undoer = new UndoManager();
        doc = getDocument();
        doc.addDocumentListener(new UpdateListener());
        doc.addUndoableEditListener(new Undoer());

    }

    /**
     * Font obtained from jmetrik preferences
     */
    private void initializeFont(){
        p = new JmetrikPreferencesManager();
        Font font = p.getFont();
        setFont(font);
    }

    public void fileSave(){
        if(f!=null){
            FileSaver fileSaver = new FileSaver(f,  this);
            PropertyChangeListener[] pcl = this.getPropertyChangeListeners();
            for(PropertyChangeListener p :pcl){
                fileSaver.addPropertyChangeListener(p);
            }
            fileSaver.addPropertyChangeListener(new FileSavedListener());
            fileSaver.execute();
        }
    }

    public void fileSave(JmetrikTab tab){
        if(f!=null){
            FileSaver fileSaver = new FileSaver(f,  this, tab);
            PropertyChangeListener[] pcl = this.getPropertyChangeListeners();
            for(PropertyChangeListener p :pcl){
                fileSaver.addPropertyChangeListener(p);
            }
            fileSaver.addPropertyChangeListener(new FileSavedListener());
            fileSaver.execute();
        }
    }

    public void fileSave(File f){
        this.f = f;
        FileSaver fileSaver = new FileSaver(f,  this);
        PropertyChangeListener[] pcl = this.getPropertyChangeListeners();
        for(PropertyChangeListener p :pcl){
            fileSaver.addPropertyChangeListener(p);
        }
        fileSaver.addPropertyChangeListener(new FileSavedListener());
        fileSaver.execute();
    }

    public void fileSave(File f, JmetrikTab tab){
        this.f = f;
        FileSaver fileSaver = new FileSaver(f,  this, tab);
        PropertyChangeListener[] pcl = this.getPropertyChangeListeners();
        for(PropertyChangeListener p :pcl){
            fileSaver.addPropertyChangeListener(p);
        }
        fileSaver.addPropertyChangeListener(new FileSavedListener());
        fileSaver.execute();
    }

    public void openFile(final File f){
        this.f = f;
        FileOpener fileOpener = new FileOpener(f,  this);
        PropertyChangeListener[] pcl = this.getPropertyChangeListeners();
        for(PropertyChangeListener p :pcl){
            fileOpener.addPropertyChangeListener(p);
        }
        fileOpener.execute();

    }

    public Document getTextFileDocument(){
        doc = getDocument();
        return doc;
    }

    public boolean hasFile(){
        return f!=null;
    }

    /*
      * Method for adding a String object to the TextFile. User's will
      *  be able to edit the JTextArea directly. This method is for, say,
      *  adding the contents of one file or the output of a method
      *  to this file.
      *
      * aStringToAdd - String to be added to TextFile
      */
    public void addText(String aStringToAdd){
        append(aStringToAdd);
    }

    /*
      * Method for adding a Formatter object to the TextFile. User's will
      *  be able to edit the JTextArea directly. This method is for, say,
      *  adding the contents of one file or the output of a method
      *  to this file.
      *
      * aFormatterToAdd - a Formatter object to be added to TextFile
      */
    public void addText(Formatter aFormatterToAdd){
        append(aFormatterToAdd.toString());
    }

    /*
      * Returns the file name if teh file has been created.
      *  Otherwise a blank file name is returned.
      */
    public String getFileName(){
        if(f==null) return "text file";
        return f.getName();
    }

    public File getFile(){
        return f;
    }
    
//    public void setFile(File f){
//        this.f = f;
//    }

    public boolean canUndo(){
        return undoer.canUndo();
    }

    public boolean canRedo(){
        return undoer.canRedo();
    }

    public void undoText(){
        try{
            undoer.undo();
        }catch(CannotRedoException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", null, "Error - Check log for details.");
        }
    }

    public void redoText(){
        try{
            undoer.redo();
        }catch(CannotRedoException ex){
            logger.fatal(ex.getMessage(), ex);
            this.firePropertyChange("error", null, "Error - Check log for details.");
        }
    }

    public int promptToSave(Component parent){
        int result = -1;
        if(unsavedChanges){
            String fileName = this.getFileName();
            result = JOptionPane.showConfirmDialog(parent,
                "Save changes to " + fileName + " before closing tab?",
                "Save Changes",
                JOptionPane.YES_NO_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE);
            if(result==JOptionPane.YES_OPTION){
                if(f==null){
                    JmetrikFileUtils fUtils = new JmetrikFileUtils();
                    f = fUtils.chooseFileToSave(parent, new File(USER_HOME));
                    if(f==null) return JOptionPane.CANCEL_OPTION;
                }
                fileSave();
            }
        }else{
            result = JOptionPane.YES_OPTION;
        }
        return result;
    }

    class UpdateListener implements DocumentListener {

        public void insertUpdate(DocumentEvent e){
            unsavedChanges = true;
        }

        public void removeUpdate(DocumentEvent e){
            unsavedChanges = true;
        }

        public void changedUpdate(DocumentEvent e){
            unsavedChanges = true;
        }

    }

    class Undoer implements UndoableEditListener {
        public Undoer(){
            undoer.die();
        }

        public void undoableEditHappened(UndoableEditEvent e){
            undoer.addEdit(e.getEdit());
        }
    }

    class FileSavedListener implements PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent evt){
            if("file-saved".equals(evt.getPropertyName())){
                unsavedChanges = false;
            }
        }

    }
    
}

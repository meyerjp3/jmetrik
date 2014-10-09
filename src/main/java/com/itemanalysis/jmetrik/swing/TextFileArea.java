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

package com.itemanalysis.jmetrik.swing;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Formatter;

import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.Document;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.UndoManager;
import org.apache.log4j.Logger;

public class TextFileArea extends JTextArea{

    String fileName = "";
    int fontSize = 12;
    Font defaultFont = null;
    BufferedReader bRead = null;
    BufferedWriter bWrite = null;
    boolean textChanged = false;
    protected File f = null;
    final UndoManager undoer;
    Document doc;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    protected Action undoAction, redoAction;

    /*
      * Constructor for blank JTextArea. Must use the setFile method
      * before saving file.
      */
    public TextFileArea(){
        setBorder(new EmptyBorder(5,5,5,5));
        setBackground(Color.WHITE);
        initializeFont();

        undoer = new UndoManager();
        doc = getDocument();
        doc.addDocumentListener(new UpdateListener());
        doc.addUndoableEditListener(new Undoer());

    }

    /*
      * aFileName - a String indicating the complete path and file name
      */
    public TextFileArea(String aFileName){
        setBorder(new EmptyBorder(5,5,5,5));
        setBackground(Color.WHITE);
        initializeFont();

        undoer = new UndoManager();
        doc = getDocument();
        doc.addDocumentListener(new UpdateListener());
        doc.addUndoableEditListener(new Undoer());

        fileName = aFileName;
        f=new File(fileName);
//		openFile(f);
    }

    public final void initializeFont(){//FIXME allow users to select font in application properties
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        boolean courierFound = false;
        int i = 0;

        while(!courierFound && i< fontNames.length){
//            System.out.println(fontNames[i]);
            if(fontNames[i].equals("Courier")){
                defaultFont = new Font("Courier",Font.PLAIN, fontSize);
                courierFound = true;
            }else if(fontNames[i].equals("Courier New")){
                defaultFont = new Font("Courier New",Font.PLAIN, 14);
                courierFound=true;
            }else{
                defaultFont = new Font("Lucida Sans Typewriter",Font.PLAIN, fontSize);
            }
            i++;
        }
        setFont(defaultFont);
//        System.out.println(this.getFont());
    }

//	public void openFile(File aF){

//		String s="";
//
//		f = new File(aFileName);
//		if(f == null || !f.isFile() || !f.fileExists()) return;
//
//		try{
//			setText("");
//			bRead = new BufferedReader(new FileReader(f));
//			while((s = bRead.readLine()) != null){
//				append(s);
//				append("\n");
//			}
//			bRead.close();
//		}catch(IOException ex){
////			throw statisticsException with stack trace
//		}
//		textChanged=false;
//	}

    public void setFile(String aFileName){
        f = new File(aFileName);
    }

    /*
      * Method for saving the existing file.
      */
    public void fileSave()throws IOException{
        try{
            bWrite=new BufferedWriter(new FileWriter(f));
            bWrite.write(getText());
            bWrite.close();
        }catch(IOException ex){
            throw new IOException(ex);
        }
        textChanged=false;
    }

    /*
      * Method for saving file with a new name.
      *
      * aNewFile  - a File that is based on the new name.
      */
    public void fileSaveAs(File aNewFile){
        f = aNewFile;
        try{
            f.createNewFile();
            bWrite=new BufferedWriter(new FileWriter(f));
            bWrite.write(getText());
            bWrite.close();
        }catch(IOException ex){
//			throw statisticsException with stack trace
        }
        textChanged=false;
    }

    public Document getTextFileDocument(){
        doc = getDocument();
        return doc;
    }

    public boolean textChanged(){
        return textChanged;
    }

    public void setTextChanged(boolean aChanged){
        textChanged=aChanged;
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
        textChanged = true;
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
        textChanged = true;
    }

    /*
      * Change the font size of the JTextArea. he default font
      *  is 10 pt Courier.
      *
      * aNewFontSize - int of new font size
      */
    public void setFontSize(int aNewFontSize){
        fontSize = aNewFontSize;
        defaultFont = new Font("Courier",Font.PLAIN, fontSize);
        setFont(defaultFont);
    }

    /*
      * Change the font style of the JTextArea. The default font
      *  is 10 pt Courier.
      *
      * aNewFontType - a Strig of the new font type
      */
    public void setFontType(String aNewFontType){
        defaultFont = new Font(aNewFontType,Font.PLAIN, fontSize);
        setFont(defaultFont);
    }

    /*
      * Returns the file name if teh file has been created.
      *  Otherwise a blank file name is returned.
      */
    public String getFileName(){
        if(f!=null){
            return f.getName();
        }
        return "";
    }

    public File getFile(){
        return f;
    }

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

        }
    }

    public void redoText(){
        try{
            undoer.redo();
        }catch(CannotRedoException ex){

        }
    }

    public void showException(){
        JOptionPane.showMessageDialog(TextFileArea.this,
                "An error occurred. \n" +
                        "Copy the information in the Log file and \n" +
                        "submit it along with a help request to the \n" +
                        " jMetrik help forum at www.itemAnalysis.com.\n" +
                        "You can access the Log file by clicking\n" +
                        "View > Log File",
                "Error - Send Log File with Help Request",
                JOptionPane.ERROR_MESSAGE);
    }

    class UpdateListener implements DocumentListener{

        public void insertUpdate(DocumentEvent e){
            textChanged = true;
        }

        public void removeUpdate(DocumentEvent e){
            textChanged = true;
        }

        public void changedUpdate(DocumentEvent e){
            textChanged = true;
        }

    }

    class Undoer implements UndoableEditListener{
        public Undoer(){
            undoer.die();
        }

        public void undoableEditHappened(UndoableEditEvent e){
            undoer.addEdit(e.getEdit());
        }
    }

}

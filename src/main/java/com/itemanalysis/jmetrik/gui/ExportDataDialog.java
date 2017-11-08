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

package com.itemanalysis.jmetrik.gui;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.workspace.ExportCommand;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ExportDataDialog extends JDialog{

    private ExportCommand command = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    String dataDelimiter = "comma" ;

    ExportPanel delimiterDialogPanel;

    boolean canRun = false;

    private boolean exportRawData = true;

    private boolean useQuotes = false;

    private DatabaseName dbName = null;

    private DataTableName tableName = null;

    protected String currentDirectory = "user.home";

    private JCheckBox scoredBox = null;

    private JCheckBox quoteBox = null;

    private ButtonGroup firstGroup = null;

    private ButtonGroup delimGroup = null;

    public ExportDataDialog(Jmetrik parent, DatabaseName dbName, DataTableName tableName, String currentDirectory){
        this.dbName = dbName;
        this.tableName = tableName;
        this.currentDirectory = currentDirectory;
        showExportDialog(parent);
    }

    public boolean canRun(){
        return canRun;
    }

    public ExportCommand getCommand(){
        return command;
    }

    public String getCurrentDirectory(){
        return currentDirectory;
    }

    private void showExportDialog(Jmetrik parent){
        delimiterDialogPanel = new ExportPanel();

        JFileChooser exportChooser = new JFileChooser();
        SimpleFilter txtFilter = new SimpleFilter("txt", "Text Files (*.txt)");
        SimpleFilter csvFilter = new SimpleFilter("csv", "CSV Files (*.csv)");
        exportChooser.addChoosableFileFilter(txtFilter);
        exportChooser.addChoosableFileFilter(csvFilter);
        exportChooser.setFileFilter(csvFilter);
        exportChooser.setAcceptAllFileFilterUsed(false);

        exportChooser.setCurrentDirectory(new File(currentDirectory));
        exportChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        exportChooser.setSelectedFile(new File(tableName.getTableName() + ".csv"));

        exportChooser.setAccessory(delimiterDialogPanel);
        exportChooser.setDialogType(JFileChooser.SAVE_DIALOG);
        exportChooser.setDialogTitle("Exporting: " + tableName.getTableName());

        if(exportChooser.showDialog(parent, "OK") == JFileChooser.APPROVE_OPTION){
            String fileName = exportChooser.getSelectedFile().toString();

            if("comma".equals(delimGroup.getSelection().getActionCommand())){
                if(!fileName.endsWith(".csv")) fileName += ".csv";
            }else{
                if(!fileName.endsWith(".txt")) fileName += ".txt";
            }
            File f = new File(fileName);


            int choice = JOptionPane.YES_OPTION;
            if(f.exists()){
                String[] options = {"Yes", "No"};
                choice = JOptionPane.showOptionDialog(parent,
                        "Selected file already fileExists.\n" +
                                "Do you want to overwrite existing file?",
                        "File Already Exists",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        options,
                        options[0]);
            }

            if(choice==JOptionPane.YES_OPTION){
                try{
                    command = new ExportCommand();
                    command.getPairedOptionList("data").addValue("db",dbName.getName());
                    command.getPairedOptionList("data").addValue("table",tableName.getTableName());

                    command.getFreeOption("file").add(f.toString());
                    command.getSelectOneOption("delimiter").setSelected(delimGroup.getSelection().getActionCommand());
                    command.getSelectOneOption("header").setSelected(firstGroup.getSelection().getActionCommand());

                    command.getSelectAllOption("options").setSelected("scored", !exportRawData);
                    command.getSelectAllOption("options").setSelected("quotes", useQuotes);

                    canRun = true;
                    String path = f.getAbsolutePath().toString();
                    path = path.replaceAll("\\\\", "/");
                    path = path.substring(0, path.lastIndexOf("/"));
                    currentDirectory = path;

                }catch(IllegalArgumentException ex){
                    logger.fatal(ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(parent,
                            ex.getMessage(),
                            "Syntax Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }





        }

    }



    class SimpleFilter extends FileFilter{
        private String extension=null;
        private String description=null;
        public SimpleFilter(String aExtension, String aDescription){
            extension="."+aExtension.toLowerCase();
            description=aDescription;
        }
        public String getDescription(){
            return description;
        }
        public boolean accept(File f){
            if(f==null)
                return false;
            if(f.isDirectory())
                return true;
            return f.getName().toLowerCase().endsWith(extension);
        }
    }


    class ExportPanel extends JPanel{
        private static final long serialVersionUID = 1L;

        public ExportPanel(){
            dataDelimiter="comma";
            JPanel delimPanel = new JPanel(new GridLayout(4,1));
            Border delimBorder = BorderFactory.createTitledBorder("Delimiter");
            delimPanel.setBorder(delimBorder);
            delimGroup = new ButtonGroup();
            JRadioButton commaButton = new JRadioButton("Comma");
            commaButton.setActionCommand("comma");
            commaButton.setSelected(true);
            delimGroup.add(commaButton);
            delimPanel.add(commaButton);

            JRadioButton tabButton = new JRadioButton("Tab");
            tabButton.setActionCommand("tab");
            delimGroup.add(tabButton);
            delimPanel.add(tabButton);

            JRadioButton semiButton = new JRadioButton("Semicolon");
            semiButton.setActionCommand("semicolon");
            delimGroup.add(semiButton);
            delimPanel.add(semiButton);

            JRadioButton colonButton = new JRadioButton("Colon");
            colonButton.setActionCommand("colon");
            delimGroup.add(colonButton);
            delimPanel.add(colonButton);

            JPanel firstRow = new JPanel(new GridLayout(2,1));
            Border firstBorder = BorderFactory.createTitledBorder("Variable Names");
            firstRow.setBorder(firstBorder);
            firstGroup = new ButtonGroup();
            JRadioButton firstRowButton = new JRadioButton("In first row");
            firstRowButton.setActionCommand("included");
            firstRowButton.setSelected(true);
            firstGroup.add(firstRowButton);
            firstRow.add(firstRowButton);

            JRadioButton noNamesButton = new JRadioButton("None");
            noNamesButton.setActionCommand("excluded");
            firstGroup.add(noNamesButton);
            firstRow.add(noNamesButton);

            JPanel optionsPanel = new JPanel();
            optionsPanel.setBorder(new TitledBorder("Options"));
            optionsPanel.setLayout(new GridLayout(2,1));

            scoredBox = new JCheckBox("Scored items");
            scoredBox.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    if(scoredBox.isSelected()) exportRawData=false;
                    else exportRawData=true;
                }
            });
            optionsPanel.add(scoredBox);

            quoteBox = new JCheckBox("Use quotes");
            quoteBox.addActionListener(new ActionListener(){
                public void actionPerformed(ActionEvent e){
                    if(quoteBox.isSelected()) useQuotes=true;
                    else useQuotes=false;
                }
            });
            optionsPanel.add(quoteBox);

            JPanel mainDiagPanel = new JPanel(new GridLayout(3,1));
            mainDiagPanel.add(delimPanel);
            mainDiagPanel.add(firstRow);
            mainDiagPanel.add(optionsPanel);
            add(mainDiagPanel);

        }
    }


}

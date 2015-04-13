/*
 *  Copyright (C) 2011 J. Patrick Meyer
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * FastImportDialog.java
 *
 * Created on Jun 22, 2011, 6:56:01 AM
 */

package com.itemanalysis.jmetrik.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import javax.swing.*;
import javax.swing.border.Border;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.swing.ExtensionFileFilter;
import com.itemanalysis.jmetrik.workspace.ImportCommand;
import org.apache.log4j.Logger;

/**
 *
 * @author J. Patrick Meyer
 */
public class ImportDialog extends JDialog {

    private DelimiterPanel delimiterDialogPanel;
    private ImportCommand command = null;
    protected ButtonGroup delimGroup  = null;
    protected ButtonGroup headerGroup = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    protected String currentDirectory = ".";
    private String importLocation;
    private boolean canRun = false;
    private int rows = -1;
    private DatabaseName dbName = null;

    // Variables declaration - do not modify
    private JButton browseButton;
    private JButton cancelButton;
    private JTextField dataFileTextField;
    private JLabel dataLabel;
    private JLabel descriptionLabel;
    private JTextArea descriptionTextArea;
    private JButton importButton;
    private JScrollPane descriptionScrollPane;
    private JTextField nameTextField;
    private JLabel tableLabel;
    // End of variables declaration


    /** Creates new form FastImportDialog */
    public ImportDialog(Jmetrik parent, DatabaseName dbName, String currentDirectory) {
        super(parent,"Import Data",true);
        this.dbName = dbName;
        this.currentDirectory = currentDirectory;
        delimiterDialogPanel = new DelimiterPanel();

        //prevent running an analysis when window close button is clicked
        this.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e){
                canRun = false;
            }
        });

        initComponents();
        setLocationRelativeTo(parent);
    }

 private void initComponents() {

        tableLabel = new JLabel();
        nameTextField = new JTextField();
        dataLabel = new JLabel();
        dataFileTextField = new JTextField();
        descriptionLabel = new JLabel();
        descriptionScrollPane = new JScrollPane();
        descriptionTextArea = new JTextArea();
        browseButton = new JButton(new BrowseImportAction("Browse"));
        importButton = new JButton(new OkAction("Import"));
        cancelButton = new JButton(new CancelAction("Cancel"));

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        tableLabel.setText("Table Name:");

        nameTextField.setMaximumSize(new Dimension(300, 28));
        nameTextField.setMinimumSize(new Dimension(300, 28));
        nameTextField.setPreferredSize(new Dimension(300, 28));

        dataLabel.setText("Data File:");

        dataFileTextField.setMaximumSize(new Dimension(300, 28));
        dataFileTextField.setMinimumSize(new Dimension(300, 28));
        dataFileTextField.setPreferredSize(new Dimension(300, 28));

        descriptionLabel.setText("Description:");

        descriptionScrollPane.setMaximumSize(new Dimension(300, 112));
        descriptionScrollPane.setMinimumSize(new Dimension(300, 112));
        descriptionScrollPane.setPreferredSize(new Dimension(300, 112));

        descriptionTextArea.setColumns(20);
        descriptionTextArea.setLineWrap(true);
        descriptionTextArea.setRows(5);
        descriptionTextArea.setTabSize(4);
        descriptionTextArea.setWrapStyleWord(true);
        descriptionScrollPane.setViewportView(descriptionTextArea);

        browseButton.setText("Browse");
        browseButton.setMaximumSize(new Dimension(72, 28));
        browseButton.setMinimumSize(new Dimension(72, 28));
        browseButton.setPreferredSize(new Dimension(72, 28));

        importButton.setText("Import");
        importButton.setMaximumSize(new Dimension(72, 28));
        importButton.setMinimumSize(new Dimension(72, 28));
        importButton.setPreferredSize(new Dimension(72, 28));

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new Dimension(72, 28));
        cancelButton.setMinimumSize(new Dimension(72, 28));
        cancelButton.setPreferredSize(new Dimension(72, 28));

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(descriptionLabel)
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(tableLabel)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(17, 17, 17)
                                                .addComponent(dataLabel))))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(dataFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(descriptionScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(browseButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(150, 150, 150)
                        .addComponent(importButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(tableLabel)
                    .addComponent(nameTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(dataLabel)
                    .addComponent(dataFileTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(browseButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(descriptionLabel)
                    .addComponent(descriptionScrollPane, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(importButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    public ImportCommand getCommand(){
        return command;
    }

    public boolean canRun(){
        return canRun;
    }

    public String getCurrentDirectory(){
        return currentDirectory;
    }

    public class OkAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Import delimited data file";

        public OkAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public OkAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public OkAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            String tableName = nameTextField.getText().trim();
            importLocation = dataFileTextField.getText().trim().replaceAll("\\\\", "/");

            if(tableName.equals("")){
                JOptionPane.showMessageDialog(
                        ImportDialog.this,
                        "Please type a value for Data Name.",
                        "Data Name Error",
                        JOptionPane.ERROR_MESSAGE);
            }else if(importLocation.equals("")){
                JOptionPane.showMessageDialog(
                        ImportDialog.this,
                        "Please type a value for Import Data File.",
                        "Import Data File Error",
                        JOptionPane.ERROR_MESSAGE);
            }else if(tableName.indexOf("variables")>-1){
                JOptionPane.showMessageDialog(
                        ImportDialog.this,
                        "The data name may not contain the word 'variables'.",
                        "Data Name Error",
                        JOptionPane.ERROR_MESSAGE);
            }else{

                DataTableName derbyTableName = new DataTableName(nameTextField.getText().trim());
                nameTextField.setText(derbyTableName.toString());

                try{
                    command = new ImportCommand();
                    command.getPairedOptionList("data").addValue("db", dbName.getName());
                    command.getPairedOptionList("data").addValue("table", derbyTableName.toString());

                    command.getFreeOption("file").add(importLocation);

                    command.getSelectOneOption("delimiter").setSelected(delimGroup.getSelection().getActionCommand());
                    command.getSelectOneOption("header").setSelected(headerGroup.getSelection().getActionCommand());

                    String desc = descriptionTextArea.getText().trim();
                    command.getFreeOption("description").add(desc);

                }catch(IllegalArgumentException ex){
                    logger.fatal(ex.getMessage(), ex);
                    JOptionPane.showMessageDialog(ImportDialog.this,
                            ex.getMessage(),
                            "Syntax Error",
                            JOptionPane.ERROR_MESSAGE);
                }


                if(derbyTableName.nameChanged()){
                    JOptionPane.showMessageDialog(
                            ImportDialog.this,
                            derbyTableName.printNameChange(),
                            "Data Name Changed",
                            JOptionPane.INFORMATION_MESSAGE);
                }

                canRun=true;
                setVisible(false);
            }



        }

    }//end

    public class CancelAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Cancel";

        public CancelAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public CancelAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public CancelAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            canRun=false;
            setVisible(false);

        }


    }//end

    public class BrowseImportAction extends AbstractAction{

        private static final long serialVersionUID = 1L;
        final static String TOOL_TIP = "Browse for files";
        JFileChooser importChooser = new JFileChooser();
        String description = "Text Files (*.txt; *.csv)";
        String[] extension = {"txt", "csv"};
        ExtensionFileFilter filter = new ExtensionFileFilter(description, extension);

        public BrowseImportAction(String text, ImageIcon icon, Integer mnemonic){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public BrowseImportAction(String text, ImageIcon icon){
            super(text, icon);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public BrowseImportAction(String text){
            super(text);
            putValue(SHORT_DESCRIPTION, TOOL_TIP);
        }

        public void actionPerformed(ActionEvent e){
            importChooser.setCurrentDirectory(new File(currentDirectory));
            makeimportDialog();
            if(importChooser.showDialog(ImportDialog.this, "Browse") != JFileChooser.APPROVE_OPTION){
//				send error message
                return;
            }
//
            File f=importChooser.getSelectedFile();
            dataFileTextField.setText(f.getAbsolutePath());
            currentDirectory = f.getAbsolutePath();
        }

        protected void makeimportDialog(){
            importChooser.setAcceptAllFileFilterUsed(true);
            importChooser.addChoosableFileFilter(filter);
            importChooser.setFileFilter(filter);

            importChooser.setAccessory(delimiterDialogPanel);
            importChooser.setDialogType(JFileChooser.OPEN_DIALOG);
            importChooser.setDialogTitle("Import Data");
            importChooser.setVisible(true);
        }

    }//end

    class DelimiterPanel extends JPanel{
        private static final long serialVersionUID = 1L;

        public DelimiterPanel(){
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
            headerGroup = new ButtonGroup();
            JRadioButton firstRowButton = new JRadioButton("In first row");
            firstRowButton.setActionCommand("included");
            firstRowButton.setSelected(true);
            headerGroup.add(firstRowButton);
            firstRow.add(firstRowButton);

            JRadioButton noNamesButton = new JRadioButton("None");
            noNamesButton.setActionCommand("excluded");
            headerGroup.add(noNamesButton);
            firstRow.add(noNamesButton);

            JPanel mainDiagPanel = new JPanel(new GridLayout(2,1));
            mainDiagPanel.add(delimPanel);
            mainDiagPanel.add(firstRow);
            add(mainDiagPanel);
        }
    }

}


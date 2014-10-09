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

package com.itemanalysis.jmetrik.gui;

import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.swing.GraphOptionPanel;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class JmetrikPreferencesDialog extends JDialog{

    private JFileChooser logLocationChooser;
    private JFileChooser databaseLocationChooser;
    private JPanel buttonPanel;
    private JButton cancelButton;
    private JButton logHomeButton;
    private JLabel logLabel;
    private JTextField logText;
    private JPanel dbOptionPanel;
    private JList fontFamilyList;
    private JScrollPane fontFamilyScrollPane;
    private JPanel fontPanel;
    private JScrollPane fontSizeScrollPane;
    private JList fontStyleList;
    private JScrollPane fontStyleScrollPane;
    private JButton hostButton;
    private JLabel hostLabel;
    private JTextField hostText;
    private JList fontSizeList;
    private JPanel jmetrikoptionPanel;
    private JButton okButton;
    private JLabel portLabel;
    private JTextField portText;
    private JLabel precisionLabel;
    private JTextField precisionText;
    private JLabel previewLabel;
    private JPanel previewPanel;
    private JComboBox typeComboBox;
    private JLabel typeLabel;
    private JTabbedPane tabbedPane = null;
    private GraphOptionPanel graphOptionsPanel = null;
    JmetrikPreferencesManager prefs = null;
    
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public JmetrikPreferencesDialog(Jmetrik parent, JmetrikPreferencesManager prefs){
        super(parent, "jMetrik Preferences", true);
        this.prefs = prefs;

        initComponents();
        loadPreferences();

        setLocationRelativeTo(parent);
        setResizable(false);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);


    }

    private void initComponents(){
        tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Program Options", getMainOptionPanel());
        graphOptionsPanel = new GraphOptionPanel(prefs);
        tabbedPane.addTab("Chart Options", graphOptionsPanel);
        getContentPane().add(tabbedPane);

        pack();
    }

    private JPanel getMainOptionPanel() {
        JPanel mainOptionPanel = new JPanel();
        mainOptionPanel.setLayout(new GridBagLayout());

        GridBagConstraints gridBagConstraints;
        dbOptionPanel = new JPanel();
        hostLabel = new JLabel();
        hostText = new JTextField();
        hostButton = new JButton();
        portLabel = new JLabel();
        portText = new JTextField();
        typeLabel = new JLabel();
        typeComboBox = new JComboBox();
        buttonPanel = new JPanel();
        okButton = new JButton("OK");
        okButton.addActionListener(new OkActionListener());
        cancelButton = new JButton(new CancelAction("Cancel"));
        jmetrikoptionPanel = new JPanel();
        logHomeButton = new JButton();
        logLabel = new JLabel();
        logText = new JTextField();
        precisionLabel = new JLabel();
        precisionText = new JTextField();
        fontPanel = new JPanel();
        fontFamilyScrollPane = new JScrollPane();
        fontFamilyList = new JList();
        fontStyleScrollPane = new JScrollPane();
        fontStyleList = new JList();
        fontSizeScrollPane = new JScrollPane();
        fontSizeList = new JList();
        previewPanel = new JPanel();
        previewLabel = new JLabel();

        dbOptionPanel.setBorder(BorderFactory.createTitledBorder("Database Options"));
        dbOptionPanel.setLayout(new GridBagLayout());

        hostLabel.setText("Home:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        dbOptionPanel.add(hostLabel, gridBagConstraints);

        hostText.setToolTipText("Location (e.g. host or path) of database");
        hostText.setMinimumSize(new Dimension(300, 28));
        hostText.setPreferredSize(new Dimension(300, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        dbOptionPanel.add(hostText, gridBagConstraints);

        hostButton.setText("Browse");
        hostButton.setToolTipText("Choose location for database files.");
        hostButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (databaseLocationChooser == null) databaseLocationChooser = new JFileChooser();
                databaseLocationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                databaseLocationChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                databaseLocationChooser.setDialogTitle("Select Location");
                if (databaseLocationChooser.showDialog(JmetrikPreferencesDialog.this, "OK") != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File f = databaseLocationChooser.getSelectedFile();
                hostText.setText(f.getAbsolutePath());
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        dbOptionPanel.add(hostButton, gridBagConstraints);

        portLabel.setText("Port:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        dbOptionPanel.add(portLabel, gridBagConstraints);

        portText.setToolTipText("Database port");
        portText.setMinimumSize(new Dimension(350, 28));
        portText.setPreferredSize(new Dimension(350, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        dbOptionPanel.add(portText, gridBagConstraints);

        typeLabel.setText("Type:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        dbOptionPanel.add(typeLabel, gridBagConstraints);

        typeComboBox.setModel(new DefaultComboBoxModel(new String[]{
                DatabaseType.APACHE_DERBY.toString(),
                DatabaseType.MYSQL.toString(),
                DatabaseType.MICROSOFT_SQL.toString(),
                DatabaseType.ORACLE.toString()
        }));
        typeComboBox.setToolTipText("Type of database");
        typeComboBox.setMinimumSize(new Dimension(300, 28));
        typeComboBox.setPreferredSize(new Dimension(300, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        dbOptionPanel.add(typeComboBox, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        mainOptionPanel.add(dbOptionPanel, gridBagConstraints);

        buttonPanel.setLayout(new GridBagLayout());

        //OK button
        okButton.setMaximumSize(new Dimension(70, 23));
        okButton.setMinimumSize(new Dimension(70, 23));
        okButton.setPreferredSize(new Dimension(70, 23));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        buttonPanel.add(okButton, gridBagConstraints);

        //cancel button
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        buttonPanel.add(cancelButton, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        mainOptionPanel.add(buttonPanel, gridBagConstraints);

        jmetrikoptionPanel.setBorder(BorderFactory.createTitledBorder("jMetrik Options"));
        jmetrikoptionPanel.setLayout(new GridBagLayout());

        logHomeButton.setText("Browse");
        logHomeButton.setToolTipText("Choose location for log file.");
        logHomeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (logLocationChooser == null) logLocationChooser = new JFileChooser();
                logLocationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                logLocationChooser.setDialogType(JFileChooser.OPEN_DIALOG);
                logLocationChooser.setDialogTitle("Select Location");
                if (logLocationChooser.showDialog(JmetrikPreferencesDialog.this, "OK") != JFileChooser.APPROVE_OPTION) {
                    return;
                }

                File f = logLocationChooser.getSelectedFile();
                logText.setText(f.getAbsolutePath());
            }
        });
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        jmetrikoptionPanel.add(logHomeButton, gridBagConstraints);

        logLabel.setText("Log home:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        jmetrikoptionPanel.add(logLabel, gridBagConstraints);

        logText.setToolTipText("Location of log file");
        logText.setMinimumSize(new Dimension(300, 28));
        logText.setPreferredSize(new Dimension(300, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(5, 0, 5, 5);
        jmetrikoptionPanel.add(logText, gridBagConstraints);

        precisionLabel.setText("Precision:");
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        jmetrikoptionPanel.add(precisionLabel, gridBagConstraints);

        precisionText.setToolTipText("Number of decimal places in output");
        precisionText.setMinimumSize(new Dimension(300, 28));
        precisionText.setPreferredSize(new Dimension(300, 28));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 0, 5, 5);
        jmetrikoptionPanel.add(precisionText, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        mainOptionPanel.add(jmetrikoptionPanel, gridBagConstraints);

        fontPanel.setBorder(BorderFactory.createTitledBorder("Font Options"));
        fontPanel.setLayout(new GridBagLayout());

        fontFamilyScrollPane.setMinimumSize(new Dimension(200, 100));
        fontFamilyScrollPane.setPreferredSize(new Dimension(200, 100));

        fontFamilyList.setModel(getFontNameModel());
        fontFamilyList.addListSelectionListener(new FontSelectinListener());
        fontFamilyList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fontFamilyScrollPane.setViewportView(fontFamilyList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        fontPanel.add(fontFamilyScrollPane, gridBagConstraints);

        fontStyleScrollPane.setMinimumSize(new Dimension(125, 100));
        fontStyleScrollPane.setPreferredSize(new Dimension(125, 100));

        fontStyleList.setModel(getFontStyleModel());
        fontStyleList.addListSelectionListener(new FontSelectinListener());
        fontStyleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        fontStyleScrollPane.setViewportView(fontStyleList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        fontPanel.add(fontStyleScrollPane, gridBagConstraints);

        fontSizeScrollPane.setMinimumSize(new Dimension(100, 100));
        fontSizeScrollPane.setPreferredSize(new Dimension(100, 100));

        fontSizeList.setModel(getFontSizeModel());
        fontSizeList.addListSelectionListener(new FontSelectinListener());
        fontSizeScrollPane.setViewportView(fontSizeList);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new Insets(5, 5, 5, 5);
        fontPanel.add(fontSizeScrollPane, gridBagConstraints);

        previewPanel.setBorder(BorderFactory.createTitledBorder("Preview"));
        previewPanel.setLayout(new GridBagLayout());

        previewLabel.setText("AaBbYyZz");
        previewLabel.setMaximumSize(new Dimension(300, 30));
        previewLabel.setMinimumSize(new Dimension(150, 30));
        previewLabel.setPreferredSize(new Dimension(150, 30));
        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new Insets(5, 30, 5, 5);
        previewPanel.add(previewLabel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        fontPanel.add(previewPanel, gridBagConstraints);

        gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = GridBagConstraints.BOTH;
        gridBagConstraints.insets = new Insets(0, 5, 5, 5);
        mainOptionPanel.add(fontPanel, gridBagConstraints);

        return mainOptionPanel;
    }

    private void loadPreferences(){
        logText.setText(prefs.getLogHome());
        hostText.setText(prefs.getDatabaseHome());
        int port = prefs.getDatabasePort();
        if(port > 0) portText.setText(Integer.toString(port));
        precisionText.setText(Integer.toString(prefs.getPrecision()));
        typeComboBox.setSelectedItem(prefs.getDatabaseType());
        
        Font font = prefs.getFont();
        
        fontFamilyList.setSelectedValue(font.getFontName(), true);
        switch(font.getStyle()){
            case Font.PLAIN: fontStyleList.setSelectedValue("Regular", true); break;
            case Font.BOLD: fontStyleList.setSelectedValue("Bold", true); break;
            case Font.ITALIC: fontStyleList.setSelectedValue("Italic", true); break;
        }
        
        fontSizeList.setSelectedValue(font.getSize(), true);
        graphOptionsPanel.setSelectedLegendPosition(prefs.getLegendPosition());
        graphOptionsPanel.setSelectedShowLegend(prefs.getShowLegend());
        graphOptionsPanel.setSelectedShowMarkers(prefs.getShowMarkers());
        graphOptionsPanel.setSelectedOrientation(prefs.getChartOrientation());
        graphOptionsPanel.setChartWidth(prefs.getChartWidth());
        graphOptionsPanel.setChartHeight(prefs.getChartHeight());
        graphOptionsPanel.setChartLineWidth(prefs.getChartLineWidth());

    }

    private Color[] getSelectedColors(){
        return graphOptionsPanel.getSelectedColors();
    }
    
    private Font getSelectedFont(){
        String name = "";
        String style = "";
        Integer size = 12;

        if(fontFamilyList.getSelectedIndex()!=-1) name = (String)fontFamilyList.getSelectedValue();
        if(fontStyleList.getSelectedIndex()!=-1)  style = (String)fontStyleList.getSelectedValue();
        if(fontSizeList.getSelectedIndex()!=-1)  size = (Integer)fontSizeList.getSelectedValue();

        int styleInt = Font.PLAIN;
        if(style.equals("Bold")){
            styleInt = Font.BOLD;
        }else if(style.equals("Italic")){
            styleInt = Font.ITALIC;
        }

        Font f = new Font(name, styleInt, size);
        return f;
    }

    private DefaultListModel getFontNameModel(){
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        String[] fontNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

        DefaultListModel nameModel = new DefaultListModel();
        for(String s : fontNames){
            nameModel.addElement(s);
        }
        return nameModel;
    }
    
    private DefaultListModel getFontStyleModel(){
        DefaultListModel m = new DefaultListModel();
        m.addElement("Regular");
        m.addElement("Bold");
        m.addElement("Italic");
        return m;
    }
    
    private DefaultListModel getFontSizeModel(){
        DefaultListModel m = new DefaultListModel();
        for(int i=2;i<51;i++){
            m.addElement(i);
        }
        return m;
    }

    public class OkActionListener implements ActionListener{

        public void actionPerformed(ActionEvent e){

            String newHost = hostText.getText().trim().replaceAll("\\\\", "/");
            String oldHost = prefs.getDatabaseHome();
            if(!newHost.equals(oldHost)){
                JOptionPane.showMessageDialog(JmetrikPreferencesDialog.this,
                        "You must restart jMetrik for these changes to take effect." ,
                        "jMetrik Restart Required",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            if(!newHost.equals("")){
                prefs.setDatabaseHome(newHost);
            }

            String port = portText.getText().trim();
            if(!port.equals("")){
                int p = Integer.parseInt(port);
                prefs.setDatabasePort(p);
            }

            String log = logText.getText().trim().replaceAll("\\\\", "/");
            if(!log.equals("")) prefs.setLogHome(log);

            String precision = precisionText.getText().trim();
            if(!precision.equals("")){
                int p = Integer.parseInt(precision);
                prefs.setPrecision(p);
            }
            
            prefs.setDatabaseType(typeComboBox.getActionCommand().toString());
            
            Font f = getSelectedFont();
            prefs.setFont(f);

            prefs.setColors(graphOptionsPanel.getSelectedColors());
            prefs.setLineStyles(graphOptionsPanel.getSelectedLineStyles());

//            prefs.printAllPreferences(); //debugging only

            setVisible(false);
        }

    }//end OkAction

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
            setVisible(false);
        }


    }//end  Cancel Action

    class FontSelectinListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e){
            if(!e.getValueIsAdjusting()){
                previewLabel.setFont(getSelectedFont());
            }
        }

    }



//    public class BrowseTempLocationAction extends AbstractAction{
//
//        private static final long serialVersionUID = 1L;
//        final static String TOOL_TIP = "Browse Location";
//
//
//        public BrowseTempLocationAction(String text, ImageIcon icon, Integer mnemonic){
//            super(text, icon);
//            putValue(SHORT_DESCRIPTION, TOOL_TIP);
//            putValue(MNEMONIC_KEY, mnemonic);
//        }
//
//        public BrowseTempLocationAction(String text, ImageIcon icon){
//            super(text, icon);
//            putValue(SHORT_DESCRIPTION, TOOL_TIP);
//        }
//
//        public BrowseTempLocationAction(String text){
//            super(text);
//            putValue(SHORT_DESCRIPTION, TOOL_TIP);
//        }
//
//        public void actionPerformed(ActionEvent e){
//            if(tempLocationChooser==null) tempLocationChooser = new JFileChooser();
//            tempLocationChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//            tempLocationChooser.setDialogType(JFileChooser.OPEN_DIALOG);
//            tempLocationChooser.setDialogTitle("Select Location");
//            if(tempLocationChooser.showDialog(JmetrikPreferencesDialog.this, "OK") != JFileChooser.APPROVE_OPTION){
//                return;
//            }
//
//            File f=tempLocationChooser.getSelectedFile();
//            tempText.setText(f.getAbsolutePath());
//        }
//
//    }//end BrowseLocationAction





}

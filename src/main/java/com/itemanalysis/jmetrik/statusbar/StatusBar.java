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

package com.itemanalysis.jmetrik.statusbar;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;


/**
 *
 * The text of the JLabel in the statusbar is set using a property change listener.
 * Classes or methods that need to set  the statusbar text should invoke either
 *
 *      firePropertyChange("error", null, "Error - Check log for details.");
 * or
 *      firePropertyChange("status", null, "Ready");
 *
 * on their propertyChangeListeners. Use "status" for any message that is not an error
 * message. Use "error" is the message is an error message. The use of "error" will
 * trigger the display of a JOptionPane error dialog (see Workspace.java).
 *
 *
 */
public class StatusBar extends JPanel{

    private JLabel statusLabel = null;

    private JLabel dbLabel = null;

    private JProgressBar progressBar = null;

    private StatusListener statusListener = null;

    public StatusBar(int width, int height){
        super();
        super.setPreferredSize(new Dimension(width, height));
        super.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        int statusWidth = (int)(width*0.6);
        int dbWidth = (int)((width-statusWidth)*0.5);
        int progressWidth = width-statusWidth-dbWidth;
        int panelHeight = 25;

        //create status label panel
        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(new EmptyBorder(0,5,0,0));
        JPanel statusPanel = new JPanel();
        statusPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        statusPanel.setPreferredSize(new Dimension(statusWidth, panelHeight));

        statusPanel.setMaximumSize(new Dimension(statusWidth, panelHeight));
        statusPanel.setMinimumSize(new Dimension(statusWidth, panelHeight));

        statusPanel.setLayout(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        statusPanel.add(statusLabel, c);

        //create database label panel
        dbLabel = new JLabel("");
        dbLabel.setBorder(new EmptyBorder(0,5,0,0));
        JPanel dbPanel = new JPanel();
        dbPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        dbPanel.setPreferredSize(new Dimension(statusWidth, panelHeight));

        dbPanel.setMaximumSize(new Dimension(dbWidth, panelHeight));
        dbPanel.setMinimumSize(new Dimension(dbWidth, panelHeight));

        dbPanel.setLayout(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        dbPanel.add(dbLabel, c);


        //create progress bar panel
        progressBar = new JProgressBar();
        progressBar.setStringPainted(false);
        progressBar.setVisible(false);
        JPanel progressPanel = new JPanel();
        progressPanel.setBorder(BorderFactory.createLoweredBevelBorder());
        progressPanel.setPreferredSize(new Dimension(progressWidth, panelHeight));

        progressPanel.setMaximumSize(new Dimension(progressWidth, panelHeight));
        progressPanel.setMinimumSize(new Dimension(progressWidth, panelHeight));

        progressPanel.setLayout(new GridBagLayout());
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        progressPanel.add(progressBar, c);

        //add subpanels to main panel
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.gridheight = 1;
        c.weightx = 3;
        c.weighty = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        this.add(statusPanel, c);

        c.gridx = 3;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        this.add(dbPanel, c);

        c.gridx = 4;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.BOTH;
        this.add(progressPanel, c);

        //instantiate listener
        statusListener = new StatusListener();


    }

    public StatusListener getStatusListener(){
        return statusListener;
    }
    
    public void showProgressBar(boolean indeterminate){
        progressBar.setVisible(true);
        progressBar.setIndeterminate(indeterminate);
    }
    
    public void hideProgressBar(){
        if(progressBar.isIndeterminate()){
            progressBar.setIndeterminate(false);
        }
        progressBar.setVisible(false);
        progressBar.setValue(0);
    }

    class StatusListener implements PropertyChangeListener{
        public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            int oldVal = 0;
            int newVal = 0;
            int progress = 0;

            if("progress".equals(propertyName)){
                progress = (Integer)e.getNewValue();
                progressBar.setValue(progress);
            }else if("progress-on".equals(propertyName)){//show progress bar
                showProgressBar(false);
            }else if("progress-ind-on".equals(propertyName)){//show indeterminant progress bar
                showProgressBar(true);
            }else if("progress-off".equals(propertyName)){
                hideProgressBar();
            }else if("db-selection".equals(propertyName)){
                dbLabel.setText(e.getNewValue().toString());
            }else if("status".equals(propertyName) || "error".equals(propertyName)){
                statusLabel.setText(e.getNewValue().toString());
            }
        }
    }


}

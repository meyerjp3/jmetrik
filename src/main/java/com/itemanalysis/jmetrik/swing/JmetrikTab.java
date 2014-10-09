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

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.net.URL;

public class JmetrikTab extends JPanel {

    private JLabel tabLabel = null;
    
    private String toolTipText = null;

    private JButton closeButton = null;
    
    public JmetrikTab(String labelText, String toolTipText){
        this.tabLabel = new JLabel(labelText);
        this.toolTipText = toolTipText;
        initButton();
    }

    public JmetrikTab(String labelText){
        this.tabLabel = new JLabel(labelText);
        initButton();
    }

    private void initButton(){
        this.setOpaque(false);

        String urlString = "/images/cleanLight.png";
        URL url = this.getClass().getResource( urlString );

        ImageIcon closeIcon = new ImageIcon(url);

        closeButton = new JButton(closeIcon);
        closeButton.setRolloverEnabled(true);
        closeButton.setBorderPainted(false);
        closeButton.setContentAreaFilled(false);
        
        Dimension buttonDimensions = new Dimension(16, 16);
        closeButton.setPreferredSize(buttonDimensions);
        closeButton.setMinimumSize(buttonDimensions);

        urlString = "/images/cleanHeavy.png";
        url = this.getClass().getResource( urlString );
        ImageIcon closeHoveredIcon = new ImageIcon(url);
        closeButton.setRolloverIcon(closeHoveredIcon);
        if(toolTipText!=null) closeButton.setToolTipText(toolTipText);


        this.setLayout(new GridBagLayout());
        GridBagConstraints g = new GridBagConstraints();
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 1;
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(0,0,0,4);
        this.add(tabLabel, g);

        g.gridx = 1;
        g.gridy = 0;
        g.gridwidth = 1;
        g.fill = GridBagConstraints.NONE;
        g.insets = new Insets(0,0,0,0);
        this.add(closeButton, g);

    }

    public void addActionListener(ActionListener listener){
        closeButton.addActionListener(listener);
    }
    
    public void setTitle(String title){
        tabLabel.setText(title);
    }

}

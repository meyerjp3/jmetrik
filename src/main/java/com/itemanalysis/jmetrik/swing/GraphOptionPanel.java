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

package com.itemanalysis.jmetrik.swing;

import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleEdge;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

public class GraphOptionPanel extends JPanel{

    private JmetrikPreferencesManager prefs = null;
    private Color[] color = null;
    private float[][] selectedLineStyles = null;

    public GraphOptionPanel(JmetrikPreferencesManager prefs){
        this.prefs = prefs;
        color = prefs.getColors();
        selectedLineStyles = prefs.getLineStyles();
        initComponents();
    }

    private void initComponents() {

        orientationButtonGroup = new ButtonGroup();
        colorPanel = new JPanel();
        color1Label = new JLabel();
        colorLabel2 = new JLabel();
        colorButton1 = new JButton();
        colorButton2 = new JButton();
        colorLabel3 = new JLabel();
        colorButton3 = new JButton();
        colorLabel4 = new JLabel();
        colorButton4 = new JButton();
        colorLabel5 = new JLabel();
        colorButton5 = new JButton();
        colorLabel6 = new JLabel();
        colorButton6 = new JButton();
        colorLabel7 = new JLabel();
        colorButton7 = new JButton();
        colorLabel8 = new JLabel();
        colorButton8 = new JButton();
        colorLabel9 = new JLabel();
        colorButton9 = new JButton();
        lineStylePanel = new JPanel();
        lineLabel1 = new JLabel();
        lineStyleComboBox1 = new JComboBox();
        lineLabel2 = new JLabel();
        lineStyleComboBox2 = new JComboBox();
        lineLabel3 = new JLabel();
        lineStyleComboBox3 = new JComboBox();
        lineLabel4 = new JLabel();
        lineStyleComboBox4 = new JComboBox();
        lineLabel5 = new JLabel();
        lineStyleComboBox5 = new JComboBox();
        lineLabel6 = new JLabel();
        lineStyleComboBox6 = new JComboBox();
        lineLabel7 = new JLabel();
        lineStyleComboBox7 = new JComboBox();
        lineLabel8 = new JLabel();
        lineStyleComboBox8 = new JComboBox();
        lineLabel9 = new JLabel();
        lineStyleComboBox9 = new JComboBox();
        lineWidthLabel = new JLabel();
        lineWidthTextField = new JTextField();
        displayPanel = new JPanel();
        legendPositionComboBox = new JComboBox();
        legendCheckbox = new JCheckBox();
        markersCheckbox = new JCheckBox();
        horizontalRadioButton = new JRadioButton();
        verticalRadioButton = new JRadioButton();
        sizePanel = new JPanel();
        widthLabel = new JLabel();
        widthTextField = new JTextField();
        heightLabel = new JLabel();
        heightTextField = new JTextField();
        resetButton = new JButton();

//        setBorder(BorderFactory.createTitledBorder(""));

        colorPanel.setBorder(BorderFactory.createTitledBorder("Color Sequence"));

        color1Label.setText("Color 1");

        colorLabel2.setText("Color 2");

        colorButton1.setText("Choose Color");
        colorButton1.setMaximumSize(new Dimension(116, 25));
        colorButton1.setMinimumSize(new Dimension(116, 25));
        colorButton1.setPreferredSize(new Dimension(116, 25));
        colorButton1.setBackground(color[0]);
        colorButton1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(GraphOptionPanel.this, "Color 1", color[0]);
                if(newColor!=null){
                    colorButton1.setBackground(newColor);
                    color[0] = newColor;
                }
            }
        });

        colorButton2.setText("Choose Color");
        colorButton2.setMaximumSize(new Dimension(116, 25));
        colorButton2.setMinimumSize(new Dimension(116, 25));
        colorButton2.setPreferredSize(new Dimension(116, 25));
        colorButton2.setBackground(color[1]);
        colorButton2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(GraphOptionPanel.this, "Color 2", color[1]);
                if(newColor!=null){
                    colorButton2.setBackground(newColor);
                    color[1] = newColor;
                }
            }
        });

        colorLabel3.setText("Color 3");

        colorButton3.setText("Choose Color");
        colorButton3.setMaximumSize(new Dimension(116, 25));
        colorButton3.setMinimumSize(new Dimension(116, 25));
        colorButton3.setPreferredSize(new Dimension(116, 25));
        colorButton3.setBackground(color[2]);
        colorButton3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(GraphOptionPanel.this, "Color 3", color[2]);
                if (newColor != null) {
                    colorButton3.setBackground(newColor);
                    color[2] = newColor;
                }
            }
        });

        colorLabel4.setText("Color 4");

        colorButton4.setText("Choose Color");
        colorButton4.setMaximumSize(new Dimension(116, 25));
        colorButton4.setMinimumSize(new Dimension(116, 25));
        colorButton4.setPreferredSize(new Dimension(116, 25));
        colorButton4.setBackground(color[3]);
        colorButton4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(GraphOptionPanel.this, "Color 4", color[3]);
                if (newColor != null) {
                    colorButton4.setBackground(newColor);
                    color[3] = newColor;
                }
            }
        });

        colorLabel5.setText("Color 5");

        colorButton5.setText("Choose Color");
        colorButton5.setMaximumSize(new Dimension(116, 25));
        colorButton5.setMinimumSize(new Dimension(116, 25));
        colorButton5.setPreferredSize(new Dimension(116, 25));
        colorButton5.setBackground(color[4]);
        colorButton5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(GraphOptionPanel.this, "Color 5", color[4]);
                if (newColor != null) {
                    colorButton5.setBackground(newColor);
                    color[4] = newColor;
                }
            }
        });

        colorLabel6.setText("Color 6");

        colorButton6.setText("Choose Color");
        colorButton6.setMaximumSize(new Dimension(116, 25));
        colorButton6.setMinimumSize(new Dimension(116, 25));
        colorButton6.setPreferredSize(new Dimension(116, 25));
        colorButton6.setBackground(color[5]);
        colorButton6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(GraphOptionPanel.this, "Color 6", color[5]);
                if (newColor != null) {
                    colorButton6.setBackground(newColor);
                    color[5] = newColor;
                }
            }
        });

        colorLabel7.setText("Color 7");

        colorButton7.setText("Choose Color");
        colorButton7.setMaximumSize(new Dimension(116, 25));
        colorButton7.setMinimumSize(new Dimension(116, 25));
        colorButton7.setPreferredSize(new Dimension(116, 25));
        colorButton7.setBackground(color[6]);
        colorButton7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(GraphOptionPanel.this, "Color 7", color[6]);
                if (newColor != null) {
                    colorButton7.setBackground(newColor);
                    color[6] = newColor;
                }
            }
        });

        colorLabel8.setText("Color 8");

        colorButton8.setText("Choose Color");
        colorButton8.setMaximumSize(new Dimension(116, 25));
        colorButton8.setMinimumSize(new Dimension(116, 25));
        colorButton8.setPreferredSize(new Dimension(116, 25));
        colorButton8.setBackground(color[7]);
        colorButton8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(GraphOptionPanel.this, "Color 8", color[7]);
                if(newColor!=null){
                    colorButton8.setBackground(newColor);
                    color[7] = newColor;
                }
            }
        });

        colorLabel9.setText("Color 9");

        colorButton9.setText("Choose Color");
        colorButton9.setMaximumSize(new Dimension(116, 25));
        colorButton9.setMinimumSize(new Dimension(116, 25));
        colorButton9.setPreferredSize(new Dimension(116, 25));
        colorButton9.setBackground(color[8]);
        colorButton9.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(GraphOptionPanel.this, "Color 9", color[8]);
                if(newColor!=null){
                    colorButton9.setBackground(newColor);
                    color[8] = newColor;
                }
            }
        });

        GroupLayout colorPanelLayout = new GroupLayout(colorPanel);
        colorPanel.setLayout(colorPanelLayout);
        colorPanelLayout.setHorizontalGroup(
            colorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(colorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(colorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(color1Label)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(colorButton1, GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(colorLabel2)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(colorButton2, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(colorLabel3)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(colorButton3, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(colorLabel4)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(colorButton4, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(colorLabel5)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(colorButton5, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(colorLabel6)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(colorButton6, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(colorLabel7)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(colorButton7, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(colorLabel8)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(colorButton8, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(colorPanelLayout.createSequentialGroup()
                        .addComponent(colorLabel9)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(colorButton9, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        colorPanelLayout.setVerticalGroup(
            colorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(colorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(colorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(color1Label)
                    .addComponent(colorButton1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel2)
                    .addComponent(colorButton2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel3)
                    .addComponent(colorButton3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel4)
                    .addComponent(colorButton4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel5)
                    .addComponent(colorButton5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel6)
                    .addComponent(colorButton6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel7)
                    .addComponent(colorButton7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel8)
                    .addComponent(colorButton8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(colorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(colorLabel9)
                    .addComponent(colorButton9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lineStylePanel.setBorder(BorderFactory.createTitledBorder("Line Styles"));

        lineLabel1.setText("Line 1");

        lineStyleComboBox1.setModel(new DefaultComboBoxModel(ChartStyle.LINE_STYLE_NAME));
        lineStyleComboBox1.setRenderer(new LineStyleComboBoxRenderer());
        lineStyleComboBox1.setSelectedItem(ChartStyle.floatStyleToString(selectedLineStyles[0]));
        lineStyleComboBox1.setMinimumSize(new Dimension(150, 25));
        lineStyleComboBox1.setPreferredSize(new Dimension(150, 25));

        lineLabel2.setText("Line 2");

        lineStyleComboBox2.setModel(new DefaultComboBoxModel(ChartStyle.LINE_STYLE_NAME));
        lineStyleComboBox2.setRenderer(new LineStyleComboBoxRenderer());
        lineStyleComboBox2.setSelectedItem(ChartStyle.floatStyleToString(selectedLineStyles[1]));
        lineStyleComboBox2.setMinimumSize(new Dimension(150, 25));
        lineStyleComboBox2.setPreferredSize(new Dimension(150, 25));

        lineLabel3.setText("Line 3");

        lineStyleComboBox3.setModel(new DefaultComboBoxModel(ChartStyle.LINE_STYLE_NAME));
        lineStyleComboBox3.setRenderer(new LineStyleComboBoxRenderer());
        lineStyleComboBox3.setSelectedItem(ChartStyle.floatStyleToString(selectedLineStyles[2]));
        lineStyleComboBox3.setMinimumSize(new Dimension(150, 25));
        lineStyleComboBox3.setPreferredSize(new Dimension(150, 25));

        lineLabel4.setText("Line 4");

        lineStyleComboBox4.setModel(new DefaultComboBoxModel(ChartStyle.LINE_STYLE_NAME));
        lineStyleComboBox4.setRenderer(new LineStyleComboBoxRenderer());
        lineStyleComboBox4.setSelectedItem(ChartStyle.floatStyleToString(selectedLineStyles[3]));
        lineStyleComboBox4.setMinimumSize(new Dimension(150, 25));
        lineStyleComboBox4.setPreferredSize(new Dimension(150, 25));

        lineLabel5.setText("Line 5");

        lineStyleComboBox5.setModel(new DefaultComboBoxModel(ChartStyle.LINE_STYLE_NAME));
        lineStyleComboBox5.setRenderer(new LineStyleComboBoxRenderer());
        lineStyleComboBox5.setSelectedItem(ChartStyle.floatStyleToString(selectedLineStyles[4]));
        lineStyleComboBox5.setMinimumSize(new Dimension(150, 25));
        lineStyleComboBox5.setPreferredSize(new Dimension(150, 25));

        lineLabel6.setText("Line 6");

        lineStyleComboBox6.setModel(new DefaultComboBoxModel(ChartStyle.LINE_STYLE_NAME));
        lineStyleComboBox6.setRenderer(new LineStyleComboBoxRenderer());
        lineStyleComboBox6.setSelectedItem(ChartStyle.floatStyleToString(selectedLineStyles[5]));
        lineStyleComboBox6.setMinimumSize(new Dimension(150, 25));
        lineStyleComboBox6.setPreferredSize(new Dimension(150, 25));

        lineLabel7.setText("Line 7");

        lineStyleComboBox7.setModel(new DefaultComboBoxModel(ChartStyle.LINE_STYLE_NAME));
        lineStyleComboBox7.setRenderer(new LineStyleComboBoxRenderer());
        lineStyleComboBox7.setSelectedItem(ChartStyle.floatStyleToString(selectedLineStyles[6]));
        lineStyleComboBox7.setMinimumSize(new Dimension(150, 25));
        lineStyleComboBox7.setPreferredSize(new Dimension(150, 25));

        lineLabel8.setText("Line 8");

        lineStyleComboBox8.setModel(new DefaultComboBoxModel(ChartStyle.LINE_STYLE_NAME));
        lineStyleComboBox8.setRenderer(new LineStyleComboBoxRenderer());
        lineStyleComboBox8.setSelectedItem(ChartStyle.floatStyleToString(selectedLineStyles[7]));
        lineStyleComboBox8.setMinimumSize(new Dimension(150, 25));
        lineStyleComboBox8.setPreferredSize(new Dimension(150, 25));

        lineLabel9.setText("Line 9");

        lineStyleComboBox9.setModel(new DefaultComboBoxModel(ChartStyle.LINE_STYLE_NAME));
        lineStyleComboBox9.setRenderer(new LineStyleComboBoxRenderer());
        lineStyleComboBox9.setSelectedItem(ChartStyle.floatStyleToString(selectedLineStyles[8]));
        lineStyleComboBox9.setMinimumSize(new Dimension(150, 25));
        lineStyleComboBox9.setPreferredSize(new Dimension(150, 25));

        GroupLayout lineStylePanelLayout = new GroupLayout(lineStylePanel);
        lineStylePanel.setLayout(lineStylePanelLayout);
        lineStylePanelLayout.setHorizontalGroup(
            lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(lineStylePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(lineStylePanelLayout.createSequentialGroup()
                        .addComponent(lineLabel1)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lineStyleComboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(lineStylePanelLayout.createSequentialGroup()
                        .addComponent(lineLabel2)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lineStyleComboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(lineStylePanelLayout.createSequentialGroup()
                        .addComponent(lineLabel3)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lineStyleComboBox3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(lineStylePanelLayout.createSequentialGroup()
                        .addComponent(lineLabel4)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lineStyleComboBox4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(lineStylePanelLayout.createSequentialGroup()
                        .addComponent(lineLabel5)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lineStyleComboBox5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(lineStylePanelLayout.createSequentialGroup()
                        .addComponent(lineLabel6)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lineStyleComboBox6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(lineStylePanelLayout.createSequentialGroup()
                        .addComponent(lineLabel7)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lineStyleComboBox7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(lineStylePanelLayout.createSequentialGroup()
                        .addComponent(lineLabel8)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lineStyleComboBox8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                    .addGroup(lineStylePanelLayout.createSequentialGroup()
                        .addComponent(lineLabel9)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lineStyleComboBox9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        lineStylePanelLayout.setVerticalGroup(
            lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(lineStylePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lineLabel1)
                    .addComponent(lineStyleComboBox1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lineLabel2)
                    .addComponent(lineStyleComboBox2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lineLabel3)
                    .addComponent(lineStyleComboBox3, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lineLabel4)
                    .addComponent(lineStyleComboBox4, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lineLabel5)
                    .addComponent(lineStyleComboBox5, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lineLabel6)
                    .addComponent(lineStyleComboBox6, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lineLabel7)
                    .addComponent(lineStyleComboBox7, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lineLabel8)
                    .addComponent(lineStyleComboBox8, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lineStylePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lineLabel9)
                    .addComponent(lineStyleComboBox9, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        displayPanel.setBorder(BorderFactory.createTitledBorder("Display Options"));

        legendPositionComboBox.setModel(new DefaultComboBoxModel(new String[] { "Bottom", "Left", "Top", "Right" }));
        legendPositionComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String s = legendPositionComboBox.getSelectedItem().toString();
                if("Bottom".equals(s)){
                    prefs.setLegendPosition(RectangleEdge.BOTTOM);
                }else if("Left".equals(s)){
                    prefs.setLegendPosition(RectangleEdge.LEFT);
                }else if("Top".equals(s)){
                    prefs.setLegendPosition(RectangleEdge.TOP);
                }else{
                    prefs.setLegendPosition(RectangleEdge.RIGHT);
                }


            }
        });

        legendCheckbox.setSelected(true);
        legendCheckbox.setText("Legend");
        legendCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(legendCheckbox.isSelected()){
                    prefs.setShowLegend(true);
                }else{
                    prefs.setShowLegend(false);
                }
            }
        });

        markersCheckbox.setText("Point markers");
        markersCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(markersCheckbox.isSelected()){
                    prefs.setShowMarkers(true);
                }else{
                    prefs.setShowMarkers(false);
                }
            }
        });

        orientationButtonGroup.add(horizontalRadioButton);
        horizontalRadioButton.setText("Horizontal orientation");
        horizontalRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(horizontalRadioButton.isSelected()){
                    prefs.setChartOrientation(PlotOrientation.HORIZONTAL);
                }
            }
        });

        orientationButtonGroup.add(verticalRadioButton);
        verticalRadioButton.setSelected(true);
        verticalRadioButton.setText("Vertical orientation");
        verticalRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(verticalRadioButton.isSelected()){
                    prefs.setChartOrientation(PlotOrientation.VERTICAL);
                }
            }
        });

        GroupLayout displayPanelLayout = new GroupLayout(displayPanel);
        displayPanel.setLayout(displayPanelLayout);
        displayPanelLayout.setHorizontalGroup(
            displayPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(displayPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(displayPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(displayPanelLayout.createSequentialGroup()
                        .addGroup(displayPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(markersCheckbox)
                            .addGroup(displayPanelLayout.createSequentialGroup()
                                .addComponent(legendCheckbox)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(legendPositionComboBox, 0, 89, Short.MAX_VALUE)))
                        .addGap(26, 26, 26))
                    .addGroup(displayPanelLayout.createSequentialGroup()
                        .addComponent(horizontalRadioButton)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(displayPanelLayout.createSequentialGroup()
                        .addComponent(verticalRadioButton)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        displayPanelLayout.setVerticalGroup(
            displayPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(displayPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(displayPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(legendCheckbox)
                    .addComponent(legendPositionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(markersCheckbox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(horizontalRadioButton)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(verticalRadioButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        sizePanel.setBorder(BorderFactory.createTitledBorder("Chart Size"));

        widthLabel.setText("Width");

        widthTextField.setText("450");
        widthTextField.setMaximumSize(new Dimension(100, 25));
        widthTextField.setMinimumSize(new Dimension(100, 25));
        widthTextField.setPreferredSize(new Dimension(100, 25));
        widthTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //do nothing
            }

            @Override
            public void focusLost(FocusEvent e) {
                String s = widthTextField.getText();
                try{
                    int w = Integer.parseInt(s);
                    prefs.setChartWidth(w);
                }catch(NumberFormatException ex){
                    widthTextField.setText("450");
                }
            }
        });

        heightLabel.setText("Height");

        heightTextField.setText("400");
        heightTextField.setMaximumSize(new Dimension(100, 25));
        heightTextField.setMinimumSize(new Dimension(100, 25));
        heightTextField.setPreferredSize(new Dimension(100, 25));
        heightTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //do nothing
            }

            @Override
            public void focusLost(FocusEvent e) {
                try{
                    String s = heightTextField.getText();
                    int h = Integer.parseInt(s);
                    prefs.setChartHeight(h);
                }catch(NumberFormatException ex){
                    heightTextField.setText("400");
                }
            }
        });

        lineWidthLabel.setText("Line width");

        lineWidthTextField.setText("1.0");
        lineWidthTextField.setMaximumSize(new Dimension(100, 25));
        lineWidthTextField.setMinimumSize(new Dimension(100, 25));
        lineWidthTextField.setPreferredSize(new Dimension(100, 25));
        lineWidthTextField.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                //do nothing
            }

            @Override
            public void focusLost(FocusEvent e) {
                try{

                    String s = lineWidthTextField.getText();
                    float lw = Float.parseFloat(s);
                    prefs.setChartLineWidth(lw);
                }catch(NumberFormatException ex){
                    lineWidthTextField.setText("1.0");
                }
            }
        });

        GroupLayout sizePanelLayout = new GroupLayout(sizePanel);
        sizePanel.setLayout(sizePanelLayout);
        sizePanelLayout.setHorizontalGroup(
            sizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(sizePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(widthLabel)
                    .addComponent(heightLabel)
                    .addComponent(lineWidthLabel))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sizePanelLayout.createParallelGroup(GroupLayout.Alignment.CENTER)
                    .addComponent(widthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(heightTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lineWidthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        sizePanelLayout.setVerticalGroup(
            sizePanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(sizePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sizePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(widthLabel)
                    .addComponent(widthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(sizePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(heightLabel)
                    .addComponent(heightTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(sizePanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lineWidthLabel)
                    .addComponent(lineWidthTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        resetButton.setText("Reset to Default Chart Options");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetPanel();
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                            .addComponent(displayPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(colorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                            .addComponent(lineStylePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(sizePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addComponent(resetButton))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(colorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(lineStylePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                    .addComponent(sizePanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(displayPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetButton)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>

    private void resetPanel(){
        colorButton1.setBackground(ChartStyle.CHART_COLOR[0]);
        colorButton2.setBackground(ChartStyle.CHART_COLOR[1]);
        colorButton3.setBackground(ChartStyle.CHART_COLOR[2]);
        colorButton4.setBackground(ChartStyle.CHART_COLOR[3]);
        colorButton5.setBackground(ChartStyle.CHART_COLOR[4]);
        colorButton6.setBackground(ChartStyle.CHART_COLOR[5]);
        colorButton7.setBackground(ChartStyle.CHART_COLOR[6]);
        colorButton8.setBackground(ChartStyle.CHART_COLOR[7]);
        colorButton9.setBackground(ChartStyle.CHART_COLOR[8]);

        color[0] = ChartStyle.CHART_COLOR[0];
        color[1] = ChartStyle.CHART_COLOR[1];
        color[2] = ChartStyle.CHART_COLOR[2];
        color[3] = ChartStyle.CHART_COLOR[3];
        color[4] = ChartStyle.CHART_COLOR[4];
        color[5] = ChartStyle.CHART_COLOR[5];
        color[6] = ChartStyle.CHART_COLOR[6];
        color[7] = ChartStyle.CHART_COLOR[7];
        color[8] = ChartStyle.CHART_COLOR[8];

        lineStyleComboBox1.setSelectedItem(ChartStyle.LINE_STYLE_NAME[0]);
        lineStyleComboBox2.setSelectedItem(ChartStyle.LINE_STYLE_NAME[1]);
        lineStyleComboBox3.setSelectedItem(ChartStyle.LINE_STYLE_NAME[2]);
        lineStyleComboBox4.setSelectedItem(ChartStyle.LINE_STYLE_NAME[3]);
        lineStyleComboBox5.setSelectedItem(ChartStyle.LINE_STYLE_NAME[4]);
        lineStyleComboBox6.setSelectedItem(ChartStyle.LINE_STYLE_NAME[5]);
        lineStyleComboBox7.setSelectedItem(ChartStyle.LINE_STYLE_NAME[6]);
        lineStyleComboBox8.setSelectedItem(ChartStyle.LINE_STYLE_NAME[7]);
        lineStyleComboBox9.setSelectedItem(ChartStyle.LINE_STYLE_NAME[8]);

        legendPositionComboBox.setSelectedItem("Bottom");
        legendCheckbox.setSelected(true);
        markersCheckbox.setSelected(false);
        verticalRadioButton.setSelected(true);
        widthTextField.setText("450");
        heightTextField.setText("400");
        lineWidthTextField.setText("1.0");

        prefs.setColors(getSelectedColors());
        prefs.setLineStyles(getSelectedLineStyles());
        prefs.setLegendPosition(RectangleEdge.BOTTOM);
        prefs.setShowLegend(true);
        prefs.setShowMarkers(false);
        prefs.setChartOrientation(PlotOrientation.VERTICAL);
        prefs.setChartWidth(450);
        prefs.setChartHeight(400);
        prefs.setChartLineWidth(1.0f);
    }

    public int getChartWidth(){
        int w = Integer.parseInt(widthTextField.getText());
        return w;
    }

    public int getChartHeight(){
        int h = Integer.parseInt(heightTextField.getText());
        return h;
    }

    public int getLineWidth(){
        int lw = Integer.parseInt(lineWidthTextField.getText());
        return lw;
    }

    public void setSelectedShowLegend(boolean selected){
        legendCheckbox.setSelected(selected);
    }

    public void setSelectedLegendPosition(RectangleEdge legendPosition){
        if(legendPosition.equals(RectangleEdge.BOTTOM)){
            legendPositionComboBox.setSelectedItem("Bottom");
        }else if(legendPosition.equals(RectangleEdge.LEFT)){
            legendPositionComboBox.setSelectedItem("Left");
        }else if(legendPosition.equals(RectangleEdge.TOP)){
            legendPositionComboBox.setSelectedItem("Top");
        }else{
            legendPositionComboBox.setSelectedItem("Right");
        }
    }

    public void setSelectedShowMarkers(boolean selected){
        markersCheckbox.setSelected(selected);
    }

    public void setSelectedOrientation(PlotOrientation orientation){
        if(orientation.equals(PlotOrientation.VERTICAL)){
            verticalRadioButton.setSelected(true);
        }else{
            horizontalRadioButton.setSelected(true);
        }
    }

    public void setChartWidth(int width){
        widthTextField.setText(Integer.toString(width));
    }

    public void setChartHeight(int height){
        heightTextField.setText(Integer.toString(height));
    }

    public void setChartLineWidth(float lineWidth){
        lineWidthTextField.setText(Float.valueOf(lineWidth).toString());
    }

    public Color[] getSelectedColors(){
        return color;
    }

    public String[] getSelectedLineStyles(){
        String[] s = new String[9];
        s[0] = lineStyleComboBox1.getSelectedItem().toString();
        s[1] = lineStyleComboBox2.getSelectedItem().toString();
        s[2] = lineStyleComboBox3.getSelectedItem().toString();
        s[3] = lineStyleComboBox4.getSelectedItem().toString();
        s[4] = lineStyleComboBox5.getSelectedItem().toString();
        s[5] = lineStyleComboBox6.getSelectedItem().toString();
        s[6] = lineStyleComboBox7.getSelectedItem().toString();
        s[7] = lineStyleComboBox8.getSelectedItem().toString();
        s[8] = lineStyleComboBox9.getSelectedItem().toString();

        return s;
    }

    // Variables declaration - do not modify
    private JLabel color1Label;
    private JButton colorButton1;
    private JButton colorButton2;
    private JButton colorButton3;
    private JButton colorButton4;
    private JButton colorButton5;
    private JButton colorButton6;
    private JButton colorButton7;
    private JButton colorButton8;
    private JButton colorButton9;
    private JLabel colorLabel2;
    private JLabel colorLabel3;
    private JLabel colorLabel4;
    private JLabel colorLabel5;
    private JLabel colorLabel6;
    private JLabel colorLabel7;
    private JLabel colorLabel8;
    private JLabel colorLabel9;
    private JPanel colorPanel;
    private JPanel displayPanel;
    private JLabel heightLabel;
    private JTextField heightTextField;
    private JRadioButton horizontalRadioButton;
    private JCheckBox legendCheckbox;
    private JComboBox legendPositionComboBox;
    private JLabel lineLabel1;
    private JLabel lineLabel2;
    private JLabel lineLabel3;
    private JLabel lineLabel4;
    private JLabel lineLabel5;
    private JLabel lineLabel6;
    private JLabel lineLabel7;
    private JLabel lineLabel8;
    private JLabel lineLabel9;
    private JLabel lineWidthLabel;
    private JTextField lineWidthTextField;
    private JComboBox lineStyleComboBox1;
    private JComboBox lineStyleComboBox2;
    private JComboBox lineStyleComboBox3;
    private JComboBox lineStyleComboBox4;
    private JComboBox lineStyleComboBox5;
    private JComboBox lineStyleComboBox6;
    private JComboBox lineStyleComboBox7;
    private JComboBox lineStyleComboBox8;
    private JComboBox lineStyleComboBox9;
    private JPanel lineStylePanel;
    private JCheckBox markersCheckbox;
    private ButtonGroup orientationButtonGroup;
    private JButton resetButton;
    private JPanel sizePanel;
    private JRadioButton verticalRadioButton;
    private JLabel widthLabel;
    private JTextField widthTextField;
    // End of variables declaration

}

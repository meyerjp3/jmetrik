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

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class LineStyleComboBoxRenderer extends JLabel implements ListCellRenderer {

    private ImageIcon[] styleImageIcon = null;

    public LineStyleComboBoxRenderer(){
        setOpaque(true);
        setHorizontalAlignment(CENTER);
        setVerticalAlignment(CENTER);

        createImageIcon();
    }

    private void createImageIcon(){
        styleImageIcon = new ImageIcon[9];

        BufferedImage lineStyleImage = new BufferedImage(100, 20, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = lineStyleImage.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, 100, 20);
        g2.setColor(Color.BLACK);
        BasicStroke stroke1 = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, ChartStyle.LINE_STYLE[0], 0.0f);
        g2.setStroke(stroke1);
        g2.drawLine(10, 10, 90, 10);
        styleImageIcon[0] = new ImageIcon(lineStyleImage);

        lineStyleImage = new BufferedImage(100, 20, BufferedImage.TYPE_INT_RGB);
        g2 = lineStyleImage.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, 100, 20);
        g2.setColor(Color.BLACK);
        BasicStroke stroke2 = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, ChartStyle.LINE_STYLE[1], 0.0f);
        g2.setStroke(stroke2);
        g2.drawLine(10, 10, 90, 10);
        styleImageIcon[1] = new ImageIcon(lineStyleImage);

        lineStyleImage = new BufferedImage(100, 20, BufferedImage.TYPE_INT_RGB);
        g2 = lineStyleImage.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, 100, 20);
        g2.setColor(Color.BLACK);
        BasicStroke stroke3 = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, ChartStyle.LINE_STYLE[2], 0.0f);
        g2.setStroke(stroke3);
        g2.drawLine(10, 10, 90, 10);
        styleImageIcon[2] = new ImageIcon(lineStyleImage);

        lineStyleImage = new BufferedImage(100, 20, BufferedImage.TYPE_INT_RGB);
        g2 = lineStyleImage.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, 100, 20);
        g2.setColor(Color.BLACK);
        BasicStroke stroke4 = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, ChartStyle.LINE_STYLE[3], 0.0f);
        g2.setStroke(stroke4);
        g2.drawLine(10, 10, 90, 10);
        styleImageIcon[3] = new ImageIcon(lineStyleImage);

        lineStyleImage = new BufferedImage(100, 20, BufferedImage.TYPE_INT_RGB);
        g2 = lineStyleImage.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, 100, 20);
        g2.setColor(Color.BLACK);
        BasicStroke stroke5 = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, ChartStyle.LINE_STYLE[4], 0.0f);
        g2.setStroke(stroke5);
        g2.drawLine(10, 10, 90, 10);
        styleImageIcon[4] = new ImageIcon(lineStyleImage);

        lineStyleImage = new BufferedImage(100, 20, BufferedImage.TYPE_INT_RGB);
        g2 = lineStyleImage.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, 100, 20);
        g2.setColor(Color.BLACK);
        BasicStroke stroke6 = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, ChartStyle.LINE_STYLE[5], 0.0f);
        g2.setStroke(stroke6);
        g2.drawLine(10, 10, 90, 10);
        styleImageIcon[5] = new ImageIcon(lineStyleImage);

        lineStyleImage = new BufferedImage(100, 20, BufferedImage.TYPE_INT_RGB);
        g2 = lineStyleImage.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, 100, 20);
        g2.setColor(Color.BLACK);
        BasicStroke stroke7 = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, ChartStyle.LINE_STYLE[6], 0.0f);
        g2.setStroke(stroke7);
        g2.drawLine(10, 10, 90, 10);
        styleImageIcon[6] = new ImageIcon(lineStyleImage);

        lineStyleImage = new BufferedImage(100, 20, BufferedImage.TYPE_INT_RGB);
        g2 = lineStyleImage.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, 100, 20);
        g2.setColor(Color.BLACK);
        BasicStroke stroke8 = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, ChartStyle.LINE_STYLE[7], 0.0f);
        g2.setStroke(stroke8);
        g2.drawLine(10, 10, 90, 10);
        styleImageIcon[7] = new ImageIcon(lineStyleImage);

        lineStyleImage = new BufferedImage(100, 20, BufferedImage.TYPE_INT_RGB);
        g2 = lineStyleImage.createGraphics();
        g2.setBackground(Color.WHITE);
        g2.fillRect(0, 0, 100, 20);
        g2.setColor(Color.BLACK);
        BasicStroke stroke9 = new BasicStroke(2.0f, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, ChartStyle.LINE_STYLE[8], 0.0f);
        g2.setStroke(stroke9);
        g2.drawLine(10, 10, 90, 10);
        styleImageIcon[8] = new ImageIcon(lineStyleImage);
    }

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        String selectedLine = value.toString();

//        setBackground(Color.WHITE);
//        setForeground(Color.WHITE);

        if(selectedLine.equals(ChartStyle.LINE_STYLE_NAME[0])){
            setIcon(styleImageIcon[0]);
        }else if(selectedLine.equals(ChartStyle.LINE_STYLE_NAME[1])){
            setIcon(styleImageIcon[1]);
        }else if(selectedLine.equals(ChartStyle.LINE_STYLE_NAME[2])){
            setIcon(styleImageIcon[2]);
        }else if(selectedLine.equals(ChartStyle.LINE_STYLE_NAME[3])){
            setIcon(styleImageIcon[3]);
        }else if(selectedLine.equals(ChartStyle.LINE_STYLE_NAME[4])){
            setIcon(styleImageIcon[4]);
        }else if(selectedLine.equals(ChartStyle.LINE_STYLE_NAME[5])){
            setIcon(styleImageIcon[5]);
        }else if(selectedLine.equals(ChartStyle.LINE_STYLE_NAME[6])){
            setIcon(styleImageIcon[6]);
        }else if(selectedLine.equals(ChartStyle.LINE_STYLE_NAME[7])){
            setIcon(styleImageIcon[7]);
        }else if(selectedLine.equals(ChartStyle.LINE_STYLE_NAME[8])){
            setIcon(styleImageIcon[8]);
        }

//        if(selectedIndex!=-1){
//            setIcon(styleImageIcon[selectedIndex]);
////            setText(styleName[selectedIndex]);
//        }

        return this;
    }

}

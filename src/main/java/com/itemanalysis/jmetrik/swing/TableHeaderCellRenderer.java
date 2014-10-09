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
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class TableHeaderCellRenderer extends DefaultTableCellRenderer{

    protected float hsb[];

    public TableHeaderCellRenderer(){
        setHorizontalAlignment(JLabel.CENTER);
        setBorder(UIManager.getBorder("TableHeader.cellBorder"));
        Color titleColor = UIManager.getColor("nimbusBase");
        hsb = Color.RGBtoHSB(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), null);
    }

    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        super.getTableCellRendererComponent(table, value,isSelected, hasFocus, row, column);

        setText(value.toString());
        setToolTipText((String)value);//TODO make this the variable label?

        setBackground(Color.getHSBColor(hsb[0] - .013f, .15f, .85f));
        setForeground(Color.getHSBColor(hsb[0], .54f, .40f));
        setBorder(new SimpleBorder());
        return this;
    }

    class SimpleBorder implements Border {

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Insets insets = getBorderInsets(c);

            //draw line on right side
//            g.setColor(Color.getHSBColor(hsb[0], .54f, .40f));
            g.setColor(Color.gray);
            g.drawRect(width-insets.right, 0, insets.right, height-insets.bottom);

            
//            g.drawRect(0, 0, width-insets.right, insets.top);
//            g.drawRect(0, insets.top, insets.left, height-insets.top);


            //draw line on bottom
            g.setColor(Color.lightGray);
            g.drawRect(insets.left, height-insets.bottom, width-insets.left, insets.bottom);

        }

        public Insets getBorderInsets(Component c) {
            return new Insets(1,1,1,1);
        }

        public boolean isBorderOpaque() {
            return true;
        }

    }

}

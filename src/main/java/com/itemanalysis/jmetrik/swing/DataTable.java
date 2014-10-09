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
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.EventObject;

public class DataTable extends JTable {

    private Color[] rowColors = null;

    private Color SELECTED_COLOR = null;

    private Color BASE_COLOR = null;

    private Color ALT_COLOR = null;

//    protected float hsb[];

    public DataTable(){
        Color titleColor = UIManager.getColor("nimbusBase");
        SELECTED_COLOR = new Color(184, 204, 217);
        BASE_COLOR = new Color(220,231,243, 50);
        ALT_COLOR = new Color(220,231,243,115);
//        hsb = Color.RGBtoHSB(titleColor.getRed(), titleColor.getGreen(), titleColor.getBlue(), null);
    }

    @Override
	public void changeSelection(final int row, final int column, boolean toggle, boolean extend){
        super.changeSelection(row, column, toggle, extend);
        if (editCellAt(row, column)){
            getEditorComponent().requestFocusInWindow();
        }
    }

    //  Select the text when the cell starts editing
    @Override
    public boolean editCellAt(int row, int column, EventObject e){
        boolean result = super.editCellAt(row, column, e);
        final Component editor = getEditorComponent();
        if (editor != null && editor instanceof JTextComponent){
            SwingUtilities.invokeLater(new Runnable(){
                public void run(){
                    ((JTextComponent)editor).selectAll();
                }
            });
        }

        return result;
    }

    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int rowIndex, int vColIndex) {
        Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
        if(isCellSelected(rowIndex, vColIndex)) {
//            c.setBackground(Color.getHSBColor(hsb[0] - .013f, .15f, .85f));
            c.setBackground(SELECTED_COLOR);
        }else{
            if (rowIndex % 2 == 0) {
                c.setBackground(BASE_COLOR);
            } else {
//                c.setBackground(Color.getHSBColor(hsb[0] - .013f, .005f, .85f));
                c.setBackground(ALT_COLOR);
            }
        }
        return c;
    }


}

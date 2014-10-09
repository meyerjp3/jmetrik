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

import org.jfree.chart.ChartColor;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;

import java.awt.*;

public class JmetrikXYLineAndShapeRenderer extends XYLineAndShapeRenderer {

    public JmetrikXYLineAndShapeRenderer(){
        super();
    }

    private float[] getLineStyle(int index){
        int n = ChartStyle.LINE_STYLE.length;
        int newIndex = index;
        while(index<0){
            newIndex += n;
        }
        newIndex = newIndex % n;
        return ChartStyle.LINE_STYLE[newIndex];
    }

    @Override
    public Stroke getItemStroke(int row, int col){
        Stroke stroke = new BasicStroke(1.0f,
                    BasicStroke.CAP_SQUARE,
                    BasicStroke.JOIN_MITER,
                    10.0f,
                    getLineStyle(row),
                    0.0f);
        return stroke;
    }

    public Paint getItemPaint(int row, int col){
        Paint[] altColors = {
                Color.BLACK,
                ChartColor.RED,
                ChartColor.DARK_BLUE,
                ChartColor.DARK_GREEN,
                ChartColor.DARK_MAGENTA,
                ChartColor.DARK_CYAN,
                ChartColor.LIGHT_RED,
                ChartColor.LIGHT_BLUE,
                ChartColor.LIGHT_GREEN,
                ChartColor.LIGHT_MAGENTA,
                ChartColor.LIGHT_CYAN,
                ChartColor.VERY_DARK_RED,
                ChartColor.VERY_DARK_BLUE,
                ChartColor.VERY_DARK_GREEN,
                ChartColor.VERY_DARK_YELLOW,
                ChartColor.VERY_DARK_MAGENTA,
                ChartColor.VERY_DARK_CYAN,
                ChartColor.VERY_LIGHT_RED,
                ChartColor.VERY_LIGHT_BLUE,
                ChartColor.VERY_LIGHT_GREEN,
                ChartColor.VERY_LIGHT_MAGENTA,
                ChartColor.VERY_LIGHT_CYAN
        };

        return altColors[row];
    }

}

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

import org.jfree.util.PaintUtilities;

import java.awt.*;
import java.util.Arrays;

public class ChartStyle {

    public static final String[] LINE_STYLE_NAME = new String[] {
            "solid",
            "dashed",
            "dot-dash",
            "two-dot-dash",
            "three-dot-dash",
            "long-dash",
            "dot-long-dash",
            "two-dot-long-dash",
            "three-dot-long-dash"};

    public static final float[][] LINE_STYLE = {
            {1.0f},
            {10.0f, 5.0f},
            {2.0f, 5.0f, 10.0f, 5.0f},
            {2.0f, 5.0f, 2.0f, 5.0f, 10.0f, 5.0f},
            {2.0f, 5.0f, 2.0f, 5.0f, 2.0f, 5.0f, 10.0f, 5.0f},
            {20.0f, 5.0f},
            {5.0f, 5.0f, 20.0f, 5.0f},
            {5.0f, 5.0f, 5.0f, 5.0f, 20.0f, 5.0f},
            {5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 5.0f, 20.0f, 5.0f}
    };

    public static final String[] CHART_COLOR_NAME = {
            "darkGray", //java.awt.Color[r=64,g=64,b=64]
            "red",      //java.awt.Color[r=255,g=0,b=0]
            "#0000c0",  //java.awt.Color[r=0,g=0,b=192]
            "#00c000",  //java.awt.Color[r=0,g=192,b=0]
            "#c000c0",  //java.awt.Color[r=192,g=0,b=192]
            "#00c0c0",  //java.awt.Color[r=0,g=192,b=192]
            "#ff4040",  //java.awt.Color[r=255,g=64,b=64]
            "#4040ff",  //java.awt.Color[r=64,g=64,b=255]
            "#40ff40"   //java.awt.Color[r=64,g=255,b=64]
    };

    public static final Color[] CHART_COLOR = {
            PaintUtilities.stringToColor(CHART_COLOR_NAME[0]),
            PaintUtilities.stringToColor(CHART_COLOR_NAME[1]),
            PaintUtilities.stringToColor(CHART_COLOR_NAME[2]),
            PaintUtilities.stringToColor(CHART_COLOR_NAME[3]),
            PaintUtilities.stringToColor(CHART_COLOR_NAME[4]),
            PaintUtilities.stringToColor(CHART_COLOR_NAME[5]),
            PaintUtilities.stringToColor(CHART_COLOR_NAME[6]),
            PaintUtilities.stringToColor(CHART_COLOR_NAME[7]),
            PaintUtilities.stringToColor(CHART_COLOR_NAME[8]),
    };

    public static final String floatStyleToString(float[] style){
        if(Arrays.equals(style, LINE_STYLE[0])){
            return LINE_STYLE_NAME[0];
        }else if(Arrays.equals(style, LINE_STYLE[1])){
            return LINE_STYLE_NAME[1];
        }else if(Arrays.equals(style, LINE_STYLE[2])){
            return LINE_STYLE_NAME[2];
        }else if(Arrays.equals(style, LINE_STYLE[3])){
            return LINE_STYLE_NAME[3];
        }else if(Arrays.equals(style, LINE_STYLE[4])){
            return LINE_STYLE_NAME[4];
        }else if(Arrays.equals(style, LINE_STYLE[5])){
            return LINE_STYLE_NAME[5];
        }else if(Arrays.equals(style, LINE_STYLE[6])){
            return LINE_STYLE_NAME[6];
        }else if(Arrays.equals(style, LINE_STYLE[7])){
            return LINE_STYLE_NAME[7];
        }else if(Arrays.equals(style, LINE_STYLE[8])){
            return LINE_STYLE_NAME[8];
        }else {
            return LINE_STYLE_NAME[9];
        }

    }

    public static final float[] stringStyleToFloat(String style){

        if(LINE_STYLE_NAME[0].equals(style)){
            return LINE_STYLE[0];
        }else if(LINE_STYLE_NAME[1].equals(style)){
            return LINE_STYLE[1];
        }else if(LINE_STYLE_NAME[2].equals(style)){
            return LINE_STYLE[2];
        }else if(LINE_STYLE_NAME[3].equals(style)){
            return LINE_STYLE[3];
        }else if(LINE_STYLE_NAME[4].equals(style)){
            return LINE_STYLE[4];
        }else if(LINE_STYLE_NAME[5].equals(style)){
            return LINE_STYLE[5];
        }else if(LINE_STYLE_NAME[6].equals(style)){
            return LINE_STYLE[6];
        }else if(LINE_STYLE_NAME[7].equals(style)){
            return LINE_STYLE[7];
        }else {
            return LINE_STYLE[8];
        }
    }

    public static final Color stringToColor(String colorName){
        return PaintUtilities.stringToColor(colorName);
    }

    public static final String colorToString(Color color){
        return PaintUtilities.colorToString(color);
    }

}

/*
 * Copyright (c) 2012 Patrick Meyer
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

package com.itemanalysis.jmetrik.graph.barchart;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class BarChartCommand extends AbstractCommand {

    public BarChartCommand()throws IllegalArgumentException{
        super("barchart", "Bar Chart");
        try{
            FreeOption selectedVariables = new FreeOption("variable", "Selected variable", true, OptionValueType.STRING);
            this.addFreeOption(selectedVariables);

//            FreeOption selectedLabels = new FreeOption("label", "Selected variable labels", false, OptionValueType.STRING);
//            this.addFreeOption(selectedLabels);

            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.addArgument("db", "name of database", OptionValueType.STRING);
            dataInfo.addArgument("table", "database table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

//            SelectOneOption orientation = new SelectOneOption("orientation", "Chart Orientation", false);
//            orientation.addArgument("horizontal", "Horizontal Orientation");
//            orientation.addArgument("vertical", "Vertical Orientation");
//            this.addSelectOneOption(orientation);

            SelectOneOption view = new SelectOneOption("view", "Chart view", false);
            view.addArgument("3D", "Three-dimensional view");
            view.addArgument("2D", "Two-dimensional view");
            this.addSelectOneOption(view);

            SelectOneOption groupLayout = new SelectOneOption("layout", "Group layout", false);
            groupLayout.addArgument("stacked", "Stacked group layout");
            groupLayout.addArgument("layered", "Layered group layout");
            groupLayout.addArgument("sidebyside", "Side-by-side group layout");
            this.addSelectOneOption(groupLayout);

            SelectOneOption yAxis = new SelectOneOption("yaxis", "Y-axis scale", false);
            yAxis.addArgument("freq", "Show frequency on y-axis");
            yAxis.addArgument("percentage", "Show percentage on y-axis");
            this.addSelectOneOption(yAxis);

//            PairedOptionList dimensions = new PairedOptionList("dimensions", "Chart dimensions", false);
//            dimensions.addArgument("width", OptionValueType.INTEGER);
//            dimensions.addArgument("height", OptionValueType.INTEGER);
//            dimensions.addValue("width", 450);
//            dimensions.addValue("height", 400);
//            this.addPairedOptionList(dimensions);

            FreeOption title = new FreeOption("title", "Chart title", false, OptionValueType.STRING);
            this.addFreeOption(title);

            FreeOption subTitle = new FreeOption("subtitle", "Chart subtitle", false, OptionValueType.STRING);
            this.addFreeOption(subTitle);

            FreeOption groupVar = new FreeOption("groupvar", "Grouping variable", false, OptionValueType.STRING);
            this.addFreeOption(groupVar);


        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
    }

    public String getDataString()throws IllegalArgumentException{
        String s = "";
        try{
            s = this.getPairedOptionList("data").getStringAt("db") + "." +
                    this.getPairedOptionList("data").getStringAt("table");
        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
        return s;
    }

}

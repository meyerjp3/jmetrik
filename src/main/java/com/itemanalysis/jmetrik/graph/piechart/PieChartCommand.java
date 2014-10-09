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

package com.itemanalysis.jmetrik.graph.piechart;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class PieChartCommand extends AbstractCommand {

    public PieChartCommand()throws IllegalArgumentException{
        super("piechart", "Pie Chart");
        try{
            FreeOption selectedVariables = new FreeOption("variable", "Selected variable", true, OptionValueType.STRING);
            this.addFreeOption(selectedVariables);

            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.add("db", OptionValueType.STRING);
            dataInfo.add("table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

            SelectOneOption view = new SelectOneOption("view", "Chart view", false);
            view.addArgument("3D", "Three-dimensional view");
            view.addArgument("2D", "Two-dimensional view");
            this.addSelectOneOption(view);

            FreeOption title = new FreeOption("title", "Chart title", false, OptionValueType.STRING);
            this.addFreeOption(title);

            FreeOption subTitle = new FreeOption("subtitle", "Chart subtitle", false, OptionValueType.STRING);
            this.addFreeOption(subTitle);

            FreeOption groupVar = new FreeOption("groupvar", "Grouping variable", false, OptionValueType.STRING);
            this.addFreeOption(groupVar);

            PairedOptionList explodeOption = new PairedOptionList("explode", "Explode section of pie chart", false);
            explodeOption.addArgument("section", "Section to explode", OptionValueType.STRING);
            explodeOption.addArgument("amount", "Amount to explode (0 to 100)", OptionValueType.INTEGER);
            this.addPairedOptionList(explodeOption);


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

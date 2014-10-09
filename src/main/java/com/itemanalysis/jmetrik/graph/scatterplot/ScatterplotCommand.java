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

package com.itemanalysis.jmetrik.graph.scatterplot;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class ScatterplotCommand extends AbstractCommand {

    public ScatterplotCommand()throws IllegalArgumentException{
        super("scatter", "Scatterplot");
        try{
            FreeOption xVariable = new FreeOption("xvar", "Variable for X-axis", true, OptionValueType.STRING);
            this.addFreeOption(xVariable);

            FreeOption yVariable = new FreeOption("yvar", "Variable for Y-axis", true, OptionValueType.STRING);
            this.addFreeOption(yVariable);

            FreeOption xVariableLabel = new FreeOption("xlabel", "Label for X-axis", true, OptionValueType.STRING);
            this.addFreeOption(xVariableLabel);

            FreeOption yVariableLabel = new FreeOption("ylabel", "Label for Y-axis", true, OptionValueType.STRING);
            this.addFreeOption(yVariableLabel);

            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.add("db", OptionValueType.STRING);
            dataInfo.add("table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

            SelectAllOption options = new SelectAllOption("options", "Chart options", false);
            options.addArgument("legend", "Show chart legend", true);
            options.addArgument("markers", "Show markers through origin", true);
            this.addSelectAllOption(options);

            PairedOptionList dimensions = new PairedOptionList("dimensions", "Chart dimensions", false);
            dimensions.add("width", OptionValueType.INTEGER);
            dimensions.add("height", OptionValueType.INTEGER);
            dimensions.addValue("width", 450);
            dimensions.addValue("height", 400);
            this.addPairedOptionList(dimensions);

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

}

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

package com.itemanalysis.jmetrik.graph.histogram;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class HistogramCommand extends AbstractCommand {

    public HistogramCommand()throws IllegalArgumentException{
        super("histogram", "Histogram", false);

        try{
            FreeOption selectedVariable = new FreeOption("variable", "Selected variable", true, OptionValueType.STRING);
            this.addFreeOption(selectedVariable);

            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.add("db", OptionValueType.STRING);
            dataInfo.add("table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

            SelectOneOption yaxis = new SelectOneOption("yaxis", "Y-axis scale", false);
            yaxis.addArgument("freq", "Frequency");
            yaxis.addArgument("density", "Density");
            this.addSelectOneOption(yaxis);

            SelectOneOption binType = new SelectOneOption("bintype", "Method for computing number of bins", false);
            binType.addArgument("scott", "Scott's method");
            binType.addArgument("fd", "Freedman-Diaconis method");
            binType.addArgument("sturges", "Stuges' method");
            this.addSelectOneOption(binType);

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

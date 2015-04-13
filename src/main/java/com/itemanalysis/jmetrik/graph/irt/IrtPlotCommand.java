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

package com.itemanalysis.jmetrik.graph.irt;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class IrtPlotCommand extends AbstractCommand {

    public IrtPlotCommand()throws IllegalArgumentException{
        super("irtplot", "IRT Plot");
        try{

            FreeOptionList variables = new FreeOptionList("variables", "Item to plot", true, OptionValueType.STRING);
            this.addFreeOptionList(variables);

            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.add("db", OptionValueType.STRING);
            dataInfo.add("table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

            PairedOptionList responseData = new PairedOptionList("response", "Name of table with item response data", false);
            responseData.addArgument("table", "Name of table that contains item resposnes.", OptionValueType.STRING);
            responseData.addArgument("thin", "Sum score increment to plot (e.g. every 5th value).", OptionValueType.INTEGER);
            this.addPairedOptionList(responseData);

            SelectAllOption options = new SelectAllOption("options", "Chart options", false);
            options.addArgument("legend", "Show chart legend", true);
            this.addSelectAllOption(options);

            SelectAllOption item = new SelectAllOption("item", "Item plot options", false);
            item.addArgument("icc", "Item characteristic curve", true);
            item.addArgument("info", "Item information function", false);
            this.addSelectAllOption(item);

            SelectAllOption person = new SelectAllOption("person", "Person plot options", false);
            person.addArgument("tcc", "Test characteristic curve", true);
            person.addArgument("info", "Test information function", false);
            person.addArgument("se", "Test stadard error", false);
            this.addSelectAllOption(person);

            SelectOneOption type = new SelectOneOption("type", "Type of curve to plot", false);
            type.addArgument("prob", "Category probabilities");
            type.addArgument("expected", "Item expected score");
            this.addSelectOneOption(type);

            PairedOptionList xaxis = new PairedOptionList("xaxis", "X-axis range and points", false);
            xaxis.addArgument("min", "Minimum value", OptionValueType.DOUBLE);
            xaxis.addArgument("max", "Maximum value", OptionValueType.DOUBLE);
            xaxis.addArgument("points", "Number of points on xaxis", OptionValueType.INTEGER);
            xaxis.addValue("min", -5.0);
            xaxis.addValue("max", 5.0);
            xaxis.addValue("points", 31);
            this.addPairedOptionList(xaxis);

            FreeOption output = new FreeOption("output", "Output directory", false, OptionValueType.STRING);
            this.addFreeOption(output);

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

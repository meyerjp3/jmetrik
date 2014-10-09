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

package com.itemanalysis.jmetrik.stats.descriptives;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class DescriptiveCommand extends AbstractCommand {

    public DescriptiveCommand()throws IllegalArgumentException{
        super("descrip", "Descriptive statistics");
        try{
            FreeOptionList selectedVariables = new FreeOptionList("variables", "List of selected variables", true, OptionValueType.STRING);
            this.addFreeOptionList(selectedVariables);

            FreeOptionList selectedLabels = new FreeOptionList("labels", "List of selected variable labels", false, OptionValueType.STRING);
            this.addFreeOptionList(selectedLabels);

            SelectAllOption stats = new SelectAllOption("stats", "Statistics to be computed for each variable", false);
            stats.addArgument("min", "Minimum", true);
            stats.addArgument("q1", "First quartile", true);
            stats.addArgument("median", "Median", true);
            stats.addArgument("mean", "Mean", true);
            stats.addArgument("q3", "Third quartile", true);
            stats.addArgument("max", "Maximum", true);
            stats.addArgument("sd", "Standard deviation", true);
            stats.addArgument("iqr", "Interquartile range", true);
            stats.addArgument("skew", "Skewness", true);
            stats.addArgument("kurtosis", "Kurtosis", true);
            this.addSelectAllOption(stats);

            SelectAllOption options = new SelectAllOption("options", "General analysis options.", false);
            options.addArgument("noprint", "Suppress output.", false);
            this.addSelectAllOption(options);

            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.add("db", OptionValueType.STRING);
            dataInfo.add("table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

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

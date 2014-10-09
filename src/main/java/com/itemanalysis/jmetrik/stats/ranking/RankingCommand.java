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

package com.itemanalysis.jmetrik.stats.ranking;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class RankingCommand extends AbstractCommand {

    public RankingCommand()throws IllegalArgumentException{
        super("rank", "Ranking analysis");
        try{
            FreeOption selectedVariable = new FreeOption("variable", "Selected variable", true, OptionValueType.STRING);
            this.addFreeOption(selectedVariable);

            FreeOption selectedVariableLabel = new FreeOption("label", "Selected variable label", false, OptionValueType.STRING);
            this.addFreeOption(selectedVariableLabel);

            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.add("db", OptionValueType.STRING);
            dataInfo.add("table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

            SelectOneOption ties = new SelectOneOption("ties", "Method for handling ties.", false);
            ties.addArgument("sequential", "Break ties by by order in data.");
            ties.addArgument("min", "Assign smallest rank to tied values.");
            ties.addArgument("average", "Assign average rank to tied values.");
            ties.addArgument("random", "Break ties randomly.");
            ties.addArgument("max", "Assign largest rank to tied values.");//same default as SAS
            this.addSelectOneOption(ties);

            SelectOneOption type = new SelectOneOption("type", "Type of rank score.", false);
            type.addArgument("ntiles", "User specified quantile.");
            type.addArgument("blom", "Blom's normal score.");
            type.addArgument("tukey", "Tukey's normal score.");
            type.addArgument("vdw", "van der Waerden's normal score");
            type.addArgument("rank", "Rank score.");
            this.addSelectOneOption(type);

            FreeOption ntiles = new FreeOption("ntiles", "Rank into specified number of groups.", false, OptionValueType.INTEGER);
            this.addFreeOption(ntiles);

            SelectOneOption order = new SelectOneOption("order", "Order of ranking.", false);
            order.addArgument("desc", "Descending order (Assign 1 to largest value)");
            order.addArgument("asc", "Ascending order (Assign 1 to smallest value)");
            this.addSelectOneOption(order);

            FreeOption name = new FreeOption("name", "Name of new variable.", false, OptionValueType.STRING);
            this.addFreeOption(name);

            SelectAllOption options = new SelectAllOption("options", "General analysis options.", false);
            options.addArgument("noprint", "Suppress output.", false);
            this.addSelectAllOption(options);


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

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

package com.itemanalysis.jmetrik.stats.itemanalysis;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class ItemAnalysisCommand extends AbstractCommand {

    public ItemAnalysisCommand()throws IllegalArgumentException{
        super("item", "Item analysis");

        FreeOptionList selectedVariables = new FreeOptionList("variables", "List of selected variables", true, OptionValueType.STRING);
        this.addFreeOptionList(selectedVariables);

        PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
        dataInfo.add("db", OptionValueType.STRING);
        dataInfo.add("table", OptionValueType.STRING);
        this.addPairedOptionList(dataInfo);

        SelectAllOption options = new SelectAllOption("options", "Analysis options", false);
        options.addArgument("spur", "Correct item discrimination for spuriousness", true);
        options.addArgument("unbiased", "Use N-1 in denominator of item covariances", false);
        options.addArgument("header", "Show item headers", false);
        options.addArgument("all", "Show statistics for all response options", true);
        options.addArgument("scores", "Save raw scores to data table", false);
        options.addArgument("delrel", "Compute reliability with each item deleted", false);
        options.addArgument("istats", "Show item-level statistics", true);
        options.addArgument("csem", "Show conditional standard error of measurement for all score levels", false);
        options.addArgument("noprint", "Suppress output", false);
        this.addSelectAllOption(options);

        SelectOneOption missing = new SelectOneOption("missing", "Missing data treatment", false);
        missing.addArgument("listwise", "Listwise deletion");
        missing.addArgument("zero", "Score missing responses as 0 points");
        this.addSelectOneOption(missing);

        SelectOneOption correlation = new SelectOneOption("correlation", "Type of correlation used for item discrimination", false);
        correlation.addArgument("pearson", "Pearson correlation");
        correlation.addArgument("polyserial", "Polyserial correlation");
        this.addSelectOneOption(correlation);

        PairedOptionList saveOutput = new PairedOptionList("output", "Output table", false);
        saveOutput.add("db", OptionValueType.STRING);
        saveOutput.add("table", OptionValueType.STRING);
        this.addPairedOptionList(saveOutput);

        FreeOptionList cut = new FreeOptionList("cut", "Cut scores", false, OptionValueType.INTEGER);
        this.addFreeOptionList(cut);
    }

    public String getDataString()throws IllegalArgumentException{
        String s = s = this.getPairedOptionList("data").getStringAt("db") + "." +
                this.getPairedOptionList("data").getStringAt("table");
        return s;
    }

}

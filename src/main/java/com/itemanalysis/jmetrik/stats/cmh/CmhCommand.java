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

package com.itemanalysis.jmetrik.stats.cmh;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class CmhCommand extends AbstractCommand {

    public CmhCommand()throws IllegalArgumentException{
        super("cmh", "Cochran-Mantel-Haenszel analysis");

        FreeOptionList selectedVariables = new FreeOptionList("variables", "List of selected variables", true, OptionValueType.STRING);
        this.addFreeOptionList(selectedVariables);

        FreeOption groupVariable = new FreeOption("groupvar", "DIF group variable", true, OptionValueType.STRING);
        this.addFreeOption(groupVariable);

        FreeOption matchingVariable = new FreeOption("matchvar", "Matching variable", true, OptionValueType.STRING);
        this.addFreeOption(matchingVariable);

        PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
        dataInfo.add("db", OptionValueType.STRING);
        dataInfo.add("table", OptionValueType.STRING);
        this.addPairedOptionList(dataInfo);

        SelectOneOption effectSize = new SelectOneOption("effectsize", "Effect size", false);
        effectSize.addArgument("ets", "ETS delta");
        effectSize.addArgument("odds", "Common odds ratio");
        this.addSelectOneOption(effectSize);

        SelectAllOption options = new SelectAllOption("options", "General options", false);
        options.addArgument("tables", "Show frequency tables", false);
        options.addArgument("noprint", "Suppress output", false);
        options.addArgument("zero", "Score missing item responses as zero points", false);
        this.addSelectAllOption(options);

        PairedOptionList groupCodes = new PairedOptionList("codes", "Focal and references group codes", false);
        groupCodes.add("focal", OptionValueType.STRING);
        groupCodes.add("reference", OptionValueType.STRING);
        this.addPairedOptionList(groupCodes);

        PairedOptionList saveOutput = new PairedOptionList("output", "Output table", false);
        saveOutput.add("db", OptionValueType.STRING);
        saveOutput.add("table", OptionValueType.STRING);
        this.addPairedOptionList(saveOutput);

    }

    public String getDataString()throws IllegalArgumentException{
        String s = this.getPairedOptionList("data").getStringAt("db") + "." +
                this.getPairedOptionList("data").getStringAt("table");
        return s;
    }

}

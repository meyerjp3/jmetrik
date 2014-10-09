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

package com.itemanalysis.jmetrik.stats.correlation;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class CorrelationCommand extends AbstractCommand {

    public CorrelationCommand()throws IllegalArgumentException{
        super("corr", "Correlation analysis");

        FreeOptionList selectedVariables = new FreeOptionList("variables", "List of selected variables", true, OptionValueType.STRING);
        this.addFreeOptionList(selectedVariables);

        FreeOptionList selectedLabels = new FreeOptionList("labels", "List of selected variable labels", false, OptionValueType.STRING);
        this.addFreeOptionList(selectedLabels);

        PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
        dataInfo.add("db", OptionValueType.STRING);
        dataInfo.add("table", OptionValueType.STRING);
        this.addPairedOptionList(dataInfo);

        SelectOneOption missing = new SelectOneOption("missing", "Missing data treatment", false);
        missing.addArgument("pairwise", "Pairwise deletion");
        missing.addArgument("listwise", "Listwise deletion");
        this.addSelectOneOption(missing);

        SelectOneOption estimator = new SelectOneOption("estimator", "Type of estimator", false);
        estimator.addArgument("biased", "Population correlation - uses n");
        estimator.addArgument("unbiased", "Sample correlation - uses n-1");
        this.addSelectOneOption(estimator);

        SelectOneOption type = new SelectOneOption("type", "Type of correlation", false);
        type.addArgument("mixed", "Pearson, polyserial, or polychoric correlation");
        type.addArgument("pearson", "Pearson correlation");
        this.addSelectOneOption(type);

        SelectOneOption polyOptions = new SelectOneOption("polychoric", "Polychoric correlation options", false);
        polyOptions.addArgument("ml", "Use maximum likelihood estimator");
        polyOptions.addArgument("twostep", "Use two-step estimator");
        this.addSelectOneOption(polyOptions);

        SelectAllOption options = new SelectAllOption("options", "General analysis options", false);
        options.addArgument("noprint", "Suppress output", false);
        options.addArgument("stderror", "Display standard errors", false);
        this.addSelectAllOption(options);

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

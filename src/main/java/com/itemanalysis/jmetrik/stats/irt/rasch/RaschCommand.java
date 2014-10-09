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

package com.itemanalysis.jmetrik.stats.irt.rasch;

import com.itemanalysis.jmetrik.commandbuilder.*;


public class RaschCommand extends AbstractCommand{

    public RaschCommand()throws IllegalArgumentException{
        super("rasch", "Rasch analysis");
        try{
            //data information--------------------------------------------------------------------------------------
            FreeOptionList selectedVariables = new FreeOptionList("variables", "List of selected variables", true, OptionValueType.STRING);
            this.addFreeOptionList(selectedVariables);

            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.add("db", OptionValueType.STRING);
            dataInfo.add("table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

            //item options--------------------------------------------------------------------------------------
            SelectAllOption item = new SelectAllOption("item", "Item options", false);
            item.addArgument("start", "Show item parameter start values (PROX or Fixed)", false);
            item.addArgument("uconbias", "Adjust item parameter estimates ofr UCON estimation bias", false);
            item.addArgument("isave", "Save item parameter estimates", false);
            this.addSelectAllOption(item);

            FreeOption itemout = new FreeOption("itemout", "Item parameter output table name", false, OptionValueType.STRING);
            this.addFreeOption(itemout);

            FreeOption residOut = new FreeOption("residout", "Residual output table name", false, OptionValueType.STRING);
            this.addFreeOption(residOut);

            FreeOptionList ifixed = new FreeOptionList("ifixed", "Item names of items with parameters fixed at start values. See istable argument.", false, OptionValueType.STRING);
            this.addFreeOptionList(ifixed);

            PairedOptionList isTable = new PairedOptionList("iptable", "Data information for item parameter values listed in ifixed", false);
            isTable.add("db", OptionValueType.STRING);
            isTable.add("table", OptionValueType.STRING);
            this.addPairedOptionList(isTable);

            FreeOption adjust = new FreeOption("adjust", "Extreme score adjustment, 0 <= adjust <= 1", false, OptionValueType.DOUBLE);
            this.addFreeOption(adjust);

            //person options--------------------------------------------------------------------------------------
            SelectAllOption person = new SelectAllOption("person", "Person options", false);
            person.addArgument("psave", "Save person estimates", false);
            person.addArgument("pfit", "Save person fit statistics", false);
            person.addArgument("rsave", "Save residuals", false);
            this.addSelectAllOption(person);

            FreeOptionList pstart = new FreeOptionList("pstart", "Person parameter start values", false, OptionValueType.DOUBLE);
            this.addFreeOptionList(pstart);

            FreeOptionList pfixed = new FreeOptionList("pfixed", "Persons with parameters fixed at start value", false, OptionValueType.STRING);
            this.addFreeOptionList(pfixed);

            PairedOptionList psTable = new PairedOptionList("pstable", "Data information for person start values", false);
            psTable.add("db", OptionValueType.STRING);
            psTable.add("table", OptionValueType.STRING);
            this.addPairedOptionList(psTable);

            //algorithm and update options--------------------------------------------------------------------------------------
            PairedOptionList globalUpdate = new PairedOptionList("gupdate", "Global update settings", false);
            globalUpdate.add("maxiter", OptionValueType.INTEGER);
            globalUpdate.add("converge", OptionValueType.DOUBLE);
            this.addPairedOptionList(globalUpdate);

            //other options-------------------------------------------------------------------------------------------

            PairedOptionList transform = new PairedOptionList("transform", "Linear transformation coefficients and precision", false);
            transform.add("intercept", OptionValueType.DOUBLE);
            transform.add("scale", OptionValueType.DOUBLE);
            transform.add("precision", OptionValueType.INTEGER);
            this.addPairedOptionList(transform);

            SelectOneOption missing = new SelectOneOption("missing", "Missing data treatment", false);
            missing.addArgument("zero", "Score missing data as zero");
            missing.addArgument("ignore", "Ignore missing data");
            this.addSelectOneOption(missing);

            SelectOneOption center = new SelectOneOption("center", "Centering for model identification", false);
            center.addArgument("items", "Set average item difficulty to zero.");
            center.addArgument("persons", "Set average person ability to zero.");
            this.addSelectOneOption(center);

            SelectAllOption options = new SelectAllOption("options", "General analysis options.", false);
            options.addArgument("noprint", "Suppress output", false);
            this.addSelectAllOption(options);

        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
    }

}

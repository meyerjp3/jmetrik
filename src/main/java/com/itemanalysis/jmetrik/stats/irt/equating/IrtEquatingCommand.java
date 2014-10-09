/*
 * Copyright (c) 2013 Patrick Meyer
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

package com.itemanalysis.jmetrik.stats.irt.equating;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class IrtEquatingCommand extends AbstractCommand {

    public IrtEquatingCommand()throws IllegalArgumentException{
        super("irteq", "Item Response Theory Score Equating");

        PairedOptionList xability = new PairedOptionList("xability", "Form X ability data information", false);
        xability.add("db", OptionValueType.STRING);
        xability.add("table", OptionValueType.STRING);
        xability.add("theta", OptionValueType.STRING);
        xability.add("weight", OptionValueType.STRING);
        this.addPairedOptionList(xability);

        PairedOptionList yability = new PairedOptionList("yability", "Form Y ability data information", false);
        yability.add("db", OptionValueType.STRING);
        yability.add("table", OptionValueType.STRING);
        yability.add("theta", OptionValueType.STRING);
        yability.add("weight", OptionValueType.STRING);
        this.addPairedOptionList(yability);

        PairedOptionList xitem = new PairedOptionList("xitem", "Form X item data information", true);
        xitem.add("db", OptionValueType.STRING);
        xitem.add("table", OptionValueType.STRING);
        this.addPairedOptionList(xitem);

        PairedOptionList yitem = new PairedOptionList("yitem", "Form Y item data information", true);
        yitem.add("db", OptionValueType.STRING);
        yitem.add("table", OptionValueType.STRING);
        this.addPairedOptionList(yitem);

        FreeOptionList selectedVariablesX = new FreeOptionList("xvar", "List of selected variables for Form X", true, OptionValueType.STRING);
        this.addFreeOptionList(selectedVariablesX);

        FreeOptionList selectedVariablesY = new FreeOptionList("yvar", "List of selected variables for Form Y", true, OptionValueType.STRING);
        this.addFreeOptionList(selectedVariablesY);

        SelectOneOption scale = new SelectOneOption("scale", "IRT scaling coefficient D", false);
        scale.addArgument("logistic", "Logistic scale D = 1.0");
        scale.addArgument("normal", "Normal ogive scale D = 1.7");
        this.addSelectOneOption(scale);

        SelectAllOption options = new SelectAllOption("options", "Analysis options", false);
        options.addArgument("noprint", "do not print output", false);
        this.addSelectAllOption(options);

        SelectOneOption method = new SelectOneOption("method", "Equating method", false);
        method.addArgument("true", "IRT true score equating");
        method.addArgument("observed", "IRT observed score equating");
        this.addSelectOneOption(method);

        PairedOptionList saveOutput = new PairedOptionList("output", "Output table", false);
        saveOutput.add("db", OptionValueType.STRING);
        saveOutput.add("table", OptionValueType.STRING);
        this.addPairedOptionList(saveOutput);

    }

    public String getDataString()throws IllegalArgumentException{
        String s = "From Form X Item Data: ";
        s += this.getPairedOptionList("xitem").getStringAt("db") + "." +
                this.getPairedOptionList("xitem").getStringAt("table") + "\n";

        if(getPairedOptionList("xability").hasValue()){
            s += "     Form X Person Data: ";
            s += this.getPairedOptionList("xability").getStringAt("db") + "." +
                    this.getPairedOptionList("xability").getStringAt("table") + "\n";
        }



        s += "  To Form Y Item Data: ";
        s += this.getPairedOptionList("yitem").getStringAt("db") + "." +
                this.getPairedOptionList("yitem").getStringAt("table") + "\n";

        if(getPairedOptionList("yability").hasValue()){
            s += "     Form Y Person Data: ";
            s += this.getPairedOptionList("yability").getStringAt("db") + "." +
                    this.getPairedOptionList("yability").getStringAt("table") + "\n";
        }


        return s;
    }


}

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

package com.itemanalysis.jmetrik.stats.irt.linking;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class IrtLinkingCommand extends AbstractCommand{

    public IrtLinkingCommand()throws IllegalArgumentException{
        super("irtlink", "Item Response Theory Scale Linking");
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

        /**
         * This list should be in the form of (xname1, yname1) (xname2, yname2) ...
         * REGEX =
         */
        FreeOptionList xyPairs = new FreeOptionList("xypairs", "Form X to Form Y item pairing", false, OptionValueType.STRING);
        this.addFreeOptionList(xyPairs);

        SelectOneOption distribution = new SelectOneOption("distribution", "Type of theta distribution", false);
        distribution.addArgument("histogram", "Compute histogram of theta values");
        distribution.addArgument("uniform", "Use evenly spaced values from uniform distribution");
        distribution.addArgument("normal", "Use evenly spaced values from standard normal distribution");
        distribution.addArgument("observed", "Use observed values of theta and possibly weights");
        this.addSelectOneOption(distribution);

        /**
         * if number of bins is specified as a positive integer, override binMethod option
         */
        FreeOption numberOfBins = new FreeOption("bins", "Number of bins", false, OptionValueType.INTEGER);
        this.addFreeOption(numberOfBins);

        SelectOneOption binMethod = new SelectOneOption("binmethod", "Binning method", false);
        binMethod.addArgument("sturges", "Compute number of bins with Stuges' method");
        binMethod.addArgument("all", "Use all observed values");
        this.addSelectOneOption(binMethod);

        PairedOptionList uniform = new PairedOptionList("uniform", "Uniform theta disribution", false);
        uniform.add("min", OptionValueType.INTEGER);
        uniform.add("max", OptionValueType.INTEGER);
        uniform.add("bins", OptionValueType.INTEGER);
        this.addPairedOptionList(uniform);

        PairedOptionList normal = new PairedOptionList("normal", "Normal theta disribution", false);
        normal.add("mean", OptionValueType.DOUBLE);
        normal.add("sd", OptionValueType.DOUBLE);
        normal.add("min", OptionValueType.DOUBLE);
        normal.add("max", OptionValueType.DOUBLE);
        normal.add("bins", OptionValueType.INTEGER);
        this.addPairedOptionList(normal);

        SelectOneOption popsd = new SelectOneOption("popsd", "Standard deviation type", false);
        popsd.addArgument("unbiased", "Use N-1 in denominator");
        popsd.addArgument("biased", "Use N in denominator");
        this.addSelectOneOption(popsd);

        SelectOneOption criterion = new SelectOneOption("criterion", "Characteristic curve criterion function", false);
        criterion.addArgument("x", "Form X ability distribution");
        criterion.addArgument("y", "Form Y ability distribution");
        criterion.addArgument("xy", "Form X and Y ability distributions (symmetric)");
        this.addSelectOneOption(criterion);

        SelectOneOption scale = new SelectOneOption("scale", "IRT scaling coefficient D", false);
        scale.addArgument("logistic", "Logistic scale D = 1.0");
        scale.addArgument("normal", "Normal ogive scale D = 1.7");
        this.addSelectOneOption(scale);

        SelectAllOption options = new SelectAllOption("options", "Analysis options", false);
        options.addArgument("noprint", "do not print output", false);
        this.addSelectAllOption(options);

        SelectOneOption method = new SelectOneOption("method", "Transformation method", false);
        method.addArgument("ms", "Mean/Sigma method");
        method.addArgument("mm", "Mean/Mean method");
        method.addArgument("hb", "Haebara characteristic curve method");
        method.addArgument("sl", "Stocking-Lord characteristic curve method");
        this.addSelectOneOption(method);

        SelectAllOption transform = new SelectAllOption("transform", "Transform Form X parameters", false);
        transform.addArgument("items", "Transform Form X item parameters", false);
        transform.addArgument("persons", "Transform Form X person parameters", false);
        this.addSelectAllOption(transform);

        FreeOption precision = new FreeOption("precision", "Number of decimal places in scaling coefficients", false, OptionValueType.INTEGER);
        this.addFreeOption(precision);
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

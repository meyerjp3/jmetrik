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

package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class IrtPersonScoringCommand extends AbstractCommand {

    public IrtPersonScoringCommand()throws IllegalArgumentException{
        super("irtscoring", "Item Response Theory Person Scoring");

        FreeOptionList selectedVariables = new FreeOptionList("variables", "List of selected variables", true, OptionValueType.STRING);
        this.addFreeOptionList(selectedVariables);

        PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
        dataInfo.add("db", OptionValueType.STRING);
        dataInfo.add("table", OptionValueType.STRING);
        this.addPairedOptionList(dataInfo);

        PairedOptionList quadrature = new PairedOptionList("quad", "User provided quadrature", false);
        quadrature.add("db", OptionValueType.STRING);
        quadrature.add("table", OptionValueType.STRING);
        quadrature.add("theta", OptionValueType.STRING);
        quadrature.add("weight", OptionValueType.STRING);
        this.addPairedOptionList(quadrature);

        PairedOptionList iParam = new PairedOptionList("iptable", "Item parameter table information", true);
        iParam.add("db", OptionValueType.STRING);
        iParam.add("table", OptionValueType.STRING);
        this.addPairedOptionList(iParam);

        SelectOneOption missing = new SelectOneOption("missing", "Missing data treatment", false);
        missing.addArgument("zero", "Score missing data as zero");
        missing.addArgument("ignore", "Ignore missing data");
        this.addSelectOneOption(missing);

        SelectAllOption method = new SelectAllOption("method", "Estimation method", true);
        method.addArgument("mle", "Maximum likelihood", true);
        method.addArgument("map", "Maximum a posteriori", false);
        method.addArgument("eap", "Expected a posteriori", false);
        this.addSelectAllOption(method);

        PairedOptionList bounds = new PairedOptionList("bounds", "Min and max values of theta", false);
        bounds.add("min", OptionValueType.DOUBLE);
        bounds.add("max", OptionValueType.DOUBLE);
        this.addPairedOptionList(bounds);

        PairedOptionList normPrior = new PairedOptionList("normprior", "Normal prior parameters", false);
        normPrior.add("mean", OptionValueType.DOUBLE);
        normPrior.add("sd", OptionValueType.DOUBLE);
        this.addPairedOptionList(normPrior);

        FreeOption points = new FreeOption("numpoints", "Number of quadrature points", false, OptionValueType.INTEGER);
        this.addFreeOption(points);

        PairedOptionList converge = new PairedOptionList("criteria", "Convergence criteria", false);
        converge.add("maxiter", OptionValueType.INTEGER);
        converge.add("converge", OptionValueType.DOUBLE);
        this.addPairedOptionList(converge);

        FreeOption personOut = new FreeOption("name", "Base name of output variable", false, OptionValueType.STRING);
        this.addFreeOption(personOut);

        SelectOneOption scale = new SelectOneOption("scale", "IRT scaling coefficient D", false);
        scale.addArgument("logistic", "Logistic scale D = 1.0");
        scale.addArgument("normal", "Normal ogive scale D = 1.7");
        this.addSelectOneOption(scale);

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

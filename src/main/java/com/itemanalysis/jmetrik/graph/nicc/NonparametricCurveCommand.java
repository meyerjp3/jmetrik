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

package com.itemanalysis.jmetrik.graph.nicc;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class NonparametricCurveCommand extends AbstractCommand {

    public NonparametricCurveCommand()throws IllegalArgumentException{
        super("nicc", "Nonparamedtric item characteristic curves");

        FreeOptionList selectedVariables = new FreeOptionList("variables", "List of selected variables", true, OptionValueType.STRING);
        this.addFreeOptionList(selectedVariables);

        FreeOption regressorVariable = new FreeOption("xvar", "Regressor variable", true, OptionValueType.STRING);
        this.addFreeOption(regressorVariable);

        FreeOption groupVariable = new FreeOption("groupvar", "DIF group variable", false, OptionValueType.STRING);
        this.addFreeOption(groupVariable);

        PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
        dataInfo.add("db", OptionValueType.STRING);
        dataInfo.add("table", OptionValueType.STRING);
        this.addPairedOptionList(dataInfo);

        SelectOneOption curves = new SelectOneOption("curves", "Type of curves", false);
        curves.addArgument("all", "Show curves for all response options");
        curves.addArgument("expected", "Show curve for correct answer (expected item score)");
        this.addSelectOneOption(curves);

        SelectOneOption kernel = new SelectOneOption("kernel", "Kernel type", false);
        kernel.addArgument("epanechnikov", "Epanechnikov kernel");
        kernel.addArgument("uniform", "Uniform kernel");
        kernel.addArgument("triangle", "Triangle kernel");
        kernel.addArgument("biweight", "Biweight kernel");
        kernel.addArgument("triweight", "Triweight kernel");
        kernel.addArgument("cosine", "Cosine kernel");
        kernel.addArgument("gaussian", "Gaussian kernel");
        this.addSelectOneOption(kernel);

        FreeOption adjust = new FreeOption("adjust", "Bandwidth adjustment factor", false, OptionValueType.DOUBLE);
        adjust.add(1.0);
        this.addFreeOption(adjust);

        FreeOption grid = new FreeOption("gridpoints", "Number of grid points for kernel estimator", false, OptionValueType.INTEGER);
        grid.add(51);
        this.addFreeOption(grid);

        SelectAllOption options = new SelectAllOption("options", "General options", false);
        options.addArgument("noprint", "Do not display results file", false);
        this.addSelectAllOption(options);

        PairedOptionList groupCodes = new PairedOptionList("codes", "Focal and references group codes", false);
        groupCodes.add("focal", OptionValueType.STRING);
        groupCodes.add("reference", OptionValueType.STRING);
        this.addPairedOptionList(groupCodes);

        FreeOption output = new FreeOption("output", "Output directory", false, OptionValueType.STRING);
        this.addFreeOption(output);

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

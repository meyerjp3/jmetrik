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

package com.itemanalysis.jmetrik.graph.density;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class DensityCommand extends AbstractCommand {


    public DensityCommand()throws IllegalArgumentException{
        super("density", "Density estimation");

        FreeOption selectedVariables = new FreeOption("variable", "Selected variable", true, OptionValueType.STRING);
        this.addFreeOption(selectedVariables);

        PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
        dataInfo.addArgument("db", "name of database", OptionValueType.STRING);
        dataInfo.addArgument("table", "name of table", OptionValueType.STRING);
        this.addPairedOptionList(dataInfo);

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

        FreeOption title = new FreeOption("title", "Chart title", false, OptionValueType.STRING);
        this.addFreeOption(title);

        FreeOption subTitle = new FreeOption("subtitle", "Chart subtitle", false, OptionValueType.STRING);
        this.addFreeOption(subTitle);

        FreeOption groupVar = new FreeOption("groupvar", "Grouping variable", false, OptionValueType.STRING);
        this.addFreeOption(groupVar);


    }


}

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

package com.itemanalysis.jmetrik.stats.transformation;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class LinearTransformationCommand extends AbstractCommand {

    public LinearTransformationCommand() throws IllegalArgumentException{
        super("lintran", "Linear Transformation");
        try{
            FreeOptionList selectedVariables = new FreeOptionList("variables", "List of selected variables", true, OptionValueType.STRING);
            this.addFreeOptionList(selectedVariables);

            FreeOption newVariableName = new FreeOption("name", "New variable name", true, OptionValueType.STRING);
            this.addFreeOption(newVariableName);

            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.add("db", OptionValueType.STRING);
            dataInfo.add("table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

            PairedOptionList constraints = new PairedOptionList("constraints", "Scaling constraints", false);
            constraints.add("min", OptionValueType.DOUBLE);
            constraints.add("max", OptionValueType.DOUBLE);
            constraints.add("precision", OptionValueType.INTEGER);
            constraints.addValue("min", Double.NEGATIVE_INFINITY);
            constraints.addValue("max", Double.POSITIVE_INFINITY);
            constraints.addValue("precision", 2);
            this.addPairedOptionList(constraints);

            PairedOptionList transform = new PairedOptionList("transform", "Linear transformation", false);
            transform.add("intercept", OptionValueType.DOUBLE);
            transform.add("slope", OptionValueType.DOUBLE);
            this.addPairedOptionList(transform);

            SelectAllOption options = new SelectAllOption("options", "General analysis options.", false);
            options.addArgument("noprint", "Suppress output", false);
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

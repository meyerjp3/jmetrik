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

package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class SubsetCasesCommand extends AbstractCommand {

    public SubsetCasesCommand()throws IllegalArgumentException{
        super("subcase", "Subset cases command");
        try{
            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.add("db", OptionValueType.STRING);
            dataInfo.add("table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

            FreeOption newTable = new FreeOption("newtable", "New table name", true, OptionValueType.STRING);
            this.addFreeOption(newTable);

            FreeOption whereStatement = new FreeOption("where", "Where statement text", true, OptionValueType.STRING);
            this.addFreeOption(whereStatement);

            SelectAllOption options = new SelectAllOption("options", "General options", false);
            options.addArgument("display", "Display imported data", true);
            options.addArgument("force", "Force unique table name", true);
            this.addSelectAllOption(options);

        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
    }

}

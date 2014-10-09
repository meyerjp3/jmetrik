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

package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.AbstractCommand;
import com.itemanalysis.jmetrik.commandbuilder.OptionValueType;
import com.itemanalysis.jmetrik.commandbuilder.PairedOptionList;

public class RenameVariableCommand extends AbstractCommand {

    public RenameVariableCommand()throws IllegalArgumentException{
        super("renamevar", "Rename database variable");

        PairedOptionList data = new PairedOptionList("data", "Database information", true);
        data.add("db", OptionValueType.STRING);
        data.add("table", OptionValueType.STRING);
        this.addPairedOptionList(data);

        PairedOptionList varNames = new PairedOptionList("variable", "Variable names", true);
        varNames.add("oldname", OptionValueType.STRING);
        varNames.add("newname", OptionValueType.STRING);
        this.addPairedOptionList(varNames);

    }

}

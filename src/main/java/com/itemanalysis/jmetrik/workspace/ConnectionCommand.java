/*
 * Copyright (c) 2011 Patrick Meyer
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
import com.itemanalysis.jmetrik.commandbuilder.FreeOption;
import com.itemanalysis.jmetrik.commandbuilder.FreeOptionList;

public class ConnectionCommand extends AbstractCommand{

    public ConnectionCommand()throws IllegalArgumentException{
        super("connect", "Database connection");

        FreeOption dbName = new FreeOption("db", "Database name", true, OptionValueType.STRING);
        this.addFreeOption(dbName);

        FreeOption hostName = new FreeOption("host", "Host/Absolute path to db", true, OptionValueType.STRING);
        this.addFreeOption(hostName);

        FreeOption port = new FreeOption("port", "Database port", true, OptionValueType.STRING);
        this.addFreeOption(hostName);

        /**
         * Should be valid db property key-value pairs i.e. "create=true;"
         */
        FreeOptionList propertyValuePairs = new FreeOptionList("props", OptionValueType.STRING);
        this.addFreeOptionList(propertyValuePairs);

    }


}

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
import com.itemanalysis.jmetrik.commandbuilder.SelectOneOption;

public class CloseDatabaseCommand extends AbstractCommand {

    public CloseDatabaseCommand()throws IllegalArgumentException{
        super("database", "New database command");
        try{
            FreeOption name = new FreeOption("name", "Database name", true, OptionValueType.STRING);
            this.addFreeOption(name);

            FreeOption path = new FreeOption("path", "Path/Host to database", true, OptionValueType.STRING);
            this.addFreeOption(path);

            FreeOption username = new FreeOption("username", "User name", true, OptionValueType.STRING);
            this.addFreeOption(username);

            FreeOption password = new FreeOption("password", "Password", true, OptionValueType.STRING);
            this.addFreeOption(password);

            FreeOption port = new FreeOption("port", "Port", false, OptionValueType.INTEGER);
            this.addFreeOption(port);

            SelectOneOption create = new SelectOneOption("action", "Database Action", true);
            create.addArgument("create", "Create database");
            create.addArgument("delete", "Delete database");
            this.addSelectOneOption(create);


        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
    }

}

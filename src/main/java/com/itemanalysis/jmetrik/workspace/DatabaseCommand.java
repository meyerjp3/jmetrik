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

import com.itemanalysis.jmetrik.commandbuilder.*;

public class DatabaseCommand extends AbstractCommand {

    public DatabaseCommand()throws IllegalArgumentException{
        super("database", "Database command");
        try{
            FreeOption name = new FreeOption("name", "Database name", true, OptionValueType.STRING);
            this.addFreeOption(name);

            FreeOption port = new FreeOption("port", "Port", false, OptionValueType.INTEGER);
            this.addFreeOption(port);

            SelectOneOption action = new SelectOneOption("action", "Database Action", true);
            action.addArgument("create", "Create database");
            action.addArgument("open", "Open database");
            action.addArgument("close", "Close database");
            action.addArgument("delete-db", "Delete database");
            action.addArgument("delete-table", "Delete table from database");
            this.addSelectOneOption(action);

            /**
             * This list is mainly for the delete-table action
             */
            FreeOptionList dataToDrop = new FreeOptionList("tables", "List of selected tables within a database to drop", false, OptionValueType.STRING);
            this.addFreeOptionList(dataToDrop);


        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
    }
}

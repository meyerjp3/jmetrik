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

public class ImportCommand extends AbstractCommand{

    public ImportCommand()throws IllegalArgumentException{
        super("import", "Import delimited data file");

        FreeOption fileName = new FreeOption("file", "File name", true, OptionValueType.STRING);
        this.addFreeOption(fileName);

        PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
        dataInfo.add("db", OptionValueType.STRING);
        dataInfo.add("table", OptionValueType.STRING);
        this.addPairedOptionList(dataInfo);

        SelectOneOption delimiter = new SelectOneOption("delimiter", "Type of delimiter", true);
        delimiter.addArgument("tab", "Tab delimiter");
        delimiter.addArgument("colon", "Colon delimiter");
        delimiter.addArgument("semicolon", "Semicolon delimiter");
        delimiter.addArgument("comma", "Comma delimiter");
        this.addSelectOneOption(delimiter);

        SelectOneOption header = new SelectOneOption("header", "Column headers in file", true);
        header.addArgument("excluded", "Header is excluded from file");
        header.addArgument("included", "Header is included in file");
        this.addSelectOneOption(header);

//        FreeOption rowScan = new FreeOption("rows", "Number of rows to scan to check data type.", false, OptionValueType.INTEGER);
//        this.addFreeOption(rowScan);

        FreeOption description = new FreeOption("description", "Description of table.", false, OptionValueType.STRING);
        this.addFreeOption(description);

        SelectAllOption options = new SelectAllOption("options", "General options", false);
        options.addArgument("display", "Display imported data", true);
        this.addSelectAllOption(options);


    }

}

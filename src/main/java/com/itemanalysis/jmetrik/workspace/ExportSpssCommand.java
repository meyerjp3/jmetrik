package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class ExportSpssCommand extends AbstractCommand {

    public ExportSpssCommand()throws IllegalArgumentException{
        super("exportspss", "Export SPSS *.sav file");

        FreeOption fileName = new FreeOption("file", "File name", true, OptionValueType.STRING);
        this.addFreeOption(fileName);

        FreeOption pathToPlugin = new FreeOption("pluginpath", "Path to SPSS Java Plugin jar file.", false, OptionValueType.STRING);
        this.addFreeOption(pathToPlugin);

        PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
        dataInfo.add("db", OptionValueType.STRING);
        dataInfo.add("table", OptionValueType.STRING);
        this.addPairedOptionList(dataInfo);

        SelectAllOption options = new SelectAllOption("options", "General options", false);
        options.addArgument("scored", "Export scored item responses", false);
        this.addSelectAllOption(options);

    }


}

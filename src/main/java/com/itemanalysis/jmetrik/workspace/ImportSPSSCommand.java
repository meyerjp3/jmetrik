package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class ImportSPSSCommand extends AbstractCommand {

    public ImportSPSSCommand()throws IllegalArgumentException{
        super("importspss", "Import SPSS *.sav file");

        FreeOption fileName = new FreeOption("file", "File name", true, OptionValueType.STRING);
        this.addFreeOption(fileName);

        FreeOption pathToPlugin = new FreeOption("path", "Path to SPSS Java Plugin jar file.", false, OptionValueType.STRING);
        this.addFreeOption(pathToPlugin);

        PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
        dataInfo.add("db", OptionValueType.STRING);
        dataInfo.add("table", OptionValueType.STRING);
        this.addPairedOptionList(dataInfo);

        SelectOneOption valueLabels = new SelectOneOption("use", "Type of data to use", false);
        valueLabels.addArgument("vlabels", "Use value labels when provided instead of original data.");
        valueLabels.addArgument("data", "Use original data and ignore value labels.");
        this.addSelectOneOption(valueLabels);

        FreeOption description = new FreeOption("description", "Description of table.", false, OptionValueType.STRING);
        this.addFreeOption(description);

        SelectAllOption options = new SelectAllOption("options", "General options", false);
        options.addArgument("display", "Display imported SPSS data", true);
        this.addSelectAllOption(options);


    }


}

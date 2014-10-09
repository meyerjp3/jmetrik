/**
 * Copyright 2014 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.AbstractCommand;
import com.itemanalysis.jmetrik.commandbuilder.OptionValueType;
import com.itemanalysis.jmetrik.commandbuilder.PairedOptionList;

public class VariableGroupCommand extends AbstractCommand {

    public VariableGroupCommand()throws IllegalArgumentException{
        super("vargroup", "Set variable group code");

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

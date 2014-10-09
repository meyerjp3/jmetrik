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

package com.itemanalysis.jmetrik.scoring;

import com.itemanalysis.jmetrik.commandbuilder.AbstractCommand;
import com.itemanalysis.jmetrik.commandbuilder.OptionValueType;
import com.itemanalysis.jmetrik.commandbuilder.FreeOptionList;
import com.itemanalysis.jmetrik.commandbuilder.PairedOptionList;

public class BasicScoringCommand extends AbstractCommand {


    public BasicScoringCommand()throws IllegalArgumentException{
        super("bscoring", "Basic item scoring");

        PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
        dataInfo.add("db", OptionValueType.STRING);
        dataInfo.add("table", OptionValueType.STRING);
        this.addPairedOptionList(dataInfo);

        FreeOptionList key = new FreeOptionList("key", "Answer key as comma delimited list.", true, OptionValueType.STRING);
        this.addFreeOptionList(key);

        FreeOptionList ncat = new FreeOptionList("ncat", "Number of response options or score categories", true, OptionValueType.INTEGER);
        this.addFreeOptionList(ncat);

        PairedOptionList special = new PairedOptionList("codes", "Special codes for omitted and not reached", false);
        special.add("omit", OptionValueType.STRING);
        special.add("nr", OptionValueType.STRING);
        this.addPairedOptionList(special);

    }

}

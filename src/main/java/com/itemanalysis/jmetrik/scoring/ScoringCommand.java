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

package com.itemanalysis.jmetrik.scoring;

import com.itemanalysis.jmetrik.commandbuilder.*;

public class ScoringCommand extends AbstractCommand {

    private final static int MAX_KEYS = 100;

    public ScoringCommand()throws IllegalArgumentException{
        super("scoring", "Item scoring");
        try{
            PairedOptionList dataInfo = new PairedOptionList("data", "Data information", true);
            dataInfo.add("db", OptionValueType.STRING);
            dataInfo.add("table", OptionValueType.STRING);
            this.addPairedOptionList(dataInfo);

            /**
             * The format of these Strings is itemName followed by the option score key (see DefaultItemScoring.java
             * for plain English details on the format). The REGEX used here includes an extra group for the item name.
             * The REGEX used in DefaultItemScoring.java does not include a group for the item name.
             * String REGEX1 = "\\s*\\(([\\w,\\s+]+(?=,|\\)|\\s+))\\)";
             * String REGEX2 = "\\s*\\((.+?(?=,|\\)|[null]))\\)";
             * String REGEX3 = "\\s*\\(([[-+]?[0-9]*\\.?[0-9]+(?=,|\\)|[null])]+?)\\)";
             * String REGEX = REGEX1+REGEX2+REGEX3;
             * The first group (REGEX1) is the item name or a comma delimited list of item names that share the same scoring,
             * The second group (REGEX2) is the original value list.
             * The third group (REGEX3) is the score value list it can only contain numbers.
             *
             * The keyword "null" should be used to clear the item scoring
             *
             * Note: If multiple lines are used, there MUST be a space at the end of each line!
             *
             * Example:
             * scoring{
             *      data = (path@DefaultWorkspace db@mydb table@math);
             *      key = ((Var2, Var3, Var4)(0,1)(0,1)
             *             (Var5)(0,1)(1,1)
             *             (Var12)(null)(null));
             * }
             *
             * Example:
             * scoring{
             *      data = (path@DefaultWorkspace db@mtdata table@POLYSIM2);
             *      key = ((V1,V2,V3,V4,V5,V6,V7,V8,V9,V10)(0,1,2,3)(0,1,2,3));
             * }
             *
             *
             *
             */
            FreeOption key = new FreeOption("key", "Scoring of imported variables", false, OptionValueType.STRING);
            this.addFreeOption(key);

            FreeOption numberKeys = new FreeOption("keys", "Number of score keys in command - max is 100", true, OptionValueType.INTEGER);
            this.addFreeOption(numberKeys);

            String base = "key";
            PairedOptionList scoring = null;
            for(int i=0;i<MAX_KEYS;i++){
                scoring = new PairedOptionList(base+(i+1), "Scoring of variables", false);
                scoring.addArgument("options", "Item options", OptionValueType.STRING);
                scoring.addArgument("scores", "Option scores", OptionValueType.STRING);
                scoring.addArgument("variables", "Item names", OptionValueType.STRING);
                scoring.addArgument("omit", "Omitted response code", OptionValueType.STRING);
                scoring.addArgument("nr", "Not reached response code", OptionValueType.STRING);
                this.addPairedOptionList(scoring);
            }

        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
    }

}

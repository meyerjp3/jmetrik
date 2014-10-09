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
package com.itemanalysis.jmetrik.commandbuilder;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class MegaOptionParserTest {

    @Test
    public void listParseTest1(){

        //Define the option
        MegaOption option = new MegaOption("variables", "Variable names", OptionType.FREE_LIST_OPTION, false);

        //text representation of the option
        String text = "variables(var1,var2,var3,var4)";

        //parse the text into option values
        MegaOptionParser optionParser = new MegaOptionParser();
        MegaOption newOpt = optionParser.parse(text, option);

        System.out.println("Testing option: " + newOpt.paste());

        String[] defaultValues = {"novar"};
        String[] values = newOpt.getValues(defaultValues);

        assertEquals("ArrayLength test: ", 4, values.length);
        assertEquals("Array value test: ", "var1", values[0]);
        assertEquals("Array value test: ", "var2", values[1]);
        assertEquals("Array value test: ", "var3", values[2]);
        assertEquals("Array value test: ", "var4", values[3]);

    }

    @Test
    public void namedListParseTest1(){

        //Define the option
        MegaOption option = new MegaOption("key", "Variable names", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("variables", "Variables to be scored", true);
        option.addArgument("options", "Response option values", true);
        option.addArgument("scores", "Response option scores", true);

        //text representation of the option
        String text = "key(variables = (var1,var2,var3,var4), options = (A,B,C,D), scores = (0,1,0,0))";

        //parse the text into the option and argument values
        MegaOptionParser optionParser = new MegaOptionParser();
        MegaOption newOpt = optionParser.parse(text, option);

        System.out.println("Testing option: " + newOpt.paste());

        String[] defaultValues = {"noval"};
        String[] variables = newOpt.getValuesAt("variables", defaultValues);
        assertEquals("Variables test: ", "var1", variables[0]);
        assertEquals("Variables test: ", "var2", variables[1]);
        assertEquals("Variables test: ", "var3", variables[2]);
        assertEquals("Variables test: ", "var4", variables[3]);

        String[] options = newOpt.getValuesAt("options", defaultValues);
        assertEquals("Variables test: ", "A", options[0]);
        assertEquals("Variables test: ", "B", options[1]);
        assertEquals("Variables test: ", "C", options[2]);
        assertEquals("Variables test: ", "D", options[3]);

        String[] scores = newOpt.getValuesAt("scores", defaultValues);
        assertEquals("Variables test: ", "0", scores[0]);
        assertEquals("Variables test: ", "1", scores[1]);
        assertEquals("Variables test: ", "0", scores[2]);
        assertEquals("Variables test: ", "0", scores[3]);


    }


}

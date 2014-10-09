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

import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MegaOptionTest {



    @Test
    public void optionDisplayTest(){

        MegaOption option = new MegaOption("data", "Database information", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("db", "Target database", true);
        option.addArgument("table", "Database table name", true);
//        System.out.println(option.paste());

        option = new MegaOption("variables", "Variable names", OptionType.SELECT_ALL_OPTION, false);
        option.addValue("var1");
        option.addValue("var2");
        option.addValue("var3");
        option.addValue("var4");
//        System.out.println(option.paste());

        option = new MegaOption("key", "Variable names", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("variables", "Name of variables to be scored", true);
        option.addArgument("options", "Response option values", true);
        option.addArgument("scores", "Response option scores", true);
        option.addValueAt("variables", "var1");
        option.addValueAt("variables", "var2");
        option.addValueAt("variables", "var3");
        option.addValueAt("variables", "var4");

        option.addValueAt("options", "A");
        option.addValueAt("options", "B");
        option.addValueAt("options", "C");
        option.addValueAt("options", "D");

        option.addValueAt("scores", "0");
        option.addValueAt("scores", "1");
        option.addValueAt("scores", "0");
        option.addValueAt("scores", "0");

        System.out.println(option.paste());


    }

    @Test
    public void helpTextTest(){

        MegaOption option = new MegaOption("data",
                "This option provide information about the database such as the database name " +
                        "and teh name of teh database table. It is used in almost every command in jMetrik. " +
                        "If the database name or table name is incorrect, jMetrik will throw a SQLException. " +
                        "You must then correct the error and run the command again. Somtimes it is hard to " +
                        "find the error. Look carefully at the option. It is case sensitive.",
                OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("db", "Target database", true);
        option.addArgument("table", "Database table name", true);
        option.addArgument("nodescription");
        System.out.println(option.getHelpText());

        option = new MegaOption("data",
                "This option has a shiorter description",
                OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("variables", "List of selected variables", true);
        option.addArgument("output", "Display output", true);
        option.addArgument("nodescription");
        System.out.println(option.getHelpText());

    }

    @Test
    public void optionValueListTest(){

        MegaOption option = new MegaOption("variables", "Variable names", OptionType.FREE_LIST_OPTION, false);
        option.addValue("var1");
        option.addValue("var2");
        option.addValue("var3");
        option.addValue("var4");

        System.out.println("Testing option: " + option.paste());

        String[] defaultValues = {"novar"};
        String[] values = option.getValues(defaultValues);

        assertEquals("ArrayLength test: ", 4, values.length);
        assertEquals("Array value test: ", "var1", values[0]);
        assertEquals("Array value test: ", "var2", values[1]);
        assertEquals("Array value test: ", "var3", values[2]);
        assertEquals("Array value test: ", "var4", values[3]);

    }

    @Test
    public void optionNamedValueListTest(){

        MegaOption option = new MegaOption("key", "Variable names", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("variables", "Name of variables to be scored", true);
        option.addArgument("options", "Response option values", true);
        option.addArgument("scores", "Response option scores", true);

        option.addValueAt("variables", "var1");
        option.addValueAt("variables", "var2");
        option.addValueAt("variables", "var3");
        option.addValueAt("variables", "var4");

        option.addValueAt("options", "A");
        option.addValueAt("options", "B");
        option.addValueAt("options", "C");
        option.addValueAt("options", "D");

        option.addValueAt("scores", "0");
        option.addValueAt("scores", "1");
        option.addValueAt("scores", "0");
        option.addValueAt("scores", "0");

        System.out.println("Testing option: " + option.paste());

        String[] defaultValues = {"noval"};
        String[] variables = option.getValuesAt("variables", defaultValues);
        assertEquals("Variables test: ", "var1", variables[0]);
        assertEquals("Variables test: ", "var2", variables[1]);
        assertEquals("Variables test: ", "var3", variables[2]);
        assertEquals("Variables test: ", "var4", variables[3]);

        String[] options = option.getValuesAt("options", defaultValues);
        assertEquals("Variables test: ", "A", options[0]);
        assertEquals("Variables test: ", "B", options[1]);
        assertEquals("Variables test: ", "C", options[2]);
        assertEquals("Variables test: ", "D", options[3]);

        String[] scores = option.getValuesAt("scores", defaultValues);
        assertEquals("Variables test: ", "0", scores[0]);
        assertEquals("Variables test: ", "1", scores[1]);
        assertEquals("Variables test: ", "0", scores[2]);
        assertEquals("Variables test: ", "0", scores[3]);


    }

    @Test (expected=IllegalArgumentException.class)
     public void selectOneExceptionTest(){

        MegaOption option = new MegaOption("method", "Linking method", OptionType.SELECT_ONE_OPTION, false);

        SelectFromListValueChecker listChecker = new SelectFromListValueChecker();
        listChecker.addPermittedValue("sl");
        listChecker.addPermittedValue("hb");
        listChecker.addPermittedValue("mm");
        listChecker.addPermittedValue("ms");

        option.setValueChecker(listChecker);

        //should throw an exception because not a permitted value
        option.addValue("slp");
    }

    @Test
    public void selectOneLastSelectedValueTest(){

        MegaOption option = new MegaOption("method", "Linking method", OptionType.SELECT_ONE_OPTION, false);

        SelectFromListValueChecker listChecker = new SelectFromListValueChecker();
        listChecker.addPermittedValue("sl");
        listChecker.addPermittedValue("hb");
        listChecker.addPermittedValue("mm");
        listChecker.addPermittedValue("ms");

        option.setValueChecker(listChecker);

        option.addValue("sl");
        option.addValue("hb");

        //Adding multiple values to a select one option should only retain teh last value added
        assertTrue("hb".equals(option.getValue("mm")));
        assertFalse("sl".equals(option.getValue("mm")));

        //Should only have one value in the list because it is a select one option
        assertEquals(1, option.getNumberOfValues());

    }

    @Test (expected=IllegalArgumentException.class)
    public void numericLowerBoundTest(){

        MegaOption option = new MegaOption("number", "A numeric option", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("lower", "Lower bound", false);
        option.addArgument("upper", "Upper bound", false);

        NumericValueChecker numberChecker = new NumericValueChecker();
        numberChecker.addBoundsAt("lower", new ValueBounds(0,1));
        numberChecker.addBoundsAt("upper", new ValueBounds(10,12.3));

        option.setValueChecker(numberChecker);

        System.out.println(option.getHelpText());

        //should throw an exception because outside lower bound
        option.addValueAt("lower", "-0.1");
    }

    @Test (expected=IllegalArgumentException.class)
    public void numericUpperBoundTest(){

        MegaOption option = new MegaOption("number", "A numeric option", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("lower", "Lower bound", false);
        option.addArgument("upper", "Upper bound", false);

        NumericValueChecker numberChecker = new NumericValueChecker();
        numberChecker.addBoundsAt("lower", new ValueBounds(0,1));
        numberChecker.addBoundsAt("upper", new ValueBounds(10,12.3));

        option.setValueChecker(numberChecker);

        //should throw an exception because outside lower bound
        option.addValueAt("upper", "12.31");
    }

    @Test (expected=IllegalArgumentException.class)
    public void numericDefaultValueTest(){

        MegaOption option = new MegaOption("number", "A numeric option", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("lower", "Lower bound", false);
        option.addArgument("upper", "Upper bound", false);

        NumericValueChecker numberChecker = new NumericValueChecker();
        numberChecker.addBoundsAt("lower", new ValueBounds(0,1));
        numberChecker.addBoundsAt("upper", new ValueBounds(10,12.3));

        option.setValueChecker(numberChecker);
        option.addValueAt("lower", "0.5");

        //should throw an exception because the default value is out of range.
        option.getValueAt("lower", "12");

    }

    @Test
    public void numericWithinBoundTest(){

        MegaOption option = new MegaOption("number", "A numeric option", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("lower", "Lower bound", false);
        option.addArgument("upper", "Upper bound", false);

        NumericValueChecker numberChecker = new NumericValueChecker();
        numberChecker.addBoundsAt("lower", new ValueBounds(0,1));
        numberChecker.addBoundsAt("upper", new ValueBounds(10,12.3));

        option.setValueChecker(numberChecker);

        //should throw an exception because outside lower bound
        option.addValueAt("upper", "12.1");

        assertEquals("Bounds test: ", "12.1", option.getValueAt("upper", "10"));
    }

    @Test
    public void selectAllTest(){

        MegaOption option = new MegaOption("method", "Linking method", OptionType.SELECT_ALL_OPTION, false);

        SelectFromListValueChecker listChecker = new SelectFromListValueChecker();
        listChecker.addPermittedValue("sl");
        listChecker.addPermittedValue("hb");
        listChecker.addPermittedValue("mm");
        listChecker.addPermittedValue("ms");

        option.setValueChecker(listChecker);

        option.addValue("sl");
        option.addValue("hb");

        System.out.println(option.paste());

        String[] v = option.getValues(new String[] {"sl"});

        assertEquals("sl", v[0]);
        assertEquals("hb", v[1]);


    }

    @Test (expected=IllegalArgumentException.class)
    public void selectAllTestFail(){

        MegaOption option = new MegaOption("method", "Linking method", OptionType.SELECT_ALL_OPTION, false);
        SelectFromListValueChecker listChecker = new SelectFromListValueChecker();
        listChecker.addPermittedValue("sl");
        listChecker.addPermittedValue("hb");
        listChecker.addPermittedValue("mm");
        listChecker.addPermittedValue("ms");

        option.setValueChecker(listChecker);

        option.addValue("sl");
        option.addValue("hb2");//should throw an exception

        System.out.println(option.paste());

        String[] v = option.getValues(new String[] {"sl"});

        assertEquals("sl", v[0]);
        assertEquals("hb", v[1]);


    }




}

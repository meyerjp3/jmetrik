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

package com.itemanalysis.jmetrik.commandbuilder;

import java.util.ArrayList;

/**
 * The commandbuilder package is designed to allow users to define complex
 * commands. The three key constructs are COMMAND, ARGUMENT, and VALUE.
 * A Command object is composed of Arguments that take on different values.
 * An argument can take on only one type of value (i.e. String, Double, or Integer).
 * The different types of arguments are:
 *
 * 1. SelectOne - an argument that allows the choice of one and only one value.
 * The possible values are predefined. This type of argument is like a radio button.
 *
 * 2. SelectAll - an argument that allows for zero or more values to be selected.
 * The possible values are predefined. This type of argument is like a check box.
 *
 * 3. SingularValueList - an argument that has no predefined value. This
 * type of argument is like a textbox. The user can input any information,
 * but only value value is allowed.
 *
 * 3. ValueList - an argument that has no predefined values. This type
 * of argument is like a text box. The user can input any information
 * as a space delimited list. Multiple values are allowed.
 *
 * 4. NamedValueList - like a value list only that each value has a name.
 * Specific values may be ontained from the list by name. Named values
 * should be entered as a space delimited list. Multiple values are allowed.
 *
 *
 */
public interface Command {

    public String getName();

    public void addSelectOneOption(SelectOneOption optionName)throws IllegalArgumentException;

    public void addSelectAllOption(SelectAllOption optionName)throws IllegalArgumentException;

    public void addFreeOption(FreeOption optionName) throws IllegalArgumentException;

    public void addFreeOptionList(FreeOptionList optionName)throws IllegalArgumentException;

    public void addPairedOptionList(PairedOptionList optionName)throws IllegalArgumentException;

    public void addRepeatedOption(RepeatedPairedOptionList optionName) throws IllegalArgumentException;

    public void removeOption(String optionName);

    public SelectOneOption getSelectOneOption(String optionName)throws IllegalArgumentException;

    public SelectAllOption getSelectAllOption(String optionName)throws IllegalArgumentException;

    public FreeOption getFreeOption(String optionName)throws IllegalArgumentException;

    public FreeOptionList getFreeOptionList(String optionName)throws IllegalArgumentException;

    public PairedOptionList getPairedOptionList(String optionName)throws IllegalArgumentException;

    public ArrayList<String> getRequiredOptions();

    public RepeatedPairedOptionList getRepeatedOption(String optionName)throws IllegalArgumentException;

    /**
     * @return String representation of this Command.
     */
    public String getHelpText();

    /**
     * Splits a string representation of a command into its member
     * arguments. Each argument is also split in turn.
     *
     * @param cmdString String representation of the Command
     */
    public void split(String cmdString)throws IllegalArgumentException;

    public String paste()throws IllegalArgumentException;

}


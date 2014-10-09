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

/**
 * Option.java is the most general interface for arguments defined in
 * the CommandBuilder package. Each argument can hold only one type
 * of data. The types are defined in ValueType.java.
 *
 *
 */
public interface Option {

    public String getOptionName();

    public String getOptionDescription();

    public String getArgumentDescription(String argName);

    public boolean isRequired();

    public boolean hasValue();

    public OptionType getOptionType();

    public int getNumberOfValues();

    public String getHelpText();

    public void split(String line) throws IllegalArgumentException;

    public String paste() throws IllegalArgumentException;

}


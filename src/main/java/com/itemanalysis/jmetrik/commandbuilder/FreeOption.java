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

import java.util.Formatter;

public class FreeOption extends AbstractOption{

    private String stringValue = null;

    private Double doubleValue = null;

    private Integer intValue = null;

    public FreeOption(String name, OptionValueType type){
        this(name, "", false, type);
    }

    public FreeOption(String optionName, String optionDescription, OptionValueType optionValueType){
        this(optionName, optionDescription, false, optionValueType);
    }

    public FreeOption(String optionName, String optionDescription, boolean required, OptionValueType optionValueType){
        this.optionName = optionName;
        this.optionDescription = optionDescription;
        this.required = required;
        this.optionValueType = optionValueType;
        this.optionType = OptionType.FREE_OPTION;
    }

    public boolean hasValue(){
        switch(optionValueType){
            case STRING: return stringValue!=null;
            case INTEGER: return intValue!=null;
            case DOUBLE: return doubleValue!=null;
        }
        return false;
    }

    public int getNumberOfValues(){
        if(hasValue()) return 1;
        return 0;
    }

    public void add(String text)throws IllegalArgumentException{
        String temp = text.replaceAll("[\\n\\r]", "").trim();//eliminate carriage returns and line feeds

        try{
            if(optionValueType== OptionValueType.DOUBLE){
                doubleValue = Double.parseDouble(temp);
            }else if(optionValueType== OptionValueType.INTEGER){
                intValue = Integer.parseInt(temp);
            }else{
                stringValue = temp;
            }
        }catch(ClassCastException ex){
            throw new IllegalArgumentException("Data type mismatch for " + optionName);
        }
    }

    public void add(Double value){
        if(optionValueType!= OptionValueType.DOUBLE) throw new IllegalArgumentException("Data type mismatch");
        doubleValue = value;
    }

    public void add(Integer value)throws IllegalArgumentException{
        if(optionValueType!= OptionValueType.INTEGER) throw new IllegalArgumentException("Data type mismatch");
        intValue = value;
    }

    public String getString(){
        return stringValue;
    }

    public Double getDouble(){
        return doubleValue;
    }

    public Integer getInteger(){
        return intValue;
    }

    public void clear(){
        stringValue = null;
        doubleValue = null;
        intValue = null;
    }

    public String paste(){
        String list = optionName + "(";
        if(optionValueType== OptionValueType.DOUBLE){
            list += doubleValue;
        }else if(optionValueType== OptionValueType.INTEGER){
            list += intValue;
        }else{
            list += stringValue;
        }
        list += ");";
        return list;
    }

    /**
     * Processes user input of the form:
     *
     * option(arg1);
     *
     * All of the values must be of the same type such as all string or all double.
     *
     * @param line
     * @return
     * @throws IllegalArgumentException
     */
    public void split(String line)throws IllegalArgumentException{
        String text = line.replaceAll("[\\n\\r]", "");//eliminate carriage returns and line feeds
        int first = text.indexOf("(");
        int last = text.lastIndexOf(")");
        if(first==-1 || last==-1) throw new IllegalArgumentException("Missing openning or closing parentheses for " + optionName);

        String opName = text.trim().substring(0, first);
        String opValue = text.trim().substring(first+1, last);

        try{
            if(opName.equals(optionName)){
                this.add(opValue);
            }
        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
    }

    public String getHelpText(){
        Formatter f = new Formatter();
        String n = "<" + optionName + ">";
        f.format("%5s", ""); f.format("%-20s", "Argument: "); f.format("%-100s", n + " " + optionDescription); f.format("%n");
        f.format("%5s", ""); f.format("%-20s", "Values: "); f.format("%-100s", "[LIST: " + optionValueType + "]");f.format("%n");
        f.format("%5s", ""); f.format("%-20s", "Required: ");
        if(required){
            f.format("%-3s", "Yes");
        }else{
            f.format("%-2s", "No");
        }
        f.format("%5s", "");
        f.format("%n");
        return f.toString();
    }


    @Override
    public String toString(){
        return optionName;
    }

    @Override
    public boolean equals(Object o){
		if(!(o instanceof Option)) return false;
		if(o==this) return true;
		Option arg = (Option)o;
		if(arg.toString().equals(this.toString())) return true;
		return false;
	}

    @Override
	public int hashCode(){
		return optionName.hashCode();
	}

}


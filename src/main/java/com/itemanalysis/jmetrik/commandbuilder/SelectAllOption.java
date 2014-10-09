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
import java.util.HashMap;
import java.util.Iterator;

public class SelectAllOption extends AbstractOption{

    private HashMap<String, Boolean> argument = null;

    private final static int ARG_NAME_LENGTH = 15;

    private final static int ARG_DESCRIPTION_LENGTH = 100;

    public SelectAllOption(String optionName, String optionDescription, boolean required){
        this.optionName = optionName;
        this.optionDescription = optionDescription;
        this.required = required;
        this.optionType = OptionType.SELECT_ALL_OPTION;
        argument = new HashMap<String, Boolean>();
    }

    public int getNumberOfValues(){
        return argument.size();
    }

    public boolean hasValue(){
        return argument !=null && !argument.isEmpty();
    }

    public boolean isRequired(){
        return required;
    }

    /**
     * addArgument values.
     *
     * @param argumentName
     */
    public void add(String argumentName, Boolean selected){
        addArgument(argumentName, "", selected);
    }

    public void addArgument(String argumentName, String argumentDescription, Boolean selected){
        if(!argumentName.trim().equals("")){
            this.argument.put(argumentName, selected);
            this.argumentDescription.put(argumentName, argumentDescription);
        }

    }

    public void setSelected(String argumentName, boolean value)throws IllegalArgumentException{
        if(argument.get(argumentName)!=null){
            argument.put(argumentName, Boolean.valueOf(value));
        }else{
            throw new IllegalArgumentException("Argument [" + argument +"] not found in " + optionName + ".");
        }
    }

    public void setNoneSelected(){
        for(String s : argument.keySet()){
            argument.put(s, Boolean.FALSE);
        }
    }

    public boolean isArgumentSelected(String value){
        if(argument.get(value)!=null){
            return argument.get(value);
        }else{
            throw new IllegalArgumentException("Argument [" + argument +"] not found in " + optionName + ".");
        }
    }

    public void clear(){
        argument.clear();
        argumentDescription.clear();
    }

    /**
     * Reads user supplied string and populates the argument. The string must have the form:
     *
     * argument(arg1, arg2); when multiple options are selected
     * argument(arg1); when a single argument is selected
     * argument(); when no options are selected
     *
     * @param line
     */
    public void split(String line)throws IllegalArgumentException{
        String text = line.replaceAll("[\\n\\r]", "");//eliminate carriage returns and line feeds
        int first = text.indexOf("(");
        int last = text.lastIndexOf(")");
        if(first==-1 || last==-1) throw new IllegalArgumentException("Missing opening or closing parentheses for " + optionName);

        String opName = text.trim().substring(0, first);
        String opValue = text.trim().substring(first+1, last);
        String[] arg = opValue.split(",");
        if(opName.equals(this.optionName)){
            setNoneSelected();
            try{
                for(String s : arg){
                    if(!s.trim().equals("")) setSelected(s.trim(), true);
                }
            }catch(IllegalArgumentException ex){
                throw new IllegalArgumentException(ex);
            }
        }
    }

    /**
     * Returns a string formatted as follows:
     *
     * argument(arg1, arg2); when arg1 and arg2 are selected
     * options(arg1); when only arg1 is selected
     * argument(); when nothing is selected
     *
     * @return String
     */
    public String paste()throws IllegalArgumentException{
        String argString = this.optionName + "(";
        if(argument.isEmpty()) throw new IllegalArgumentException("No options found for " + optionName);

        for(String s : argument.keySet()){
            if(argument.get(s)==null) throw new IllegalArgumentException("Argument not found");
            if(argument.get(s)){
                argString += s;
                argString+=", ";
            }
        }

        if(argString.trim().endsWith(",")){
            argString = argString.substring(0, argString.lastIndexOf(","));
        }
        return argString += ");";
    }

    public String getHelpText(){
        Formatter f = new Formatter();
        String n = "<" + optionName + ">";

        f.format("%5s", ""); f.format("%-20s", "Argument: "); f.format("%-100s", n + " " + optionDescription); f.format("%n");
        f.format("%5s", ""); f.format("%-20s", "Values: ");
        f.format("%1s", "[");


        Iterator<String> iter = argument.keySet().iterator();
        while(iter.hasNext()){
            String temp = iter.next();
            f.format("%"+temp.toString().length()+"s", temp.toString());
            if(iter.hasNext()){
                f.format("%3s", " | ");
            }else{
                 f.format("%1s", "]");
            }
        }
        f.format("%n");

        iter = argumentDescription.keySet().iterator();
        while(iter.hasNext()){
            String v = iter.next();
            String temp = argumentDescription.get(v);
            int l = v.toString().length() + temp.length() + 3;
            f.format("%5s", ""); f.format("%-20s", "");
            f.format("%"+l+"s", v.toString() + " = " + temp);
            f.format("%n");
        }

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


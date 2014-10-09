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

public class SelectOneOption extends AbstractOption{

    private HashMap<String, Boolean> argument = null;

    public final static int ARG_NAME_LENGTH = 15;

    public final static int ARG_DESCRIPTION_LENGTH = 100;

    Formatter f = null;

    public SelectOneOption(String optionName, String optionDescription){
        this(optionName, optionDescription, false);
    }

    public SelectOneOption(String optionName, String optionDescription, boolean required){
        this.optionName = optionName;
        this.optionDescription = optionDescription;
        this.required = required;
        this.optionType = OptionType.SELECT_ONE_OPTION;
        argument = new HashMap<String, Boolean>();
    }

    public String getSelectedArgument(){
        for(String s : argument.keySet()){
            if(argument.get(s)==Boolean.TRUE) return s;
        }
        return "";//should never be returned. There is always a selected value.
    }

    /**
     * addArgument values. Last value added is set to selected.
     *
     * @param arg
     */
    public void addArgument(String arg){
        addArgument(arg, "");
    }

    public void addArgument(String arg, String description){
        for(String s : argument.keySet()){
            argument.put(s, Boolean.FALSE);
        }
        argument.put(arg, Boolean.TRUE);
        argumentDescription.put(arg, description);
    }

    /**
     * One value must always be true (i.e. selected).
     *
     * @param arg
     * @throws IllegalArgumentException
     */
    public void setSelected(String arg)throws IllegalArgumentException{
        if(argument.get(arg)!=null){
            for(String s : argument.keySet()){
                argument.put(s, Boolean.FALSE);
            }
            argument.put(arg, Boolean.TRUE);
        }else{
            throw new IllegalArgumentException("Argument [" + argument +"] not found in " + optionName + ".");
        }
    }

    public boolean isValueSelected(String value){
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

    public String paste()throws IllegalArgumentException{
        String output = optionName + "(";
        for(String s : argument.keySet()){
            if(argument.get(s)) output += s;
        }
        output += ");";
        return output;
    }

    /**
     * This method takes user input and splits it into the parts needed for processing the argument and command.
     *
     * @param line must have the form: argument(arg)
     * @return
     * @throws IllegalArgumentException
     */
    public void split(String line)throws IllegalArgumentException{
        int first = line.indexOf("(");
        int last = line.lastIndexOf(")");
        if(first==-1 || last==-1) throw new IllegalArgumentException("Missing opening or closing parentheses for " + optionName);

        String opName = line.trim().substring(0, first);
        String opValue = line.trim().substring(first+1, last);

        if(opName.equals(this.optionName)){
            try{
                setSelected(opValue.trim());
            }catch(IllegalArgumentException ex){
                throw new IllegalArgumentException(ex);
            }
        }
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

    public String getHelpText(){
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


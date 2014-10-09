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

public class PairedOptionList extends AbstractOption{

    private HashMap<String, String> option = null;

    private HashMap<String, OptionValueType> typeMap = null;

    private HashMap<String, String> stringMap = null;

    private HashMap<String, Double> doubleMap = null;

    private HashMap<String, Integer> intMap = null;

    public PairedOptionList(String optionName, String optionDescription){
        this(optionName, optionDescription, false);
    }

    public PairedOptionList(String optionName, String optionDescription, boolean required){
        this.optionName = optionName;
        this.optionDescription = optionDescription;
        this.required = required;
        this.optionType = OptionType.ARGUMENT_VALUE_OPTION_LIST;
        option = new HashMap<String, String>();
        typeMap = new HashMap<String, OptionValueType>();
        stringMap = new HashMap<String, String>();
        doubleMap = new HashMap<String, Double>();
        intMap = new HashMap<String, Integer>();
    }

    public int getNumberOfValues(){
        return option.size();
    }

    /**
     * Add required arguments and argument types. This method is used for defining the list.
     *
     * @param argumentName
     * @param optionValueType
     */
    public void add(String argumentName, OptionValueType optionValueType){
        addArgument(argumentName, "", optionValueType);
    }

    public void addArgument(String argumentName, String argumentDescription, OptionValueType optionValueType){
        typeMap.put(argumentName, optionValueType);
        option.put(argumentName, argumentDescription);
    }

    /**
     * This method is used for processing user input
     *
     * @param argumentName
     * @param value
     */
    public void addValue(String argumentName, String value)throws IllegalArgumentException{
        if(option.containsKey(argumentName)){
            try{
                OptionValueType type = typeMap.get(argumentName);
                switch(type){
                    case STRING: stringMap.put(argumentName, value); break;
                    case DOUBLE: doubleMap.put(argumentName, Double.parseDouble(value)); break;
                    case INTEGER: intMap.put(argumentName, Integer.parseInt(value)); break;
                }
            }catch(ClassCastException ex){
                throw new IllegalArgumentException("Type mismatch for argument [" + argumentName + "] in "   + optionName + ".");
            }

        }
    }

    public void addValue(String argumentName, Double value) throws IllegalArgumentException{
        if(option.containsKey(argumentName)){
            if(typeMap.get(argumentName)!= OptionValueType.DOUBLE) {
                throw new IllegalArgumentException("Type mismatch for argument [" + argumentName + "] in "   + optionName + ".");
            }
            try{
                doubleMap.put(argumentName, value);
            }catch(ClassCastException ex){
                throw new IllegalArgumentException("Type mismatch for argument [" + argumentName + "] in "   + optionName + ".");
            }
        }
    }

    public void addValue(String argumentName, Integer value) throws IllegalArgumentException{
        if(option.containsKey(argumentName)){
            if(typeMap.get(argumentName)!= OptionValueType.INTEGER) {
                throw new IllegalArgumentException("Type mismatch for argument [" + argumentName + "] in "   + optionName + ".");
            }
            try{
                intMap.put(argumentName, value);
            }catch(ClassCastException ex){
                throw new IllegalArgumentException("Type mismatch for argument [" + argumentName + "] in "   + optionName + ".");
            }
        }
    }

    public String getStringAt(String argumentName)throws IllegalArgumentException{
        if(option.containsKey(argumentName)){
            if(typeMap.get(argumentName)!= OptionValueType.STRING) {
                throw new IllegalArgumentException("Argument [" + argumentName + "] is not of type String");
            }
            if(stringMap.get(argumentName)!=null){
                return stringMap.get(argumentName);
            }else{
                return null;
            }
        }else{
            throw new IllegalArgumentException("Argument not found");
        }
    }

    public Double getDoubleAt(String argumentName)throws IllegalArgumentException{
        if(option.containsKey(argumentName)){
            if(typeMap.get(argumentName)!= OptionValueType.DOUBLE){
                throw new IllegalArgumentException("Argument [" + argumentName + "] is not of type Double");
            }
            if(doubleMap.get(argumentName)!=null){
                return doubleMap.get(argumentName);
            }else{
                return null;
            }
        }else{
            throw new IllegalArgumentException("Argument not found");
        }
    }

    public Integer getIntegerAt(String argumentName)throws IllegalArgumentException{
        if(option.containsKey(argumentName)){
            if(typeMap.get(argumentName)!= OptionValueType.INTEGER){
                throw new IllegalArgumentException("Argument [" + argumentName + "] is not of type Integer");
            }
            if(intMap.get(argumentName)!=null){
                return intMap.get(argumentName);
            }else{
                return null;
            }
        }else{
            throw new IllegalArgumentException("Argument not found");
        }
    }

    public boolean hasAllArguments(){
        int rCount = option.size();
        int count = 0;
        for(String s : option.keySet()){
            if(typeMap.get(s)== OptionValueType.STRING && stringMap.get(s)!=null) count++;
            if(typeMap.get(s)== OptionValueType.DOUBLE && doubleMap.get(s)!=null) count++;
            if(typeMap.get(s)== OptionValueType.INTEGER && doubleMap.get(s)!=null) count++;
        }
        return count==rCount;
    }

    public void clear(){
        for(String s : option.keySet()){
            if(typeMap.get(s)== OptionValueType.STRING) stringMap.clear();
            if(typeMap.get(s)== OptionValueType.DOUBLE) doubleMap.clear();
            if(typeMap.get(s)== OptionValueType.INTEGER) intMap.clear();
        }
        typeMap.clear();
        option.clear();
    }

    public boolean hasValue(){
        int count = 0;
        for(String s : option.keySet()){
            OptionValueType t = typeMap.get(s);
            if(t== OptionValueType.DOUBLE){
                count+=doubleMap.size();
            }else if(t== OptionValueType.INTEGER){
                count+=intMap.size();
            }else{
                count+=stringMap.size();
            }
        }
        return count>0;
    }

    public String getHelpText(){
        Formatter f = new Formatter();
        String n = "<" + optionName + ">";
        f.format("%5s", ""); f.format("%-20s", "Argument: "); f.format("%-100s", n + " " + optionDescription); f.format("%n");
        if(hasValue()){
            f.format("%5s", ""); f.format("%-20s", "Named index: ");
            f.format("%1s", "[");

            Iterator<String> iter = option.keySet().iterator();
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

    /**
     * Processes user input formnatted as follows:
     *
     * option(arg1=value1, arg2 = value2, arg2=  value3); when there are multiple arg-value pairs
     * option(arg1=value1); when there is a single arg-value pair
     *
     *
     * @param line
     * @return
     * @throws IllegalArgumentException
     */
    public void split(String line)throws IllegalArgumentException{
        String text = line.replaceAll("[\\n\\r]", "");//eliminate carriage returns and line feeds
        text = text.trim();
        int first = text.indexOf("(");
        int last = text.lastIndexOf(")");
        if(first==-1 || last==-1) throw new IllegalArgumentException("Missing opening or closing parentheses for " + optionName);

        String opName = text.substring(0, first);
        String opValue = text.substring(first+1, last);
//        String[] arg = opValue.split(",");
        String REGEX = ",(?![^()]*+\\))";//split only on commas not contained in parentheses
        String[] arg = opValue.split(REGEX);
        String[] pair = null;

        if(opName.equals(optionName)){
            for(String s : arg){
                if(s.indexOf("=")==-1)  throw new IllegalArgumentException("Invalid argument: " + s);
                pair = s.trim().split("=");
                pair[0] = pair[0].trim();
                pair[1] = pair[1].trim();
                if(option.get(pair[0])==null) throw new IllegalArgumentException("Argument not found: " + pair[0]);
                if(option.keySet().contains(pair[0])){
                    try{
                        addValue(pair[0], pair[1]);
                    }catch(IllegalArgumentException ex){
                        throw new IllegalArgumentException(ex);
                    }
                }
            }
        }

    }

    public String paste(){
        String argString = optionName + "(";
        OptionValueType t = null;

        if(!hasValue()){
            //no optional values selected
            return "";
        }else{
            Iterator<String> iter = option.keySet().iterator();
            String ni = "";
            while(iter.hasNext()){
                ni = iter.next();
                t = typeMap.get(ni);
                argString += ni + " = ";
                if(t== OptionValueType.DOUBLE){
                    argString += doubleMap.get(ni);
                }else if(t== OptionValueType.INTEGER){
                    argString += intMap.get(ni);
                }else{
                    argString += stringMap.get(ni);
                }
                if(iter.hasNext()) argString += ", ";
            }
            argString += ");";
        }
        return argString;

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


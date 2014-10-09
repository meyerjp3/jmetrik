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
import java.util.Formatter;
import java.util.Iterator;

public class FreeOptionList extends AbstractOption{

    private OptionValueType type = OptionValueType.STRING;

    private ArrayList<String> stringList = null;

    private ArrayList<Double> doubleList = null;

    private ArrayList<Integer> intList = null;

    public FreeOptionList(String name, OptionValueType type){
        this(name, "", false, type);
    }

    public FreeOptionList(String optionName, String optionDescription, OptionValueType type){
        this(optionName, optionDescription, false, type);
    }

    public FreeOptionList(String optionName, String optionDescription, boolean required, OptionValueType type){
        this.optionName = optionName;
        this.optionDescription = optionDescription;
        this.required = required;
        this.type = type;
        this.optionType = OptionType.FREE_LIST_OPTION;

        if(type== OptionValueType.DOUBLE){
            doubleList = new ArrayList<Double>();
        }else if(type == OptionValueType.INTEGER){
            intList = new ArrayList<Integer>();
        }else{
            stringList = new ArrayList<String>();
        }

    }

    public boolean hasValue(){
        switch(type){
            case STRING: return stringList.size()>0;
            case INTEGER: return intList.size()>0;
            case DOUBLE: return doubleList.size()>0;
        }
        return false;
    }

    public int getNumberOfValues(){
        switch(type){
            case STRING: return stringList.size();
            case INTEGER: return intList.size();
            case DOUBLE: return doubleList.size();
        }
        return -1;
    }

    /**
     * Add values to this option in the form of a single value or a comma delimited list of values.
     * The values must all be of the same type as indicated by type variable.
     *
     * This method is used for processing user supplied input.
     *
     * @param text
     */
    public void addValue(String text)throws IllegalArgumentException{
        String temp = text.replaceAll("[\\n\\r]", "");//eliminate carriage returns and line feeds
//        String[] v = temp.trim().split(",");

        String REGEX = ",(?![^()]*+\\))";//split only on commas not contained in parentheses
        String[] v = temp.split(REGEX);

        try{
            if(type== OptionValueType.DOUBLE){
                for(int i=0;i<v.length;i++){
                    doubleList.add(Double.parseDouble(v[i].trim()));
                }
            }else if(type== OptionValueType.INTEGER){
                for(int i=0;i<v.length;i++){
                    intList.add(Integer.parseInt(v[i].trim()));
                }
            }else{
                for(int i=0;i<v.length;i++){
                    stringList.add(v[i].replaceAll("\"", "").trim()); //replaceAll eliminates quotes
                }
            }
        }catch(ClassCastException ex){
            throw new IllegalArgumentException("Data type mismatch for " + optionName);
        }

    }

    public void addValue(Double value)throws IllegalArgumentException{
        if(type!= OptionValueType.DOUBLE) throw new IllegalArgumentException("Double values not allowed in " + optionName + ".");
        doubleList.add(value);
    }

    public void addValue(Integer value)throws IllegalArgumentException{
        if(type!= OptionValueType.INTEGER) throw new IllegalArgumentException("Integer values not allowed in " + optionName + ".");
        intList.add(value);
    }

    public ArrayList<String> getString()throws IllegalArgumentException{
        if(type!= OptionValueType.STRING) throw new IllegalArgumentException("List does not contain String values " + optionName + ".");
        return stringList;
    }

    public String getStringAt(int index)throws IllegalArgumentException{
        if(type!= OptionValueType.STRING) throw new IllegalArgumentException("List does not contain String values " + optionName + ".");
        return stringList.get(index);
    }

    public void removeStringAt(int index)throws IllegalArgumentException{
        if(type!= OptionValueType.STRING) throw new IllegalArgumentException("List does not contain String values " + optionName + ".");
        stringList.remove(index);
    }

    public ArrayList<Double> getDouble()throws IllegalArgumentException{
        if(type!= OptionValueType.DOUBLE) throw new IllegalArgumentException("List does not contain Double values " + optionName + ".");
        return doubleList;
    }

    public Double getDoubleAt(int index)throws IllegalArgumentException{
        if(type!= OptionValueType.DOUBLE) throw new IllegalArgumentException("List does not contain Double values " + optionName + ".");
        return doubleList.get(index);
    }

    public void removeDoubleAt(int index)throws IllegalArgumentException{
        if(type!= OptionValueType.DOUBLE) throw new IllegalArgumentException("List does not contain Double values " + optionName + ".");
        doubleList.remove(index);
    }

    public ArrayList<Integer> getInteger()throws IllegalArgumentException{
        if(type!= OptionValueType.INTEGER) throw new IllegalArgumentException("List does not contain Integer values " + optionName + ".");
        return intList;
    }

    public Integer getIntegerAt(int index)throws IllegalArgumentException{
        if(type!= OptionValueType.INTEGER) throw new IllegalArgumentException("List does not contain Integer values " + optionName + ".");
        return intList.get(index);
    }

    public void removeIntegerAt(int index)throws IllegalArgumentException{
        if(type!= OptionValueType.INTEGER) throw new IllegalArgumentException("List does not contain Integer values " + optionName + ".");
        intList.remove(index);
    }

    public void clear(){
        switch(type){
            case DOUBLE: doubleList.clear(); break;
            case INTEGER: intList.clear(); break;
            case STRING: stringList.clear(); break;
        }
    }

    public Iterator<String> getStringValueIterator(){
        return stringList.iterator();
    }

    public Iterator<Integer> getIntegerValueIterator(){
        return intList.iterator();
    }

    public Iterator<Double> getDoubleValueIterator(){
        return doubleList.iterator();
    }

    public String paste(){
        String list = optionName + "(";
        if(type== OptionValueType.DOUBLE){
            for(Double d : doubleList){
                list += d.toString() + ", ";
            }
        }else if(type== OptionValueType.INTEGER){
            for(Integer i : intList){
               list += i.toString() + ", ";
            }
        }else{
            for(String s : stringList){
                list += s + ", ";
            }
        }

        list = list.substring(0, list.lastIndexOf(",")).trim(); //eliminate trailing comma and white space
        list += ");";
        return list;
    }

    /**
     * Processes user input of the form:
     *
     * option(arg1, arg2, arg3); when there are multiple values
     * option(arg1); when there is a single value
     *
     * All of the values must be of the same type such as all string or all double.
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

        try{
            if(opName.equals(optionName)){
                this.addValue(opValue);
            }
        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
    }

    public String getHelpText(){
        Formatter f = new Formatter();
        String n = "<" + optionName + ">";
        f.format("%5s", ""); f.format("%-20s", "Argument: "); f.format("%-100s", n + " " + optionDescription); f.format("%n");
        f.format("%5s", ""); f.format("%-20s", "Values: "); f.format("%-100s", "[LIST: " + type + "]");f.format("%n");
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

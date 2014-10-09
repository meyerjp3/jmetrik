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

import java.util.Formatter;
import java.util.Iterator;
import java.util.LinkedHashMap;

public abstract class AbstractOption implements Option {

    /**
     * An option name must be unique to a Command.
     */
    protected String optionName = "";

    /**
     * A short description of the option.
     */
    protected String optionDescription = "";

    /**
     * Value type of option.
     */
    protected OptionValueType optionValueType = OptionValueType.STRING;

    /**
     * A required option must be used by the command.
     */
    protected boolean required = false;

    /**
     * Short descriptions of each argument. This map also defines the argument names.
     */
    protected LinkedHashMap<String, String> argumentDescription = new LinkedHashMap<String, String>();

    protected OptionType optionType = null;

    public String getOptionName(){
        return optionName;
    }

    public String getOptionDescription(){
        return optionDescription;
    }

    public String getArgumentDescription(String argName){
        String s = argumentDescription.get(argName);
        if(s!=null) return s;
        else return "";
    }

    public boolean isRequired(){
        return required;
    }

    public OptionType getOptionType(){
        return optionType;
    }

    public OptionValueType getOptionValueType(){
        return optionValueType;
    }

    /**
     * Argument names and descriptions are added with this method. This method is NOT used for setting
     * the option values.
     *
     * @param argName
     * @param argDescription
     */
    public void addArgument(String argName, String argDescription){
        argumentDescription.put(argName, argDescription);
    }

    public String getHelpText(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        String n = "<" + optionName + ">";
        f.format("%5s", ""); f.format("%-20s", "Argument: "); f.format("%-100s", n + " " + optionDescription); f.format("%n");
        if(!argumentDescription.isEmpty()){
            f.format("%5s", ""); f.format("%-20s", "Named index: ");
            f.format("%1s", "[");

            Iterator<String> iter = argumentDescription.keySet().iterator();
            while(iter.hasNext()){
                String temp = iter.next();
                f.format("%"+temp.length()+"s", temp);
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

    @Override
    public String toString(){
        return optionName;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof Option)) return false;
        if(o==this) return true;
        Option option = (Option)o;
        if(option.toString().equals(this.toString())) return true;
        return false;
    }

    @Override
    public int hashCode(){
        return optionName.hashCode();
    }

}

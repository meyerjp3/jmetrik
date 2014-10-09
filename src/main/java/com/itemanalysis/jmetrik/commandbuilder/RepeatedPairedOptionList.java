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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * This class keeps list of options that have the same name, description, and arguments.
 *
 */
public class RepeatedPairedOptionList extends AbstractOption {

    ArrayList<Option> optionList = null;
    Option optionTemplate = null;

    public RepeatedPairedOptionList(String optionName, String optionDescription, boolean required){
        this.optionName = optionName;
        this.optionDescription = optionDescription;
        this.required = required;
        optionList = new ArrayList<Option>();
    }

    public void setOptionTemplate(Option optionTemplate){
        this.optionTemplate = optionTemplate;
    }

    public void addOption(Option option)throws IllegalArgumentException{
        if(this.getOptionName().equals(option.getOptionName()) && this.getOptionType()==option.getOptionType()){
            optionList.add(option);
        }else{
            throw new IllegalArgumentException("Option name <" + option.getOptionName() +"> does not match expected name <" + this.getOptionName() +">.");
        }
    }

    public Option getOptionAt(int index){
        return optionList.get(index);
    }

    public Iterator<Option> iterator(){
        return optionList.iterator();
    }

    public void split(String text){
        optionTemplate.split(text);
    }

    public String paste(){
        String output = "";
        Iterator<Option> iter = optionList.iterator();
        while(iter.hasNext()){
            Option o = iter.next();
            output += o.paste();
            if(iter.hasNext()){
                output += "\n     ";
            }
        }
        return output;
    }

    public boolean hasValue(){
        return !optionList.isEmpty();
    }

    public int getNumberOfValues(){
        return optionList.size();
    }

}

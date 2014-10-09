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

public class MegaOptionParser {

    public MegaOptionParser(){

    }

    public MegaOption parse(String input, MegaOption option){
        if(option==null) throw new NullPointerException("Check your option names");

        String REGEX = ",(?![^()]*+\\))";//split only on commas not contained in parentheses
        int nameEnd = input.indexOf("(");
        int optionEnd = input.lastIndexOf(")");
        String optionName = input.substring(0, nameEnd).trim();
        String optionValue = input.substring(nameEnd+1, optionEnd).trim();

        if(!optionName.equals(option.getOptionName())){
            return null;
        }

        if(option.hasArguments()){
            if(optionValue.contains("=")){
                String[] argValuePairs = optionValue.split(REGEX);
                String REGEX2 = "=(?![^()]*+\\))";//split only on equal signs not contained in parentheses
                for(int i=0;i<argValuePairs.length;i++){
                    //pair[0] is the argument name
                    //pair[1] is the argument value, which could be a comma delimited list
                    String[] pair = argValuePairs[i].split(REGEX2);
                    String arg = pair[0].trim();
                    if(option.includesArgument(arg)){

                        String value = pair[1].trim();
                        int start = value.indexOf("(");
                        int end = value.lastIndexOf(")");

                        if(start==-1 && end == -1){
                            //A single value, just add it
                            option.addValueAt(arg, value);
                        }else{
                            //A list of values contained in parentheses. Split the list and add each element.
                            value = value.substring(start+1, end);
                            String[] valueList = value.split(",");
                            for(String v : valueList){
                                option.addValueAt(arg, v.trim());
                            }
                        }


                    }else{
                        throw new IllegalArgumentException(option.getOptionName() + " does not include the argument " + arg);
                    }
                }
            }else{
                throw new IllegalArgumentException("Argument/value pairs must be separated by an = sign in " + option.getOptionName() + ".");
            }


        }else{
            String[] s = optionValue.split(REGEX);
            for(int i=0;i<s.length;i++){
                option.addValue(s[i].trim());
            }
        }

        return option;
    }


}

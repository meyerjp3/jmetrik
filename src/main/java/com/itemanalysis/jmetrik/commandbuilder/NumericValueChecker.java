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

import java.util.HashMap;

/**
 * Checks that a string can be parsed into a double and that the value is within bounds.
 */
public class NumericValueChecker implements ArgumentValueChecker {

    private HashMap<String, ValueBounds> valueBounds = null;

    private static String DEFAULT_ARG = "default";

    public NumericValueChecker(){
        valueBounds = new HashMap<String, ValueBounds>();
    }

    public void addBounds(ValueBounds valueBounds){
        this.valueBounds.put(DEFAULT_ARG, valueBounds);
    }

    public void addBoundsAt(String argName, ValueBounds valueBounds){
        this.valueBounds.put(argName, valueBounds);
    }

    public boolean checkValue(String value){
        try{
            double v = Double.parseDouble(value.trim());
            if(valueBounds.get(DEFAULT_ARG).contains(v)){
                return false;
            }else{
                return true;
            }
        }catch(NumberFormatException ex){
            return false;
        }
    }

    public boolean checkValueAt(String argName, String value){
        try{
            double v = Double.parseDouble(value.trim());
            if(valueBounds.get(argName).contains(v)){
                return false;
            }else{
                return true;
            }
        }catch(NumberFormatException ex){
            return false;
        }
    }


}

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
import java.util.HashMap;

public class SelectFromListValueChecker implements ArgumentValueChecker {

    private HashMap<String, ArrayList<String>> permittedValues = null;

    private static String DEFAULT_ARG = "default";

    public SelectFromListValueChecker(){
        permittedValues = new HashMap<String, ArrayList<String>>();
    }

    public void addPermittedValue(String value){
        ArrayList<String> pv = permittedValues.get(DEFAULT_ARG);
        if(pv==null){
            pv = new ArrayList<String>();
            pv.add(value.trim());
            permittedValues.put(DEFAULT_ARG, pv);
        }else{
            pv.add(value.trim());
        }
    }

    public void addPermittedValueAt(String argName, String value){
        ArrayList<String> pv = permittedValues.get(argName);
        if(pv==null){
            pv = new ArrayList<String>();
            pv.add(value.trim());
            permittedValues.put(argName, pv);
        }else{
            pv.add(value.trim());
        }
    }

    public boolean checkValue(String value){
        ArrayList<String> pv = permittedValues.get(DEFAULT_ARG);
        if(pv==null) return false;
        return pv.contains(value.trim());
    }

    public boolean checkValueAt(String argName, String value){
        ArrayList<String> pv = permittedValues.get(argName);
        if(pv==null) return false;
        return pv.contains(value.trim());
    }


}

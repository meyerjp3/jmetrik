/*
 * Copyright (c) 2012 Patrick Meyer
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

package com.itemanalysis.jmetrik.scoring;

import com.itemanalysis.psychometrics.data.VariableName;

import java.util.ArrayList;

public class OptionScoreKey implements Comparable<OptionScoreKey>{
    
    private String keyName = "";
    
    private String options = "";
    
    private String scores = "";

    private String omit = "";

    private String notReached = "";
    
    private ArrayList<String> varNames = null;
    
    public OptionScoreKey(String keyName, String options, String scores, ArrayList<String> varNames){
        this.varNames = varNames;
        this.keyName = keyName;
        this.options = options;
        this.scores = scores;
    }

    public OptionScoreKey(String keyName, OptionScoreTableModel model, ArrayList<String> varNames){
        this.keyName = keyName;
        this.varNames = varNames;
        this.options = model.getOptionString();
        this.scores = model.getScoreString();
        this.omit = model.getOmitString();
        this.notReached = model.getNotReachedString();
    }
    
    public OptionScoreKey(String keyName, ArrayList<String> varNames){
        this(keyName, "()", "()", varNames);
    }

    public String getOptions(){
        return options;
    }
    
    public String getScores(){
        return scores;
    }

    public String getOmitCode(){
        return omit;
    }

    public String getNotReachedCode(){
        return notReached;
    }
    
    public String getVariableNames(){
        String names = "(";
        VariableName vName = null;
        for(String s : varNames){
            vName = new VariableName(s);
            names += vName.toString() + ",";
        }
        names = names.substring(0, names.length()-1);
        names += ")";
        return names;
    }
    
    public String getKeyName(){
        return keyName;
    }

    @Override
    public int hashCode(){
        return this.getKeyName().hashCode();
    }

    @Override
    public int compareTo(OptionScoreKey o){
        return this.getKeyName().compareTo(o.getKeyName());
    }
    
}

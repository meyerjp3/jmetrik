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

package com.itemanalysis.jmetrik.stats.irt.linking;

import com.itemanalysis.psychometrics.data.VariableName;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LinkingItemPair {

    private VariableName pairName = null;

    private VariableName x = null;

    private VariableName y = null;

    private static String REGEX = "\\s*\\((\\w+):\\s*(\\w+)\\s*\\)\\s*";

    private Pattern pattern = null;

    private Matcher matcher = null;

    public LinkingItemPair(VariableName x, VariableName y){
        this.x = x;
        this.y = y;
        pairName = new VariableName(x.toString() + "_" + y.toString());
        pattern = Pattern.compile(REGEX);
    }

    public LinkingItemPair(String s){
        pattern = Pattern.compile(REGEX);
        split(s);
    }

    public VariableName getXVariable(){
        return x;
    }

    public VariableName getYVariable(){
        return y;
    }

    public VariableName getPairName(){
        return pairName;
    }

    /**
     * Takes a string of the form "(xVariable, yVariable)" without the quotes.
     *
     * @param s string to be split
     */
    public void split(String s){
        matcher = pattern.matcher(s);
        String xName = "";
        String yName = "";
        while(matcher.find()){
            xName = matcher.group(1).trim();
            yName = matcher.group(2).trim();
        }
        x = new VariableName(xName);
        y = new VariableName(yName);
        pairName = new VariableName(x.toString() + "_" + y.toString());
    }

    public String commandString(){
        return "(" + x.toString() + ":" + y.toString() + ")";
    }

    @Override
    public String toString(){
        return pairName.toString();
    }

}

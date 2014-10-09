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

public class ItemParameterSet {

    private double difficulty = Double.NaN;

    private double discrimination = 1.0;

    private double guessing = 0.0;

    public ItemParameterSet(double difficulty, double discrimination, double guessing){
        this.difficulty = difficulty;
        this.discrimination = discrimination;
        this.guessing = guessing;
    }

    public ItemParameterSet(double difficulty, double discrimination){
        this.difficulty = difficulty;
        this.discrimination = discrimination;
    }

    public ItemParameterSet(double difficulty){
        this.difficulty = difficulty;
    }

    public double getDifficulty(){
        return difficulty;
    }

    public double getDiscrimination(){
        return discrimination;
    }

    public double getGuessing(){
        return guessing;
    }

    @Override
    public String toString(){
        String s = "[" + discrimination + ", " + difficulty + ", " + guessing + "]";
        return s;
    }

}

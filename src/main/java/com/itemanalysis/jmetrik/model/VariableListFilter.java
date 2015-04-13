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

package com.itemanalysis.jmetrik.model;

import com.itemanalysis.psychometrics.data.*;

import java.util.ArrayList;

public class VariableListFilter {

    private ArrayList<DataType> filteredDataTypes = new ArrayList<DataType>();

    private ArrayList<ItemType> filteredItemTypes = new ArrayList<ItemType>();

    public VariableListFilter(){

    }

    public VariableListFilter(DataType dataType, ItemType itemType){
        filteredDataTypes.add(dataType);
        filteredItemTypes.add(itemType);
    }

    public void addFilteredType(DataType dataType, ItemType itemType){
        filteredDataTypes.add(dataType);
        filteredItemTypes.add(itemType);
    }

    public void addFilteredDataType(DataType type){
        filteredDataTypes.add(type);
    }

    public void addFilteredItemType(ItemType type){
        filteredItemTypes.add(type);
    }

    /**
     * VariableTypes that match the item and data type do not pass through the filter.
     * The filter checks bith the item and data type. All undesirable types
     * should be added to the filtered types.
     *
     * @param dataType
     * @param itemType
     * @return
     */
    public boolean passThroughFilter(DataType dataType, ItemType itemType){
        for(DataType t : filteredDataTypes){
            if(t==dataType) return false;
        }

        for(ItemType t : filteredItemTypes){
            if(t==itemType) return false;
        }
        return true;
    }

    public boolean passThroughFilter(VariableAttributes variableAttributes){
        return passThroughFilter(variableAttributes.getType().getDataType(), variableAttributes.getType().getItemType());
    }
    
//    ArrayList<VariableType> filteredTypes = null;
//
//    public VariableListFilter(){
//        filteredTypes = new ArrayList<VariableType>();
//    }
//
//    public void addFilteredType(VariableType type){
//        filteredTypes.add(type);
//    }
//
//    /**
//     * VariableTypes that match the item and data type do not pass through the filter.
//     * The filter checks bith the item and data type. All undesirable types
//     * should be added to the filtered types.
//     *
//     * @param type
//     * @return
//     */
//    public boolean passThroughFilter(VariableType type){
//        for(VariableType t : filteredTypes){
//            if(t.getItemType()==type.getItemType() && t.getDataType()==type.getDataType()){
//                return false;
//            }
//        }
//        return true;
//    }
//
//    public boolean passThroughFilter(VariableAttributes varAttr){
//        return passThroughFilter(varAttr.getType());
//    }
    
}

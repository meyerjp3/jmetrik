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

import com.itemanalysis.psychometrics.data.VariableName;

import javax.swing.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class VariableNameListModel extends AbstractListModel {

    TreeSet<VariableName> model;

    /**
     * In this comparator, VariableNames are compared on their index.
     * If they have the same index, they are compared by their string
     * value. By default, all VariableName object have the same index.
     * So, they will be compared by string if the index is not changed.
     */
    private static Comparator LIST_COMPARATOR = new Comparator(){
        public int compare(Object o1, Object o2){
            VariableName a = (VariableName)o1;
            VariableName b = (VariableName)o2;
            int result = 0;

            if(a.getIndex()<b.getIndex()){
                result = -1;
            }else if(a.getIndex()==b.getIndex()){
                result=a.toString().compareTo(b.toString());
            }else{
                result=1;
            }
            return result;

        }
    };

    public VariableNameListModel(){
        model=new TreeSet<VariableName>(LIST_COMPARATOR);
    }

    public int getSize(){
        return model.size();
    }

    public Object getElementAt(int index){
        return model.toArray()[index];
    }

    public void addElement(VariableName element){
        if(model.add(element)){
            fireContentsChanged(this,0,getSize()-1);
        }else{
        }
    }

    public void replaceElement(VariableName element){
        if(model.contains(element)){
            model.remove(element);
            model.add(element);
        }

    }

    public void addAll(VariableName[] elements){
        for(int i=0;i<elements.length;i++){
            addElement(elements[i]);
        }
    }

    public VariableName[] getAll(){
        VariableName[] variables = new VariableName[model.size()];
        int i=0;
        for(VariableName v : model){
            variables[i] = v;
            i++;
        }
        return variables;
    }

    public void clear(){
        model.clear();
        fireContentsChanged(this,0,getSize());
    }

    public boolean contains(VariableName element){
        return model.contains(element);
    }

    public VariableName firstElement(){
        return model.first();
    }

    public Iterator<VariableName> iterator(){
        return model.iterator();
    }

    public VariableName lastElement(){
        return model.last();
    }

    public boolean removeElement(VariableName element){
        boolean removed = model.remove(element);
        if(removed){
            fireContentsChanged(this, 0, getSize());
        }
        return removed;
    }

}

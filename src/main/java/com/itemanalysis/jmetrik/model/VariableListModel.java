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

import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class VariableListModel extends AbstractListModel {

    private TreeSet<VariableInfo> variableSet = null;

    private VariableListFilter filter = null;

    private static Comparator LIST_COMPARATOR = new Comparator(){
        public int compare(Object o1, Object o2){
            int result;
            VariableInfo a = (VariableInfo)o1;
            VariableInfo b = (VariableInfo)o2;

            if(a.positionInDb()<b.positionInDb()){
                result = -1;
            }else if(a.positionInDb()==b.positionInDb()){
                result=0;
            }else{
                result=1;
            }
            return result;

        }
    };

    public VariableListModel(VariableListFilter filter){
        this.filter = filter;
        variableSet = new TreeSet<VariableInfo>(LIST_COMPARATOR);
    }

    public int getSize(){
        return  variableSet.size();
    }
    
    public VariableInfo getElementAt(int index){
        return (VariableInfo)variableSet.toArray()[index];
    }

    public void addElement(VariableInfo element){
        if(filter.passThroughFilter(element)){
            if(variableSet.add(element)){
                fireContentsChanged(this,0,getSize()-1);
            }
        }
    }

    public void addAll(VariableInfo[] elements){
        for(VariableInfo v: elements){
            addElement(v);
        }
    }

    public void removeAll(VariableInfo[] elements){
        for(VariableInfo v: elements){
            removeElement(v);
        }
    }

    public VariableInfo[] getAll(){
        Object[] o = variableSet.toArray();
        int size = o.length;
        VariableInfo[] v=new VariableInfo[size];
        for(int i=0;i<size;i++){
            v[i]=(VariableInfo)o[i];
        }
        return v;
    }

    public void replaceElement(VariableInfo element){
        if(filter.passThroughFilter(element)){
            if(variableSet.contains(element)){
                variableSet.remove(element);
                variableSet.add(element);
            }
        }
    }

    public void clear(){
        variableSet.clear();
        fireContentsChanged(this,0,getSize());
    }

    public boolean contains(VariableInfo varInfo){
        return variableSet.contains(varInfo);
    }

    public Iterator<VariableInfo> iterator(){
        return variableSet.iterator();
    }

    public VariableInfo lastElement(){
        return variableSet.last();
    }

    public boolean removeElement(VariableInfo element){
        boolean removed = variableSet.remove(element);
        if(removed){
            fireContentsChanged(this, 0, getSize());
        }
        return removed;
    }

}

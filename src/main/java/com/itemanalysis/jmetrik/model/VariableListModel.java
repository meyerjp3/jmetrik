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

import com.itemanalysis.psychometrics.data.VariableAttributes;

import javax.swing.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;

public class VariableListModel extends AbstractListModel {

    private TreeSet<VariableAttributes> variableSet = new TreeSet<VariableAttributes>(LIST_COMPARATOR);

    private VariableListFilter filter = null;

    private static Comparator LIST_COMPARATOR = new Comparator(){
        public int compare(Object o1, Object o2){
            int result;
            VariableAttributes a = (VariableAttributes)o1;
            VariableAttributes b = (VariableAttributes)o2;

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
    }

    public int getSize(){
        return  variableSet.size();
    }
    
    public VariableAttributes getElementAt(int index){
        return (VariableAttributes)variableSet.toArray()[index];
    }

    public void addElement(VariableAttributes element){
        if(filter.passThroughFilter(element)){
            if(variableSet.add(element)){
                fireContentsChanged(this,0,getSize()-1);
            }
        }
    }

    public void addAll(VariableAttributes[] elements){
        for(VariableAttributes v: elements){
            addElement(v);
        }
    }

    public void removeAll(VariableAttributes[] elements){
        for(VariableAttributes v: elements){
            removeElement(v);
        }
    }

    public VariableAttributes[] getAll(){
        Object[] o = variableSet.toArray();
        int size = o.length;
        VariableAttributes[] v=new VariableAttributes[size];
        for(int i=0;i<size;i++){
            v[i]=(VariableAttributes)o[i];
        }
        return v;
    }

    public void replaceElement(VariableAttributes element){
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

    public boolean contains(VariableAttributes variableAttributes){
        return variableSet.contains(variableAttributes);
    }

    public Iterator<VariableAttributes> iterator(){
        return variableSet.iterator();
    }

    public VariableAttributes lastElement(){
        return variableSet.last();
    }

    public boolean removeElement(VariableAttributes element){
        boolean removed = variableSet.remove(element);
        if(removed){
            fireContentsChanged(this, 0, getSize());
        }
        return removed;
    }

}

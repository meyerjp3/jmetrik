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

package com.itemanalysis.jmetrik.workspace;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;

public abstract class AbstractJmetrikProcess implements JmetrikProcess{

    protected ArrayList<PropertyChangeListener> propertyChangeListeners = new ArrayList<PropertyChangeListener>();

    protected ArrayList<VariableChangeListener> variableChangeListeners = new ArrayList<VariableChangeListener>();

    public final static String JMETRIK_TEXT_FILE = "com.itemanalysis.jmetrik.swing.JmetrikTextFile";

    public void addVariableChangeListener(VariableChangeListener listener){
        variableChangeListeners.add(listener);
    }

    public void removeVariableChangeListener(VariableChangeListener listener){
        variableChangeListeners.remove(listener);
    }

    public void removeAllVariableChangeListeners(){
        variableChangeListeners.clear();
    }

    public void fireVariableChangeEvent(VariableChangeEvent evt){
        for(VariableChangeListener l : variableChangeListeners){
            l.variableChanged(evt);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener){
        propertyChangeListeners.add(listener);
    }


}

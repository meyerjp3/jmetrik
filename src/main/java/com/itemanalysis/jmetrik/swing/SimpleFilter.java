/*
 * Copyright (c) 2011 Patrick Meyer
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

package com.itemanalysis.jmetrik.swing;

import javax.swing.filechooser.FileFilter;
import java.io.File;

public class SimpleFilter extends FileFilter {

    private String extension=null;
    private String description=null;

    public SimpleFilter(String aExtension, String aDescription){
        extension=aExtension.toLowerCase();
        description=aDescription;
    }

    public String getDescription(){
        return description;
    }

    public boolean accept(File f){
        String path = f.getAbsolutePath().toLowerCase();
        if(f==null)
            return false;
        if(f.isDirectory())
            return true;
        if(f.getName().toLowerCase().endsWith(extension)){
            return true;
        }

        return false;
    }

}

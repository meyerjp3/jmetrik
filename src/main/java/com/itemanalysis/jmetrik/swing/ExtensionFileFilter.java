/*
 * Copyright (c) 2013 Patrick Meyer
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

public class ExtensionFileFilter extends FileFilter {

    private String description = "";
    private String[] extension = null;

    public ExtensionFileFilter(String description, String extension){
        this(description, new String[]{extension});
    }

    public ExtensionFileFilter(String description, String[] extension){
        if(description==null){
            this.description = extension[0];
        }else{
            this.description = description;
        }
        this.extension = (String[])extension.clone();
        toLower(this.extension);
    }

    private void toLower(String[] s){
        for(int i=0;i<s.length;i++){
            s[i] = s[i].toLowerCase();
        }
    }

    public String getDescription(){
        return description;
    }

    public boolean accept(File file){
        if(file.isDirectory()){
            return true;
        }else{
            String path = file.getAbsolutePath().toLowerCase();
            for(int i=0;i<extension.length;i++){
                String temp = extension[i];
                if((path.endsWith(temp) && (path.charAt(path.length()-temp.length()-1))=='.')){
                    return true;
                }
            }
        }
        return false;
    }


}

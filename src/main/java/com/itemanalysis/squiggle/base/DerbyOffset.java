/*
 * Offset.java
 * 
 * Copyright (C) 2010 J. Patrick Meyer <meyerjp at itemanalysis.com>
 * 
 * Offset.java is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Offset.java is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License
 * along with Offset.java.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package com.itemanalysis.squiggle.base;

import com.itemanalysis.squiggle.output.Output;
import com.itemanalysis.squiggle.output.Outputable;

/**
 *
 * @author J. Patrick Meyer <meyerjp at itemanalysis.com>
 */
public class DerbyOffset implements Outputable {
    
    private int offset = 0;
    
    private int size = Integer.MAX_VALUE;
    
    public DerbyOffset(int offset){
        this.offset = offset;
    }

    public DerbyOffset(int offset, int size){
        this.offset = offset;
        this.size = size;
    }

    public void write(Output out) {
        out.print(" " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY");
    }

}

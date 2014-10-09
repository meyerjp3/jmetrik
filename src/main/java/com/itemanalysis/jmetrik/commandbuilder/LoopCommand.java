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

package com.itemanalysis.jmetrik.commandbuilder;

public class LoopCommand {

    private String indexName = "";

    private int begin = 0;

    private int end = 0;

    private int position = 0;

    private String body = "";

    public LoopCommand(String indexName, int begin, int end, String body){
        this.indexName = indexName;
        this.begin = begin;
        this.end = end;
        this.position = begin-1;
        this.body = body;
    }

    public String getIndexName(){
        return indexName;
    }

    public String getBody(){
        return body;
    }

    public boolean hasNext(){
        return position<end;
    }

    public String next(){
        position++;
        String s = body.replaceAll("<" + indexName + ">", Integer.valueOf(position).toString());
        return s;
    }



}


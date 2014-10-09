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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * FIFO queue of command objects. Many commands will be added to Queue and
 * processed in FIFO order.
 *
 *
 */
public class CommandQueue {

    ArrayList<AbstractCommand> commands = null;

    public CommandQueue(){
        commands = new ArrayList<AbstractCommand>();
    }

    /**
     * New elements added to beginning to ensure FIFO
     *
     * @param command
     */
    public void addToQueue(AbstractCommand command){
        commands.add(0, command);
    }

    /**
     * Reads syntax file,  parses file into commands, and adds each command to queue.
     *
     * @param fileName
     */
    public void parseFile(String fileName)throws IOException{
        File f = new File(fileName);
        BufferedReader br = null;

        try{
            br = new BufferedReader(new FileReader(f));
            String line = null;
            StringBuilder txt = new StringBuilder();
            while((line=br.readLine())!=null){

            }
        }catch(IOException ex){
            throw new IOException(ex);
        }


    }

    public boolean hasNext(){
        return commands.size()>0;
    }

    /**
     * Remove next element from bottom of list to ensure FIFO.
     *
     * @return
     */
    public AbstractCommand next(){
        return commands.remove(commands.size()-1);
    }

    public int size(){
        return commands.size();
    }

    public void clearQueue(){
        commands.clear();
    }

}


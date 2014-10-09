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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextToCommand {

    private ArrayList<String[]> observedCommands = null;

    private ArrayList<LoopCommand> loops = null;

    private boolean hasLoops = false;

    private int loopIndex = 0;

    public TextToCommand(){
        observedCommands = new ArrayList<String[]>();
        loops = new ArrayList<LoopCommand>();
    }

    /**
     * This method takes text and separates the command name from the command body.
     * A series of commands are stored in a String array with two elements.
     * The first element is the command name and the second is the command body.
     * The command body can be split by a class that extends AbstractCommand.
     *
     * @param text text to be converted to commands
     */
    public void convertToCommands(String text){
       Pattern pattern = null;
       Matcher matcher = null;

       /**
        * regex for parsing a loop
        * group 1 (\\D+) = index name
        * group 2 (\\d+) = index start
        * group 3 (\\d+) = index end
        * group 4 ([\\s\\S]+?) = command body to be parsed with splitCommandBody(String body)
        */
       String LOOP_REGEX = "loop\\((\\D+)\\s+in\\s+(\\d+)\\:(\\d+)\\)([\\s\\S]+?)endloop";

       pattern = Pattern.compile(LOOP_REGEX);
       matcher = pattern.matcher(text);

       LoopCommand loop = null;

       String indexName = "";
       int begin = 0;
       int end = 0;
       String body = "";
       while(matcher.find()){
           indexName = matcher.group(1);
           begin = Integer.parseInt(matcher.group(2).trim());
           end = Integer.parseInt(matcher.group(3).trim());
           body = matcher.group(4);
           loop = new LoopCommand(indexName, begin, end, body);
           loops.add(loop);
           hasLoops = true;
       }

       if(hasLoops){
           for(LoopCommand c:loops){
               while(c.hasNext()){
                   splitCommandBody(c.next());
               }
           }
       }else{
           splitCommandBody(text);
       }

    }

    private void splitCommandBody(String body){
       /**
        * regex for parsing a command
        * group 1 (.+) = command name
        * group 2 (\\{[^\\}]*\\}) = command body (i.e. all the arguments and values)
        */
       String COMMAND_REGEX = "(.+)(\\{[^\\}]*\\})";
       Pattern p = Pattern.compile(COMMAND_REGEX);
       Matcher m = p.matcher(body);
       String name = "";
       String command = "";
       String temp = "";
       String[] c = null;

       while(m.find()){
           name = m.group(1);
           temp = m.group(2);
           command = temp.substring(1,temp.length()-1);
           c = new String[2];
           c[0] = name;//command name
           c[1] = command;//command arguments
           observedCommands.add(c);
       }
    }

    public Iterator<String[]> iterator(){
        return observedCommands.iterator();
    }

}


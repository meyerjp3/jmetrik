/**
 * Copyright 2014 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.jmetrik.commandbuilder;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The text representation of a command has the form commandName{ commandBody }, where the commandBody
 * is a list of MegaOptions separated by semicolons.
 */
public class MegaCommand {

    private String commandName = "";
    private String commandDescription = "";
    private HashMap<String, MegaOption> commandOptions = null;

    public MegaCommand(String commandName, String commandDescription){
        this.commandName = commandName;
        this.commandDescription = commandDescription;
        commandOptions = new HashMap<String, MegaOption>();
    }

    public MegaCommand(String commandName){
        this(commandName, "");
    }

    /**
     * Add options to the command. If duplicates are NOT allowed then only the first option is retained
     * if teh option name is repeated.
     *
     * @param option an option to be added
     */
    public void addOption(MegaOption option){
        String optionName = option.getOptionName();
        commandOptions.put(optionName, option);
    }

    /**
     * REturns only the first option in a list of options.
     *
     * @param optionName name of option to be returned
     * @return
     */
    public MegaOption getOption(String optionName){
        return commandOptions.get(optionName);
    }

    public void split(String text){
        int commandNameEnd = text.indexOf("{");
        int commandBodyEnd = text.lastIndexOf("}");
        String cName = text.substring(0, commandNameEnd).trim();
        if(!commandName.equals(cName)) return;

        String commandBody = text.substring(commandNameEnd+1, commandBodyEnd).trim();
        commandBody = commandBody.replaceAll("(?:\\n|\\r)", "");//remove all new lines and carriage returns

        MegaOptionParser optionParser = new MegaOptionParser();
        String[] options = commandBody.trim().split(";");
        MegaOption temp = null;
        for(String s : options){
            String op = s.trim();
            int nameEnd = op.indexOf("(");
            int bodyEnd = op.indexOf(")");
            String optionName = op.substring(0, nameEnd).trim();
            String optionBody = op.substring(nameEnd+1, bodyEnd);

            temp = commandOptions.get(optionName);
            optionParser.parse(op, temp);
        }
    }

    public String paste(){
        StringBuilder sb = new StringBuilder();
        sb.append(commandName + "{\n");
        for(String s : commandOptions.keySet()){
            MegaOption mo = commandOptions.get(s);
            sb.append("  " + mo.paste() + ";\n");
        }
        sb.append("}");
        return sb.toString();
    }

    public String getHelpText(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%-20s", commandName);

        if(!"".equals(commandDescription)){
            String[] textArray = commandDescription.split("\\s+");//split on white space
            String line = "";
            int lineCount = 0;

            for(String s : textArray){
                if((line+s).trim().length()<=100){
                    line += s + " ";
                }else{
                    //print line
                    if(lineCount>0) f.format("%20s", "");
                    f.format("%-100s", line);f.format("%n");
                    line = s + " ";
                    lineCount++;
                }
            }
        }
        f.format("%n");
        for(String s : commandOptions.keySet()){
            MegaOption mo = commandOptions.get(s);
//            f.format("%2s", "");
            sb.append(mo.getHelpText());
        }
        sb.append("}");
        return sb.toString();
    }

}

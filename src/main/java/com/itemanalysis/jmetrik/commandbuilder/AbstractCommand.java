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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


public abstract class AbstractCommand implements Command{

    public String commandName = "";

    public String description = "";

    public Boolean required = false;

    public HashMap<String, SelectOneOption> selectOneOption = null;

    public HashMap<String, SelectAllOption> selectAllOption = null;

    public HashMap<String, FreeOption> freeOption = null;

    public HashMap<String, FreeOptionList> freeOptionList = null;

    public HashMap<String, PairedOptionList> pairedOptionList = null;

    public HashMap<String, RepeatedPairedOptionList> repeatedOption = null;

    public ArrayList<String> requiredOptions = null;

    public Set<String> commandOptions = null;

    public AbstractCommand(String commandName){
        this(commandName, "", false);
    }

    public AbstractCommand(String commandName, String description){
        this(commandName, description, false);
    }

    public AbstractCommand(String commandName, String description, Boolean required){
        this.commandName = commandName;
        this.description = description;
        this.required = required;
        selectOneOption = new HashMap<String, SelectOneOption>();
        selectAllOption = new HashMap<String, SelectAllOption>();
        freeOption = new HashMap<String, FreeOption>();
        freeOptionList = new HashMap<String, FreeOptionList>();
        pairedOptionList = new HashMap<String, PairedOptionList>();
        requiredOptions = new ArrayList<String>();
        commandOptions = new HashSet<String>();
        repeatedOption = new HashMap<String, RepeatedPairedOptionList>();
    }

    public void addSelectOneOption(SelectOneOption option)throws IllegalArgumentException{
        if(commandOptions.contains(option.getOptionName()))throw new IllegalArgumentException("Command already contains the option " + option.getOptionName() + ".");
        commandOptions.add(option.getOptionName());
        selectOneOption.put(option.getOptionName(), option);
        if(option.isRequired()) requiredOptions.add(option.getOptionName());
    }

    public void addSelectAllOption(SelectAllOption option)throws IllegalArgumentException{
        if(commandOptions.contains(option.getOptionName()))throw new IllegalArgumentException("Command already contains the option " + option.getOptionName() + ".");
        commandOptions.add(option.getOptionName());
        selectAllOption.put(option.getOptionName(), option);
        if(option.isRequired()) requiredOptions.add(option.getOptionName());
    }

    public void addFreeOption(FreeOption option)throws IllegalArgumentException{
        if(commandOptions.contains(option.getOptionName()))throw new IllegalArgumentException("Command already contains the option " + option.getOptionName() + ".");
        commandOptions.add(option.getOptionName());
        freeOption.put(option.getOptionName(), option);
        if(option.isRequired()) requiredOptions.add(option.getOptionName());
    }

    public void addFreeOptionList(FreeOptionList option)throws IllegalArgumentException{
        if(commandOptions.contains(option.getOptionName()))throw new IllegalArgumentException("Command already contains the option " + option.getOptionName() + ".");
        commandOptions.add(option.getOptionName());
        freeOptionList.put(option.getOptionName(), option);
        if(option.isRequired()) requiredOptions.add(option.getOptionName());
    }

    public void addPairedOptionList(PairedOptionList option)throws IllegalArgumentException{
        if(commandOptions.contains(option.getOptionName()))throw new IllegalArgumentException("Command already contains the option " + option.getOptionName() + ".");
        commandOptions.add(option.getOptionName());
        pairedOptionList.put(option.getOptionName(), option);
        if(option.isRequired()) requiredOptions.add(option.getOptionName());
    }

    public void addRepeatedOption(RepeatedPairedOptionList option)throws IllegalArgumentException{
        if(commandOptions.contains(option.getOptionName()))throw new IllegalArgumentException("Command already contains the option " + option.getOptionName() + ".");
        commandOptions.add(option.getOptionName());
        repeatedOption.put(option.getOptionName(), option);
        if(option.isRequired()) requiredOptions.add(option.getOptionName());
    }

    public void removeOption(String optionName){
        Option option = selectOneOption.get(optionName);
        if(option != null){
            if(option.isRequired()) requiredOptions.remove(optionName);
            selectOneOption.remove(optionName);
            commandOptions.remove(option.getOptionName());
        }

        option = selectAllOption.get(optionName);
        if(option != null){
            if(option.isRequired()) requiredOptions.remove(optionName);
            selectAllOption.remove(optionName);
            commandOptions.remove(option.getOptionName());
        }

        option = freeOptionList.get(optionName);
        if(option!=null){
            if(option.isRequired()) requiredOptions.remove(optionName);
            freeOptionList.remove(optionName);
            commandOptions.remove(option.getOptionName());
        }

        option = pairedOptionList.get(optionName);
        if(option!=null){
            if(option.isRequired()) requiredOptions.remove(optionName);
            pairedOptionList.remove(optionName);
            commandOptions.remove(option.getOptionName());
        }

        option = repeatedOption.get(optionName);
        if(option!=null){
            if(option.isRequired()) requiredOptions.remove(optionName);
            repeatedOption.remove(optionName);
            commandOptions.remove(option.getOptionName());
        }
    }

    public SelectOneOption getSelectOneOption(String optionName)throws IllegalArgumentException{
        SelectOneOption option = selectOneOption.get(optionName);
        if(option!=null){
            return option;
        }else{
            throw new IllegalArgumentException("Option not found: " + optionName);
        }
    }

    public SelectAllOption getSelectAllOption(String optionName)throws IllegalArgumentException{
        SelectAllOption option = selectAllOption.get(optionName);
        if(option!=null){
            return option;
        }else{
            throw new IllegalArgumentException("Option not found: " + optionName);
        }
    }

    public FreeOption getFreeOption(String optionName)throws IllegalArgumentException{
        FreeOption option = freeOption.get(optionName);
        if(option!=null){
            return option;
        }else{
            throw new IllegalArgumentException("Option not found: " + optionName);
        }
    }

    public FreeOptionList getFreeOptionList(String optionName)throws IllegalArgumentException{
        FreeOptionList option = freeOptionList.get(optionName);
        if(option!=null){
            return option;
        }else{
            throw new IllegalArgumentException("Option not found: " + optionName);
        }
    }

    public PairedOptionList getPairedOptionList(String optionName)throws IllegalArgumentException{
        PairedOptionList option = pairedOptionList.get(optionName);
        if(option!=null){
            return option;
        }else{
            throw new IllegalArgumentException("Option not found: " + optionName);
        }
    }

    public RepeatedPairedOptionList getRepeatedOption(String optionName)throws IllegalArgumentException{
        RepeatedPairedOptionList option = repeatedOption.get(optionName);
        if(option!=null){
            return option;
        }else{
            throw new IllegalArgumentException("Option not found: " + optionName);
        }
    }

    public ArrayList<String> getRequiredOptions(){
        return requiredOptions;
    }

    public String getName(){
        return commandName;
    }

    public String getHelpText(){
        String output = commandName + "{\n\n";

        if(selectOneOption.size()>0){
            for(String s : selectOneOption.keySet()){
                output+=selectOneOption.get(s).getHelpText() + "\n";
            }
        }

        if(selectAllOption.size()>0){
            for(String s : selectAllOption.keySet()){
                output+=selectAllOption.get(s).getHelpText() + "\n";
            }
        }

        if(freeOptionList.size()>0){
            for(String s : freeOptionList.keySet()){
                output+=freeOptionList.get(s).getHelpText() + "\n";
            }
        }

        if(pairedOptionList.size()>0){
            for(String s : pairedOptionList.keySet()){
                output+=pairedOptionList.get(s).getHelpText() + "\n";
            }
        }

        if(repeatedOption.size()>0){
            for(String s : repeatedOption.keySet()){
                output+=repeatedOption.get(s).getHelpText() + "\n";
            }
        }

        output+="}\n";
        return output;
    }

    public String paste()throws IllegalArgumentException{
        String output = commandName + "{\n";

        try{
            if(selectOneOption.size()>0){
                for(String s : selectOneOption.keySet()){
                    if(selectOneOption.get(s).hasValue()){
                        output+="     ";
                        output+=selectOneOption.get(s).paste() + "\n";
                    }
                }
            }

            if(selectAllOption.size()>0){
                for(String s : selectAllOption.keySet()){
                    if(selectAllOption.get(s).hasValue()){
                        String temp = selectAllOption.get(s).paste();
                        if(!temp.equals("")){
                            output+="     ";
                            output+=selectAllOption.get(s).paste() + "\n";
                        }
                    }
                }
            }

            if(freeOption.size()>0){
                for(String s : freeOption.keySet()){
                    if(freeOption.get(s).hasValue()){
                        output+="     ";
                        output+=freeOption.get(s).paste() + "\n";
                    }
                }
            }

            if(freeOptionList.size()>0){
                for(String s : freeOptionList.keySet()){
                    if(freeOptionList.get(s).hasValue()){
                        output+="     ";
                        output+=freeOptionList.get(s).paste() + "\n";
                    }
                }
            }

            if(pairedOptionList.size()>0){
                for(String s : pairedOptionList.keySet()){
                    if(pairedOptionList.get(s).hasValue()){
                        output+="     ";
                        output+=pairedOptionList.get(s).paste() + "\n";
                    }
                }
            }

            if(repeatedOption.size()>0){
                for(String s : repeatedOption.keySet()){
                    if(repeatedOption.get(s).hasValue()){
                        output+="     ";
                        output+=repeatedOption.get(s).paste() + "\n";
                    }
                }
            }
        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
        output+="}\n";
        return output;

    }

    /**
     * This method takes a string, presumably from a text file,
     * and parses the command into its arguments. It then checks
     * for required arguments.
     *
     * @param cmdString
     */
    public void split(String cmdString)throws IllegalArgumentException{
//        String regex = "\\s+(?=([^\"]*\"[^\"]*\")*[^\"]*$)"; //split on whitespace only, preserve quoted text
        cmdString = cmdString.replaceAll("\\n\\s+", "");
        String[] optionString = cmdString.split(";");
        String optionName = "";
        int index = 0;

        try{
            //parse command into options
            for(int i=0;i<optionString.length;i++){
                if(!optionString[i].trim().equals("")){
                    index = optionString[i].indexOf("(");
                    if(index==-1) throw new IllegalArgumentException("Invalid argument value(s) pairing");
                    optionName = optionString[i].substring(0, index).trim();

                    FreeOption fo = freeOption.get(optionName);
                    if(fo!=null){
                        fo.split(optionString[i]);
                        requiredOptions.remove(fo.getOptionName());
                    }
                    FreeOptionList fol = freeOptionList.get(optionName);
                    if(fol!=null){
                        fol.split(optionString[i]);
                        requiredOptions.remove(fol.getOptionName());
                    }
                    SelectOneOption soo = selectOneOption.get(optionName);
                    if(soo!=null){
                        soo.split(optionString[i]);
                        requiredOptions.remove(soo.getOptionName());
                    }
                    SelectAllOption sao = selectAllOption.get(optionName);
                    if(sao!=null){
                        sao.split(optionString[i]);
                        requiredOptions.remove(sao.getOptionName());
                    }
                    PairedOptionList pol = pairedOptionList.get(optionName);
                    if(pol!=null){
                        pol.split(optionString[i]);
                        requiredOptions.remove(pol.getOptionName());
                    }
                    RepeatedPairedOptionList repeat = repeatedOption.get(optionName);
                    if(repeat!=null){
                        repeat.split(optionString[i]);
                        requiredOptions.remove(pol.getOptionName());
                    }
                }

            }

            if(requiredOptions.size()>0){
                throw new IllegalArgumentException("Required argument not found: " + requiredOptions.get(0));
            }

        }catch(IllegalArgumentException ex){
            throw new IllegalArgumentException(ex);
        }
    }

    @Override
    public String toString(){
        return commandName;
    }

    @Override
    public boolean equals(Object o){
		if(!(o instanceof AbstractCommand)) return false;
		if(o==this) return true;
		AbstractCommand cmd = (AbstractCommand)o;
		if(cmd.toString().equals(this.toString())) return true;
		return false;
	}

    @Override
	public int hashCode(){
		return commandName.hashCode();
	}

}
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

package com.itemanalysis.jmetrik.scoring;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

public class OptionScoreTableModel extends AbstractTableModel{

    private String[] columnNames = {"Option", "Score"};

    private int numCols=columnNames.length;

    private int numRows=100; //FIXME allow user to specify this default max value in program options

    private TreeMap<Integer, String[]> optionScore = new TreeMap<Integer, String[]>();

    private Object omitCode = null;

    private Object notReachedCode = null;

    public OptionScoreTableModel(){

    }

    /**
     * Same method as the VariableInfo.characterCount(String text, char target)
     * @param text
     * @param target
     * @return
     */
    public int charaterCount(String text, char target){
        char[] characters = text.toCharArray();
        int count = 0;
        for(int i=0; i<characters.length;i++){
            if(characters[i]==target) count++;
        }
        return count;
    }

    public Object getValueAt(int r, int c){
        Object[] temp = optionScore.get(new Integer(r));
        if(temp==null){
            return null;
        }else{
            return temp[c];
        }
    }

    @Override
    public void setValueAt(Object value, int r, int c){
        Integer index = new Integer(r);
        String[] temp = optionScore.get(index);
        if(value!=null){
            if(temp==null){
                String[] obj = {"",""};
                obj[c]=value.toString().trim();
                optionScore.put(index, obj);
            }else{
                temp[c]=value.toString();
            }
        }

    }

    public void clearAll(){
        optionScore.clear();
        this.fireTableDataChanged();
    }

    public int getRowCount(){
        return numRows;
    }

    public int getColumnCount(){
        return numCols;
    }

    @Override
    public String getColumnName(int c){
        return columnNames[c];
    }

    @Override
    public Class getColumnClass(int c){
        return Object.class;
    }

    @Override
    public boolean isCellEditable(int row, int col){
        return true;
    }
    
    public String getOptionString(){
        if(optionScore.size()==0) return "()";
        Set<Integer> keys = optionScore.keySet();
        Iterator<Integer> iter = keys.iterator();
        String[] tempObject = null;

        ArrayList<String> optionList = new ArrayList<String>();

        while(iter.hasNext()){
            tempObject = optionScore.get(iter.next());
            if(tempObject!=null){
                if(!tempObject[0].trim().equals("") && tempObject[1].trim().toUpperCase().equals("OM")){
                    omitCode = tempObject[0].trim();
                }else if(!tempObject[0].trim().equals("") && tempObject[1].trim().toUpperCase().equals("NR")){
                    notReachedCode = tempObject[0].trim();
                }else if(!tempObject[0].trim().equals("") && !tempObject[1].trim().equals("")){
                    optionList.add(tempObject[0].toString().trim());
                }
            }
        }

        String catOrig = "(";
        Iterator<String> optionIter = optionList.iterator();
        while(optionIter.hasNext()){
            catOrig += optionIter.next();
            if(optionIter.hasNext()){
                catOrig += ",";
            }else{
                catOrig += ")";
            }
        }
        
        return catOrig;
    }
    
    public String getScoreString(){
        if(optionScore.size()==0) return "()";
        Set<Integer> keys = optionScore.keySet();
        Iterator<Integer> iter = keys.iterator();
        String[] tempObject = null;
        ArrayList<String> scoreList = new ArrayList<String>();

        while(iter.hasNext()){
            tempObject = optionScore.get(iter.next());
            if(tempObject!=null){
                 if(!tempObject[0].trim().equals("") && tempObject[1].trim().toUpperCase().equals("OM")){
                    omitCode = tempObject[0].trim();
                }else if(!tempObject[0].trim().equals("") && tempObject[1].trim().toUpperCase().equals("NR")){
                    notReachedCode = tempObject[0].trim();
                }else if(!tempObject[0].trim().equals("") && !tempObject[1].trim().equals("")){
                    scoreList.add(tempObject[1].toString().trim());
                }
            }
        }

        String scoreOrig = "(";
        Iterator<String> scoreIter = scoreList.iterator();
        while(scoreIter.hasNext()){
            scoreOrig += scoreIter.next();
            if(scoreIter.hasNext()){
                scoreOrig += ",";
            }else{
                scoreOrig += ")";
            }
        }


        return scoreOrig;
    }

    public String getOmitString(){
        if(omitCode!=null) return omitCode.toString();
        return "";
    }

    public String getNotReachedString(){
        if(notReachedCode!=null) return notReachedCode.toString();
        return "";
    }

    @Override
    /**
     * This method is similar to VariableInfo.printOptionScoreKey();
     *
     */
    public String toString(){
        if(optionScore.size()==0) return "";
        Set<Integer> keys = optionScore.keySet();
        Iterator<Integer> iter = keys.iterator();
        String[] tempObject = null;
        String catOrig = "(";
        String catScor = "(";
        String finString = "";
        
        while(iter.hasNext()){
            tempObject = optionScore.get(iter.next());
            if(tempObject!=null){
                if(!tempObject[0].trim().equals("") && !tempObject[1].trim().equals("")){
                    catOrig += tempObject[0].toString();
                    catScor += tempObject[1].toString();
                    if(iter.hasNext()){
                        catOrig += ",";
                        catScor += ",";
                    }else{
                        catOrig += ")";
                        catScor += ")";
                    }
                }
            }


        }
        finString = catOrig + " " + catScor;

        return finString;
    }


}

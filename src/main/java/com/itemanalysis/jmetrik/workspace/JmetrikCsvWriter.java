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

import au.com.bytecode.opencsv.CSVWriter;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;

import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class JmetrikCsvWriter extends CSVWriter {

    public JmetrikCsvWriter(Writer writer){
        super(writer);
    }

    public JmetrikCsvWriter(Writer writer, char separator){
        super(writer, separator);
    }

    public JmetrikCsvWriter(Writer writer, char separator, char quotechar){
        super(writer, separator, quotechar);
    }

    public JmetrikCsvWriter(Writer writer, char separator, char quotechar, char escapechar){
        super(writer, separator, quotechar, escapechar);
    }

    public JmetrikCsvWriter(Writer writer, char separator, char quotechar, char escapechar, String lineEnd){
        super(writer, separator, quotechar, escapechar, lineEnd);
    }

    public JmetrikCsvWriter(Writer writer, char separator, char quotechar, String lineEnd){
        super(writer, separator, quotechar, lineEnd);
    }

    public void writeHeader(ArrayList<VariableAttributes> variables){
        String[] line = new String[variables.size()];
        int index = 0;
        for(VariableAttributes v : variables){
            line[index] = v.getName().toString();
            index++;
        }
        this.writeNext(line);
    }

    public void writeDatabase(ResultSet rs, ArrayList<VariableAttributes> variables)throws SQLException{
        String[] line = null;
        Object response = null;
        int index = 0;
        int cols = variables.size();
        try{
            while(rs.next()){
                line = new String[cols];
                index = 0;
                for(VariableAttributes v : variables){
                    response = rs.getObject(v.getName().nameForDatabase());
                    if(response==null){
                        line[index] = "";
                    }else{
                        line[index] = response.toString();
                    }
                    index++;
                }
                this.writeNext(line);
                line = null;
            }
        }catch(SQLException ex){
            throw new SQLException(ex);
        }

    }

    public void writeScoredDatabase(ResultSet rs, ArrayList<VariableAttributes> variables)throws SQLException{
        String[] line = null;
        Object response = null;
        Double score = null;
        int cols = variables.size();
        int index = 0;
        try{
            while(rs.next()){
                line = new String[cols];
                index = 0;
                for(VariableAttributes v : variables){
                    if(v.getType().getItemType()== ItemType.BINARY_ITEM || v.getType().getItemType()==ItemType.POLYTOMOUS_ITEM){
                        response = rs.getObject(v.getName().nameForDatabase());
                        score = v.getItemScoring().computeItemScore(response);
                        line[index] = score.toString();
                    }else{
                        response = rs.getObject(v.getName().nameForDatabase());
                        if(response==null){
                            line[index] = "";
                        }else{
                            line[index] = response.toString();
                        }
                    }
                    index++;
                }
                this.writeNext(line);
                line = null;
            }
        }catch(SQLException ex){
            throw new SQLException(ex);
        }

    }


}

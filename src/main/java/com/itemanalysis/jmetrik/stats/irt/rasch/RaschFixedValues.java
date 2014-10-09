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

package com.itemanalysis.jmetrik.stats.irt.rasch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;

/**
 *
 * @author J. Patrick Meyer
 */
public class RaschFixedValues {

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public RaschFixedValues(){

    }

    private ArrayList<VariableName> getSelectedVariables(ArrayList<String> variables){
        ArrayList<VariableName> vName = new ArrayList<VariableName>();
        for(String s:variables){
            vName.add(new VariableName(s));
        }
        return vName;
    }

    public void setFixedParameterValues(Connection conn, DataTableName table, ItemResponseModel[] irm,  ArrayList<String> selectedVariables)throws SQLException{
        ArrayList<VariableName> fixedVariables = getSelectedVariables(selectedVariables);

        try{
            Table fTable = new Table(table.getNameForDatabase());
            SelectQuery query = new SelectQuery();
            query.addColumn(fTable, "*");
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery(query.toString());

            VariableName tempName = null;
            VariableName vName = new VariableName("name");
            String V = vName.nameForDatabase();
            VariableName ncatName = new VariableName("ncat");
            VariableName tau = null;
            VariableName difficulty = new VariableName("bparam");
            int ncat = 0;

            ItemResponseModel model;
            VariableName selectedVariable = null;
            double[] threshold = null;
            int index = 0;

            while(rs.next()){
                tempName = new VariableName(rs.getString(V));
                ncat = rs.getInt(ncatName.nameForDatabase());
                index = 0;

                if(fixedVariables.contains(tempName)){
                    for(ItemResponseModel m : irm){
                        if(tempName.equals(m.getName())){

                            //setFixed parameters
                            m.setDifficulty(rs.getDouble(difficulty.nameForDatabase()));
                            m.setProposalDifficulty(rs.getDouble(difficulty.nameForDatabase()));
                            if(ncat>2){
                                threshold = new double[ncat-1];
                                for(int i=0;i<ncat-1;i++){
                                    tau = new VariableName("step" + (i+1));
                                    threshold[i] = rs.getDouble(tau.nameForDatabase());
                                }
                                m.setThresholdParameters(threshold);
                                m.setProposalThresholds(threshold);
                            }
                            m.setFixed(true);

                        }
                    }
                }
            }
            rs.close();
            stmt.close();
        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex);
        }
    }


}


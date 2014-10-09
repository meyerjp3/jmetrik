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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.irt.estimation.JointMaximumLikelihoodEstimation;
import org.apache.log4j.Logger;

/**
 *
 * @author J. Patrick Meyer
 */
public class RaschResidualOut {

    private JointMaximumLikelihoodEstimation jmle = null;
    private ArrayList<VariableInfo> variables = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private DataTableName tableName = null;
    private DataTableName newTableName = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public RaschResidualOut(Connection conn, DatabaseAccessObject dao, JointMaximumLikelihoodEstimation jmle, DataTableName tableName, DataTableName newTableName){
        this.conn = conn;
        this.dao = dao;
        this.jmle = jmle;
        this.tableName = tableName;
        this.newTableName = newTableName;
        createVariables();
    }

    private void createVariables(){
        int column = 0;
        int index = 0;
        variables = new ArrayList<VariableInfo>();
        VariableInfo vInfo = null;
        String name = "";
        jmle.getItemResponseModelAt(index).getName();

        for(int i=0;i<jmle.getNumberOfItems();i++){
            name = jmle.getItemResponseModelAt(i).getName().toString();
            vInfo = new VariableInfo(name, (name + " residual"), VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
            variables.add(vInfo);
        }
    }

    public void outputToDb()throws SQLException{
        PreparedStatement pstmt = null;

        try{

            //start transaction
            conn.setAutoCommit(false);

            int nrow = 0;
            int ncol = variables.size();

            VariableTableName variableTableName = new VariableTableName(newTableName.toString());
            dao.createTables(conn, newTableName, variableTableName, variables);

            String updateString = "INSERT INTO " + newTableName.getNameForDatabase() + " VALUES(";
			for(int i=0;i<ncol;i++){
				updateString+= "?";
				if(i<ncol-1){
					updateString+=",";
				}else{
					updateString+=")";
				}
			}
            pstmt = conn.prepareStatement(updateString);
            double residual = 0.0;
            for(int i=0;i<jmle.getNumberOfPeople();i++){
//                scores = jmle.getItemResponseVector(i);
                //compute residuals and add to db
                for(int j=0;j<jmle.getNumberOfItems();j++){
                    residual = jmle.getResidualAt(i, j);
                    if(Double.isNaN(residual)){
                        pstmt.setNull(variables.get(j).positionInDb(), Types.DOUBLE);
                    }else{
                        pstmt.setDouble(variables.get(j).positionInDb(), residual);
                    }
                }

                pstmt.executeUpdate();
                nrow++;
            }

            //add row count to row count table
            dao.setTableInformation(conn, newTableName, nrow, "Rasch Analysis table of residuals for analysis of " +
                    tableName.toString() + ".");

            conn.commit();

        }catch(SQLException ex){
            conn.rollback();
			logger.fatal(ex.getMessage(), ex);
			throw new SQLException(ex.getMessage());
		}finally{
            conn.setAutoCommit(true);
            if(pstmt!=null) pstmt.close();
        }

    }

}


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

import java.sql.*;
import java.util.ArrayList;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.estimation.IrtEstimation;
import com.itemanalysis.psychometrics.irt.estimation.ItemResponseVector;
import com.itemanalysis.psychometrics.irt.estimation.JointMaximumLikelihoodEstimation;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import org.apache.log4j.Logger;

/**
 *
 * Creates a database table that contains the residuals values from a set of item response models.
 *
 * @author J. Patrick Meyer
 */
public class IrtResidualOut {

    private JointMaximumLikelihoodEstimation jmle = null;
    private ArrayList<VariableAttributes> variables = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private DataTableName tableName = null;
    private DataTableName newTableName = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    private ItemResponseVector[] itemResponseVector = null;
    private ItemResponseModel[] irm = null;
    private double[] theta = null;
    private int numberOfPeople = 0;
    private int numberOfItems = 0;
    private boolean usingJmle = true;

    /**
     * Use this constructor when saving a residual table from JointMaximumLikelihoodEstimation.java.
     * @param conn
     * @param dao
     * @param jmle
     * @param tableName
     * @param newTableName
     */
    public IrtResidualOut(Connection conn, DatabaseAccessObject dao, JointMaximumLikelihoodEstimation jmle, DataTableName tableName, DataTableName newTableName){
        this.conn = conn;
        this.dao = dao;
        this.jmle = jmle;
        this.tableName = tableName;
        this.newTableName = newTableName;
        this.irm = jmle.getItems();
        numberOfPeople = jmle.getNumberOfPeople();
        numberOfItems = irm.length;
        usingJmle = true;
        createVariables();
    }

    /**
     * Use this constructor for saving a residual table from MarginalMaximumLikelihoodEstimation.java.
     *
     * @param conn
     * @param dao
     * @param itemResponseVector
     * @param theta
     * @param irm
     * @param tableName
     * @param newTableName
     */
    public IrtResidualOut(Connection conn, DatabaseAccessObject dao, ItemResponseVector[] itemResponseVector, double[] theta, ItemResponseModel[] irm, DataTableName tableName, DataTableName newTableName){
        this.conn = conn;
        this.dao = dao;
        this.tableName = tableName;
        this.newTableName = newTableName;
        this.itemResponseVector = itemResponseVector;
        this.theta = theta;
        this.irm = irm;
        numberOfPeople = itemResponseVector.length;//NOTE: assumes one response vector per examinee.
        numberOfItems = irm.length;
        usingJmle = false;
        createVariables();
    }

    private void createVariables(){
        int column = 0;
        variables = new ArrayList<VariableAttributes>();
        VariableAttributes vInfo = null;
        String name = "";

        for(int j=0;j<numberOfItems;j++){
            name = irm[j].getName().toString();
            vInfo = new VariableAttributes(name, (name + " residual"), ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
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

            if(usingJmle){
                for(int i=0;i<numberOfPeople;i++){
                    //compute residuals and add to db
                    for(int j=0;j<numberOfItems;j++){
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
            }else{
                for(int i=0;i<numberOfPeople;i++){
                    //compute residuals and add to db
                    for(int j=0;j<numberOfItems;j++){
                        residual = itemResponseVector[i].getResponseAt(j) - irm[j].expectedValue(theta[i]);
                        if(Double.isNaN(residual)){
                            pstmt.setNull(variables.get(j).positionInDb(), Types.DOUBLE);
                        }else{
                            pstmt.setDouble(variables.get(j).positionInDb(), residual);
                        }
                    }
                    pstmt.executeUpdate();
                    nrow++;
                }
            }

            //add row count to row count table
            dao.setTableInformation(conn, newTableName, nrow, "IRT table of residuals for analysis of " +
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


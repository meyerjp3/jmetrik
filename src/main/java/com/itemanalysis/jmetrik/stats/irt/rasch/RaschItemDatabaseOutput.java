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

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.irt.estimation.JointMaximumLikelihoodEstimation;
import com.itemanalysis.psychometrics.irt.estimation.RaschFitStatistics;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.irt.model.RaschRatingScaleGroup;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;

public class RaschItemDatabaseOutput {

    int maxCat = 0;
    private ArrayList<VariableInfo> variables = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private DataTableName newTableName = null;
    private DataTableName currentTableName = null;
    private VariableInfo name = null;
    private VariableInfo model = null;
    private VariableInfo ncat = null;
    private VariableInfo group = null;
    private VariableInfo bparam = null;
    private VariableInfo se = null;
    private VariableInfo wms = null;
    private VariableInfo stdwms = null;
    private VariableInfo ums = null;
    private VariableInfo stdums = null;
    private VariableInfo extreme = null;
    private JointMaximumLikelihoodEstimation jmle = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");


    public RaschItemDatabaseOutput(Connection conn, DatabaseAccessObject dao,
                                DataTableName currentTableName, DataTableName newTableName, JointMaximumLikelihoodEstimation jmle){
        this.conn = conn;
        this.dao = dao;
        this.currentTableName = currentTableName;
        this.newTableName = newTableName;
        this.jmle = jmle;
        maxCat = jmle.getMaxCategory();
        variables = new ArrayList<VariableInfo>();
    }

    private void createVariables(){
        int column = 0;
        name = new VariableInfo("name", "Item Name", VariableType.NOT_ITEM, VariableType.STRING, ++column, "");
        model = new VariableInfo("model", "IRT Model", VariableType.NOT_ITEM, VariableType.STRING, ++column, "");
        ncat = new VariableInfo("ncat", "Number of categories", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        group = new VariableInfo("group", "Item group", VariableType.NOT_ITEM, VariableType.STRING, ++column, "");
        extreme = new VariableInfo("extreme", "Extreme value flag", VariableType.NOT_ITEM, VariableType.STRING, ++column, "");
        bparam = new VariableInfo("bparam", "Item difficulty", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        se = new VariableInfo("se", "Standard error", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        wms = new VariableInfo("wms", "Weighted mean squre - INFIT", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        stdwms = new VariableInfo("stdwms", "Standardized weighted mean square - INFIT", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        ums = new VariableInfo("ums", "Unweighted mean square - OUTFIT", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        stdums = new VariableInfo("stdums", "Standardized unweighted mean square - OUTFIT", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        variables.add(name);
        variables.add(model);
        variables.add(ncat);
        variables.add(group);
        variables.add(extreme);
        variables.add(bparam);
        variables.add(se);
        variables.add(wms);
        variables.add(stdwms);
        variables.add(ums);
        variables.add(stdums);

        VariableInfo step = null;
        VariableInfo cSe = null;
        VariableInfo cWms = null;
        VariableInfo cUms = null;

        if(maxCat>2){
            for(int i=1; i<maxCat;i++){
                step = new VariableInfo("step"+i, "Threshold for category " + i, VariableType.NOT_ITEM, VariableType.DOUBLE, column++, "");
                cSe = new VariableInfo("cse"+i, "Standard error for category " + i, VariableType.NOT_ITEM, VariableType.DOUBLE, column++, "");
                cWms = new VariableInfo("cwms"+i, "Weighted mean square for category " + i, VariableType.NOT_ITEM, VariableType.DOUBLE, column++, "");
                cUms = new VariableInfo("cums"+i, "Unweighted mean square for category " + i, VariableType.NOT_ITEM, VariableType.DOUBLE, column++, "");
                variables.add(step);
                variables.add(cSe);
                variables.add(cWms);
                variables.add(cUms);
            }
        }

    }

    public void outputToDb()throws SQLException {
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try{

            createVariables();

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
            int column = 11;
            int nrow = 0;

            ItemResponseModel irm = null;
            RaschRatingScaleGroup rsg = null;
            RaschFitStatistics fitStats = null;
            int extremeItem = 0;
            int k = 2;

            for(int j=0;j<jmle.getNumberOfItems();j++){
                irm = jmle.getItemResponseModelAt(j);
                fitStats = jmle.getItemFitStatisticsAt(j);
                extremeItem = jmle.getExtremeItemAt(j);
                k = irm.getNcat();

                //begin adding information to prepared statement.
                //Item name and model type set for all items.
                pstmt.setString(1, irm.getName().toString());

                if(k>2){
                    pstmt.setString(2, "PC3");
                }else{
                    pstmt.setString(2, "L3");
                }

                //Only add remaining information for items that have not been dropped.
                if(jmle.getDroppedStatusAt(j)==0){
                    int intEx = jmle.getExtremeItemAt(nrow);
                    String strExtreme = "No";
                    if(intEx==-1) strExtreme = "Minimum";
                    if(intEx==1) strExtreme = "Maximum";

                    pstmt.setDouble(3, irm.getNcat());
                    pstmt.setString(4, irm.getGroupId());
                    pstmt.setString(5, strExtreme);
                    pstmt.setDouble(6, irm.getDifficulty());
                    pstmt.setDouble(7, irm.getDifficultyStdError());
                    if(extremeItem!=0){
                        pstmt.setNull(8, Types.DOUBLE);
                        pstmt.setNull(9, Types.DOUBLE);
                        pstmt.setNull(10, Types.DOUBLE);
                        pstmt.setNull(11, Types.DOUBLE);
                    }else{
                        pstmt.setDouble(8, fitStats.getWeightedMeanSquare());
                        pstmt.setDouble(9, fitStats.getStandardizedWeightedMeanSquare());
                        pstmt.setDouble(10, fitStats.getUnweightedMeanSquare());
                        pstmt.setDouble(11, fitStats.getStandardizedUnweightedMeanSquare());
                    }

                    column=11;

                    if(maxCat>2){
                        rsg = jmle.getRatingScaleGroupAt(j);
                        for(int i=0;i<maxCat-1;i++){
                            if(rsg!=null && i<k-1){
                                pstmt.setDouble(++column, rsg.getThresholdAt(i));
                                pstmt.setDouble(++column, rsg.getThresholdStdErrorAt(i));
                                pstmt.setDouble(++column, rsg.getCategoryFitAt(i).getWeightedMeanSquare());
                                pstmt.setDouble(++column, rsg.getCategoryFitAt(i).getUnweightedMeanSquare());
                            }else{
                                pstmt.setNull(++column, Types.DOUBLE);
                                pstmt.setNull(++column, Types.DOUBLE);
                                pstmt.setNull(++column, Types.DOUBLE);
                                pstmt.setNull(++column, Types.DOUBLE);
                            }
                        }
                    }
                }else{
                    //for dropped items set all values to null
                    pstmt.setNull(3, Types.DOUBLE);
                    pstmt.setNull(4, Types.VARCHAR);
                    pstmt.setNull(5, Types.VARCHAR);
                    pstmt.setNull(6, Types.DOUBLE);
                    pstmt.setNull(7, Types.DOUBLE);
                    pstmt.setNull(8, Types.DOUBLE);
                    pstmt.setNull(9, Types.DOUBLE);
                    pstmt.setNull(10, Types.DOUBLE);
                    pstmt.setNull(11, Types.DOUBLE);
                    column=11;

                    if(maxCat>2){
                        for(int i=0;i<maxCat-1;i++){
                            pstmt.setNull(++column, Types.DOUBLE);
                            pstmt.setNull(++column, Types.DOUBLE);
                            pstmt.setNull(++column, Types.DOUBLE);
                            pstmt.setNull(++column, Types.DOUBLE);
                        }
                    }

                }

                pstmt.executeUpdate();
                nrow++;
            }
            pstmt.close();

            //add row count to row count table
            dao.setTableInformation(conn, newTableName, nrow, "Rasch item output table for analysis of " +
                    currentTableName.toString()  + ".");

        }catch(SQLException ex){
			logger.fatal(ex.getMessage(), ex);
			throw new SQLException(ex.getMessage());
		}finally{
            if(pstmt!=null) pstmt.close();
        }

    }

}

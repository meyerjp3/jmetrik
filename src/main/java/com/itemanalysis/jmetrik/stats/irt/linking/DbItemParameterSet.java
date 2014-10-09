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

package com.itemanalysis.jmetrik.stats.irt.linking;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DbItemParameterSet {

    public DbItemParameterSet(){

    }

    public LinkedHashMap<String, ItemResponseModel> getFormXItemParameters(Connection conn, DataTableName tableName,
                                                                      ArrayList<LinkingItemPair> commonItems,
                                                                      boolean logisticScale) throws SQLException{
        return getItemParameters(conn, tableName, commonItems, true, logisticScale);
    }

    public LinkedHashMap<String, ItemResponseModel> getFormYItemParameters(Connection conn, DataTableName tableName,
                                                                      ArrayList<LinkingItemPair> commonItems,
                                                                      boolean logisticScale) throws SQLException{
        return getItemParameters(conn, tableName, commonItems, false, logisticScale);
    }

    private LinkedHashMap<String, ItemResponseModel> getItemParameters(Connection conn, DataTableName tableName,
                                                                      ArrayList<LinkingItemPair> commonItems, boolean formX,
                                                                      boolean logisticScale) throws SQLException{

        LinkedHashMap<String, ItemResponseModel> irmSet = new LinkedHashMap<String, ItemResponseModel>();

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ItemResponseModel irm = null;
        VariableName itemName = new VariableName("name");//must be in item parameter table
        VariableName modelName = new VariableName("model");//must be in item parameter table
        VariableName ncatName = new VariableName("ncat");//must be in item parameter table
        VariableName aparam = new VariableName("aparam");
        VariableName bparam = new VariableName("bparam");
        VariableName cparam = new VariableName("cparam");
        VariableName scoreWeight = new VariableName("sweight");
        VariableName scale = new VariableName("scale");
        VariableName step = null;
        VariableName iName = null;

        double a = 1, b = 0, c = 0, D = 1.0, defaultD = 1.0;
        double[] stepParam = null;
        String model = "L3";
        int ncat = 2;
        int binaryModelParam = 1;//Rasch model by default

        if(logisticScale) defaultD = 1.0;
        else defaultD = 1.7;

        try{
            pstmt = conn.prepareStatement("SELECT * FROM " + tableName.getNameForDatabase() + " WHERE " + itemName.nameForDatabase() + "=?");

            //get meta data to check for variable names -- could be slow for some drivers
            ResultSetMetaData rsmd = pstmt.getMetaData();
            int ncols = rsmd.getColumnCount();
            ArrayList<VariableName> colNames = new ArrayList<VariableName>();
            for(int i=0;i<ncols;i++){
                VariableName vName = new VariableName(rsmd.getColumnName(i+1));
                colNames.add(vName);
            }

            //loop over common items and read values from database
            for(LinkingItemPair pair : commonItems){
                a = 1;
                b = 0;
                c = 0;
                D = defaultD;
                binaryModelParam = 1;

                if(formX){
                    iName = pair.getXVariable();
                }else{
                    iName = pair.getYVariable();
                }

                pstmt.setString(1, iName.toString());
                rs = pstmt.executeQuery();
                rs.next();

                //read resultset -- required fields
                model = rs.getString(modelName.nameForDatabase());
                ncat = rs.getInt(ncatName.nameForDatabase());

                //discrimination parameter -- optional
                if(colNames.contains(aparam)){
                    a = rs.getDouble(aparam.nameForDatabase());
                    if(rs.wasNull()){
                        a = 1.0;
                    }else{
                        binaryModelParam = 2;
                    }
                }else{
                    a = 1.0;
                }

                //scale factor -- optional
                if(colNames.contains(scale)){
                    D = rs.getDouble(scale.nameForDatabase());
                    if(rs.wasNull()) D = defaultD;
                }else{
                    D = defaultD;
                }

                //binary item response model
                if("L3".equals(model) || "L2".equals(model) || "L1".equals(model)){

                    //lower-asymptote parameter -- optional
                    if(colNames.contains(cparam)){
                        c = rs.getDouble(cparam.nameForDatabase());
                        if(rs.wasNull()){
                            c = 0.0;
                        }else{
                            binaryModelParam = 3;
                        }
                    }

                    //difficulty parameter -- required column for L3
                    b = rs.getDouble(bparam.nameForDatabase());

                    //Set specific type of binary model because irm constructor will
                    //determine number of parameters from constructor.
                    if(binaryModelParam==1){
                        irm = new Irm3PL(b, D);
                    }else if(binaryModelParam==2){
                        irm = new Irm3PL(a, b, D);
                    }else{
                        irm = new Irm3PL(a, b, c, D);
                    }

                }else{
                    //polytomous item response models

                    //all polytomous models have step parameter variables in database
                    stepParam = new double[ncat-1];
                    for(int k=1;k<ncat;k++){
                        step = new VariableName("step" + k);
                        stepParam[k-1] = rs.getDouble(step.nameForDatabase());
                    }

                    if("PC1".equals(model)){
                        irm = new IrmGPCM(a, stepParam, D);
                    }else if("PC2".equals(model)){
                        b = rs.getDouble(bparam.nameForDatabase());
                        irm = new IrmGPCM2(a, b, stepParam, D);
                    }else if("PC3".equals(model)){
                        b = rs.getDouble(bparam.nameForDatabase());
                        irm = new IrmPCM(b, stepParam, D);
                    }else if("GR".equals(model)){
                        irm = new IrmGRM(a, stepParam, D);
                    }

                }

                //read score weights if provided
                if(colNames.contains(scoreWeight)){
                    String s = rs.getString(scoreWeight.nameForDatabase());
                    String[] sa = s.split("\\s+");
                    double[] sw = new double[sa.length];
                    for(int i=0;i<sa.length;i++){
                        sw[i] = Double.parseDouble(sa[i]);
                    }
                    irm.setScoreWeights(sw);
                }

                irm.setName(iName);

                //common name given to formX and formY items
                irmSet.put(pair.getPairName(), irm);

            }//end loop over common items

            return irmSet;
        }catch(SQLException ex){
            throw(ex);
        }finally{
            if(pstmt!=null) pstmt.close();
        }



    }

    /**
     * Tihs methods will get IRT item parameter and model information from a database table.
     * It will get the information for a list of selected variables.
     *
     * @param conn connection to the database
     * @param tableName name of database table that contains item parameter and model information
     * @param selectedItems selected items for which database information is sought
     * @param logisticScale default IRT scale parameter
     * @return
     * @throws SQLException
     */
    public LinkedHashMap<String, ItemResponseModel> getItemParameters(Connection conn, DataTableName tableName,
                                                                       ArrayList<VariableName> selectedItems,
                                                                      boolean logisticScale) throws SQLException{

        LinkedHashMap<String, ItemResponseModel> irmSet = new LinkedHashMap<String, ItemResponseModel>();

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        ItemResponseModel irm = null;
        VariableName itemName = new VariableName("name");//must be in item parameter table
        VariableName modelName = new VariableName("model");//must be in item parameter table
        VariableName ncatName = new VariableName("ncat");//must be in item parameter table
        VariableName aparam = new VariableName("aparam");
        VariableName bparam = new VariableName("bparam");
        VariableName cparam = new VariableName("cparam");
        VariableName scoreWeight = new VariableName("sweight");
        VariableName scale = new VariableName("scale");
        VariableName step = null;

        double a = 1, b = 0, c = 0, D = 1.0, defaultD = 1.0;
        double[] stepParam = null;
        String model = "L3";
        int ncat = 2;
        int binaryModelParam = 1;//Rasch model by default

        if(logisticScale) defaultD = 1.0;
        else defaultD = 1.7;

        try{
            pstmt = conn.prepareStatement("SELECT * FROM " + tableName.getNameForDatabase() + " WHERE " + itemName.nameForDatabase() + "=?");

            //get meta data to check for variable names -- could be slow for some drivers
            ResultSetMetaData rsmd = pstmt.getMetaData();
            int ncols = rsmd.getColumnCount();
            ArrayList<VariableName> colNames = new ArrayList<VariableName>();
            for(int i=0;i<ncols;i++){
                VariableName vName = new VariableName(rsmd.getColumnName(i+1));
                colNames.add(vName);
            }

            for(VariableName v : selectedItems){
                a = 1;
                b = 0;
                c = 0;
                D = defaultD;
                binaryModelParam = 1;

                pstmt.setString(1, v.toString());
                rs = pstmt.executeQuery();
                rs.next();

                //read resultset -- required fields
                model = rs.getString(modelName.nameForDatabase());
                ncat = rs.getInt(ncatName.nameForDatabase());

                //discrimination parameter -- optional
                if(colNames.contains(aparam)){
                    a = rs.getDouble(aparam.nameForDatabase());
                    if(rs.wasNull()){
                        a = 1.0;
                    }else{
                        binaryModelParam = 2;
                    }
                }else{
                    a = 1.0;
                }

                //scale factor -- optional
                if(colNames.contains(scale)){
                    D = rs.getDouble(scale.nameForDatabase());
                    if(rs.wasNull()) D = defaultD;
                }else{
                    D = defaultD;
                }

                //binary item response model
                if("L3".equals(model) || "L2".equals(model) || "L1".equals(model)){

                    //lower-asymptote parameter -- optional
                    if(colNames.contains(cparam)){
                        c = rs.getDouble(cparam.nameForDatabase());
                        if(rs.wasNull()){
                            c = 0.0;
                        }else{
                            binaryModelParam = 3;
                        }
                    }

                    //difficulty parameter -- required column for L3
                    b = rs.getDouble(bparam.nameForDatabase());

                    //Set specific type of binary model because irm constructor will
                    //determine number of parameters from constructor.
                    if(binaryModelParam==1){
                        irm = new Irm3PL(b, D);
                    }else if(binaryModelParam==2){
                        irm = new Irm3PL(a, b, D);
                    }else{
                        irm = new Irm3PL(a, b, c, D);
                    }

                }else{
                    //polytomous item response models

                    //all polytomous models have step parameter variables in database
                    stepParam = new double[ncat-1];
                    for(int k=1;k<ncat;k++){
                        step = new VariableName("step" + k);
                        stepParam[k-1] = rs.getDouble(step.nameForDatabase());
                    }

                    if("PC1".equals(model)){
                        irm = new IrmGPCM(a, stepParam, D);
                    }else if("PC2".equals(model)){
                        b = rs.getDouble(bparam.nameForDatabase());
                        irm = new IrmGPCM2(a, b, stepParam, D);
                    }else if("PC3".equals(model)){
                        b = rs.getDouble(bparam.nameForDatabase());
                        irm = new IrmPCM(b, stepParam, D);
                    }else if("GR".equals(model)){
                        irm = new IrmGRM(a, stepParam, D);
                    }

                }

                //read score weights if provided
                if(colNames.contains(scoreWeight)){
                    String s = rs.getString(scoreWeight.nameForDatabase());
                    String[] sa = s.split("\\s+");
                    double[] sw = new double[sa.length];
                    for(int i=0;i<sa.length;i++){
                        sw[i] = Double.parseDouble(sa[i]);
                    }
                    irm.setScoreWeights(sw);
                }

                irm.setName(v);

                //add irm to collection
                irmSet.put(irm.getName().toString(), irm);
            }

            return irmSet;
        }catch(SQLException ex){
            throw(ex);
        }finally{
            if(pstmt!=null) pstmt.close();
        }


    }




}

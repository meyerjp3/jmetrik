/*
 * Copyright (c) 2013 Patrick Meyer
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

package com.itemanalysis.jmetrik.stats.itemanalysis;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.dao.DerbyDatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.measurement.ClassicalItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

public class ItemAnalysisOutputTable {

    private Connection conn = null;
    private DataTableName oldTableName = null;
    private DataTableName tableName = null;
    private VariableTableName variableTableName = null;
    private Statement stmt = null;
    private PreparedStatement pstmt = null;
    private DatabaseAccessObject dao = null;

    public ItemAnalysisOutputTable(Connection conn, DataTableName oldTableName, DataTableName tableName){
        this.conn = conn;
        this.oldTableName = oldTableName;
        this.tableName = tableName;
        variableTableName = new VariableTableName(tableName.toString());
    }

    public void saveOutput(TreeMap<Integer, ClassicalItem> itemTreeMap, boolean allCategories, boolean addDIndex)throws SQLException {

        ArrayList<VariableAttributes> variables = new ArrayList<VariableAttributes>();
        int nItems = itemTreeMap.size();

        int maxCategory = 0;
        ClassicalItem item = null;
        for(Integer i : itemTreeMap.keySet()){
            item = itemTreeMap.get(i);
            maxCategory = Math.max(maxCategory, item.numberOfCategories());
        }
        int totalColumns = 4+3*maxCategory;

        VariableAttributes var1 = new VariableAttributes("name", "Item Name", ItemType.NOT_ITEM, DataType.STRING, 1, "");
        VariableAttributes var2 = new VariableAttributes("difficulty", "Item Difficulty", ItemType.NOT_ITEM, DataType.DOUBLE, 2, "");
        VariableAttributes var3 = new VariableAttributes("stdev", "Item Standard Deviation", ItemType.NOT_ITEM, DataType.DOUBLE, 3, "");
        VariableAttributes var4 = new VariableAttributes("discrimination", "Item Discrimination", ItemType.NOT_ITEM, DataType.DOUBLE, 4, "");
        variables.add(var1);
        variables.add(var2);
        variables.add(var3);
        variables.add(var4);

        VariableAttributes lower = null;
        VariableAttributes upper = null;
        VariableAttributes dIndex = null;
        if(addDIndex){
            lower = new VariableAttributes("lower", "Difficulty for lower 27%", ItemType.NOT_ITEM, DataType.DOUBLE, 5, "");
            upper = new VariableAttributes("upper", "Difficulty for upper 27%", ItemType.NOT_ITEM, DataType.DOUBLE, 6, "");
            dIndex = new VariableAttributes("D_index", "Discrimination index", ItemType.NOT_ITEM, DataType.DOUBLE, 7, "");
            variables.add(lower);
            variables.add(upper);
            variables.add(dIndex);
        }

        VariableAttributes vProp = null;
        VariableAttributes vSD = null;
        VariableAttributes vCor = null;

        int colNumber = 5;
        if(addDIndex) colNumber = 8;

        if(allCategories){
            for(int k=0;k<maxCategory;k++){
                vProp = new VariableAttributes("prop"+(k+1), "Proportion endorsing option " + (k+1), ItemType.NOT_ITEM, DataType.DOUBLE, colNumber++, "");
                vSD = new VariableAttributes("stdev"+(k+1), "Std. Dev. for option " + (k+1), ItemType.NOT_ITEM, DataType.DOUBLE, colNumber++, "");
                vCor = new VariableAttributes("cor"+(k+1), "Distractor-total correlation for option " + (k+1), ItemType.NOT_ITEM, DataType.DOUBLE, colNumber++, "");

                variables.add(vProp);
                variables.add(vSD);
                variables.add(vCor);
            }
        }

        int n = 0;

        //start transaction
        conn.setAutoCommit(false);



        try{
            //add values to table
            createTables(variables);

            String sqlString = "INSERT INTO " + tableName.getNameForDatabase() + " VALUES(?,?,?,?";

            if(addDIndex){
                sqlString += ",?,?,?";
            }

            if(allCategories){
                for(int k=0;k<maxCategory;k++){
                    sqlString += ",?,?,?";//three variables per category
                }
            }
            sqlString += ")";
            pstmt = conn.prepareStatement(sqlString);

            int itemCat = 0;
            int index = 1;
            double df = 0, sd = 0, ds = 0, dL = 0, dU = 0, D = 0;
            for(Integer i : itemTreeMap.keySet()){
                index=1;

                item = itemTreeMap.get(i);
                pstmt.setString(index++, item.getName().toString());

                df = item.getDifficulty();
                if(Double.isNaN(df)){
                    pstmt.setNull(index++, Types.DOUBLE);
                }else{
                    pstmt.setDouble(index++, df);
                }

                sd = item.getStdDev();
                if(Double.isNaN(sd)){
                    pstmt.setNull(index++, Types.DOUBLE);
                }else{
                    pstmt.setDouble(index++, sd);
                }

                ds = item.getDiscrimination();
                if(Double.isNaN(ds)){
                    pstmt.setNull(index++, Types.DOUBLE);
                }else{
                    pstmt.setDouble(index++, ds);
                }

                if(addDIndex){
                    dL = item.getDindexLower();

                    if(Double.isNaN(dL)){
                        pstmt.setNull(index++, Types.DOUBLE);
                    }else{
                        pstmt.setDouble(index++, dL);
                    }

                    dU = item.getDindexUpper();
                    if(Double.isNaN(dU)){
                        pstmt.setNull(index++, Types.DOUBLE);
                    }else{
                        pstmt.setDouble(index++, dU);
                    }

                    D = dU-dL;
                    if(Double.isNaN(D)){
                        pstmt.setNull(index++, Types.DOUBLE);
                    }else{
                        pstmt.setDouble(index++, D);
                    }
                }


                if(allCategories){
                    Object temp;
                    Iterator<Object> iter = item.categoryIterator();

                    while(iter.hasNext()){
                        temp = iter.next();

                        //category difficulty
                        df = item.getDifficultyAt(temp);
                        if(Double.isNaN(df)){
                            pstmt.setNull(index++, Types.DOUBLE);
                        }else{
                            pstmt.setDouble(index++, df);
                        }

                        //category sd
                        sd = item.getStdDevAt(temp);
                        if(Double.isNaN(sd)){
                            pstmt.setNull(index++, Types.DOUBLE);
                        }else{
                            pstmt.setDouble(index++, sd);
                        }

                        //category discrimination
                        ds = item.getDiscriminationAt(temp);
                        if(Double.isNaN(ds)){
                            pstmt.setNull(index++, Types.DOUBLE);
                        }else{
                            pstmt.setDouble(index++, ds);
                        }
                    }//end loop over categories

                    //index should be equal to totalColumns
                    // if not, add null values to remaining columns
                    while(index<=totalColumns){
                        pstmt.setNull(index++, Types.DOUBLE);

                    }

                }

                n += pstmt.executeUpdate();

            }//end loop over items

            updateRowCount(n);

            //commit transaction
            conn.commit();

        }catch(SQLException ex){
            conn.rollback();
            throw(ex);
        }finally{
            conn.setAutoCommit(true);
            if(stmt!=null) stmt.close();
            if(pstmt!=null) pstmt.close();
        }

    }

    private void updateRowCount(int n)throws SQLException{
        String desc = "Item Analysis output for " + oldTableName.toString();
        dao.setTableInformation(conn, tableName, n, desc);
    }

    private void createTables(ArrayList<VariableAttributes> variables)throws SQLException{

        //get type of database according to properties
        JmetrikPreferencesManager preferencesManager = new JmetrikPreferencesManager();
        String dbType = preferencesManager.getDatabaseType();
        JmetrikDatabaseFactory dbFactory = null;

        if(DatabaseType.APACHE_DERBY.toString().equals(dbType)){
            dao = new DerbyDatabaseAccessObject();
            dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
        }else if(DatabaseType.MYSQL.toString().equals(dbType)){
            //not yet implemented
        }else{
            //default is apache derby
            dao = new DerbyDatabaseAccessObject();
            dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
        }

        dao = dbFactory.getDatabaseAccessObject();
        dao.createTables(conn, tableName, variableTableName, variables);

    }


}

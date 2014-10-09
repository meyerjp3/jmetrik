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

package com.itemanalysis.jmetrik.stats.cmh;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.dao.DerbyDatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.psychometrics.cmh.CochranMantelHaenszel;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;

import java.sql.*;
import java.util.ArrayList;
import java.util.TreeMap;

public class CmhOutputTable {

    private DataTableName dataTableName;
    private DataTableName oldTableName;
    private VariableTableName variableTableName;
    private Connection conn;
    private Statement stmt = null;
    private PreparedStatement pstmt = null;
    private DatabaseAccessObject dao = null;

    public CmhOutputTable(Connection conn, DataTableName oldTableName, DataTableName dataTableName){
        this.conn = conn;
        this.oldTableName = oldTableName;
        this.dataTableName = dataTableName;
        variableTableName = new VariableTableName(dataTableName.toString());
    }

    public void saveOutput(TreeMap<Integer, CochranMantelHaenszel> cmhTreeMap)throws SQLException {

        VariableInfo var1 = new VariableInfo("name", "Item Name", VariableType.NOT_ITEM, VariableType.STRING, 1, "");
        VariableInfo var2 = new VariableInfo("chisq", "Mantel-Haenszel Chi-square", VariableType.NOT_ITEM, VariableType.DOUBLE, 2, "");
        VariableInfo var3 = new VariableInfo("pvalue", "Chi-square p-value", VariableType.NOT_ITEM, VariableType.DOUBLE, 3, "");
        VariableInfo var4 = new VariableInfo("n", "Valid Sample Size", VariableType.NOT_ITEM, VariableType.DOUBLE, 4, "");
        VariableInfo var5 = new VariableInfo("effectsize", "Effect Size", VariableType.NOT_ITEM, VariableType.DOUBLE, 5, "");
        VariableInfo var6 = new VariableInfo("lower", "95% Confidence Interval Lower bound", VariableType.NOT_ITEM, VariableType.DOUBLE, 6, "");
        VariableInfo var7 = new VariableInfo("upper", "95% Confidence Interval Upper bound", VariableType.NOT_ITEM, VariableType.DOUBLE, 7, "");
        VariableInfo var8 = new VariableInfo("etsclass", "ETS DIF CLassification", VariableType.NOT_ITEM, VariableType.STRING, 8, "");

        ArrayList<VariableInfo> variables = new ArrayList<VariableInfo>();
        variables.add(var1);
        variables.add(var2);
        variables.add(var3);
        variables.add(var4);
        variables.add(var5);
        variables.add(var6);
        variables.add(var7);
        variables.add(var8);

        int n = 0;

        //start transaction
        conn.setAutoCommit(false);

        try{
            createTables(variables);

            //add values to table
            pstmt = conn.prepareStatement("INSERT INTO " + dataTableName.getNameForDatabase() +
                    " VALUES(?,?,?,?,?,?,?,?)");

            for(Integer i : cmhTreeMap.keySet()){
                CochranMantelHaenszel cmh = cmhTreeMap.get(i);
                String dbString = cmh.getDatabaseString();
                String[] results = dbString.split(",");

                pstmt.setString(1, cmh.getVariableName().toString());

                //chi-square value
                if(results[1].equals("")){
                    pstmt.setNull(2, Types.DOUBLE);
                }else{
                    pstmt.setDouble(2, Double.parseDouble(results[1]));
                }

                //p-value
                if(results[2].equals("")){
                    pstmt.setNull(3, Types.DOUBLE);
                }else{
                    pstmt.setDouble(3, Double.parseDouble(results[2]));
                }

                //valid sample size
                pstmt.setDouble(4, Double.parseDouble(results[3]));

                //effectSize
                if(results[4].equals("")){
                    pstmt.setNull(5, Types.DOUBLE);
                }else{
                    pstmt.setDouble(5, Double.parseDouble(results[4]));
                }

                //confidence interval lower bound
                if(results[5].equals("")){
                    pstmt.setNull(6, Types.DOUBLE);
                }else{
                    pstmt.setDouble(6, Double.parseDouble(results[5]));
                }

                //confidence interval upper bound
                if(results[6].equals("")){
                    pstmt.setNull(7, Types.DOUBLE);
                }else{
                    pstmt.setDouble(7, Double.parseDouble(results[6]));
                }

                //ets classification
                pstmt.setString(8, results[7]);

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
        String desc = "CMH Analysis output for " + oldTableName.toString();
        dao.setTableInformation(conn, dataTableName, n, desc);
    }

    private void createTables(ArrayList<VariableInfo> variables)throws SQLException{

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
        dao.createTables(conn, dataTableName, variableTableName, variables);

//        //create variable table
//        stmt = conn.createStatement();
//        String sqlString = "CREATE TABLE " + variableTableName.getNameForDatabase() +
//                " (" +
//                "VARIABLE VARCHAR(30)," + //name
//                "VARGROUP VARCHAR(30)," + //group
//                "SCORING VARCHAR(250)," +  //scoring
//                "ITEMTYPE SMALLINT," +    //item type
//                "DATATYPE SMALLINT," +    //data type
//                "LABEL VARCHAR(150))";     //label
//        stmt = conn.createStatement();
//        stmt.execute(sqlString);
//
//        //Populate variable table
//        sqlString = "INSERT INTO " + variableTableName.getNameForDatabase() + " VALUES(?, ?, ?, ?, ?, ?)";
//        pstmt = conn.prepareStatement(sqlString);
//        for(VariableInfo v : variables){
//            pstmt.setString(1, v.getName().toString());                 //name
//            pstmt.setString(2, v.getSubscale());                        //subscale/group
//            pstmt.setString(3, v.printOptionScoreKey());                //scoring
//            pstmt.setInt(4, v.getType().getItemType());                 //item type
//            pstmt.setInt(5, v.getType().getDataType());                 //data type
//            pstmt.setString(6, v.getLabel().toString());                //label
//            pstmt.executeUpdate();
//        }
//
//        //create data table
//        sqlString = "CREATE TABLE " + dataTableName.getNameForDatabase() + " (";
//        int counter = 0;
//        for(VariableInfo v : variables){
//            if(counter>0) sqlString += ", ";
//            sqlString += v.getName().nameForDatabase() + " " + v.getDatabaseTypeString();
//            counter++;
//        }
//        sqlString+=")";
//        stmt.execute(sqlString);




    }

}

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

package com.itemanalysis.jmetrik.dao;

import com.itemanalysis.jmetrik.commandbuilder.PairedOptionList;
import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.scoring.ScoringCommand;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.SqlSafeTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.workspace.JmetrikPreferencesManager;
import com.itemanalysis.psychometrics.data.*;
import com.itemanalysis.psychometrics.irt.model.*;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import com.itemanalysis.squiggle.criteria.MatchCriteria;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.*;
import java.sql.*;
import java.util.*;

/**
 * This class performs functions at a database level. For example, it
 * will create models for lists of databases and a list of tables
 * within a specific database.
 *
 */
public class DerbyDatabaseAccessObject implements DatabaseAccessObject {

    /**
     * This table contains information about each data table in the database.
     */
    private static final String TABLE_OF_ROW_COUNTS = "JMKTBLROWS";

    /**
     * This variable names are always part of the table TABLE_OF_ROW_COUNTS.
     */
    private VariableName sqlTableName = null;
    private VariableName sqlRowCount = null;
    private VariableName sqlDescription = null;
    
    public DerbyDatabaseAccessObject(){
        sqlTableName = new VariableName("tablename");
        sqlRowCount = new VariableName("rowcount");
        sqlDescription = new VariableName("description");
    }

    //=================================================================================================================================
    // Database level methods
    //=================================================================================================================================
    
    public DefaultTreeModel getDatabaseTreeModel(Connection conn)throws SQLException{
        DatabaseMetaData m = null;
        ResultSet tables = null;
        Statement stmt = null;
        try{
            String url = conn.getMetaData().getURL();
            String[] url2 = url.split(":");
            String dbName = url2[url2.length-1];
            DefaultMutableTreeNode root = new DefaultMutableTreeNode(dbName);
            stmt = conn.createStatement();
            m = conn.getMetaData();
            String[] types = {"TABLE"};
            tables = m.getTables(null, "%", "%", types);
            String tableName = "";
            DataTableName tempTableName = null;
            DefaultMutableTreeNode tableNode = null;

            TreeMap<DataTableName, DefaultMutableTreeNode> tableNodes = new TreeMap<DataTableName, DefaultMutableTreeNode>();

            //create tables and add corresponding tables using a HashMap
            while(tables.next()){
                tableName = tables.getString("TABLE_NAME");
                if(tableName.trim().toUpperCase().startsWith("TBL")){
                    tempTableName = new DataTableName(tableName);
                    tableNode = new DefaultMutableTreeNode(tempTableName, false);

                    tableNodes.put(tempTableName, tableNode);
                }

            }
            tables.close();

            //add HashMap contents to root node
            for(DataTableName tName : tableNodes.keySet()){
                tableNode = tableNodes.get(tName);
                root.add(tableNode);
            }

            //create tree model
            DefaultTreeModel model = new DefaultTreeModel(root, true);
            return model;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(tables!=null) tables.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * Get list of databases in a system.
     *
     * @param path path to location of databases (i.e. database home directory).
     * @return
     * @throws IOException
     */
    public SortedListModel<DatabaseName> getDatabaseListModel(String path)throws IOException {
        File dir = new File(path);
        FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(File file) {
                return file.isDirectory();
            }
        };
        File[] folders = dir.listFiles(filter);

        SortedListModel<DatabaseName> listModel = new SortedListModel<DatabaseName>();
        for(File f : folders){
            listModel.addElement(new DatabaseName(f.getName()));
        }
        return listModel;
    }


    /**
     * Get list of tables within a database.
     *
     * @param conn Connection to database
     * @return
     * @throws SQLException
     */
    public SortedListModel<DataTableName> getTableListModel(Connection conn)throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try{
            String[] types={"TABLE"};
            String temp = "";
            DataTableName tableName = null;
            SortedListModel<DataTableName> listModel = new SortedListModel<DataTableName>();

            DatabaseMetaData dbmd = conn.getMetaData();
            rs = dbmd.getTables(null, null, "%", types);

            while(rs.next()){
                temp=rs.getString(3);
                if(temp.trim().toUpperCase().startsWith("TBL")){
                    tableName = new DataTableName(temp);
                    listModel.addElement(new DataTableName(tableName.toString()));
                }
            }

            return listModel;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
        }

    }

    /**
     * this method modifes the database so that it includes columns added to
     * the database in version 3 (December 2012).
     *
     * Prior to version 3 of jMetrik, TABLE_OF_ROW_COUNTS did not contain a column
     * for the table description. This method checks for this column and adds it
     * if it is not there.
     *
     * Also, variable tables did not contain column for an omit code or a column
     * for a not reached code. Add them now if they do not exist.
     *
     * @param conn
     * @throws SQLException
     */
    public void updateDatabasesVersion(Connection conn)throws SQLException, IOException{
        Statement stmt = null;
        ResultSet rs = null;

        conn.setAutoCommit(false);//begin transaction

        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            //add table description column to TABLE_OF_ROW_COUNTS================================================
            VariableName sqlDescription = new VariableName("description");
            String sqlString = "SELECT * FROM " + TABLE_OF_ROW_COUNTS;
            rs = stmt.executeQuery(sqlString);
            ResultSetMetaData rsmd = rs.getMetaData();
            int ncols = rsmd.getColumnCount();
            String tempName = "";
            int hasColumnDescription = 0;

            for(int i=1;i<=ncols;i++){
                tempName = rsmd.getColumnName(i).toLowerCase();
                if(sqlDescription.nameForDatabase().toLowerCase().equals(tempName)) hasColumnDescription++;
            }

            if(hasColumnDescription==0){
                sqlString = "ALTER TABLE " + TABLE_OF_ROW_COUNTS + " ADD COLUMN " +
                        sqlDescription.nameForDatabase() + " VARCHAR(1000)";
                stmt.execute(sqlString);
            }
            rs.close();

            //now add new columns to all variable tables=========================================================
            String[] types={"TABLE"};
            String temp = "";
            VariableTableName variableTableName = null;
            ArrayList<VariableTableName> variableTables = new ArrayList<VariableTableName>();

            DatabaseMetaData dbmd = conn.getMetaData();
            rs = dbmd.getTables(null, null, "%", types);

            //create list of all variable tables in db
            while(rs.next()){
                temp=rs.getString(3);
                if(temp.trim().toUpperCase().startsWith("VTBL")){
                    variableTableName = new VariableTableName(temp);
                    variableTables.add(variableTableName);
                }
            }
            rs.close();

            //add new variable table columns to variable table if they do not exist
            for(VariableTableName v : variableTables){

//                System.out.println("DerbyDatabaseAccessObject: Updating table: " + v.getNameForDatabase());

                sqlString = "SELECT * FROM " + v.getNameForDatabase();
                rs = stmt.executeQuery(sqlString);
                rsmd = rs.getMetaData();
                ncols = rsmd.getColumnCount();
                tempName = "";
                int hasOmitColumn = 0;
                int hasNotReachedColumn = 0;

                //check variable table for new columns
                for(int i=1;i<=ncols;i++){
                    tempName = rsmd.getColumnName(i).toLowerCase();
                    if(tempName.toLowerCase().equals("omitcode")) hasOmitColumn++;
                    if(tempName.toLowerCase().equals("notreachedcode")) hasNotReachedColumn++;
                }

                //new columns not found. Add them.
                if(hasOmitColumn==0){
                    System.out.println(" omit ALTER TABLE: " + v.toString());

                    sqlString = "ALTER TABLE " + v.getNameForDatabase() + " ADD COLUMN OMITCODE VARCHAR(30)";
                    stmt.execute(sqlString);
                }

                if(hasNotReachedColumn==0){
                    System.out.println(" not reached ALTER TABLE: " + v.toString());

                    sqlString = "ALTER TABLE " + v.getNameForDatabase() + " ADD COLUMN NOTREACHEDCODE VARCHAR(30)";
                    stmt.execute(sqlString);
                }

            }

            conn.commit();
            conn.setAutoCommit(true);

            //add properties file to databasehome
            JmetrikPreferencesManager prefs = new JmetrikPreferencesManager();
            String dbHome = prefs.getDatabaseHome();
            Properties props = new Properties();

            //create properties file in the database folder to indicate the current version of the database
            Properties p = new Properties();
            File f = new File(dbHome + "/jmetrik-db-version.props");
            if(!f.exists()) f.createNewFile();
            FileInputStream in = new FileInputStream(f);
            p.load(in);
            in.close();

            String dbName = dbHome + "/" + conn.getMetaData().getURL();
            dbName = dbName.replaceAll("[\\\\/:]+", ".");
            dbName = dbName.replaceAll("[.]+", ".");
            p.setProperty(dbName, "version3");

            FileOutputStream out = new FileOutputStream(f);
            p.store(out, "#DO NOT MODIFY - JMETRIK CONFIGURATION FILE - DO NOT MODIFY");
            out.close();

        }catch(SQLException ex){
            conn.rollback();
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }
    }

    public void createTables(Connection conn, DataTableName dataTableName, VariableTableName variableTableName, LinkedHashMap<String, VariableAttributes> variables)throws SQLException{
        ArrayList<VariableAttributes> variableAttributeses = new ArrayList<VariableAttributes>();
        for(String s : variables.keySet()){
            variableAttributeses.add(variables.get(s));
        }
        createTables(conn, dataTableName, variableTableName, variableAttributeses);
    }

    public void createTables(Connection conn, DataTableName dataTableName, VariableTableName variableTableName, ArrayList<VariableAttributes> variables)throws SQLException{

        Statement stmt = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        //start transaction
        conn.setAutoCommit(false);

        try{

            //create variable table
            stmt = conn.createStatement();
            String sqlString = "CREATE TABLE " + variableTableName.getNameForDatabase() +
                    " (" +
                    "VARIABLE VARCHAR(30)," +           //name
                    "VARGROUP VARCHAR(30)," +           //group
                    "SCORING VARCHAR(250)," +           //scoring
                    "ITEMTYPE SMALLINT," +              //item type
                    "DATATYPE SMALLINT," +              //data type
                    "LABEL VARCHAR(150)," +              //label
                    "OMITCODE VARCHAR(30)," +            //omit code
                    "NOTREACHEDCODE VARCHAR(30))";      //not reached code
            stmt = conn.createStatement();
            stmt.execute(sqlString);

            //Populate variable table
            sqlString = "INSERT INTO " + variableTableName.getNameForDatabase() + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(sqlString);

            ItemType it;
            DataType dt;
            for(VariableAttributes v : variables){

                pstmt.setString(1, v.getName().toString());                 //name
                pstmt.setString(2, v.getItemGroup());                        //subscale/group
                pstmt.setString(3, v.printOptionScoreKey());                //scoring

                it = v.getType().getItemType();
                int itemInt = ItemType.toInt(it);
                dt = v.getType().getDataType();
                int dataInt = dt.toInt(dt);

                pstmt.setInt(4, itemInt);                 //item type
                pstmt.setInt(5, dataInt);                 //data type
                pstmt.setString(6, v.getLabel().toString());                //label
                pstmt.setNull(7, Types.VARCHAR);                            //omit code initially set to null
                pstmt.setNull(8, Types.VARCHAR);                            //not reached code initially set to null
                pstmt.executeUpdate();
            }

            //create data table
            sqlString = "CREATE TABLE " + dataTableName.getNameForDatabase() + " (";
            int counter = 0;
            for(VariableAttributes v : variables){
                if(counter>0) sqlString += ", ";
                /**
                 * The next line was added on April 19, 2014. It uses escaped double-quotes when creating the
                 * column names. The double quotes are needed because without them, an occasional lexical error
                 * would occur with some column names. The earlier version of this line of code was
                 *
                 * sqlString += v.getName().nameForDatabase() + " " + v.getDatabaseTypeString();
                 *
                 * and it would infrequently and unpredictably result in a lexical error.
                 *
                 * The upper case in the new line is also needed because quoted column names would result in
                 * case-sensitive column names. Derby uses upper case to store column names in a
                 * case-insensitive fashion.
                 */
                sqlString += "\""+ v.getName().nameForDatabase().toUpperCase() + "\" " + v.getDatabaseTypeString();
                counter++;
            }
            sqlString+=")";

            stmt = conn.createStatement();
            stmt.execute(sqlString);

            //add table name to table information table, TABLE_OF_ROW_COUNTS
            sqlString = "INSERT INTO " +  TABLE_OF_ROW_COUNTS + " (" + sqlTableName.nameForDatabase() + ") " +
                    "VALUES ('" + dataTableName.getNameForDatabase() + "')";

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            stmt.execute(sqlString);

            //end transaction
            conn.commit();

        }catch(SQLException ex){
            ex.printStackTrace();
            conn.rollback();
            conn.setAutoCommit(true);
            throw ex;
        }finally {
            conn.setAutoCommit(true);
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
            if(pstmt!=null) pstmt.close();
        }

    }

    public TreeSet<DataTableName> getExistingTableNames(Connection conn)throws SQLException{
        String url = conn.getMetaData().getURL();
        String[] url2 = url.split(":");
        String dbName = url2[url2.length-1];
        Statement stmt = null;
        ResultSet tables = null;

        try{
            stmt = conn.createStatement();
            DatabaseMetaData m = conn.getMetaData();
            String[] types = {"TABLE"};
            tables = m.getTables(null, "%", "%", types);
            String tableName = "";
            DataTableName tempTableName = null;

            TreeSet<DataTableName> tableNameSet = new TreeSet<DataTableName>();

            //create tables and add corresponding tables using a HashMap
            while(tables.next()){
                tableName = tables.getString("TABLE_NAME");
                if(tableName.trim().toUpperCase().startsWith("TBL")){
                    tempTableName = new DataTableName(tableName);
                    tableNameSet.add(tempTableName);
                }

            }
            return tableNameSet;

        }catch(SQLException ex){
            throw ex;
        }finally {
            if(tables!=null) tables.close();
            if(stmt!=null) stmt.close();
        }
    }

    public boolean isTableNameUnique(Connection conn, String originalName)throws SQLException{
        DataTableName propName = new DataTableName(originalName);
        TreeSet<DataTableName> tableNameSet = getExistingTableNames(conn);
        return !tableNameSet.contains(propName);
    }

    public DataTableName getUniqueTableName(Connection conn, String originalName)throws SQLException {
        TreeSet<DataTableName> tableNameSet = getExistingTableNames(conn);
        DataTableName propName = new DataTableName(originalName);
        int i=0;
        while(tableNameSet.contains(propName)){
            i++;
            propName = new DataTableName(originalName+i);
        }
        return propName;
    }

    //=================================================================================================================================
    // Table level methods
    //=================================================================================================================================

    public Object[][] getData(Connection conn, SelectQuery select, int numRow, int numCol)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(select.toString());
            Object[][] data = new Object[numRow][numCol];
            int i=0;
            while(rs.next()){
                for(int j=0;j<numCol;j++){
                    data[i][j]=rs.getObject(j+1);
                }
                i++;
            }
            return data;
        }catch(SQLException ex){
            throw ex;
        }finally {
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    public Object[] getDataFromColumn(Connection conn, DataTableName tableName, VariableName variableName)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        try{
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery query = new SelectQuery();
            query.addColumn(sqlTable, variableName.nameForDatabase());

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(query.toString());
            rs.last();
            int numRow = rs.getRow();
            rs.beforeFirst();

            Object[] data = new Object[numRow];
            int i=0;
            while(rs.next()){
                data[i]=rs.getObject(1);
                i++;
            }
            return data;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    public ArrayList<VariableAttributes> getVariableAttributesFromColumn(Connection conn, DataTableName tableName, VariableName variableName)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        try{
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery query = new SelectQuery();
            query.addColumn(sqlTable, variableName.nameForDatabase());

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(query.toString());

            ArrayList<VariableAttributes> variables = new ArrayList<VariableAttributes>();
            VariableAttributes tempInfo = null;
            int index=0;
            Object temp = null;
            while(rs.next()){
                temp=rs.getObject(variableName.nameForDatabase());
                tempInfo = new VariableAttributes(temp.toString(), "", ItemType.NOT_ITEM, DataType.DOUBLE, index, "");
                variables.add(tempInfo);
                index++;
            }
            return variables;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }
    }

//    public int getRowCount(Connection conn, SqlSafeTableName tableName)throws SQLException{
//        DerbyRowCounter rowCounter = new DerbyRowCounter();
//        int rows = rowCounter.getRowCount(conn, tableName);
//        return rows;
//    }

    public int getColumnCount(Connection conn, DataTableName tableName)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        try{
            String select = "SELECT * FROM " + tableName.getNameForDatabase();
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(select);
            ResultSetMetaData rsmd = rs.getMetaData();
            int numCol = rsmd.getColumnCount();
            return numCol;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    public VariableName[] getColumnNames(Connection conn, DataTableName tableName)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            String select = "SELECT * FROM " + tableName.getNameForDatabase();
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(select);
            ResultSetMetaData rsmd = rs.getMetaData();
            int cols = rsmd.getColumnCount();
            VariableName[] vName = new VariableName[cols];
            for(int i=0;i<cols;i++){
                vName[i] = new VariableName(rsmd.getColumnName(i+1));
            }
            return vName;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    public Class[] getColumnClass(Connection conn, DataTableName tableName)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            String select = "SELECT * FROM " + tableName.getNameForDatabase();
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(select);
            ResultSetMetaData rsmd = rs.getMetaData();
            int numCol = rsmd.getColumnCount();
            int type = 0;
            Class[] classes = new Class[numCol];
            for(int i=1;i<=numCol;i++){
                type = rsmd.getColumnType(i);
                if(type==Types.DOUBLE){
                    classes[i-1]=Double.class;
                }else{
                    classes[i-1]=String.class;
                }
            }
            return classes;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * The Integer keys in the dataToSave TreeMap correspond to database rows. If the SQL query
     * uses an offset value, these keys are assumed to be consistent with the offset. For example,
     * if the offset is 50 and the table contains 100 rows, a key of 1 indicates row 1 of the
     * resultset. However, row 1 of this result set is actually row 51 of the entire table.
     *
     * Note the offset is applied in the SelectQuery object.
     *
     * @param conn
     * @param select
     * @param dataToSave
     * @throws SQLException
     */
    public void saveData(Connection conn, SelectQuery select, TreeMap<Integer, Object[]> dataToSave)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery(select.toString());
            ResultSetMetaData rsmd = rs.getMetaData();

            int colCount = rsmd.getColumnCount();
            int dbColIndex = 1;

            Object[] temp = null;
            for(Integer i : dataToSave.keySet()){
                rs.absolute(i);
                temp = dataToSave.get(i);//get row of data
                if(temp.length==colCount){//make sure array of data is same length as number of columns in query
                    for(int j=0;j<temp.length;j++){
                        dbColIndex = j+1;//apache derby column indexes start at 1

                        //update correct type of data
                        if(temp[j]==null){
                            rs.updateNull(rsmd.getColumnName(dbColIndex));
                        }else if(rsmd.getColumnType(dbColIndex)==Types.DOUBLE){
                            rs.updateDouble(rsmd.getColumnName(dbColIndex), (Double)temp[j]);
                        }else{
                            rs.updateString(rsmd.getColumnName(dbColIndex), temp[j].toString());
                        }

                    }//end loop over columns
                }//end if
                rs.updateRow();
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * Gets the row count for a table. This method should be called whenever a
     * table row count is needed. If the table name is not found, this
     * method will return a value of 0.
     *
     * this method is somewhat critical because the jMetrik interface needs the row count
     * to display data and initialize the progress bars. If now row count exists, no data
     * are displayed and multiple exceptions will occur when running an analysis. Care is
     * taken to ensure a row count is in the database and a nonzero value is returned.
     *
     * @param conn connection to the database containing the table.
     * @param tableName the table for which the row count is needed.
     * @return the table's row count.
     * @throws SQLException
     */
    public int getRowCount(Connection conn, SqlSafeTableName tableName)throws SQLException {
        PreparedStatement pstmt = null;
        Statement stmt = null;
        ResultSet rs = null;
        int nrow = 0;
        try{
            Table sqlTable = new Table(TABLE_OF_ROW_COUNTS);
            SelectQuery sq = new SelectQuery();
            sq.addColumn(sqlTable, sqlTableName.nameForDatabase());
            sq.addColumn(sqlTable, sqlRowCount.nameForDatabase());
            sq.addCriteria(
                    new MatchCriteria(
                            sqlTable,
                            sqlTableName.nameForDatabase(),
                            MatchCriteria.EQUALS, tableName.getNameForDatabase().toUpperCase()//names in db are upper case
                    )
            );

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(sq.toString());

            if(!rs.isBeforeFirst()){
                //table name does not exist in row count table. Insert it and add row count.
                int rowCount = countRows(conn, tableName);
                nrow = rowCount;
                String sqlString = "INSERT INTO " + TABLE_OF_ROW_COUNTS + " VALUES(?,?,?)";
                pstmt = conn.prepareStatement(sqlString);
                pstmt.setString(1, tableName.getNameForDatabase());
                pstmt.setInt(2, rowCount);
                pstmt.setString(3, "");
                pstmt.executeUpdate();

            }else{
                if(rs.next()){
                    //row count found. If things are right. You should get here every time.
                    nrow = rs.getInt(sqlRowCount.nameForDatabase());

                    //Table name exists in row count table but row count is null. Compute row count and add to table.
                    if(rs.wasNull()){
                        int rowCount = countRows(conn, tableName);
                        nrow = rowCount;

                        String sqlString = "UPDATE " + TABLE_OF_ROW_COUNTS + " SET " + sqlRowCount.nameForDatabase() + " = ? " +
                                " WHERE " + sqlTableName.nameForDatabase() + " = '" + tableName.getNameForDatabase() + "'";
                        pstmt = conn.prepareStatement(sqlString);
                        pstmt.setInt(1, nrow);
                        pstmt.executeUpdate();
                    }

                }

            }

            return nrow;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
            if(pstmt!=null) pstmt.close();
        }

    }

    private int countRows(Connection conn, SqlSafeTableName tableName)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;

        try{
            String QUERY = "SELECT COUNT(*) FROM " + tableName.getNameForDatabase();
            stmt = conn.createStatement();
            rs = stmt.executeQuery(QUERY);
            rs.next();
            int rowCount = rs.getInt(1);
            rs.close();

            return rowCount;

        }catch(SQLException ex){
            throw(ex);
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * Sets the table row count in TABLE_OF_ROW_COUNTS. This method should be called
     * after a new table is created such as after data import.
     *
     * @param conn Connection to the database containing the table.
     * @param tableName Table from which row counts were obtained.
     * @param numRows row count.
     * @throws SQLException
     */
    public void setRowCount(Connection conn, SqlSafeTableName tableName, int numRows)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String sqlString  = "SELECT " + sqlRowCount.nameForDatabase() +
                    " FROM " + TABLE_OF_ROW_COUNTS +
                    " WHERE " + sqlTableName.nameForDatabase() + " = '" + tableName.getNameForDatabase() + "'";
            rs = stmt.executeQuery(sqlString);
            if(rs.next()){
                rs.updateInt(sqlRowCount.nameForDatabase(), numRows);
                rs.updateRow();
            }

        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

        /**
     * Gets the description for a table in the database. Returns an empty String if the
     * description column does not exist in TABLE_OF_ROW_COUNTS or if the description
     * is null;
     *
     * @param conn
     * @param tableName
     * @return
     * @throws SQLException
     */
    public String getTableDescription(Connection conn, SqlSafeTableName tableName)throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try{
            String sqlString = "SELECT " +  sqlDescription.nameForDatabase() + " FROM " + TABLE_OF_ROW_COUNTS +
                    " WHERE " + sqlTableName.nameForDatabase() + " = '" + tableName.getNameForDatabase() + "'";

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(sqlString);
            String desc = "";

            //nothing found in db. return empty string.
            if(!rs.isBeforeFirst()){
                return "";
            }

            //Value found. return it.
            if(rs.next()){
                desc = rs.getString(sqlDescription.nameForDatabase());
                if(rs.wasNull()){
                    desc = "";
                }
            }

            return desc;

        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * Sets the table description. Assumes that the
     * @param conn
     * @param tableName
     * @param description
     * @throws SQLException
     */
    public void setTableDescription(Connection conn, SqlSafeTableName tableName, String description)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            int max = Math.min(description.length(), 1000);
            String shortDescription = description.substring(0, max);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            String sqlString = "SELECT " + sqlDescription.nameForDatabase() + " FROM " + TABLE_OF_ROW_COUNTS +
                    " WHERE " + sqlTableName.nameForDatabase() + " = '" + tableName.getNameForDatabase() + "'";
            rs = stmt.executeQuery(sqlString);
            if(rs.next()){
                rs.updateString(sqlDescription.nameForDatabase(), shortDescription);
                rs.updateRow();
            }

        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * Set table row count and table description.
     *
     * @param conn
     * @param tableName
     * @param rowCount
     * @param description
     * @throws SQLException
     */
    public void setTableInformation(Connection conn, SqlSafeTableName tableName, int rowCount, String description)throws SQLException{
        PreparedStatement pstmt = null;
        Statement stmt = null;
        ResultSet rs = null;
        try{
            int max = Math.min(description.length(), 1000);
            String shortDescription = description.substring(0, max);
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            String sqlString = "SELECT * FROM " + TABLE_OF_ROW_COUNTS +
                    " WHERE " + sqlTableName.nameForDatabase() + " = '" + tableName.getNameForDatabase() + "'";

            rs = stmt.executeQuery(sqlString);

            if(!rs.isBeforeFirst()){
                //No results returned from query. We need to add the row to the table and update it
                sqlString = "INSERT INTO " + TABLE_OF_ROW_COUNTS + " VALUES(?,?,?)";
                pstmt = conn.prepareStatement(sqlString);
                pstmt.setString(1, tableName.getNameForDatabase());
                pstmt.setInt(2, rowCount);
                pstmt.setString(3, description);
                pstmt.executeUpdate();
            }else if(rs.next()){
                //resultset returned. It should only be one row. Update it.
                rs.updateInt(sqlRowCount.nameForDatabase(), rowCount);
                rs.updateString(sqlDescription.nameForDatabase(), shortDescription);
                rs.updateRow();
            }

        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
            if(pstmt!=null) pstmt.close();
        }

    }

    /**
     * Deletes (i.e. drops) entry from TABLE_OF_ROW_COUNTS for a given table.
     *
     * @param conn
     * @param tableName
     * @throws SQLException
     */
    public void dropRowCount(Connection conn, SqlSafeTableName tableName)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            String sql = "SELECT * FROM " + TABLE_OF_ROW_COUNTS +
                    " WHERE " + sqlTableName.nameForDatabase() + " = '" + tableName.getNameForDatabase() + "'";
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                rs.deleteRow();
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }


    public void addColumnToDb(Connection conn, DataTableName tableName, VariableAttributes variable)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String sqlString = "SELECT * FROM " + tableName.getNameForDatabase();
            rs=stmt.executeQuery(sqlString);
            ResultSetMetaData rsmd = rs.getMetaData();
            rs.close();
            stmt.close();

            //get column names
            int n = rsmd.getColumnCount();
            HashSet<VariableName> names = new HashSet<VariableName>();
            VariableName tempName = null;
            ItemType it = null;
            DataType dt = null;
            for(int i=0;i<n;i++){
                tempName = new VariableName(rsmd.getColumnName(i+1));
                names.add(tempName);
            }

            //create unique name
            int i = 1;
            tempName = variable.getName();
            String originalName = tempName.toString();
            while(names.contains(tempName)){
                tempName = new VariableName(originalName + i);
                i++;
            }
            variable.setName(tempName);
            variable.setTestItemOrder(n+1);

            //begin transaction
            conn.setAutoCommit(false);

            //add column to data table
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            sqlString = "ALTER TABLE " + tableName.getNameForDatabase() + " ADD COLUMN " + variable.getName().nameForDatabase() +
                    " " + variable.getDatabaseTypeString();
            stmt.execute(sqlString);//TODO sometimes throws an error because of an open result set. The open result set is probably in Workspace.java when updating table views.

            //update variable table
            VariableTableName variableTableName = new VariableTableName(tableName.toString());
            sqlString = "SELECT * FROM " + variableTableName.getNameForDatabase();
            rs=stmt.executeQuery(sqlString);

            rs.moveToInsertRow();
            rs.updateString(1, variable.getName().toString());
            rs.updateString(2, "");
            rs.updateString(3, "");

            it = variable.getType().getItemType();
            int itemInt = ItemType.toInt(it);
            dt = variable.getType().getDataType();
            int dataInt = DataType.toInt(dt);

            rs.updateInt(4, itemInt);
            rs.updateInt(5, dataInt);
            rs.updateString(6, variable.getLabel().toString());
            rs.insertRow();
            rs.moveToCurrentRow();
            conn.commit();
        }catch(SQLException ex){
            ex.printStackTrace();
            conn.rollback();
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
            conn.setAutoCommit(true);
        }
    }

    public void copyTable(Connection conn, DataTableName oldTable, DataTableName newTable)throws SQLException{
        PreparedStatement pstmt = null;
        Statement stmt = null;

        try{
            VariableTableName oldVariableTableName = new VariableTableName(oldTable.toString());
            VariableTableName newVariableTableName = new VariableTableName(newTable.toString());

            //get variable info for all variables in table
            JmetrikDatabaseFactory dbFactory = new JmetrikDatabaseFactory(DatabaseType.APACHE_DERBY);
            DatabaseAccessObject dao = dbFactory.getDatabaseAccessObject();
            ArrayList<VariableAttributes> variables = dao.getAllVariables(conn, oldVariableTableName);

            String oldDesc = dao.getTableDescription(conn, oldTable);

            //start transaction
            conn.setAutoCommit(false);

            //create new variable table
            stmt = conn.createStatement();
            String sqlString = "CREATE TABLE " + newVariableTableName.getNameForDatabase() +
                    " (" +
                    "VARIABLE VARCHAR(30)," +           //name
                    "VARGROUP VARCHAR(30)," +           //group
                    "SCORING VARCHAR(250)," +           //scoring
                    "ITEMTYPE SMALLINT," +              //item type
                    "DATATYPE SMALLINT," +              //data type
                    "LABEL VARCHAR(150)," +              //label
                    "OMITCODE VARCHAR(30)," +            //omit code
                    "NOTREACHEDCODE VARCHAR(30))";      //not reached code
            stmt.execute(sqlString);

            //Populate new variable table
            String updateString = "INSERT INTO " + newVariableTableName.getNameForDatabase() + " VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
            pstmt = conn.prepareStatement(updateString);
            ItemType it;
            DataType dt;
            for(VariableAttributes v : variables){
                pstmt.setString(1, v.getName().toString());                 //name
                pstmt.setString(2, v.getItemGroup());                        //subscale/group
                pstmt.setString(3, v.printOptionScoreKey());                //scoring

                it = v.getType().getItemType();
                int itemInt = ItemType.toInt(it);
                dt = v.getType().getDataType();
                int dataInt = DataType.toInt(dt);

                pstmt.setInt(4, itemInt);                 //item type
                pstmt.setInt(5, dataInt);                 //data type
                pstmt.setString(6, v.getLabel().toString());                //label

                Object omit = v.getSpecialDataCodes().getOmittedCode();
                if(omit!=null && !omit.toString().trim().equals("")){
                    pstmt.setString(7, omit.toString().trim());                //omit code
                }else{
                    pstmt.setNull(7, Types.VARCHAR);                           //omit code initially set to null
                }

                Object nr = v.getSpecialDataCodes().getNotReachedCode();
                if(nr!=null && !nr.toString().trim().equals("")){
                    pstmt.setString(8, nr.toString().trim());                //not reached code
                }else{
                    pstmt.setNull(8, Types.VARCHAR);                         //not reached code initially set to null
                }

                pstmt.executeUpdate();
            }
            pstmt.close();

            //create new data table
            String newTableString = "CREATE TABLE " + newTable.getNameForDatabase() + " AS SELECT * FROM " +
                    oldTable.getNameForDatabase() + " WITH NO DATA";//future releases of Derby will allow WITH DATA but it is not currently available
            stmt.execute(newTableString);

            //populate new table with selected cases
            newTableString = "INSERT INTO " + newTable.getNameForDatabase() +
                    " SELECT * FROM " + oldTable.getNameForDatabase();

            int updates = stmt.executeUpdate(newTableString);

            stmt.close();

            //set row count and table description
            String desc = "Copy of " + oldTable.toString();
            if(!oldDesc.trim().equals("")) desc += " (" + oldDesc + ")";
            dao.setTableInformation(conn, newTable, updates, desc);

            //close transaction
            conn.commit();
            conn.setAutoCommit(true);

        }catch(SQLException ex){
            conn.rollback();
            conn.setAutoCommit(true);
            throw new SQLException(ex);
        }finally{
            if(pstmt!=null) pstmt.close();
            if(stmt!=null) stmt.close();
        }
    }


    //=================================================================================================================================
    // Variable level methods
    //=================================================================================================================================

    /**
     * Retrieves all variables from database
     *
     * @param conn
     * @param tableName
     * @return
     * @throws SQLException
     */
    public ArrayList<VariableAttributes> getAllVariables(Connection conn, VariableTableName tableName)throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try{
            Table table = new Table(tableName.getNameForDatabase());
            SelectQuery selectQuery = new SelectQuery();
            selectQuery.addColumn(table, "*");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(selectQuery.toString());
            ArrayList<VariableAttributes> variables = new ArrayList<VariableAttributes>();
            int dbColumnPosition = 0;
            int testItemOrder = 0;
            String groupId = "";
            String scoring = "";
            VariableName name = null;
            VariableType type = null;

            while(rs.next()){//loop over all items
                if(rs.getString(4).equals(ItemType.BINARY_ITEM.toString()) ||
                        rs.getString(4).equals(ItemType.POLYTOMOUS_ITEM.toString()) ||
                        rs.getString(4).equals(ItemType.CONTINUOUS_ITEM.toString())){
                    testItemOrder++;
                }

                name = new VariableName(rs.getString(1));
                groupId = rs.getString(2);
                type = new VariableType(rs.getInt(4), rs.getInt(5));
                scoring = rs.getString(3);

                VariableAttributes var = new VariableAttributes(
                        name.toString(), //name
                        rs.getString(6), //label
                        type.getItemType(), //item type
                        type.getDataType(), //data type
                        (Integer)dbColumnPosition,//position in db
                        groupId); //subscale
                var.addAllCategories(scoring);	//value and score
                var.setTestItemOrder(testItemOrder);

                //set Special Codes
                String omit = rs.getString(7); //omitted code

                if(!rs.wasNull()){
                    var.getSpecialDataCodes().setOmittedCode(omit);
                }

                String notReached = rs.getString(8); //not reached code
                if(!rs.wasNull()){
                    var.getSpecialDataCodes().setNotReachedCode(notReached);
                }

                variables.add(var);
                dbColumnPosition++;
            }
            return variables;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * Retrieves only selected variable from database
     *
     * @param conn
     * @param tableName
     * @param selectedVariables
     * @return
     * @throws SQLException
     */
    public ArrayList<VariableAttributes> getSelectedVariables(Connection conn, VariableTableName tableName, ArrayList<String> selectedVariables)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            Table table = new Table(tableName.getNameForDatabase());
            SelectQuery selectQuery = new SelectQuery();
            selectQuery.addColumn(table, "*");
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(selectQuery.toString());
            HashMap<VariableName, VariableAttributes> variables = new HashMap<VariableName, VariableAttributes>();
            int dbColumnPosition = 0;
            int testItemOrder = 0;
            String groupId = "";
            String scoring = "";
            VariableName name = null;
            VariableType type = null;

            while(rs.next()){//loop over all items
                if(rs.getString(4).equals(ItemType.BINARY_ITEM.toString()) ||
                        rs.getString(4).equals(ItemType.POLYTOMOUS_ITEM.toString()) ||
                        rs.getString(4).equals(ItemType.CONTINUOUS_ITEM.toString())){
                    testItemOrder++;
                }

                name = new VariableName(rs.getString(1));
                groupId = rs.getString(2);
                type = new VariableType(rs.getInt(4), rs.getInt(5));
                scoring = rs.getString(3);

                VariableAttributes var = new VariableAttributes(
                        name.toString(), //name
                        rs.getString(6), //label
                        type.getItemType(), //item type
                        type.getDataType(), //data type
                        (Integer)dbColumnPosition,//position in db
                        groupId); //subscale
                var.addAllCategories(scoring);	//value and score
                var.setTestItemOrder(testItemOrder);

                //set Special Codes
                String omit = rs.getString(7);
                if(!rs.wasNull()){
                    var.getSpecialDataCodes().setOmittedCode(omit);
                }
                String notReached = rs.getString(8);
                if(!rs.wasNull()){
                    var.getSpecialDataCodes().setNotReachedCode(notReached);
                }


                if(selectedVariables.contains(var.getName().toString())){
                    variables.put(var.getName(), var);
                }

                dbColumnPosition++;
            }

            //Ensures that returned attributes are in teh same order as the selectedVariables
            ArrayList<VariableAttributes> attributes = new ArrayList<VariableAttributes>();
            for(String s : selectedVariables){
                VariableName vname = new VariableName(s);
                attributes.add(variables.get(vname));
            }

            return attributes;
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * Retrieves a single variable from the database. Looping over all variables in teh database is necessary
     * to record the correct column position and item order.
     *
     * @param conn
     * @param tableName
     * @param varName
     * @return
     * @throws SQLException
     */
    public VariableAttributes getVariableAttributes(Connection conn, VariableTableName tableName, String varName)throws SQLException{
        ArrayList<String> selectedVariables = new ArrayList<String>();
        selectedVariables.add(varName);
        return getSelectedVariables(conn, tableName, selectedVariables).get(0);
    }

    /**
     * This method updates the variable table in the database with variable scoring information.
     *
     * @param variables
     * @throws SQLException
     */
    public synchronized void setVariableScoring(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT * FROM " + tableName.getNameForDatabase());
            String name = "";

            Outer:
            while(rs.next()){
                name = rs.getString("variable");
                Inner:
                for(VariableAttributes v : variables){
                    if(v.getName().toString().equals(name)){
                        rs.updateString("scoring", v.printOptionScoreKey()); //scoring
                        rs.updateRow();
                        break Inner;
                    }
                }
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    public synchronized void setVariableScoring(Connection conn, ScoringCommand command)throws SQLException, IllegalArgumentException{
        Statement stmt = null;
        ResultSet rs = null;

        String db = command.getPairedOptionList("data").getStringAt("db");
        String table = command.getPairedOptionList("data").getStringAt("table");
        DatabaseName dbName = new DatabaseName(db);
        VariableTableName tableName = new VariableTableName(table);
        int nKey = command.getFreeOption("keys").getInteger();

        ArrayList<VariableName> selectedVariables = new ArrayList<VariableName>();
        HashMap<VariableName, String> itemScoring = new HashMap<VariableName, String>();
        HashMap<VariableName, String> itemOmit = new HashMap<VariableName, String>();
        HashMap<VariableName, String> itemNotReached = new HashMap<VariableName, String>();

        PairedOptionList tempList = null;
        String tempString = "";
        String scoreString = "";
        String omitString = "";
        String notReachedString = "";
        String[] itemNames = null;
        VariableName tempName = null;
        for(int i=0;i<nKey;i++){
            tempList = command.getPairedOptionList("key"+(i+1));
            tempString = tempList.getStringAt("variables");
            if(tempString.startsWith("(") && tempString.endsWith(")")){
                tempString = tempString.substring(1, tempString.length()-1);
            }
            itemNames = tempString.split(",");
            scoreString = tempList.getStringAt("options") + tempList.getStringAt("scores");

            omitString = tempList.getStringAt("omit");
            notReachedString = tempList.getStringAt("nr");

            for(String s : itemNames){
                tempName = new VariableName(s.trim());
                selectedVariables.add(tempName);
                itemScoring.put(tempName, scoreString);
                if(omitString!=null && !omitString.equals("") && !omitString.equals("null")) itemOmit.put(tempName, omitString);
                if(notReachedString!=null && !notReachedString.equals("") && !notReachedString.equals("null")) itemNotReached.put(tempName, notReachedString);

            }
        }

        conn.setAutoCommit(false);

        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT * FROM " + tableName.getNameForDatabase());
            String name = "";
            String omit = "";
            String nr = "";

            Outer:
            while(rs.next()){
                name = rs.getString("variable");
                Inner:
                for(VariableName v : selectedVariables){
                    if(v.toString().equals(name)){
                        rs.updateString("scoring", itemScoring.get(v)); //scoring
                        omit = itemOmit.get(v);
                        if(omit!=null && !omit.equals("")){
                            rs.updateString("omitcode", omit);
                        }

                        nr = itemNotReached.get(v);
                        if(nr!=null && !nr.equals("")){
                            rs.updateString("notreachedcode", nr);
                        }

                        rs.updateRow();
                        break Inner;
                    }
                }
            }
            conn.commit();
        }catch(SQLException ex){
            conn.rollback();
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
            conn.setAutoCommit(true);
        }

    }

    /**
     * This method updates the database. The updatable option must be set to true in constructor.
     *
     * @param variables
     * @throws SQLException
     */
    public synchronized void setVariableGrouping(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT * FROM " + tableName.getNameForDatabase());
            String name = "";

            Outer:
            while(rs.next()){
                name = rs.getString("variable");
                Inner:
                for(VariableAttributes v : variables){
                    if(v.getName().toString().equals(name)){
                        rs.updateString("vargroup", v.getItemGroup()); //grouping
                        rs.updateRow();
                        break Inner;
                    }
                }
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * This method updates the variable table with an omit code. Note that omit and not reached
     * codes are stored in the variable table as VARCHAR(30). If the variable is actually
     * a double, the type conversion is made when the code is processed by a VariableAttributes
     * object.
     *
     * @param conn
     * @param tableName
     * @param variables
     * @throws SQLException
     */
    public synchronized void setOmitCode(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT * FROM " + tableName.getNameForDatabase());
            String name = "";
            Object omitCode;

            Outer:
            while(rs.next()){
                name = rs.getString("variable");
                Inner:
                for(VariableAttributes v : variables){
                    if(v.getName().toString().equals(name)){
                        omitCode = v.getSpecialDataCodes().getOmittedCode();
                        if(omitCode==null){
                            rs.updateNull("omitcode");
                        }else{
                            rs.updateString("omitcode", omitCode.toString()); //omit code
                        }

                        rs.updateRow();
                        break Inner;
                    }
                }
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }
    }

    /**
     * This method updates the variable table with a not reached code. Note that omit and not reached
     * codes are stored in the variable table as VARCHAR(30). If the variable is actually
     * a double, the type conversion is made when the code is processed by a VariableAttributes
     * object.
     *
     * @param conn
     * @param tableName
     * @param variables
     * @throws SQLException
     */
    public synchronized void setNotReachedCode(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT * FROM " + tableName.getNameForDatabase());
            String name = "";
            Object nrCode;

            Outer:
            while(rs.next()){
                name = rs.getString("variable");
                Inner:
                for(VariableAttributes v : variables){
                    if(v.getName().toString().equals(name)){
                        nrCode = v.getSpecialDataCodes().getNotReachedCode();
                        if(nrCode==null){
                            rs.updateNull("notreachedcode");
                        }else{
                            rs.updateString("notreachedcode", nrCode.toString()); //not reached code
                        }

                        rs.updateRow();
                        break Inner;
                    }
                }
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }
    }

    /**
     * This method updates the variable table with an omit and not reached code.
     * Note that omit and not reached codes are stored in the variable table as
     * VARCHAR(30). If the variable is actually a double, the type conversion
     * is made when the code is processed by a VariableAttributes object.
     *
     * @param conn
     * @param tableName
     * @param variables
     * @throws SQLException
     */
    public synchronized void setOmitAndNotReachedCode(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT * FROM " + tableName.getNameForDatabase());
            String name = "";

            Object omitCode;
            Object nrCode;

            Outer:
            while(rs.next()){
                name = rs.getString("variable");
                Inner:
                for(VariableAttributes v : variables){
                    if(v.getName().toString().equals(name)){
                        omitCode = v.getSpecialDataCodes().getOmittedCode();
                        if(omitCode==null){
                            rs.updateNull("omitcode");
                        }else{
                            rs.updateString("omitcode", omitCode.toString()); //omit code
                        }


                        nrCode = v.getSpecialDataCodes().getNotReachedCode();
                        if(nrCode==null){
                            rs.updateNull("notreachedcode");
                        }else{
                            rs.updateString("notreachedcode", nrCode.toString()); //not reached code
                        }

                        rs.updateRow();
                        break Inner;
                    }
                }
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }
    }

    public synchronized void saveVariables(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException{
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs = stmt.executeQuery("SELECT * FROM " + tableName.getNameForDatabase());
            String name = "";
            int i = 0;
            for(VariableAttributes v : variables){
                rs.absolute(i+1);
                rs.updateObject(1, v.getName().toString());              //name
                rs.updateObject(2, v.getItemGroup());                     //group (i.e. subscale)
                rs.updateObject(3, v.printOptionScoreKey());                   //scoring
                rs.updateObject(4, v.getType().getItemType());        //item type
                rs.updateObject(5, v.getType().getDataType());  //data type
                rs.updateObject(6, v.getLabel().toString());             //label

                if(v.getSpecialDataCodes().getOmittedCode()!=null) rs.updateObject(7, v.getSpecialDataCodes().getOmittedCode().toString());
                if(v.getSpecialDataCodes().getNotReachedCode()!=null) rs.updateObject(8, v.getSpecialDataCodes().getNotReachedCode().toString());

                rs.updateRow();
                i++;
            }
        }catch(SQLException ex){
            throw ex;
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * Returns a LinkedHashMap of item names and ItemResponseModels. The order of items in this map
     * is teh same as teh order of items in the selectedItems ArrayList argument.
     *
     * @param conn connection to database
     * @param tableName name of the item parameter table
     * @param selectedItems items for which parameter are needed. Determine the order of items in the map.
     * @param logisticScale use logistic scale by default if true (i.e. (D=1.0). Otherwise, use normal scale (i.e. D=1.7)
     * @return
     * @throws SQLException
     */
    public LinkedHashMap<String, ItemResponseModel> getItemParameterSet(Connection conn, DataTableName tableName,
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
        VariableName uparam = new VariableName("uparam");
        VariableName scoreWeight = new VariableName("sweight");
        VariableName scale = new VariableName("scale");
        VariableName step = null;

        double a = 1, b = 0, c = 0, u = 1.0, D = 1.0, defaultD = 1.0;
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
                u = 1;
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
                if("L4".equals(model) || "L3".equals(model) || "L2".equals(model) || "L1".equals(model)){

                    //lower-asymptote parameter -- optional
                    if(colNames.contains(cparam)){
                        c = rs.getDouble(cparam.nameForDatabase());
                        if(rs.wasNull()){
                            c = 0.0;
                        }else{
                            binaryModelParam = 3;
                        }
                    }

                    //upper-asymptote parameter -- optional
                    if(colNames.contains(uparam)){
                        u = rs.getDouble(uparam.nameForDatabase());
                        if(rs.wasNull()){
                            u = 1.0;
                        }else{
                            binaryModelParam = 4;
                        }
                    }

                    //difficulty parameter -- required column for L3
                    b = rs.getDouble(bparam.nameForDatabase());

                    //Set specific type of binary model because irm constructor will
                    //determine number of parameters from constructor.
                    if("L4".equals(model)){
                        irm = new Irm4PL(a, b, c, u, D);
                    }else{
                        if(binaryModelParam==1){
                            irm = new Irm3PL(b, D);
                        }else if(binaryModelParam==2){
                            irm = new Irm3PL(a, b, D);
                        }else{
                            irm = new Irm3PL(a, b, c, D);
                        }
                        irm.setSlipping(u);
                    }

                }else{
                    //polytomous item response models

                    //all polytomous models have step parameter variables in database
                    if("PC1".equals(model) || "PC4".equals(model)){
                        stepParam = new double[ncat];
                        stepParam[0]=0;
                        for(int k=1;k<ncat;k++){
                            step = new VariableName("step" + k);
                            stepParam[k] = rs.getDouble(step.nameForDatabase());
                        }
                    }else{
                        stepParam = new double[ncat-1];
                        for(int k=1;k<ncat;k++){
                            step = new VariableName("step" + k);
                            stepParam[k-1] = rs.getDouble(step.nameForDatabase());
                        }
                    }


                    if("PC1".equals(model)){
                        irm = new IrmGPCM(a, stepParam, D);
                    }else if("PC2".equals(model)){
                        b = rs.getDouble(bparam.nameForDatabase());
                        irm = new IrmGPCM2(a, b, stepParam, D);
                    }else if("PC3".equals(model)){
                        b = rs.getDouble(bparam.nameForDatabase());
                        irm = new IrmPCM(b, stepParam, D);
                    }else if("PC4".equals(model)){
                        irm = new IrmPCM2(stepParam, D);
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
                irmSet.put(irm.getName().toString(), irm);//item response models added in the same order as selectedItems ArrayList.
            }

            return irmSet;
        }catch(SQLException ex){
            throw(ex);
        }finally{
            if(pstmt!=null) pstmt.close();
        }


    }



}

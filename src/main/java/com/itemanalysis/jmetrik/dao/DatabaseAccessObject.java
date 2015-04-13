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

import com.itemanalysis.jmetrik.model.SortedListModel;
import com.itemanalysis.jmetrik.scoring.ScoringCommand;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.SqlSafeTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.squiggle.base.SelectQuery;

import javax.swing.tree.DefaultTreeModel;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.TreeSet;

public interface DatabaseAccessObject {

    //=================================================================================================================================
    // Database level methods
    //=================================================================================================================================
    public DefaultTreeModel getDatabaseTreeModel(Connection conn)throws SQLException;

    public SortedListModel<DatabaseName> getDatabaseListModel(String path)throws IOException;

    public SortedListModel<DataTableName> getTableListModel(Connection conn)throws SQLException;

    public void createTables(Connection conn, DataTableName dataTableName, VariableTableName variableTableName, LinkedHashMap<String, VariableAttributes> variables)throws SQLException;

    public void createTables(Connection conn, DataTableName dataTableName, VariableTableName variableTableName, ArrayList<VariableAttributes> variables)throws SQLException;

    public TreeSet<DataTableName> getExistingTableNames(Connection conn)throws SQLException;

    public DataTableName getUniqueTableName(Connection conn, String proposedName) throws SQLException;

    public boolean isTableNameUnique(Connection conn, String originalName)throws SQLException;

    public void updateDatabasesVersion(Connection conn)throws SQLException, IOException;

    //=================================================================================================================================
    // Table level methods
    //=================================================================================================================================

    /**
     * Return an array list of data. The number of rows depends on the information
     * in the SelectQuery. The number of rows is controlled by the DerbyOffset that
     * is added to the SelectQuery.
     *
     * @param conn
     * @param select
     * @return
     * @throws SQLException
     */
    public Object[][] getData(Connection conn, SelectQuery select, int numRow, int numCol)throws SQLException;

    public Object[] getDataFromColumn(Connection conn, DataTableName tableName, VariableName variableName)throws SQLException;

    /**
     * Creates an ArrayList of VariableAttributes objects from the values in the specified column.
     * The values in the specified column are assumed to be unique variable names.
     *
     * @param conn
     * @param tableName name of table containing volumn
     * @param variableName name of column to use for VariableAttributes data
     * @return
     * @throws SQLException
     */
    public ArrayList<VariableAttributes> getVariableAttributesFromColumn(Connection conn, DataTableName tableName, VariableName variableName)throws SQLException;

    public int getColumnCount(Connection conn, DataTableName tableName)throws SQLException;

    public Class[] getColumnClass(Connection conn,  DataTableName tableNam)throws SQLException;

    public VariableName[] getColumnNames(Connection conn, DataTableName tableName)throws SQLException;

    public void saveData(Connection conn, SelectQuery select, TreeMap<Integer, Object[]> dataToSave)throws SQLException;

    public int getRowCount(Connection conn, SqlSafeTableName table)throws SQLException;

    public void setRowCount(Connection conn, SqlSafeTableName table, int numRows)throws SQLException;

    public String getTableDescription(Connection conn, SqlSafeTableName table) throws SQLException;

    public void setTableDescription(Connection conn, SqlSafeTableName table, String description) throws SQLException;

    public void setTableInformation(Connection conn, SqlSafeTableName table, int numRows, String description) throws SQLException;

    public void dropRowCount(Connection conn, SqlSafeTableName table)throws SQLException;

    public void addColumnToDb(Connection conn, DataTableName tableName, VariableAttributes variableAttributes)throws SQLException;

    public void copyTable(Connection conn, DataTableName oldTable, DataTableName newTableName)throws SQLException;


    //=================================================================================================================================
    // Variable level methods
    //=================================================================================================================================
    public ArrayList<VariableAttributes> getAllVariables(Connection conn, VariableTableName tableName)throws SQLException;

    public ArrayList<VariableAttributes> getSelectedVariables(Connection conn, VariableTableName tableName, ArrayList<String> selectedVariables)throws SQLException;

    public VariableAttributes getVariableAttributes(Connection conn, VariableTableName tableName, String varName)throws SQLException;

    public void setVariableScoring(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException;

    public void setVariableScoring(Connection conn, ScoringCommand command)throws SQLException;

    public void setVariableGrouping(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException;

    public void setOmitCode(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException;

    public void setNotReachedCode(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException;

    public void setOmitAndNotReachedCode(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException;

    public void saveVariables(Connection conn, VariableTableName tableName, ArrayList<VariableAttributes> variables)throws SQLException;

    public LinkedHashMap<String, ItemResponseModel> getItemParameterSet(Connection conn, DataTableName tableName,
                                                                        ArrayList<VariableName> selectedItems,
                                                                        boolean logisticScale) throws SQLException;


    
}

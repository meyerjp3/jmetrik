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
import com.itemanalysis.psychometrics.histogram.*;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbHistogram {

    public DbHistogram(){

    }

    public Histogram getHistogram(Connection conn, DataTableName tableName, VariableName thetaName, int bins)throws SQLException {

        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery query = new SelectQuery();
        query.addColumn(sqlTable, thetaName.nameForDatabase());

        Statement stmt = null;
        ResultSet rs = null;

        try{
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs = stmt.executeQuery(query.toString());

            BinCalculationType binType = BinCalculationType.STURGES;
            if(bins==-1) binType = BinCalculationType.SIMPLE;

            //fill bins
            Histogram histogram = new Histogram(HistogramType.DENSITY, binType, true);
            rs = stmt.executeQuery(query.toString());
            double t;
            while(rs.next()){
                t = rs.getDouble(thetaName.nameForDatabase());
                if(!rs.wasNull()){
                    histogram.increment(t);
                }
            }
            histogram.evaluate();//do computation
            return histogram;
        }catch(SQLException ex){
            throw(ex);
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }



    }


}

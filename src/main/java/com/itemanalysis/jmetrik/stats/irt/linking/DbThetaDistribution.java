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
import com.itemanalysis.psychometrics.distribution.DistributionApproximation;
import com.itemanalysis.psychometrics.distribution.UserSuppliedDistributionApproximation;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DbThetaDistribution {

    UserSuppliedDistributionApproximation dist = null;

    public DbThetaDistribution(){

    }

    public DistributionApproximation getDistribution(Connection conn, DataTableName tableName, VariableName thetaName,
                                                     VariableName weightName, boolean hasWeight)throws SQLException {

        dist = new UserSuppliedDistributionApproximation();

        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery query = new SelectQuery();
        query.addColumn(sqlTable, thetaName.nameForDatabase());
        if(hasWeight){
            query.addColumn(sqlTable, weightName.nameForDatabase());
        }

        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery(query.toString());
        double value = 0.0;
        double w = 1.0;

        while(rs.next()){
            value = rs.getDouble(thetaName.nameForDatabase());
            if(!rs.wasNull()){
                if(hasWeight){
                    w = rs.getDouble(weightName.nameForDatabase());
                    if(rs.wasNull()){
                        w=0.0;
                    }
                    dist.increment(value, w);
                }else{
                    dist.increment(value);
                }
            }
        }
        rs.close();
        stmt.close();

        return dist;

    }

}

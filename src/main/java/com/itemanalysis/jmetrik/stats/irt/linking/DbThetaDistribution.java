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
import com.itemanalysis.psychometrics.quadrature.ContinuousQuadratureRule;
import com.itemanalysis.psychometrics.quadrature.QuadratureRule;
import com.itemanalysis.psychometrics.quadrature.UniformQuadratureRule;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.stat.descriptive.rank.Max;
import org.apache.commons.math3.stat.descriptive.rank.Min;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DbThetaDistribution {

    private ArrayList<Double> points = null;
    private ArrayList<Double> weights = null;

    public DbThetaDistribution(){

    }

    public QuadratureRule getDistribution(Connection conn, DataTableName tableName, VariableName thetaName,
                                          VariableName weightName, boolean hasWeight)throws SQLException {

        points = new ArrayList<Double>();
        Min min = new Min();
        Max max = new Max();

        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery query = new SelectQuery();
        query.addColumn(sqlTable, thetaName.nameForDatabase());
        if(hasWeight){
            query.addColumn(sqlTable, weightName.nameForDatabase());
            weights = new ArrayList<Double>();
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
                    points.add(value);
                    weights.add(w);
                    min.increment(value);
                    max.increment(value);
                }else{
                    points.add(value);
                    min.increment(value);
                    max.increment(value);
                }
            }
        }
        rs.close();
        stmt.close();


        QuadratureRule dist = new ContinuousQuadratureRule(points.size(), min.getResult(), max.getResult());

        if(hasWeight){
            for(int i=0;i<points.size();i++){
                dist.setPointAt(i, points.get(i));
                dist.setDensityAt(i, weights.get(i));
            }
        }else{
            for(int i=0;i<points.size();i++){
                dist.setPointAt(i, points.get(i));
            }
        }

        return dist;

    }

}

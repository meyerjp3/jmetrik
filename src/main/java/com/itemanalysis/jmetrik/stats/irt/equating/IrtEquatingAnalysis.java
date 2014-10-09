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

package com.itemanalysis.jmetrik.stats.irt.equating;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.stats.irt.linking.DbItemParameterSet;
import com.itemanalysis.jmetrik.stats.irt.linking.DbThetaDistribution;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.distribution.DistributionApproximation;
import com.itemanalysis.psychometrics.irt.equating.IrtTrueScoreEquating;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.tools.StopWatch;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

public class IrtEquatingAnalysis extends SwingWorker<String, String> {

    private IrtEquatingCommand command = null;
    private Connection conn = null;
    private JmetrikTextFile textFile = null;
    private Throwable theException = null;
    private StopWatch sw = null;

    private LinkedHashMap<String, ItemResponseModel> irmX = null;
    private LinkedHashMap<String, ItemResponseModel> irmY = null;
    private DistributionApproximation xDistribution = null;
    private DistributionApproximation yDistribution = null;
    private DataTableName tableNameItemsX = null;
    private DataTableName tableNameItemsY = null;
    private ArrayList<VariableName> itemsFormX = null;
    private ArrayList<VariableName> itemsFormY = null;

    private DatabaseName outputDbName = null;
    private DataTableName outputTable = null;

    private DataTableName tableNamePersonsX = null;
    private VariableName thetaNameX = null;
    private VariableName weightNameX = null;
    private boolean hasWeightX = false;
    private VariableInfo newTheta = null;

    private DataTableName tableNamePersonsY = null;
    private VariableName thetaNameY = null;
    private VariableName weightNameY = null;
    private boolean hasWeightY = false;

    private boolean logisticScale = true;
    private boolean trueScoreMethod = true;
    private boolean newItemTableCreated = false;
    private  boolean createOutputTable = false;

    private DatabaseAccessObject dao = null;
    private DatabaseName dbName = null;

    IrtTrueScoreEquating irtTrueScoreEquating = null;

    static Logger logger = Logger.getLogger("jmetrik-logger");

    public IrtEquatingAnalysis(Connection conn, DatabaseAccessObject dao, IrtEquatingCommand command, JmetrikTextFile textFile){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.textFile = textFile;
        itemsFormX = new ArrayList<VariableName>();
        itemsFormY = new ArrayList<VariableName>();
    }

    public void processCommand()throws IllegalArgumentException{
        String xDbName = command.getPairedOptionList("xitem").getStringAt("db");
        String xTable = command.getPairedOptionList("xitem").getStringAt("table");
        tableNameItemsX = new DataTableName(xTable);

        dbName = new DatabaseName(xDbName);//assumes that same database is used for all parameter

        String yDbName = command.getPairedOptionList("yitem").getStringAt("db"); //same as dbName
        String yTable = command.getPairedOptionList("yitem").getStringAt("table");
        tableNameItemsY = new DataTableName(yTable);

        ArrayList<String> fX = command.getFreeOptionList("xvar").getString();
        itemsFormX = new ArrayList<VariableName>();
        for(String s : fX){
            itemsFormX.add(new VariableName(s));
        }

        ArrayList<String> fY = command.getFreeOptionList("yvar").getString();
        for(String s : fY){
            itemsFormY.add(new VariableName(s));
        }

        if(command.getPairedOptionList("xability").hasValue() &&
                command.getPairedOptionList("yability").hasValue()){
            String xPersonTable = command.getPairedOptionList("xability").getStringAt("table");
            String xPersonTheta = command.getPairedOptionList("xability").getStringAt("theta");
            String xPersonWeight = command.getPairedOptionList("xability").getStringAt("weight");
            if(xPersonTable!=null && !xPersonTable.equals("null")){
                tableNamePersonsX = new DataTableName(xPersonTable);
                thetaNameX = new VariableName(xPersonTheta);
                weightNameX = new VariableName(xPersonWeight);
                if(!xPersonWeight.trim().equals("")){
                    hasWeightX = true;
                }
            }

            String yPersonTable = command.getPairedOptionList("yability").getStringAt("table");
            String yPersonTheta = command.getPairedOptionList("yability").getStringAt("theta");
            String yPersonWeight = command.getPairedOptionList("yability").getStringAt("weight");
            if(yPersonTable!=null && !yPersonTable.equals("null")){
                tableNamePersonsY = new DataTableName(yPersonTable);
                thetaNameY = new VariableName(yPersonTheta);
                weightNameY = new VariableName(yPersonWeight);
                if(!yPersonWeight.trim().equals("")){
                    hasWeightY = true;
                }
            }

        }

        logisticScale = command.getSelectOneOption("scale").isValueSelected("logistic");
        trueScoreMethod = command.getSelectOneOption("method").isValueSelected("true");

        if(command.getPairedOptionList("output").hasValue()){
            String oDb = command.getPairedOptionList("output").getStringAt("db");
            String oT = command.getPairedOptionList("output").getStringAt("table");
            outputDbName = new DatabaseName(oDb);
            outputTable = new DataTableName(oT);
            createOutputTable = true;
        }

    }

    private void getItemParameters() throws SQLException {
        DbItemParameterSet itemParameterSet = new DbItemParameterSet();
        irmX = itemParameterSet.getItemParameters(conn, tableNameItemsX, itemsFormX, logisticScale);
        irmY = itemParameterSet.getItemParameters(conn, tableNameItemsY, itemsFormY, logisticScale);
    }

    public void getThetaDistributions()throws IllegalArgumentException, SQLException{
        DbThetaDistribution dist = new DbThetaDistribution();
        xDistribution = dist.getDistribution(conn, tableNamePersonsX, thetaNameX, weightNameX, hasWeightX);
        yDistribution = dist.getDistribution(conn, tableNamePersonsY, thetaNameY, weightNameY, hasWeightY);
    }

    private String conductEquating(){
        irtTrueScoreEquating = new IrtTrueScoreEquating(irmX, irmY);
        irtTrueScoreEquating.equateScores();
        return irtTrueScoreEquating.printResults();
    }



    private void saveOutput()throws SQLException{

        PreparedStatement pstmt = null;

        try{
            conn.setAutoCommit(false);//start transaction
            outputTable = dao.getUniqueTableName(conn, outputTable.toString());
            VariableTableName variableTableName = new VariableTableName(outputTable.toString());

            VariableInfo score = new VariableInfo("score", "Score", VariableType.NOT_ITEM, VariableType.DOUBLE, 1, "");
            VariableInfo theta = new VariableInfo("theta", "Form X theta", VariableType.NOT_ITEM, VariableType.DOUBLE, 2, "");
            VariableInfo yequiv = new VariableInfo("yequiv", "Y Equivalent of score", VariableType.NOT_ITEM, VariableType.DOUBLE, 3, "");
            VariableInfo conv = new VariableInfo("conv", "Newton-Rhapson convergence status", VariableType.NOT_ITEM, VariableType.STRING, 4, "");
            ArrayList<VariableInfo> variables = new ArrayList();
            variables.add(score);
            variables.add(theta);
            variables.add(yequiv);
            variables.add(conv);
            dao.createTables(conn, outputTable, variableTableName, variables);

            double[] scorePoints = irtTrueScoreEquating.getScorePoints();
            double[] xtheta = irtTrueScoreEquating.getFormXThetaValues();
            double[] yeq = irtTrueScoreEquating.getYEquivalentTrueScores();
            char[] status = irtTrueScoreEquating.getStatus();

            String query = "INSERT INTO " + outputTable.getNameForDatabase() + " VALUES(?, ?, ?, ?)";
            pstmt = conn.prepareStatement(query);

            for(int i=0;i<scorePoints.length;i++){
                pstmt.setDouble(1,scorePoints[i]);
                pstmt.setDouble(2,xtheta[i]);
                pstmt.setDouble(3,yeq[i]);
                pstmt.setString(4,String.valueOf(status[i]).toString());
                pstmt.executeUpdate();
            }
            newItemTableCreated = true;

            conn.commit();//end transaction
        }catch(SQLException ex){
            conn.rollback();
            conn.setAutoCommit(true);
            throw ex;
        }finally{
            conn.setAutoCommit(true);
            if(pstmt!=null) pstmt.close();
        }

    }

    public void publishHeader()throws IllegalArgumentException{
        StringBuilder header = new StringBuilder();
        Formatter f = new Formatter(header);
        String s1 = String.format("%1$tB %1$te, %1$tY  %tT", Calendar.getInstance());
        int len = 38+Double.valueOf(Math.floor(Double.valueOf(s1.length()).doubleValue()/2.0)).intValue();
        String dString = "";
        dString = command.getDataString();

        f.format("%45s", "IRT SCORE EQUATING"); f.format("%n");
        f.format("%" + len + "s", s1); f.format("%n"); f.format("%n");
        f.format("%-" + dString.length() + "s", dString); f.format("%n");
        publish(f.toString());
    }

    @Override
    protected void process(List<String> chunks){
        for(String s : chunks){
            textFile.append(s + "\n");
        }
    }

    @Override
    protected String doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running IRT Score Equating...");
        this.firePropertyChange("progress-ind-on", null, null);

        try{
            logger.info(command.paste());
            processCommand();
            publishHeader();
            getItemParameters();
            publish(conductEquating());
            if(createOutputTable) saveOutput();

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible
        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException = t;
        }
        return sw.getElapsedTime();
    }

    @Override
    protected void done(){
        try{
            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
            }else{
                if(newItemTableCreated){
                    this.firePropertyChange("table-added", "", outputTable);//will add to list
                }
            }
            textFile.addText(get());
            textFile.setCaretPosition(0);
        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }
    }

}

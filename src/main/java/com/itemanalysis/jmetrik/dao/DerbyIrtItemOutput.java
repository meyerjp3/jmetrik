package com.itemanalysis.jmetrik.dao;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.irt.estimation.ItemFitStatistic;
import com.itemanalysis.psychometrics.irt.model.*;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import com.sun.org.apache.xpath.internal.operations.Variable;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class DerbyIrtItemOutput {

//    int maxCat = 0;
    int maxCol = 0;
    private LinkedHashMap<String, VariableAttributes> variableMap = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private DataTableName itemTableName = null;
    private DataTableName dataTableName = null;
    private VariableAttributes name = null;
    private VariableAttributes model = null;
    private VariableAttributes ncat = null;
    private VariableAttributes scale = null;
    private VariableAttributes group = null;
    private VariableAttributes aparam = null;
    private VariableAttributes bparam = null;
    private VariableAttributes cparam = null;
    private VariableAttributes uparam = null;
    private VariableAttributes ase = null;
    private VariableAttributes bse = null;
    private VariableAttributes cse = null;
    private VariableAttributes use = null;
    private VariableAttributes fitValue = null;
    private VariableAttributes fitDf = null;
    private VariableAttributes fitPvalue = null;
    private ItemResponseModel[] irm = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    private int maxBinaryParam = 0;
    private boolean hasDiscrimination = false;
    private boolean hasDifficulty = false;

    public DerbyIrtItemOutput(Connection conn, DatabaseAccessObject dao, DataTableName dataTableName, DataTableName itemTableName, ItemResponseModel[] irm){
        this.conn = conn;
        this.dao = dao;
        this.dataTableName = dataTableName;
        this.itemTableName = itemTableName;
        this.irm = irm;
        variableMap = new LinkedHashMap<String, VariableAttributes>();
        initialize();
    }

    private void initialize(){
        for(int j=0;j<irm.length;j++){
//            if(irm[j].getType()==IrmType.L4 || irm[j].getType()==IrmType.L3){
//                maxCol = Math.max(1, maxCol);
//                maxBinaryParam = Math.max(maxBinaryParam, irm[j].getNumberOfParameters());
//                hasDifficulty = true;
//                if(maxBinaryParam>1) hasDiscrimination = true;
//            }
            if(irm[j].getType()==IrmType.L4){
                maxBinaryParam = Math.max(maxBinaryParam, 4);
                hasDifficulty = true;
                hasDiscrimination = true;
            }else if(irm[j].getType()==IrmType.L3){
                maxBinaryParam = Math.max(maxBinaryParam, 3);
                hasDifficulty = true;
                hasDiscrimination = true;
            }
            else if(irm[j].getType()==IrmType.GPCM || irm[j].getType()==IrmType.PCM2){
                maxCol = Math.max(maxCol, irm[j].getNcat()-1);
            }else if(irm[j].getType()==IrmType.GPCM2 || irm[j].getType()==IrmType.PCM){
                hasDifficulty = true;
                maxCol = Math.max(maxCol, irm[j].getNcat());//Will change to ncat-1 after ItemPCM and IrmGPCM2 have been refactored to include zero step parameter for first category
            }

            if(irm[j].getType()==IrmType.GPCM || irm[j].getType()==IrmType.GPCM2){
                hasDiscrimination = true;
            }

//            else if(irm[j].getType()==IrmType.GPCM){
//                maxCol = Math.max(maxCol, ncM1);
//            }else if(irm[j].getType()==IrmType.GPCM2){
//                maxCol = Math.max(maxCol, ncM1);
//            }else if(irm[j].getType()==IrmType.PCM){
//                maxCol = Math.max(maxCol, ncM1);
//            }else if(irm[j].getType()==IrmType.PCM2){
//                maxCol = Math.max(maxCol, ncM1);
//            }
        }
    }

    private void createVariables(){
        int column = 0;
        name = new VariableAttributes("name", "Item Name", ItemType.NOT_ITEM, DataType.STRING, ++column, "");
        model = new VariableAttributes("model", "IRT Model", ItemType.NOT_ITEM, DataType.STRING, ++column, "");
        ncat = new VariableAttributes("ncat", "Number of categories", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
        scale = new VariableAttributes("scale", "Scaling constant", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
        group = new VariableAttributes("group", "Item group", ItemType.NOT_ITEM, DataType.STRING, ++column, "");
        variableMap.put(name.getName().toString(), name);
        variableMap.put(model.getName().toString(), model);
        variableMap.put(ncat.getName().toString(), ncat);
        variableMap.put(scale.getName().toString(), scale);
        variableMap.put(group.getName().toString(), group);

        if(hasDiscrimination){
            aparam = new VariableAttributes("aparam", "Item discrimination", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
            ase = new VariableAttributes("a_se", "Discrimination standard error", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
            variableMap.put("aparam", aparam);
            variableMap.put("a_se", ase);
        }

        if(hasDifficulty){
            bparam = new VariableAttributes("bparam", "Item difficulty", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
            bse = new VariableAttributes("b_se", "Difficulty standard error", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
            variableMap.put("bparam", bparam);
            variableMap.put("b_se", bse);
        }

        if(maxBinaryParam>=3){
            cparam = new VariableAttributes("cparam", "Item guessing", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
            cse = new VariableAttributes("c_se", "Guessing standard error", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
            variableMap.put("cparam", cparam);
            variableMap.put("c_se", cse);
        }

        if(maxBinaryParam==4){
            uparam = new VariableAttributes("uparam", "Item slipping", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
            use = new VariableAttributes("u_se", "Slipping standard error", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
            variableMap.put("uparam", uparam);
            variableMap.put("u_se", use);
        }

        VariableAttributes step = null;
        VariableAttributes stepSe = null;

        int stepNum = 0;
        String stepName = "";
        String stepSeName = "";
        if(maxCol>1){
            for(int i=0; i<maxCol;i++){
                stepNum = i+1;
                stepName = "step"+stepNum;
                stepSeName = "step_se"+stepNum;
                step = new VariableAttributes(stepName, "Step parameter for category " + stepNum, ItemType.NOT_ITEM, DataType.DOUBLE, column++, "");
                stepSe = new VariableAttributes(stepSeName, "Standard error for category " + stepNum, ItemType.NOT_ITEM, DataType.DOUBLE, column++, "");
                variableMap.put(stepName, step);
                variableMap.put(stepSeName, stepSe);
            }
        }

        fitValue = new VariableAttributes("sx2_fit", "SX2 Fit statistic", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
        fitDf = new VariableAttributes("sx2_df", "SX2 Fit statistic degrees of freedom", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
        fitPvalue = new VariableAttributes("sx2_pvalue", "SX2 Fit statistic p-value", ItemType.NOT_ITEM, DataType.DOUBLE, ++column, "");
        variableMap.put("sx2_fit", fitValue);
        variableMap.put("sx2_df", fitDf);
        variableMap.put("sx2_pvalue", fitPvalue);

    }

    public void outputToDb()throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;

        try{
            createVariables();

            VariableTableName variableTableName = new VariableTableName(itemTableName.toString());
            dao.createTables(conn, itemTableName, variableTableName, variableMap);

            //Select items and new score variables
            Table sqlTable = new Table(itemTableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(String s : variableMap.keySet()){
                select.addColumn(sqlTable, variableMap.get(s).getName().nameForDatabase());
            }

            conn.setAutoCommit(false);//begin transaction

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs=stmt.executeQuery(select.toString());

            int nrow = 0;
            String stepName = "";
            String stepSeName = "";
            ItemFitStatistic fitStatistic = null;

            for(int j=0;j<irm.length;j++){
                rs.moveToInsertRow();

                rs.updateString(name.getName().nameForDatabase(), irm[j].getName().toString());

                if(irm[j] instanceof Irm4PL){
                    rs.updateString(model.getName().nameForDatabase(), "L4");
                }else if(irm[j] instanceof IrmGPCM){
                    rs.updateString(model.getName().nameForDatabase(), "PC1");
                }else if(irm[j] instanceof IrmPCM2){
                    rs.updateString(model.getName().nameForDatabase(), "PC4");
                }else{
                    rs.updateString(model.getName().nameForDatabase(), "L3");
                }

                rs.updateDouble(ncat.getName().nameForDatabase(), irm[j].getNcat());
                rs.updateDouble(scale.getName().nameForDatabase(), irm[j].getScalingConstant());
                rs.updateString(group.getName().nameForDatabase(), irm[j].getGroupId());

                //Only set difficulty and discrimination parameters for a model that include them.

                if(irm[j].getType()== IrmType.L3 || irm[j].getType()== IrmType.L4){
                    if(!Double.isNaN(irm[j].getDifficulty())) rs.updateDouble(bparam.getName().nameForDatabase(), irm[j].getDifficulty());
                    if(!Double.isNaN(irm[j].getDifficultyStdError())) rs.updateDouble(bse.getName().nameForDatabase(), irm[j].getDifficultyStdError());

                    if(maxBinaryParam>=2){
                        irm[j].getDiscrimination();
                        aparam.getName();
                        if(!Double.isNaN(irm[j].getDiscrimination())) rs.updateDouble(aparam.getName().nameForDatabase(), irm[j].getDiscrimination());
                        if(!Double.isNaN(irm[j].getDiscriminationStdError())) rs.updateDouble(ase.getName().nameForDatabase(), irm[j].getDiscriminationStdError());
                    }

                    if(maxBinaryParam>=3){
                        if(!Double.isNaN(irm[j].getGuessing())) rs.updateDouble(cparam.getName().nameForDatabase(), irm[j].getGuessing());
                        if(!Double.isNaN(irm[j].getGuessingStdError())) rs.updateDouble(cse.getName().nameForDatabase(), irm[j].getGuessingStdError());
                    }

                    if(maxBinaryParam>=4){
                        if(!Double.isNaN(irm[j].getSlipping())) rs.updateDouble(uparam.getName().nameForDatabase(), irm[j].getSlipping());
                        if(!Double.isNaN(irm[j].getSlippingStdError())) rs.updateDouble(use.getName().nameForDatabase(), irm[j].getSlippingStdError());
                    }
                }else if(irm[j].getType()==IrmType.GPCM){
                    if(!Double.isNaN(irm[j].getDiscrimination())) rs.updateDouble(aparam.getName().nameForDatabase(), irm[j].getDiscrimination());
                    if(!Double.isNaN(irm[j].getDiscriminationStdError())) rs.updateDouble(ase.getName().nameForDatabase(), irm[j].getDiscriminationStdError());
                }

                //Add step parameters for polytomous items
                VariableAttributes tempStep = null;
                if(maxCol>1){
                    if(irm[j].getType()==IrmType.GPCM || irm[j].getType()==IrmType.PCM2){
                        double[] step = irm[j].getStepParameters();
                        double[] stepSe = irm[j].getStepStdError();
                        for(int k=1;k<step.length;k++){//First step is always zero. Skip it here.
                            stepName = "step"+k;
                            stepSeName = "step_se"+k;

                            tempStep = variableMap.get(stepName);
                            if(!Double.isNaN(step[k])) rs.updateDouble(tempStep.getName().nameForDatabase(), step[k]);

                            tempStep = variableMap.get(stepSeName);
                            if(!Double.isNaN(stepSe[k])) rs.updateDouble(tempStep.getName().nameForDatabase(), stepSe[k]);
                        }
                    }

                }

                //Add item fit statistics
                fitStatistic = irm[j].getItemFitStatistic();
                if(!Double.isNaN(fitStatistic.getValue())) rs.updateDouble(fitValue.getName().nameForDatabase(), fitStatistic.getValue());
                if(!Double.isNaN(fitStatistic.getDegreesOfFreedom())) rs.updateDouble(fitDf.getName().nameForDatabase(), fitStatistic.getDegreesOfFreedom());
                if(!Double.isNaN(fitStatistic.getPValue())) rs.updateDouble(fitPvalue.getName().nameForDatabase(), fitStatistic.getPValue());

                nrow++;
                rs.insertRow();
            }

            //add row count to row count table
            dao.setTableInformation(conn, itemTableName, nrow, "Item parameter output table for analysis of " +
                    dataTableName.toString()  + ".");

            conn.commit();
        }catch(SQLException ex){
			logger.fatal(ex.getMessage(), ex);
            conn.rollback();
            throw new SQLException(ex.getMessage());
		}finally{
            conn.setAutoCommit(true);
            if(stmt!=null) stmt.close();
            if(rs!=null) rs.close();
        }

    }

    /**
     * Apache Derby does not like NaNs or infinite values. Treat them as null values.
     * @param pstmt
     * @param index
     * @param value
     * @throws SQLException
     */
    private void safeSetValue(PreparedStatement pstmt, int index, double value)throws SQLException{
        if(Double.isNaN(value) || Double.isInfinite(value)){
            pstmt.setNull(index, Types.DOUBLE);
        }else{
            pstmt.setDouble(index, value);
        }
    }

}

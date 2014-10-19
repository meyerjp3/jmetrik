package com.itemanalysis.jmetrik.dao;

import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.psychometrics.data.VariableInfo;
import com.itemanalysis.psychometrics.data.VariableType;
import com.itemanalysis.psychometrics.irt.model.*;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;

public class DerbyIrtItemOutput {

//    int maxCat = 0;
    int maxCol = 0;
    private ArrayList<VariableInfo> variables = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private DataTableName itemTableName = null;
    private DataTableName dataTableName = null;
    private VariableInfo name = null;
    private VariableInfo model = null;
    private VariableInfo ncat = null;
    private VariableInfo group = null;
    private VariableInfo aparam = null;
    private VariableInfo bparam = null;
    private VariableInfo cparam = null;
    private VariableInfo uparam = null;
    private VariableInfo ase = null;
    private VariableInfo bse = null;
    private VariableInfo cse = null;
    private VariableInfo use = null;
    private ItemResponseModel[] irm = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    public DerbyIrtItemOutput(Connection conn, DatabaseAccessObject dao, DataTableName dataTableName, DataTableName itemTableName, ItemResponseModel[] irm){
        this.conn = conn;
        this.dao = dao;
        this.dataTableName = dataTableName;
        this.itemTableName = itemTableName;
        this.irm = irm;
        variables = new ArrayList<VariableInfo>();
        initialize();
    }

    private void initialize(){
        for(int j=0;j<irm.length;j++){
            if(irm[j].getType()==IrmType.L4 || irm[j].getType()==IrmType.L3){
                maxCol = Math.max(1, maxCol);
            }else if(irm[j].getType()==IrmType.GPCM || irm[j].getType()==IrmType.PCM2){
                maxCol = Math.max(maxCol, irm[j].getNcat()-1);
            }else if(irm[j].getType()==IrmType.GPCM2 || irm[j].getType()==IrmType.PCM){
                maxCol = Math.max(maxCol, irm[j].getNcat());//Will change to ncat-1 after ItemPCM and IrmGPCM2 have been refactored to include zero step parameter for first category
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
        name = new VariableInfo("name", "Item Name", VariableType.NOT_ITEM, VariableType.STRING, ++column, "");
        model = new VariableInfo("model", "IRT Model", VariableType.NOT_ITEM, VariableType.STRING, ++column, "");
        ncat = new VariableInfo("ncat", "Number of categories", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        group = new VariableInfo("group", "Item group", VariableType.NOT_ITEM, VariableType.STRING, ++column, "");
        aparam = new VariableInfo("aparam", "Item discrimination", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        bparam = new VariableInfo("bparam", "Item difficulty", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        cparam = new VariableInfo("cparam", "Item guessing", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        uparam = new VariableInfo("uparam", "Item slipping", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        ase = new VariableInfo("a_se", "Discrimination standard error", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        bse = new VariableInfo("b_se", "Difficulty standard error", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        cse = new VariableInfo("c_se", "Guessing standard error", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        use = new VariableInfo("u_se", "Slipping standard error", VariableType.NOT_ITEM, VariableType.DOUBLE, ++column, "");
        variables.add(name);
        variables.add(model);
        variables.add(ncat);
        variables.add(group);
        variables.add(aparam);
        variables.add(ase);
        variables.add(bparam);
        variables.add(bse);
        variables.add(cparam);
        variables.add(cse);
        variables.add(uparam);
        variables.add(use);

        VariableInfo step = null;
        VariableInfo stepSe = null;

        int stepNum = 0;
        if(maxCol>1){
            for(int i=0; i<maxCol;i++){
                stepNum = i+1;
                step = new VariableInfo("step"+stepNum, "Step parameter for category " + stepNum, VariableType.NOT_ITEM, VariableType.DOUBLE, column++, "");
                stepSe = new VariableInfo("step_se"+stepNum, "Standard error for category " + stepNum, VariableType.NOT_ITEM, VariableType.DOUBLE, column++, "");
                variables.add(step);
                variables.add(stepSe);
            }
        }

    }

    public void outputToDb()throws SQLException {
        PreparedStatement pstmt = null;

        try{

            createVariables();

            int ncol = variables.size();

            VariableTableName variableTableName = new VariableTableName(itemTableName.toString());
            dao.createTables(conn, itemTableName, variableTableName, variables);

            String updateString = "INSERT INTO " + itemTableName.getNameForDatabase() + " VALUES(";
			for(int i=0;i<ncol;i++){
				updateString+= "?";
				if(i<ncol-1){
					updateString+=",";
				}else{
					updateString+=")";
				}
			}

            conn.setAutoCommit(false);//begin transaction

            pstmt = conn.prepareStatement(updateString);
            int column = 12;
            int nrow = 0;


            for(int j=0;j<irm.length;j++){
                //begin adding information to prepared statement.
                //Item name and model type set for all items.
                pstmt.setString(1, irm[j].getName().toString());

                if(irm[j] instanceof Irm4PL){
                    pstmt.setString(2, "L4");
                }else if(irm[j] instanceof IrmGPCM){
                    pstmt.setString(2, "PC1");
                }else if(irm[j] instanceof IrmPCM2){
                    pstmt.setString(2, "PC4");
                }else{
                    pstmt.setString(2, "L3");
                }

                pstmt.setDouble(3, irm[j].getNcat());
                pstmt.setString(4, irm[j].getGroupId());

                //Only set discrimination parameter for model that include it.
                if(irm[j].getType()!=IrmType.PCM && irm[j].getType()!=IrmType.PCM2){
                    safeSetValue(pstmt, 5, irm[j].getDiscrimination());
                    safeSetValue(pstmt, 6, irm[j].getDiscriminationStdError());
                }else{
                    pstmt.setNull(5, Types.DOUBLE);
                    pstmt.setNull(6, Types.DOUBLE);
                }

                //Only set difficulty parameter for models that include it.
                if(irm[j].getType()!=IrmType.GPCM && irm[j].getType()!=IrmType.PCM2){
                    safeSetValue(pstmt, 7, irm[j].getDifficulty());
                    safeSetValue(pstmt, 8, irm[j].getDifficultyStdError());
                }else{
                    pstmt.setNull(7, Types.DOUBLE);
                    pstmt.setNull(8, Types.DOUBLE);
                }

                //Only set guessing and slipping parameter for models that include it.
                if(irm[j].getType()==IrmType.L4 || irm[j].getType()==IrmType.L3){
                    safeSetValue(pstmt, 9, irm[j].getGuessing());
                    safeSetValue(pstmt, 10, irm[j].getGuessingStdError());
                    safeSetValue(pstmt, 11, irm[j].getSlipping());
                    safeSetValue(pstmt, 12, irm[j].getSlippingStdError());
                }else{
                    pstmt.setNull(9, Types.DOUBLE);
                    pstmt.setNull(10, Types.DOUBLE);
                    pstmt.setNull(11, Types.DOUBLE);
                    pstmt.setNull(12, Types.DOUBLE);
                }
                column=12;

                //Add step parameters for polytomous items
                if(maxCol>1){
                    if(irm[j].getType()==IrmType.GPCM || irm[j].getType()==IrmType.PCM2){
                        double[] step = irm[j].getStepParameters();
                        double[] stepSe = irm[j].getStepStdError();
                        for(int k=1;k<step.length;k++){//First step is always zero. Skip it here.
                            safeSetValue(pstmt, ++column, step[k]);
                            safeSetValue(pstmt, ++column, stepSe[k]);
                        }
                    }else if(irm[j].getType()==IrmType.GPCM2 || irm[j].getType()==IrmType.PCM){
                        double[] step = irm[j].getStepParameters();
                        double[] stepSe = irm[j].getStepStdError();
                        for(int k=0;k<step.length;k++){//Will change with refactoring
                            safeSetValue(pstmt, ++column, step[k]);
                            safeSetValue(pstmt, ++column, stepSe[k]);
                        }
                    }else{
                        for(int k=0;k<maxCol;k++){
                            pstmt.setNull(++column, Types.DOUBLE);
                            pstmt.setNull(++column, Types.DOUBLE);
                        }
                    }

                }
                pstmt.executeUpdate();
                nrow++;
            }
            pstmt.close();

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
            if(pstmt!=null) pstmt.close();
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

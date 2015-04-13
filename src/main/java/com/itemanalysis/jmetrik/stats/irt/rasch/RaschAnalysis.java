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

package com.itemanalysis.jmetrik.stats.irt.rasch;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTree;
import javax.swing.SwingWorker;

import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.estimation.JointMaximumLikelihoodEstimation;
import com.itemanalysis.psychometrics.irt.estimation.RaschScaleQualityOutput;
import com.itemanalysis.psychometrics.irt.model.Irm3PL;
import com.itemanalysis.psychometrics.irt.model.IrmPCM;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.scaling.DefaultLinearTransformation;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.log4j.Logger;

public class RaschAnalysis extends SwingWorker<String, String>{

    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");
    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private ArrayList<VariableAttributes> variables = null;
    private RaschCommand command = null;
    private JmetrikTextFile tfa = null;
    private Throwable theException = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private StopWatch sw = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private double adjust = 0.3;
    public boolean itemTableAdded = false;
    public boolean residualTableAdded = false;
    private boolean hasfixedValues = false;
    private boolean showStart = false;
    private ArrayList<String> fixedVariables = null;
    private DatabaseName ipTabledbName = null;
    private DataTableName ipTableName = null;
    private JTree tree = null;
    private DataTableName itemOutputTable = null;
    private DataTableName residualOutputTable = null;

    private int globalMaxUpdate = 150;
    private double globalConvergence = 0.005;
    private boolean unbiased = false;
    private double nPeople = 0.0;
    private boolean ignoreMissingData = true;
    private boolean savePersonEstimates = false;
    private boolean savePersonFit = false;
    private boolean saveItemEstimates = false;
    private boolean saveResiduals = false;
    private RaschPersonDatabaseOutput personOut = null;

//    private byte[][] data = null;
//    private ItemResponseModel[] irm = null;
    private boolean centerItems = true;

    public RaschAnalysis(Connection conn, DatabaseAccessObject dao, RaschCommand command, JmetrikTextFile tfa){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.tfa = tfa;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    public void addItemsToDb(JointMaximumLikelihoodEstimation jmle)throws SQLException, IllegalArgumentException{
        try{
            String itemTableName = command.getFreeOption("itemout").getString();
            itemOutputTable = dao.getUniqueTableName(conn, itemTableName);
            RaschItemDatabaseOutput itemOut = new RaschItemDatabaseOutput(conn, dao, tableName, itemOutputTable, jmle);
            itemOut.outputToDb();
            itemTableAdded = true;
        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex);
        }catch(IllegalArgumentException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new IllegalArgumentException(ex);
        }
    }

    public void addResidualsToDb(JointMaximumLikelihoodEstimation jmle)throws SQLException{
        String residualTableName = command.getFreeOption("residout").getString();
        residualOutputTable = dao.getUniqueTableName(conn, residualTableName);
        IrtResidualOut rOut = new IrtResidualOut(conn, dao, jmle, tableName, residualOutputTable);

        try{
            rOut.outputToDb();
            residualTableAdded = true;
        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex);
        }

    }

    private ItemResponseModel[] getItemResponseModels() throws SQLException{
        ItemResponseModel[] irm = new ItemResponseModel[variables.size()];
        double[] threshold = null;
        int index = 0;
        String group = "";
        for(VariableAttributes v : variables){
            if(v.getType().getItemType()== ItemType.BINARY_ITEM){
                irm[index] = new Irm3PL(0.0, 1.0);
            }else{
                int ncat = v.getItemScoring().numberOfScoreLevels();

                threshold = new double[ncat-1];
                for(int i=0;i<ncat-1;i++){
                    threshold[i] = 0.0;
                }
                irm[index] = new IrmPCM(0.0, threshold, 1.0);
            }
            irm[index].setName(new VariableName(v.getName().toString()));
            group = v.getItemGroup();
            if("".equals(group)) group = v.getName().toString();
            irm[index].setGroupId(group);
            index++;
        }


        if(hasfixedValues){
            System.out.println("Setting fixed values");
            RaschFixedValues fixedValue = new RaschFixedValues();
            fixedValue.setFixedParameterValues(conn, ipTableName, irm, fixedVariables);

        }

        return irm;

    }

    private double getSampleSize()throws SQLException{
        int nrow = dao.getRowCount(conn, tableName);
        nPeople = (double)nrow;
        return nPeople;
    }

    private byte[][] getData()throws SQLException{
        this.firePropertyChange("status", "", "Summarizing data...");
        Statement stmt = null;
        ResultSet rs = null;
        Object response = null;
        byte responseScore = 0;

        int nrow = (int)getSampleSize();
        int ncol = variables.size();
        byte[][] data = new byte[nrow][ncol];

        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        for(VariableAttributes v : variables){
            select.addColumn(sqlTable, v.getName().nameForDatabase());
        }
        stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        rs=stmt.executeQuery(select.toString());

        int r = 0;
        int c = 0;
        while(rs.next()){
            c = 0;
            for(VariableAttributes v : variables){//columns in data will be in same order as variables
                response = rs.getObject(v.getName().nameForDatabase());
                if((response==null || response.equals("") || response.equals("NA")) && ignoreMissingData){
                    data[r][c] = -1;//code for omitted responses
                }else{
                    responseScore = (byte)v.getItemScoring().computeItemScore(response);
                    data[r][c] = responseScore;
                }
                c++;
            }
            r++;
        }
        rs.close();
        stmt.close();

        return data;

    }

    public void runEstimation(double intercept, double scale, int precision)throws SQLException{
        try{
            this.firePropertyChange("status", "", "Running JMLE...");

            JointMaximumLikelihoodEstimation jmle = new JointMaximumLikelihoodEstimation(getData(), getItemResponseModels());
            DefaultLinearTransformation linearTransformation = new DefaultLinearTransformation(intercept, scale);
            jmle.summarizeData(adjust);

            //compute start values
            jmle.itemProx();

            //print initial values and frequencies if requested
            if(showStart) publish("\n" + jmle.printBasicItemStats("PROX STARTING VALUES") + "\n\n");

            //estimate parameters and optionally adjust for bias
            jmle.estimateParameters(globalMaxUpdate, globalConvergence, centerItems);
            if(unbiased) jmle.biasCorrection();
            logger.info(jmle.printIterationHistory());

            //compute item fit statistics
            this.firePropertyChange("status", "", "Computing item fit...");
            jmle.computeItemFitStatistics();
            jmle.computeItemCategoryFitStatistics();

            //Create score table before applying any transformation to parameters.
            this.firePropertyChange("status", "", "Printing score table...");
            String scoreTable = jmle.printScoreTable(globalMaxUpdate, globalConvergence, adjust,
                    linearTransformation, precision);

            //compute standard errors
            this.firePropertyChange("status", "", "Computing standard errors...");
            jmle.computeItemStandardErrors();
            jmle.computePersonStandardErrors();

            //transform parameters - must be done after computing standard errors
            if(intercept!=0 && scale !=1) jmle.linearTransformation(intercept, scale);

            //print final jml estimates and score table
            this.firePropertyChange("status", "", "Printing estimates...");
            publish("\n" + jmle.printItemStats("FINAL JMLE ITEM STATISTICS") + "\n\n");
            publish(jmle.printCategoryStats());
            publish(scoreTable);

            //Compute and print scale quality statistics
            RaschScaleQualityOutput scaleOutput = new RaschScaleQualityOutput(
                    jmle.getItemSideScaleQuality(),
                    jmle.getPersonSideScaleQuality());
            publish("\n\n" + scaleOutput.printScaleQuality());

            //add item estimates to db
            if(saveItemEstimates){
                this.firePropertyChange("status", "", "Saving item estimates...");
                addItemsToDb(jmle);
            }

            //optionally save person estimates to database
            if(savePersonEstimates){
                this.firePropertyChange("status", "", "Saving person estimates...");
                if(personOut==null){
                    personOut = new RaschPersonDatabaseOutput(conn, dao, dbName, tableName, variables, jmle);
                }
                personOut.addEstimates();
            }

            //optionally save person fit to database
            if(savePersonFit){
                this.firePropertyChange("status", "", "Saving person fit statistics...");
                if(personOut==null){
                    personOut = new RaschPersonDatabaseOutput(conn, dao, dbName, tableName, variables, jmle);
                }
                personOut.addFitStatistics();
            }

            //optionally save residuals to database
            if(saveResiduals){
                this.firePropertyChange("status", "", "Saving residuals...");
                addResidualsToDb(jmle);
            }

            this.firePropertyChange("status", "", "Done");

        }catch(SQLException ex){
            logger.fatal(ex.getMessage(), ex);
            throw new SQLException(ex);
        }
    }

    private void processCommand()throws IllegalAccessException, SQLException{
        dbName = new DatabaseName(command.getPairedOptionList("data").getStringAt("db"));
        if(command.getFreeOption("adjust").hasValue()) adjust = command.getFreeOption("adjust").getDouble();

        //get variable info from db
        tableName = new DataTableName(command.getPairedOptionList("data").getStringAt("table"));
        VariableTableName variableTableName = new VariableTableName(tableName.toString());
        ArrayList<String> selectVariables = command.getFreeOptionList("variables").getString();
        variables = dao.getSelectedVariables(conn, variableTableName, selectVariables);

        if(command.getFreeOptionList("ifixed").hasValue()){
            ipTabledbName = new DatabaseName(command.getPairedOptionList("iptable").getStringAt("db"));
            ipTableName = new DataTableName(command.getPairedOptionList("iptable").getStringAt("table"));
            fixedVariables = command.getFreeOptionList("ifixed").getString();
            hasfixedValues  = true;
        }

        showStart = command.getSelectAllOption("item").isArgumentSelected("start");

        globalMaxUpdate = (command.getPairedOptionList("gupdate").getIntegerAt("maxiter")).intValue();
        globalConvergence = command.getPairedOptionList("gupdate").getDoubleAt("converge");
        unbiased = command.getSelectAllOption("item").isArgumentSelected("uconbias");

        ignoreMissingData = command.getSelectOneOption("missing").isValueSelected("ignore");

        saveItemEstimates = command.getSelectAllOption("item").isArgumentSelected("isave");
        savePersonEstimates = command.getSelectAllOption("person").isArgumentSelected("psave");
        savePersonFit = command.getSelectAllOption("person").isArgumentSelected("pfit");
        saveResiduals = command.getSelectAllOption("person").isArgumentSelected("rsave");
        centerItems = command.getSelectOneOption("center").isValueSelected("items");
    }

    public String doInBackground(){
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running Rasch Analysis...");
        this.firePropertyChange("progress-ind-on", null, null);

        String s = "";
        try{
            processCommand();

            runEstimation(command.getPairedOptionList("transform").getDoubleAt("intercept"),
                        command.getPairedOptionList("transform").getDoubleAt("scale"),
                        command.getPairedOptionList("transform").getIntegerAt("precision"));

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible

        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return s;
    }

    @Override
    protected void process(List<String> chunks){
        for(String s : chunks){
            tfa.append(s + "\n");
        }
    }

    @Override
    public void done(){
        try{
			if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
			}else{
                if(itemTableAdded) firePropertyChange("table-added", "", itemOutputTable);//will addArgument table to list
                if(residualTableAdded) firePropertyChange("table-added", "", residualOutputTable);//will addArgument table to list

                if(savePersonEstimates || savePersonFit){
                    for(VariableChangeListener v : variableChangeListeners){
                        personOut.addVariableChangeListener(v);
                    }
                    personOut.updateGui();
                }

                tfa.addText(get());
                tfa.addText("Elapsed time: " + sw.getElapsedTime());
                tfa.setCaretPosition(0);
                scriptLogger.info(command.paste());
            }

		}catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
		}
    }

    //===============================================================================================================
    //Handle variable changes here
    //   -Dialogs will use these methods to add their variable listeners
    //===============================================================================================================
    public synchronized void addVariableChangeListener(VariableChangeListener l){
        variableChangeListeners.add(l);
    }

    public synchronized void removeVariableChangeListener(VariableChangeListener l){
        variableChangeListeners.remove(l);
    }

    public synchronized  void removeAllVariableChangeListeners(){
        variableChangeListeners.clear();
    }

    public void fireVariableChanged(VariableChangeEvent event){
        for(VariableChangeListener l : variableChangeListeners){
            l.variableChanged(event);
        }
    }
    //===============================================================================================================


}


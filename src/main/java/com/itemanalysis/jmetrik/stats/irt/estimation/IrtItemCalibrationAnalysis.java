package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.commandbuilder.MegaOption;
import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DerbyIrtItemOutput;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.stats.irt.rasch.IrtResidualOut;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.jmetrik.workspace.VariableChangeType;
import com.itemanalysis.psychometrics.data.DataType;
import com.itemanalysis.psychometrics.data.ItemType;
import com.itemanalysis.psychometrics.data.VariableAttributes;
import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.distribution.DistributionApproximation;
import com.itemanalysis.psychometrics.distribution.NormalDistributionApproximation;
import com.itemanalysis.psychometrics.distribution.UserSuppliedDistributionApproximation;
import com.itemanalysis.psychometrics.irt.estimation.*;
import com.itemanalysis.psychometrics.irt.model.*;
import com.itemanalysis.psychometrics.tools.StopWatch;
import com.itemanalysis.squiggle.base.SelectQuery;
import com.itemanalysis.squiggle.base.Table;
import org.apache.commons.math3.analysis.integration.gauss.HermiteRuleFactory;
import org.apache.commons.math3.stat.Frequency;
import org.apache.commons.math3.util.Pair;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.sql.*;
import java.util.*;

public class IrtItemCalibrationAnalysis extends SwingWorker<String, String> {

    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private ArrayList<VariableAttributes> variables = null;
    private VariableAttributes groupByVariable = null;
    private Throwable theException = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private StopWatch sw = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private VariableTableName variableTableName = null;
    private JmetrikTextFile tfa = null;
    private ArrayList<String> selectedItems = null;
    private IrtItemCalibrationCommand command = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");
    static Logger scriptLogger = Logger.getLogger("jmetrik-script-logger");

    private ItemResponseModel[] itemResponseModels = null;
    private double tol = 0.001;
    private int maxIter = 250;
    private DataTableName itemOutputTableName = null;
    private boolean ignoreMissingData = true;
    private ItemResponseVector[] responseVectors = null;
    private DistributionApproximation latentDistribution = null;
    private boolean itemTableAdded = false;
    private int mincell = 1;
    private DataTableName latentOutputTableName = null;
    private DataTableName residualOutputTableName = null;
    private boolean estimatePersonScores = false;
    private VariableName scoreName = null;
    private String scoreType = "";
    private double scoreMean = 0;
    private double scoreSd = 1;
    private double scoreTol = 1e-5;
    private int scoreMaxIter = 150;
    private int scorePoints = 60;
    private double scoreMin = -4.5;
    private double scoreMax = 4.5;
    private boolean residualTableAdded = false;
    private MarginalMaximumLikelihoodEstimation mmle = null;
    private PersonScoringType personScoringType = PersonScoringType.EAP;
    private boolean personScoreAdded = false;
    private boolean saveResiduals = false;
    private VariableAttributes personScoreVariable = null;
    private VariableAttributes personScoreStdErrorVariable = null;
    private int numberOfExaminees = 0;
    private double[] theta = null;
    private boolean latentDistributionSaved = false;

    public IrtItemCalibrationAnalysis(Connection conn, DatabaseAccessObject dao, IrtItemCalibrationCommand command, JmetrikTextFile tfa){
        this.conn = conn;
        this.dao = dao;
        this.command = command;
        this.tfa = tfa;
        variableChangeListeners = new ArrayList<VariableChangeListener>();
    }

    /**
     * A support method for converting a command into an array of ItemResponseModel objects
     * using information from the command. It also configures other estimation options
     * according to value found in the command.
     */
    private void parseCommand() throws SQLException{

        selectedItems = new ArrayList<String>();

        //Convergence information
        tol = command.getOption("converge").getValueAtAsDouble("tol", 0.001);
        maxIter = command.getOption("converge").getValueAtAsInteger("maxiter", 250);

        //Data information
        String temp = command.getOption("data").getValueAt("db", "");
        dbName = new DatabaseName(temp);

        temp = command.getOption("data").getValueAt("table", "");
        if(!"".equals(temp)) tableName = new DataTableName(temp);
        variableTableName = new VariableTableName(tableName.toString());

        //Output information
        if(command.getOption("output").hasAnyValues()){
            temp = command.getOption("output").getValueAt("item", "");
            if(!"".equals(temp)) itemOutputTableName = new DataTableName(temp);

            temp = command.getOption("output").getValueAt("latent", "");
            if(!"".equals(temp)) latentOutputTableName = new DataTableName(temp);

            temp = command.getOption("output").getValueAt("residual", "");
            if(!"".equals(temp)){
                residualOutputTableName = new DataTableName(temp);
                saveResiduals = true;
            }

        }


        ignoreMissingData = command.getOption("missing").containsValue("ignore");

        mincell = command.getOption("itemfit").getValueAtAsInteger("mincell", 1);

        //PersonScoring options
        if(command.getOption("scoring").hasAnyValues()){
            estimatePersonScores = true;
            scoreName = new VariableName(command.getOption("scoring").getValueAt("name", "theta"));
            scoreType = command.getOption("scoring").getValueAt("type", "EAP");

            if("EAP".equals(scoreType)){
                personScoringType = PersonScoringType.EAP;
                scoreMean = command.getOption("scoring").getValueAtAsDouble("mean", 0);
                scoreSd = command.getOption("scoring").getValueAtAsDouble("sd", 0);
                scorePoints = command.getOption("scoring").getValueAtAsInteger("points", 150);

            }else if("MAP".equals(scoreType)){
                personScoringType = PersonScoringType.MAP;
                scoreMean = command.getOption("scoring").getValueAtAsDouble("mean", 0);
                scoreSd = command.getOption("scoring").getValueAtAsDouble("sd", 0);
                scoreTol = command.getOption("scoring").getValueAtAsDouble("tol", 0.00005);
                scoreMaxIter = command.getOption("scoring").getValueAtAsInteger("maxiter", 100);

            }else if("MLE".equals(scoreType)){
                personScoringType = PersonScoringType.MLE;
                scoreTol = command.getOption("scoring").getValueAtAsDouble("tol", 0.00005);
                scoreMaxIter = command.getOption("scoring").getValueAtAsInteger("maxiter", 100);
            }

            //All method require this information
            scoreMin = command.getOption("scoring").getValueAtAsDouble("min", -4.5);
            scoreMax = command.getOption("scoring").getValueAtAsDouble("max", 4.5);
        }


        //Latent distribution
        String[] df = {"normal", "0.0", "1.0"};
        extractLatentDistributionFromOption(
                command.getOption("latent").getValueAt("name", "normal"),
                command.getOption("latent").getValueAtAsDouble("min", -4.0),
                command.getOption("latent").getValueAtAsDouble("max", 4.0),
                command.getOption("latent").getValueAtAsInteger("points", 40)
                );

        //Create item response model objects
        ItemResponseModel irm = null;
        int nGroups = command.getOption("groups").getValueAsInteger(1);
        String gName = "";
        MegaOption option = null;
        double scalingConstant = 1.0;

        int itemIndex = 0;
        String[] selectedVariables = null;
        String[] emptyStringArray = {"null"};

        VariableName itemName = null;

        int nItems = 0;
        for(int i=0;i<nGroups; i++){
            gName = "group" + (i+1);
            option = command.getOption(gName);
            selectedVariables = option.getValuesAt("variables", emptyStringArray);
            nItems += selectedVariables.length;
        }
        itemResponseModels = new ItemResponseModel[nItems];

        for(int i=0;i<nGroups;i++){
            gName = "group" + (i+1);
            option = command.getOption(gName);

            //Get model
            String model = option.getValueAt("model", "L3");

            //Get number of response categories
            int ncat = option.getValueAtAsInteger("ncat", 2);

            //Get scaling constant
            scalingConstant = option.getValueAtAsDouble("scale", 1.0);

            //Get selected items for this group
            selectedVariables = option.getValuesAt("variables", emptyStringArray);

            for(int j=0;j<selectedVariables.length;j++){//Loop over all items in a group

                //Get starting values
                double[] empty = {-9};
                double[] startValues = option.getValuesAtAsDouble("start", empty);

                String[] ss = {"s"};

                //Create item response models for this group
                if("L4".equals(model)){
                    if(startValues[0]==-9){
                        irm = new Irm4PL(1.0, 0.0, 0.05, 0.95, scalingConstant);
                    }else{
                        irm = new Irm4PL(startValues[0], startValues[1], startValues[2], startValues[3], scalingConstant);
                    }

                }else if("L3".equals(model)){
                    if(startValues[0]==-9){
                        irm = new Irm3PL(1.0, 0.0, 0.05, scalingConstant);
                    }else{
                        irm = new Irm3PL(startValues[0], startValues[1], startValues[2], scalingConstant);
                        if(option.containsValueAt("fixed", "uparam") && startValues.length>=4){
                            irm.setSlipping(startValues[3]);
                            irm.setProposalSlipping(startValues[3]);
                        }
                    }

                }else if("L2".equals(model)){
                    if(startValues[0]==-9){
                        irm = new Irm3PL(1.0, 0.0, scalingConstant);
                    }else{
                        irm = new Irm3PL(startValues[0], startValues[1], scalingConstant);

                        if(option.containsValueAt("fixed", "cparam") && startValues.length>=3){
                            irm.setGuessing(startValues[2]);
                            irm.setProposalGuessing(startValues[2]);
                        }
                        if(option.containsValueAt("fixed", "uparam") && startValues.length>=4){
                            irm.setSlipping(startValues[3]);
                            irm.setProposalSlipping(startValues[3]);
                        }
                    }
                }else if ("L1".equals(model)){
                    if(startValues[0]==-9){
                        irm = new Irm3PL(0.0, scalingConstant);
                    }else{
                        irm = new Irm3PL(startValues[0], scalingConstant);
                        if(option.containsValueAt("fixed", "aparam") && startValues.length>=2){
                            irm.setDiscrimination(startValues[0]);
                            irm.setProposalDiscrimination(startValues[0]);
                        }
                        if(option.containsValueAt("fixed", "cparam") && startValues.length>=3){
                            irm.setGuessing(startValues[2]);
                            irm.setProposalGuessing(startValues[2]);
                        }
                        if(option.containsValueAt("fixed", "uparam") && startValues.length>=4){
                            irm.setSlipping(startValues[3]);
                            irm.setProposalSlipping(startValues[3]);
                        }
                    }
                }else if("PC1".equals(model)){
                    if(startValues[0]==-9){
                        startValues = new double[ncat];
                        for(int k=0;k<ncat;k++){
                            startValues[k] = 0.0;//TODO does not use user provided start values
                        }
                    }
                    irm = new IrmGPCM(1.0, startValues, scalingConstant);
                }else if("PC4".equals(model)){
                    if(startValues[0]==-9){
                        startValues = new double[ncat];
                        for(int k=0;k<ncat;k++){
                            startValues[k] = 0.0;//TODO does not use user provided start values
                        }
                    }
                    irm = new IrmPCM2(startValues, scalingConstant);
                }

                //Get priors
                String[] emptyString = {""};
                String[] priorString = option.getValuesAt("aprior", emptyString);

                if(!priorString[0].equals("")) irm.setDiscriminationPrior(extractPriorFromOption(priorString));

                priorString = option.getValuesAt("bprior", emptyString);
                if(!priorString[0].equals("")) irm.setDifficultyPrior(extractPriorFromOption(priorString));

                priorString = option.getValuesAt("cprior", emptyString);
                if(!priorString[0].equals("")) irm.setGuessingPrior(extractPriorFromOption(priorString));

                priorString = option.getValuesAt("uprior", emptyString);
                if(!priorString[0].equals("")) irm.setSlippingPrior(extractPriorFromOption(priorString));

                //Add item group name
                irm.setGroupId(gName);

                //And individual item name to irm and list of selected items
                itemName = new VariableName(selectedVariables[j]);
                irm.setName(itemName);
                selectedItems.add(selectedVariables[j]);
                itemResponseModels[itemIndex] = irm;
                itemIndex++;

            }//End loop over items within the group

        }//End loop over groups

        //Add item scoring information to item resposne models (Very inefficient nested loops)
        variables = dao.getSelectedVariables(conn, variableTableName, selectedItems);
        for(VariableAttributes v : variables){
            for(ItemResponseModel model : itemResponseModels){
                if(v.getName().equals(model.getName())){
                    model.setItemScoring(v.getItemScoring());
                    break;//Break inner loop
                }
            }
        }

    }

    private void savePersonScores() throws SQLException{
        int numberOfColumns = dao.getColumnCount(conn, tableName);
        theta = new double[numberOfExaminees];

        //begin transaction
        conn.setAutoCommit(false);

        //Person score variable
        personScoreVariable = new VariableAttributes(
                scoreName.toString(),
                personScoringType.toString() + " person score",
                ItemType.NOT_ITEM,
                DataType.DOUBLE,
                numberOfColumns+1,
                "");
        dao.addColumnToDb(conn, tableName, personScoreVariable);
        variables.add(personScoreVariable);

        //Person score standard error variable
        personScoreStdErrorVariable = new VariableAttributes(
                scoreName.toString()+"_se",
                personScoringType.toString() + " person score standard error",
                ItemType.NOT_ITEM,
                DataType.DOUBLE,
                numberOfColumns+2,
                "");
        dao.addColumnToDb(conn, tableName, personScoreStdErrorVariable);
        variables.add(personScoreStdErrorVariable);

        //Select items and new score variables
        Table sqlTable = new Table(tableName.getNameForDatabase());
        SelectQuery select = new SelectQuery();
        select.addColumn(sqlTable, personScoreVariable.getName().nameForDatabase());
        select.addColumn(sqlTable, personScoreStdErrorVariable.getName().nameForDatabase());

        //Update database variables
        Statement stmt = null;
        ResultSet rs = null;
        try{
            int i=0;
            double thetaSE = 0;
            IrtExaminee irtExaminee = new IrtExaminee(itemResponseModels);

            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs=stmt.executeQuery(select.toString());

            while(rs.next()){
                irtExaminee.setResponseVector(responseVectors[i]);

                //compute person ability estimates
                if(PersonScoringType.MAP==personScoringType){
                    theta[i] = irtExaminee.mapEstimate(scoreMean, scoreSd, scoreMin, scoreMax, scoreMaxIter, scoreTol);
                    thetaSE = irtExaminee.mapStandardErrorAt(theta[i]);
                }else if(PersonScoringType.MLE==personScoringType){
                    theta[i] = irtExaminee.maximumLikelihoodEstimate(scoreMin, scoreMax, scoreMaxIter, scoreTol);
                    thetaSE = irtExaminee.mleStandardErrorAt(theta[i]);
                }else{
                    //EAP
                    theta[i] = irtExaminee.eapEstimate(scoreMean, scoreSd, scoreMin, scoreMax, scorePoints);
                    thetaSE = irtExaminee.eapStandardErrorAt(theta[i]);
                }

                //Add value to database
                if(Double.isNaN(theta[i])){
                    rs.updateNull(personScoreVariable.getName().nameForDatabase());
                    rs.updateNull(personScoreStdErrorVariable.getName().nameForDatabase());
                }else if(Double.isNaN(thetaSE)){
                    rs.updateDouble(personScoreVariable.getName().nameForDatabase(), theta[i]);
                    rs.updateNull(personScoreStdErrorVariable.getName().nameForDatabase());
                }else{
                    rs.updateDouble(personScoreVariable.getName().nameForDatabase(), theta[i]);
                    rs.updateDouble(personScoreStdErrorVariable.getName().nameForDatabase(), thetaSE);
                }
                rs.updateRow();
                i++;

            }
        }catch(SQLException ex){
            conn.rollback();
            conn.setAutoCommit(true);
			throw new SQLException(ex);
        }finally{
            personScoreAdded = true;
            conn.commit();
            conn.setAutoCommit(true);//end transaction
            if(stmt!=null) stmt.close();
            if(rs!=null) rs.close();
        }



    }

    private void saveResiduals()throws SQLException{
        if(!personScoreAdded || residualOutputTableName==null) return;
        IrtResidualOut irtResidualOut = new IrtResidualOut(conn, dao,
                responseVectors,
                theta,
                itemResponseModels,
                tableName,
                residualOutputTableName);
        irtResidualOut.outputToDb();

        residualTableAdded = true;
    }

    private void saveLatentDistribution()throws SQLException{
        VariableAttributes theta = new VariableAttributes("theta", "Theta values (quadrature points)", ItemType.NOT_ITEM, DataType.DOUBLE, 0, "");
        VariableAttributes weight = new VariableAttributes("weight", "Density values (quadrature weights)", ItemType.NOT_ITEM, DataType.DOUBLE, 1, "");
        ArrayList<VariableAttributes> latentVariables = new ArrayList<VariableAttributes>();
        latentVariables.add(theta);
        latentVariables.add(weight);

        VariableTableName latentOutputVariableTableName = new VariableTableName(latentOutputTableName.toString());

        Statement stmt = null;
        ResultSet rs = null;

        try{
            conn.setAutoCommit(false);//start transaction
            dao.createTables(conn, latentOutputTableName, latentOutputVariableTableName, latentVariables);

            //connect to tables and update rows
            //Select items and new score variables
            Table sqlTable = new Table(latentOutputTableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            select.addColumn(sqlTable, theta.getName().nameForDatabase());
            select.addColumn(sqlTable, weight.getName().nameForDatabase());

            //Update database variables
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            rs=stmt.executeQuery(select.toString());

            int nrow = latentDistribution.getNumberOfPoints();
            double thetaValue = 0;
            double weightValue = 0;
            for(int i=0;i<nrow;i++){
                rs.moveToInsertRow();

                thetaValue = latentDistribution.getPointAt(i);
                if(Double.isNaN(thetaValue) || Double.isInfinite(thetaValue)){
                    rs.updateNull(theta.getName().nameForDatabase());
                }else{
                    rs.updateDouble(theta.getName().nameForDatabase(), thetaValue);
                }

                weightValue = latentDistribution.getDensityAt(i);
                if(Double.isNaN(weightValue) || Double.isInfinite(weightValue)){
                    rs.updateNull(weight.getName().nameForDatabase());
                }else{
                    rs.updateDouble(weight.getName().nameForDatabase(), weightValue);
                }

                rs.insertRow();
            }

            dao.setTableInformation(conn, latentOutputVariableTableName, nrow, "Latent distribution table");
            latentDistributionSaved = true;

        }catch(SQLException ex){
            conn.rollback();
            conn.setAutoCommit(true);
			throw new SQLException(ex);
        }finally{
            personScoreAdded = true;
            conn.commit();
            conn.setAutoCommit(true);//end transaction
            if(stmt!=null) stmt.close();
            if(rs!=null) rs.close();
        }

    }

    /**
     * Support method for converting command informaiton into a latent distribution object.
     *
     * @param name name of latent distribution
     * @param min minimum value for distribution
     * @param max maximum value for distribution
     * @param nPoints number of quadrature points in distribution
     */
    private void extractLatentDistributionFromOption(String name, double min, double max, int nPoints){
        if("normal".equals(name)){
            latentDistribution = new NormalDistributionApproximation(min, max, nPoints);
        }else if("GH".equals(name)){
            HermiteRuleFactory gaussHermite = new HermiteRuleFactory();
            Pair<double[], double[]> dist = gaussHermite.getRule(41);
            latentDistribution = new UserSuppliedDistributionApproximation(dist.getKey(), dist.getValue());
        }else{
            latentDistribution = new NormalDistributionApproximation(-4.0, 4.0, 40);
        }
    }

    /**
     * Support method for converting option information into an item parameter prior object.
     * The text representation is expected to be as shown on right hand side of each line below.
     * Normal(mean, sd) = {normal, 0, 1}
     * logNormal(logmean, logsd) = {lognormal, 0, 1}
     * Beta4(shape1, shape2, lower, upper) = {beta, 1, 2, 3, 4}
     *
     * @param text text representation of of the prior
     * @return
     */
    private ItemParamPrior extractPriorFromOption(String[] text){
        ItemParamPrior prior = null;
        if("normal".equals(text[0].trim())){
            prior = new ItemParamPriorNormal(
                    Double.parseDouble(text[1].trim()),
                    Double.parseDouble(text[2].trim())
            );
        }else if("lognormal".equals(text[0].trim())){
            prior = new ItemParamPriorLogNormal(
                    Double.parseDouble(text[1].trim()),
                    Double.parseDouble(text[2].trim())
            );
        }else{
            //beta prior expected by default
            prior = new ItemParamPriorBeta4(
                    Double.parseDouble(text[1].trim()),
                    Double.parseDouble(text[2].trim()),
                    Double.parseDouble(text[3].trim()),
                    Double.parseDouble(text[4].trim())
            );
        }
        return prior;
    }

    private double getSampleSize()throws SQLException {
        int nrow = dao.getRowCount(conn, tableName);
        return (double)nrow;
    }

    /**
     * Summarizes data into response vectors. If condensed==true, it will only store
     * unique response vectors and a frequency count. Otherwise, there will be
     * one response vector for each examinee. If saving person scores or residuals,
     * then condensed should be false.
     *
     * @param condensed if true will save unique response patterns and a frequency count. Otherwise
     *                  will store a response vector for each examinee.
     *
     * @throws SQLException
     */
    private void summarizeData(boolean condensed)throws SQLException{
        this.firePropertyChange("status", "", "Summarizing data...");

        Frequency freq = new Frequency();
        String responseString = "";

        Statement stmt = null;
        ResultSet rs = null;
        Object response = null;
        byte responseScore = 0;

        numberOfExaminees = (int)getSampleSize();
        int ncol = itemResponseModels.length;
        byte[] rv;

        if(!condensed) responseVectors = new ItemResponseVector[numberOfExaminees];

        try{

            //Query the db. Variables include the select items and the grouping variable is one is available.
            Table sqlTable = new Table(tableName.getNameForDatabase());
            SelectQuery select = new SelectQuery();
            for(ItemResponseModel irm : itemResponseModels){
                select.addColumn(sqlTable, irm.getName().nameForDatabase());
            }
            if(groupByVariable!=null) select.addColumn(sqlTable, groupByVariable.getName().nameForDatabase());
            stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            rs=stmt.executeQuery(select.toString());

            //Create item response vectors for the analysis.
            int r = 0;
            int c = 0;
            ItemResponseVector iVec = null;
            while(rs.next()){
                c = 0;
                rv = new byte[ncol];

                //Using the item response model array as the source of item names will ensure that the
                //items in the response vector are the same order as the items in the array of item
                //response model objects.
                for(ItemResponseModel irm : itemResponseModels){
                    response = rs.getObject(irm.getName().nameForDatabase());
                    if((response==null || response.equals("") || response.equals("NA")) && ignoreMissingData){
                        rv[c] = -1;//code for omitted responses
                    }else{
                        responseScore = (byte)irm.getItemScoring().computeItemScore(response);
                        rv[c] = responseScore;
                    }
                    c++;
                }
                iVec = new ItemResponseVector(rv, 1.0);

                if(condensed){
                    freq.addValue(iVec);
                }else{
                    responseVectors[r] = iVec;
                }
                r++;
            }//End initial summary

            if(condensed){
                responseVectors = new ItemResponseVector[freq.getUniqueCount()];
                int index = 0;
                Iterator<Comparable<?>> iter = freq.valuesIterator();
                while(iter.hasNext()){
                    responseVectors[index] = (ItemResponseVector)iter.next();
                    responseVectors[index].setFrequency(Long.valueOf(freq.getCount(responseVectors[index])).doubleValue());
                    index++;
                }

            }


            //For debugging
//            System.out.println("Unique Vectors: " + freq.getUniqueCount());
//            for(int i=0;i<responseVectors.length;i++){
//                System.out.println(responseVectors[i].printResponseVector());
//            }

            freq = null;
        }catch(SQLException ex){
            throw(ex);
        }finally{
            if(rs!=null) rs.close();
            if(stmt!=null) stmt.close();
        }

    }

    /**
     * Method responsible for estimating parameters.
     *
     * @return
     */
    private String estimateParameters(){
        this.firePropertyChange("status", "", "Estimating parameters...");

        PrintableEMStatusListener emStatus = new PrintableEMStatusListener();
        LiveEMStatusListener liveStatus = new LiveEMStatusListener();

        //Compute starting values
        StartingValues startValues = new StartingValues(responseVectors, itemResponseModels);
        startValues.addEMStatusListener(emStatus);
        itemResponseModels = startValues.computeStartingValues();
        logger.info("IRT START VALUES\n" + startValues.printItemParameters());

        mmle = new MarginalMaximumLikelihoodEstimation(
                responseVectors,
                itemResponseModels,
                latentDistribution);

        mmle.addEMStatusListener(emStatus);
        mmle.addEMStatusListener(liveStatus);
        mmle.setVerbose(true);//will store details for each EM cycle
        mmle.estimateParameters(tol, maxIter);
        mmle.computeItemStandardErrors();

        publish(mmle.printItemParameters()+"\n\n");
        logger.info(emStatus.toString());

        this.firePropertyChange("status", "", "Computing item fit statistics...");
        mmle.computeSX2ItemFit(mincell);
        publish(mmle.printItemFitStatistics() + "\n\n");
        publish(mmle.printLatentDistribution() + "\n\n");

        return sw.getElapsedTime();
    }

    @Override
    protected void process(List<String> chunks){
        for(String s : chunks){
            tfa.append(s + "\n");
        }
    }

    private void saveItemEstimates()throws SQLException{
        this.firePropertyChange("status", "", "Saving estimates...");
        itemOutputTableName = dao.getUniqueTableName(conn, itemOutputTableName.toString());
        DerbyIrtItemOutput itemOutput = new DerbyIrtItemOutput(conn, dao, tableName, itemOutputTableName, itemResponseModels);
        itemOutput.outputToDb();
        itemTableAdded = true;
    }

    @Override
    public String doInBackground(){
        String s = "";
        sw = new StopWatch();
        this.firePropertyChange("status", "", "Running IRT Item Calibration...");
        this.firePropertyChange("progress-ind-on", null, null);

        try{
            parseCommand();

            boolean condensed = true;
            if(estimatePersonScores || saveResiduals) condensed = false;
            summarizeData(condensed);

            publish(printHeader());

            estimateParameters();

            if(itemOutputTableName!=null) saveItemEstimates();
            if(latentOutputTableName!=null) saveLatentDistribution();
            if(estimatePersonScores){
                this.firePropertyChange("status", "", "Saving person scores...");
                savePersonScores();
            }

            if(saveResiduals){
                this.firePropertyChange("status", "", "Saving residuals...");
                saveResiduals();
            }

            s = sw.getElapsedTime();
            firePropertyChange("status", "", "Done: " + s);
            return s;
        }catch(Throwable t){
            logger.fatal(t.getMessage(), t);
            theException=t;
        }
        return s;
    }

    @Override
    public void done(){
        try{
            if(theException!=null){
                logger.fatal(theException.getMessage(), theException);
                firePropertyChange("error", "", "Error - Check log for details.");
			}else{
                //Fire database changed information
                if(itemTableAdded) firePropertyChange("table-added", "", itemOutputTableName);//will addArgument table to list
                if(residualTableAdded) firePropertyChange("table-added", "", residualOutputTableName);//will addArgument table to list
                if(personScoreAdded){
                    fireVariableChanged(new VariableChangeEvent(this, tableName, personScoreVariable, VariableChangeType.VARIABLE_ADDED));
                    fireVariableChanged(new VariableChangeEvent(this, tableName, personScoreStdErrorVariable, VariableChangeType.VARIABLE_ADDED));
                }

                if(latentDistributionSaved){
                    firePropertyChange("table-added", "", latentOutputTableName);//will addArgument table to list
                }

                tfa.addText(get());
                tfa.addText("Elapsed time: " + sw.getElapsedTime());
                tfa.setCaretPosition(0);
                scriptLogger.info(command.paste());
            }
        }catch(Exception ex){
            logger.fatal(ex.getMessage(), ex);
            firePropertyChange("error", "", "Error - Check log for details.");
        }finally{
            firePropertyChange("progress-off", null, null); //make statusbar progress not visible
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

    public String printHeader()throws IllegalArgumentException{
        StringBuilder header = new StringBuilder();
        Formatter f = new Formatter(header);
        String s1 = String.format("%1$tB %1$te, %1$tY  %tT", Calendar.getInstance());
        int len = 43+Double.valueOf(Math.floor(Double.valueOf(s1.length()).doubleValue()/2.0)).intValue();
        String dString = dbName.toString() + "." + tableName.toString();
        int len2 = 43+Double.valueOf(Math.floor(Double.valueOf(dString.length()).doubleValue()/2.0)).intValue();

        f.format("%53s", "IRT ITEM CALIBRATION"); f.format("%n");
        f.format("%" + len2 + "s", dString); f.format("%n");
        f.format("%" + len + "s", s1); f.format("%n");
        f.format("%n");
        f.format("%n");
        f.format("%n");

        return f.toString();
    }

    public class LiveEMStatusListener implements EMStatusListener {
        public void handleEMStatusEvent(EMStatusEventObject eventObject){
            Formatter f = new Formatter();
            String s = eventObject.getStatus();
            if(!"".equals(s)){
                f.format(eventObject.getStatus() + "\n");
            }else{
                f.format("%10s", "EM CYCLE: ");
                f.format("%5d", eventObject.getIteration()); f.format("%4s", "");
                f.format("%.10f", eventObject.getDelta()); f.format("%4s", "");
                f.format("%.10f", eventObject.getLoglikelihood()); //f.format("%n");
            }
            IrtItemCalibrationAnalysis.this.firePropertyChange("status", "", f.toString());
        }
    }

}

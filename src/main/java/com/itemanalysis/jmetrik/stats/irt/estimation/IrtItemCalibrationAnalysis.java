package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.commandbuilder.MegaOption;
import com.itemanalysis.jmetrik.dao.DatabaseAccessObject;
import com.itemanalysis.jmetrik.dao.DerbyIrtItemOutput;
import com.itemanalysis.jmetrik.sql.DataTableName;
import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.VariableTableName;
import com.itemanalysis.jmetrik.swing.JmetrikTextFile;
import com.itemanalysis.jmetrik.workspace.VariableChangeEvent;
import com.itemanalysis.jmetrik.workspace.VariableChangeListener;
import com.itemanalysis.psychometrics.data.VariableInfo;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class IrtItemCalibrationAnalysis extends SwingWorker<String, String> {

    private ArrayList<VariableChangeListener> variableChangeListeners = null;
    private ArrayList<VariableInfo> variables = null;
    private VariableInfo groupByVariable = null;
    private Throwable theException = null;
    private Connection conn = null;
    private DatabaseAccessObject dao = null;
    private StopWatch sw = null;
    private DatabaseName dbName = null;
    private DataTableName tableName = null;
    private VariableTableName variableTableName = null;
    private DataTableName ipTable = null;
    private JmetrikTextFile tfa = null;
    private ArrayList<String> selectedItems = null;
    private IrtItemCalibrationCommand command = null;
    static Logger logger = Logger.getLogger("jmetrik-logger");

    private ItemResponseModel[] itemResponseModels = null;
    private double tol = 0.001;
    private int maxIter = 250;
    private DataTableName itemOutputTableName = null;
    private boolean ignoreMissingData = true;
    private ItemResponseVector[] responseVectors = null;
    private DistributionApproximation latentDistribution = null;
    private boolean itemTableAdded = false;

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
        temp = command.getOption("itemout").getValueAt("table", "");
        if(!"".equals(temp)) itemOutputTableName = new DataTableName(temp);

        ignoreMissingData = command.getOption("missing").containsValue("ignore");

        //Latent distribution
        String[] df = {"normal", "0.0", "1.0"};
        ;
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
        for(VariableInfo v : variables){
            for(ItemResponseModel model : itemResponseModels){
                if(v.getName().equals(model.getName())){
                    model.setItemScoring(v.getItemScoring());
                    break;//Break inner loop
                }
            }
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
     * Support method for converting database table into an array of item response vectors.
     * NOTE: This could be improved. The item response vectors can be stored as unique values with frequency counts.
     * This version stored on vector for each examinee.
     *
     * @throws SQLException
     */
    private void summarizeData()throws SQLException{
        this.firePropertyChange("status", "", "Summarizing data...");

        Frequency freq = new Frequency();
        String responseString = "";

        Statement stmt = null;
        ResultSet rs = null;
        Object response = null;
        byte responseScore = 0;

        int nrow = (int)getSampleSize();
        int ncol = variables.size();
        byte[] rv;
//        responseVectors = new ItemResponseVector[nrow];

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
                freq.addValue(iVec);
//                responseVectors[r] = iVec;
                r++;
            }//End initial summary

            responseVectors = new ItemResponseVector[freq.getUniqueCount()];
            int index = 0;
            Iterator<Comparable<?>> iter = freq.valuesIterator();
            while(iter.hasNext()){
                responseVectors[index] = (ItemResponseVector)iter.next();
                responseVectors[index].setFrequency(Long.valueOf(freq.getCount(responseVectors[index])).doubleValue());
                index++;
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

//    public ItemResponseVector[] getResponseVectors(File f, boolean headerIncluded){
//        Frequency freq = new Frequency();
//        String responseString = "";
//
//        try{
//            BufferedReader br = new BufferedReader(new FileReader(f));
//            String line = "";
//            if(headerIncluded) br.readLine();//skip header
//            while((line=br.readLine())!=null){
//                line = line.replaceAll(",", "");
//                freq.addValue(line);
//            }
//            br.close();
//
//        }catch(IOException ex){
//            ex.printStackTrace();
//        }
//
//        ItemResponseVector[] responseData = new ItemResponseVector[freq.getUniqueCount()];
//        ItemResponseVector irv = null;
//        Iterator<Comparable<?>> iter = freq.valuesIterator();
//        int index = 0;
//        byte[] rv = null;
//
//        //create array of ItemResponseVector objects
//        while(iter.hasNext()){
//            Comparable<?> value = iter.next();
//            responseString = value.toString();
//
//            int n=responseString.length();
//            rv = new byte[n];
//
//            String response = "";
//            for (int i = 0;i < n; i++){
//                response = String.valueOf(responseString.charAt(i)).toString();
//                rv[i] = Byte.parseByte(response);
//            }
//
//            //create response vector objects
//            irv = new ItemResponseVector(rv, Long.valueOf(freq.getCount(value)).doubleValue());
//            responseData[index] = irv;
//            index++;
//        }
//
//        return responseData;
//
//    }

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

        MarginalMaximumLikelihoodEstimation mmle = new MarginalMaximumLikelihoodEstimation(
                responseVectors,
                itemResponseModels,
                latentDistribution);

        mmle.addEMStatusListener(emStatus);
        mmle.addEMStatusListener(liveStatus);
        mmle.setVerbose(true);//will store details for each EM cycle
        mmle.estimateParameters(tol, maxIter);
        mmle.computeItemStandardErrors();

        logger.info(emStatus.toString());

        String output = printHeader() + mmle.printItemParameters();

        return output;
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
        logger.info(command.paste());

        try{
            parseCommand();
            summarizeData();
            s = estimateParameters();
            if(itemOutputTableName!=null) saveItemEstimates();

            firePropertyChange("status", "", "Done: " + sw.getElapsedTime());

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

                tfa.addText(get());
                tfa.addText("Elapsed time: " + sw.getElapsedTime());
                tfa.setCaretPosition(0);
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

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

import com.itemanalysis.psychometrics.data.VariableName;
import com.itemanalysis.psychometrics.irt.equating.RobustZEquatingTest;
import com.itemanalysis.psychometrics.irt.model.IrmType;
import com.itemanalysis.psychometrics.irt.model.ItemResponseModel;
import com.itemanalysis.psychometrics.polycor.PearsonCorrelation;
import com.itemanalysis.psychometrics.texttable.TextTable;
import com.itemanalysis.psychometrics.texttable.TextTableColumnFormat;
import com.itemanalysis.psychometrics.texttable.TextTablePosition;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.LinkedHashMap;

public class CommonItemSummaryStatistics {

    private LinkedHashMap<VariableName, ItemResponseModel> irmX = null;
    private LinkedHashMap<VariableName, ItemResponseModel> irmY = null;
    private ArrayList<VariableName> varNamesA = null;
    private ArrayList<VariableName> varNamesB = null;
    private double[] aX = null;
    private double[] bX = null;
    private double[] cX = null;
    private double[] aY = null;
    private double[] bY = null;
    private double[] cY = null;
    private int numberOfCommonItems = 0;

    private Mean xAMean;
    private Mean yAMean;
    private Mean xBMean;
    private Mean yBMean;
    private Mean xCMean;
    private Mean yCMean;

    private StandardDeviation xASd;
    private StandardDeviation yASd;
    private StandardDeviation xBSd;
    private StandardDeviation yBSd;
    private StandardDeviation xCSd;
    private StandardDeviation yCSd;

    public CommonItemSummaryStatistics(LinkedHashMap<VariableName, ItemResponseModel> irmX, LinkedHashMap<VariableName, ItemResponseModel> irmY){
        this.irmX = irmX;
        this.irmY = irmY;
        varNamesA = new ArrayList<VariableName>();
        varNamesB = new ArrayList<VariableName>();
        numberOfCommonItems = irmX.size();
        createArrays();
    }

    private void createArrays(){
        xAMean = new Mean();
        yAMean = new Mean();
        xBMean = new Mean();
        yBMean = new Mean();
        xCMean = new Mean();
        yCMean = new Mean();

        xASd = new StandardDeviation();
        yASd = new StandardDeviation();
        xBSd = new StandardDeviation();
        yBSd = new StandardDeviation();
        xCSd = new StandardDeviation();
        yCSd = new StandardDeviation();


        int xBinary = 0;
        int xPolytomous = 0;
        int xTotalSteps = 0;
        ItemResponseModel irm;
        for(VariableName v : irmX.keySet()){
            irm = irmX.get(v);
            if(irm.getNcat()==2){
                xBinary++;
            }else{
                xPolytomous++;
            }
            xTotalSteps+=(irm.getNcat()-1);
        }

        aX = new double[xBinary+xPolytomous];
        bX = new double[xTotalSteps];
        cX = new double[xBinary];

        int yBinary = 0;
        int yPolytomous = 0;
        int yTotalSteps = 0;
        for(VariableName v : irmY.keySet()){
            irm = irmY.get(v);
            if(irm.getNcat()==2){
                yBinary++;
            }else{
                yPolytomous++;
            }
            yTotalSteps+=(irm.getNcat()-1);
        }

        aY = new double[yBinary+yPolytomous];
        bY = new double[yTotalSteps];
        cY = new double[yBinary];

        //populate arrays for Form X
        int itemIndex = 0;
        int binaryIndex = 0;
        int polyIndex = 0;
        int ncat = 0;
        for(VariableName v : irmX.keySet()){
            irm = irmX.get(v);
            ncat = irm.getNcat();

            varNamesA.add(v);

            aX[itemIndex] = irm.getDiscrimination();
            xAMean.increment(aX[itemIndex]);
            xASd.increment(aX[itemIndex]);

            if(ncat==2){
                cX[binaryIndex] = irm.getGuessing();
                xCMean.increment(cX[binaryIndex]);
                xCSd.increment(cX[binaryIndex]);

                bX[polyIndex] = irm.getDifficulty();
                xBMean.increment(bX[polyIndex]);
                xBSd.increment(bX[polyIndex]);

                binaryIndex++;
                polyIndex++;
                varNamesB.add(v);
            }else{
                double[] steps;
                if(irm.getType()== IrmType.GPCM || irm.getType()==IrmType.PCM2){
                    steps = irm.getStepParameters();
                    steps = Arrays.copyOfRange(steps, 1, steps.length);//Omit first step since it is zero.
                }else{
                    steps = irm.getStepParameters();
                }

                for(int k=0;k<steps.length;k++){
                    bX[polyIndex] = steps[k];
                    xBMean.increment(bX[polyIndex]);
                    xBSd.increment(bX[polyIndex]);

                    polyIndex++;
                    varNamesB.add(new VariableName(v.toString()+"_"+(k+1)));
                }
            }
            itemIndex++;

        }//end loop over items


        //populate arrays for Form Y
        itemIndex = 0;
        binaryIndex = 0;
        polyIndex = 0;
        ncat = 0;
        for(VariableName v : irmY.keySet()){
            irm = irmY.get(v);
            ncat = irm.getNcat();

            aY[itemIndex] = irm.getDiscrimination();
            yAMean.increment(aY[itemIndex]);
            yASd.increment(aY[itemIndex]);

            if(ncat==2){
                cY[binaryIndex] = irm.getGuessing();
                yCMean.increment(cY[binaryIndex]);
                yCSd.increment(cY[binaryIndex]);

                bY[polyIndex] = irm.getDifficulty();
                yBMean.increment(bY[polyIndex]);
                yBSd.increment(bY[polyIndex]);

                binaryIndex++;
                polyIndex++;
            }else{
                double[] steps;
                if(irm.getType()== IrmType.GPCM || irm.getType()==IrmType.PCM2){
                    steps = irm.getStepParameters();
                    steps = Arrays.copyOfRange(steps, 1, steps.length);//Omit first step since it is zero.
                }else{
                    steps = irm.getStepParameters();
                }

                for(int k=0;k<steps.length;k++){
                    bY[polyIndex] = steps[k];
                    yBMean.increment(bY[polyIndex]);
                    yBSd.increment(bY[polyIndex]);

                    polyIndex++;
                }
            }
            itemIndex++;

        }//end loop over items

    }


    public String robustZTest(){
        StringBuilder sb = new StringBuilder();
        RobustZEquatingTest robustZ = new RobustZEquatingTest(aX, aY, bX, bY, 0.05);
        robustZ.setNames(varNamesA, varNamesB);
        sb.append(robustZ.print(true) + "\n\n");
        sb.append(robustZ.print(false) + "\n\n");

        return sb.toString();
    }

    public String commonItemCorrelations(){
        PearsonCorrelation corA = new PearsonCorrelation();
        PearsonCorrelation corB = new PearsonCorrelation();
        for(int i=0;i<numberOfCommonItems;i++){
            corB.increment(bX[i], bY[i]);
            corA.increment(aX[i], aY[i]);
        }

        TextTable table = new TextTable();
        TextTableColumnFormat[] columnFormats = new TextTableColumnFormat[3];
        TextTableColumnFormat cf0 = new TextTableColumnFormat();
        cf0.setStringFormat(15, TextTableColumnFormat.OutputAlignment.LEFT);
        columnFormats[0] = cf0;
        TextTableColumnFormat cf1 = new TextTableColumnFormat();
        cf1.setDoubleFormat(8, 4, TextTableColumnFormat.OutputAlignment.RIGHT);
        columnFormats[1] = cf1;
        TextTableColumnFormat cf2 = new TextTableColumnFormat();
        cf2.setDoubleFormat(8, 4, TextTableColumnFormat.OutputAlignment.RIGHT);
        columnFormats[2] = cf2;
        int numRows = 6;
        numRows++;
        table.addAllColumnFormats(columnFormats, numRows);

        table.getRowAt(0).addHeader(0, 3, "Correlation and SD Ratio", TextTablePosition.CENTER);

        table.getRowAt(1).addHorizontalRule(0, 3, "=");

        table.getRowAt(2).addHeader(0, 1, "Parameter", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(1, 1, "r", TextTablePosition.CENTER);
        table.getRowAt(2).addHeader(2, 1, "Sy/Sx", TextTablePosition.CENTER);

        table.getRowAt(3).addHorizontalRule(0, 3, "-");

        table.getRowAt(4).addStringAt(0, "Difficulty");
        table.getRowAt(4).addDoubleAt(1, corB.value());
        table.getRowAt(4).addDoubleAt(2, yBSd.getResult()/xBSd.getResult());

        table.getRowAt(5).addStringAt(0, "Discrimination");
        table.getRowAt(5).addDoubleAt(1, corA.value());
        table.getRowAt(5).addDoubleAt(2, yASd.getResult()/xASd.getResult());

        table.getRowAt(6).addHorizontalRule(0, 3, "=");

        return table.toString() + "\n\n";
    }

    public String printItemSummary(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);
        f.format("%n");
        f.format("%64s", "                    ITEM SUMMARY STATISTICS                     "); f.format("%n");
        f.format("%64s", "================================================================"); f.format("%n");
        f.format("%-15s", " Form");f.format("%-17s", "Parameter");f.format("%-17s", " Mean"); f.format("%-15s", "S.D."); f.format("%n");
        f.format("%64s", "----------------------------------------------------------------"); f.format("%n");
        f.format("%-14s", " X (From)"); f.format("%-17s", " Discrimination");    f.format("%10.6f", xAMean.getResult()); f.format("%6s"," "); f.format("%10.6f", xASd.getResult()); f.format("%n");
        f.format("%-14s", "  "); f.format("%-17s", " Difficulty");    f.format("%10.6f", xBMean.getResult()); f.format("%6s"," "); f.format("%10.6f", xBSd.getResult()); f.format("%n");
        f.format("%-14s", "  "); f.format("%-17s", " Guessing");    f.format("%10.6f", xCMean.getResult()); f.format("%6s"," "); f.format("%10.6f", xCSd.getResult()); f.format("%n");
        f.format("%64s", " "); f.format("%n");
        f.format("%-14s", " Y (To)"); f.format("%-17s", " Discrimination");    f.format("%10.6f", yAMean.getResult()); f.format("%6s"," "); f.format("%10.6f", yASd.getResult()); f.format("%n");
        f.format("%-14s", "  "); f.format("%-17s", " Difficulty");    f.format("%10.6f", yBMean.getResult()); f.format("%6s"," "); f.format("%10.6f", yBSd.getResult()); f.format("%n");
        f.format("%-14s", "  "); f.format("%-17s", " Guessing");    f.format("%10.6f", yCMean.getResult()); f.format("%6s"," "); f.format("%10.6f", yCSd.getResult()); f.format("%n");
        f.format("%64s", "================================================================"); f.format("%n");
        f.format("%n");
        return f.toString();
    }


}

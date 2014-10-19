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

package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.commandbuilder.MegaCommand;
import com.itemanalysis.jmetrik.graph.barchart.BarChartProcess;
import com.itemanalysis.jmetrik.graph.histogram.HistogramProcess;
import com.itemanalysis.jmetrik.graph.irt.IrtPlotProcess;
import com.itemanalysis.jmetrik.graph.itemmap.ItemMapProcess;
import com.itemanalysis.jmetrik.graph.linechart.LineChartProcess;
import com.itemanalysis.jmetrik.graph.nicc.NonparametricCurveProcess;
import com.itemanalysis.jmetrik.graph.piechart.PieChartProcess;
import com.itemanalysis.jmetrik.graph.scatterplot.ScatterplotProcess;
import com.itemanalysis.jmetrik.scoring.BasicScoringProcess;
import com.itemanalysis.jmetrik.scoring.ScoringProcess;
import com.itemanalysis.jmetrik.stats.cmh.CmhProcess;
import com.itemanalysis.jmetrik.stats.correlation.CorrelationProcess;
import com.itemanalysis.jmetrik.graph.density.DensityProcess;
import com.itemanalysis.jmetrik.stats.descriptives.DescriptiveProcess;
import com.itemanalysis.jmetrik.stats.frequency.FrequencyProcess;
import com.itemanalysis.jmetrik.stats.irt.equating.IrtEquatingProcess;
import com.itemanalysis.jmetrik.stats.irt.estimation.IrtItemCalibrationProcess;
import com.itemanalysis.jmetrik.stats.irt.estimation.IrtPersonScoringProcess;
import com.itemanalysis.jmetrik.stats.irt.linking.IrtLinkingProcess;
import com.itemanalysis.jmetrik.stats.irt.rasch.RaschAnalysisProcess;
import com.itemanalysis.jmetrik.stats.itemanalysis.ItemAnalysisProcess;
import com.itemanalysis.jmetrik.stats.ranking.RankingProcess;
import com.itemanalysis.jmetrik.stats.scaling.TestScalingProcess;
import com.itemanalysis.jmetrik.stats.transformation.LinearTransformationProcess;

public class JmetrikProcessFactory {

    public JmetrikProcessFactory(){
        
    }

    public JmetrikProcess getProcess(Command command){
        return getProcess(command.getName());
    }

    public JmetrikProcess getProcess(MegaCommand command){
        return getProcess(command.getCommandName());
    }
    
    public JmetrikProcess getProcess(String commandName){

        //database and table procceses
        if(commandName.equals("import")){
            return new ImportProcess();
        }
        else if(commandName.equals("export")){
            return new ExportProcess();
        }
        else if(commandName.equals("database")){
            return new DatabaseProcess();
        }
        else if(commandName.equals("scoring")){
            return new ScoringProcess();
        }
        else if(commandName.equals("bscoring")){
            return new BasicScoringProcess();
        }
        else if(commandName.equals("subcase")){
            return new SubsetCasesProcess();
        }
        else if(commandName.equals("subvar")){
            return new SubsetVariablesProcess();
        }
        else if(commandName.equals("delvar")){
            return new DeleteVariableProcess();
        }
        else if(commandName.equals("renamevar")){
            return new RenameVariableProcess();
        }


        //graph processes
        else if(commandName.equals("barchart")){
            return new BarChartProcess();
        }
        else if(commandName.equals("piechart")){
            return new PieChartProcess();
        }
        else if(commandName.equals("histogram")){
            return new HistogramProcess();
        }
        else if(commandName.equals("density")){
            return new DensityProcess();
        }
        else if(commandName.equals("irtplot")){
            return new IrtPlotProcess();
        }
        else if(commandName.equals("itemmap")){
            return new ItemMapProcess();
        }
        else if(commandName.equals("line")){
            return new LineChartProcess();
        }
        else if(commandName.equals("scatter")){
            return new ScatterplotProcess();
        }
        else if(commandName.equals("nicc")){
            return new NonparametricCurveProcess();
        }

        //statistics processes
        else if(commandName.equals("freq")){
            return new FrequencyProcess();
        }
        else if(commandName.equals("descrip")){
            return new DescriptiveProcess();
        }
        else if(commandName.equals("corr")){
            return new CorrelationProcess();
        }
        else if(commandName.equals("rank")){
            return new RankingProcess();
        }
        else if(commandName.equals("scale")){
            return new TestScalingProcess();
        }
        else if(commandName.equals("lintran")){
            return new LinearTransformationProcess();
        }
        else if(commandName.equals("irtlink")){
            return new IrtLinkingProcess();
        }
        else if(commandName.equals("irteq")){
            return new IrtEquatingProcess();
        }
        else if(commandName.equals("item")){
            return new ItemAnalysisProcess();
        }
        else if(commandName.equals("cmh")){
            return new CmhProcess();
        }
        else if(commandName.equals("rasch")){
            return new RaschAnalysisProcess();
        }
        else if(commandName.equals("irtscoring")){
            return new IrtPersonScoringProcess();
        }
        else if(commandName.equals("irt")){
            return new IrtItemCalibrationProcess();
        }

        return null;
    }
    
}

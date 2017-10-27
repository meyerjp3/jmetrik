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

package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.graph.barchart.BarChartCommand;
import com.itemanalysis.jmetrik.graph.density.DensityCommand;
import com.itemanalysis.jmetrik.graph.histogram.HistogramCommand;
import com.itemanalysis.jmetrik.graph.irt.IrtPlotCommand;
import com.itemanalysis.jmetrik.graph.itemmap.ItemMapCommand;
import com.itemanalysis.jmetrik.graph.linechart.LineChartCommand;
import com.itemanalysis.jmetrik.graph.nicc.NonparametricCurveCommand;
import com.itemanalysis.jmetrik.graph.piechart.PieChartCommand;
import com.itemanalysis.jmetrik.graph.scatterplot.ScatterplotCommand;
import com.itemanalysis.jmetrik.scoring.BasicScoringCommand;
import com.itemanalysis.jmetrik.scoring.ScoringCommand;
import com.itemanalysis.jmetrik.stats.cmh.CmhCommand;
import com.itemanalysis.jmetrik.stats.correlation.CorrelationCommand;
import com.itemanalysis.jmetrik.stats.descriptives.DescriptiveCommand;
import com.itemanalysis.jmetrik.stats.frequency.FrequencyCommand;
import com.itemanalysis.jmetrik.stats.irt.equating.IrtEquatingCommand;
import com.itemanalysis.jmetrik.stats.irt.estimation.IrtItemCalibrationCommand;
import com.itemanalysis.jmetrik.stats.irt.estimation.IrtPersonScoringCommand;
import com.itemanalysis.jmetrik.stats.irt.linking.IrtLinkingCommand;
import com.itemanalysis.jmetrik.stats.irt.rasch.RaschCommand;
import com.itemanalysis.jmetrik.stats.itemanalysis.ItemAnalysisCommand;
import com.itemanalysis.jmetrik.stats.ranking.RankingCommand;
import com.itemanalysis.jmetrik.stats.scaling.TestScalingCommand;
import com.itemanalysis.jmetrik.stats.transformation.LinearTransformationCommand;

/**
 * This class is for processing commands from text. The text is first
 * parsed by com.itemanalysis.jmetrik.commandbuilder.TextToCommand and
 * the parsed text is used in this class to obtain a Command object.
 * The command object is then executed in Workspace.
 *
 */
public class JmetrikCommandFactory {

    public JmetrikCommandFactory(){

    }

    public Command getCommand(String commandName, String commandSyntax){

        if(commandName.equals("freq")){
            FrequencyCommand command = new FrequencyCommand();
            command.split(commandSyntax);
            return command;
        }
        //database and table procceses
        if(commandName.equals("import")){
            ImportCommand command = new ImportCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("export")){
            ExportCommand command = new ExportCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("database")){
            DatabaseCommand command = new DatabaseCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("scoring")){
            ScoringCommand command = new ScoringCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("bscoring")){
            BasicScoringCommand command = new BasicScoringCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("subcase")){
            SubsetCasesCommand command = new SubsetCasesCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("subvar")){
            SubsetVariableCommand command = new SubsetVariableCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("delvar")){
            DeleteVariableCommand command = new DeleteVariableCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("renamevar")){
            RenameVariableCommand command = new RenameVariableCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("vargroup")){
            VariableGroupCommand command = new VariableGroupCommand();
            command.split(commandSyntax);
            return command;
        }
        if(commandName.equals("importspss")){
            ImportSPSSCommand command = new ImportSPSSCommand();
            command.split(commandSyntax);
            return command;
        }

        //graph processes
        else if(commandName.equals("barchart")){
            BarChartCommand command = new BarChartCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("piechart")){
            PieChartCommand command = new PieChartCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("histogram")){
            HistogramCommand command = new HistogramCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("density")){
            DensityCommand command = new DensityCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("irtplot")){
            IrtPlotCommand command = new IrtPlotCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("itemmap")){
            ItemMapCommand command = new ItemMapCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("line")){
            LineChartCommand command = new LineChartCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("scatter")){
            ScatterplotCommand command = new ScatterplotCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("nicc")){
            NonparametricCurveCommand command = new NonparametricCurveCommand();
            command.split(commandSyntax);
            return command;
        }

        //statistics processes
        else if(commandName.equals("freq")){
            FrequencyCommand command = new FrequencyCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("descrip")){
            DescriptiveCommand command = new DescriptiveCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("corr")){
            CorrelationCommand command = new CorrelationCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("rank")){
            RankingCommand command = new RankingCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("scale")){
            TestScalingCommand command = new TestScalingCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("lintran")){
            LinearTransformationCommand command = new LinearTransformationCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("irtlink")){
            IrtLinkingCommand command = new IrtLinkingCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("irteq")){
            IrtEquatingCommand command = new IrtEquatingCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("item")){
            ItemAnalysisCommand command = new ItemAnalysisCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("cmh")){
            CmhCommand command = new CmhCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("rasch")){
            RaschCommand command = new RaschCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("irtscoring")){
            IrtPersonScoringCommand command = new IrtPersonScoringCommand();
            command.split(commandSyntax);
            return command;
        }
        else if(commandName.equals("irt")){
            IrtItemCalibrationCommand command = new IrtItemCalibrationCommand();
            command.split(commandSyntax);
            return command;
        }

        return null;
    }

}

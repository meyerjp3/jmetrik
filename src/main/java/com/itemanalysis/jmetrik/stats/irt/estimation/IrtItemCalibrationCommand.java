package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.commandbuilder.MegaCommand;
import com.itemanalysis.jmetrik.commandbuilder.MegaOption;
import com.itemanalysis.jmetrik.commandbuilder.OptionType;
import com.itemanalysis.jmetrik.commandbuilder.SelectFromListValueChecker;

public class IrtItemCalibrationCommand extends MegaCommand {

    private int maxGroups  = 100;//Number of item groups. Is 100 enough?

    public IrtItemCalibrationCommand(){
        super("irt", "Item response theory item calibration");
        initialize();
    }

    private void initialize(){
        MegaOption data = new MegaOption("data", "Data information", OptionType.ARGUMENT_VALUE_OPTION_LIST, true);
        data.addArgument("db", "Database name", true);
        data.addArgument("table", "Database table name", true);
        this.addOption(data);

        MegaOption converge = new MegaOption("converge", "EM algorithm convergence criteria", OptionType.ARGUMENT_VALUE_OPTION_LIST);
        converge.addArgument("maxiter", "Maximum number of EM cycles", false);
        converge.addArgument("tol", "Convergence criterion 0 <= tol < 1", false);
        this.addOption(converge);

        MegaOption optimizer = new MegaOption("optim", "Name of optimizer to use in M-step", OptionType.SELECT_ONE_OPTION);
        SelectFromListValueChecker listChecker = new SelectFromListValueChecker();
        listChecker.addPermittedValue("uncmin");
        listChecker.addPermittedValue("bfgs");
        optimizer.setValueChecker(listChecker);
        this.addOption(optimizer);

        MegaOption latent = new MegaOption("latent", "Latent distribution specification", OptionType.ARGUMENT_VALUE_OPTION_LIST);
        latent.addArgument("name", "Name of the distribution. Possible values are \"normal\" for normal distribution or \"GH\" for Gauss-Hermite", false);
        latent.addArgument("min", "Minimum value", false);
        latent.addArgument("max", "Maximum value", false);
        latent.addArgument("points", "Number of quadrature points", false);
        this.addOption(latent);

        MegaOption output = new MegaOption("itemout", "Name of item parameter estimate output table", OptionType.ARGUMENT_VALUE_OPTION_LIST);
        output.addArgument("db", "Database name", false);
        output.addArgument("table", "Database table name", false);
        this.addOption(output);

        MegaOption missing = new MegaOption("missing", "Treatment of missing data", OptionType.SELECT_ONE_OPTION);
        listChecker = new SelectFromListValueChecker();
        listChecker.addPermittedValue("ignore");
        listChecker.addPermittedValue("zero");
        missing.setValueChecker(listChecker);
        this.addOption(missing);

        MegaOption groupVariable = new MegaOption("groupvar", "An examinee grouping variable such as gender", OptionType.FREE_OPTION);
        this.addOption(groupVariable);

        MegaOption numberOfGroups = new MegaOption("groups", "Number of item groups", OptionType.FREE_OPTION);
        this.addOption(numberOfGroups);

        MegaOption group = null;
        for(int i=0;i<maxGroups;i++){
            group = new MegaOption("group"+(i+1), "Item information for group " + (i+1), OptionType.ARGUMENT_VALUE_OPTION_LIST, i==0);
            group.addArgument("variables", "Items that belong to this group", true);

            group.addArgument("model", "Item response model for this group", true);
            group.addArgument("scale", "Scaling constant, either 1.0 or 1.7", false);
            group.addArgument("ncat", "Number of categories for items in this group", true);
            group.addArgument("start", "Starting values e.g. (par1, par2, par3. The order is very important. " +
                    "3PL order is aparam, bparam, cparam. 4PL order is aparam, bparam, cparam, uparam. " +
                    "GPCM order is step1, step2, ...", false);

            //Prior information
            group.addArgument("aprior", "Discrimination prior specification e.g. (name, par1, par2). " +
                    "Possible names are beta, normal, and lognormal. The parameter order is important. " +
                    "beta order is shape1, shape2, lower bound, upper bound. normal order is mean, sd. " +
                    "lognormal order is logmean, logsd.", false);
            group.addArgument("bprior", "Difficulty prior specification e.g. (name, par1, par2)"+
                    "Possible names are beta, normal, and lognormal. The parameter order is important." +
                    "beta order is shape1, shape2, lower bound, upper bound." +
                    "normal order is mean, sd." +
                    "lognormal order is logmean, logsd.", false);
            group.addArgument("cprior", "Lower asymptote prior specification e.g. (name, par1, par2)"+
                    "Possible names are beta, normal, and lognormal. The parameter order is important." +
                    "beta order is shape1, shape2, lower bound, upper bound." +
                    "normal order is mean, sd." +
                    "lognormal order is logmean, logsd.", false);
            group.addArgument("uprior", "Upper asymptote prior specification e.g. (name, par1, par2)"+
                    "Possible names are beta, normal, and lognormal. The parameter order is important." +
                    "beta order is shape1, shape2, lower bound, upper bound." +
                    "normal order is mean, sd." +
                    "lognormal order is logmean, logsd.", false);
            group.addArgument("stprior", "Step/Threshold prior specification e.g. (name, par1, par2)"+
                    "Possible names are beta, normal, and lognormal. The parameter order is important." +
                    "beta order is shape1, shape2, lower bound, upper bound." +
                    "normal order is mean, sd." +
                    "lognormal order is logmean, logsd.", false);

            //fixed value flags
            group.addArgument("fixed", "List of parameters that are fixed at starting values", false);

            this.addOption(group);

        }
    }

}

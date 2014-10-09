package com.itemanalysis.jmetrik.commandbuilder;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class MegaCommandTest {

    @Test
    public void megaCommandSplitTest1(){
        MegaCommand command = new MegaCommand("descriptives");

        MegaOption option = new MegaOption("display", "Show minimum value", OptionType.SELECT_ALL_OPTION);
        SelectFromListValueChecker checker = new SelectFromListValueChecker();
        checker.addPermittedValue("min");
        checker.addPermittedValue("max");
        checker.addPermittedValue("mean");
        checker.addPermittedValue("sd");
        option.setValueChecker(checker);
        command.addOption(option);

        option = new MegaOption("variables", "Variables to be analyzed", OptionType.FREE_LIST_OPTION);
        command.addOption(option);

        option = new MegaOption("data", "Data table", OptionType.ARGUMENT_VALUE_OPTION_LIST);
        option.addArgument("db", "Name of database", true);
        option.addArgument("table", "Name of database table", true);
        command.addOption(option);

        System.out.println("BEFORE SPLITTING TEXT");
        System.out.println(command.paste());
        System.out.println();

        String text = "descriptives{\n" +
                "display(min, mean);\n" +
                "variables(item1, item2, item3);\n" +
                "data(db=mydb, table = mytable);\n" +
                "}";

        command.split(text);

        System.out.println("AFTER SPLITTING TEXT");
        System.out.println(command.paste());

        MegaOption temp = command.getOption("display");
        assertTrue(temp.containsValue("min"));
        assertTrue(temp.containsValue("mean"));
        assertFalse(temp.containsValue("max"));
        assertFalse(temp.containsValue("sd"));

        temp = command.getOption("data");
        assertTrue(temp.getValueAt("db", "default").equals("mydb"));
        assertTrue(temp.getValueAt("table", "default").equals("mytable"));

        temp = command.getOption("variables");
        String[] defaultValues = {"d1", "d2", "d3"};
        String[] s = temp.getValues(defaultValues);
        assertTrue(s[0].equals("item1"));
        assertTrue(s[1].equals("item2"));
        assertTrue(s[2].equals("item3"));


    }

    @Test
    public void megaCommandRepeatedOptionTest(){

        MegaCommand command = new MegaCommand("scoring");

        MegaOption option = new MegaOption("key1", "Variable names", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("variables", "Name of variables to be scored", true);
        option.addArgument("options", "Response option values", true);
        option.addArgument("scores", "Response option scores", true);

        option.addValueAt("variables", "var1");
        option.addValueAt("variables", "var2");
        option.addValueAt("variables", "var3");
        option.addValueAt("variables", "var4");

        option.addValueAt("options", "A");
        option.addValueAt("options", "B");
        option.addValueAt("options", "C");
        option.addValueAt("options", "D");

        option.addValueAt("scores", "0");
        option.addValueAt("scores", "1");
        option.addValueAt("scores", "0");
        option.addValueAt("scores", "0");

        command.addOption(option);


        option = new MegaOption("key2", "Variable names", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("variables", "Name of variables to be scored", true);
        option.addArgument("options", "Response option values", true);
        option.addArgument("scores", "Response option scores", true);

        option.addValueAt("variables", "var6");
        option.addValueAt("variables", "var7");
        option.addValueAt("variables", "var8");
        option.addValueAt("variables", "var9");

        option.addValueAt("options", "A");
        option.addValueAt("options", "B");
        option.addValueAt("options", "C");
        option.addValueAt("options", "D");

        option.addValueAt("scores", "1");
        option.addValueAt("scores", "0");
        option.addValueAt("scores", "0");
        option.addValueAt("scores", "0");

        command.addOption(option);


        option = new MegaOption("key3", "Variable names", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("variables", "Name of variables to be scored", true);
        option.addArgument("options", "Response option values", true);
        option.addArgument("scores", "Response option scores", true);

        option.addValueAt("variables", "var10");
        option.addValueAt("variables", "var11");
        option.addValueAt("variables", "var12");
        option.addValueAt("variables", "var13");

        option.addValueAt("options", "A");
        option.addValueAt("options", "B");
        option.addValueAt("options", "C");
        option.addValueAt("options", "D");

        option.addValueAt("scores", "0");
        option.addValueAt("scores", "0");
        option.addValueAt("scores", "1");
        option.addValueAt("scores", "1");

        command.addOption(option);

        System.out.println(command.paste());
        System.out.println();
        System.out.println(command.getHelpText());

        String[] defaultValues = {"1","1","1","1"};

        MegaOption mo = command.getOption("key1");
        String[] v = mo.getValuesAt("options", defaultValues);
        assertTrue(v[0].equals("A"));
        assertTrue(v[1].equals("B"));
        assertTrue(v[2].equals("C"));
        assertTrue(v[3].equals("D"));
        assertFalse(v[3].equals("A"));

    }

    @Test
    public void megaCommandRepeatedOptionSplitTest(){

        MegaCommand command = new MegaCommand("scoring",
                "This is an example of a scoring command. It has multiple keys but each one has a unique name." +
                        "The objects must be added so that the text can be parsed correctly. This description" +
                        "is written to be very long so that I can check the output on teh help text.");

        MegaOption option = new MegaOption("key1", "Variable names", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("variables", "Name of variables to be scored", true);
        option.addArgument("options", "Response option values", true);
        option.addArgument("scores", "Response option scores", true);
        command.addOption(option);

        option = new MegaOption("key2", "Variable names", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("variables", "Name of variables to be scored", true);
        option.addArgument("options", "Response option values", true);
        option.addArgument("scores", "Response option scores", true);
        command.addOption(option);

        option = new MegaOption("key3", "Variable names", OptionType.ARGUMENT_VALUE_OPTION_LIST, false);
        option.addArgument("variables", "Name of variables to be scored", true);
        option.addArgument("options", "Response option values", true);
        option.addArgument("scores", "Response option scores", true);
        command.addOption(option);

        String text = "scoring{\n" +
                "  key1(options=(A, B, C, D), scores=(1,0,0,0), variables=(i1, i2, i3) );\n" +
                "  key2(options=(A, B, C, D), scores=(0,1,0,0), variables=(i4, i5, i6) );\n" +
                "  key3(options=(A, B, C, D), scores=(0,0,1,0), variables=(i7, i8, i9, i10) );\n" +
                "}";

        System.out.println(command.getHelpText());
        System.out.println();
        System.out.println();

        System.out.println("BEFORE SPLIT:");
        System.out.println(command.paste());
        System.out.println();

        command.split(text);

        System.out.println("AFTER SPLIT:");
        System.out.println(command.paste());
        System.out.println();
        System.out.println(command.getHelpText());

        String[] defaultValues = {"1","1","1","1"};

        MegaOption mo = command.getOption("key1");
        String[] v = mo.getValuesAt("options", defaultValues);
        assertTrue(v[0].equals("A"));
        assertTrue(v[1].equals("B"));
        assertTrue(v[2].equals("C"));
        assertTrue(v[3].equals("D"));
        assertFalse(v[3].equals("A"));

    }

    public MegaCommand getIrtCommand(int maxGroups){
        MegaCommand cmd = new MegaCommand("irt", "IRT item calibration");

        MegaOption data = new MegaOption("data", "Data informaiton", OptionType.ARGUMENT_VALUE_OPTION_LIST, true);
        data.addArgument("db", "Database name", true);
        data.addArgument("table", "Database table name", true);
        cmd.addOption(data);

        MegaOption converge = new MegaOption("converge", "EM algorithm convergence criteria", OptionType.ARGUMENT_VALUE_OPTION_LIST);
        converge.addArgument("maxiter", "Maximum number of EM cycles", false);
        converge.addArgument("tol", "Convergence criterion 0 <= tol < 1", false);
        cmd.addOption(converge);

        MegaOption numberOfGroups = new MegaOption("groups", "Number of item groups", OptionType.FREE_OPTION);
        cmd.addOption(numberOfGroups);

        MegaOption group = null;
        for(int i=0;i<maxGroups;i++){
            group = new MegaOption("group"+(i+1), "Item information for group " + (i+1), OptionType.ARGUMENT_VALUE_OPTION_LIST, i==0);
            group.addArgument("variables", "Items that belong to this group", true);

            group.addArgument("model", "Item response model for this group", true);
            group.addArgument("ncat", "Number of categories for items in this group", true);
            group.addArgument("start", "Starting values e.g. (par1, par2, par3. The order is very important. " +
                    "3PL order is aparam, bparam, cparam. 4PL order is aparam, bparam, cparam, uparam. " +
                    "GPCM order is step1, step2, ...", false);

            //Prior information
            group.addArgument("aprior", "Discrimination prior specification e.g. (name, par1, par2). " +
                    "Possible names are beta, normal, and lognormal. The parameter order is important. " +
                    "beta order is shape1, shape2, lower bound, upper bound. normal order is mean, sd. " +
                    "lognormal order is logmean, logsd.", false);
            group.addArgument("bprior", "Difficulty/step prior specification e.g. (name, par1, par2)"+
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

            //fixed value flags
            group.addArgument("afixed", "Do not estimate discrimination parameter. Fix it to start value", false);
            group.addArgument("bfixed", "Do not estimate difficulty/step parameter. Fix it to start value", false);
            group.addArgument("cfixed", "Do not estimate lower asymptote parameter. Fix it to start value", false);
            group.addArgument("ufixed", "Do not estimate upper asymptote parameter. Fix it to start value", false);

            cmd.addOption(group);

        }
        return cmd;
    }

    @Test
    public void irtCommandTest(){

        MegaCommand cmd = getIrtCommand(1);
        System.out.println(cmd.paste());
        System.out.println();
        System.out.println(cmd.getHelpText());

    }

    @Test
    public void IrtParseTest1(){
        String commandText = "irt{\n" +
                "  data(db = mydb, table = exam1);\n" +
                "  group1(bprior = (normal, 0, 1), aprior = (beta, 1, 2, 3, 4), model = 3PL, start = (1.0,0.0,0.05), ncat = 2, variables = (item1, item2, item3));\n" +
                "  group2(bprior = (normal, 0, 1), aprior = (beta, 1, 2, 3, 4), model = GPCM, ncat = 4, variables = (item5, item6, item7));\n" +
                "  converge(maxiter = 1000, tol = 0.0001);\n" +
                "  groups(2);\n" +
                "}";

        MegaCommand cmd = getIrtCommand(2);
        cmd.split(commandText);
        System.out.println(cmd.paste());

        String[] dv = {"1"};
        String[] test = cmd.getOption("group1").getValuesAt("aprior", dv);
        System.out.println("APRIOR: " + Arrays.toString(test));

        test = cmd.getOption("group1").getValuesAt("start", dv);
        System.out.println("START: " + Arrays.toString(test));

        System.out.println("GROUP1-MODEL: " + cmd.getOption("group1").getValueAt("model", "dv"));
        System.out.println("GROUP2-MODEL: " + cmd.getOption("group2").getValueAt("model", "dv"));




    }


}
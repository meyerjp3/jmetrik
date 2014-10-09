/**
 * Copyright 2014 J. Patrick Meyer
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.itemanalysis.jmetrik.commandbuilder;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;

/**
 * A flexible class for defining an option and storing values. It can take named arguments of not.
 * The option value may be an individual element or a list of values. This class can handle options
 * in various forms such as the following:
 *
 * 1. myOptionName(someValue)
 * 2. myOptionName(someValue1, someValue2)
 * 3. myOptionName(argName = argValue)
 * 4. myOptionName(argName1 = argValue1, argName2 = argValue2 )
 * 5. myOptionName(argName1 = (argValue11, argValue12, argValue13), argName2 = (argValue21, argValue22) )
 *
 * When combined with {@link ArgumentValueChecker} the values can be validated. If validation fails, no
 * value is added.
 *
 * All values are stored as Strings, but there are methods that will convert the value to double or int
 * before returning them.
 *
 */
public class MegaOption {

    /**
     * A unique option name
     */
    private String optionName = "";

    /**
     * An option description for help files.
     */
    private String optionDescription = "";

    /**
     * A flag that indicates whether the option has named arguments. If false, the option is a list of values.
     */
    private boolean argumentValuePair = false;

    /**
     * A flag that indicates whether the option is required or not.
     */
    private boolean required = false;

    /**
     * A flag to indicate whether an argument may have multiple values
     */
    private boolean allowMultiple = false;

    /**
     * Arguments are not required. All possible arguments are included in this list.
     */
    private HashSet<String> argument = null;

    /**
     * An argument may have a description. Descriptions are stored here.
     */
    private HashMap<String, String> argumentDescription = null;

    private HashMap<String, Boolean> argumentRequired = null;

    /**
     * An argument may have a single value or a list of values. Values are stored here.
     */
    private HashMap<String, ArrayList<String>> argumentValue = null;

    /**
     * A value checker
     */
    private ArgumentValueChecker valueChecker = null;

    /**
     * A static name for the no argument case.
     */
    private static String NULL_ARGUMENT = "NOARG";

    public MegaOption(String optionName, String optionDescription, OptionType type){
        this(optionName, optionDescription, type, false);
    }

    public MegaOption(String optionName, OptionType type){
        this(optionName, "", type, false);
    }

    /**
     * This constructor is preferred as several of the options are predefined according to the type of option.
     * Two types of options require additional configuration. For {@link OptionType#SELECT_ALL_OPTION} and
     * {@link OptionType#SELECT_ONE_OPTION} you must also add a {@link SelectFromListValueChecker} object
     * using {@link this#setValueChecker(ArgumentValueChecker)}. Those value checker will ensure that
     * the value is obtained from a predefined list of possible values.
     *
     * @param optionName name of option that is unique to a command.
     * @param optionDescription a short description of the option for help text.
     * @param type the type of option.
     */
    public MegaOption(String optionName, String optionDescription, OptionType type, boolean required){
        this.optionName = optionName;
        this.optionDescription = optionDescription;
        this.required = required;
        this.argumentRequired = new HashMap<String, Boolean>();

        if(type == OptionType.FREE_LIST_OPTION){
            this.argumentValuePair = false;
            this.allowMultiple = true;
        }else if(type == OptionType.ARGUMENT_VALUE_OPTION_LIST){
            this.argumentValuePair = true;
            this.allowMultiple = true;
        }else if(type == OptionType.SELECT_ALL_OPTION){
            //For a SelectAll option to be complete, you must also
            //use the SelectFromListValueChecker to define the list
            //of options from which to choose.
            this.argumentValuePair = false;
            this.allowMultiple = true;
        }else{
            //This could either be a FreeOption or a SelectOne option.
            //They are stored in the same way. The difference is that
            //a SelectOne option will use the SelectFromListValueChecker to
            // define the list of options from which to choose. A FReeOption
            //does not use a SelectFromListValueChecker.
            this.argumentValuePair = false;
            this.allowMultiple = false;
        }

        initialize();
    }

    private void initialize(){
        argumentValue = new HashMap<String, ArrayList<String>>();
        argumentDescription = new HashMap<String, String>();
        if(argumentValuePair){
            argument = new HashSet<String>();
        }else{
            argumentValue.put(NULL_ARGUMENT, new ArrayList<String>());
        }
    }

    public void isRequired(boolean required){
        this.required = required;
    }

    /**
     * Check the values using an instance of {@link ArgumentValueChecker}. You can define your own
     * value checker or use one of the predefined class. {@link SelectFromListValueChecker} is designed
     * for options that have values that must be selected from a predefined list. {@link NumericValueChecker}
     * is designed for checking that String can be parsed into a double and optionally that the numeric
     * value is within a ranges such as [min <= value <= max].
     *
     * @param checker
     */
    public void setValueChecker(ArgumentValueChecker checker){
        this.valueChecker = checker;
    }

    public String getOptionName(){
        return optionName;
    }

    public boolean isRequired(){
        return required;
    }

    public boolean hasArguments(){
        return argumentValuePair;
    }

    public boolean includesArgument(String argName){
        return argument.contains(argName);
    }

    public HashSet<String> getArguments(){
        return argument;
    }

    /**
     * Adds an argument with no description. The description will default to an empty String.
     *
     * @param argName an argument name that is unique to the option.
     */
    public void addArgument(String argName){
        this.addArgument(argName, "", false);
    }

    /**
     * If {@link #required} is true then you must add arguments with this method to define them.
     * This method is called during the definition stage.
     *
     * @param argName an argument name unique to the option that is 50 characters or less . If a name is
     *                repeated only the first one is kept in the list
     * @param description a description of the argument that is 2000 characters or less for help text.
     * @throws IllegalArgumentException
     */
    public void addArgument(String argName, String description, boolean required)throws IllegalArgumentException{
        String name = "";
        if(argumentValuePair){
            name = argName.substring(0, Math.min(50, argName.length()));//Names must be 50 characters or less
            boolean added = argument.add(name);
            if(added){
                int length = description.length();
                this.argumentDescription.put(name, description.substring(0, Math.min(length, 2000)));
                this.argumentRequired.put(name, required);
                ArrayList<String> av = argumentValue.get(name);
                if(av==null){
                    argumentValue.put(name, new ArrayList<String>());
                }
            }

        }else{
            throw new IllegalArgumentException(optionName + " does not take named arguments.");
        }
    }

    /**
     * Adds an argument value to this option. This is usually called from {@link MegaOptionParser} when
     * text is split into option values.
     *
     * @param argName name of argument to which the value belongs.
     * @param argValue value for the argument.
     * @throws IllegalArgumentException
     */
    public void addValueAt(String argName, String argValue)throws IllegalArgumentException{
        if(valueChecker !=null){
            if(!valueChecker.checkValueAt(argName, argValue)){
                throw new IllegalArgumentException("<" + argValue + "> is an invalid value for option [" + optionName + "].");
            }
        }

        if(argumentValuePair){
            if(allowMultiple){
                argumentValue.get(argName).add(argValue);
            }else{
                argumentValue.get(argName).add(0, argValue);
            }

        }else{
            throw new IllegalArgumentException(optionName + " does not take named arguments.");
        }
    }

    /**
     * Adds a value to a unnamed list. This is usually called from {@link MegaOptionParser} when
     * text is split into option values.
     *
     * @param value a value for the option.
     * @throws IllegalArgumentException
     */
    public void addValue(String value)throws IllegalArgumentException{
        if(valueChecker !=null){
            if(!valueChecker.checkValue(value)){
                throw new IllegalArgumentException("<" + value + "> is an invalid value for the option [" + optionName + "].");
            }
        }

        if(argumentValuePair){
            throw new IllegalArgumentException(optionName + " requires named argument value pairs.");
        }else{
            if(allowMultiple){
                argumentValue.get(NULL_ARGUMENT).add(value);
            }else{
                ArrayList<String> argumentValueList = argumentValue.get(NULL_ARGUMENT);
                if(!argumentValueList.isEmpty()) argumentValueList.remove(0);
                argumentValueList.add(value);
            }

        }
    }

    /**
     * This method provides a way to check if a value is included in a list. It is designed for
     * options of type OptionType.SELECT_ALL_OPTION or OptionType.SELECT_ONE_OPTION because
     * only selected values wil be included in the list.
     *
     * @param value value that might be included in the list
     * @return true if value is included, false otherwise
     */
    public boolean containsValue(String value){
        ArrayList<String> valueList = argumentValue.get(NULL_ARGUMENT);
        return valueList.contains(value.trim());
    }

    /**
     * This method provides a way to check if a value is included in a list. It is designed for
     * options of type OptionType.SELECT_ALL_OPTION or OptionType.SELECT_ONE_OPTION because
     * only selected values wil be included in the list.
     *
     * @param argument argument of interest
     * @param value value that might be included in the list
     * @return true if value is included, false otherwise
     */
    public boolean containsValueAt(String argument, String value){
        ArrayList<String> valueList = argumentValue.get(argument);
        return valueList.contains(value.trim());
    }

    public int getNumberOfValues(){
        return argumentValue.get(NULL_ARGUMENT).size();
    }

    public int getNumberOfValuesAt(String argument){
        return argumentValue.get(argument).size();
    }

    /**
     * Gets the first value in the list and returns it. If the list is empty it will
     * return the default value.
     *
     * List values and default values will be checked. If they do not pass the test
     * an exception will be thrown.
     *
     * @return value of the option.
     */
    public String getValue(String defaultValue)throws IllegalArgumentException{
        //check the default value
        if(valueChecker !=null){
            if(!valueChecker.checkValue(defaultValue)){
                throw new IllegalArgumentException("<" + defaultValue + "> is an invalid value for option [" + optionName + "].");
            }
        }

        if(argumentValuePair){
            throw new IllegalArgumentException(optionName + " requires named argument value pairs.");
        }else{
            if(argumentValue.isEmpty())return defaultValue;
            return argumentValue.get(NULL_ARGUMENT).get(0);
        }
    }

    /**
     * Converts value to a double before returning it.
     *
     * @param defaultValue
     * @return
     * @throws IllegalArgumentException
     */
    public double getValueAsDouble(double defaultValue)throws IllegalArgumentException{
        String value = getValue(Double.valueOf(defaultValue).toString());
        return Double.parseDouble(value);
    }

    /**
     * Converts value to an int before returning it.
     * @param defaultValue
     * @return
     * @throws IllegalArgumentException
     */
    public double getValueAsInteger(int defaultValue)throws IllegalArgumentException{
        String value = getValue(Integer.valueOf(defaultValue).toString());
        return Integer.parseInt(value);
    }

    /**
     * Gets the first value in the list for an argument and returns it.
     *
     * @param argName name of argument for which the value is sought.
     * @return default value for the argument. If argument is required but not value is found an
     * IllegalArgumentException will be thrown. The default value is ignored for required arguments.
     */
    public String getValueAt(String argName, String defaultValue)throws IllegalArgumentException{

        //check the default value
        if(valueChecker !=null){
            if(!valueChecker.checkValueAt(argName, defaultValue)){
                throw new IllegalArgumentException("<" + defaultValue + "> is an invalid value for option [" + optionName + "].");
            }
        }

        if(argumentValuePair){
            if(argument.contains(argName) && argumentValue.containsKey(argName)){
                String value = argumentValue.get(argName).get(0);
                return value;
            }else{
                if(argumentRequired.get(argName)==Boolean.TRUE) throw new IllegalArgumentException("Value not found for required argument: " + argName);
                return defaultValue;
            }
        }else{
            throw new IllegalArgumentException(optionName + " does not take named arguments.");
        }
    }

    public double getValueAtAsDouble(String argName, double defaultValue)throws IllegalArgumentException{
        String value = getValueAt(argName, Double.valueOf(defaultValue).toString());
        return Double.parseDouble(value);
    }

    public double getValueAtAsInteger(String argName, double defaultValue)throws IllegalArgumentException{
        String value = getValueAt(argName, Double.valueOf(defaultValue).toString());
        return Integer.parseInt(value);
    }

    /**
     * Gets all the values in the list for this option.
     *
     * @param defaultValues if no values exist in the list, the default values will be returned.
     * @return a list values as an array of strings.
     * @throws IllegalArgumentException
     */
    public String[] getValues(String[] defaultValues)throws IllegalArgumentException{
        //check the default values
        if(valueChecker !=null){
            for(String dflt : defaultValues){
                if(!valueChecker.checkValue(dflt)){
                    throw new IllegalArgumentException("<" + dflt + "> is an invalid value for option [" + optionName + "].");
                }
            }
        }

        if(argumentValuePair){
            throw new IllegalArgumentException(optionName + " requires named argument value pairs.");
        }else{
            ArrayList<String> valueList = argumentValue.get(NULL_ARGUMENT);
            if(valueList.isEmpty()) return defaultValues;
            String[] values = new String[valueList.size()];
            for(int i=0;i<valueList.size();i++){
                values[i] = valueList.get(i);
            }
            return values;
        }
    }

    public double[] getValuesAsDouble(double[] defaultValues)throws IllegalArgumentException{
        int n = defaultValues.length;
        String[] dv = new String[n];
        for(int i=0;i<n;i++){
            dv[i] = Double.valueOf(defaultValues[i]).toString();
        }

        String[] s = getValues(dv);

        double[] d = new double[n];
        for(int i=0;i<n;i++){
            d[i] = Double.parseDouble(s[i]);
        }
        return d;
    }

    public int[] getValuesAsInteger(int[] defaultValues)throws IllegalArgumentException{
        int n = defaultValues.length;
        String[] dv = new String[n];
        for(int i=0;i<n;i++){
            dv[i] = Integer.valueOf(defaultValues[i]).toString();
        }

        String[] s = getValues(dv);

        int[] d = new int[n];
        for(int i=0;i<n;i++){
            d[i] = Integer.parseInt(s[i]);
        }
        return d;
    }

    /**
     * Gets all the values in a list ofr a particular argument.
     *
     * @param argName name of argument
     * @param defaultValues if no values exist in the list, the default values will be returned.
     * @return a list values for the argument as an array of strings.
     * @throws IllegalArgumentException
     */
    public String[] getValuesAt(String argName, String[] defaultValues)throws IllegalArgumentException{
        //check the default values
        if(valueChecker !=null){
            for(String dflt : defaultValues){
                if(!valueChecker.checkValue(dflt)){
                    throw new IllegalArgumentException("<" + dflt + "> is an invalid value for option [" + optionName + "].");
                }
            }
        }

        if(argumentValuePair){
            if(argument.contains(argName) && argumentValue.containsKey(argName)){
                ArrayList<String> valueList = argumentValue.get(argName);
                String[] values = new String[valueList.size()];
                for(int i=0;i<valueList.size();i++){
                    values[i] = valueList.get(i);
                }
                return values;
            }else{
                return defaultValues;
            }
        }else{
            throw new IllegalArgumentException(optionName + " does not use named argument value pairs.");
        }
    }

    public double[] getValuesAtAsDouble(String argName, double[] defaultValues)throws IllegalArgumentException{
        int n = defaultValues.length;
        String[] dv = new String[n];
        for(int i=0;i<n;i++){
            dv[i] = Double.valueOf(defaultValues[i]).toString();
        }

        String[] s = getValuesAt(argName, dv);

        double[] d = new double[n];
        for(int i=0;i<n;i++){
            d[i] = Double.parseDouble(s[i]);
        }
        return d;
    }

    public int[] getValuesAtAsInteger(String argName, int[] defaultValues)throws IllegalArgumentException{
        int n = defaultValues.length;
        String[] dv = new String[n];
        for(int i=0;i<n;i++){
            dv[i] = Integer.valueOf(defaultValues[i]).toString();
        }

        String[] s = getValuesAt(argName, dv);

        int[] d = new int[n];
        for(int i=0;i<n;i++){
            d[i] = Integer.parseInt(s[i]);
        }
        return d;
    }

    /**
     * Formatted output for the display of the option. A question mark is used for options or arguments
     * that have no current values. The option is displayed in a way that it can be split by
     * {@link MegaOptionParser}.
     *
     * @return a string representation of the option.
     */
    public String paste(){
        String output = getOptionName() + "(";
        if(argumentValuePair){
            for(String arg : argument){
//                output += arg + " = ";
                ArrayList<String> values = argumentValue.get(arg);

                if(!values.isEmpty()){
                    if(values.size()==1){
                        output += arg + " = " + values.get(0);
                    }else{
                        output += arg + " = (";
                        for(String s : values){
                            output += s + ",";
                        }
                        output = output.trim();
                        output = output.substring(0, output.length()-1);//remove last comma
                        output += ")";
                    }
                    output += ", ";
                }

//                if(values.isEmpty()){
////                    output += arg + " = ?";
//                }else
            }
        }else{
            ArrayList<String> values = argumentValue.get(NULL_ARGUMENT);
            if(values.isEmpty()){
                output += "?";
            }else if(values.size()==1){
                output += values.get(0);
            }else{
                for(String s : values){
                    output += s + ",";
                }
                output = output.trim();
                output = output.substring(0, output.length()-1);//remove last comma
            }
        }
        output = output.trim();
        if(output.endsWith(",")) output = output.substring(0,output.length()-1);
        output += ")";

        return output;
    }

    public String getHelpText(){
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb);

        f.format("%2s", "");
        f.format("%-20s", optionName.substring(0, Math.min(20, optionName.length())));

        //display parameters
        String output = "";
        if(required){
            output += "(Required, ";
        }else{
            output += "(Optional, ";
        }
        if(argumentValuePair){
            output += "Named arguments, ";
        }else{
            output += "Value only, ";
        }
        if(allowMultiple){
            output += "Multiple values allowed)";
        }else{
            output += "One value only)";
        }
        f.format("%-45s", output);
//        f.format("%n");

        if(!"".equals(optionDescription)){
            f.format("%1s", " ");
            formatDescription(optionDescription, f, "");
            f.format("%n");
        }else{
            f.format("%n");
        }

        //display arguments and description if included
        if(argumentValuePair){
            String text = "";
            for(String arg : argument){
                f.format("%3s", "");
                f.format("%-22s", "["+arg+"]");
                if(argumentRequired.get(arg)==Boolean.TRUE){
                    text = "(Required) ";
                }else{
                    text = "(Optional) ";
                }

                String descr = argumentDescription.get(arg);

                if(descr!=null || !"".equals(descr)){
                    formatDescription(descr, f, text);
                    f.format("%n");
                }else{
                    f.format("%n");
                }
            }
        }



        return f.toString();
    }

    private void formatDescription(String description, Formatter f, String text){
        String[] textArray = description.split("\\s+");//split on white space
        String line = text;
        int lineCount = 0;
        for(String s : textArray){
            if((line+s).trim().length()<=100){
                //concatenate text to line
                line += s + " ";
            }else{
                //print line
                if(lineCount>0) f.format("%25s", "");
                f.format("%-100s", line);
                f.format("%n");
                line = s + " ";
                lineCount++;
            }
        }
        //last line
        if(lineCount>0) f.format("%25s", "");
        f.format("%-100s", line);
    }

    @Override
    public String toString(){
        return optionName;
    }

    @Override
    public boolean equals(Object o){
        if(!(o instanceof MegaOption)) return false;
        if(o==this) return true;
        Option opt = (Option)o;
        if(opt.toString().equals(this.toString())) return true;
        return false;
    }

    @Override
    public int hashCode(){
        return optionName.hashCode();
    }

}

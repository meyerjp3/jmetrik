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

import com.itemanalysis.jmetrik.dao.DatabaseType;
import com.itemanalysis.jmetrik.swing.ChartStyle;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PaintUtilities;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * This class creates default application preferences and default log4j properties.
 * It creates the necessary files and folders if none exist.
 *
 * It also provides getter and setter methods for the application preferences.
 *
 * Note: loadLog() must be called once at application startup to start the logger.
 * Otherwise, the application will not find any log4j appenders.
 *
 */
public class JmetrikPreferencesManager {

    private static final String JMETRIK_PREFERENCES_NODE = "com/itemanalysis/jmetrik/prefs";

    //jmetrik output property names
    private static final String OUTPUT_PRECISION = "output_precision";

    //jmetrik output property defaults
    private static final int DEFAULT_PRECISION = 4;

    //property names
    private static final String LOG_HOME = "log_home";
    private static final String LOG_NAME = "log_name";
    private static final String LOG_PROP = "log_prop";
    private static final String APP_DATA_HOME = "app_home";
    
    private static final String USER_HOME = System.getProperty("user.home").replaceAll("\\\\", "/");
    
    private static final String DEFAULT_LOG_HOME = (USER_HOME + "/jmetrik/log");
    public static final String DEFAULT_LOG_NAME = "jmetrik-logger";//cannot be changed by user
    public static final String DEFAULT_SCRIPT_LOG_NAME = "jmetrik-script-log";//cannot be changed by user

    //Log properties before version 4
    private static final String DEFAULT_LOG_PROPERTY_FILE = "jmetrik-log.props";//cannot be changed by user

    //Log properties changed in version 4
    private static final String DEFAULT_LOG_PROPERTY_FILE_V4 = "jmetrik-log-v4.props";//cannot be changed by user
    private static final String DEFAULT_APP_DATA_HOME = USER_HOME + "/jmetrik/jmk";//not editable by user

    //database property names
    private static final String DATABASE_HOME = "database_home";
    private static final String DATABASE_PORT = "database_port";
    private static final String DATABASE_TYPE = "database_type";
    private static final String DATABASE_AUTH_NAME = "database_auth_name";

    //database property defaults
    private static final String DEFAULT_DB_HOME = (USER_HOME + "/jmetrik/database");
    private static final int DEFAULT_DB_PORT = -1;
    private static final String DEFAULT_DB_TYPE = DatabaseType.APACHE_DERBY.toString();
    private static final String DEFAULT_DB_AUTH_NAME = "jmetrik-db-auth.props";

    //font property names
    private static final String OUTPUT_FONT = "output_font";
    private static final String OUTPUT_FONT_STYLE = "output_font_style";
    private static final String OUTPUT_FONT_SIZE = "output_font_size";
    
    //default font style and size
    private static final int DEFAULT_FONT_STYLE = Font.PLAIN;
    private static final int DEFAULT_FONT_SIZE = 12;

    //Location of spss plugin
    private static final String SPSS_PLUGIN_PATH = "spss_plugin_path";
    private static final String DEFAULT_SPSS_PLUGIN_PATH = "";

    //chart line names
    private static final String CHART_LINE1 = "chart_line1";
    private static final String CHART_LINE2 = "chart_line2";
    private static final String CHART_LINE3 = "chart_line3";
    private static final String CHART_LINE4 = "chart_line4";
    private static final String CHART_LINE5 = "chart_line5";
    private static final String CHART_LINE6 = "chart_line6";
    private static final String CHART_LINE7 = "chart_line7";
    private static final String CHART_LINE8 = "chart_line8";
    private static final String CHART_LINE9 = "chart_line9";

    //default line styles
    private static final String DEFAULT_CHART_LINE1 = ChartStyle.LINE_STYLE_NAME[0];
    private static final String DEFAULT_CHART_LINE2 = ChartStyle.LINE_STYLE_NAME[1];
    private static final String DEFAULT_CHART_LINE3 = ChartStyle.LINE_STYLE_NAME[2];
    private static final String DEFAULT_CHART_LINE4 = ChartStyle.LINE_STYLE_NAME[3];
    private static final String DEFAULT_CHART_LINE5 = ChartStyle.LINE_STYLE_NAME[4];
    private static final String DEFAULT_CHART_LINE6 = ChartStyle.LINE_STYLE_NAME[5];
    private static final String DEFAULT_CHART_LINE7 = ChartStyle.LINE_STYLE_NAME[6];
    private static final String DEFAULT_CHART_LINE8 = ChartStyle.LINE_STYLE_NAME[7];
    private static final String DEFAULT_CHART_LINE9 = ChartStyle.LINE_STYLE_NAME[8];


    //chart color names
    private static final String CHART_COLOR1 = "chart_color1";
    private static final String CHART_COLOR2 = "chart_color2";
    private static final String CHART_COLOR3 = "chart_color3";
    private static final String CHART_COLOR4 = "chart_color4";
    private static final String CHART_COLOR5 = "chart_color5";
    private static final String CHART_COLOR6 = "chart_color6";
    private static final String CHART_COLOR7 = "chart_color7";
    private static final String CHART_COLOR8 = "chart_color8";
    private static final String CHART_COLOR9 = "chart_color9";

    //default chart colors
    private static final String DEFAULT_CHART_COLOR1 = ChartStyle.CHART_COLOR_NAME[0];
    private static final String DEFAULT_CHART_COLOR2 = ChartStyle.CHART_COLOR_NAME[1];
    private static final String DEFAULT_CHART_COLOR3 = ChartStyle.CHART_COLOR_NAME[2];
    private static final String DEFAULT_CHART_COLOR4 = ChartStyle.CHART_COLOR_NAME[3];
    private static final String DEFAULT_CHART_COLOR5 = ChartStyle.CHART_COLOR_NAME[4];
    private static final String DEFAULT_CHART_COLOR6 = ChartStyle.CHART_COLOR_NAME[5];
    private static final String DEFAULT_CHART_COLOR7 = ChartStyle.CHART_COLOR_NAME[6];
    private static final String DEFAULT_CHART_COLOR8 = ChartStyle.CHART_COLOR_NAME[7];
    private static final String DEFAULT_CHART_COLOR9 = ChartStyle.CHART_COLOR_NAME[8];

    //chart options
    private static final String CHART_SHOW_LEGEND = "chart_legend";
    private static final String CHART_LEGEND_POSITION = "chart_legend_position";
    private static final String CHART_ORIENTATION = "chart_orientation";
    private static final String CHART_SHOW_MARKERS = "chart_markers";
    private static final String CHART_WIDTH = "chart_width";
    private static final String CHART_HEIGHT = "chart_height";
    private static final String CHART_LINE_WIDTH = "chart_line_width";

    //default chart options
    private static final boolean DEFAULT_CHART_SHOW_LEGEND = true;
    private static final boolean DEFAULT_CHART_SHOW_MARKERS = false;
    private static final String DEFAULT_CHART_LEGEND_POSITION = "bottom";
    private static final String DEFAULT_CHART_ORIENTATION = "vertical";
    private static final int DEFAULT_CHART_WIDTH = 450;
    private static final int DEFAULT_CHART_HEIGHT = 400;
    private static final float DEFAULT_CHART_LINE_WIDTH = 1.0f;

    Preferences p = null;
    static Logger logger = Logger.getLogger(DEFAULT_LOG_NAME);
    private ArrayList<PropertyChangeListener> propertyChangeListeners = null;

    public JmetrikPreferencesManager(){
        propertyChangeListeners = new ArrayList<PropertyChangeListener>();
        p = Preferences.userRoot().node(JMETRIK_PREFERENCES_NODE);

//        removeAllPreferences();//debugging only
        initializePreferences();
//        printAllPreferences();//debugging only

    }

    /**
     * For debugging only
     */
    public void printAllPreferences(){
        try{
            for(String s : p.keys()){
                System.out.println(s + " = " + p.get(s, ""));
            }
        }catch(BackingStoreException ex){
            ex.printStackTrace();
        }
    }

    /**
     * For debugging and development only
     */
    private void removeAllPreferences(){
        try{
            for(String s : p.keys()){
                p.remove(s);
            }
        }catch(BackingStoreException ex){
            ex.printStackTrace();
        }
    }
    
    private void createDatabaseHome(File f){
        if(!f.exists()){
            if(f.mkdirs()){
                firePropertyChange("status", "", "Database home created.");
            }else{
                firePropertyChange("error", "", "Error - Database home could not be created.");
            }
        }        
    }

    private void createAppDataHome(File f){
        if(!f.exists()){
            if(f.mkdirs()){
                firePropertyChange("status", "", "App home created.");
            }else{
                firePropertyChange("error", "", "Error - Application data home could not be created.");
            }
        }
    }
    
    private void createDatabaseAuthenticationProperties(File dbAuthFile){
        try{
            dbAuthFile.createNewFile();
        }catch(IOException ex){
            firePropertyChange("error", "", "Error - Database authentication file could not be created.");
        }
        
    }

    private void createLogHome(String logHome){
        File f = new File(logHome);
        if(!f.exists()){
            if(f.mkdirs()){
                firePropertyChange("status", "", "Log home created.");
            }else{
                firePropertyChange("error", "", "Error - Log home could not be created.");
            }
        }
    }
    
    private void createLogProperties(String logHome){
        //directory should already exist
        //create log4j properties file if it does not exist
        String header = "#DO NOT EDIT - JMETRIK LOG PROPERTIES FILE - DO NOT EDIT";
        String fullPropertiesName = (logHome + "/" + DEFAULT_LOG_PROPERTY_FILE_V4);
        String fullLogFileName = (logHome + "/" + DEFAULT_LOG_NAME);
        String fullScriptLogFileName = (logHome + "/" + DEFAULT_SCRIPT_LOG_NAME);
        File f = new File(fullPropertiesName);
        if(!f.exists()){
            try{
                createLogHome(logHome);
                f.createNewFile();
                BufferedWriter bw = new BufferedWriter(new FileWriter(f));
                bw.append(header); bw.newLine();
                bw.append("log4j.logger.jmetrik-logger=ALL, adminAppender"); bw.newLine();
                bw.append("log4j.logger.jmetrik-script-logger=INFO, scriptAppender"); bw.newLine();
                bw.append("log4j.additivity.jmetrik-logger=false"); bw.newLine();
                bw.append("log4j.additivity.jmetrik-script-logger=false"); bw.newLine();

                //Main appender processes all levels
                bw.append("log4j.appender.adminAppender=org.apache.log4j.FileAppender"); bw.newLine();
                bw.append("log4j.appender.adminAppender.layout=org.apache.log4j.PatternLayout"); bw.newLine();
                bw.append("log4j.appender.adminAppender.File=" + fullLogFileName); bw.newLine();
                bw.append("log4j.appender.adminAppender.Append=false"); bw.newLine();
                bw.append("log4j.appender.adminAppender.layout.ConversionPattern=[%p] %d{DATE} %n%m%n%n"); bw.newLine();

                //Script appender processes scripts only
                bw.append("log4j.appender.scriptAppender=org.apache.log4j.FileAppender"); bw.newLine();
                bw.append("log4j.appender.scriptAppender.layout=org.apache.log4j.PatternLayout"); bw.newLine();
                bw.append("log4j.appender.scriptAppender.File=" + fullScriptLogFileName); bw.newLine();
                bw.append("log4j.appender.scriptAppender.Append=false"); bw.newLine();
                bw.append("log4j.appender.scriptAppender.layout.ConversionPattern=%m%n%n"); bw.newLine();


                bw.close();
            }catch(IOException ex){
                firePropertyChange("error", "", "Error - Log properties file could not be created.");
            }
        }
    }
    
    public void loadLog(){
        String logHome = p.get(LOG_HOME, DEFAULT_LOG_HOME);

        //Delete old log properties file -- needed for version 4
        String fullPropertiesName = (logHome + "/" + DEFAULT_LOG_PROPERTY_FILE);
        File f1 = new File(fullPropertiesName);
        if(f1.exists()){
            f1.delete();
            System.out.print("LOG PROP FILE: " + f1.getAbsolutePath() + " DELETED");
        }

        String header = "#DO NOT EDIT - JMETRIK LOG PROPERTIES FILE - DO NOT EDIT";
        fullPropertiesName = (logHome + "/" + DEFAULT_LOG_PROPERTY_FILE_V4);
        String fullLogFileName = (logHome + "/" + DEFAULT_LOG_NAME);
        String fullScriptLogFileName = (logHome + "/" + DEFAULT_SCRIPT_LOG_NAME);

        //create log properties file, if it does not exist
        createLogProperties(logHome);

        //start logging
        Properties p = new Properties();
        File f = new File(fullPropertiesName);
        try{
            File logFile = new File(logHome + "/" + DEFAULT_LOG_NAME);
            if(!logFile.exists()) logFile.createNewFile();
            
            FileInputStream in = new FileInputStream(f);
            p.load(in);
            in.close();
            p.setProperty("log4j.appender.jmetrik-logger.File", fullLogFileName);
            p.setProperty("log4j.appender.jmetrik-script-logger.File", fullScriptLogFileName);

            FileOutputStream out = new FileOutputStream(new File(fullLogFileName));
            p.store(out, header);
            out.close();
            PropertyConfigurator.configure(p);
            logger.info("Logging started");
        }catch(IOException ex){
            this.firePropertyChange("error", "", "Error - Unable to configure log.");
        }

    }

    private String getDefaultFont(){
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();

        for(String s : fontNames){
            if(s.equals("Courier")) return "Courier";
            if(s.equals("Courier New")) return "Courier New";
            if(s.equals("Menlo")) return "Menlo";
            if(s.equals("Monaco")) return "Monaco";
            if(s.equals("Lucida Sans Typewriter")) return "Lucida Sans Typewriter";
        }
        return "Lucida Sans Typewriter";//preferred default font
    }

    private void initializePreferences(){
        //if preference fileExists, set it. Otherwise, set default

        String appDataHome = p.get(APP_DATA_HOME, DEFAULT_APP_DATA_HOME);
        File appFolder = new File(appDataHome);
        if(!appFolder.exists()) createAppDataHome(appFolder);

        String dbHome = p.get(DATABASE_HOME, DEFAULT_DB_HOME);
        File dbFolder = new File(dbHome);
        if(!dbFolder.exists()) createDatabaseHome(dbFolder);
        
        String authName = p.get(DATABASE_AUTH_NAME, DEFAULT_DB_AUTH_NAME);
        File dbAuthFile = new File(dbHome + "/" + authName);
        if(!dbAuthFile.exists()) createDatabaseAuthenticationProperties(dbAuthFile);

        p.putInt(OUTPUT_PRECISION, p.getInt(OUTPUT_PRECISION, DEFAULT_PRECISION));
        
        p.put(LOG_HOME, p.get(LOG_HOME, DEFAULT_LOG_HOME));
        p.put(LOG_NAME, p.get(LOG_NAME, DEFAULT_LOG_NAME));
        
        p.put(DATABASE_HOME, p.get(DATABASE_HOME, DEFAULT_DB_HOME));
        p.putInt(DATABASE_PORT, p.getInt(DATABASE_PORT, DEFAULT_DB_PORT));
        p.put(DATABASE_TYPE, p.get(DATABASE_TYPE, DEFAULT_DB_TYPE));
        p.put(DATABASE_AUTH_NAME, p.get(DATABASE_AUTH_NAME, DEFAULT_DB_AUTH_NAME));
        
        p.put(OUTPUT_FONT, p.get(OUTPUT_FONT, getDefaultFont()));
        p.putInt(OUTPUT_FONT_STYLE, p.getInt(OUTPUT_FONT_STYLE, DEFAULT_FONT_STYLE));
        p.putInt(OUTPUT_FONT_SIZE, p.getInt(OUTPUT_FONT_SIZE, DEFAULT_FONT_SIZE));

        p.put(CHART_COLOR1, p.get(CHART_COLOR1, DEFAULT_CHART_COLOR1));
        p.put(CHART_COLOR2, p.get(CHART_COLOR2, DEFAULT_CHART_COLOR2));
        p.put(CHART_COLOR3, p.get(CHART_COLOR3, DEFAULT_CHART_COLOR3));
        p.put(CHART_COLOR4, p.get(CHART_COLOR4, DEFAULT_CHART_COLOR4));
        p.put(CHART_COLOR5, p.get(CHART_COLOR5, DEFAULT_CHART_COLOR5));
        p.put(CHART_COLOR6, p.get(CHART_COLOR6, DEFAULT_CHART_COLOR6));
        p.put(CHART_COLOR7, p.get(CHART_COLOR7, DEFAULT_CHART_COLOR7));
        p.put(CHART_COLOR8, p.get(CHART_COLOR8, DEFAULT_CHART_COLOR8));
        p.put(CHART_COLOR9, p.get(CHART_COLOR9, DEFAULT_CHART_COLOR9));

        p.put(CHART_LINE1, p.get(CHART_LINE1, DEFAULT_CHART_LINE1));
        p.put(CHART_LINE2, p.get(CHART_LINE2, DEFAULT_CHART_LINE2));
        p.put(CHART_LINE3, p.get(CHART_LINE3, DEFAULT_CHART_LINE3));
        p.put(CHART_LINE4, p.get(CHART_LINE4, DEFAULT_CHART_LINE4));
        p.put(CHART_LINE5, p.get(CHART_LINE5, DEFAULT_CHART_LINE5));
        p.put(CHART_LINE6, p.get(CHART_LINE6, DEFAULT_CHART_LINE6));
        p.put(CHART_LINE7, p.get(CHART_LINE7, DEFAULT_CHART_LINE7));
        p.put(CHART_LINE8, p.get(CHART_LINE8, DEFAULT_CHART_LINE8));
        p.put(CHART_LINE9, p.get(CHART_LINE9, DEFAULT_CHART_LINE9));

        p.putBoolean(CHART_SHOW_LEGEND, p.getBoolean(CHART_SHOW_LEGEND, DEFAULT_CHART_SHOW_LEGEND));
        p.putBoolean(CHART_SHOW_MARKERS, p.getBoolean(CHART_SHOW_MARKERS, DEFAULT_CHART_SHOW_MARKERS));
        p.put(CHART_LEGEND_POSITION, p.get(CHART_LEGEND_POSITION, DEFAULT_CHART_LEGEND_POSITION));
        p.put(CHART_ORIENTATION, p.get(CHART_ORIENTATION, DEFAULT_CHART_ORIENTATION));
        p.putInt(CHART_WIDTH, p.getInt(CHART_WIDTH, DEFAULT_CHART_WIDTH));
        p.putInt(CHART_HEIGHT, p.getInt(CHART_HEIGHT, DEFAULT_CHART_HEIGHT));
        p.putFloat(CHART_LINE_WIDTH, p.getFloat(CHART_LINE_WIDTH, DEFAULT_CHART_LINE_WIDTH));

        this.firePropertyChange("status", "", "Default preferences created.");
    }

    public Color[] getColors(){
        Color[] color = new Color[9];
        color[0] = PaintUtilities.stringToColor(p.get(CHART_COLOR1, DEFAULT_CHART_COLOR1));
        color[1] = PaintUtilities.stringToColor(p.get(CHART_COLOR2, DEFAULT_CHART_COLOR2));
        color[2] = PaintUtilities.stringToColor(p.get(CHART_COLOR3, DEFAULT_CHART_COLOR3));
        color[3] = PaintUtilities.stringToColor(p.get(CHART_COLOR4, DEFAULT_CHART_COLOR4));
        color[4] = PaintUtilities.stringToColor(p.get(CHART_COLOR5, DEFAULT_CHART_COLOR5));
        color[5] = PaintUtilities.stringToColor(p.get(CHART_COLOR6, DEFAULT_CHART_COLOR6));
        color[6] = PaintUtilities.stringToColor(p.get(CHART_COLOR7, DEFAULT_CHART_COLOR7));
        color[7] = PaintUtilities.stringToColor(p.get(CHART_COLOR8, DEFAULT_CHART_COLOR8));
        color[8] = PaintUtilities.stringToColor(p.get(CHART_COLOR9, DEFAULT_CHART_COLOR9));
        return color;
    }

    public void setColors(Color[] color){
        int n = color.length;
        if(n>=1) p.put(CHART_COLOR1, PaintUtilities.colorToString(color[0]));
        if(n>=2) p.put(CHART_COLOR2, PaintUtilities.colorToString(color[1]));
        if(n>=3) p.put(CHART_COLOR3, PaintUtilities.colorToString(color[2]));
        if(n>=4) p.put(CHART_COLOR4, PaintUtilities.colorToString(color[3]));
        if(n>=5) p.put(CHART_COLOR5, PaintUtilities.colorToString(color[4]));
        if(n>=6) p.put(CHART_COLOR6, PaintUtilities.colorToString(color[5]));
        if(n>=7) p.put(CHART_COLOR7, PaintUtilities.colorToString(color[6]));
        if(n>=8) p.put(CHART_COLOR8, PaintUtilities.colorToString(color[7]));
        if(n>=9) p.put(CHART_COLOR9, PaintUtilities.colorToString(color[8]));
    }

    /**
     * Line styles defined here.
     *
     * @param s name of line style
     * @return
     */
    private float[] stringToLineStyle(String s){
        String temp = s.trim().toLowerCase();
        if(DEFAULT_CHART_LINE1.equals(temp)){
             return ChartStyle.LINE_STYLE[0];//solid line
        }else if(DEFAULT_CHART_LINE2.equals(temp)){
            return ChartStyle.LINE_STYLE[1];
        }else if(DEFAULT_CHART_LINE3.equals(temp)){
            return ChartStyle.LINE_STYLE[2];
        }else if(DEFAULT_CHART_LINE4.equals(temp)){
            return ChartStyle.LINE_STYLE[3];
        }else if(DEFAULT_CHART_LINE5.equals(temp)){
            return ChartStyle.LINE_STYLE[4];
        }else if(DEFAULT_CHART_LINE6.equals(temp)){
            return ChartStyle.LINE_STYLE[5];
        }else if(DEFAULT_CHART_LINE7.equals(temp)){
            return ChartStyle.LINE_STYLE[6];
        }else if(DEFAULT_CHART_LINE8.equals(temp)){
            return ChartStyle.LINE_STYLE[7];
        }else if(DEFAULT_CHART_LINE9.equals(temp)){
            return ChartStyle.LINE_STYLE[8];
        }else{
             return ChartStyle.LINE_STYLE[0];//solid line
        }
    }

    public float[][] getLineStyles(){
        float[][] lineStyles = new float[9][];
        lineStyles[0] = stringToLineStyle(p.get(CHART_LINE1, DEFAULT_CHART_LINE1));
        lineStyles[1] = stringToLineStyle(p.get(CHART_LINE2, DEFAULT_CHART_LINE1));
        lineStyles[2] = stringToLineStyle(p.get(CHART_LINE3, DEFAULT_CHART_LINE1));
        lineStyles[3] = stringToLineStyle(p.get(CHART_LINE4, DEFAULT_CHART_LINE1));
        lineStyles[4] = stringToLineStyle(p.get(CHART_LINE5, DEFAULT_CHART_LINE1));
        lineStyles[5] = stringToLineStyle(p.get(CHART_LINE6, DEFAULT_CHART_LINE1));
        lineStyles[6] = stringToLineStyle(p.get(CHART_LINE7, DEFAULT_CHART_LINE1));
        lineStyles[7] = stringToLineStyle(p.get(CHART_LINE8, DEFAULT_CHART_LINE1));
        lineStyles[8] = stringToLineStyle(p.get(CHART_LINE9, DEFAULT_CHART_LINE1));
        return lineStyles;
    }

    public void setLineStyles(String[] lineStyles){
        int n = lineStyles.length;
        if(n>=1) p.put(CHART_LINE1, lineStyles[0]);
        if(n>=2) p.put(CHART_LINE2, lineStyles[1]);
        if(n>=3) p.put(CHART_LINE3, lineStyles[2]);
        if(n>=4) p.put(CHART_LINE4, lineStyles[3]);
        if(n>=5) p.put(CHART_LINE5, lineStyles[4]);
        if(n>=6) p.put(CHART_LINE6, lineStyles[5]);
        if(n>=7) p.put(CHART_LINE7, lineStyles[6]);
        if(n>=8) p.put(CHART_LINE8, lineStyles[7]);
        if(n>=9) p.put(CHART_LINE9, lineStyles[8]);
    }

    public void setShowLegend(boolean showLegend){
        p.putBoolean(CHART_SHOW_LEGEND, showLegend);
    }

    public boolean getShowLegend(){
        return p.getBoolean(CHART_SHOW_LEGEND, DEFAULT_CHART_SHOW_LEGEND);
    }

    public void setShowMarkers(boolean showMarkers){
        p.putBoolean(CHART_SHOW_MARKERS, showMarkers);
    }

    public boolean getShowMarkers(){
        return p.getBoolean(CHART_SHOW_MARKERS, DEFAULT_CHART_SHOW_MARKERS);
    }

    public void setLegendPosition(RectangleEdge position){
        String s = "right";
        if(position.equals(RectangleEdge.BOTTOM)){
            s = "bottom";
        }else if(position.equals(RectangleEdge.LEFT)){
            s = "left";
        }else if(position.equals(RectangleEdge.TOP)){
            s = "top";
        }
        p.put(CHART_LEGEND_POSITION, s);
    }

    public RectangleEdge getLegendPosition(){
        String s = p.get(CHART_LEGEND_POSITION, DEFAULT_CHART_LEGEND_POSITION);
        if("bottom".equals(s)){
            return RectangleEdge.BOTTOM;
        }else if("left".equals(s)){
            return RectangleEdge.LEFT;
        }else if("top".equals(s)){
            return RectangleEdge.TOP;
        }else{
            return RectangleEdge.RIGHT;
        }
    }

    public void setChartOrientation(PlotOrientation plotOrientation){
        String s = "vertical";
        if(plotOrientation.equals(PlotOrientation.HORIZONTAL)){
            s = "horizontal";
        }
        p.put(CHART_ORIENTATION, s);
    }

    public PlotOrientation getChartOrientation(){
        String s = p.get(CHART_ORIENTATION, DEFAULT_CHART_ORIENTATION);
        if("horizontal".equals(s)){
            return PlotOrientation.HORIZONTAL;
        }else{
            return PlotOrientation.VERTICAL;
        }
    }

    public void setChartWidth(int width){
        p.putInt(CHART_WIDTH, width);
    }

    public int getChartWidth(){
        return p.getInt(CHART_WIDTH, DEFAULT_CHART_WIDTH);
    }

    public void setChartHeight(int height){
        p.putInt(CHART_HEIGHT, height);
    }

    public int getChartHeight(){
        return p.getInt(CHART_HEIGHT, DEFAULT_CHART_HEIGHT);
    }

    public void setChartLineWidth(float lineWidth){
        p.putFloat(CHART_LINE_WIDTH, lineWidth);
    }

    public float getChartLineWidth(){
        return p.getFloat(CHART_LINE_WIDTH, DEFAULT_CHART_LINE_WIDTH);
    }
    
    public Font getFont(){
        String fName = p.get(OUTPUT_FONT, getDefaultFont());
        int fStyle = p.getInt(OUTPUT_FONT_STYLE, DEFAULT_FONT_STYLE);
        int fSize = p.getInt(OUTPUT_FONT_SIZE, DEFAULT_FONT_SIZE);
        Font font = new Font(fName, fStyle, fSize);
        return font;
    }

    public void setFont(Font f){
        p.put(OUTPUT_FONT, f.getFontName());
        p.putInt(OUTPUT_FONT_STYLE, f.getStyle());
        p.putInt(OUTPUT_FONT_SIZE, f.getSize());
    }
    
    public String getLogHome(){
        return p.get(LOG_HOME, DEFAULT_LOG_HOME);
    }
    
    public void setLogHome(String logHome){
        String newHome = logHome.trim();
        String oldHome = p.get(LOG_HOME, DEFAULT_LOG_HOME);
        if(!newHome.equals("") && !oldHome.equals(newHome)){
            createLogProperties(newHome);
            p.put(LOG_HOME, newHome);
            loadLog();
        }else{
            p.put(LOG_HOME,oldHome);
        }
    }
    
    public String getLogFileName(){
        return p.get(LOG_NAME, DEFAULT_LOG_NAME);
    }

    public String getSpssPluginPath(){
        return p.get(SPSS_PLUGIN_PATH, DEFAULT_SPSS_PLUGIN_PATH);
    }

    public void setSpssPluginPath(String path){
        p.put(SPSS_PLUGIN_PATH, path);
    }
    
    public String getDatabaseHome(){
        String h = p.get(DATABASE_HOME, DEFAULT_DB_HOME);
        return h;
    }

    /**
     * Create required folders and files if new database home differs from the existing one.
     *
     * @param newDbHome
     */
    public void setDatabaseHome(String newDbHome){
        String h = newDbHome.trim();
        if(!h.equals("")){
            String oldDbHome = p.get(DATABASE_HOME, DEFAULT_DB_HOME);
            if(oldDbHome.equals(h)){
                p.put(DATABASE_HOME, oldDbHome);
            }else{
                File dbFolder = new File(h);
                if(!dbFolder.exists()) createDatabaseHome(dbFolder);
                String authName = p.get(DATABASE_AUTH_NAME, DEFAULT_DB_AUTH_NAME);
                File dbAuthFile = new File(h + "/" + authName);
                if(!dbAuthFile.exists()) createDatabaseAuthenticationProperties(dbAuthFile);
                p.put(DATABASE_HOME, h);
            }
        }
    }

    public String getApplicationDataHome(){
        return p.get(APP_DATA_HOME, DEFAULT_APP_DATA_HOME);
    }
    
    public int getDatabasePort(){
        return p.getInt(DATABASE_PORT, -1);
    }
    
    public void setDatabasePort(int port){
        p.putInt(DATABASE_PORT, port);
    }
    
    public String getDatabaseAuthenticationName(){
        return p.get(DATABASE_AUTH_NAME, DEFAULT_DB_AUTH_NAME);
    }
    
    public int getPrecision(){
        return p.getInt(OUTPUT_PRECISION, 4);
    }
    
    public void setPrecision(int precision){
        if(precision>0) p.putInt(OUTPUT_PRECISION, precision);
    }
    
    public String getDatabaseType(){
        return DatabaseType.APACHE_DERBY.toString();//apache derby is the only database currently implemented in jmetrik
//        return p.get(DATABASE_TYPE, DatabaseType.APACHE_DERBY.toString()); //use this line whe other databases are implemented
    }
    
    public void setDatabaseType(String dbType){
        //apache derby is the only database currently implemented in jmetrik
        //do not allow other database type
        p.put(DATABASE_TYPE, DatabaseType.APACHE_DERBY.toString());
//        p.put(DATABASE_TYPE, dbType); use this line when other databases are implemented
    }

    //===============================================================================================================
    //Process messages here
    //  Note that SwingWorker classes also implement these methods. Just need to add list of
    //  propertyChangeListeners to SwingWorker classes. See importTable(...) for an example.
    //===============================================================================================================
    public synchronized void addPropertyChangeListener(PropertyChangeListener l){
        propertyChangeListeners.add(l);
    }

    public synchronized void removePropertyChangeListener(PropertyChangeListener l){
        propertyChangeListeners.remove(l);
    }

    public synchronized void firePropertyChange(String propertyName, Object oldValue, Object newValue){
        PropertyChangeEvent e = new PropertyChangeEvent(this, propertyName, oldValue, newValue);
        for(PropertyChangeListener l : propertyChangeListeners){
            l.propertyChange(e);
        }
    }

}
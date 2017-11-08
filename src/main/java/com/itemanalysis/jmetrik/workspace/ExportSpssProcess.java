package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.dao.SpssFileExporter;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

public class ExportSpssProcess extends AbstractJmetrikProcess {

    private ExportSpssCommand command = null;

    public ExportSpssProcess(){
        command = new ExportSpssCommand();
    }

    public String getName(){
        return "Export SPSS Process";
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (ExportSpssCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){

    }

    public void runProcess(Connection conn, JmetrikDatabaseFactory dbFactory, JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){
        SpssFileExporter exporter = new SpssFileExporter(conn, command);
        SwingWorker worker = (SwingWorker)exporter;
        for(PropertyChangeListener pcl : propertyChangeListeners){
            worker.addPropertyChangeListener(pcl);
        }
        threadPool.execute(worker);
    }

}

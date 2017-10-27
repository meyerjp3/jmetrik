package com.itemanalysis.jmetrik.workspace;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.dao.JmetrikDatabaseFactory;
import com.itemanalysis.jmetrik.dao.SpssFileImporter;

import javax.swing.*;
import java.beans.PropertyChangeListener;
import java.sql.Connection;
import java.util.TreeMap;
import java.util.concurrent.ThreadPoolExecutor;

public class ImportSpssProcess extends AbstractJmetrikProcess {

    private ImportSPSSCommand command = null;

    public ImportSpssProcess(){
        command = new ImportSPSSCommand();
    }

    public String getName(){
        return "Import SPSS Process";
    }

    public boolean commandMatch(Command command){
        return this.command.equals(command);
    }

    public void setCommand(Command command){
        this.command = (ImportSPSSCommand)command;
    }

    public void addMenuItem(final JFrame parent, JMenu menu, final TreeMap<String, JDialog> dialogs, final Workspace workspace, final JList tableList){

    }

    public void runProcess(Connection conn, JmetrikDatabaseFactory dbFactory, JTabbedPane tabbedPane, ThreadPoolExecutor threadPool){
        SpssFileImporter importer = new SpssFileImporter(conn, command);
        SwingWorker worker = (SwingWorker)importer;
        for(PropertyChangeListener pcl : propertyChangeListeners){
            worker.addPropertyChangeListener(pcl);
        }
        threadPool.execute(worker);
    }

}

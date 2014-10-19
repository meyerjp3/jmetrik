package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.psychometrics.irt.estimation.EMStatusEventObject;
import com.itemanalysis.psychometrics.irt.estimation.EMStatusListener;

import java.util.ArrayList;
import java.util.Formatter;

public class PrintableEMStatusListener implements EMStatusListener {

    private StringBuilder sb = new StringBuilder();
    private Formatter f = new Formatter(sb);

    public PrintableEMStatusListener(){

    }

    public void handleEMStatusEvent(EMStatusEventObject eventObject){
        String s = eventObject.getStatus();
        if(!"".equals(s)){
            sb.append(eventObject.getStatus()+"\n");
        }else{
            f.format("%10s", eventObject.getTitle());
            f.format("%5d", eventObject.getIteration()); f.format("%4s", "");
            f.format("%.10f", eventObject.getDelta()); f.format("%4s", "");
            f.format("%.10f", eventObject.getLoglikelihood());
            f.format("%10s", eventObject.getTermCode());f.format("%n");
        }

    }

    @Override
    public String toString(){
        return sb.toString();
    }

}

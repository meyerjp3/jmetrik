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

package com.itemanalysis.jmetrik.graph.histogram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.itemanalysis.psychometrics.histogram.Histogram;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.AbstractDataset;
import org.jfree.data.xy.IntervalXYDataset;

public class HistogramChartDataset extends AbstractDataset implements IntervalXYDataset{

    private HashMap<Comparable, Histogram> histogram = null;

    private ArrayList<Comparable> seriesKeys = null;

    public HistogramChartDataset(){
        super();
        histogram = new HashMap<Comparable, Histogram>();
        seriesKeys = new ArrayList<Comparable>();
    }

    public void addHistogram(Comparable seriesKey, Histogram histogram){
        this.histogram.put(seriesKey, histogram);
        this.seriesKeys.add(seriesKey);
    }

    public Histogram getHistogram(Comparable seriesKey){
        return histogram.get(seriesKey);
    }

    public Comparable getSeriesKey(int series){
        return seriesKeys.get(series);
    }

    public Number getX(int series, int item){
        Histogram h = histogram.get(getSeriesKey(series));
        Double d = new Double(h.getBinAt(item).getMidPoint());
        return d;
    }

    public double getXValue(int series, int item){
        Double d = (Double)getX(series, item);
        return d.doubleValue();
    }

    public Number getY(int series, int item) {
        Histogram h = histogram.get(getSeriesKey(series));
        Double d = null;
        d = new Double(h.getBinAt(item).getValue());
        return d;
    }

    public double getYValue(int series, int item){
        Double d = (Double)getY(series, item);
        return d.doubleValue();
    }

    public Number getStartX(int series, int item){
        Histogram h = histogram.get(seriesKeys.get(series));
        Double d = null;
        d = new Double(h.getBinAt(item).getLowerBound());
        return d;
    }

    public Number getEndX(int series, int item){
        Histogram h = histogram.get(seriesKeys.get(series));
        Double d = null;
        d = new Double(h.getBinAt(item).getUpperBound());
        return d;
    }

    public double getStartXValue(int series, int item){
        Double d = (Double)getStartX(series, item);
        return d.doubleValue();
    }

    public double getEndXValue(int series, int item){
        Double d = (Double)getEndX(series, item);
        return d.doubleValue();
    }

    public Number getStartY(int series, int item){
        return getY(series, item);
    }

    public Number getEndY(int series, int item){
        return getY(series, item);
    }

    public double getStartYValue(int series, int item){
        Double d = (Double)getStartY(series, item);
        return d.doubleValue();
    }

    public double getEndYValue(int series, int item){
        Double d = (Double)getEndY(series, item);
        return d.doubleValue();
    }

    public int getItemCount(int series){
        Histogram h = histogram.get(getSeriesKey(series));
        return h.getNumberOfBins();
    }

    public DomainOrder getDomainOrder(){
        return DomainOrder.NONE;
    }

    public int getSeriesCount(){
        return seriesKeys.size();
    }

    public int indexOf(Comparable seriesKey){
        int seriesCount = getSeriesCount();
        for(int s=0; s<seriesCount; s++){
            if(getSeriesKey(s).equals(seriesKey)){
                return s;
            }
        }
        return -1;
    }

    public Iterator<Comparable> iterator(){
        return histogram.keySet().iterator();
    }

}

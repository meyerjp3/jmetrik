/*
 * Copyright (c) 2011 Patrick Meyer
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

package com.itemanalysis.jmetrik.utils;

import com.itemanalysis.jmetrik.swing.JmetrikTextFile;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;

public class PrintUtilities implements Printable{

    private String[] m_lines;
    private JmetrikTextFile printComponent;
    private int fontSize = 10;
    private Font defaultFont = null;
    public static final int TAB_SIZE=5;

    public PrintUtilities(JmetrikTextFile c){
        printComponent=c;
        initializeFont();
//		printComponent.setFont(new Font("Courier",Font.PLAIN, 10));
    }

    public void initializeFont(){
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontNames = ge.getAvailableFontFamilyNames();
        boolean hasCourier = false;
        int i = 0;

        while(!hasCourier && i< fontNames.length){
            if(fontNames[i].equals("Courier") || fontNames[i].equals("Courier New")){
                hasCourier = true;
            }
            i++;
        }

        if(hasCourier){
            defaultFont = new Font("Courier",Font.PLAIN, fontSize);
        }else{
            defaultFont = new Font("Lucida Sans Typewriter",Font.PLAIN, fontSize);
        }
    }

    public int print(Graphics pg, PageFormat pageFormat, int pageIndex)throws PrinterException {

        Graphics2D g2=(Graphics2D)pg;

        g2.translate((int)pageFormat.getImageableX(), (int)pageFormat.getImageableY());
        int wPage=(int)pageFormat.getImageableWidth();
        int hPage=(int)pageFormat.getImageableHeight();
        g2.setClip(0,0,wPage,hPage);
//		g2.clip(new Rectangle2D.Double(0,0,wPage,hPage));



        g2.setColor(printComponent.getBackground());
        g2.fillRect(0,0,wPage,hPage);
        g2.setColor(printComponent.getForeground());

        g2.setFont(defaultFont);
        FontMetrics fm = g2.getFontMetrics();
        int hLine = fm.getHeight();
//		double scale=10.0/printComponent.getFont().getSize();
//		g2.scale(scale,scale);

        if(m_lines==null){
            m_lines = getLines(fm,wPage);
        }

        int numLines = m_lines.length;
        int linesPerPage = Math.max(hPage/hLine,1);
        int numPages=(int)Math.ceil((double)numLines/(double)linesPerPage);
        if(pageIndex>=numPages){
            m_lines=null;
            return Printable.NO_SUCH_PAGE;
        }

        int x=0;
        int y=fm.getAscent();

        int lineIndex=linesPerPage*pageIndex;
        while(lineIndex<m_lines.length && y<hPage){
            String str=(String)m_lines[lineIndex];
            g2.drawString(str,x,y);
            y+=hLine;
            lineIndex++;
        }

        return Printable.PAGE_EXISTS;
    }


    protected String[] getLines(FontMetrics fm, int wPage){
        String[] lineArray;

        String test=printComponent.getText();
        lineArray=test.split("\n");
        return lineArray;
    }

}

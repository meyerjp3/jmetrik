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

package com.itemanalysis.jmetrik.gui;

import org.apache.log4j.Logger;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

public class JmetrikAboutDialog extends JDialog {



    static Logger logger = Logger.getLogger("jmetrik-logger");

    public JmetrikAboutDialog(Jmetrik parent, String appName, String version, String author, String releaseDate, String copyright, boolean isBeta){

        super(parent, "About", true);

        JPanel p = new JPanel();
        p.setLayout(new GridBagLayout());

        JLabel txt1 = getImageLabel();
        txt1.setBorder(new EmptyBorder(10,5,5,5));
        txt1.setFont(new Font("SansSerif",Font.PLAIN, 18));

        JLabel jmetrikLabel = new JLabel("jMetrik");
        jmetrikLabel.setFont(new Font("SansSerif",Font.PLAIN, 22));

        String versionText = version;
        if(isBeta) versionText += " Beta";
        JLabel versionLabel = new JLabel("Version: " + versionText);
        versionLabel.setFont(new Font("SansSerif",Font.PLAIN, 14));

        JLabel releaseLabel = new JLabel("Release Date: " + releaseDate);
        releaseLabel.setFont(new Font("SansSerif",Font.PLAIN, 14));


        JLabel txt2 = new JLabel("Copyright \u00A9 " + copyright + " " + author);
        txt2.setBorder(new EmptyBorder(0,5,5,5));
        txt2.setFont(new Font("SansSerif",Font.PLAIN, 14));


        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        p.add(txt1,c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        p.add(jmetrikLabel,c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        p.add(versionLabel,c);

        c.gridx = 0;
        c.gridy = 3;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        p.add(releaseLabel,c);

        c.gridx = 0;
        c.gridy = 4;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        p.add(txt2,c);


        JLabel txt4 = new JLabel(appName + " is distributed under GPL version 3.0 or higher");
        txt4.setBorder(new EmptyBorder(5,5,5,5));
        txt4.setFont(new Font("SansSerif",Font.PLAIN, 12));

//        c.gridx = 0;
//        c.gridy = 3;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.NORTHEAST;
//        c.fill = GridBagConstraints.NONE;
//        p.add(new JLabel(""),c);
//        c.gridx = 0;
//        c.gridy = 3;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.NORTHEAST;
//        c.fill = GridBagConstraints.NONE;
//        p.add(new JLabel(""),c);
//        c.gridx = 0;
//        c.gridy = 4;
//        c.gridwidth = 1;
//        c.gridheight = 1;
//        c.weightx = 1;
//        c.weighty = 1;
//        c.anchor = GridBagConstraints.NORTHEAST;
//        c.fill = GridBagConstraints.NONE;
//        p.add(new JLabel(""),c);


        c.gridx = 0;
        c.gridy = 5;
        c.gridwidth = 1;
        c.gridheight = 1;
        c.weightx = 1;
        c.weighty = 1;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        p.add(txt4,c);


        getContentPane().add(p,BorderLayout.CENTER);

        final JButton btOK = new JButton("OK");
        ActionListener lst = new ActionListener(){
            public void actionPerformed(ActionEvent e){
                dispose();
            }
        };
        btOK.addActionListener(lst);
        p = new JPanel();
        p.add(btOK);
        getRootPane().setDefaultButton(btOK);
        getRootPane().registerKeyboardAction(lst,
                KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        getContentPane().add(p, BorderLayout.SOUTH);

        WindowListener wl = new WindowAdapter(){
            public void windowOpened(WindowEvent e){
                btOK.requestFocus();
            }
        };
        addWindowListener(wl);

        pack();
        setSize(400,500);
        setResizable(false);
        setLocationRelativeTo(parent);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

    }

    private JLabel getImageLabel(){
        JLabel picLabel = null;
        String urlString = "/images/jmetrik-256.png";
        URL url = this.getClass().getResource(urlString);
        picLabel = new JLabel(new ImageIcon( url ));
//        try{
//            String urlString = "/images/jmetrik.png";
//            URL url = this.getClass().getResource(urlString);
//            picLabel = new JLabel(new ImageIcon( url ));
////            JmetrikPreferencesManager prefs = new JmetrikPreferencesManager();
////            String appData = prefs.getApplicationDataHome();
////            File f = new File(appData + "/jmetrik.png");
////            BufferedImage myPicture = ImageIO.read(new File(url.getFile()));
//            picLabel = new JLabel(new ImageIcon( url ));
//        }catch(IOException ex){
//            logger.fatal(ex.getMessage(), ex);
//        }
        return picLabel;
    }

}

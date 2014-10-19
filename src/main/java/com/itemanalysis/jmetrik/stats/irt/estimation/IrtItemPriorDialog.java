package com.itemanalysis.jmetrik.stats.irt.estimation;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class IrtItemPriorDialog extends JDialog {

    // Variables declaration - do not modify
    private JRadioButton betaRadioButton;
    private JButton cancelButton;
    private JLabel lognormalMeanLabel;
    private JTextField lognormalMeanTextField;
    private JRadioButton lognormalRadioButton;
    private JLabel lognormalSDLabel;
    private JTextField lognormalSDTextField;
    private JLabel lowerBoundLabel;
    private JTextField lowerBoundTextField;
    private JRadioButton noPriorButton;
    private JTextField normalMeanTextField;
    private JLabel normalMeanLabel;
    private JRadioButton normalRadioButton;
    private JLabel normalSDLabel;
    private JTextField normalSDTextField;
    private JButton okButton;
    private ButtonGroup priorButtonGroup;
    private JPanel priorPanel;
    private JLabel shape1Label;
    private JTextField shape1TextField;
    private JLabel shape2Label;
    private JTextField shape2TextField;
    private JLabel upperBoundLabel;
    private JTextField upperBoundTextField;
    // End of variables declaration

    private String priorString = "";
    private boolean canRun = false;

    public IrtItemPriorDialog(JDialog parent, String priorString){
        super(parent, "Item Prior Specification", true);

        initComponents();
        setResizable(false);
        setLocationRelativeTo(parent);

        this.priorString = priorString;

        if(!"".equals(priorString.trim())){
            String[] s = priorString.split(",");
            String name = s[0].trim();

            if("normal".equals(name) && s.length==3){
                normalRadioButton.setSelected(true);
                normalMeanTextField.setText(s[1]);
                normalSDTextField.setText(s[2]);
                normalMeanTextField.setEnabled(true);
                normalSDTextField.setEnabled(true);
            }else if("lognormal".equals(name) && s.length==3){
                lognormalRadioButton.setSelected(true);
                lognormalMeanTextField.setText(s[1]);
                lognormalSDTextField.setText(s[2]);
                lognormalMeanTextField.setEnabled(true);
                lognormalSDTextField.setEnabled(true);
            }else if("beta".equals(name) && s.length==5){
                betaRadioButton.setSelected(true);
                shape1TextField.setText(s[1]);
                shape2TextField.setText(s[2]);
                lowerBoundTextField.setText(s[3]);
                upperBoundTextField.setText(s[4]);
                shape1TextField.setEnabled(true);
                shape2TextField.setEnabled(true);
                lowerBoundTextField.setEnabled(true);
                upperBoundTextField.setEnabled(true);
            }
        }


    }

    public String getPriorString(){
        return priorString;
    }

    private void initComponents() {
        
        boolean normalPrior = false;
        boolean logNormalPrior = false;
        boolean betaPrior = false;

        priorButtonGroup = new ButtonGroup();
        priorPanel = new JPanel();
        noPriorButton = new JRadioButton();
        normalRadioButton = new JRadioButton();
        normalMeanLabel = new JLabel();
        normalMeanTextField = new JTextField();
        normalSDTextField = new JTextField();
        normalSDLabel = new JLabel();
        lognormalRadioButton = new JRadioButton();
        lognormalMeanLabel = new JLabel();
        lognormalMeanTextField = new JTextField();
        lognormalSDTextField = new JTextField();
        lognormalSDLabel = new JLabel();
        betaRadioButton = new JRadioButton();
        shape1Label = new JLabel();
        shape1TextField = new JTextField();
        lowerBoundLabel = new JLabel();
        lowerBoundTextField = new JTextField();
        upperBoundTextField = new JTextField();
        upperBoundLabel = new JLabel();
        shape2TextField = new JTextField();
        shape2Label = new JLabel();
        okButton = new JButton();
        cancelButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        priorPanel.setBorder(BorderFactory.createTitledBorder(""));

        priorButtonGroup.add(noPriorButton);
        noPriorButton.setSelected(true);
        noPriorButton.setText("No prior");
        noPriorButton.setActionCommand("noprior");
        noPriorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                normalMeanTextField.setEnabled(false);
                normalSDTextField.setEnabled(false);
                lognormalMeanTextField.setEnabled(false);
                lognormalSDTextField.setEnabled(false);
                shape1TextField.setEnabled(false);
                shape2TextField.setEnabled(false);
                lowerBoundTextField.setEnabled(false);
                upperBoundTextField.setEnabled(false);
            }
        });

        priorButtonGroup.add(normalRadioButton);
        normalRadioButton.setText("Normal");
        normalRadioButton.setActionCommand("normal");
        normalRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                normalMeanTextField.setEnabled(true);
                normalSDTextField.setEnabled(true);
                lognormalMeanTextField.setEnabled(false);
                lognormalSDTextField.setEnabled(false);
                shape1TextField.setEnabled(false);
                shape2TextField.setEnabled(false);
                lowerBoundTextField.setEnabled(false);
                upperBoundTextField.setEnabled(false);
            }
        });

        normalMeanLabel.setText("Mean");

        normalMeanTextField.setPreferredSize(new Dimension(100, 28));
        normalMeanTextField.setText("0.0");
        normalMeanTextField.setEnabled(false);

        normalSDTextField.setPreferredSize(new Dimension(100, 28));
        normalSDTextField.setText("1.0");
        normalSDTextField.setEnabled(false);

        normalSDLabel.setText("S.D.");

        priorButtonGroup.add(lognormalRadioButton);
        lognormalRadioButton.setText("Log-normal");
        lognormalRadioButton.setActionCommand("lognormal");
        lognormalRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                normalMeanTextField.setEnabled(false);
                normalSDTextField.setEnabled(false);
                lognormalMeanTextField.setEnabled(true);
                lognormalSDTextField.setEnabled(true);
                shape1TextField.setEnabled(false);
                shape2TextField.setEnabled(false);
                lowerBoundTextField.setEnabled(false);
                upperBoundTextField.setEnabled(false);
            }
        });

        lognormalMeanLabel.setText("Mean");

        lognormalMeanTextField.setPreferredSize(new Dimension(100, 28));
        lognormalMeanTextField.setText("0.0");
        lognormalMeanTextField.setEnabled(false);

        lognormalSDTextField.setPreferredSize(new Dimension(100, 28));
        lognormalSDTextField.setText("0.5");
        lognormalSDTextField.setEnabled(false);

        lognormalSDLabel.setText("S.D.");

        priorButtonGroup.add(betaRadioButton);
        betaRadioButton.setText("Beta");
        betaRadioButton.setActionCommand("beta");
        betaRadioButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                normalMeanTextField.setEnabled(false);
                normalSDTextField.setEnabled(false);
                lognormalMeanTextField.setEnabled(false);
                lognormalSDTextField.setEnabled(false);
                shape1TextField.setEnabled(true);
                shape2TextField.setEnabled(true);
                lowerBoundTextField.setEnabled(true);
                upperBoundTextField.setEnabled(true);
            }
        });

        shape1Label.setText("Shape 1");

        shape1TextField.setPreferredSize(new Dimension(100, 28));
        shape1TextField.setText("3.5");
        shape1TextField.setEnabled(false);

        lowerBoundLabel.setText("Lower bound");

        lowerBoundTextField.setPreferredSize(new Dimension(100, 28));
        lowerBoundTextField.setText("0.0");
        lowerBoundTextField.setEnabled(false);

        upperBoundTextField.setPreferredSize(new Dimension(100, 28));
        upperBoundTextField.setText("0.5");
        upperBoundTextField.setEnabled(false);

        upperBoundLabel.setText("Upper bound");

        shape2TextField.setPreferredSize(new Dimension(100, 28));
        shape2TextField.setText("4.0");
        shape2TextField.setEnabled(false);

        shape2Label.setText("Shape 2");

        okButton.setText("OK");
        okButton.setMaximumSize(new Dimension(75, 28));
        okButton.setMinimumSize(new Dimension(75, 28));
        okButton.setPreferredSize(new Dimension(75, 28));
        okButton.addActionListener(new OkActionListener());

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new Dimension(75, 28));
        cancelButton.setMinimumSize(new Dimension(75, 28));
        cancelButton.setPreferredSize(new Dimension(75, 28));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });

        GroupLayout priorPanelLayout = new GroupLayout(priorPanel);
        priorPanel.setLayout(priorPanelLayout);
        priorPanelLayout.setHorizontalGroup(
            priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(priorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(priorPanelLayout.createSequentialGroup()
                        .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(noPriorButton)
                            .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addGroup(priorPanelLayout.createSequentialGroup()
                                    .addGap(21, 21, 21)
                                    .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addGroup(priorPanelLayout.createSequentialGroup()
                                            .addComponent(normalMeanLabel)
                                            .addGap(5, 5, 5)
                                            .addComponent(normalMeanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addGroup(priorPanelLayout.createSequentialGroup()
                                            .addComponent(normalSDLabel)
                                            .addGap(5, 5, 5)
                                            .addComponent(normalSDTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                                .addComponent(normalRadioButton, GroupLayout.Alignment.LEADING)))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(okButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                            .addComponent(cancelButton, GroupLayout.Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                    .addGroup(priorPanelLayout.createSequentialGroup()
                        .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(lognormalRadioButton)
                            .addGroup(priorPanelLayout.createSequentialGroup()
                                .addGap(21, 21, 21)
                                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addGroup(priorPanelLayout.createSequentialGroup()
                                        .addComponent(lognormalSDLabel)
                                        .addGap(5, 5, 5)
                                        .addComponent(lognormalSDTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGroup(priorPanelLayout.createSequentialGroup()
                                        .addComponent(lognormalMeanLabel)
                                        .addGap(5, 5, 5)
                                        .addComponent(lognormalMeanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))))
                            .addGroup(priorPanelLayout.createSequentialGroup()
                                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                    .addGroup(priorPanelLayout.createSequentialGroup()
                                        .addComponent(shape2Label)
                                        .addGap(5, 5, 5)
                                        .addComponent(shape2TextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addGroup(priorPanelLayout.createSequentialGroup()
                                            .addGap(21, 21, 21)
                                            .addComponent(shape1Label)
                                            .addGap(5, 5, 5)
                                            .addComponent(shape1TextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                        .addComponent(betaRadioButton)))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                    .addGroup(priorPanelLayout.createSequentialGroup()
                                        .addComponent(lowerBoundLabel)
                                        .addGap(5, 5, 5)
                                        .addComponent(lowerBoundTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                    .addGroup(priorPanelLayout.createSequentialGroup()
                                        .addComponent(upperBoundLabel)
                                        .addGap(5, 5, 5)
                                        .addComponent(upperBoundTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        priorPanelLayout.setVerticalGroup(
            priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(priorPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addGroup(priorPanelLayout.createSequentialGroup()
                        .addComponent(noPriorButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(normalRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(normalMeanLabel)
                            .addComponent(normalMeanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(normalSDLabel)
                            .addComponent(normalSDTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lognormalRadioButton)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lognormalMeanLabel)
                            .addComponent(lognormalMeanTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                            .addComponent(lognormalSDLabel)
                            .addComponent(lognormalSDTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(betaRadioButton))
                    .addGroup(priorPanelLayout.createSequentialGroup()
                        .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(shape1Label)
                    .addComponent(shape1TextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(lowerBoundLabel)
                    .addComponent(lowerBoundTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(priorPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(shape2Label)
                    .addComponent(shape2TextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(upperBoundLabel)
                    .addComponent(upperBoundTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(16, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(priorPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(priorPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();

    }

    public class OkActionListener implements ActionListener{

        public void actionPerformed(ActionEvent evt){

            String actionCommand = priorButtonGroup.getSelection().getActionCommand();

            if("noprior".equals(actionCommand)){
                priorString = "";

            }else if("normal".equals(actionCommand)){
                String temp1 = normalMeanTextField.getText().trim();
                String temp2 = normalSDTextField.getText().trim();
                if(!"".equals(temp1) && !"".equals(temp1)) {
                    priorString = "normal, " + temp1 + ", " + temp2;
                }

            }else if("lognormal".equals(actionCommand)){
                String temp1 = lognormalMeanTextField.getText().trim();
                String temp2 = lognormalSDTextField.getText().trim();
                if(!"".equals(temp1) && !"".equals(temp1)) {
                    priorString = "lognormal, " + temp1 + ", " + temp2;
                }

            }else if("beta".equals(actionCommand)){
                                String temp1 = shape1TextField.getText().trim();
                String temp2 = shape2TextField.getText().trim();
                if(!"".equals(temp1) && !"".equals(temp1)) {
                    priorString = "beta, " + temp1 + ", " + temp2 + ", ";
                }

                temp1 = lowerBoundTextField.getText().trim();
                temp2 = upperBoundTextField.getText().trim();

                if(!"".equals(temp1) && !"".equals(temp1)) {
                    priorString += temp1 + ", " + temp2;
                }else{
                    priorString += 0.0 + ", " + 1.0;
                }

            }

            setVisible(false);
        }

    }

}

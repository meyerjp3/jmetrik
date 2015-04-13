package com.itemanalysis.jmetrik.stats.irt.estimation;

import com.itemanalysis.jmetrik.commandbuilder.MegaOption;
import com.itemanalysis.jmetrik.model.VariableListModel;
import com.itemanalysis.psychometrics.data.VariableAttributes;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ItemGroupWizzard extends JDialog {

    // Variables declaration - do not modify
    private JPanel buttonPanel;
    private ItemGroupConfigurationPanel itemConfigurationPanel;
    private JButton cancelButton;
    private JPanel mainPanel;
    private JButton nextButton;
    private JButton okButton;
    private JButton previousButton;
    private JButton resetButton;
    // End of variables declaration

    private ArrayList<ItemGroupConfigurationPanel> groupPanels = null;
    private ItemGroupConfigurationPanel currentCard = null;
    private VariableListModel variableListModel = null;
    private int groupIndex = 1;
    private int maxGroup = 100;
    private int numberOfGroups = 0;
    private boolean canRun = false;
//    private IrtItemCalibrationCommand command = null;
    private ArrayList<VariableAttributes> selectedVariableMasterList = null;

    public ItemGroupWizzard(JDialog parent, VariableListModel variableListModel){
        super(parent, "Item Group Configuration", true);
        this.variableListModel = variableListModel;
//        this.command = new IrtItemCalibrationCommand();
        groupPanels = new ArrayList<ItemGroupConfigurationPanel>();
        selectedVariableMasterList = new ArrayList<VariableAttributes>();
        initComponents();

        setResizable(false);
        setLocationRelativeTo(parent);
    }


    private void initComponents() {

        mainPanel = new JPanel();
        itemConfigurationPanel = new ItemGroupConfigurationPanel(ItemGroupWizzard.this, variableListModel, selectedVariableMasterList);
        itemConfigurationPanel.setName("group1");
        currentCard = itemConfigurationPanel;
        groupPanels.add(itemConfigurationPanel);
        buttonPanel = new JPanel();
        nextButton = new JButton();
        previousButton = new JButton();
        resetButton = new JButton();
        okButton = new JButton();
        cancelButton = new JButton();

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Item Group Wizzard: Group " + groupIndex);
        setMinimumSize(new Dimension(800, 400));
        setPreferredSize(new Dimension(800, 400));

        mainPanel.setLayout(new CardLayout());
        mainPanel.add(itemConfigurationPanel, "card2");

        nextButton.setText("Next");
        nextButton.setMaximumSize(new Dimension(80, 28));
        nextButton.setMinimumSize(new Dimension(80, 28));
        nextButton.setPreferredSize(new Dimension(80, 28));
        nextButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                nextButtonActionPerformed(evt);
            }
        });

        previousButton.setText("Previous");
        previousButton.setMaximumSize(new Dimension(80, 28));
        previousButton.setMinimumSize(new Dimension(80, 28));
        previousButton.setPreferredSize(new Dimension(80, 28));
        previousButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                previousButtonActionPerformed(evt);
            }
        });

        resetButton.setText("Reset Page");
        resetButton.setMaximumSize(new Dimension(95, 28));
        resetButton.setMinimumSize(new Dimension(95, 28));
        resetButton.setPreferredSize(new Dimension(95, 28));
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentCard.resetPanel();
                updateListSelections();
            }
        });

        okButton.setText("OK");
        okButton.setMaximumSize(new Dimension(75, 28));
        okButton.setMinimumSize(new Dimension(75, 282));
        okButton.setPreferredSize(new Dimension(75, 28));
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                setVisible(false);

//                //If someone clicked OK without first clicking Next or Previous, check that current card
//                //only involves items with the same scoring. If not, do not run.
//                if(currentCard.hasSameNumberOfCategories()){
//                    ItemGroupConfigurationPanel card = null;
//                    MegaOption option = null;
//                    numberOfGroups=0;
//                    for (Component comp : mainPanel.getComponents() ) {
//                        if (comp instanceof ItemGroupConfigurationPanel) {
//                            card = (ItemGroupConfigurationPanel)comp;
//                            option = command.getOption(card.getName());
//                            if(card.setOption(option)) numberOfGroups++;
//                        }
//                    }
//                    canRun = true;
//                    setVisible(false);
//                }
            }
        });

        cancelButton.setText("Cancel");
        cancelButton.setMaximumSize(new Dimension(75, 28));
        cancelButton.setMinimumSize(new Dimension(75, 282));
        cancelButton.setPreferredSize(new Dimension(75, 28));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                canRun = false;
                setVisible(false);
            }
        });

        GroupLayout buttonPanelLayout = new GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previousButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nextButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(41, 41, 41))
        );
        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(GroupLayout.Alignment.TRAILING, buttonPanelLayout.createSequentialGroup()
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(buttonPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(nextButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(previousButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(resetButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(okButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(cancelButton, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(buttonPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(mainPanel, GroupLayout.PREFERRED_SIZE, 304, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(14, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>

    private void nextButtonActionPerformed(ActionEvent evt) {
        CardLayout cl = (CardLayout)(mainPanel.getLayout());

//        updateSelectedVariables();
        updateListSelections();

        boolean itemsRemaining = selectedVariableMasterList.size()!= variableListModel.getSize();

        int origSize = groupPanels.size();

        //Add new group panel if current panel is the last and the max number of panels is nto reached and some items remain to be selected.
        if(groupIndex==origSize && origSize<maxGroup && itemsRemaining){
            String groupName = "group" + (groupPanels.size()+1);
            ItemGroupConfigurationPanel groupConfigurationPanel = new ItemGroupConfigurationPanel(ItemGroupWizzard.this, variableListModel, selectedVariableMasterList);
            groupConfigurationPanel.setName(groupName);
            groupPanels.add(groupConfigurationPanel);
            mainPanel.add(groupConfigurationPanel, groupName);
//            System.out.println("ADDED");

        }

        //Advanced to next card if not at teh end and the selected items all have the same number of categories
        if(groupIndex<groupPanels.size() && currentCard.hasSameNumberOfCategories()){
            cl.next(mainPanel);
            currentCard = getCurrentCard();
            groupIndex++;
            setTitle("Item Group Wizzard: Group " + groupIndex);
//            System.out.println("ADVANCED TO NEXT CARD: " + currentCard.getName());
        }
    }

    private void updateListSelections(){
        ItemGroupConfigurationPanel card = null;
        JList<VariableAttributes> tempList;
        int[] selectedIndices;
        selectedVariableMasterList.clear();

        for (Component comp : mainPanel.getComponents() ) {
            if (comp instanceof ItemGroupConfigurationPanel) {
                card = (ItemGroupConfigurationPanel)comp;
                tempList = card.getItemList();
                selectedIndices = tempList.getSelectedIndices();
                for(int i=0;i<selectedIndices.length;i++){
                    selectedVariableMasterList.add(tempList.getModel().getElementAt(selectedIndices[i]));
                }

            }
        }
    }

//    private void updateSelectedVariables(){
//        currentCard.setSelectedVariables();
//    }

    private ItemGroupConfigurationPanel getCurrentCard(){
        ItemGroupConfigurationPanel card = null;
        for (Component comp : mainPanel.getComponents() ) {
            if (comp.isVisible() == true && comp instanceof ItemGroupConfigurationPanel) {
                card = (ItemGroupConfigurationPanel)comp;
            }
        }
        return card;
    }

    private void previousButtonActionPerformed(ActionEvent evt) {
        if(groupIndex>1 && currentCard.hasSameNumberOfCategories()){
            CardLayout cl = (CardLayout)(mainPanel.getLayout());
//            updateSelectedVariables();
            updateListSelections();
            cl.previous(mainPanel);
            currentCard = getCurrentCard();
            groupIndex--;
            setTitle("Item Group Wizzard: Group " + groupIndex);
        }
    }

//    public IrtItemCalibrationCommand getCommand(){
//        return command;
//    }

    public int getNumberOfGroups(){
        return numberOfGroups;
    }

    public void updateCommand(IrtItemCalibrationCommand command){
        //If someone clicked OK without first clicking Next or Previous, check that current card
        //only involves items with the same scoring. If not, do not run.
        if(currentCard.hasSameNumberOfCategories()){
            ItemGroupConfigurationPanel card = null;
            MegaOption option = null;
            numberOfGroups=0;
            for (Component comp : mainPanel.getComponents() ) {
                if (comp instanceof ItemGroupConfigurationPanel) {
                    card = (ItemGroupConfigurationPanel)comp;
                    option = command.getOption(card.getName());
                    if(card.setOption(option)) numberOfGroups++;
                }
            }
        }
    }


}

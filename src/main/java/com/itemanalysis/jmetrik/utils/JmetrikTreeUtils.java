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

package com.itemanalysis.jmetrik.utils;

import com.itemanalysis.jmetrik.sql.DatabaseName;
import com.itemanalysis.jmetrik.sql.DataTableName;

import javax.swing.*;
import javax.swing.tree.*;
import java.util.Enumeration;

public class JmetrikTreeUtils {

    private JTree tree = null;

    public JmetrikTreeUtils(JTree tree){
        this.tree = tree;
    }

//    public void removeDatabaseFromTree(DatabaseDescription db){
//        DefaultTreeModel m = (DefaultTreeModel)tree.getModel();
//        String[] names = {wkspDescription.getName() , db.getName()};
//        TreePath path = findNodeInTreeByName(tree, names);
//        MutableTreeNode node = (MutableTreeNode)path.getLastPathComponent();
//        m.removeNodeFromParent(node);
//    }

    public void removeTableFromTree(DatabaseName dbName, DataTableName tableName){
        DefaultTreeModel m = (DefaultTreeModel)tree.getModel();
        String[] names = {dbName.getName(), tableName.getTableName()};
        TreePath path = findNodeInTreeByName(names);
        MutableTreeNode node = (MutableTreeNode)path.getLastPathComponent();
        m.removeNodeFromParent(node);
    }

//    public TreePath findNodeInTreeByName(JTree tree, DefaultMutableTreeNode node) {
//        Object[] path = node.getUserObjectPath();
//
//    }
//
    public TreePath findNodeInTreeByName(String[] names) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();
        return findNodeInTree(new TreePath(root), names, 0, true);
    }

    private TreePath findNodeInTree(TreePath parent, Object[] nodes, int depth, boolean byName) {
        TreeNode node = (TreeNode)parent.getLastPathComponent();

        Object o = node;

        // If by name, convert node to a string
        if (byName) {
            o = o.toString();
        }

        // If equal, go down the branch
        if (o.equals(nodes[depth])) {
            // If at end, return match
            if (depth == nodes.length-1) {
                return parent;
            }

            // Traverse children
            if (node.getChildCount() >= 0) {
                for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                    TreeNode n = (TreeNode)e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    TreePath result = findNodeInTree(path, nodes, depth+1, byName);
                    // Found a match
                    if (result != null) {
                        return result;
                    }
                }
            }
        }

        // No match at this branch
        return null;
    }

    /**
     * There are only two levels in the tree: Root.table
     * Inserted nodes should be table.
     *
     * Nodes should already be tested for uniqueness because they
     * are database schema and tables.
     *
     * @param tableName
     */
    public void insertNode(DataTableName tableName){
        DefaultMutableTreeNode tableNode = new DefaultMutableTreeNode(tableName.getTableName(), false);
        insert(tableNode);
    }

    private void insert(DefaultMutableTreeNode node){
        TreeNode[] path = node.getPath();
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();

        //node is only a data table
        if(node.isLeaf()){
            model.insertNodeInto(node, root, root.getChildCount());

            //node is a schema with table name
        }else{
            boolean found = false;
            //children are schemas
            for(Enumeration e=root.children(); e.hasMoreElements();){
                DefaultMutableTreeNode child = (DefaultMutableTreeNode)e.nextElement();
                if(child.equals(path[1])){
                    model.insertNodeInto(node, child, child.getChildCount());
                    found = true;
                }
            }
            //schema name does not exist, insert schema and table node
            if(!found) model.insertNodeInto(node, root, root.getChildCount());
        }
    }

    private void removeNode(DefaultMutableTreeNode node){
        TreeNode[] path = node.getPath();
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        DefaultMutableTreeNode root = (DefaultMutableTreeNode)model.getRoot();
        model.removeNodeFromParent(node);


    }

}

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

package com.itemanalysis.jmetrik.dao;

import com.itemanalysis.jmetrik.commandbuilder.Command;
import com.itemanalysis.jmetrik.workspace.ImportCommand;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public interface DelimitedFileImporter {

    /**
     * Process input command
     */
    public void parseCommand()throws IllegalArgumentException;

    /**
     * Convert delimiter name to delimiter character
     * @param delimiterName
     * @return
     */
    public char getDelimiter(String delimiterName);

    /**
     * Converts first row of file into VariableInfo objects. If no row fileExists,
     * then it creates variables from scratch.
     *
     * @throws IOException
     */
    public String processHeader()throws IOException;

    /**
     * Import delimited file. Trans action should be started at teh beginning of this method and
     * it should be closed at the end of this method.
     *
     * @throws IOException
     * @throws SQLException
     */
    public void importDelimitedFile()throws IOException, SQLException;

    /**
     * Scan entire file to determine data types. This method is needed prior to
     * writeTempFile so that the temp file will correctly write strings and doubles.
     *
     * @param fileName
     * @throws IOException
     */
    public void scanFile(File fileName)throws IOException;

    /**
     * Writes a temporary file for use in import. The temp file should be readable
     * by the database and have the proper escape characters and quotes.
     *
     * @param scanFileName
     * @return
     * @throws IOException
     */
    public File writeTempFile(String scanFileName)throws IOException;

}

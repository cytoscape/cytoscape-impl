/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.data;

import java.io.FileWriter;
import java.io.IOException;

import de.mpg.mpi_inf.bioinf.netanalyzer.data.io.LineReader;

/**
 * Interface implemented by all types of complex parameters.
 * <p>
 * Complex network parameters are immutable collections of numbers. This interface specifies how the
 * parameters are saved to and loaded from text files. Methods for accessing the underlying data are
 * specific for every parameter and are not declared in this interface.
 * </p>
 * 
 * @author Yassen Assenov
 */
public interface ComplexParam {

	/**
	 * String used to separate values on one row when saving complex parameter to a text stream.
	 */
	public static final String SEP = "\t";

	/**
	 * Regular expression that matches the string {@link #SEP}.
	 * <p>
	 * This regular expression is used when a complex parameter is loaded from a text stream.
	 * </p>
	 */
	public static final String SEPREGEX = "\\t";

	/**
	 * Parameter types of the {@link #load(String[], LineReader)} method of this interface.
	 */
	public static final Class<?>[] loadParams = new Class[] { String[].class, LineReader.class };

	/**
	 * Loads the data of the parameter from the given stream.
	 * 
	 * @param aArgs Arguments passed to this complex parameter type. These arguments are specific
	 *        for every complex parameter; for more information look at the documentation of this
	 *        method in the implementing classes.
	 * @param aReader Reader from a text stream. The reader must be open and positioned in the
	 *        stream such that the data for the complex parameter follows.
	 * @throws IOException If I/O error occurs.
	 * @throws NumberFormatException If the stream contains invalid data.
	 * @throws NullPointerException If at least one of the parameters is <code>null</code>.
	 */
	public void load(String[] aArgs, LineReader aReader) throws IOException;

	/**
	 * Saves the data of the parameter in the given file.
	 * 
	 * @param aWriter Writer to a text stream. The writer must be open.
	 * @param aSaveArgs Flag indicating if type arguments must also be saved. Setting this parameter
	 *        to <code>true</code> enables reconstruction of the complex parameter from the stream
	 *        using the {@link #load(String[], LineReader)} method.
	 * @throws IOException If I/O error occurs.
	 */
	public void save(FileWriter aWriter, boolean aSaveArgs) throws IOException;
}

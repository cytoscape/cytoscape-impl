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

package de.mpg.mpi_inf.bioinf.netanalyzer.sconnect;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Utility class containing helping methods related to communications with the MPII server over HTTP.
 * 
 * @author Yassen Assenov
 */
public abstract class ServerUtils {

	/**
	 * Translates the given string to UTF-8 encoding.
	 * 
	 * @param aString String to be encoded.
	 * @return The string encoded in UTF-8.
	 * @throws UnsupportedEncodingException If the system does not support the UTF-8 encoding.
	 */
	public static String encode(String aString) throws UnsupportedEncodingException {
		return URLEncoder.encode(aString, "UTF-8");
	}
}

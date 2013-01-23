package de.mpg.mpi_inf.bioinf.netanalyzer.sconnect;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

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

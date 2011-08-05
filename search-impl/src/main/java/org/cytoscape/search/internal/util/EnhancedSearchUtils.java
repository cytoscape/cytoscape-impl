/*
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

 This library is free software; you can redistribute it and/or modify it
 under the terms of the GNU Lesser General Public License as published
 by the Free Software Foundation; either version 2.1 of the License, or
 any later version.

 This library is distributed in the hope that it will be useful, but
 WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 documentation provided hereunder is on an "as is" basis, and the
 Institute for Systems Biology and the Whitehead Institute
 have no obligations to provide maintenance, support,
 updates, enhancements or modifications.  In no event shall the
 Institute for Systems Biology and the Whitehead Institute
 be liable to any party for direct, indirect, special,
 incidental or consequential damages, including lost profits, arising
 out of the use of this software and its documentation, even if the
 Institute for Systems Biology and the Whitehead Institute
 have been advised of the possibility of such damage.  See
 the GNU Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public License
 along with this library; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package org.cytoscape.search.internal.util;


import java.util.regex.Pattern;

public class EnhancedSearchUtils {

	public static final String SEARCH_STRING = "\\s";

	public static final String REPLACE_STRING = "_";

	public static final String LOWERCASE_AND = " and ";
	public static final String LOWERCASE_OR = " or ";
	public static final String LOWERCASE_NOT = " not ";
	public static final String LOWERCASE_TO = " to ";
	public static final String UPPERCASE_AND = " AND ";
	public static final String UPPERCASE_OR = " OR ";
	public static final String UPPERCASE_NOT = " NOT ";
	public static final String UPPERCASE_TO = " TO ";

	/**
	 * Replaces whitespace characters with underline. Method: Search for
	 * SEARCH_STRING, replace with REPLACE_STRING.
	 */
	public static String replaceWhitespace(String searchTerm) {
		String replaceTerm = "";

		if (searchTerm == null){
			return replaceTerm;
		}
		
		Pattern searchPattern = Pattern.compile(SEARCH_STRING);
		
		String[] result = searchPattern.split(searchTerm);
		replaceTerm = result[0];
		for (int i = 1; i < result.length; i++) {
			replaceTerm = replaceTerm + REPLACE_STRING + result[i];
		}

		return replaceTerm;
	}

	/**
	 * This special lowercase handling turns a query string into lowercase,
	 * and logical operators (AND, OR, NOT) into uppercase.
	 */
	public static String queryToLowerCase (String queryString) {

		String lowercaseQuery;

		lowercaseQuery = queryString;
		lowercaseQuery = lowercaseQuery.toLowerCase();
		
		lowercaseQuery = lowercaseQuery.replaceAll(LOWERCASE_AND, UPPERCASE_AND);
		lowercaseQuery = lowercaseQuery.replaceAll(LOWERCASE_OR, UPPERCASE_OR);
		lowercaseQuery = lowercaseQuery.replaceAll(LOWERCASE_NOT, UPPERCASE_NOT);
		lowercaseQuery = lowercaseQuery.replaceAll(LOWERCASE_TO, UPPERCASE_TO);
		
		return lowercaseQuery;
	}


}

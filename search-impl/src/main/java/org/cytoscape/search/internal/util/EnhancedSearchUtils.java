package org.cytoscape.search.internal.util;

/*
 * #%L
 * Cytoscape Search Impl (search-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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



import java.util.regex.Pattern;

public final class EnhancedSearchUtils {

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
	
	private EnhancedSearchUtils() {		
	}
}

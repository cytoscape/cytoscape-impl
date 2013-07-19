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


import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.NumericUtils;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.util.Version;
import org.apache.lucene.search.NumericRangeQuery;
import org.cytoscape.search.internal.util.AttributeFields;



/**
 * This custom MultiFieldQueryParser is used to parse queries containing numerical values.
 * Lucene treats all attribute field values as strings. During indexing, numerical values were transformed
 * into structured strings preserving their numerical sorting order. Now, numerical values in query
 * should also be transformed so they can be properly compared to the values stored in the index.
 */
public class CustomMultiFieldQueryParser extends MultiFieldQueryParser {

	private AttributeFields attrFields;

	public CustomMultiFieldQueryParser(AttributeFields attrFields,
			StandardAnalyzer analyzer) {
		super(Version.LUCENE_30,attrFields.getFields(), analyzer);
		analyzer.setMaxTokenLength(1024*10);  // Increase for sequences
		this.attrFields = attrFields;
		setAllowLeadingWildcard(true);
	}

	protected Query getFieldQuery(String field, String queryText)
			throws ParseException {
		
		if (attrFields.getType(field) == Integer.class) {
			try {
				int num1 = Integer.parseInt(queryText);
				return super.getFieldQuery(field, NumericUtils.longToPrefixCoded(num1));
			} catch (NumberFormatException e) {
				// Do nothing. When using a MultiFieldQueryParser, queryText is
				// searched in each one of the fields. This exception occurs
				// when trying to convert non-numeric queryText into numeric.
				// throw new ParseException(e.getMessage());
			}

		} else if (attrFields.getType(field) == Double.class) {
			try {
				double num1 = Double.parseDouble(queryText);
				
				Query q = NumericRangeQuery.newDoubleRange(field, num1, num1, true, true);
				return q;
				
			} catch (NumberFormatException e) {
				// Do nothing. When using a MultiFieldQueryParser, queryText is
				// searched in each one of the fields. This exception occurs
				// when trying to format String to numerical.
				// throw new ParseException(e.getMessage());
			}
		}
		
		// Look to see if the leading character of our query is "?" or "*".  If so, issue
		// a warning, but do it anyways
		if (queryText.charAt(0) == '?' || queryText.charAt(0) == '*') {
			System.out.println("Returning the wildcard query");
			return super.getWildcardQuery(field, queryText);
		}
		return super.getFieldQuery(field, queryText);
	}

	protected Query getRangeQuery(String field, String part1, String part2,
			boolean inclusive) throws ParseException {
		
		// a workaround to avoid a TooManyClauses exception.
		// Temporary until RangeFilter is implemented.
		BooleanQuery.setMaxClauseCount(5120); // 5 * 1024		
		
		if (attrFields.getType(field) == Integer.class) {
			try {
				
				int num1 = Integer.parseInt(part1);
				int num2 = Integer.parseInt(part2);

				Query q = NumericRangeQuery.newIntRange(field, num1, num2, inclusive, inclusive);

				return q;
			} catch (NumberFormatException e) {
				throw new ParseException(e.getMessage());
			}
		}
		if (attrFields.getType(field) == Double.class) {
			try {
				double num1 = Double.parseDouble(part1);
				double num2 = Double.parseDouble(part2);
				
				Query q = NumericRangeQuery.newDoubleRange(field, num1, num2, inclusive, inclusive);
				return q;
				
			} catch (NumberFormatException e) {
				throw new ParseException(e.getMessage());
			}
		}

		return super.getRangeQuery(field, part1, part2, inclusive);
	}
}

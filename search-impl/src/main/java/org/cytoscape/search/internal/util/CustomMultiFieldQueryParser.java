
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

import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.analysis.Analyzer;
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
			Analyzer analyzer) {
		super(Version.LUCENE_30,attrFields.getFields(), analyzer);
		this.attrFields = attrFields;
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

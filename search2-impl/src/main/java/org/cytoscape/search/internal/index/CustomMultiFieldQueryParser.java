package org.cytoscape.search.internal.index;

import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.DoublePoint;
import org.apache.lucene.document.IntPoint;
import org.apache.lucene.document.LongPoint;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class CustomMultiFieldQueryParser extends MultiFieldQueryParser {

	private final AttributeFields fields;
	
	public CustomMultiFieldQueryParser(AttributeFields fields, Analyzer analyzer) {
		super(fields.getFields(), analyzer);
		this.fields = fields;
		setAllowLeadingWildcard(true);
	}
	
	@Override
	public Query parse(String queryText) throws ParseException {
		queryText = queryToLowerCase(queryText);
		Query query = super.parse(queryText);
		return new ConstantScoreQuery(query);
	}
	
	/**
	 * We want index fields to be case-insensitive. 
	 * In order to achieve that in a query we have to lower case the entire query.
	 */
	private static String queryToLowerCase(String query) {
		return query.toLowerCase()
			.replaceAll(" and ", " AND ")
			.replaceAll(" or ", " OR ")
			.replaceAll(" not ", " NOT ")
			.replaceAll(" to ", " TO ");
	}
	
	/**
	 * Support for numeric queries.
	 */
	@Override
	protected Query getFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
		// field == null means its a multi-field query
		if(field != null) {
			Set<String> names = fields.getAltNames(field);
			if(names.size() == 1) {
				String name = names.iterator().next();
				return computeExactFieldQuery(name, queryText, quoted);
			} else {
				var builder = new BooleanQuery.Builder();
				for(String name : names) {
					var query = computeExactFieldQuery(name, queryText, quoted);
					builder.add(query, BooleanClause.Occur.SHOULD);
				}
				return builder.build();
			}
		}
		return super.getFieldQuery(field, queryText, quoted);
	}
	
	
	@Override
	protected Query newFieldQuery(Analyzer analyzer, String field, String queryText, boolean quoted) throws ParseException {
		Class<?> type = fields.getType(field);
		
		// Boolean attributes are indexed using StringTerm containing "true" or "false"
		// and cannot be searched by PhraseQuery (CYTOSCAPE-13045)
		if(type == Boolean.class) {
			if(queryText.equalsIgnoreCase("true") || queryText.equalsIgnoreCase("false")) {
				return new TermQuery(new Term(field, queryText.toLowerCase()));
			}
			return null;
		}
		
		return super.newFieldQuery(analyzer, field, queryText, quoted);
	}
	
	
	private Query computeExactFieldQuery(String field, String queryText, boolean quoted) throws ParseException {
		Class<?> type = fields.getType(field);
		try {
			if(type == Integer.class) {
				int num = Integer.parseInt(queryText);
				return IntPoint.newExactQuery(field, num);
			}
			if(type == Long.class) {
				long num = Long.parseLong(queryText);
				return LongPoint.newExactQuery(field, num);
			}
			if(type == Double.class) {
				double num = Double.parseDouble(queryText);
				return DoublePoint.newExactQuery(field, num);
			}
		} catch(NumberFormatException e) {
		}
		
		return super.getFieldQuery(field, queryText, quoted);
	}
	
	
	/**
	 * Support for numeric range queries.
	 */
	@Override
	protected Query getRangeQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive) throws ParseException {
		// field == null means its a multi-field query
		if(field != null) {
			Set<String> names = fields.getAltNames(field);
			if(names.size() == 1) {
				String name = names.iterator().next();
				return computeRangeFieldQuery(name, part1, part2, startInclusive, endInclusive);
			} else {
				var builder = new BooleanQuery.Builder();
				for(String name : names) {
					var query = computeRangeFieldQuery(name, part1, part2, startInclusive, endInclusive);
					builder.add(query, BooleanClause.Occur.SHOULD);
				}
				return builder.build();
			}
		}
		return super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);
	}
	
	
	private Query computeRangeFieldQuery(String field, String part1, String part2, boolean startInclusive, boolean endInclusive) throws ParseException {
		Class<?> type = fields.getType(field);
		try {
			if(type == Integer.class) {
				int num1 = Integer.parseInt(part1);
				int num2 = Integer.parseInt(part2);
				if(!startInclusive)
					num1++;
				if(!endInclusive)
					num2--;
				return IntPoint.newRangeQuery(field, num1, num2);
			}
			if(type == Long.class) {
				long num1 = Long.parseLong(part1);
				long num2 = Long.parseLong(part2);
				if(!startInclusive)
					num1++;
				if(!endInclusive)
					num2--;
				return LongPoint.newRangeQuery(field, num1, num2);
			}
			if(type == Double.class) {
				double num1 = Double.parseDouble(part1);
				double num2 = Double.parseDouble(part2);
				if(!startInclusive)
					num1 = Math.nextUp(num1);
				if(!endInclusive)
					num2 = Math.nextDown(num2);
				return DoublePoint.newRangeQuery(field, num1, num2);
			}
		} catch(NumberFormatException e) {
			throw new ParseException(e.getMessage());
		}
		return super.getRangeQuery(field, part1, part2, startInclusive, endInclusive);
	}
	
}

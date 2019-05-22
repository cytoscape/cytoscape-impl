package org.cytoscape.search.internal.util;

import java.io.Reader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.WhitespaceTokenizer;

public class CaseInsensitiveWhitespaceAnalyzer extends Analyzer {

	@Override
	public TokenStream tokenStream(String fieldName, Reader reader) {
		return new LowerCaseFilter(new WhitespaceTokenizer(reader));
	}

}

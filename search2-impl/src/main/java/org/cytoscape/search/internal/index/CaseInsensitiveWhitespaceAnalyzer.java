package org.cytoscape.search.internal.index;

import org.apache.lucene.analysis.Analyzer;
//import org.apache.lucene.analysis.WhitespaceTokenizer;

public class CaseInsensitiveWhitespaceAnalyzer extends Analyzer {

//	@Override
//	public TokenStream tokenStream(String fieldName, Reader reader) {
//		return new LowerCaseFilter(new WhitespaceTokenizer(reader));
//	}

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

}

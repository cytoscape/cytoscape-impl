package org.cytoscape.search.internal.index;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.WhitespaceTokenizer;

public class CaseInsensitiveWhitespaceAnalyzer extends Analyzer {

	@Override
	protected TokenStreamComponents createComponents(String fieldName) {
		Tokenizer tokenizer = new WhitespaceTokenizer();
		TokenStream tokenStream = new LowerCaseFilter(tokenizer);
		return new TokenStreamComponents(tokenizer::setReader, tokenStream);
	}

	@Override
	protected TokenStream normalize(String fieldName, TokenStream tokenStream) {
		return new LowerCaseFilter(tokenStream);
	}

}

package org.cytoscape.search.internal.index;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.cytoscape.model.CyTable;
import org.cytoscape.search.internal.search.AttributeFields;
import org.cytoscape.search.internal.search.CustomMultiFieldQueryParser;

public class Index {
	
	private final Long tableSuid;
	private final Path indexPath;
	private final TableType type;
	
	private IndexWriter indexWriter;
	
	
	public Index(Long tableSuid, TableType type, Path indexPath) {
		this.tableSuid = tableSuid;
		this.type = type;
		this.indexPath = indexPath;
	}
	
	public Long getTableSUID() {
		return tableSuid;
	}
	
	public TableType getTableType() {
		return type;
	}
	
	private IndexWriterConfig getIndexWriterConfig(OpenMode openMode) {
		Analyzer analyzer = new CaseInsensitiveWhitespaceAnalyzer();
		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
		iwc.setOpenMode(openMode);
		return iwc;
	}
	
	public IndexWriter getWriter() throws IOException {
		if(indexWriter == null) {
			Directory dir = FSDirectory.open(indexPath);
			IndexWriterConfig iwc = getIndexWriterConfig(OpenMode.CREATE_OR_APPEND);
			indexWriter = new IndexWriter(dir, iwc);
		}
		return indexWriter;
	}
	
	public IndexReader getIndexReader() throws IOException {
		IndexWriter writer = getWriter();
		return DirectoryReader.open(writer, true, false);
	}
	
	public QueryParser getQueryParser(CyTable table) {
		var analyser = new CaseInsensitiveWhitespaceAnalyzer();
		var fields = new AttributeFields(table);
		var parser = new CustomMultiFieldQueryParser(fields, analyser);
		return parser;
	}

}

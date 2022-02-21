package org.cytoscape.search.internal.index;

import java.io.IOException;
import java.nio.file.Path;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Index {
	
	private final Long suid;
	private final Path indexPath;
	private IndexWriter indexWriter;

	public Index(Long suid, Path indexPath) {
		this.suid = suid;
		this.indexPath = indexPath;
	}
	
	public Long getTableSUID() {
		return suid;
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
		return DirectoryReader.open(writer);
		
//		Directory directory = FSDirectory.open(indexPath);
//		IndexReader reader = DirectoryReader.open(directory);
//		return reader;
	}

}

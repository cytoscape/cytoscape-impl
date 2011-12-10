package org.cytoscape.app.internal.ui;

import java.io.IOException;
import java.util.Vector;
import java.util.HashMap;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.cytoscape.app.internal.DownloadableInfo;
import org.cytoscape.app.internal.AppInfo;
import org.cytoscape.app.internal.ThemeInfo;

import java.util.List;

public class AppIndex {

    private static StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_30);

	private static Vector indices = new Vector();
	private static HashMap<String,Directory> indexTracker = new HashMap<String,Directory>();
	private static HashMap<String,Vector> appVectTracker = new HashMap<String,Vector>();
	
	private static Vector allAppVect;
		
	public static void setAllAppVector( Vector allAppVect,  boolean versionCheck, String downloadLocText  ) throws Exception {

		String index_id = downloadLocText.trim() + Boolean.toString(versionCheck);

		appVectTracker.put(index_id, allAppVect);
		
		// build an index for this id
	    Directory index = new RAMDirectory();

	    // the boolean arg in the IndexWriter ctor means to
	    // create a new index, overwriting any existing index
	    IndexWriter w = new IndexWriter(index, analyzer, true, IndexWriter.MaxFieldLength.UNLIMITED);
	    
	    for (int i=0; i<allAppVect.size(); i++ ){
	    	Vector aApp = (Vector) allAppVect.elementAt(i);

	    	if (aApp.elementAt(2) instanceof AppInfo){
		    	AppInfo info = (AppInfo) aApp.elementAt(2);
		    	
		    	List<AppInfo.AuthorInfo> authorInfo = info.getAuthors();
		    	String authorNames = "";
		    	String institutionNames = "";
		    	for (int j=0; j<authorInfo.size(); j++){
		    		AppInfo.AuthorInfo oneAuthor = authorInfo.get(j);
		    		authorNames += " " + oneAuthor.getAuthor();
		    		institutionNames += " " + oneAuthor.getInstitution();
		    	}
		    	String wholeText = info.getName() + " " + info.getDescription() + " "+ authorNames + " "+ institutionNames;
		    	
		    	addDoc(w, wholeText, "default");	    		
	    	}
	    	else {
	    		// This could be a themeInfo
	    		DownloadableInfo  downloadableInfo = (DownloadableInfo) aApp.elementAt(2);
	    		addDoc(w, downloadableInfo.getDescription(), "default");
	    	}
	    }
	    
	    w.close();
	    
	    indexTracker.put(index_id, index);
		
	}
	  
	private static void addDoc(IndexWriter w, String value, String title) throws IOException {
		Document doc = new Document();
		doc.add(new Field(title, value, Field.Store.YES, Field.Index.ANALYZED));
		w.addDocument(doc);
	}

	
	public static Vector getSearchResult(String querystr, boolean versionCheck, String downloadLocText ) throws Exception {

		String index_id = downloadLocText.trim() + Boolean.toString(versionCheck);
		
		// Check if the index for this case already existed
		if (!indexTracker.containsKey(index_id)){
			// The index does not exist, so we can not do search, just return null
			return null;
		}
		
		// The index does exist, do the search now
		Directory index = (Directory) indexTracker.get(index_id);
				
		// Add  wildcard characters to queryStr at beginning and end 
		querystr = querystr.trim();
		if (!querystr.startsWith("*")){
			querystr = "*"+querystr;
		}
		if (!querystr.endsWith("*")){
			querystr = querystr+ "*";
		}
		
		// The index does exist, do the search now
		QueryParser qp = new QueryParser(Version.LUCENE_30, "default", analyzer);
		qp.setAllowLeadingWildcard(true); 
	    Query q = qp.parse(querystr);

	    // search
	    int hitsPerPage = 1000;
	    IndexSearcher searcher = new IndexSearcher(index, true);
	    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
	    searcher.search(q, collector);
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;

	    // Retrive all the apps for this situation
	    Vector appVect = appVectTracker.get(index_id);
		
	    // This will hold the filered set of apps 
	    Vector filteredAppVector = new Vector();
	    
	    for(int i=0;i<hits.length;++i) {
	      int docId = hits[i].doc;
	      filteredAppVector.add(appVect.elementAt(docId));	      
	    }
		
		return filteredAppVector;
	}
}

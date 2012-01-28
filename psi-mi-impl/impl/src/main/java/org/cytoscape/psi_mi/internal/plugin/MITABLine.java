package org.cytoscape.psi_mi.internal.plugin; 

import java.util.List;
import java.util.ArrayList;

// There are 15 tab separated columns.  Columns can be further separated by '|' and sub-columns
// can be further separated by ':'
// These are the columns
// 
// 0   srcAlias:sourceRawId|srcAlias:srcAlias  
// 1   tgtAlias:targetRawId|tgtAlias:tgtAlias  
// 2   srcAlias:srcAlias|srcAlias:srcAlias  
// 3   tgtAlias:tgtAlias|tgtAlias:tgtAlias
// 4   srcAlias:srcAlias|srcAlias:srcAlias  
// 5   tgtAlias:tgtAlias|tgtAlias:tgtAlias
// 6   detectionMethod|detectionMethod
// 7   authors|authors
// 8   publicationIDKey:publicationIDValue|publicationIDKey:publicationIDValue
// 9   srcAttrName:srcTaxonName|XXXX:XXXX
// 10  tgtAttrName:tgtTaxonName|XXXX:XXXX
// 11  interactionType|interactionType
// 12  sourceDB|sourceDB  
// 13  interactionID|XXXX
// 14  edgeScoreType:edgeScoreString|edgeScoreType:edgeScoreString

public class MITABLine {

	final char COLON = ':';
	final char PIPE = '|';
	final char TAB = '	';

	String sourceRawID = "";
	String targetRawID = "";
	String srcAttrName = "";
	String srcTaxonName = "";
	String tgtAttrName = "";
	String tgtTaxonName = "";
	String interactionID = "";

	List<String> srcAliases = new ArrayList<String>(20);
	List<String> tgtAliases = new ArrayList<String>(20);
	List<String> authors = new ArrayList<String>(20);
	List<String> detectionMethods = new ArrayList<String>(20);
	List<String> publicationIDs = new ArrayList<String>(20);
	List<String> publicationValues = new ArrayList<String>(20);
	List<String> sourceDBs = new ArrayList<String>(20);
	List<String> interactionTypes = new ArrayList<String>(20);
	List<String> edgeScoreTypes = new ArrayList<String>(20);
	List<String> edgeScoreStrings = new ArrayList<String>(20);

	int colon = 0; 
	int tab = 0; 
	int pipe = 0; 

	private void init() {
		sourceRawID = "";
		targetRawID = "";
		srcAttrName = "";
		srcTaxonName = "";
		tgtAttrName = "";
		tgtTaxonName = "";
		interactionID = "";
		colon = 0; 
		tab = 0; 
		pipe = 0; 
		srcAliases.clear(); 
		tgtAliases.clear();
		authors.clear();
		detectionMethods.clear();
		publicationIDs.clear();
		publicationValues.clear();
		sourceDBs.clear();
		interactionTypes.clear();
		edgeScoreTypes.clear();
		edgeScoreStrings.clear();
	}

	private int nextIndex(String s, int start) {
		colon = s.indexOf(COLON, start);
		pipe = s.indexOf(PIPE, start);
		tab = s.indexOf(TAB, start);
		return Math.min(colon, Math.min(pipe,tab));
	}

	void readLine(String line) {
		init();

		int begin = 0; 
		int end = 0; 

		// column 0
		// get first source alias
		end = nextIndex(line,0);	
		srcAliases.add(line.substring(begin,end));
		begin = end+1;

		// get sourceRawID
		end = nextIndex(line,begin);	
		sourceRawID = line.substring(begin,end);
		begin = end+1;

		// get any additional source aliases
		do { 
			end = nextIndex(line,begin);
			srcAliases.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );

		// column 1
		// get first target alias
		end = nextIndex(line,begin);
		tgtAliases.add(line.substring(begin,end));
		begin = end+1;

		// get targetRawID
		end = nextIndex(line,begin);	
		targetRawID = line.substring(begin,end);
		begin = end+1;

		// get any additional target aliases
		do { 
			end = nextIndex(line,begin);
			tgtAliases.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );

		// column 2
		// get any additional source aliases
		do { 
			end = nextIndex(line,begin);
			srcAliases.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );

		// column 3
		// get any additional target aliases
		do { 
			end = nextIndex(line,begin);
			tgtAliases.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );

		// column 4
		// get any additional source aliases
		do { 
			end = nextIndex(line,begin);
			srcAliases.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );

		// column 5
		// get any additional target aliases
		do { 
			end = nextIndex(line,begin);
			tgtAliases.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );
		
		// column 6
		// get any detection methods 
		do { 
			end = nextIndex(line,begin);
			detectionMethods.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );

		// column 7
		// get any authors 
		do { 
			end = nextIndex(line,begin);
			authors.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );

		// column 8
		// get any additional publications 
		do { 
			end = nextIndex(line,begin);
			publicationIDs.add(line.substring(begin,end));
			begin = end+1;
			end = nextIndex(line,begin);
			publicationValues.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );

		// column 9
		// get source taxon 
		end = nextIndex(line,begin);
		srcAttrName = line.substring(begin,end);
		begin = end+1;

		end = nextIndex(line,begin);
		srcTaxonName = line.substring(begin,end);
		begin = end+1;

		// skip anything else in this column
		do { 
			end = nextIndex(line,begin);
			begin = end+1;
		} while ( end != tab );

		// column 10 
		// get target taxon 
		end = nextIndex(line,begin);
		tgtAttrName = line.substring(begin,end);
		begin = end+1;

		end = nextIndex(line,begin);
		tgtTaxonName = line.substring(begin,end);
		begin = end+1;

		// skip anything else in this column
		do { 
			end = nextIndex(line,begin);
			begin = end+1;
		} while ( end != tab );

		// column 11 
		// get any interaction types 
		do { 
			end = nextIndex(line,begin);
			interactionTypes.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );

		// column 12 
		// get any source databases 
		do { 
			end = nextIndex(line,begin);
			sourceDBs.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );

		// column 13 
		// get interaction ID 
		end = nextIndex(line,begin);
		interactionID = line.substring(begin,end);
		begin = end+1;

		// skip anything else in this column
		do { 
			end = nextIndex(line,begin);
			begin = end+1;
		} while ( end != tab );

		// column 14 
		// get edge scores 
		do { 
			end = nextIndex(line,begin);
			edgeScoreTypes.add(line.substring(begin,end));
			begin = end+1;
			end = nextIndex(line,begin);
			edgeScoreStrings.add(line.substring(begin,end));
			begin = end+1;
		} while ( end != tab );
	}

	public void print() {
		System.out.println("sourceRawID: " + sourceRawID);
		System.out.println("targetRawID: " + targetRawID);
		System.out.println("srcAttrName: " + srcAttrName);
		System.out.println("srcTaxonName: " + srcTaxonName);
		System.out.println("tgtAttrName: " + tgtAttrName);
		System.out.println("tgtTaxonName: " + tgtTaxonName);
		System.out.println("interactionID: " + interactionID);
		printList("srcAliases", srcAliases);
		printList("tgtAliases", tgtAliases);
		printList("authors", authors);
		printList("detectionMethods", detectionMethods);
		printList("publicationIDs", publicationIDs);
		printList("publicationValues", publicationValues);
		printList("sourceDBs", sourceDBs);
		printList("interactionTypes", interactionTypes);
		printList("edgeScoreTypes", edgeScoreTypes);
		printList("edgeScoreStrings", edgeScoreStrings);
		System.out.println();
		System.out.println();
	}

	private void printList(String name, List<String> vals) {
		System.out.print(name + ": ");
		for ( String s : vals )
			System.out.print(s + ", ");
		System.out.println();
	}
}


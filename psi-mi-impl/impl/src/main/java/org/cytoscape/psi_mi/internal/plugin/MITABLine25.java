package org.cytoscape.psi_mi.internal.plugin;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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


import java.util.List;
import java.util.ArrayList;

// There are 15 tab separated columns.  Columns can be further separated by '|' and sub-columns
// can be further separated by ':'
// These are the columns
// 
// 0   srcDB:sourceRawId|srcDB:srcAlias  
// 1   tgtDB:targetRawId|tgtDB:tgtAlias  
// 2   srcDB:srcAlias|srcDB:srcAlias  
// 3   tgtDB:tgtAlias|tgtDB:tgtAlias
// 4   srcDB:srcAlias|srcDB:srcAlias  
// 5   tgtDB:tgtAlias|tgtDB:tgtAlias
// 6   detectionDB:detectionMethod|detectionDB:detectionMethod
// 7   authors|authors
// 8   publicationIDKey:publicationIDValue|publicationIDKey:publicationIDValue
// 9   srcTaxonDB:srcTaxonName|srcTaxonDB:srcTaxonName
// 10  tgtTaxonDB:tgtTaxonName|tgtTaxonDB:tgtTaxonName
// 11  interactionType|interactionType
// 12  sourceDB|sourceDB  
// 13  interactionID|XXXX
// 14  edgeScoreType:edgeScoreString|edgeScoreType:edgeScoreString
// subsequent optional columns are ignored!
// 
// For a better description see: http://wiki.reactome.org/index.php/PSI-MITAB_interactions
//

/**
 * This class quickly reads a single line of PSI-MI Tab delimited format into a data
 * structure for easy processing. 
 * 
 * This is a PSIMITAB 25 format.
 */
public class MITABLine25 {

	private static final char COLON = ':';
	private static final char PIPE = '|';
	private static final char TAB = '	';
	private static final char QUOTE = '"';

	String sourceRawID = "";
	String targetRawID = "";

	final List<String> srcAliases = new ArrayList<String>(10);
	final List<String> srcDBs = new ArrayList<String>(10);

	final List<String> tgtAliases = new ArrayList<String>(10);
	final List<String> tgtDBs = new ArrayList<String>(10);

	final List<String> authors = new ArrayList<String>(5);

	final List<String> detectionMethods = new ArrayList<String>(5);
	final List<String> detectionDBs = new ArrayList<String>(5);

	final List<String> publicationValues = new ArrayList<String>(5);
	final List<String> publicationDBs = new ArrayList<String>(5);

	final List<String> srcTaxonDBs = new ArrayList<String>(5);
	final List<String> srcTaxonIDs = new ArrayList<String>(5);

	final List<String> tgtTaxonDBs = new ArrayList<String>(5);
	final List<String> tgtTaxonIDs = new ArrayList<String>(5);

	final List<String> sourceIDs = new ArrayList<String>(5);
	final List<String> sourceDBs = new ArrayList<String>(5);

	final List<String> interactionTypes = new ArrayList<String>(5);
	final List<String> interactionTypeDBs = new ArrayList<String>(5);

	final List<String> edgeScoreTypes = new ArrayList<String>(5);
	final List<String> edgeScoreStrings = new ArrayList<String>(5);

	final List<String> interactionIDs = new ArrayList<String>(5);
	List<String> interactionDBs = new ArrayList<String>(5);

	private int colon = 0; 
	private int tab = 0; 
	private int pipe = 0; 
	private int begin = 0; 
	private int end = 0; 

	private void init() {
		sourceRawID = "";
		targetRawID = "";
		colon = 0; 
		tab = 0; 
		pipe = 0; 
		begin = 0;
		end = 0;
		srcAliases.clear(); 
		tgtAliases.clear();
		authors.clear();
		detectionMethods.clear();
		detectionDBs.clear();
		publicationDBs.clear();
		publicationValues.clear();
		srcTaxonIDs.clear();
		srcTaxonDBs.clear();
		tgtTaxonIDs.clear();
		tgtTaxonDBs.clear();
		sourceIDs.clear();
		sourceDBs.clear();
		interactionTypes.clear();
		interactionTypeDBs.clear();
		edgeScoreTypes.clear();
		edgeScoreStrings.clear();
		interactionIDs.clear();
		interactionDBs.clear();
	}


	public void readLine(final String line) {
		init();

		// column 0
		// get first source DB
		srcDBs.add(nextString(line));

		// get sourceRawID
		sourceRawID = nextString(line); 
		srcAliases.add(sourceRawID);

		// get any additional source aliases from col 0
		addNextPairs("additional src aliases", srcDBs, srcAliases, line );

		// column 1
		// get first target db
		tgtDBs.add(nextString(line));

		// get targetRawID
		targetRawID = nextString(line); 
		tgtAliases.add(targetRawID);

		// get any additional target aliases from col 1
		addNextPairs("additional tgt aliases", tgtDBs, tgtAliases, line);

		// column 2
		// get any additional source aliases
		addNextPairs("col 2 src", srcDBs, srcAliases, line );

		// column 3
		// get any additional target aliases
		addNextPairs("col 3 tgt", tgtDBs, tgtAliases, line);

		// column 4
		// get any additional source aliases
		addNextPairs("col 4 src", srcDBs, srcAliases, line );

		// column 5
		// get any additional target aliases
		addNextPairs("col 5 tgt", tgtDBs, tgtAliases, line);
		
		// column 6
		// get any detection methods 
		addNextPairs("detection", detectionDBs, detectionMethods, line);

		// column 7
		// get any authors 
		addNextValues("authors",authors,line);

		// column 8
		// get any additional publications 
		addNextPairs("publications", publicationDBs, publicationValues, line);

		// column 9
		// get source taxon 
		addNextPairs("src taxon", srcTaxonDBs, srcTaxonIDs, line);

		// column 10 
		// get target taxon 
		addNextPairs("tgt taxon", tgtTaxonDBs, tgtTaxonIDs, line);

		// column 11 
		// get any interaction types 
		addNextPairs("interaction", interactionTypeDBs, interactionTypes, line);

		// column 12 
		// get any source databases 
		addNextPairs("source", sourceDBs,sourceIDs,line);

		// column 13 
		// get interaction ID 
		addNextPairs("interaction IDs", interactionDBs, interactionIDs, line );

		// column 14 
		// get edge scores 
		addNextPairs("edge scores", edgeScoreTypes, edgeScoreStrings, line);
	}

	// just for debugging
	public void print() {
		System.out.println("sourceRawID: " + sourceRawID);
		System.out.println("targetRawID: " + targetRawID);
		printList("srcAliases", srcAliases);
		printList("tgtAliases", tgtAliases);
		printList("detectionDBs", detectionDBs);
		printList("detectionMethods", detectionMethods);
		printList("authors", authors);
		printList("publicationDBs", publicationDBs);
		printList("publicationValues", publicationValues);
		printList("sourceDBs", sourceDBs);
		printList("sourceIDs", sourceIDs);
		printList("interactionTypes", interactionTypes);
		printList("interactionTypeDBs", interactionTypeDBs);
		printList("interactionIDs", interactionIDs);
		printList("interactionDBs", interactionDBs);
		printList("edgeScoreTypes", edgeScoreTypes);
		printList("edgeScoreStrings", edgeScoreStrings);
		System.out.println();
		System.out.println();
	}

	// just for debugging
	public void printList(String name, List<String> vals) {
		System.out.print(name + ": ");
		for ( String s : vals )
			System.out.print("'" + s + "', ");
		System.out.println();
	}

	private String nextString(final String line) {
		end = nextIndex(line,begin);
		if ( (begin > end) || (begin > line.length() - 1))
			return "";

		String ret = line.substring(begin,end);

		// This is an attempt to handle quoted strings, which may
		// include our tokenizing characters! Basically, if
		// we see a quote, make sure we get a close quote too!
		int openQuote = ret.indexOf(QUOTE);
		if ( openQuote >= 0 ) {
			int closeQuote = ret.indexOf(QUOTE,openQuote+1);
			if ( closeQuote < 0 ) {
				end = nextIndex(line,end+1);
				ret = line.substring(begin,end);
			} 
		}

		begin = end+1;
		return ret;
	}

	private void addNextValues(String desc,List<String> values, String line) {
		do { 
			authors.add(nextString(line));
		} while ( end != tab );
	}

	private void addNextPairs(String desc, List<String> dbs, List<String> values, String line) {
		do {
			String db = nextString(line);
			//System.out.println("      next db string: '" + db + "'");

			// make sure the first column is valid before continuing
			if ( db.equals("") || db.equals("-") ) {
				//System.out.println("      got invalid col: " + db);
				return;
			}
			dbs.add(db);

			String val = nextString(line);
			//System.out.println("      next val string: '" + val + "'");
			values.add(val);
		} while ( end != tab );
	}

	private int nextIndex(String s, int start) {
		colon = s.indexOf(COLON, start);
		if ( colon < 0 ) colon = s.length() - 1;

		pipe = s.indexOf(PIPE, start);
		if ( pipe < 0 ) pipe = s.length() - 1;

		tab = s.indexOf(TAB, start);
		if ( tab < 0 ) tab = s.length() - 1;

		int ind =  Math.min(colon, Math.min(pipe,tab));

		return ind; 
	}

	// just for debugging!
	private int peekNextIndex(String s, int start) {
		int x, y, z = 0;
		x = s.indexOf(COLON, start);
		if ( x < 0 ) x = s.length() - 1;

		y = s.indexOf(PIPE, start);
		if ( y < 0 ) y = s.length() - 1;

		z = s.indexOf(TAB, start);
		if ( z < 0 ) z = s.length() - 1;

		int ind =  Math.min(x, Math.min(y,z));
		return ind; 
	}
}


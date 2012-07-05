/*
  File: ExpressionReader.java

  Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.io.internal.read.expression;


import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;

import org.cytoscape.io.internal.read.AbstractTableReader;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyTableFactory;
import org.cytoscape.model.CyTableManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;


//--------------------------------------------------------------------

/**
 * This class provides a reader for the common file format for expression data
 * and an interface to access the data.
 * <P>
 * <p/> <p/> There are variations in the file format used; the following
 * assumptions about the file format are considered valid. Attempting to read a
 * file that does not satisfy these assumptions is not guaranteed to work.
 * <P>
 * <p/> 1. A token is a consecutive sequence of alphanumeric characters
 * separated by whitespace.<BR>
 * 2. The file consists of an arbitrary number of lines, each of which contains
 * the same number of tokens (except for possibly the first line) and has a
 * total length less than 8193 characters.<BR>
 * 3. The first line of the file is a header line with one of the following
 * three formats:
 * <P>
 * <p/> <text> <text> cond1 cond2 ... condN cond1 cond2 ... condN NumSigConds
 * <P>
 * <p/> <text> <text> cond1 cond2 ... condN
 * <P>
 * <p/> <\t><\t>RATIOS<\t><\t>...LAMBDAS
 * <P>
 * <p/> Here cond1 through condN are the names of the conditions. In the first
 * case, the two sequences of condition names must match exactly in order and
 * lexicographically; each name among cond1 ... condN must be unique. In the
 * second case, each name must be unique, but need only appear once. The last
 * label, NumSigConds, is optional.<BR>
 * The third case is the standard header for a MTX file. The numer of '\t'
 * characters between the words "RATIOS" and "LAMBDAS" is equal to the number of
 * ratio columns in the file (which must be equal to the number of lambda
 * columns).
 * <P>
 * <p/> 4. Each successive line represents the measurements for a partcular
 * gene, and has one of the following two formats, depending on the header:
 * <P>
 * <p/> <FNAME> <CNAME> E E ... E S S ... S I
 * <P>
 * <p/> <FNAME> <CNAME> E E ... E
 * <P>
 * <p/> where <FNAME> is the formal name of the gene, <CNAME> is the common
 * name, the E's are tokens, parsable as doubles, representing the expression
 * level change for each condition, the S's are tokens parsable as doubles
 * representing the statistical significance of the expression level change, and
 * I is an optional integer giving the number of conditions in which the
 * expression level change was significant for this gene.
 * <P>
 * <p/> The first format is used in conjuction with the first or third header
 * formats. The second format is used in conjunction with the second header
 * format.
 * <P>
 * <p/> 5. An optional last line can be included with the following form:
 * <P>
 * <p/> NumSigGenes: I I ... I
 * <P>
 * <p/> where there are N I's, each an integer representing the number of
 * significant genes in that condition.
 * <P>
 */
public class ExpressionReader extends AbstractTableReader {
	/**
	 *
	 */
	public static final int MAX_LINE_SIZE = 8192;

	/**
	 * Significance value: PVAL.
	 */
	public static final int PVAL = 0;

	/**
	 * Significance value: LAMBA.
	 */
	public static final int LAMBDA = 1;

	/**
	 * Significance value: NONE.
	 */
	public static final int NONE = 2;

	/**
	 * Significance value: UNKNOWN.
	 */
	public static final int UNKNOWN = 3;
	protected int significanceType = 3;

	private boolean mappingByAttribute = false;
	int numGenes;
	int numConds;
	int extraTokens;
	boolean haveSigValues;
	Vector<String> geneNames;
	Vector<String> geneDescripts;
	Vector<String> condNames;
	Map<String,Integer> geneNameToIndex;
	Map<String,Integer> condNameToIndex;
	double minExp;
	double maxExp;
	double minSig;
	double maxSig;
	Vector<Vector<mRNAMeasurement>> allMeasurements;
	private boolean isCancelled;
	private CyTable table;

	public ExpressionReader(final InputStream stream, final CyTableFactory tableFactory)
	{
		super(stream, tableFactory);
		
		isCancelled = false;
		table = null;
		numGenes = 0;
		numConds = 0;
		extraTokens = 0;
		haveSigValues = false;
		initDataStructures();
	}

	@Override
	public void cancel() {
		isCancelled = true;
	}

	/**
	 * Initializes all data structures.
	 */
	private void initDataStructures() {
		/*
		 * on overflow, capacity of vector will be increased by "expand"
		 * elements all at once; much more efficient when we don't know how many
		 * thousand genes are left in the file
		 */
		int expand = 1000;

		if (geneNames != null) {
			geneNames.clear();
		}

		geneNames = new Vector<String>(0, expand);

		if (geneDescripts != null) {
			geneDescripts.clear();
		}

		geneDescripts = new Vector<String>(0, expand);

		if (condNames != null) {
			condNames.clear();
		}

		condNames = new Vector<String>();

		if (geneNameToIndex != null) {
			geneNameToIndex.clear();
		}

		geneNameToIndex = new HashMap<String,Integer>();

		if (condNameToIndex != null) {
			condNameToIndex.clear();
		}

		condNameToIndex = new HashMap<String,Integer>();
		minExp = Double.MAX_VALUE;
		maxExp = Double.MIN_VALUE;
		minSig = Double.MAX_VALUE;
		maxSig = Double.MIN_VALUE;

		if (allMeasurements != null) {
			allMeasurements.clear();
		}

		allMeasurements = new Vector<Vector<mRNAMeasurement>>(0, expand);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		taskMonitor.setProgress(0.0);
		final BufferedReader input = new BufferedReader(new InputStreamReader(inputStream));

		try {
			int lineCount = 0;
			String line;

			// allow file to start with an arbitrary number
			// of comment lines starting with '#' symbol
			while ((line = input.readLine()) != null && line.startsWith("#"))
				++lineCount;

			String headerLine = line;
			++lineCount;

			if (headerLine == null || headerLine.length() == 0) {
				taskMonitor.setStatusMessage("Missing header in input file.");
				return;
			}
			taskMonitor.setProgress(0.1);
			
			if (isHeaderLineMTXHeader(headerLine)) {
				// for sure we know that the file contains lambdas
				significanceType = LAMBDA;
				headerLine = input.readLine();
				++lineCount;
				if (headerLine == null) {
					taskMonitor.setStatusMessage("Missing header in input file.");
					return;
				}
			}

			final boolean hasCOMMON = doesHeaderLineHasCOMMON(headerLine);
			final boolean expectPvals = doesHeaderLineHaveDuplicates(headerLine, hasCOMMON);
			
			taskMonitor.setProgress(0.2);
			
			if ((significanceType != LAMBDA) && !expectPvals) {
				// we know that we don't have a lambda header and we don't
				// have significance values
				significanceType = NONE;
			}

			StringTokenizer headerTok = new StringTokenizer(headerLine);
			int numTokens = headerTok.countTokens();

			// if we don't expect p-values, 3 is the minimum number with COMMON column, 2 if without COMMON.
			// if we expect p-values, 4 is the minimum number with COMMON column, 3 if without COMMON.
			int minTokens = 2;
			if (hasCOMMON) {
				minTokens = 3;
			}
			if ((numTokens < minTokens) || ((numTokens < (minTokens+1)) && expectPvals)) {
				StringBuffer msg = new StringBuffer("Invalid header format in data file.");
				msg.append("\nNumber of tokens parsed: " + numTokens);

				for (int i = 0; i < numTokens; i++) {
					msg.append("\nToken " + i + ": " + headerTok.nextToken());
				}

				throw new IOException(msg.toString());
			}

			double tmpF = numTokens / 2.0;
			int tmpI = (int) Math.rint(tmpF);
			int numberOfConditions;
			int haveExtraTokens = 0;

			if (expectPvals) {
				if (tmpI == tmpF) { // missing numSigConds field
					numberOfConditions = (numTokens - 2) / 2;
					haveExtraTokens = 0;
				} else {
					numberOfConditions = (numTokens - 3) / 2;
					haveExtraTokens = 1;
				} // else
			} else {
				numberOfConditions = numTokens - 2;
			}

			// Since COMMON is optional, it may not exist
			if (!hasCOMMON) {
				if (expectPvals) {
					if (tmpI == tmpF) {
						numberOfConditions = (numTokens - 2) / 2;
						haveExtraTokens = 1;
					} else {
						numberOfConditions = (numTokens - 1) / 2;
						haveExtraTokens = 0;
					}
				} else {
					numberOfConditions = numTokens - 1;
				}
			}

			/* eat the first two tokens from the header line */
			headerTok.nextToken();
			if (hasCOMMON) {
				headerTok.nextToken();
			}
			
			/* the next numConds tokens are the condition names */
			Vector<String> cNames = new Vector<String>(numberOfConditions);

			for (int i = 0; i < numberOfConditions; i++)
				cNames.add(headerTok.nextToken());

			/*
			 * the next numConds tokens should duplicate the previous list of
			 * condition names
			 */
			if (expectPvals) {
				for (int i = 0; i < numberOfConditions; i++) {
					final String title = headerTok.nextToken();

					if (!title.equals(cNames.get(i))) {
						final String msg = "Expecting both ratios and p-values.\n"
							+"Condition name mismatch in header line" + " of expression matrix data file "
							+ ":" + cNames.get(i) + " vs. " + title;
						throw new IOException(msg);
					}
				}
			}

			taskMonitor.setProgress(0.25);

			/*
			 * OK, we have a reasonable header; clobber all old information
			 */
			this.numConds = numberOfConditions;
			this.extraTokens = haveExtraTokens;
			this.haveSigValues = expectPvals;

			/* wipe old data */
			initDataStructures();

			/* store condition names */
			condNames = cNames;

			for (int i = 0; i < numConds; i++)
				condNameToIndex.put(condNames.get(i), Integer.valueOf(i));

			/* parse rest of file line by line */
			if (taskMonitor != null)
				taskMonitor.setStatusMessage("Reading in Data...");

			final boolean mappingByKeyAttribute = false; // FIXME: I just made this up!
			final Map<String, List<String>> attributeToId = new HashMap<String, List<String>>(); // FIXME: I just made this up!
			while ((line = input.readLine()) != null) {
				++lineCount;
				parseOneLine(line, lineCount, expectPvals, mappingByKeyAttribute, attributeToId, hasCOMMON);
				if (isCancelled)
					return;
			}
			//taskMonitor.setProgress(100.0);
			taskMonitor.setProgress(0.9);
			
			/* save numGenes and build hash of gene names to indices */
			numGenes = geneNames.size();

			for (int i = 0; i < geneNames.size(); i++) {
				if (geneNames.get(i) != null)
					geneNameToIndex.put(geneNames.get(i), i);
			}

			/* trim capacity of data structures for efficiency */
			geneNames.trimToSize();
			geneDescripts.trimToSize();
			allMeasurements.trimToSize();

			copyToAttribs(taskMonitor);
		} finally {
			input.close();
		}
		taskMonitor.setProgress(1.0);
	}

	private Map<String,List<String>> getAttributeToIdList(CyNetwork network, String keyAttributeName) throws IOException {
		Map<String,List<String>> attributeToIdList = new HashMap<String,List<String>>();
		// TODO needs to be converted to create a CyTable with node keys rather than looking
		// up all node ids.
		List<CyNode> allNodes = network.getNodeList(); 

		for (CyNode node : allNodes) {
			String nodeName = network.getRow(node).get("name",String.class);
			Object attrValue = network.getRow(node).getRaw(keyAttributeName);

			if (attrValue != null) {
				String attributeValue = attrValue.toString();

				if (attributeValue != null) {
					List<String> genesThisAttribute = attributeToIdList.get(attributeValue);
					if (genesThisAttribute == null) {
						genesThisAttribute = new ArrayList<String>();
						genesThisAttribute.add(nodeName);
						attributeToIdList.put(attributeValue, genesThisAttribute);
					}
					genesThisAttribute.add(nodeName);
				}
			}
		}

		return attributeToIdList;
	}

	// Check if the name of the second column is "COMMON" or "DESCRIPT"
	private boolean doesHeaderLineHasCOMMON(String hline) {

		StringTokenizer headerTok = new StringTokenizer(hline);

		if (headerTok.countTokens() <2) {
			return false;
		}

		headerTok.nextToken();
		String secondColHeader = headerTok.nextToken();

		if (secondColHeader.equalsIgnoreCase("COMMON")||secondColHeader.equalsIgnoreCase("DESCRIPT")) {
			return true;
		}
		return false;
	}


	private boolean doesHeaderLineHaveDuplicates(String hline, boolean hasCOMMON) {
		boolean retval = false;

		StringTokenizer headerTok = new StringTokenizer(hline);
		int numTokens = headerTok.countTokens();

		int minTokens =2;
		if (hasCOMMON) {
			minTokens =3;
		}
		if (numTokens < minTokens) {
			retval = false;
		} else {
			headerTok.nextToken();
			if (hasCOMMON) {
				headerTok.nextToken();
			}

			HashMap<Object,Object> names = new HashMap<Object,Object>();

			while ((!retval) && headerTok.hasMoreTokens()) {
				String title = headerTok.nextToken();
				Object titleObject = (Object) title;

				if (names.get(titleObject) == null) {
					names.put(titleObject, titleObject);
				} else {
					retval = true;
				}
			}
		}

		return retval;
	}

	// added by iliana on 11.25.2002
	// it is convenient for users to load their MTX files as they are
	// the current code requires them to remove the first line
	private boolean isHeaderLineMTXHeader(String hline) {
		boolean b = false;
		String pattern = "\t+RATIOS\t+LAMBDAS";
		b = hline.matches(pattern);

		return b;
	}

	private void parseOneLine(final String line, final int lineCount, final boolean sig_vals,
	                          boolean mappingByAttribute, Map<String,List<String>> attributeToId,
	                          boolean hasCOMMON)
		throws IOException
	{
		//
		// Step 1: divide the line into input tokens, and parse through
		// the input tokens.
		//
		StringTokenizer strtok = new StringTokenizer(line);
		int numTokens = strtok.countTokens();
		if (numTokens == 0)
			return;

		/* first token is gene name (or identifying attribute), or NumSigGenes */
		String firstToken = strtok.nextToken();
		if (firstToken.startsWith("NumSigGenes"))
			return;

		final int numPreCols = hasCOMMON ? 2 : 1; // Number of columns before data columns

		if ((sig_vals && (numTokens < ((2 * numConds) + numPreCols)))
		    || ((!sig_vals) && (numTokens < (numConds + numPreCols))))
			throw new IOException("Warning: parse error on line " + lineCount + "  tokens read: "
			                      + numTokens);

		final String geneDescript = hasCOMMON ? strtok.nextToken() : "";

		String[] expData = new String[numConds];

		for (int i = 0; i < numConds; i++)
			expData[i] = strtok.nextToken();

		String[] sigData = new String[numConds];

		if (sig_vals) {
			for (int i = 0; i < numConds; i++)
				sigData[i] = strtok.nextToken();
		} else {
			for (int i = 0; i < numConds; i++)
				sigData[i] = expData[i];
		}

		List<String> gNames = new ArrayList<String>();

		if (mappingByAttribute) {
			List<String> names = attributeToId.get(firstToken);
			if (names != null) {
				gNames = names;
			}
		} else {
			gNames = new ArrayList<String>();
			gNames.add(firstToken);
		}

		for (int ii = 0; ii < gNames.size(); ii++) {
			geneNames.add(gNames.get(ii));

			/* store descriptor token */
			geneDescripts.add(geneDescript);

			Vector<mRNAMeasurement> measurements = new Vector<mRNAMeasurement>(numConds);

			for (int jj = 0; jj < numConds; jj++) {
				mRNAMeasurement m = new mRNAMeasurement(expData[jj], sigData[jj]);
				measurements.add(m);

				double ratio = m.getRatio();
				double signif = m.getSignificance();

				if (ratio < minExp) {
					minExp = ratio;
				}

				if (ratio > maxExp) {
					maxExp = ratio;
				}

				if (signif < minSig) {
					minSig = signif;
				}

				if (signif > maxSig) {
					maxSig = signif;

					if ((this.significanceType != this.LAMBDA) && sig_vals && (maxSig > 1)) {
						this.significanceType = this.LAMBDA;
					}
				}
			}

			if ((this.significanceType != this.LAMBDA) && sig_vals && (minSig > 0)) {
				// We are probably not looking at lambdas, since no
				// significance value was > 1
				// and the header is not a LAMBDA header
				this.significanceType = this.PVAL;
			}

			allMeasurements.add(measurements);
		}
	} // parseOneLine

	/**
	 * Converts all lambdas to p-values. Lambdas are lost after this call.
	 */
	public void convertLambdasToPvals() {
		for ( Vector<mRNAMeasurement> v : allMeasurements ) {
			for ( mRNAMeasurement m : v ) {
				double pval = getPvalueFromLambda(m.getSignificance());
				m.setSignificance(pval);
			}
		}
	} // convertPValsToLambdas

	/**
	 * Gets a PValue of the specified lambda value.
	 *
	 * @return a very close approximation of the pvalue that corresponds to the
	 *         given lambda value
	 */
	static public double getPvalueFromLambda(double lambda) {
		double x = StrictMath.sqrt(lambda) / 2.0;
		double t = 1.0 / (1.0 + (0.3275911 * x));
		double erfc = StrictMath.exp(-(x * x)) * ((0.254829592 * t)
		                                         + (-0.284496736 * StrictMath.pow(t, 2.0))
		                                         + (1.421413741 * StrictMath.pow(t, 3.0))
		                                         + (-1.453152027 * StrictMath.pow(t, 4.0))
		                                         + (1.061405429 * StrictMath.pow(t, 5.0)));
		erfc = erfc / 2.0;

		if ((erfc < 0) || (erfc > 1)) {
			// P-value must be >= 0 and <= 1
			throw new IllegalStateException("The calculated pvalue for lambda = " + lambda + " is "
			                                + erfc);
		}

		return erfc;
	} // getPvalueFromLambda

	/**
	 * Gets an Array of All Experimental Conditions.
	 *
	 * @return Array of String Objects.
	 */
	private String[] getConditionNames() {
		return (String[]) condNames.toArray(new String[0]);
	}

	/**
	 * Gets a Vector of all Measurements associated with the specified gene.
	 *
	 * @param gene Gene Name.
	 * @return Vector of mRNAMeasurement Objects.
	 */
	private Vector<mRNAMeasurement> getMeasurements(String gene) {
		if (gene == null)
			return null;

		final Integer geneIndex = geneNameToIndex.get(gene);
		if (geneIndex == null)
			return null;

		return allMeasurements.get(geneIndex);
	}

	/**
	 * Gets Single Measurement Value for the specified gene at the specified
	 * condition.
	 *
	 * @param gene      Gene Name.
	 * @param condition Condition Name (corresponds to column heading in original
	 *                  expression data file.)
	 * @return an mRNAMeasurement Object.
	 */
	private mRNAMeasurement getMeasurement(String gene, String condition) {
		Integer condIndex = condNameToIndex.get(condition);

		if (condIndex == null) {
			return null;
		}

		Vector<mRNAMeasurement> measurements = this.getMeasurements(gene);

		if (measurements == null) {
			return null;
		}

		mRNAMeasurement returnVal = measurements.get(condIndex.intValue());

		return returnVal;
	}

	/**
	 * Copies ExpressionData data structure into CyAttributes data structure.
	 *
	 * @param nodeAttribs Node Attributes CyTable.
	 * @param taskMonitor Task Monitor. Can be null.
	 */
	private void copyToAttribs(TaskMonitor taskMonitor) {
		String[] condNames = getConditionNames();

		table = tableFactory.createTable("Expression Matrix", "geneName", String.class,
						 /* public = */ true, /* mutable = */ true);

		// first set up the columns
		for (int condNum = 0; condNum < condNames.length; condNum++) {
			String condName = condNames[condNum];
			String eStr = condName + "exp";
			String sStr = condName + "sig";
			table.createColumn(eStr, Double.class, false);
			table.createColumn(sStr, Double.class, false);
		}

		// now create the rows and populate
		for (int i = 0; i < geneNames.size(); i++) {
			String canName = geneNames.get(i);
			CyRow row = table.getRow(canName);

			for (int condNum = 0; condNum < condNames.length; condNum++) {
				String condName = condNames[condNum];
				String eStr = condName + "exp";
				String sStr = condName + "sig";

				mRNAMeasurement mm = getMeasurement(canName, condName);

				if (mm != null) {
					row.set(eStr, new Double(mm.getRatio()));
					row.set(sStr, new Double(mm.getSignificance()));
				}

				// Report on Progress to the Task Monitor.
				if (taskMonitor != null) {
					int currentCoordinate = (condNum * geneNames.size()) + i;
					int matrixSize = condNames.length * geneNames.size();
					double percent = ((double) currentCoordinate / matrixSize) * 100.0;
					taskMonitor.setProgress(percent);
				}
			}
		}

	}

	@Override
	public CyTable[] getTables() {
		if (table == null)
			return null;
		return new CyTable[] { table };
	}
}

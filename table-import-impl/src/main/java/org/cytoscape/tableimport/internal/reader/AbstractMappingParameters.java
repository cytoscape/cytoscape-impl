package org.cytoscape.tableimport.internal.reader;

/*
 * #%L
 * Cytoscape Table Import Impl (table-import-impl)
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

import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.TAB;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.tableimport.internal.util.AttributeDataTypes;

public abstract class AbstractMappingParameters implements MappingParameter{
	
	public static final String ID = "name";
	private static final String DEF_LIST_DELIMITER = PIPE.toString();
	private static final String DEF_DELIMITER = TAB.toString();
	private String[] attributeNames;
	private Byte[] attributeTypes;
	private Byte[] listAttributeTypes;
	private List<String> delimiters;
	private String listDelimiter;
	private boolean[] importFlag;
	
	private Map<String, List<String>> attr2id;
	private Map<String, String> networkTitle2ID;

	// Case sensitivity
	private int startLineNumber;
	private String commentChar;
	
	public InputStream is;
	public String fileType;
	
	public AbstractMappingParameters(InputStream is, String fileType) {
		this.delimiters = new ArrayList<String>();
		this.delimiters.add(DEF_DELIMITER);
		this.listDelimiter = DEF_DELIMITER;
		this.is = is;
		this.fileType = fileType;
	}

	public AbstractMappingParameters( final List<String> delimiters,
	                                  final String listDelimiter, //final int keyIndex,
	                                 // final String mappingAttribute,
	                                 //final List<Integer> aliasIndex, 
	                                  final String[] attrNames, Byte[] attributeTypes, Byte[] listAttributeTypes,
	                                  boolean[] importFlag,
	                                  boolean caseSensitive) throws Exception {
		this(delimiters, listDelimiter, attrNames, attributeTypes, listAttributeTypes, importFlag, 0, null);
	}

	public AbstractMappingParameters( final List<String> delimiters,
	                                  final String listDelimiter, //final int keyIndex,
	                                  //final String mappingAttribute,
	                                  //final List<Integer> aliasIndex, 
	                                  final String[] attrNames,Byte[] attributeTypes, 
	                                  Byte[] listAttributeTypes, 
	                                  boolean[] importFlag, int startNumber, String commentChar)
	    throws Exception {
		this.listAttributeTypes = listAttributeTypes;
		this.startLineNumber= startNumber;
		this.commentChar = commentChar;

		if (attrNames == null) {
			throw new Exception("attributeNames should not be null.");
		}

		/*
		 * Error check: Key column number should be smaller than actual number
		 * of columns in the text table.
		 */
/*		if (attrNames.length < keyIndex) {
			throw new IOException("Key is out of range.");
		}
*/
		/*
		 * These calues should not be null!
		 */
		//this.keyIndex = keyIndex;
		this.attributeNames = attrNames;

		/*
		 * If attribute mapping is null, use ID for mapping.
		 */
		/*
		if (mappingAttribute == null) {
			this.mappingAttribute = ID; // Note: ID = 'name'
		} else {
			this.mappingAttribute = mappingAttribute;
		}
*/
		/*
		 * If delimiter is not available, use default value (TAB)
		 */
		if (delimiters == null) {
			this.delimiters = new ArrayList<String>();
			this.delimiters.add(DEF_DELIMITER);
		} else {
			this.delimiters = delimiters;
		}

		/*
		 * If list delimiter is null, use default "|"
		 */
		if (listDelimiter == null) {
			this.listDelimiter = DEF_LIST_DELIMITER;
		} else {
			this.listDelimiter = listDelimiter;
		}

		/*
		if (aliasIndex == null) {
			this.aliasIndex = new ArrayList<Integer>();
		} else {
			this.aliasIndex = aliasIndex;
		}
		*/

		/*
		 * If not specified, import everything as String attributes.
		 */
		if (attributeTypes == null) {
			this.attributeTypes = new Byte[attrNames.length];

			for (int i = 0; i < attrNames.length; i++) {
				this.attributeTypes[i] = AttributeDataTypes.TYPE_STRING;
			}
		} else {
			this.attributeTypes = attributeTypes;
		}

		/*
		 * If not specified, import everything.
		 */
		if (importFlag == null) {
			this.importFlag = new boolean[attrNames.length];

			for (int i = 0; i < this.importFlag.length; i++) {
				this.importFlag[i] = true;
			}
		} else {
			this.importFlag = importFlag;
		}

	}

	@Override
	public String[] getAttributeNames() {
		// TODO Auto-generated method stub
		return attributeNames;
	}
	
	@Override
	public Byte[] getAttributeTypes() {
		return attributeTypes;
	}

	public Byte[] getListAttributeTypes() {
		return listAttributeTypes;
	}
	
	@Override
	public boolean[] getImportFlag() {
		// TODO Auto-generated method stub
		return importFlag;
	}

	public String getListDelimiter() {
		// TODO Auto-generated method stub
		return listDelimiter;
	}

	public List<String> getDelimiters() {
		return delimiters;
	}

	public String getDelimiterRegEx() {
		StringBuffer delimiterBuffer = new StringBuffer();
		delimiterBuffer.append("[");

		for (String delimiter : delimiters) {
			if (delimiter.equals(" += +")) {
				return " += +";
			}

			delimiterBuffer.append(delimiter);
		}

		delimiterBuffer.append("]");

		return delimiterBuffer.toString();
	}

	public List<String> toID(String attributeValue) {
		return attr2id.get(attributeValue);
	}

	public Map<String, List<String>> getAttributeToIDMap() {
		return attr2id;
	}

	private void putAttrValue(String attributeValue, String objectID) {
		List<String> objIdList = attr2id.get(attributeValue);
		if (objIdList == null) {
			objIdList = new ArrayList<String>();
		}

		objIdList.add(objectID);
		attr2id.put(attributeValue, objIdList);
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		if (attributeNames == null)
			return -1;
		return attributeNames.length;
	}
	
	public int getSelectedColumnCount(){
		if (attributeNames == null)
			return -1;
		int count = 0;
		for (boolean b : importFlag)
			if (b)
				count++;
		return count;
	}

	protected Map<String, String> getnetworkTitleMap() {
		return networkTitle2ID;
	}

	public int getStartLineNumber(){
		return startLineNumber;
	}
	
	public String getCommentChar(){
		return commentChar;
	}
}

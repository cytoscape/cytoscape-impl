package org.cytoscape.tableimport.internal.reader;

import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextFileDelimiters.TAB;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyTable;
import org.cytoscape.tableimport.internal.reader.TextTableReader.ObjectType;
import org.cytoscape.tableimport.internal.util.AttributeTypes;

public abstract class AbstractMappingParameters implements MappingParameter{
	
	public static final String ID = "name";
	private static final String DEF_LIST_DELIMITER = PIPE.toString();
	private static final String DEF_DELIMITER = TAB.toString();
	//private final int keyIndex;
	//private final List<Integer> aliasIndex;
	private String[] attributeNames;
	private Byte[] attributeTypes;
	private Byte[] listAttributeTypes;
	//private final String mappingAttribute;
	private List<String> delimiters;
	private String listDelimiter;
	private boolean[] importFlag;
	private Map<String, List<String>> attr2id;
	//private CyTable attributes;
	//private Aliases existingAliases;
	private Map<String, String> networkTitle2ID = null;

	// Case sensitivity
	private int startLineNumber;
	private String commentChar;
	
	public InputStream is;
	public String fileType;
	
	public AbstractMappingParameters( InputStream is, String fileType){	
	
		/*this.keyIndex = -1;
		this.aliasIndex = null;
		this.mappingAttribute = null;
		*/
		this.delimiters = new ArrayList<String>();
		this.delimiters.add(DEF_DELIMITER);
		this.listDelimiter = DEF_DELIMITER;
		this.is = is;
		this.fileType = fileType;
	}

	/**
	 * Creates a new AttributeMappingParameters object.
	 *
	 * @param objectType  DOCUMENT ME!
	 * @param delimiters  DOCUMENT ME!
	 * @param listDelimiter  DOCUMENT ME!
	 * @param keyIndex  DOCUMENT ME!
	 * @param mappingAttribute  DOCUMENT ME!
	 * @param aliasIndex  DOCUMENT ME!
	 * @param attrNames  DOCUMENT ME!
	 * @param attributeTypes  DOCUMENT ME!
	 * @param listAttributeTypes  DOCUMENT ME!
	 * @param importFlag  DOCUMENT ME!
	 *
	 * @throws Exception  DOCUMENT ME!
	 */
	public AbstractMappingParameters( final List<String> delimiters,
	                                  final String listDelimiter, //final int keyIndex,
	                                 // final String mappingAttribute,
	                                 //final List<Integer> aliasIndex, 
	                                  final String[] attrNames, Byte[] attributeTypes, Byte[] listAttributeTypes,
	                                  boolean[] importFlag,
	                                  boolean caseSensitive) throws Exception {
		this( delimiters, listDelimiter,
				//keyIndex, mappingAttribute, aliasIndex,
		     attrNames, attributeTypes, listAttributeTypes, 
		     importFlag, 
			 0, null);
	}

	/**
	 * Creates a new AttributeMappingParameters object.
	 *
	 * @param objectType  DOCUMENT ME!
	 * @param delimiters  DOCUMENT ME!
	 * @param listDelimiter  DOCUMENT ME!
	 * @param keyIndex  DOCUMENT ME!
	 * @param mappingAttribute  DOCUMENT ME!
	 * @param aliasIndex  DOCUMENT ME!
	 * @param attrNames  DOCUMENT ME!
	 * @param attributeTypes  DOCUMENT ME!
	 * @param listAttributeTypes  DOCUMENT ME!
	 * @param importFlag  DOCUMENT ME!
	 *
	 * @throws Exception  DOCUMENT ME!
	 * @throws IOException  DOCUMENT ME!
	 */
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
				this.attributeTypes[i] = AttributeTypes.TYPE_STRING;
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

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	//public Aliases getAlias() {
	//	return existingAliases;
	//}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	//public CyAttributes getAttributes() {
	//	return attributes;
	//}


 
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
/*	public List<Integer> getAliasIndexList() {
		return aliasIndex;
	}
*/
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String[] getAttributeNames() {
		// TODO Auto-generated method stub
		return attributeNames;
	}
	
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Byte[] getAttributeTypes() {
		return attributeTypes;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Byte[] getListAttributeTypes() {
		return listAttributeTypes;
	}
	
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public boolean[] getImportFlag() {
		// TODO Auto-generated method stub
		return importFlag;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
/*	public int getKeyIndex() {
		// TODO Auto-generated method stub
		return keyIndex;
	}
*/
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getListDelimiter() {
		// TODO Auto-generated method stub
		return listDelimiter;
	}

	/**
	 *  Returns attribute name for mapping.
	 *
	 * @return  Key CyAttribute name for mapping.
	 */
/*	public String getMappingAttribute() {
		return mappingAttribute;
	}
*/

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public List<String> getDelimiters() {
		return delimiters;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
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

	/**
	 *  DOCUMENT ME!
	 *
	 * @param attributeValue DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public List<String> toID(String attributeValue) {
		return attr2id.get(attributeValue);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
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

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
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

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public int getStartLineNumber(){
		return startLineNumber;
	}
	
	

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getCommentChar(){
		return commentChar;
	}

}

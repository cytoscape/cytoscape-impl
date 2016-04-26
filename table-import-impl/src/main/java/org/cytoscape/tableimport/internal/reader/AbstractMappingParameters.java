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

import static org.cytoscape.tableimport.internal.reader.TextDelimiter.PIPE;
import static org.cytoscape.tableimport.internal.reader.TextDelimiter.TAB;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;

public abstract class AbstractMappingParameters implements MappingParameter{
	
	public static final String ID = "name";
	public static final String DEF_LIST_DELIMITER = PIPE.getDelimiter();
	private static final String DEF_DELIMITER = TAB.getDelimiter();
	
	private String name;
	protected String[] attributeNames;
	protected AttributeDataType[] dataTypes;
	protected SourceColumnSemantic[] types;
	protected List<String> delimiters;
	protected String[] listDelimiters;
	
	private Map<String, List<String>> attr2id;
	private Map<String, String> networkTitle2ID;

	// Case sensitivity
	private int startLineNumber;
	private String commentChar;
	
	public InputStream is;
	public String fileType;
	
	public AbstractMappingParameters(final InputStream is, final String fileType) {
		this.delimiters = new ArrayList<>();
		this.delimiters.add(DEF_DELIMITER);
		this.is = is;
		this.fileType = fileType;
	}

	public AbstractMappingParameters( 
			final String name,
			final List<String> delimiters,
			final String[] listDelimiters,
			final String[] attrNames,
			final AttributeDataType[] dataTypes,
			final SourceColumnSemantic[] types,
			final boolean caseSensitive
	) throws Exception {
		this(name, delimiters, listDelimiters, attrNames, dataTypes, types, 0, null);
	}

	public AbstractMappingParameters(
			final String name,
			final List<String> delimiters,
			final String[] listDelimiters,
			final String[] attrNames,
			final AttributeDataType[] dataTypes,
			final SourceColumnSemantic[] types,
			final int startNumber,
			final String commentChar
	) throws Exception {
		this.name = name;
		this.startLineNumber = startNumber;
		this.commentChar = commentChar;

		if (attrNames == null)
			throw new Exception("attributeNames should not be null.");
		
		/*
		 * These values should not be null!
		 */
		this.attributeNames = attrNames;

		/*
		 * If delimiter is not available, use default value (TAB)
		 */
		if (delimiters == null) {
			this.delimiters = new ArrayList<>();
			this.delimiters.add(DEF_DELIMITER);
		} else {
			this.delimiters = delimiters;
		}

		this.listDelimiters = listDelimiters;

		/*
		 * If not specified, import everything as String attributes.
		 */
		if (dataTypes == null) {
			this.dataTypes = new AttributeDataType[attrNames.length];
			Arrays.fill(this.dataTypes, AttributeDataType.TYPE_STRING);
		} else {
			this.dataTypes = dataTypes;
		}

		/*
		 * If not specified, do not import anything.
		 */
		if (types == null) {
			this.types = new SourceColumnSemantic[attrNames.length];
			Arrays.fill(types, SourceColumnSemantic.NONE);
		} else {
			this.types = types;
		}
	}

	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public String[] getAttributeNames() {
		return attributeNames;
	}
	
	@Override
	public AttributeDataType[] getDataTypes() {
		return dataTypes;
	}
	
	@Override
	public SourceColumnSemantic[] getTypes() {
		return types;
	}

	public String[] getListDelimiters() {
		return listDelimiters;
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

	@Override
	public int getColumnCount() {
		if (attributeNames == null)
			return -1;
		return attributeNames.length;
	}
	
	public int getSelectedColumnCount(){
		if (attributeNames == null)
			return -1;
		
		int count = 0;
		
		for (SourceColumnSemantic t : types) {
			if (t != SourceColumnSemantic.NONE)
				count++;
		}
		
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

package org.cytoscape.tableimport.internal.ui;

import org.cytoscape.tableimport.internal.util.AttributeDataType;
import org.cytoscape.tableimport.internal.util.SourceColumnSemantic;

public class AttributeSettings {

	private SourceColumnSemantic attrType;
	private String namespace;
	private AttributeDataType attrDataType;
	private String listDelimiter;
	
	public AttributeSettings(
			SourceColumnSemantic attrType,
			String namespace,
			AttributeDataType attrDataType,
			String listDelimiter
	) {
		this.attrType = attrType;
		this.namespace = namespace;
		this.attrDataType = attrDataType;
		this.listDelimiter = listDelimiter;
	}

	public SourceColumnSemantic getAttrType() {
		return attrType;
	}

	public void setAttrType(SourceColumnSemantic attrType) {
		this.attrType = attrType;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public AttributeDataType getAttrDataType() {
		return attrDataType;
	}

	public void setAttrDataType(AttributeDataType attrDataType) {
		this.attrDataType = attrDataType;
	}

	public String getListDelimiter() {
		return listDelimiter;
	}

	public void setListDelimiter(String listDelimiter) {
		this.listDelimiter = listDelimiter;
	}
}

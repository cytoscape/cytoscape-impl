package org.cytoscape.io.internal.read.xgmml;

import org.cytoscape.io.internal.read.xgmml.handler.AttributeValueUtil;
import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public interface Handler {
	
	public ParseState handle(String tag, Attributes atts, ParseState current)
			throws SAXException;
	
	public void setManager(ReadDataManager manager);
	
	public void setAttributeValueUtil(AttributeValueUtil attributeValueUtil);

}
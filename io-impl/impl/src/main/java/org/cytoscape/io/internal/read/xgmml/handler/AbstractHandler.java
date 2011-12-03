package org.cytoscape.io.internal.read.xgmml.handler;

import org.cytoscape.io.internal.read.xgmml.Handler;
import org.cytoscape.io.internal.read.xgmml.ObjectTypeMap;
import org.cytoscape.io.internal.read.xgmml.ParseState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public abstract class AbstractHandler implements Handler {

	protected ReadDataManager manager;
	protected AttributeValueUtil attributeValueUtil;
	
	ObjectTypeMap typeMap;
	
	protected static final Logger logger = LoggerFactory.getLogger(AbstractHandler.class);

	public AbstractHandler() {
	    typeMap = new ObjectTypeMap();
	}

	@Override
	abstract public ParseState handle(String tag, Attributes atts, ParseState current) throws SAXException;

	@Override
	public void setManager(ReadDataManager manager) {
		this.manager = manager;
	}

	@Override
	public void setAttributeValueUtil(AttributeValueUtil attributeValueUtil) {
		this.attributeValueUtil = attributeValueUtil;
	}

}

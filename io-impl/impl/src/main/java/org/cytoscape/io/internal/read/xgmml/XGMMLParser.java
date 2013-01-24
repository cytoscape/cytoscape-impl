package org.cytoscape.io.internal.read.xgmml;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

import java.util.Stack;

import org.cytoscape.io.internal.read.xgmml.handler.ReadDataManager;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XGMMLParser extends DefaultHandler {

	private Locator locator;
	private String currentCData;

	private ParseState parseState;
	private Stack<ParseState> stateStack;

	private final HandlerFactory handlerFactory;

	private final ReadDataManager readDataManager;

	/**
	 * Main constructor for our parser. Initialize any local arrays. Note that
	 * this parser is designed to be as memory efficient as possible. As a
	 * result, a minimum number of local data structures are created.
	 */
	public XGMMLParser(HandlerFactory handlerFactory, ReadDataManager readDataManager) {
		this.handlerFactory = handlerFactory;
		this.readDataManager = readDataManager;
	}

	/********************************************************************
	 * Handler routines. The following routines are called directly from the SAX
	 * parser.
	 *******************************************************************/

	/**
	 * @see org.xml.sax.helpers.DefaultHandler#startDocument()
	 */
	@Override
	public void startDocument() throws SAXException {
		stateStack = new Stack<ParseState>();
		parseState = ParseState.NONE;
		handlerFactory.init();
		super.startDocument();
	}

	/**
	 * startElement is called whenever the SAX parser sees a start tag. We use
	 * this as the way to fire our state table.
	 * 
	 * @param namespace
	 *            the URL of the namespace (full spec)
	 * @param localName
	 *            the tag itself, stripped of all namespace stuff
	 * @param qName
	 *            the tag with the namespace prefix
	 * @param atts
	 *            the Attributes list from the tag
	 */
	@Override
	public void startElement(String namespace, String localName, String qName, Attributes atts) throws SAXException {
		final ParseState nextState = handleStartState(parseState, localName, atts);
		stateStack.push(parseState);
		parseState = nextState;
	}

	/**
	 * endElement is called whenever the SAX parser sees an end tag. We use this
	 * as the way to fire our state table.
	 * 
	 * @param uri
	 *            the URL of the namespace (full spec)
	 * @param localName
	 *            the tag itself, stripped of all namespace stuff
	 * @param qName
	 *            the tag with the namespace prefix
	 */
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		handleEndState(parseState, localName, null);
		parseState = stateStack.pop();
	}

	/**
	 * characters is called to handle CData
	 * 
	 * @param ch
	 *            the character data
	 * @param start
	 *            the start of the data for this tag
	 * @param length
	 *            the number of bytes for this tag
	 */
	@Override
	public void characters(char[] ch, int start, int length) {
		currentCData = new String(ch, start, length);
	}

	/**
	 * fatalError -- handle a fatal parsing error
	 * 
	 * @param e
	 *            the exception that generated the error
	 */
	@Override
	public void fatalError(SAXParseException e) throws SAXException {
		String err = "Fatal parsing error on line " + e.getLineNumber() + " -- '" + e.getMessage() + "'";
		throw new SAXException(err);
	}

	/**
	 * error -- handle a parsing error
	 * 
	 * @param e
	 *            the exception that generated the error
	 */
	@Override
	public void error(SAXParseException e) {

	}

	/**
	 * Set the document locator to help us construct our own exceptions
	 * 
	 * @param locator
	 *            the document locator to set
	 */
	@Override
	public void setDocumentLocator(Locator locator) {
		this.locator = locator;
	}

	/********************************************************************
	 * Private parser routines. The following routines are used to manage the
	 * state data.
	 *******************************************************************/

	/**
	 * handleState takes as input a state table, the current state, and the tag.
	 * It then looks for a match in the state table of current state and tag,
	 * then it calls the appropriate handler.
	 * 
	 * @param table
	 *            the state table to use
	 * @param currentState
	 *            the current state
	 * @param tag
	 *            the element tag
	 * @param atts
	 *            the Attributes associated with this tag. These will be passed
	 *            to the handler
	 * @return the new state
	 */
	private ParseState handleStartState(ParseState currentState, String tag, Attributes atts) throws SAXException {
		return handleState(currentState, tag, atts, handlerFactory.getStartHandler(currentState, tag));
	}

	private ParseState handleEndState(ParseState currentState, String tag, Attributes atts) throws SAXException {
		return handleState(currentState, tag, atts, handlerFactory.getEndHandler(currentState, tag));
	}

	private ParseState handleState(ParseState currentState, String tag, Attributes atts, SAXState state)
			throws SAXException {
		if (state != null) {
			final Handler handler = state.getHandler();

			if (handler != null)
				return handler.handle(tag, atts, state.getEndState());
			else
				return state.getEndState();
		} else {
			return currentState;
		}
	}
}

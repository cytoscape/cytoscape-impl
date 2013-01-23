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

public class SAXState {
	
	private ParseState startState;
	private ParseState endState;
	private String tag;
	private Handler handler;
	
	
	public SAXState(ParseState startState, String tag, ParseState endState, Handler handler) {
		this.startState = startState;
		this.tag = tag;
		this.endState = endState;
		this.handler = handler;
	}
	
	public boolean isCurrentState() {
		return true;
	}
	
	public ParseState getStartState() {
		return this.startState;
	}
	
	public ParseState getEndState() {
		return this.endState;
	}
	
	public String getTag() {
		return this.tag;
	}
	
	public Handler getHandler() {
		return handler;
	}
	
}

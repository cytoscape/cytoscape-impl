package org.cytoscape.io.internal.read.xgmml;

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

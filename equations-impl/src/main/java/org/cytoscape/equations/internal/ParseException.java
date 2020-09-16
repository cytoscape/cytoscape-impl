package org.cytoscape.equations.internal;

@SuppressWarnings("serial")
public class ParseException extends IllegalStateException {

	private final int location;
	
	public ParseException(int location) {
		this.location = location;
	}
	
	public ParseException(int location, String s) {
		super(s);
		this.location = location;
	}
	
	public int getErrorLocation() {
		return location;
	}
	
	public String getErrorMsg() {
		return super.getMessage();
	}
	
	@Override
	public String getMessage() {
		return location + ": " + super.getMessage();
	}

}

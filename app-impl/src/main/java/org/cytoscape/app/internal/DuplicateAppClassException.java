package org.cytoscape.app.internal;

public class DuplicateAppClassException extends AppException {

	public DuplicateAppClassException() {
		super("Failed to load duplicate app class");
	}

	public DuplicateAppClassException(String arg0) {
		super(arg0);
	}

	public DuplicateAppClassException(Throwable arg0) {
		super("Failed to load duplicate app class", arg0);
	}

	public DuplicateAppClassException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}

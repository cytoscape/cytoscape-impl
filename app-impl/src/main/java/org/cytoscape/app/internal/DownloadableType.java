/**
 * 
 */
package org.cytoscape.app.internal;

public enum DownloadableType {
	APP("app"), THEME("theme"), FILE("file");

	private String type;
	
	private DownloadableType(String Type) {
		type = Type;
	}

	public String value() {
		return type;
	}

	public static DownloadableType getStatus(String value)
			throws InvalidDownloadable {
		for (DownloadableType type : values()) {
			if (type.value().equals(value)) return type;
		}
		throw new InvalidDownloadable("'" + value + "' is not a valid downloadable object.");
	}

}

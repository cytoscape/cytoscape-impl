package org.cytoscape.io.internal.write.graphml;

public enum GraphMLAttributeDataTypes {
	BOOLEAN("boolean", Boolean.class), INTEGER("int", Integer.class), LONG(
			"long", Long.class), FLOAT("float", Float.class), DOUBLE("double",
			Double.class), STRING("string", String.class);

	private final String typeTag;
	private final Class<?> type;

	private GraphMLAttributeDataTypes(final String typeTag, final Class<?> type) {
		this.typeTag = typeTag;
		this.type = type;
	}

	public String getTypeTag() {
		return this.typeTag;
	}

	public static String getTag(Class<?> classType) {
		for (GraphMLAttributeDataTypes tag : values()) {
			if (classType.equals(tag.type))
				return tag.typeTag;
		}

		return null;
	}
}

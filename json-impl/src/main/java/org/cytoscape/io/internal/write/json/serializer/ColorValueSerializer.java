package org.cytoscape.io.internal.write.json.serializer;

import java.awt.Color;
import java.awt.Paint;

public class ColorValueSerializer implements ValueSerializer<Paint> {

	@Override
	public String serialize(final Paint value) {
		Color color = null;
		if(value instanceof Color == false) {
			color = Color.white;
		} else {
			color = (Color) value;
		}

		final StringBuilder builder = new StringBuilder();

		builder.append("rgb(");
		builder.append(color.getRed() + ",");
		builder.append(color.getGreen() + ",");
		builder.append(color.getBlue() + ")");

		return builder.toString();
	}

}

package org.cytoscape.ding.impl.cyannotator.api;

import java.awt.Color;
import java.awt.Font;

public interface TextAnnotation extends Annotation {
	public void setText(String text);
	public String getText();

	public void setTextColor(Color color);
	public Color getTextColor();

	public void setFontSize(double size);
	public double getFontSize();

	public void setFontStyle(int style);
	public int getFontStyle();

	public void setFontFamily(String family);
	public String getFontFamily();

	public Font getFont();
	public void setFont(Font font);
}


package org.cytoscape.ding.impl.cyannotator.api;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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


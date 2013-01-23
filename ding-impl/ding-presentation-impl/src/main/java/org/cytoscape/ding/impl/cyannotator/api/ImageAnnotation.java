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

import java.awt.Image;
import java.net.URL;

public interface ImageAnnotation extends ShapeAnnotation {
	public void reloadImage();

	public Image getImage();
	public void setImage(Image image);
	public URL getImageURL();
	public void setImage(URL url);

	public void setImageOpacity(float opacity);
	public float getImageOpacity();

	public void setImageBrightness(int brightness);
	public int getImageBrightness();

	public void setImageContrast(int contrast);
	public int getImageContrast();
}

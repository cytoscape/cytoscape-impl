package org.cytoscape.ding.customgraphics.bitmap;

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

import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import org.cytoscape.view.presentation.customgraphics.CustomGraphicLayer;
import org.cytoscape.view.presentation.customgraphics.ImageCustomGraphicLayer;
import org.cytoscape.ding.customgraphics.paint.TexturePaintFactory;

public class ImageCustomGraphicImpl implements ImageCustomGraphicLayer {
	private Rectangle2D bounds;
	private TexturePaintFactory pf;

	public ImageCustomGraphicImpl(Rectangle2D bounds, TexturePaintFactory factory) {
		this.bounds = bounds;
		this.pf = factory;
	}

	public Rectangle2D getBounds2D() { return bounds; }

	// TODO: at some point, we should just bring all of the TexturePaintFactory
	// stuff into here....
	@Override
	public TexturePaint getPaint(Rectangle2D bounds) {
		return pf.getPaint(bounds);
	}

	public CustomGraphicLayer transform(AffineTransform xform) {
		Shape s = xform.createTransformedShape(bounds);
		return new ImageCustomGraphicImpl(s.getBounds2D(), pf);
	}
	
}

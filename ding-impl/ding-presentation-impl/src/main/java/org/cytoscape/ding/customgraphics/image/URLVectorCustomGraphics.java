package org.cytoscape.ding.customgraphics.image;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2020 The Cytoscape Consortium
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

/**
 * Render vector images (e.g. SVG) created from a URL.
 */
public class URLVectorCustomGraphics extends AbstractURLImageCustomGraphics<SVGLayer> {

	public static final String SERIALIZABLE_NAME = "URLVectorCustomGraphics";
	
	private static final String DEF_TAG = "vector image";
	private static final String DEF_SVG = "TODO"; // TODO
	
	private String svg;
	
	public URLVectorCustomGraphics(Long id, String url) throws IOException {
		super(id, url);
		
		tags.add(DEF_TAG);
		buildCustomGraphics();
	}
	
	public URLVectorCustomGraphics(Long id, String name, String svg) throws IOException {
		super(id, name);
		
		if (svg == null || svg.isBlank())
			throw new IllegalArgumentException("'svg' must not be null or empty");
		
		this.svg = svg;
		tags.add(DEF_TAG);
		layers.add(new SVGLayer(svg));
	}

	@Override
	public Image getRenderedImage() {
		if (!layers.isEmpty())
			return ((SVGLayer) layers.get(0)).createImage(new Rectangle(width, height));
		
		return null;
	}
	
	/**
	 * Useful to draw to icons, components, etc.
	 */
	public void draw(Graphics2D g, Rectangle rect) {
		for (var cgl : layers)
			cgl.draw(g, rect, rect);
	}
	
	public String getSVG() {
		return svg;
	}
	
	private void buildCustomGraphics() {
		layers.clear();
		
		try {
			var sourceUrl = getSourceURL();
			
			try (var in = new BufferedReader(new InputStreamReader(sourceUrl.openStream()))) {
				var sb = new StringBuilder();
				String line = null;
				
				while ((line = in.readLine()) != null) {
		            sb.append(line);
		            sb.append("\n");
		        }
				
				svg = sb.toString();
			} catch (Exception e) {
				svg = DEF_SVG;
			}
		} catch (Exception e) {
			svg = DEF_SVG;
		}
		
		if (svg == null)
			svg = DEF_SVG;
		
		var cg = new SVGLayer(svg);
		layers.add(cg);
	}
}

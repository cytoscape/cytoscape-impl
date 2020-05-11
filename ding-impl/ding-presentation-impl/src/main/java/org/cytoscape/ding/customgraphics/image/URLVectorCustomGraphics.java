package org.cytoscape.ding.customgraphics.image;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;

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
	/** Layer used only to draw rendered images */
	private SVGLayer renderedImageLayer;
	
	public URLVectorCustomGraphics(Long id, URL url) throws IOException {
		super(id, url);
		
		tags.add(DEF_TAG);
	}
	
	public URLVectorCustomGraphics(Long id, String name, String svg) throws IOException {
		super(id, name);
		
		if (svg == null || svg.isBlank())
			throw new IllegalArgumentException("'svg' must not be null or empty");
		
		this.svg = svg;
		tags.add(DEF_TAG);
	}
	
	@Override
	public List<SVGLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> graphObject) {
		// IMPORTANT:
		//    We cannot return the cached layer here, or CustomGraphicsPositionCalculator will apply
		//    a position transformation repeatedly on the same layer, which will end up moving the image
		//    farther from the node on every repaint.
		var cg = createLayer();
		
		return layers = Collections.singletonList(cg);
	}

	@Override
	public Image getRenderedImage() {
		if (renderedImageLayer == null)
			renderedImageLayer = createLayer();
		
		return renderedImageLayer.createImage(new Rectangle(width, height));
	}
	
	public String getSVG() {
		return svg;
	}
	
	private SVGLayer createLayer() {
		if (svg == null) {
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
		}
		
		var cg = new SVGLayer(svg);
		
		return cg;
	}
}

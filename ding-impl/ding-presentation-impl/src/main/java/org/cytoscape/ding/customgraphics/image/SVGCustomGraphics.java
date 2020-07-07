package org.cytoscape.ding.customgraphics.image;

import java.awt.Image;
import java.awt.Rectangle;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.List;
import java.util.Scanner;

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
 * Render SVG images created from a URL.
 */
public class SVGCustomGraphics extends AbstractURLImageCustomGraphics<SVGLayer> {

	// DO NOT change, or you can break saving/restoring image_metadata.props!
	public static final String TYPE_NAMESPACE = "org.cytoscape.ding.customgraphics.image";
	// DO NOT change, or you can break saving/restoring image_metadata.props!
	public static final String TYPE_NAME = "SVGCustomGraphics";
	
	private static final String DEF_TAG = "vector image";
	private static final String DEF_IMAGE_FILE = "/images/no_image.svg";
	
	private String svg;
	/** Layer used only to draw rendered images */
	private SVGLayer renderedImageLayer;
	
	static String DEF_IMAGE;
	
	static {
		try (var scan = new Scanner(new BufferedInputStream(
				SVGCustomGraphics.class.getResourceAsStream(DEF_IMAGE_FILE)))) {
			var sb = new StringBuilder();

			while (scan.hasNextLine()) {
				sb.append(scan.nextLine());
				sb.append("\n");
			}
			
			DEF_IMAGE = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public SVGCustomGraphics(Long id, String name, URL url) throws IOException {
		super(id, name, url);
		
		tags.add(DEF_TAG);
	}
	
	public SVGCustomGraphics(Long id, String name, String svg) throws IOException {
		super(id, name);
		
		if (svg == null || svg.isBlank())
			throw new IllegalArgumentException("'svg' must not be null or empty");
		
		this.svg = svg;
		tags.add(DEF_TAG);
	}
	
	@Override
	public String getTypeNamespace() {
		// This way, we can refactor this class package without breaking the serialization and backwards compatibility.
		return TYPE_NAMESPACE;
	}
	
	@Override
	public String getTypeName() {
		// This way, we can refactor this class package without breaking the serialization and backwards compatibility.
		return TYPE_NAME;
	}
	
	@Override
	public List<SVGLayer> getLayers(CyNetworkView networkView, View<? extends CyIdentifiable> graphObject) {
		if (layers.isEmpty())
			layers.add(createLayer());

		return layers;
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
					svg = DEF_IMAGE;
				}
			} catch (Exception e) {
				svg = DEF_IMAGE;
			}
			
			if (svg == null)
				svg = DEF_IMAGE;
		}
		
		var cg = new SVGLayer(svg);
		
		return cg;
	}
}
package org.cytoscape.cg.model;

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
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.model.table.CyTableView;

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
	
	/** Layer used to draw the custom graphics directly onto the network view or other component */
	private final SVGLayer svgLayer;
	
	/** Layer used only to draw rendered images */
	private SVGLayer renderedImageLayer;
	
	public static String DEF_IMAGE;
	
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
		
		svgLayer = createLayer();
		init();
	}
	
	public SVGCustomGraphics(Long id, String name, String svg) throws IOException {
		super(id, name);
		
		if (svg == null || svg.isBlank())
			throw new IllegalArgumentException("'svg' must not be null or empty");
		
		this.svg = svg;
		svgLayer = createLayer();
		init();
	}
	
	public SVGCustomGraphics(Long id, String name, URL url, String svg) throws IOException {
		this(id, name, svg);
		
		sourceUrl = url;
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
		return getLayers();
	}
	
	@Override
	public List<SVGLayer> getLayers(CyTableView tableView, CyColumnView columnView) {
		return getLayers();
	}
	
	public List<SVGLayer> getLayers() {
		if (layers.isEmpty())
			layers.add(svgLayer);

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
	
	private void init() {
		tags.add(DEF_TAG);
		
		var bounds = svgLayer.getBounds2D().getBounds();
		width = bounds.width;
		height = bounds.height;
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
	
	@Override
	public int hashCode() {
		int prime = 31;
		int result = 13;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof SVGCustomGraphics))
			return false;
		
		var other = (SVGCustomGraphics) obj;
		
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id)) {
			return false;
		}
		
		return true;
	}
}

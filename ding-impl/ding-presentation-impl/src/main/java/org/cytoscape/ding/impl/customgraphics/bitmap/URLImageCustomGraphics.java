package org.cytoscape.ding.impl.customgraphics.bitmap;

import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.cytoscape.ding.customgraphics.ImageUtil;
import org.cytoscape.ding.customgraphics.paint.TexturePaintFactory;
import org.cytoscape.ding.impl.customgraphics.AbstractDCustomGraphics;
import org.cytoscape.ding.impl.customgraphics.DLayer;
import org.cytoscape.graph.render.stateful.CustomGraphic;
import org.cytoscape.graph.render.stateful.PaintFactory;

public class URLImageCustomGraphics extends AbstractDCustomGraphics {

	private static final String DEF_IMAGE_FILE = "images/no_image.png";
	private static BufferedImage DEF_IMAGE;
	
	static  {
		try {
			DEF_IMAGE =ImageIO.read(URLImageCustomGraphics.class
					.getClassLoader().getResource(DEF_IMAGE_FILE));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static final String DEF_TAG = "bitmap image";

	private CustomGraphic cg;

	private BufferedImage originalImage;
	private BufferedImage scaledImage;

	private URL sourceUrl;

	
	public URLImageCustomGraphics(String url) throws IOException {
		super(url);
		this.tags.add(DEF_TAG);
		createImage(url);
		buildCustomGraphics(originalImage);
	}
	
	
	public URLImageCustomGraphics(Long id, String url) throws IOException {
		super(id, url);
		this.tags.add(DEF_TAG);
		createImage(url);
		buildCustomGraphics(originalImage);
	}

	/**
	 * 
	 * @param name
	 *            - display name of this object. NOT UNIQUE!
	 * @param img
	 */
	public URLImageCustomGraphics(String name, BufferedImage img) {
		super(name);
		if (img == null)
			throw new IllegalArgumentException("Image cannot be null.");

		this.tags.add(DEF_TAG);
		this.originalImage = img;
		buildCustomGraphics(originalImage);
	}

	private void buildCustomGraphics(BufferedImage targetImg) {
		layers.clear();

		Rectangle2D bound = null;
		width = targetImg.getWidth();
		height = targetImg.getHeight();

		bound = new Rectangle2D.Double(-width / 2, -height / 2, width, height);
		final PaintFactory paintFactory = new TexturePaintFactory(targetImg);

		cg = new CustomGraphic(bound, paintFactory);
		
		// This object is always one layer, so simply add without sorting.
		DLayer layer = new DLayer(cg, 1);
		layers.add(layer);
	}

	private void createImage(String url) throws MalformedURLException {
		if (url == null)
			throw new IllegalStateException("URL string cannot be null.");

		URL imageLocation = new URL(url);
		
		sourceUrl = imageLocation;
		try {
			originalImage = ImageIO.read(imageLocation);
		} catch (IOException e) {
			originalImage = DEF_IMAGE;
		}

		if (originalImage == null) {
			originalImage = DEF_IMAGE;
		}
	}

	@Override
	public Image getRenderedImage() {
		
		if (width == originalImage.getWidth() && height == originalImage.getHeight()) {
			return originalImage;
		}
		
		if(scaledImage == null) {
			resizeImage(width, height);
		} else if (scaledImage.getWidth() != width || scaledImage.getHeight() != height) {
			resizeImage(width, height);
		} 
		
		return scaledImage;
	}

	
	
	private Image resizeImage(int width, int height) {
		final Image img = originalImage.getScaledInstance(width, height,
				Image.SCALE_AREA_AVERAGING);
		try {
			scaledImage = ImageUtil.toBufferedImage(img);
		} catch (InterruptedException e) {
			// Could not get scaled one
			e.printStackTrace();
			return originalImage;
		}
		buildCustomGraphics(scaledImage);
		return scaledImage;
	}

	public Image resetImage() {
		if (scaledImage != null) {
			scaledImage.flush();
			scaledImage = null;
		}
		buildCustomGraphics(originalImage);
		return originalImage;
	}
	

	public URL getSourceURL() {
		return this.sourceUrl;
	}

}

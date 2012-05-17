package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import java.util.Map;

import java.net.URL;

import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.ImageUtil;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.ding.impl.DGraphView;

import org.cytoscape.ding.impl.cyannotator.CyAnnotator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageAnnotation extends Annotation {

	private static final Logger logger = LoggerFactory.getLogger(ImageAnnotation.class);

	private BufferedImage image;
	private	URL url = null;
	private static final String URL="URL";
	private static final String WIDTH="width";
	private static final String HEIGHT="height";
	
	private double imageWidth=0, imageHeight=0;
	private BufferedImage resizedImage;
	private CyCustomGraphics cg = null;
	private CustomGraphicsManager customGraphicsManager;

	public static final String NAME="IMAGE";

	// XXX HACK to force the custom graphics manager to respect these graphics
	public void preserveCustomGraphics() {
		for (CyCustomGraphics cg: customGraphicsManager.getAllCustomGraphics()) {
			customGraphicsManager.setUsedInCurrentSession(cg, true);
		}
	}

	public ImageAnnotation() { super(); }

	public ImageAnnotation(CyAnnotator cyAnnotator, DGraphView view, int x, int y, 
	                       URL url, BufferedImage image, int compCount, double zoom, 
	                       CustomGraphicsManager customGraphicsManager) {
		super(cyAnnotator, view, x, y, compCount, zoom);
		this.image=image;
		this.customGraphicsManager = customGraphicsManager;
		imageWidth=image.getWidth();
		imageHeight=image.getHeight();
		this.url = url;
		resizedImage=resize(image, (int)imageWidth, (int)imageHeight);
		final Long id = customGraphicsManager.getNextAvailableID();
		this.cg = new URLImageCustomGraphics(id, url.toString(), image);
		customGraphicsManager.addCustomGraphics(cg, url);
		customGraphicsManager.setUsedInCurrentSession(cg, true);
		updateAnnotationAttributes();
	}

	public ImageAnnotation(CyAnnotator cyAnnotator, DGraphView view, 
	                       Map<String, String> argMap, CustomGraphicsManager customGraphicsManager) {
		super(cyAnnotator, view, argMap);
		this.customGraphicsManager = customGraphicsManager;
		// Get the image from the image pool
		try {
			this.url = new URL(argMap.get(URL));
			this.cg = customGraphicsManager.getCustomGraphicsBySourceURL(this.url);
			this.image = ImageUtil.toBufferedImage(cg.getRenderedImage());
			customGraphicsManager.addCustomGraphics(cg, this.url);
			customGraphicsManager.setUsedInCurrentSession(cg, true);
		} catch (Exception e) {
			logger.warn("Unable to restore image '"+argMap.get(URL)+"'",e);
			return;
		}

		imageWidth = Double.parseDouble(argMap.get(WIDTH));
		imageHeight = Double.parseDouble(argMap.get(HEIGHT));
		resizedImage=resize(image, (int)image.getWidth(), (int)image.getHeight());
		updateAnnotationAttributes();
	}

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE, NAME);
		argMap.put(URL, url.toString());
		argMap.put(WIDTH, Double.toString(imageWidth));
		argMap.put(HEIGHT, Double.toString(imageHeight));
		customGraphicsManager.setUsedInCurrentSession(cg, true);

		return argMap;
	}

	//Returns a resized high quality BufferedImage
	private static BufferedImage resize(BufferedImage image, int width, int height)
	{
		if (image == null) {
			if (width == 0) width = 1;
			if (height == 0) height = 1;
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		}

		int type = image.getType() == 0? BufferedImage.TYPE_INT_ARGB : image.getType();
		if(height==0)
			height++;
		if(width==0)
			width++;
		BufferedImage resizedImage = new BufferedImage(width, height, type);
		Graphics2D g = resizedImage.createGraphics();
		g.setComposite(AlphaComposite.Src);
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		g.drawImage(image, 0, 0, width, height, null);
		g.dispose();
		return resizedImage;
	}

	public void dropImage() {
		customGraphicsManager.setUsedInCurrentSession(cg, false);
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.paint(g);
		
		Graphics2D g2=(Graphics2D)g;

		g2.setComposite(AlphaComposite.Src);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		int width = (int)Math.round(imageWidth*scaleFactor/zoom);
		int height = (int)Math.round(imageHeight*scaleFactor/zoom);
		BufferedImage newImage =resize(image, width, height);

		g2.drawImage(newImage, (int)(x*scaleFactor), (int)(y*scaleFactor), null);
	}

	@Override
	public void paint(Graphics g) {				
		super.paint(g);
		
		Graphics2D g2=(Graphics2D)g;

		g2.setComposite(AlphaComposite.Src);

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		g2.drawImage(resizedImage, getX(), getY(), null);
		
		if(isSelected()) {
			g2.setColor(Color.YELLOW);
			g2.setStroke(new BasicStroke(2.0f));
			g2.drawRect(getTopX(), getTopY(), getAnnotationWidth(), getAnnotationHeight());
		}
	}


	@Override
	public void adjustSpecificZoom(double newZoom) {

		double factor=newZoom/getTempZoom();
		
		imageWidth=imageWidth*factor;
		imageHeight=imageHeight*factor;

		resizedImage=resize(image, (int)Math.round(imageWidth), (int)Math.round(imageHeight));

		setBounds(getX(), getY(), getAnnotationWidth(), getAnnotationHeight());
	   
		setTempZoom(newZoom);		
		updateAnnotationAttributes();
	}


	@Override
	public void adjustZoom(double newZoom) {

		double factor=newZoom/getZoom();
		
		adjustArrowThickness(newZoom);

		imageWidth=imageWidth*factor;
		imageHeight=imageHeight*factor;

		resizedImage=resize(image, (int)Math.round(imageWidth), (int)Math.round(imageHeight));

		setBounds(getX(), getY(), getAnnotationWidth(), getAnnotationHeight());
				
		setZoom(newZoom);		
		updateAnnotationAttributes();
	}


	public int getAnnotationWidth() {
		return (int)Math.round(imageWidth);
	}

	public int getAnnotationHeight() {
		return (int)Math.round(imageHeight);
	}

	@Override
	public boolean isImageAnnotation() {
		return true;
	}
}

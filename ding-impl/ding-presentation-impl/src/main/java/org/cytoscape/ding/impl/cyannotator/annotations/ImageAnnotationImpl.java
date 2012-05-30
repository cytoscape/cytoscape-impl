package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;

import java.util.Map;

import java.net.URL;

import javax.swing.JFrame;

import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.ImageUtil;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;

import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.ding.impl.cyannotator.CyAnnotator;
import org.cytoscape.ding.impl.cyannotator.api.Annotation;
import org.cytoscape.ding.impl.cyannotator.api.ImageAnnotation;
import org.cytoscape.ding.impl.cyannotator.modify.mImageAnnotation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageAnnotationImpl extends AbstractAnnotation implements ImageAnnotation {
	private BufferedImage image;
	private	URL url = null;

	public static final String NAME="IMAGE";
	private static final String URL="URL";
	private static final String WIDTH="width";
	private static final String HEIGHT="height";
	
	protected double imageWidth=0, imageHeight=0;
	private BufferedImage resizedImage;
	private CyCustomGraphics cg = null;
	protected CustomGraphicsManager customGraphicsManager;

	private static final Logger logger = LoggerFactory.getLogger(ImageAnnotationImpl.class);


	// XXX HACK to force the custom graphics manager to respect these graphics
	public void preserveCustomGraphics() {
		for (CyCustomGraphics cg: customGraphicsManager.getAllCustomGraphics()) {
			customGraphicsManager.setUsedInCurrentSession(cg, true);
		}
	}

	public ImageAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view) { super(cyAnnotator, view); }

	public ImageAnnotationImpl(ImageAnnotationImpl c) { 
		super(c);
		this.image = c.image;
		this.customGraphicsManager = c.customGraphicsManager;
		imageWidth=image.getWidth();
		imageHeight=image.getHeight();
		this.url = c.url;
	}

	public ImageAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, int x, int y, 
	                           URL url, BufferedImage image, double zoom, 
	                           CustomGraphicsManager customGraphicsManager) {
		super(cyAnnotator, view, x, y, zoom);
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

	public ImageAnnotationImpl(CyAnnotator cyAnnotator, DGraphView view, 
	                           Map<String, String> argMap, CustomGraphicsManager customGraphicsManager) {
		super(cyAnnotator, view, argMap);
		this.customGraphicsManager = customGraphicsManager;

		imageWidth = Double.parseDouble(argMap.get(WIDTH));
		imageHeight = Double.parseDouble(argMap.get(HEIGHT));

		this.image = null;
		this.resizedImage = null;

		// Get the image from the image pool
		try {
			this.url = new URL(argMap.get(URL));
			this.cg = customGraphicsManager.getCustomGraphicsBySourceURL(this.url);
			if (cg != null) {
				this.image = ImageUtil.toBufferedImage(cg.getRenderedImage());
				customGraphicsManager.addCustomGraphics(cg, this.url);
				customGraphicsManager.setUsedInCurrentSession(cg, true);
				resizedImage=resize(image, (int)image.getWidth(), (int)image.getHeight());
			}
		} catch (Exception e) {
			logger.warn("Unable to restore image '"+argMap.get(URL)+"'",e);
			return;
		}
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

	public void reloadImage()
	{
		// Get the image from the image pool again
		try {
			this.cg = customGraphicsManager.getCustomGraphicsBySourceURL(this.url);
			if (cg != null) {
				this.image = ImageUtil.toBufferedImage(cg.getRenderedImage());
				customGraphicsManager.addCustomGraphics(cg, this.url);
				customGraphicsManager.setUsedInCurrentSession(cg, true);
			} else {
				return;
			}
		} catch (Exception e) {
			logger.warn("Unable to restore image '"+this.url+"'",e);
			return;
		}
		resizedImage=resize(image, (int)imageWidth, (int)imageHeight);

		updateAnnotationAttributes();
	}

	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		if (image instanceof BufferedImage)
			this.image = (BufferedImage)image;
		else if (image instanceof VolatileImage)
			this.image = ((VolatileImage)image).getSnapshot();
		else
			return;

		this.imageWidth=this.image.getWidth();
		this.imageHeight=this.image.getHeight();
		resizedImage=resize(this.image, (int)resizedImage.getWidth(), (int)resizedImage.getHeight());
		getCyAnnotator().update();
	}

	public void setImage(URL url) {
		this.url = url;
		reloadImage();
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

	public JFrame getModifyDialog(Annotation annotation) {
			return new mImageAnnotation(this);
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);
		
		Graphics2D g2=(Graphics2D)g;

		int width = (int)Math.round(imageWidth*scaleFactor/getZoom());
		int height = (int)Math.round(imageHeight*scaleFactor/getZoom());
		BufferedImage newImage =resize(image, width, height);
		if (newImage == null) return;

		boolean selected = isSelected();
		setSelected(false);
		g2.drawImage(newImage, (int)(x*scaleFactor), (int)(y*scaleFactor), null);
		setSelected(selected);
	}

	@Override
	public void paint(Graphics g) {				
		super.paint(g);

		Graphics2D g2=(Graphics2D)g;

		if (resizedImage == null) return;

		g2.drawImage(resizedImage, getX(), getY(), null);
		
		if(isSelected()) {
			g2.setColor(Color.YELLOW);
			g2.setStroke(new BasicStroke(2.0f));
			g2.drawRect(getX()-1, getY()-1, getAnnotationWidth()+1, getAnnotationHeight()+1);
		}
	}


	@Override
	public void setSpecificZoom(double newZoom) {

		double factor=newZoom/getSpecificZoom();
		
		imageWidth=imageWidth*factor;
		imageHeight=imageHeight*factor;

		resizedImage=resize(image, (int)Math.round(imageWidth), (int)Math.round(imageHeight));

		setBounds(getX(), getY(), getAnnotationWidth(), getAnnotationHeight());
	   
		super.setSpecificZoom(newZoom);		
	}


	@Override
	public void setZoom(double newZoom) {

		double factor=newZoom/getZoom();
		
		imageWidth=imageWidth*factor;
		imageHeight=imageHeight*factor;

		resizedImage=resize(image, (int)Math.round(imageWidth), (int)Math.round(imageHeight));

		setBounds(getX(), getY(), getAnnotationWidth(), getAnnotationHeight());
				
		super.setZoom(newZoom);		
	}


	public int getAnnotationWidth() {
		return (int)Math.round(imageWidth);
	}

	public int getAnnotationHeight() {
		return (int)Math.round(imageHeight);
	}
}

package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
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
import org.cytoscape.ding.impl.cyannotator.api.ShapeAnnotation;
import org.cytoscape.ding.impl.cyannotator.dialogs.ImageAnnotationDialog;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageAnnotationImpl extends AbstractAnnotation implements ImageAnnotation {
	private BufferedImage image;
	private	URL url = null;

	public static final String NAME="IMAGE";
	private static final String URL="URL";
	private static final String WIDTH="width";
	private static final String HEIGHT="height";
	private static final String OPACITY="opacity";
	private static final String CONTRAST="contrast";
	private static final String LIGHTNESS="brightness";

	private static final float MAX_CONTRAST=4.0f;
	
	protected double imageWidth=0, imageHeight=0;
	private BufferedImage resizedImage;
	private float opacity = 1.0f;
	private int brightness = 0;
	private int contrast = 0;
	private CyCustomGraphics cg = null;
	protected CustomGraphicsManager customGraphicsManager;

	private double borderWidth = 0.0;
	private Paint borderColor = null;

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
		resizedImage=resizeImage((int)imageWidth, (int)imageHeight);
		final Long id = customGraphicsManager.getNextAvailableID();
		this.cg = new URLImageCustomGraphics(id, url.toString(), image);
		customGraphicsManager.addCustomGraphics(cg, url);
		customGraphicsManager.setUsedInCurrentSession(cg, true);
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
				resizedImage=resizeImage((int)image.getWidth(), (int)image.getHeight());
			}
		} catch (Exception e) {
			logger.warn("Unable to restore image '"+argMap.get(URL)+"'",e);
			return;
		}
		if (argMap.containsKey(OPACITY))
			opacity = Float.parseFloat(argMap.get(OPACITY));

		if (argMap.containsKey(LIGHTNESS))
			brightness = Integer.parseInt(argMap.get(LIGHTNESS));

		if (argMap.containsKey(CONTRAST))
			contrast = Integer.parseInt(argMap.get(CONTRAST));

	}

	public Map<String,String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE, NAME);
		argMap.put(URL, url.toString());
		argMap.put(WIDTH, Double.toString(imageWidth));
		argMap.put(HEIGHT, Double.toString(imageHeight));
		argMap.put(OPACITY, Float.toString(opacity));
		argMap.put(LIGHTNESS, Integer.toString(brightness));
		argMap.put(CONTRAST, Integer.toString(contrast));
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
		resizedImage=resizeImage((int)imageWidth, (int)imageHeight);
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
		
		int width = (int)this.image.getWidth();
		int height = (int)this.image.getHeight();
		if (resizedImage != null) {
			width = (int)resizedImage.getWidth();
			height = (int)resizedImage.getHeight();
		}
		resizedImage=resizeImage((int)width, (int)height);
		if (!usedForPreviews())
			getCanvas().repaint();
	}

	public void setImage(URL url) {
		this.url = url;
		reloadImage();
	}

	public URL getImageURL() {
		return url;
	}

	public void setImageOpacity(float opacity) {
		this.opacity = opacity;
		resizedImage=null;
	}
	public float getImageOpacity() { return this.opacity; }

	public void setImageBrightness(int brightness) {
		this.brightness = brightness;
		resizedImage=null;
	}
	public int getImageBrightness() { return this.brightness; }

	public void setImageContrast(int contrast) {
		this.contrast = contrast;
		resizedImage=null;
	}
	public int getImageContrast() { return this.contrast; }

	// Shape annotation methods.  We add these so we can get resizeImage functionality
	public ShapeType[] getSupportedShapes() {
		ShapeType[] types = {ShapeType.RECTANGLE};
		return types;
	}

	public void setSize(double width, double height) {
		this.imageWidth = width;
		this.imageHeight = height;

		// Resize the image
		resizedImage=resizeImage((int)imageWidth, (int)imageHeight);
		if (!usedForPreviews())
			getCanvas().repaint();

		setSize((int)width, (int)height);
	}

	public ShapeType getShapeType() {
		return ShapeType.RECTANGLE;
	}
	public void setShapeType(ShapeType type) {}

	public double getBorderWidth() {
		return borderWidth;
	}

	public void setBorderWidth(double width) {
		borderWidth = width*getZoom();
	}

	public Paint getBorderColor() {return borderColor;}
	public Paint getFillColor() {return null;}

	public void setBorderColor(Paint border) {borderColor = border;}
	public void setFillColor(Paint fill) {}

	public Shape getShape() {
		return new Rectangle2D.Double((double)getX(), (double)getY(), imageWidth, imageHeight);
	}
	

	//Returns a resizeImaged high quality BufferedImage
	private BufferedImage resizeImage(int width, int height)
	{
		if (image == null) {
			if (width == 0) width = 1;
			if (height == 0) height = 1;
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}

		int type = image.getType() == 0? BufferedImage.TYPE_INT_RGB : image.getType();
		if(height==0)
			height++;
		if(width==0)
			width++;

		BufferedImage adjustedImage = image;

		// Handle image adjustments
		if (contrast != 0 || brightness != 0) {
			BufferedImage source = image;
			// This only works for RGB
			if (type != BufferedImage.TYPE_INT_RGB) {
				BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), 
				                                           BufferedImage.TYPE_INT_RGB);
				Graphics2D g = rgbImage.createGraphics();
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), this);
				source = rgbImage;
			}
			adjustedImage = new BufferedImage(image.getWidth(), image.getHeight(), 
				                                BufferedImage.TYPE_INT_RGB);

			// Do Brightness first...
			// offset goes from -255 - 255 for RGB
			float offset = (float)brightness*255.0f/100.0f;
			RescaleOp op = new RescaleOp(1.0f, offset, null);
			op.filter(source, adjustedImage);

			float scaleFactor = 1.0f;
			// scaleFactor goes from 0-4.0 with a 
			if (contrast <= 0) {
				scaleFactor = 1.0f + ((float)contrast)/100.0f;
			} else
				scaleFactor = 1.0f + ((float)contrast)*3.0f/100.0f;
		
			op = new RescaleOp(scaleFactor, 0.0f, null);
			op.filter(adjustedImage, adjustedImage);
		}

		BufferedImage newImage = new BufferedImage(width, height, type);
		Graphics2D g = newImage.createGraphics();
		g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);

		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity);
		g.setComposite(ac);
		g.drawImage(adjustedImage, 0, 0, width, height, this);
		g.dispose();
		return newImage;
	}

	public void dropImage() {
		customGraphicsManager.setUsedInCurrentSession(cg, false);
	}

	public JFrame getModifyDialog() {
			return new ImageAnnotationDialog(this);
	}

	@Override
	public void drawAnnotation(Graphics g, double x, double y, double scaleFactor) {
		super.drawAnnotation(g, x, y, scaleFactor);
		
		Graphics2D g2=(Graphics2D)g;

		int width = (int)Math.round(imageWidth*scaleFactor/getZoom());
		int height = (int)Math.round(imageHeight*scaleFactor/getZoom());
		BufferedImage newImage =resizeImage(width, height);
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

		if (image == null)
			return;

		if (resizedImage == null)
			resizedImage = resizeImage((int)imageWidth, (int)imageHeight);

		// int x = getX();
		// int y = getY();
		int x = 0;
		int y = 0;

		if (usedForPreviews()) {
			x = 0; y = 0;
		}

		g2.drawImage(resizedImage, x, y, this);

		if (borderColor != null && borderWidth > 0.0) {
			g2.setPaint(borderColor);
			g2.setStroke(new BasicStroke((float)borderWidth));
			g2.drawRect(x, y, getAnnotationWidth(), getAnnotationHeight());
		}
		
		if(isSelected()) {
			g2.setColor(Color.YELLOW);
			g2.setStroke(new BasicStroke(2.0f));
			g2.drawRect(x, y, getAnnotationWidth(), getAnnotationHeight());
		}
	}

	@Override
	public void setSpecificZoom(double newZoom) {

		double factor=newZoom/getSpecificZoom();
		
		imageWidth=imageWidth*factor;
		imageHeight=imageHeight*factor;

		resizedImage=resizeImage((int)Math.round(imageWidth), (int)Math.round(imageHeight));

		setBounds(getX(), getY(), getAnnotationWidth(), getAnnotationHeight());
	   
		super.setSpecificZoom(newZoom);		
	}

	@Override
	public void setZoom(double newZoom) {

		double factor=newZoom/getZoom();
		
		imageWidth=imageWidth*factor;
		imageHeight=imageHeight*factor;

		borderWidth*=factor;

		resizedImage=resizeImage((int)Math.round(imageWidth), (int)Math.round(imageHeight));

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

package org.cytoscape.ding.impl.cyannotator.annotations;

import java.awt.AlphaComposite;
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
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.cytoscape.cg.model.AbstractURLImageCustomGraphics;
import org.cytoscape.cg.model.BitmapCustomGraphics;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.model.SVGCustomGraphics;
import org.cytoscape.cg.model.SVGLayer;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.internal.util.ImageUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.annotations.ShapeAnnotation;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

public class ImageAnnotationImpl extends ShapeAnnotationImpl implements ImageAnnotation {
	
	private String svg;
	private BufferedImage image;
	private BufferedImage modifiedImage;
	private	URL url;

	private float opacity = 1.0f;
	private int brightness;
	private int contrast;
	private CyCustomGraphics<?> cg;
	
	protected final CustomGraphicsManager customGraphicsManager;

	private static final Logger logger = LoggerFactory.getLogger(ImageAnnotationImpl.class);
	
	public ImageAnnotationImpl(ImageAnnotationImpl annotation, boolean usedForPreviews) {
		super((ShapeAnnotationImpl) annotation, 0, 0, usedForPreviews);
		
		if (annotation == null)
			throw new IllegalArgumentException("'annotation' must not be null.");
		
		this.image = annotation.image;
		this.svg = annotation.svg;
		this.customGraphicsManager = annotation.customGraphicsManager;
		this.cg = annotation.cg;
		this.width = annotation.getWidth();
		this.height = annotation.getHeight();
		this.url = annotation.url;
		this.opacity = annotation.opacity;
		this.brightness = annotation.brightness;
		this.contrast = annotation.contrast;
		
		setBorderWidth(0.0); // Our default border width is 0
		name = annotation.getName() != null ? annotation.getName() : getDefaultName();
	}

	public ImageAnnotationImpl(
			DRenderingEngine re,
			double x,
			double y,
			double rotation,
			URL url,
			BufferedImage image,
			double zoom,
			CustomGraphicsManager customGraphicsManager
	) {
		super(re, x, y, rotation, ShapeType.RECTANGLE, 0, 0, null, null, 0.0f);

		if (image == null)
			throw new IllegalArgumentException("'image' must not be null.");
		if (customGraphicsManager == null)
			throw new IllegalArgumentException("'customGraphicsManager' must not be null.");
		
		this.image = image;
		this.customGraphicsManager = customGraphicsManager;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.url = url;
		this.cg = new BitmapCustomGraphics(customGraphicsManager.getNextAvailableID(), url.toString(), image);
		
		customGraphicsManager.addCustomGraphics(cg, url);
		customGraphicsManager.setUsedInCurrentSession(cg, true);
		name = getDefaultName();
	}
	
	public ImageAnnotationImpl(
			DRenderingEngine re,
			double x,
			double y,
			double rotation,
			URL url,
			String svg,
			double zoom,
			CustomGraphicsManager customGraphicsManager
	) {
		super(re, x, y, rotation, ShapeType.RECTANGLE, 0, 0, null, null, 0.0f);
		
		if (svg == null)
			throw new IllegalArgumentException("'svg' must not be null.");
		if (customGraphicsManager == null)
			throw new IllegalArgumentException("'customGraphicsManager' must not be null.");
		
		this.svg = svg;
		this.customGraphicsManager = customGraphicsManager;
		this.url = url;
		
		try {
			cg = new SVGCustomGraphics(customGraphicsManager.getNextAvailableID(), url.toString(), url, svg);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		width = cg.getWidth();
		height = cg.getHeight();
		
		customGraphicsManager.addCustomGraphics(cg, url);
		customGraphicsManager.setUsedInCurrentSession(cg, true);
		name = getDefaultName();
	}
	
	public ImageAnnotationImpl(
			DRenderingEngine re,
			AbstractURLImageCustomGraphics<?> cg,
			int x,
			int y,
			double rotation,
			double zoom,
			CustomGraphicsManager customGraphicsManager
	) {
		super(re, x, y, rotation, ShapeType.RECTANGLE, 0, 0, null, null, 0.0f);
		
		if (cg == null)
			throw new IllegalArgumentException("'cg' must not be null.");
		if (customGraphicsManager == null)
			throw new IllegalArgumentException("'customGraphicsManager' must not be null.");
		
		this.cg = cg;
		this.url = cg.getSourceURL();
		this.customGraphicsManager = customGraphicsManager;
		
		if (cg instanceof SVGCustomGraphics) {
			svg = ((SVGCustomGraphics) cg).getSVG();
			width = cg.getWidth();
			height = cg.getHeight();
		} else if (cg instanceof BitmapCustomGraphics) {
			image = ((BitmapCustomGraphics) cg).getOriginalImage();
			width = image.getWidth();
			height = image.getHeight();
		} else {
			throw new IllegalArgumentException("'cg' must be a BitmapCustomGraphics or SVGCustomGraphics.");
		}
		
		customGraphicsManager.addCustomGraphics(cg, url);
		customGraphicsManager.setUsedInCurrentSession(cg, true);
		name = getDefaultName();
	}

	public ImageAnnotationImpl(
			DRenderingEngine re,
			Map<String, String> argMap,
			CustomGraphicsManager customGraphicsManager
	) {
		super(re, argMap);
		
		if (argMap == null)
			throw new IllegalArgumentException("'argMap' must not be null.");
		if (customGraphicsManager == null)
			throw new IllegalArgumentException("'customGraphicsManager' must not be null.");
		
		this.customGraphicsManager = customGraphicsManager;
		
		opacity = clamp(ViewUtils.getFloat(argMap, OPACITY, 1f), 0f, 1f);
		brightness = ViewUtils.getInteger(argMap, LIGHTNESS, 0);
		contrast = ViewUtils.getInteger(argMap, CONTRAST, 0);

		if (argMap.containsKey(URL)) {
			// Get the image from the image pool
			try {
				url = new URL(argMap.get(URL));
				reloadImage();

				if (name == null)
					name = getDefaultName();

				double aspectRatio = (double) image.getHeight() / (double) image.getWidth();
				// If the user didn't specify the WIDTH and HEIGHT, it will be set to 100X100.
				// We want to
				// scale that to the appropriate aspect ratio
				if (argMap.containsKey(ShapeAnnotation.WIDTH) && argMap.containsKey(ShapeAnnotation.HEIGHT))
					return;

				// If the user specified the width, adjust the height
				if (argMap.containsKey(ShapeAnnotation.WIDTH))
					height = width * aspectRatio;
				else
					width = height / aspectRatio;

			} catch (Exception e) {
				logger.warn("Unable to restore image '" + argMap.get(URL) + "'", e);
			}
		}
	}
	
	public ImageAnnotationImpl(
			DRenderingEngine re,
			boolean usedForPreviews,
			CustomGraphicsManager customGraphicsManager
	) {
		super(re, 0, 0, usedForPreviews);
		
		if (customGraphicsManager == null)
			throw new IllegalArgumentException("'customGraphicsManager' must not be null.");
		
		this.customGraphicsManager = customGraphicsManager;
	}
	
	// XXX HACK to force the custom graphics manager to respect these graphics
	public void preserveCustomGraphics() {
		for (var cg : customGraphicsManager.getAllCustomGraphics())
			customGraphicsManager.setUsedInCurrentSession(cg, true);
	}
	
	private static float clamp(float val, float min, float max) {
	    return Math.max(min, Math.min(max, val));
	}
	

	@Override
	public Class<? extends Annotation> getType() {
		return ImageAnnotation.class;
	}
	
	@Override
	public Map<String, String> getArgMap() {
		var argMap = super.getArgMap();
		if (argMap.containsKey(ShapeAnnotation.FILLOPACITY))
			argMap.remove(ShapeAnnotation.FILLOPACITY);
		if (argMap.containsKey(ShapeAnnotation.FILLCOLOR))
			argMap.remove(ShapeAnnotation.FILLCOLOR);

		argMap.put(TYPE, ImageAnnotation.class.getName());
		
		if (url != null)
			argMap.put(URL, url.toString());
		
		argMap.put(ImageAnnotation.WIDTH, Double.toString(width));
		argMap.put(ImageAnnotation.HEIGHT, Double.toString(height));
		argMap.put(OPACITY, Float.toString(opacity));
		argMap.put(LIGHTNESS, Integer.toString(brightness));
		argMap.put(CONTRAST, Integer.toString(contrast));
		
		if (cg != null)
			customGraphicsManager.setUsedInCurrentSession(cg, true);

		return argMap;
	}
	
	/**
	 * Width and height are not applied, only colors, shape, image adjustments, etc.
	 */
	@Override
	public void setStyle(Map<String, String> argMap) {
		super.setStyle(argMap);
		
		if (argMap != null) {
			setImageOpacity(ViewUtils.getFloat(argMap, OPACITY, 1.0f));
			setImageBrightness(ViewUtils.getInteger(argMap, LIGHTNESS, 0));
			setImageContrast(ViewUtils.getInteger(argMap, CONTRAST, 0));
		}
	}

	public void reloadImage() {
		// Get the image from the image pool again
		try {
			cg = customGraphicsManager.getCustomGraphicsBySourceURL(url);
			
			if (cg != null) {
				if (!isSVG())
					image = ImageUtil.toBufferedImage(cg.getRenderedImage());
				modifiedImage = null;

				customGraphicsManager.addCustomGraphics(cg, url);
				customGraphicsManager.setUsedInCurrentSession(cg, true);
			} else {
				try {
					// We don't already have the image -- fetch it
					image = ImageIO.read(url);
					cg = new BitmapCustomGraphics(customGraphicsManager.getNextAvailableID(), url.toString(), image);
					customGraphicsManager.addCustomGraphics(cg, url);
					customGraphicsManager.setUsedInCurrentSession(cg, true);
					modifiedImage = null;
				} catch (Exception e) {
					logger.error("Unable to read image from " + url.toString() + ": " + e.getMessage());
					throw e;
				}
			}
		} catch (Exception e) {
			logger.warn("Unable to restore image '" + url + "'", e);
			return;
		}
	}

	@Override
	public Image getImage() {
		if (image == null)
			return getModifiedImage();
		
		return image;
	}

	@Override
	public void setImage(Image image) {
		var oldValue = this.image;
		
		if (image instanceof BufferedImage)
			this.image = (BufferedImage) image;
		else if (image instanceof VolatileImage)
			this.image = ((VolatileImage) image).getSnapshot();
		else
			return;

		svg = null;
		update();
		firePropertyChange("image", oldValue, image);
	}
	
	@Override
	public String getSVG() {
		return svg;
	}

	@Override
	public void setSVG(String svg) {
		this.svg = svg;
		image = null;
		update();
	}

	@Override
	public void setImage(URL url) {
		var oldValue = this.url;
		this.url = url;
		reloadImage();
		getModifiedImage();
		update();
		firePropertyChange("imageURL", oldValue, url);
	}

	@Override
	public URL getImageURL() {
		return url;
	}

	@Override
	public void setImageOpacity(float opacity) {
		opacity = clamp(opacity, 0f, 1f);
		if (this.opacity != opacity) {
			var oldValue = this.opacity;
			this.opacity = opacity;
			update();
			firePropertyChange("imageOpacity", oldValue, opacity);
		}
	}

	@Override
	public float getImageOpacity() {
		return opacity;
	}

	@Override
	public void setImageBrightness(int brightness) {
		if (this.brightness != brightness) {
			var oldValue = this.brightness;
			this.brightness = brightness;
			modifiedImage = null;
			update();
			firePropertyChange("imageBrightness", oldValue, brightness);
		}
	}

	@Override
	public int getImageBrightness() {
		return brightness;
	}

	@Override
	public void setImageContrast(int contrast) {
		if (this.contrast != contrast) {
			var oldValue = this.contrast;
			this.contrast = contrast;
			modifiedImage = null;
			update();
			firePropertyChange("imageContrast", oldValue, contrast);
		}
	}

	@Override
	public int getImageContrast() {
		return contrast;
	}
	
	@Override
	public void setSize(double width, double height) {
		if (this.width != width || this.height != height) {
			super.setSize(width, height);
			
			// We want to take advantage of the fact that SVG images scale and convert the SVG to bitmap again
			if (isSVG()) {
				modifiedImage = null;
				update();
			}
		}
	}

	// Shape annotation methods. We add these so we can get resizeImage functionality

	// At this point, we only support RECTANGLE. At some point, it would be really
	// useful to clip the image to the shape
	@Override
	public List<String> getSupportedShapes() {
		return Collections.singletonList(ShapeType.RECTANGLE.shapeName());
	}

	@Override
	public String getShapeType() {
		return ShapeType.RECTANGLE.shapeName();
	}

	@Override
	public void setCustomShape(Shape shape) {
		// Not supported yet...
	}

	@Override
	public void setShapeType(String type) {
		// Not supported yet...
	}

	@Override
	public Paint getFillColor() {
		return null;
	}

	@Override
	public double getFillOpacity() {
		return 0.0;
	}

	@Override
	public void setFillColor(Paint fill) {
		// Does this make sense here?
	}

	@Override
	public void setFillOpacity(double opacity) {
		// Does this make sense here?
	}

	@Override
	public Shape getShape() {
		return new Rectangle2D.Double((double) getX(), (double) getY(), width, height);
	}

	@Override
	protected String getDefaultName() {
		if (url != null) {
			try {
				var fileName = Paths.get(new URI(url.toString()).getPath()).getFileName().toString();
				return fileName;
			} catch (Exception e) {
				// Just ignore...
			}
		}

		return super.getDefaultName();
	}
	
	public boolean isSVG() {
		return cg instanceof SVGCustomGraphics || svg != null;
	}

	public void dropImage() {
		customGraphicsManager.setUsedInCurrentSession(cg, false);
	}

	private Image getModifiedImage() {
		if (modifiedImage != null)
			return modifiedImage;

		BufferedImage image = null;

		if (isSVG()) {
			var w = Math.max(1, (int) Math.round(getWidth())); // will throw exception if w <= 0
			var h = Math.max(1, (int) Math.round(getHeight())); // will throw exception if h <= 0
			var rect = new Rectangle2D.Float(w / 2.0f, h / 2.0f, w, h);
			
			image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			var g = image.createGraphics();
			
			var layers = ((SVGCustomGraphics) cg).getLayers();
			
			for (var cgl : layers) {
				// Much easier to use the SVGLayer draw method than have calculate and apply
				// the same scale factor and translation transform already done by the layer!
				if (cgl instanceof SVGLayer)
					((SVGLayer) cgl).draw(g, rect, rect);
			}
			
			g.dispose();
		} else {
			image = this.image;
		}
		
		if (image != null) {
			if (brightness == 0 && contrast == 0) {
				modifiedImage = image;
			} else {
				var rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
				var g = rgbImage.createGraphics();
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
				modifiedImage = rgbImage;
				g.dispose();

				float offset = (float) brightness * 255.0f / 100.0f;
				float scaleFactor = 1.0f;

				// scaleFactor goes from 0 - 4.0 with a
				if (contrast <= 0)
					scaleFactor = 1.0f + ((float) contrast) / 100.0f;
				else
					scaleFactor = 1.0f + ((float) contrast) * 3.0f / 100.0f;

				var op = new RescaleOp(scaleFactor, offset, null);
				op.filter(modifiedImage, modifiedImage);
			}
		}
		
		return modifiedImage;
	}
	
	@Override
	public void paint(Graphics g, boolean showSelection) {
		var g2 = (Graphics2D) g.create();
		g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
		
		if (isSVG()) {
			// SVG...
			var w = Math.max(1.0, getWidth()); // will throw exception if w <= 0
			var h = Math.max(1.0, getHeight()); // will throw exception if h <= 0
			var rect = new Rectangle2D.Double(getX() + w / 2.0f, getY() + h / 2.0f, w, h);
			
			var layers = ((SVGCustomGraphics) cg).getLayers();
			
			for (var cgl : layers) {
				if (cgl instanceof SVGLayer)
					((SVGLayer) cgl).draw(g2, rect, rect);
			}
		} else {
			// Bitmap...
			var image = getModifiedImage();
	
			if (image != null) {
				g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
				g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
				var currentTransform = g2.getTransform();
				
				if (rotation != 0)
					g2.rotate(Math.toRadians(rotation), (int) (getX() + getWidth()/2), (int) (getY() + getHeight()/2));
				
				g2.drawImage(
						image, 
						Math.round((float) getX()), 
						Math.round((float) getY()), 
						Math.round((float) getWidth()),
						Math.round((float) getHeight()), 
						null
				);
				g2.setTransform(currentTransform);
			}
		}
		
		g2.dispose();
		
		super.paint(g, showSelection); // draw the border over top
	}
}

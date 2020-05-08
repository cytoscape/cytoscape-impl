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
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JDialog;

import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.image.URLBitmapCustomGraphics;
import org.cytoscape.ding.impl.DRenderingEngine;
import org.cytoscape.ding.impl.cyannotator.dialogs.ImageAnnotationDialog;
import org.cytoscape.ding.impl.cyannotator.utils.ViewUtils;
import org.cytoscape.ding.internal.util.ImageUtil;
import org.cytoscape.ding.internal.util.ViewUtil;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.ImageAnnotation;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

public class ImageAnnotationImpl extends ShapeAnnotationImpl implements ImageAnnotation {
	
	private BufferedImage image;
	private BufferedImage modifiedImage;
	private	URL url;

	private float opacity = 1.0f;
	private int brightness;
	private int contrast;
	private CyCustomGraphics<?> cg;
	protected CustomGraphicsManager customGraphicsManager;

	private static final Logger logger = LoggerFactory.getLogger(ImageAnnotationImpl.class);
	
	// XXX HACK to force the custom graphics manager to respect these graphics
	public void preserveCustomGraphics() {
		for (CyCustomGraphics<?> cg : customGraphicsManager.getAllCustomGraphics())
			customGraphicsManager.setUsedInCurrentSession(cg, true);
	}

	public ImageAnnotationImpl(DRenderingEngine re, boolean usedForPreviews) {
		super(re, 0, 0, usedForPreviews);
	}

	public ImageAnnotationImpl(ImageAnnotationImpl c, boolean usedForPreviews) { 
		super((ShapeAnnotationImpl) c, 0, 0, usedForPreviews);
		this.image = c.image;
		this.customGraphicsManager = c.customGraphicsManager;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.url = c.url;
		this.opacity = c.opacity;
		this.brightness = c.brightness;
		this.contrast = c.contrast;
		setBorderWidth(0.0); // Our default border width is 0
		name = c.getName() != null ? c.getName() : getDefaultName();
	}

	public ImageAnnotationImpl(
			DRenderingEngine re,
			double x,
			double y,
			URL url,
			BufferedImage image,
			double zoom,
			CustomGraphicsManager customGraphicsManager
	) {
		super(re, x, y, ShapeType.RECTANGLE, 0, 0, null, null, 0.0f);

		this.image = image;
		this.customGraphicsManager = customGraphicsManager;
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.url = url;
		final Long id = customGraphicsManager.getNextAvailableID();
		this.cg = new URLBitmapCustomGraphics(id, url.toString(), image);
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
		this.customGraphicsManager = customGraphicsManager;

		opacity = ViewUtils.getFloat(argMap, OPACITY, 1.0f);
		brightness = ViewUtils.getInteger(argMap, LIGHTNESS, 0);
		contrast = ViewUtils.getInteger(argMap, CONTRAST, 0);

		this.image = null;

		if (argMap.containsKey(URL)) {
			// Get the image from the image pool
			try {
				this.url = new URL(argMap.get(URL));
				this.cg = customGraphicsManager.getCustomGraphicsBySourceURL(this.url);
				
				if (cg != null) {
					this.image = ImageUtil.toBufferedImage(cg.getRenderedImage());
					customGraphicsManager.addCustomGraphics(cg, this.url);
					customGraphicsManager.setUsedInCurrentSession(cg, true);
				}
				name = getDefaultName();
			} catch (Exception e) {
				logger.warn("Unable to restore image '" + argMap.get(URL) + "'", e);
			}
		}
	}

	@Override
	public Class<? extends Annotation> getType() {
		return ImageAnnotation.class;
	}
	
	@Override
	public Map<String, String> getArgMap() {
		Map<String, String> argMap = super.getArgMap();
		argMap.put(TYPE, ImageAnnotation.class.getName());
		if (url != null)
			argMap.put(URL, url.toString());
		argMap.put(ImageAnnotation.WIDTH, Double.toString(width));
		argMap.put(ImageAnnotation.HEIGHT, Double.toString(height));
		argMap.put(OPACITY, Float.toString(opacity));
		argMap.put(LIGHTNESS, Integer.toString(brightness));
		argMap.put(CONTRAST, Integer.toString(contrast));
		customGraphicsManager.setUsedInCurrentSession(cg, true);

		return argMap;
	}

	public void reloadImage() {
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
	}

	@Override
	public Image getImage() {
		return image;
	}

	@Override
	public void setImage(Image image) {
		if (image instanceof BufferedImage)
			this.image = (BufferedImage)image;
		else if (image instanceof VolatileImage)
			this.image = ((VolatileImage)image).getSnapshot();
		else
			return;
		
		update();
	}

	@Override
	public void setImage(URL url) {
		this.url = url;
		reloadImage();
		update();
	}

	@Override
	public URL getImageURL() {
		return url;
	}

	@Override
	public void setImageOpacity(float opacity) {
		this.opacity = opacity;
		update();
	}

	@Override
	public float getImageOpacity() {
		return this.opacity;
	}

	@Override
	public void setImageBrightness(int brightness) {
		if(this.brightness != brightness) {
			this.brightness = brightness;
			this.modifiedImage = null;
			update();
		}
	}

	@Override
	public int getImageBrightness() {
		return this.brightness;
	}

	@Override
	public void setImageContrast(int contrast) {
		if(this.contrast != contrast) {
			this.contrast = contrast;
			this.modifiedImage = null;
			update();
		}
	}

	@Override
	public int getImageContrast() {
		return this.contrast;
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
	}

	@Override
	public void setShapeType(String type) {
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
	}

	@Override
	public void setFillOpacity(double opacity) {
	}

	@Override
	public Shape getShape() {
		return new Rectangle2D.Double((double) getX(), (double) getY(), width, height);
	}
	
	@Override
	protected String getDefaultName() {
		if (url != null) {
			try {
				String fileName = Paths.get(new URI(url.toString()).getPath()).getFileName().toString();
				return fileName;
			} catch (Exception e) {
				// Just ignore...
			}
		}
		
		return super.getDefaultName();
	}
	
	public void dropImage() {
		customGraphicsManager.setUsedInCurrentSession(cg, false);
	}

	@Override
	public JDialog getModifyDialog() {
		return new ImageAnnotationDialog(this, ViewUtil.getActiveWindow(re));
	}


	private Image getModifiedImage() {
		if(image == null)
			return null;
		
		if(modifiedImage == null) {
			if(brightness == 0 && contrast == 0) {
				modifiedImage = image;
			} else {
				BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
				Graphics2D g = rgbImage.createGraphics();
				g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
				modifiedImage = rgbImage;
				g.dispose();

				float offset = (float)brightness*255.0f/100.0f;
				float scaleFactor = 1.0f;
				// scaleFactor goes from 0 - 4.0 with a 
				if(contrast <= 0)
					scaleFactor = 1.0f + ((float)contrast)/100.0f;
				else
					scaleFactor = 1.0f + ((float)contrast)*3.0f/100.0f;
				
				RescaleOp op = new RescaleOp(scaleFactor, offset, null);
				op.filter(modifiedImage, modifiedImage);
			}
		}
		return modifiedImage;
	}
	
	
	@Override
	public void paint(Graphics graphics, boolean showSelection) {
		Image image = getModifiedImage();
		if(image != null) {
			Graphics2D g = (Graphics2D)graphics.create();
	
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
			g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			g.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
			g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
	
			g.drawImage(image, 
					Math.round((float)getX()), 
					Math.round((float)getY()), 
					Math.round((float)getWidth()),
					Math.round((float)getHeight()), 
					null);
			
			g.dispose();
		}
		super.paint(graphics, showSelection); // draw the border over top
	}

}

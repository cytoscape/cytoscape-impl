package org.cytoscape.io.internal.write.graphics;

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

public class BitmapWriter extends AbstractTask implements CyWriter {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);
	
	private static final double MAX_ZOOM = 500;
	
	private static final int DEFAULT_RESOLUTION = 72;
	private static final List<Integer> RESOLUTION_VALUES = Arrays.asList(DEFAULT_RESOLUTION, 100, 150, 300, 600);

	private static final String PIXELS = "pixels";
	private static final String INCHES = "inches";
	
	protected Map<String,String> renderProps = new HashMap<>();
	
	@Tunable(
			description = "Show All Graphics Details:",
			longDescription = "If true the exported image detail will be high. "
					+ "If false then the image detail may be decreased so that the image export is faster.",
			exampleStringValue = "true",
			groups = { "_Others" },
			gravity = 2.0
	)
	public boolean allGraphicsDetails = true;
	
	
	@Tunable(
			description = "Hide Labels:",
			longDescription = "If true then node and edge labels will not be visible in the image.",
			exampleStringValue = "true",
			groups = { "_Others" },
			gravity = 2.2
	)
	public boolean hideLabels;
	
	
	@ProvidesTitle
	public String getTitle() {
		return "Image Parameters";
	}

	// ----------------------------
	public BoundedDouble zoom;
	
	@Tunable(
			description = "Zoom (%):",
			longDescription =
					"The zoom value to proportionally scale the image. The default value is ```100.0```. "
					+ "Valid only for bitmap formats, such as PNG and JPEG.",
			exampleStringValue = "200.0",
			groups = { "Image Size" },
			params = "alignments=vertical;slider=true",
			listenForChange = { "WidthInPixels", "HeightInPixels", "WidthInInches", "HeightInInches" },
			format = "###.##'%'",
			gravity = 1.0
	)
	public BoundedDouble getZoom() {
		return zoom;
	}
	
	public void setZoom(BoundedDouble zf){
		zoom = zf;
		// update height
		heightInPixels = (int) ((zoom.getValue() / 100) * initialHPixel);
		// update width
		widthInPixels = (int) ((zoom.getValue() / 100) * initialWPixel);
		// update inch measures
		final double dpi = resolution.getSelectedValue().doubleValue();
		widthInInches  = Double.valueOf(widthInPixels  / dpi);
		heightInInches = Double.valueOf(heightInPixels / dpi);
	}

	// ----------------------------
	public ListSingleSelection<String> units = new ListSingleSelection<>(PIXELS, INCHES);

	@Tunable(
			description = "Units:",
			longDescription =
					"The units for the 'width' and 'height' values. "
					+ "Valid only for bitmap formats, such as PNG and JPEG. "
					+ "The possible values are: ```" + PIXELS + "``` (default), ```" + INCHES + "```.",
			exampleStringValue = INCHES,
			groups = { "Image Size" },
			gravity = 1.05
	)
	public ListSingleSelection<String> getUnits() {
		return units;
	}

	public void setUnits(ListSingleSelection<String> units) {
		this.units = units;
	}
	
	// ----------------------------
	public int widthInPixels;
	public double widthInInches;

	@Tunable(
			description = "Width:",
			longDescription = "The width of the exported image. Valid only for bitmap formats, such as PNG and JPEG.",
			exampleStringValue = "800.0",
			groups = { "Image Size" },
			params = "alignments=vertical",
			listenForChange = { "Zoom", "Units", "Height", "Resolution" },
			gravity = 1.1
	)
	public Double getWidth() {
		if (PIXELS.equals(units.getSelectedValue()))
			return Double.valueOf(widthInPixels);
		else
			return widthInInches;
	}

	public void setWidth(Double width) {
		if (PIXELS.equals(units.getSelectedValue())) {
			// update zoom
			zoom.setValue((((double) width.intValue()) / initialWPixel) * 100.0);

			widthInPixels = width.intValue();
			heightInPixels = (int) ((zoom.getValue() / 100) * initialHPixel);

			final double dpi = resolution.getSelectedValue().doubleValue();
			widthInInches = widthInPixels / dpi;
			heightInInches = heightInPixels / dpi;
		} else {
			// update zoom
			final double dpi = resolution.getSelectedValue().doubleValue();
			zoom.setValue((((double) width * dpi) / initialWPixel) * 100.0);

			widthInInches = width;
			widthInPixels = (int) (widthInInches * dpi);

			heightInPixels = (int) ((zoom.getValue() / 100) * initialHPixel);
			heightInInches = heightInPixels / dpi;
		}
	}
	
	// ----------------------------
	public int heightInPixels;
	public double heightInInches;

	@Tunable(
			description = "Height:",
			longDescription = "The height of the exported image. Valid only for bitmap formats, such as PNG and JPEG.",
			exampleStringValue = "600.0",
			groups = { "Image Size" },
			params = "alignments=vertical",
			listenForChange = { "Zoom", "Units", "Width", "Resolution" },
			gravity = 1.2
	)
	public Double getHeight() {
		if (PIXELS.equals(units.getSelectedValue()))
			return Double.valueOf(heightInPixels);
		else
			return heightInInches;
	}

	public void setHeight(Double height) {
		if (PIXELS.equals(units.getSelectedValue())) {
			// update zoom
			zoom.setValue((((double) height.intValue()) / initialHPixel) * 100.0);

			widthInPixels = (int) ((zoom.getValue() / 100) * initialWPixel);
			heightInPixels = height.intValue();

			final double dpi = resolution.getSelectedValue().doubleValue();
			widthInInches = widthInPixels / dpi;
			heightInInches = heightInPixels / dpi;
		} else {
			final double dpi = resolution.getSelectedValue().doubleValue();
			zoom.setValue((((double) height * dpi) / initialHPixel) * 100.0);

			heightInPixels = (int) (height * dpi);
			heightInInches = height;

			widthInPixels = (int) ((zoom.getValue() / 100) * initialWPixel);
			widthInInches = widthInPixels / dpi;
		}
	}
	
	// ----------------------------
	public ListSingleSelection<Integer> resolution;

	@Tunable(
			description = "Resolution (DPI):",
			longDescription =
					"The resolution of the exported image, in DPI. "
					+ "Valid only for bitmap formats, when the selected width and height 'units' is ```" + INCHES + "```. "
					+ "The possible values are: ```72``` (default), ```100```, ```150```, ```300```, ```600```.",
			exampleStringValue = "" + DEFAULT_RESOLUTION,
			groups = { "Image Size" },
			params = "alignments=vertical",
			dependsOn = "Units=inches",
			gravity = 1.5
	)
	public ListSingleSelection<Integer> getResolution() {
		return resolution;
	}

	public void setResolution(ListSingleSelection<Integer> rescb) {
		final double dpi = resolution.getSelectedValue().doubleValue();
		widthInInches = widthInPixels / dpi;
		heightInInches = heightInPixels / dpi;
	}
	
	// ----------------------------
	private final OutputStream outStream;
	protected final RenderingEngine<?> re;
	private String extension;
	private int initialWPixel, initialHPixel; 

	public BitmapWriter(RenderingEngine<?> re, OutputStream outStream, Set<String> extensions) {
		this.re = re;
		this.outStream = outStream;
		setExtension(extensions);
		
		zoom = new BoundedDouble(0.0, 100.0, MAX_ZOOM, false, false);
		initialWPixel = (re.getViewModel().getVisualProperty(NETWORK_WIDTH).intValue());
		initialHPixel = (re.getViewModel().getVisualProperty(NETWORK_HEIGHT).intValue());

		widthInPixels = initialWPixel;
		heightInPixels = initialHPixel;
		resolution = new ListSingleSelection<>(RESOLUTION_VALUES);
		resolution.setSelectedValue(DEFAULT_RESOLUTION);
		double dpi = DEFAULT_RESOLUTION;
		
		widthInInches =  initialWPixel / dpi;
		heightInInches = initialHPixel / dpi;
	}
	
	private void setExtension(Set<String> extensions) {
		for (String format : ImageIO.getWriterFormatNames()) {
			for (String ext : extensions) {
				if (format.equals(ext)) {
					extension = format;
					return;
				}
			}
		}
		throw new IllegalArgumentException("Image format (" + extensions.toString() + ") NOT supported by ImageIO");
	}
	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Bitmap Image Writer");
		tm.setStatusMessage("Creating image...");
		tm.setProgress(0.0);
		logger.debug("Bitmap image rendering start.");

		renderProps.put("exportHideLabels", String.valueOf(hideLabels));
		renderProps.put("highDetail", String.valueOf(allGraphicsDetails));
		writeImage(tm);
		
		logger.debug("Bitmap image rendering finished.");
		tm.setProgress(1.0);
	}
	
	private void writeImage(TaskMonitor tm) throws Exception {
		final BufferedImage image = new BufferedImage(widthInPixels, heightInPixels, getImageType());
		
		final Graphics2D g = (Graphics2D) image.getGraphics();
		final double scale = zoom.getValue() / 100.0;
		g.scale(scale, scale);
		
		tm.setProgress(0.2);
		re.printCanvas(g, renderProps);
		
		tm.setProgress(0.4);
		g.dispose();

		try {
			tm.setStatusMessage("Writing " + extension + "...");
			ImageIO.write(image, extension, outStream);
		} finally {
			outStream.close();
		}
	}
	
	
	protected int getImageType() {
		return BufferedImage.TYPE_INT_RGB;
	}
}

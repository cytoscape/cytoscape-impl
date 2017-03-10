package org.cytoscape.io.internal.write.graphics;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
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

import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.BasicVisualLexicon.NETWORK_WIDTH;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;

import javax.imageio.ImageIO;

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


/**
 */
public class BitmapWriter extends AbstractTask implements CyWriter {

	private static final Logger logger = LoggerFactory.getLogger(BitmapWriter.class);
	
	private static final double MAX_ZOOM= 500;

	//****
	@ProvidesTitle
	public String getTitle() { return "Image Parameters"; }

	//****
	public BoundedDouble zoom;
	@Tunable(description = "Zoom (%):",groups={"Image Size"},params="alignments=vertical;slider=true",listenForChange={"WidthInPixels","HeightInPixels", "WidthInInches", "HeightInInches"}, format="###.##'%'", gravity = 1.0)
	public BoundedDouble getZoom(){
		return zoom;
	}
	public void setZoom(BoundedDouble zf){
		zoom = zf;
		
		//update height
		heightInPixels = (int) ((zoom.getValue()/100) * initialHPixel);
		//update width
		widthInPixels = (int) ((zoom.getValue()/100) * initialWPixel);
		//update inch measures
		final double dpi = resolution.getSelectedValue().doubleValue();
		widthInInches = new Double( widthInPixels/dpi);
		heightInInches = new Double( heightInPixels/dpi);
	}

	//****
	public ListSingleSelection<String> units = new ListSingleSelection<String>("pixels","inches");
	@Tunable(description="Units", groups={"Image Size"}, gravity = 1.05)
	public ListSingleSelection<String> getUnits() {
		return units;
	}
	public void setUnits(ListSingleSelection<String> units) {
		this.units = units;
	}
	
	public int widthInPixels;
	public double widthInInches;
	@Tunable(description = "Width:",groups={"Image Size"},params="alignments=vertical",listenForChange={"Zoom","Units","Height","Resolution"}, gravity = 1.1)
	public Double getWidth(){
		if(units.getSelectedValue().equals("pixels"))
			return new Double(widthInPixels);
		else
			return widthInInches;
	}
	public void setWidth(Double width){
		if(units.getSelectedValue().equals("pixels")) {
			// update zoom
			zoom.setValue(( ((double)width.intValue()) / initialWPixel) * 100.0);
			
			widthInPixels = width.intValue();
			heightInPixels = (int) ((zoom.getValue() / 100) * initialHPixel);
			
			final double dpi = resolution.getSelectedValue().doubleValue();
			widthInInches =  widthInPixels/dpi;
			heightInInches = heightInPixels/dpi;
		}
		else {
			// update zoom
			final double dpi = resolution.getSelectedValue().doubleValue();
			zoom.setValue(( ((double)width * dpi) / initialWPixel) * 100.0);

			widthInInches = width;
			widthInPixels = (int) (widthInInches * dpi);
			
			heightInPixels = (int) ((zoom.getValue()/100) * initialHPixel);
			heightInInches = heightInPixels/dpi;
		}
	}
	
	//****
	public int heightInPixels;
	public double heightInInches;
	@Tunable(description = "Height:",groups={"Image Size"},params="alignments=vertical",listenForChange={"Zoom","Units", "Width", "Resolution"}, gravity = 1.2)
	public Double getHeight(){
		if(units.getSelectedValue().equals("pixels"))
			return new Double(heightInPixels);
		else
			return heightInInches;
	}
	public void setHeight(Double height){
		if(units.getSelectedValue().equals("pixels")) {
			//update zoom
			zoom.setValue (( ((double)height.intValue()) / initialHPixel) * 100.0);
			
			widthInPixels = (int) ((zoom.getValue()/100) * initialWPixel);
			heightInPixels = height.intValue();	
			
			final double dpi = resolution.getSelectedValue().doubleValue();
			widthInInches =  widthInPixels/dpi;
			heightInInches = heightInPixels/dpi;
		}
		else {
			final double dpi = resolution.getSelectedValue().doubleValue();
			zoom.setValue(( ((double)height * dpi) / initialHPixel) * 100.0);
			
			heightInPixels = (int) (height * dpi);
			heightInInches = height;
			
			widthInPixels = (int) ((zoom.getValue()/100) * initialWPixel);
			widthInInches = widthInPixels/dpi;
		}
	}
	
	//****
	public ListSingleSelection<Integer> resolution;
	@Tunable(description = "Resolution (DPI):",groups={"Image Size"},params="alignments=vertical", dependsOn="Units=inches", gravity = 1.5)
	public ListSingleSelection<Integer> getResolution(){
		return resolution;
	}
	public void setResolution(ListSingleSelection<Integer> rescb){
		final double dpi = resolution.getSelectedValue().doubleValue();
		widthInInches =  widthInPixels/dpi;
		heightInInches = heightInPixels/dpi;
	}
	

	private final OutputStream outStream;
	private RenderingEngine<?> re;
	private String extension = null;
	private int initialWPixel, initialHPixel; 

	public BitmapWriter( RenderingEngine<?> re, OutputStream outStream,
			Set<String> extensions) {
		
		
		this.re = re;
		this.outStream = outStream;
		setExtension(extensions);
		
		zoom = new BoundedDouble(0.0, 100.0, MAX_ZOOM,false, false);
		
		initialWPixel =  (re.getViewModel()
				.getVisualProperty(NETWORK_WIDTH).intValue());
		
		initialHPixel = (re.getViewModel()
				.getVisualProperty(NETWORK_HEIGHT).intValue());
		
		

		widthInPixels = initialWPixel;
		heightInPixels = initialHPixel;
		ArrayList<Integer> values = new ArrayList<Integer>();
		values.add(72);
		values.add(100);
		values.add(150);
		values.add(300);
		values.add(600);
		resolution = new ListSingleSelection<Integer>(values);
		resolution.setSelectedValue(72);
		double dpi = 72.0 ;
		
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
		throw new IllegalArgumentException("Image format ("
				+ extensions.toString() + ") NOT supported by ImageIO");
	}

	
	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setProgress(0.0);
		logger.debug("Bitmap image rendering start.");

		final double scale = zoom.getValue() / 100.0; 
		tm.setProgress(0.1);		
		final BufferedImage image = new BufferedImage(widthInPixels, heightInPixels, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = (Graphics2D) image.getGraphics();
		g.scale(scale, scale);
		tm.setProgress(0.2);
		re.printCanvas(g);
		tm.setProgress(0.4);
		g.dispose();
		
		try {
			ImageIO.write(image, extension, outStream);			
		} finally {
			outStream.close();
		}
		
		logger.debug("Bitmap image rendering finished.");
		tm.setProgress(1.0);
	}	


}


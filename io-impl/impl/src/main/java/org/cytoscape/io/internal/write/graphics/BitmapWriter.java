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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.security.PublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.BoundedRangeModel;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;

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
import org.cytoscape.work.AbstractTunableHandler;


/**
 */
public class BitmapWriter extends AbstractTask implements CyWriter {

	private static final Logger logger = LoggerFactory.getLogger(BitmapWriter.class);
	
	private static final double MAX_ZOOM= 500;

	//****
	@ProvidesTitle
	public String getTitle() { return "Image parameters"; }

	//****
	public BoundedDouble zoom;
	@Tunable(description = "Zoom (%):",groups={"Image Size"},params="alignments=vertical;slider=true",listenForChange={"WidthInPixels","HeightInPixels", "WidthInInches", "HeightInInches"}, format="###'%'")
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
	
	public int widthInPixels;
	@Tunable(description = "Width (px):",groups={"Image Size"},params="alignments=vertical",listenForChange={"Zoom","HeightInPixels", "WidthInInches", "HeightInInches"})
	public int getWidthInPixels(){
		return widthInPixels;
	}
	public void setWidthInPixels(int wpf){
		widthInPixels = wpf;
		// udate zoom
		zoom.setValue(( ((double)widthInPixels) / initialWPixel) * 100.0);
		//update height
		heightInPixels = (int) ((zoom.getValue() / 100) * initialHPixel);
		
		final double dpi = resolution.getSelectedValue().doubleValue();
		widthInInches =  widthInPixels/dpi;
		heightInInches = heightInPixels/dpi;
	}
	
	//****
	public int heightInPixels;
	@Tunable(description = "Height (px):",groups={"Image Size"},params="alignments=vertical",listenForChange={"Zoom","WidthInPixels", "WidthInInches", "HeightInInches"})
	public int getHeightInPixels(){
		return heightInPixels;
	}
	public void setHeightInPixels(int hpf){
		heightInPixels = hpf;	
		// udate zoom
		zoom.setValue (( ((double)heightInPixels) / initialHPixel) * 100.0);
		//update width
		widthInPixels = (int) ((zoom.getValue()/100) * initialWPixel);
		
		final double dpi = resolution.getSelectedValue().doubleValue();
		widthInInches =  widthInPixels/dpi;
		heightInInches = heightInPixels/dpi;
	}
	
	//****
	public double widthInInches;
	@Tunable(description = "Width (inches):",groups={"Image Size"},params="alignments=vertical",listenForChange={"Resolution", "Zoom", "HeightInPixels", "WidthInPixels" , "HeightInInches"})
	public double getWidthInInches(){
		return widthInInches;
	}
	public void setWidthInInches(double wif){
		widthInInches =  wif;
		
		final double dpi = resolution.getSelectedValue().doubleValue();
		widthInPixels = (int) (widthInInches * dpi);
		
		zoom.setValue(( ((double)widthInPixels) / initialWPixel) * 100.0);
		heightInPixels = (int) ((zoom.getValue()/100) * initialHPixel);
		
		heightInInches = heightInPixels/dpi;

	}
	
	//****
	public double heightInInches;
	@Tunable(description = "Height (inches):",groups={"Image Size"},params="alignments=vertical",listenForChange={"Resolution", "Zoom", "HeightInPixels", "WidthInPixels", "WidthInInches"})
	public double getHeightInInches(){
		return heightInInches;
	}
	public void  setHeightInInches(double hif){
		heightInInches = hif;
		
		final double dpi = resolution.getSelectedValue().doubleValue();
		heightInPixels = (int) (heightInInches * dpi);
		
		zoom.setValue(( ((double)heightInPixels) / initialHPixel) * 100.0);
		widthInPixels = (int) ((zoom.getValue()/100) * initialWPixel);
		
		widthInInches = widthInPixels/dpi;
	}
	
	//****
	public ListSingleSelection<Integer> resolution;
	@Tunable(description = "Resolution (DPI):",groups={"Image Size"},params="alignments=vertical")
	public ListSingleSelection<Integer> getResolution(){
		return resolution;
	}
	public void setResolution(ListSingleSelection<Integer> rescb){
		final double dpi = resolution.getSelectedValue().doubleValue();
		widthInInches =  widthInPixels/dpi;
		heightInInches = heightInPixels/dpi;
	}
	

	private final OutputStream outStream;
	private  RenderingEngine<?> re;
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


package org.cytoscape.io.internal.write.graphics;

import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_HEIGHT;
import static org.cytoscape.view.presentation.property.MinimalVisualLexicon.NETWORK_WIDTH;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesGUI;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.BoundedDouble;
import org.cytoscape.work.util.BoundedInteger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.io.internal.ui.ExportBitmapOptionsPanel;

/**
 */
public class BitmapWriter extends AbstractTask implements CyWriter {

	private static final Logger logger = LoggerFactory.getLogger(BitmapWriter.class);
	
	private static final int MAX_SIZE = 50000;

	private ExportBitmapOptionsPanel exportBitmapOptionsPanel = null;

	//@Tunable(description = "Image scale")
	//public BoundedDouble scaleFactor;
	
	//@Tunable(description = "Original Width (px)")
	//public BoundedInteger width;
	
	//@Tunable(description = "Original Height (px)")
	//public BoundedInteger height;

	private final OutputStream outStream;
	private final RenderingEngine<?> re;
	private String extension = null;
	private int w, h; 

	public BitmapWriter(final RenderingEngine<?> re, OutputStream outStream,
			Set<String> extensions) {
		this.re = re;
		this.outStream = outStream;
		setExtension(extensions);
		
		w = (int) (re.getViewModel()
				.getVisualProperty(NETWORK_WIDTH).doubleValue());
		h = (int) (re.getViewModel()
				.getVisualProperty(NETWORK_HEIGHT).doubleValue());
	}
	
	
	@ProvidesGUI
	public JPanel getGUI() {		
		if (exportBitmapOptionsPanel == null) {
			try {
				this.exportBitmapOptionsPanel = 
					new ExportBitmapOptionsPanel(w, h);
			} catch (Exception e) {
				throw new IllegalStateException("Could not initialize BitmapWriterPanel.", e);
			}
		}

		return exportBitmapOptionsPanel;
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
				
		// Extract size
		//final double scale = scaleFactor.getValue().doubleValue();
		//final int finalW = ((Number)(width.getValue()*scale)).intValue();
		//final int finalH = ((Number)(height.getValue()*scale)).intValue();

		final double scale =exportBitmapOptionsPanel.getZoom();
		final int finalW = exportBitmapOptionsPanel.getWidthPixels();
		final int finalH = exportBitmapOptionsPanel.getHeightPixels();
		tm.setProgress(0.1);		
		final BufferedImage image = new BufferedImage(finalW, finalH, BufferedImage.TYPE_INT_RGB);
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

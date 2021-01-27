package org.cytoscape.cg.internal.task;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.cytoscape.application.CyUserLog;
import org.cytoscape.cg.internal.image.SVGCustomGraphics;
import org.cytoscape.cg.internal.util.ImageUtil;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SaveUserImagesTask implements Task {

	private final File location;
	private final CustomGraphicsManager manager;

	private static final int TIMEOUT = 1000;
	private static final int NUM_THREADS = 4;

	private static final String METADATA_FILE = "image_metadata.props";
	
	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	public SaveUserImagesTask(File location, CustomGraphicsManager manager) {
		this.location = location;
		this.manager = manager;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Save User Images");
		tm.setStatusMessage("Saving image library to your local disk.\n\nPlease wait...");
		tm.setProgress(0.0);

		// Remove all existing files
		var files = location.listFiles();

		if (files != null) {
			for (File old : files)
				old.delete();
		}

		long startTime = System.currentTimeMillis();
		var exService = Executors.newFixedThreadPool(NUM_THREADS);

		for (var cg : manager.getAllPersistantCustomGraphics()) {
			if (cg instanceof SVGCustomGraphics) {
				var svg = ((SVGCustomGraphics) cg).getSVG();
				
				if (svg != null && !svg.isBlank()) {
					try {
						exService.submit(new SaveSVGImageTask(location, cg.getIdentifier().toString(), svg));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				var img = cg.getRenderedImage();
				
				if (img != null) {
					try {
						exService.submit(new SavePNGImageTask(location, cg.getIdentifier().toString(),
								ImageUtil.toBufferedImage(img)));
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

		try {
			exService.shutdown();
			exService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw e;
		}

		try {
			manager.getMetadata().store(new FileOutputStream(new File(location, METADATA_FILE)), "Image Metadata");
		} catch (IOException e) {
			throw new IOException("Could not save image metadata file.", e);
		}

		long endTime = System.currentTimeMillis();
		double sec = (endTime - startTime) / (1000.0);
		logger.info("Image saving process finished in " + sec + " sec.");
	}

	@Override
	public void cancel() {
	}

	private final class SavePNGImageTask implements Callable<String> {

		private final File imageHome;
		private String fileName;
		private final BufferedImage image;

		public SavePNGImageTask(File imageHomeDirectory, String fileName, BufferedImage image) {
			this.imageHome = imageHomeDirectory;
			this.fileName = fileName;
			this.image = image;
		}

		@Override
		public String call() throws Exception {
			logger.debug("  Saving PNG Image: " + fileName);

			if (!fileName.endsWith(".png"))
				fileName += ".png";
			
			var file = new File(imageHome, fileName);

			try {
				file.createNewFile();
				ImageIO.write(image, "PNG", file);
			} catch (IOException e) {
				logger.error("Cannot save PNG image " + file.getAbsolutePath(), e);
			}

			return file.toString();
		}
	}
	
	private final class SaveSVGImageTask implements Callable<String> {

		private final File imageHome;
		private String fileName;
		private final String svg;

		public SaveSVGImageTask(File imageHomeDirectory, String fileName, String svg) {
			this.imageHome = imageHomeDirectory;
			this.fileName = fileName;
			this.svg = svg;
		}

		@Override
		public String call() throws Exception {
			logger.debug("  Saving SVG Image: " + fileName);

			if (!fileName.endsWith(".svg"))
				fileName += ".svg";
			
			var file = new File(imageHome, fileName);

			try (var writer = new FileWriter(file);) {
				file.createNewFile();
				writer.write(svg);
			} catch (IOException e) {
				logger.error("Cannot save SVG image " + file.getAbsolutePath(), e);
			}

			return file.toString();
		}
	}
}

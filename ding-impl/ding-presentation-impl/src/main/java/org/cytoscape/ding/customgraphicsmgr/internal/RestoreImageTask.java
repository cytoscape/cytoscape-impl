package org.cytoscape.ding.customgraphicsmgr.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.cytoscape.ding.customgraphics.AbstractDCustomGraphics;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.Taggable;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.ding.customgraphics.vector.GradientOvalLayer;
import org.cytoscape.ding.customgraphics.vector.GradientRoundRectangleLayer;
import org.cytoscape.ding.customgraphicsmgr.internal.event.CustomGraphicsLibraryUpdatedEvent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

public class RestoreImageTask implements Task {

	private static final Logger logger = LoggerFactory.getLogger(RestoreImageTask.class);
	
	private final CustomGraphicsManager manager;

	private final ExecutorService imageLoaderService;

	private static final int TIMEOUT = 1000;
	private static final int NUM_THREADS = 8;

	private static final String METADATA_FILE = "image_metadata.props";

	private File imageHomeDirectory;
	private final Set<URL> defaultImageURLs;

	private final CyServiceRegistrar serviceRegistrar;
	
	// For image I/O, PNG is used as bitmap image format.
	private static final String IMAGE_EXT = "png";

	// Default vectors
	private static final Set<Class<?>> DEF_VECTORS = new HashSet<>();
	private static final Set<String> DEF_VECTORS_NAMES = new HashSet<>();

	static {
		DEF_VECTORS.add(GradientRoundRectangleLayer.class);
		DEF_VECTORS.add(GradientOvalLayer.class);
		
		for (Class<?> cls : DEF_VECTORS)
			DEF_VECTORS_NAMES.add(cls.getName());
	}
	
	RestoreImageTask(final Set<URL> defaultImageURLs, final File imageLocaiton, 
	                 final CustomGraphicsManager manager, final CyServiceRegistrar serviceRegistrar) {
		this.manager = manager;
		this.serviceRegistrar = serviceRegistrar;

		// For loading images in parallel.
		this.imageLoaderService = Executors.newFixedThreadPool(NUM_THREADS);
		this.imageHomeDirectory = imageLocaiton;
		this.defaultImageURLs = defaultImageURLs;
	}


	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {		
		taskMonitor.setStatusMessage("Loading image library from local disk.");
		taskMonitor.setProgress(0.0);

		final long startTime = System.currentTimeMillis();

		restoreImages();
		restoreSampleImages();

		long endTime = System.currentTimeMillis();
		double sec = (endTime - startTime) / (1000.0);
		logger.info("Image saving process finished in " + sec + " sec.");
		
		serviceRegistrar.getService(CyEventHelper.class).fireEvent(new CustomGraphicsLibraryUpdatedEvent(manager));
	}
	
	private void restoreSampleImages() throws IOException {
		// Filter by display name
		final Collection<CyCustomGraphics> allGraphics = manager.getAllCustomGraphics();
		final Set<String> names = new HashSet<>();

		for (CyCustomGraphics<?> cg : allGraphics)
			names.add(cg.getDisplayName());
		
		for (final URL imageURL : defaultImageURLs) {
			final String[] parts = imageURL.getFile().split("/");
			final String dispNameString = parts[parts.length-1];
			
			if (this.manager.getCustomGraphicsBySourceURL(imageURL) == null && !names.contains(dispNameString)) {
				final CyCustomGraphics<?> cg = new URLImageCustomGraphics<>(manager.getNextAvailableID(),
						imageURL.toString());
				
				if (cg != null) {
					manager.addCustomGraphics(cg, imageURL);
					cg.setDisplayName(dispNameString);
				}
			}
		}
	}

	private void restoreImages() {
		final CompletionService<BufferedImage> cs = new ExecutorCompletionService<BufferedImage>(imageLoaderService);

		imageHomeDirectory.mkdir();

		long startTime = System.currentTimeMillis();

		// Load metadata first.
		final Properties prop = new Properties();
		
		try {
			prop.load(new FileInputStream(new File(imageHomeDirectory, METADATA_FILE)));
			logger.info("Custom Graphics Image property file loaded from: " + imageHomeDirectory);
		} catch (Exception e) {
			logger.info("Custom Graphics Metadata was not found. (This is normal for the first time.)");
			// Restore process is not necessary.
			return;
		}

		if (this.imageHomeDirectory != null && imageHomeDirectory.isDirectory()) {
			final File[] imageFiles = imageHomeDirectory.listFiles();
			final Map<Future<BufferedImage>, String> fMap = new HashMap<>();
			final Map<Future<BufferedImage>, Long> fIdMap = new HashMap<>();
			final Map<Future<BufferedImage>, Set<String>> metatagMap = new HashMap<>();
			
			final Set<File> validFiles = new HashSet<>();
			
			try {
				for (File file : imageFiles) {
					if (file.toString().endsWith(IMAGE_EXT) == false)
						continue;

					final String fileName = file.getName();
					final String key = fileName.split("\\.")[0];
					final String value = prop.getProperty(key);
					
					// Filter unnecessary files.
					if(value == null || value.contains("URLImageCustomGraphics") == false)
						continue;
					
					final String[] imageProps = value.split(",");
					if (imageProps == null || imageProps.length < 2)
						continue;

					String name = imageProps[2];
					if (name.contains("___"))
						name = name.replace("___", ",");

					Future<BufferedImage> f = cs.submit(new LoadImageTask(file.toURI().toURL()));
					validFiles.add(file);
					fMap.put(f, name);
					fIdMap.put(f, Long.parseLong(imageProps[1]));

					String tagStr = null;
					if (imageProps.length > 3) {
						tagStr = imageProps[3];
						final Set<String> tags = new TreeSet<>();
						String[] tagParts = tagStr.split("\\" + AbstractDCustomGraphics.LIST_DELIMITER);
						for (String tag : tagParts)
							tags.add(tag.trim());

						metatagMap.put(f, tags);
					}
				}
				for (File file : validFiles) {
					if (file.toString().endsWith(IMAGE_EXT) == false)
						continue;
					
					final Future<BufferedImage> f = cs.take();
					final BufferedImage image = f.get();
					if (image == null)
						continue;

					final CyCustomGraphics<?> cg = new URLImageCustomGraphics<>(fIdMap.get(f), fMap.get(f), image);
					
					if (cg instanceof Taggable && metatagMap.get(f) != null)
						((Taggable) cg).getTags().addAll(metatagMap.get(f));

					try {
						final URL source = new URL(fMap.get(f));
						if (source != null)
							manager.addCustomGraphics(cg, source);
					} catch (MalformedURLException me) {
						manager.addCustomGraphics(cg, null);
					}
				}
			} catch (IOException ioe) {
				ioe.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}

		try {
			imageLoaderService.shutdown();
			imageLoaderService.awaitTermination(TIMEOUT, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		long endTime = System.currentTimeMillis();
		double sec = (endTime - startTime) / (1000.0);
		logger.info("Image loading process finished in " + sec + " sec.");
		logger.info("Currently,  " + (manager.getAllCustomGraphics().size() - 1) + " images are available.");
	}

	@Override
	public void cancel() {
	}

	private final class LoadImageTask implements Callable<BufferedImage> {

		private final URL imageURL;

		public LoadImageTask(final URL imageURL) {
			this.imageURL = imageURL;
		}

		@Override
		public BufferedImage call() throws Exception {
			if (imageURL == null)
				throw new IllegalStateException("URL string cannot be null.");

			return ImageIO.read(imageURL);
		}
	}
}

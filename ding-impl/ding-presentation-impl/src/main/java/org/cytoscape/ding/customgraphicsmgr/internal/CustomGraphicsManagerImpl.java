package org.cytoscape.ding.customgraphicsmgr.internal;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.ding.customgraphics.IDGenerator;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CustomGraphicsManagerImpl implements CustomGraphicsManager, CyShutdownListener,
		SessionAboutToBeSavedListener, SessionLoadedListener {

	private static final Logger logger = LoggerFactory.getLogger(CustomGraphicsManagerImpl.class);

	private static final String IMAGE_DIR_NAME = "images3";
	private static final String APP_NAME = "org.cytoscape.ding.customgraphicsmgr";

	private final Map<Long, CyCustomGraphics> graphicsMap = new ConcurrentHashMap<Long, CyCustomGraphics>();

	// URL to hash code map. For images associated with URL.
	protected final Map<URL, Long> sourceMap = new ConcurrentHashMap<URL, Long>();

	// Null Object
	private final File imageHomeDirectory;
	private final Map<CyCustomGraphics, Boolean> isUsedCustomGraphics;
	private final Map<String, CyCustomGraphicsFactory> factoryMap;
	private final Map<CyCustomGraphicsFactory, Map> factoryPropsMap;
	private final DialogTaskManager taskManager;

	private final CyEventHelper eventHelper;

	private final VisualMappingManager vmm;
	private final CyApplicationManager applicationManager;
	private static CustomGraphicsManagerImpl instance = null;

	/**
	 * Creates an image pool object and restore existing images from user
	 * resource directory.
	 */
	public CustomGraphicsManagerImpl(final CyProperty<Properties> properties, 
	                                 final DialogTaskManager taskManager, 
	                                 final CyApplicationConfiguration config, 
	                                 final CyEventHelper eventHelper, 
	                                 final VisualMappingManager vmm, 
	                                 final CyApplicationManager applicationManager, 
	                                 final Set<URL> defaultImageURLs) {

		this.taskManager = taskManager;
		this.eventHelper = eventHelper;
		this.vmm = vmm;
		this.applicationManager = applicationManager;
		this.isUsedCustomGraphics = new HashMap<CyCustomGraphics, Boolean>();
		this.factoryMap = new HashMap<String, CyCustomGraphicsFactory>();
		this.factoryPropsMap = new HashMap<CyCustomGraphicsFactory, Map>();

		if (properties == null)
			throw new NullPointerException("Property object is null.");

		final Properties props = properties.getProperties();

		if (props == null)
			throw new NullPointerException("Property is missing.");

		// Usually, this is USER_HOME/.cytoscape/images3
		this.imageHomeDirectory = new File(config.getConfigurationDirectoryLocation(), IMAGE_DIR_NAME);

		// Restore Custom Graphics from the directory.
		final RestoreImageTaskFactory taskFactory = 
		                 new RestoreImageTaskFactory(defaultImageURLs, imageHomeDirectory, this, eventHelper,
		                                             vmm, applicationManager);
		taskManager.execute(taskFactory.createTaskIterator());
		instance = this;
	}

	public static CustomGraphicsManagerImpl getInstance() { return instance; }

	public void addCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map props) {
		if (factory == null) return;
		factoryMap.put(factory.getSupportedClass().getName(), factory);
		factoryPropsMap.put(factory, props);
	}

	public void removeCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map props) {
		if (factory == null) return;
		factoryMap.remove(factory.getSupportedClass());
		factoryPropsMap.remove(factory);
	}

	public CyCustomGraphicsFactory getCustomGraphicsFactory(Class<? extends CyCustomGraphics> cls) {
		if (factoryMap.containsKey(cls.getName()))
			return factoryMap.get(cls.getName());
		return null;
	}

	public CyCustomGraphicsFactory getCustomGraphicsFactory(String className) {
		if (factoryMap.containsKey(className))
			return factoryMap.get(className);
		return null;
	}

	public Collection<CyCustomGraphicsFactory> getAllCustomGraphicsFactories() {
		return factoryMap.values();
	}

	/**
	 * Add a custom graphics to current session.
	 * 
	 * @param hash
	 *            : Hasn code of image object
	 * @param graphics
	 *            : Actual custom graphics object
	 * @param source
	 *            : Source URL of graphics (if exists. Can be null)
	 */
	@Override
	public void addCustomGraphics(final CyCustomGraphics graphics, final URL source) {
		if (graphics == null)
			throw new IllegalArgumentException("Custom Graphics and its ID should not be null.");

		if (graphics.getIdentifier() == null) {
			graphics.setIdentifier(getNextAvailableID());
		}

		// Souce URL is an optional field.
		if (source != null)
			sourceMap.put(source, graphics.getIdentifier());

		graphicsMap.put(graphics.getIdentifier(), graphics);
		this.isUsedCustomGraphics.put(graphics, false);
	}

	/**
	 * Remove graphics from current session (memory).
	 * 
	 * @param id
	 *            : ID of graphics (hash code)
	 */
	@Override
	public void removeCustomGraphics(final Long id) {
		final CyCustomGraphics cg = graphicsMap.get(id);
		if (cg != null && cg != NullCustomGraphics.getNullObject()) {
			graphicsMap.remove(id);
			this.isUsedCustomGraphics.remove(cg);
		}
	}

	/**
	 * Get a Custom Graphics by integer ID.
	 * 
	 * @param hash
	 *            Hash code of Custom Graphics object
	 * 
	 * @return Custom Graphics if exists. Otherwise, null.
	 * 
	 */
	@Override
	public CyCustomGraphics getCustomGraphicsByID(Long id) {
		return graphicsMap.get(id);
	}

	/**
	 * Get Custom Graphics by source URL. Images without source cannot be
	 * retreved by this method.
	 * 
	 * @param sourceURL
	 * @return
	 */
	@Override
	public CyCustomGraphics getCustomGraphicsBySourceURL(final URL sourceURL) {
		final Long id = sourceMap.get(sourceURL);
		if (id != null)
			return graphicsMap.get(id);
		else
			return null;
	}

	@Override
	public Collection<CyCustomGraphics> getAllCustomGraphics() {
		return getAllCustomGraphics(false);
	}

	/**
	 * Get a collection of all Custom Graphics in current session.
	 * 
	 * @return
	 */
	@Override
	public Collection<CyCustomGraphics> getAllCustomGraphics(boolean sorted) {
		if (!sorted)
			return graphicsMap.values();

		List<CyCustomGraphics> values = 
			new ArrayList<CyCustomGraphics>(graphicsMap.values());
		Collections.sort(values, new CGComparator());
		return values;
	}

	/**
	 * Get a collection of all Custom Graphics in current session that
	 * we want to serialize into the session file.
	 * 
	 * @return
	 */
	@Override
	public Collection<CyCustomGraphics> getAllPersistantCustomGraphics() {
		Set<CyCustomGraphics> cgSet = new HashSet<CyCustomGraphics>();
		for (CyCustomGraphics cg: getAllCustomGraphics()) {
			// Currently, we only export URLImageCustomGraphics to the session file.  This
			// may change in the future......
			if (cg instanceof URLImageCustomGraphics) {
				URLImageCustomGraphics urlCG = (URLImageCustomGraphics)cg;
				// Don't serialize bundle-generated graphics
				if (urlCG.getSourceURL() != null && urlCG.getSourceURL().toString().startsWith("bundle:"))
					continue;
				cgSet.add(cg);
			}
		}
		return cgSet;
	}

	/**
	 * Remove all custom graphics from memory.
	 */
	@Override
	public void removeAllCustomGraphics() {
		this.graphicsMap.clear();
		this.sourceMap.clear();
		this.isUsedCustomGraphics.clear();
	}

	/**
	 * Convert current list of custom graphics into Property object.
	 * 
	 * @return
	 */
	@Override
	public Properties getMetadata() {
		final Properties props = new Properties();
		// Use hash code as the key, and value will be a string returned by
		// toString() method.
		// This means all CyCustomGraphics implementations should have a special
		// toString method.
		for (final CyCustomGraphics graphics : graphicsMap.values()) {
			props.setProperty(graphics.getIdentifier().toString(), 
			                  graphics.getClass().getCanonicalName()+","+graphics.toSerializableString());
		}
		
		return props;
	}

	@Override
	public SortedSet<Long> getIDSet() {
		return new TreeSet<Long>(graphicsMap.keySet());
	}

	@Override
	public boolean isUsedInCurrentSession(final CyCustomGraphics graphics) {
		if (graphics == null || this.isUsedCustomGraphics.containsKey(graphics) == false)
			return false;

		return isUsedCustomGraphics.get(graphics);
	}

	@Override
	public void setUsedInCurrentSession(final CyCustomGraphics graphics, final Boolean isUsed) {
		if (isUsed == null || graphics == null)
			return;

		if (this.isUsedCustomGraphics.containsKey(graphics) == false) {
			// Just ignore.
			return;
		}

		this.isUsedCustomGraphics.put(graphics, isUsed);
	}

	@Override
	public void handleEvent(CyShutdownEvent e) {
		// Persist images
		logger.info("Start Saving images to: " + imageHomeDirectory);

		// Create Task
		final PersistImageTaskFactory factory = new PersistImageTaskFactory(imageHomeDirectory, this);

		try {

			// FIXME how this section can wait until everything is done?
			taskManager.execute(factory.createTaskIterator());
		} catch (Exception e1) {
			logger.error("Could not save images to disk.", e1);
		}

		logger.info("========== Image saving process finished =============");
	}

	/**
	 * Prepare files to be saved in the session.
	 */
	@Override
	public void handleEvent(final SessionAboutToBeSavedEvent e) {
		final SaveGraphicsToSessionTaskFactory factory = new SaveGraphicsToSessionTaskFactory(imageHomeDirectory, this,
				e);

		try {
			taskManager.execute(factory.createTaskIterator());
		} catch (Exception ex) {
			logger.error("Could not save images to .", ex);
		}
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// Add new images
		final CySession sess = e.getLoadedSession();

		if (sess != null) {
			final Map<String, List<File>> filesMap = sess.getAppFileListMap();

			if (filesMap != null) {
				final List<File> files = filesMap.get(APP_NAME);
				// TODO: 2.x compatibility

				if (files != null && files.size() != 0) {
					// get parent directory
					final File parent = files.get(0).getParentFile();
					final RestoreImageTaskFactory taskFactory = new RestoreImageTaskFactory(new HashSet<URL>(), parent, this, eventHelper, vmm, applicationManager);
					TaskIterator loadImagesIterator = taskFactory.createTaskIterator();
					for (CyNetworkView networkView: sess.getNetworkViews()) {
						loadImagesIterator.append(((DGraphView)networkView).getCyAnnotator().getReloadImagesTask());
					}
					taskManager.execute(loadImagesIterator);
				}
			}
		}
	}

	@Override
	public Long getNextAvailableID() {
		
		Long key = IDGenerator.getIDGenerator().getNextId();
		
		while(graphicsMap.get(key) != null)
			key = IDGenerator.getIDGenerator().getNextId();
		
		return key;
	}
}

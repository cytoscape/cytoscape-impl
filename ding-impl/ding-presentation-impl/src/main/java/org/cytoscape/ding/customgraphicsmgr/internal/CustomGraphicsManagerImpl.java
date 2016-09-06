package org.cytoscape.ding.customgraphicsmgr.internal;

import static org.cytoscape.ding.DVisualLexicon.NODE_CUSTOMGRAPHICS_1;
import static org.cytoscape.ding.DVisualLexicon.NODE_CUSTOMGRAPHICS_2;
import static org.cytoscape.ding.DVisualLexicon.NODE_CUSTOMGRAPHICS_3;
import static org.cytoscape.ding.DVisualLexicon.NODE_CUSTOMGRAPHICS_4;
import static org.cytoscape.ding.DVisualLexicon.NODE_CUSTOMGRAPHICS_5;
import static org.cytoscape.ding.DVisualLexicon.NODE_CUSTOMGRAPHICS_6;
import static org.cytoscape.ding.DVisualLexicon.NODE_CUSTOMGRAPHICS_7;
import static org.cytoscape.ding.DVisualLexicon.NODE_CUSTOMGRAPHICS_8;
import static org.cytoscape.ding.DVisualLexicon.NODE_CUSTOMGRAPHICS_9;

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
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.IDGenerator;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.ding.customgraphics.bitmap.MissingImageCustomGraphics;
import org.cytoscape.ding.customgraphics.bitmap.URLImageCustomGraphics;
import org.cytoscape.ding.impl.DGraphView;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.BoundaryRangeValues;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.ContinuousMappingPoint;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
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

public final class CustomGraphicsManagerImpl
		implements CustomGraphicsManager, CyShutdownListener, SessionAboutToBeSavedListener, SessionLoadedListener {

	private static final Logger logger = LoggerFactory.getLogger(CustomGraphicsManagerImpl.class);

	private static final String IMAGE_DIR_NAME = "images3";
	private static final String APP_NAME = "org.cytoscape.ding.customgraphicsmgr";

	private final Map<Long, CyCustomGraphics> graphicsMap = new ConcurrentHashMap<>(16, 0.75f, 2);

	// URL to hash code map. For images associated with URL.
	protected final Map<URL, Long> sourceMap = new ConcurrentHashMap<>(16, 0.75f, 2);
	
	private final Set<MissingImageCustomGraphics> missingImageCustomGraphicsSet = new HashSet<>();

	// Null Object
	private final File imageHomeDirectory;
	private final Map<CyCustomGraphics, Boolean> isUsedCustomGraphics;
	private final Map<String, CyCustomGraphicsFactory> factoryMap;
	private final Map<CyCustomGraphicsFactory, Map> factoryPropsMap;
	private final CyServiceRegistrar serviceRegistrar;

	private static CustomGraphicsManager instance;

	/**
	 * Creates an image pool object and restore existing images from user
	 * resource directory.
	 */
	public CustomGraphicsManagerImpl(final Set<URL> defaultImageURLs, final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
		this.isUsedCustomGraphics = new HashMap<>();
		this.factoryMap = new HashMap<>();
		this.factoryPropsMap = new HashMap<>();

		final CyApplicationConfiguration config = serviceRegistrar.getService(CyApplicationConfiguration.class);
		this.imageHomeDirectory = new File(config.getConfigurationDirectoryLocation(), IMAGE_DIR_NAME);

		// Restore Custom Graphics from the directory.
		final RestoreImageTaskFactory taskFactory = 
		                 new RestoreImageTaskFactory(defaultImageURLs, imageHomeDirectory, this, serviceRegistrar);
		serviceRegistrar.getService(DialogTaskManager.class).execute(taskFactory.createTaskIterator());
		
		instance = this;
	}

	public static CustomGraphicsManager getInstance() { return instance; }

	@Override
	public void addCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map props) {
		if (factory == null)
			return;
		
		factoryMap.put(factory.getSupportedClass().getName(), factory);
		factoryPropsMap.put(factory, props);
	}

	@Override
	public void removeCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map props) {
		if (factory == null)
			return;
		
		factoryMap.remove(factory.getSupportedClass());
		factoryPropsMap.remove(factory);
	}

	@Override
	public CyCustomGraphicsFactory getCustomGraphicsFactory(Class<? extends CyCustomGraphics> cls) {
		return factoryMap.containsKey(cls.getName()) ? factoryMap.get(cls.getName()) : null;
	}

	@Override
	public CyCustomGraphicsFactory getCustomGraphicsFactory(String className) {
		return factoryMap.containsKey(className) ? factoryMap.get(className) : null;
	}

	@Override
	public Collection<CyCustomGraphicsFactory> getAllCustomGraphicsFactories() {
		return factoryMap.values();
	}

	@Override
	public void addCustomGraphics(final CyCustomGraphics graphics, final URL source) {
		if (graphics == null)
			throw new IllegalArgumentException("Custom Graphics and its ID should not be null.");

		if (graphics.getIdentifier() == null)
			graphics.setIdentifier(getNextAvailableID());

		// Source URL is an optional field.
		if (source != null)
			sourceMap.put(source, graphics.getIdentifier());

		graphicsMap.put(graphics.getIdentifier(), graphics);
		this.isUsedCustomGraphics.put(graphics, false);
	}

	@Override
	public void removeCustomGraphics(final Long id) {
		final CyCustomGraphics cg = graphicsMap.get(id);
		
		if (cg != null && cg != NullCustomGraphics.getNullObject()) {
			graphicsMap.remove(id);
			this.isUsedCustomGraphics.remove(cg);
		}
	}

	@Override
	public CyCustomGraphics getCustomGraphicsByID(Long id) {
		return graphicsMap.get(id);
	}

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

	@Override
	public Collection<CyCustomGraphics> getAllCustomGraphics(boolean sorted) {
		if (!sorted)
			return graphicsMap.values();

		List<CyCustomGraphics> values = new ArrayList<>(graphicsMap.values());
		Collections.sort(values, new CGComparator());
		
		return values;
	}

	/* (non-Javadoc)
	 * @see org.cytoscape.ding.customgraphicsmgr.internal.CGM#getAllPersistantCustomGraphics()
	 */
	@Override
	public Collection<CyCustomGraphics> getAllPersistantCustomGraphics() {
		Set<CyCustomGraphics> cgSet = new HashSet<>();
		
		for (CyCustomGraphics cg: getAllCustomGraphics()) {
			// Currently, we only export URLImageCustomGraphics to the session file.  This may change in the future...
			if (cg instanceof URLImageCustomGraphics) {
				URLImageCustomGraphics<?> urlCG = (URLImageCustomGraphics<?>) cg;
				
				// Don't serialize bundle-generated graphics
				if (urlCG.getSourceURL() != null && urlCG.getSourceURL().toString().startsWith("bundle:"))
					continue;
				
				cgSet.add(cg);
			}
		}
		
		return cgSet;
	}

	@Override
	public void removeAllCustomGraphics() {
		this.graphicsMap.clear();
		this.sourceMap.clear();
		this.isUsedCustomGraphics.clear();
	}

	@Override
	public Properties getMetadata() {
		final Properties props = new Properties();
		// Use hash code as the key, and value will be a string returned by toString() method.
		// This means all CyCustomGraphics implementations should have a special toString method.
		for (final CyCustomGraphics graphics : graphicsMap.values()) {
			props.setProperty(graphics.getIdentifier().toString(), graphics.toSerializableString());
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
			serviceRegistrar.getService(DialogTaskManager.class).execute(factory.createTaskIterator());
		} catch (Exception e1) {
			logger.error("Could not save images to disk.", e1);
		}

		logger.info("========== Image saving process finished =============");
	}

	@Override
	public void handleEvent(final SessionAboutToBeSavedEvent e) {
		final SaveGraphicsToSessionTaskFactory factory = new SaveGraphicsToSessionTaskFactory(imageHomeDirectory, this,
				e);

		try {
			// Make sure this task is executed synchronously in the current thread!
			serviceRegistrar.getService(SynchronousTaskManager.class).execute(factory.createTaskIterator());
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
					final RestoreImageTaskFactory taskFactory =
							new RestoreImageTaskFactory(new HashSet<>(), parent, this, serviceRegistrar);
					final TaskIterator loadImagesIterator = taskFactory.createTaskIterator();
					
					for (CyNetworkView networkView: sess.getNetworkViews()) {
						loadImagesIterator.append(((DGraphView)networkView).getCyAnnotator().getReloadImagesTask());
					}
					
					loadImagesIterator.append(new ReloadMissingImagesTask(sess.getNetworkViews()));
					
					serviceRegistrar.getService(DialogTaskManager.class).execute(loadImagesIterator);
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

	@Override
	public void addMissingImageCustomGraphics(final MissingImageCustomGraphics cg) {
		missingImageCustomGraphicsSet.add(cg);
	}

	@Override
	public Collection<MissingImageCustomGraphics> reloadMissingImageCustomGraphics() {
		final Set<MissingImageCustomGraphics> reloadedSet = new HashSet<>();
		
		for (final MissingImageCustomGraphics mcg : missingImageCustomGraphicsSet) {
			final CyCustomGraphics cg = mcg.reloadImage();
			
			if (cg != null)
				reloadedSet.add(mcg);
		}
		
		missingImageCustomGraphicsSet.removeAll(reloadedSet);
		
		return reloadedSet;
	}
	
	private class ReloadMissingImagesTask implements Task {

		private boolean canceled;
		private final Set<CyNetworkView> networkViews;
		
		private final VisualProperty<?>[] cgProperties = new VisualProperty[] {
				NODE_CUSTOMGRAPHICS_1,
				NODE_CUSTOMGRAPHICS_2,
				NODE_CUSTOMGRAPHICS_3,
				NODE_CUSTOMGRAPHICS_4,
				NODE_CUSTOMGRAPHICS_5,
				NODE_CUSTOMGRAPHICS_6,
				NODE_CUSTOMGRAPHICS_7,
				NODE_CUSTOMGRAPHICS_8,
				NODE_CUSTOMGRAPHICS_9,
		};
		
		public ReloadMissingImagesTask(final Set<CyNetworkView> networkViews) {
			this.networkViews = networkViews;
		}

		@Override
		public void run(final TaskMonitor taskMonitor) throws Exception {
			final Collection<MissingImageCustomGraphics> reloaded = reloadMissingImageCustomGraphics();
			
			if (!reloaded.isEmpty() && networkViews != null) {
				// Create a set of visual styles that contain reloaded custom graphics
				final Set<VisualStyle> updatedStyles = new HashSet<>();
				final VisualMappingManager vmManager = serviceRegistrar.getService(VisualMappingManager.class);
				
				for (VisualStyle style : vmManager.getAllVisualStyles()) {
					for (VisualProperty<?> vp : cgProperties) {
						// First check the default value
						final Object defValue = style.getDefaultValue(vp);
						
						if (defValue != null && reloaded.contains(defValue)) {
							updatedStyles.add(style);
							break;
						}
						
						if (canceled) return;
						
						// Then check the mapping
						final VisualMappingFunction<?, ?> fn = style.getVisualMappingFunction(vp);
						
						if (fn instanceof PassthroughMapping) {
							// Just add this style; we don't want to check all networks' mapped attributes
							updatedStyles.add(style);
							break;
						} else if (fn instanceof DiscreteMapping) {
							final DiscreteMapping<?, ?> dm = (DiscreteMapping<?, ?>) fn;
							final Map map = dm.getAll();
							
							for (MissingImageCustomGraphics mcg : reloaded) {
								if (map.containsValue(mcg)) {
									updatedStyles.add(style);
									break;
								}
							}
						} else if (fn instanceof ContinuousMapping) {
							final ContinuousMapping<?, ?> cm = (ContinuousMapping<?, ?>) fn;
							
							for (ContinuousMappingPoint<?, ?> point : cm.getAllPoints()) {
								final BoundaryRangeValues<?> range = point.getRange();
								
								if ( (range.equalValue != null && reloaded.contains(range.equalValue)) ||
									 (range.lesserValue != null && reloaded.contains(range.lesserValue)) ||
									 (range.greaterValue != null && reloaded.contains(range.greaterValue)) ) {
									updatedStyles.add(style);
									break;
								}
							}
						}
					}
				}
				
				for (CyNetworkView networkView: networkViews) {
					if (canceled) return;
					
					// Check bypass values
					for (View<CyNode> nv : networkView.getNodeViews()) {
						for (VisualProperty<?> vp : cgProperties) {
							if (nv.isDirectlyLocked(vp)) {
								final Object value = nv.getVisualProperty(vp);
								
								if (canceled) return;
								
								// Set the same value again to force the renderer to repaint the node image
								if (value != null && reloaded.contains(value))
									nv.setLockedValue(vp, value);
							}
						}
					}
					
					// Only re-apply the styles that contain at least one reloaded image, as checked before
					final VisualStyle style = vmManager.getVisualStyle(networkView);
					
					if (updatedStyles.contains(style)) {
						style.apply(networkView);
						networkView.updateView();
					}
				}
			}
		}

		@Override
		public void cancel() {
			canceled = true;
		}
	}
}

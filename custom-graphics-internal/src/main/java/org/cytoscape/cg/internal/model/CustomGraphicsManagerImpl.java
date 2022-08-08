package org.cytoscape.cg.internal.model;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.CyUserLog;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;
import org.cytoscape.cg.internal.image.MissingImageCustomGraphics;
import org.cytoscape.cg.internal.task.RestoreImagesTaskFactory;
import org.cytoscape.cg.internal.task.SaveGraphicsToSessionTaskFactory;
import org.cytoscape.cg.model.AbstractURLImageCustomGraphics;
import org.cytoscape.cg.model.CGComparator;
import org.cytoscape.cg.model.CustomGraphicsManager;
import org.cytoscape.cg.model.IDGenerator;
import org.cytoscape.cg.model.NullCustomGraphics;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyIdentifiable;
import org.cytoscape.model.CyNode;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphics;
import org.cytoscape.view.presentation.customgraphics.CyCustomGraphicsFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.view.vizmap.mappings.ContinuousMapping;
import org.cytoscape.view.vizmap.mappings.DiscreteMapping;
import org.cytoscape.view.vizmap.mappings.PassthroughMapping;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public final class CustomGraphicsManagerImpl implements CustomGraphicsManager, CyStartListener,
		SessionAboutToBeSavedListener, SessionAboutToBeLoadedListener, SessionLoadedListener {

	private static final Logger logger = LoggerFactory.getLogger(CyUserLog.NAME);

	private static final String IMAGE_DIR_NAME = "images3";
	private static final String APP_NAME = "org.cytoscape.ding.customgraphicsmgr";
	private static final String TEMP_DIR = "java.io.tmpdir";
	private static final String PNG_EXT = ".png";
	private static final String SVG_EXT = ".svg";

	private final Map<Long, CyCustomGraphics> graphicsMap = new ConcurrentHashMap<>(16, 0.75f, 2);

	// URL to hash code map. For images associated with URL.
	protected final Map<URL, Long> sourceMap = new ConcurrentHashMap<>(16, 0.75f, 2);
	
	private final Set<MissingImageCustomGraphics<?>> missingImageCustomGraphicsSet = new HashSet<>();

	// Null Object
	private final File imageHomeDirectory;
	private final Map<Long, Boolean> isUsedCustomGraphics;
	private final Map<String, CyCustomGraphicsFactory> factoryMap;
	private final Map<CyCustomGraphicsFactory, Map<?, ?>> factoryPropsMap;
	
	private final Set<URL> defaultImageURLs;
	private final CyServiceRegistrar serviceRegistrar;

	private static CustomGraphicsManager instance;

	/**
	 * Creates an image pool object and restore existing images from user resource directory.
	 */
	public CustomGraphicsManagerImpl(Set<URL> defaultImageURLs, CyServiceRegistrar serviceRegistrar) {
		this.defaultImageURLs = defaultImageURLs;
		this.serviceRegistrar = serviceRegistrar;
		this.isUsedCustomGraphics = new HashMap<>();
		this.factoryMap = new HashMap<>();
		this.factoryPropsMap = new HashMap<>();

		var config = serviceRegistrar.getService(CyApplicationConfiguration.class);
		this.imageHomeDirectory = new File(config.getConfigurationDirectoryLocation(), IMAGE_DIR_NAME);

		instance = this;
	}

	public static CustomGraphicsManager getInstance() {
		return instance;
	}

	@Override
	public void addCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map<?, ?> props) {
		if (factory == null)
			return;
		
		var key = (String) props.get(SUPPORTED_CLASS_ID);
		
		if (key == null)
			key = factory.getSupportedClass().getName();
		
		factoryMap.put(key, factory);
		factoryPropsMap.put(factory, props);
	}

	@Override
	public void removeCustomGraphicsFactory(CyCustomGraphicsFactory factory, Map<?, ?> props) {
		if (factory == null)
			return;
		
		var iter = factoryMap.entrySet().iterator();
		
		while (iter.hasNext()) {
			var entry = iter.next();
			
			if (factory.equals(entry.getValue()))
				iter.remove();
		}
		
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
	@SuppressWarnings("unchecked")
	public Collection<CyCustomGraphicsFactory> getCustomGraphicsFactories(Class<? extends CyIdentifiable> targetType) {
		Collection<CyCustomGraphicsFactory> col = null;
		var all = factoryMap.values();
		
		if (all != null) {
			col = new HashSet<>();
			
			for (var f : all) {
				if (f.getSupportedTargetTypes().contains(targetType))
					col.add(f);
			}
		}
		
		return col != null ? col : (Collection) Collections.emptySet();
	}

	@Override
	public void addCustomGraphics(CyCustomGraphics graphics, URL source) {
		if (graphics == null)
			throw new IllegalArgumentException("Custom Graphics and its ID should not be null.");

		if (graphics.getIdentifier() == null)
			graphics.setIdentifier(getNextAvailableID());

		// Source URL is an optional field.
		if (source != null)
			sourceMap.put(source, graphics.getIdentifier());

		graphicsMap.put(graphics.getIdentifier(), graphics);
		isUsedCustomGraphics.put(graphics.getIdentifier(), false);
	}

	@Override
	public void removeCustomGraphics(Long id) {
		var cg = graphicsMap.get(id);
		
		if (cg != null && cg != NullCustomGraphics.getNullObject()) {
			graphicsMap.remove(id);
			isUsedCustomGraphics.remove(id);
		}
	}

	@Override
	public CyCustomGraphics getCustomGraphicsByID(Long id) {
		return graphicsMap.get(id);
	}

	@Override
	public CyCustomGraphics getCustomGraphicsBySourceURL(URL sourceURL) {
		var id = sourceMap.get(sourceURL);
		
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

		var values = new ArrayList<>(graphicsMap.values());
		Collections.sort(values, new CGComparator());
		
		return values;
	}

	@Override
	public Collection<CyCustomGraphics> getAllPersistantCustomGraphics() {
		var cgSet = new HashSet<CyCustomGraphics>();
		
		for (var cg : getAllCustomGraphics()) {
			// Currently, we only export BitmapCustomGraphics to the session file.  This may change in the future...
			if (cg instanceof AbstractURLImageCustomGraphics) {
				var urlCG = (AbstractURLImageCustomGraphics<?>) cg;
				
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
		var props = new Properties();
		
		// Use hash code as the key, and value will be a string returned by toString() method.
		// This means all CyCustomGraphics implementations should have a special toString method.
		for (var cg : graphicsMap.values()) {
			props.setProperty(cg.getIdentifier().toString(), cg.toSerializableString());
		}
		
		return props;
	}

	@Override
	public SortedSet<Long> getIDSet() {
		return new TreeSet<>(graphicsMap.keySet());
	}

	@Override
	public boolean isUsedInCurrentSession(final CyCustomGraphics graphics) {
		if (graphics == null || !isUsedCustomGraphics.containsKey(graphics.getIdentifier()))
			return false;

		return isUsedCustomGraphics.get(graphics.getIdentifier());
	}

	@Override
	public void setUsedInCurrentSession(CyCustomGraphics graphics, Boolean isUsed) {
		if (isUsed == null || graphics == null)
			return;

		if (!isUsedCustomGraphics.containsKey(graphics.getIdentifier())) {
			// Just ignore.
			return;
		}

		isUsedCustomGraphics.put(graphics.getIdentifier(), isUsed);
	}
	

	@Override
	public void handleEvent(CyStartEvent e) {
		// Restore Custom Graphics from the directory.
		var taskFactory = new RestoreImagesTaskFactory(defaultImageURLs, imageHomeDirectory, serviceRegistrar);
		serviceRegistrar.getService(DialogTaskManager.class).execute(taskFactory.createTaskIterator());
	}

//	@Override
//	public void handleEvent(CyShutdownEvent e) {
//		// Persist images
//		logger.info("Start Saving images to: " + imageHomeDirectory);
//
//		// Create Task
//		var factory = new SaveUserImagesTaskFactory(imageHomeDirectory, this);
//
//		try {
//			// FIXME how this section can wait until everything is done?
//			serviceRegistrar.getService(DialogTaskManager.class).execute(factory.createTaskIterator());
//		} catch (Exception e1) {
//			logger.error("Could not save images to disk.", e1);
//		}
//
//		logger.info("========== Image saving process finished =============");
//	}

	@Override
	public void handleEvent(SessionAboutToBeSavedEvent e) {
		var factory = new SaveGraphicsToSessionTaskFactory(imageHomeDirectory, e);

		try {
			// Make sure this task is executed synchronously in the current thread!
			serviceRegistrar.getService(SynchronousTaskManager.class).execute(factory.createTaskIterator());
		} catch (Exception ex) {
			logger.error("Could not save images to .", ex);
		}
	}
	
	@Override
	public void handleEvent(SessionAboutToBeLoadedEvent e) {
		// Since version 3.10, the current images are removed before the new ones are restored!
		removeAllCustomGraphics();
		
		// Delete the actual image files from the TEMP folder
		var dir = new File(System.getProperty(TEMP_DIR));
		var files = dir.listFiles();
		
		for (var f : files) {
			if (isSupportedImageFile(f))
				f.delete();
		}
	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		// Add new images
		var sess = e.getLoadedSession();

		if (sess != null) {
			var filesMap = sess.getAppFileListMap();

			if (filesMap != null) {
				var files = filesMap.get(APP_NAME);
				
				if (files != null && files.size() != 0) {
					// get parent directory
					var parent = files.get(0).getParentFile();
					var taskFactory = new RestoreImagesTaskFactory(new HashSet<>(), parent, serviceRegistrar);
					var taskIterator = taskFactory.createTaskIterator();
					
					taskIterator.append(new ReloadMissingImagesTask(sess.getNetworkViews()));
					serviceRegistrar.getService(DialogTaskManager.class).execute(taskIterator);
				}
			}
		}
	}

	@Override
	public Long getNextAvailableID() {
		Long key = IDGenerator.getIDGenerator().getNextId();
		
		while (graphicsMap.get(key) != null)
			key = IDGenerator.getIDGenerator().getNextId();
		
		return key;
	}

	@Override
	public void addMissingImageCustomGraphics(MissingImageCustomGraphics<?> cg) {
		missingImageCustomGraphicsSet.add(cg);
	}

	@Override
	public Collection<MissingImageCustomGraphics<?>> reloadMissingImageCustomGraphics() {
		var reloadedSet = new HashSet<MissingImageCustomGraphics<?>>();
		
		for (var mcg : missingImageCustomGraphicsSet) {
			var cg = mcg.reloadImage();
			
			if (cg != null)
				reloadedSet.add(mcg);
		}
		
		missingImageCustomGraphicsSet.removeAll(reloadedSet);
		
		return reloadedSet;
	}
	
	@Override
	public boolean isSupportedImageFile(File file) {
		var name = file.getName().toLowerCase();
		
		return file.isFile() && (name.endsWith(PNG_EXT) || name.endsWith(SVG_EXT));
	}
	
	private class ReloadMissingImagesTask implements Task {

		private boolean canceled;
		private final Set<CyNetworkView> networkViews;
		
		private final String[] nodeCGIdentifiers = new String[] {
				"NODE_CUSTOMGRAPHICS_1",
				"NODE_CUSTOMGRAPHICS_2",
				"NODE_CUSTOMGRAPHICS_3",
				"NODE_CUSTOMGRAPHICS_4",
				"NODE_CUSTOMGRAPHICS_5",
				"NODE_CUSTOMGRAPHICS_6",
				"NODE_CUSTOMGRAPHICS_7",
				"NODE_CUSTOMGRAPHICS_8",
				"NODE_CUSTOMGRAPHICS_9",
		};
		private final String[] columnCGIdentifiers = new String[] {
				"CELL_CUSTOMGRAPHICS",
		};
		
		public ReloadMissingImagesTask(Set<CyNetworkView> networkViews) {
			this.networkViews = networkViews;
		}

		@Override
		public void run(TaskMonitor tm) throws Exception {
			var reloaded = reloadMissingImageCustomGraphics();
			
			if (!reloaded.isEmpty() && networkViews != null) {
				var rendererManager = serviceRegistrar.getService(RenderingEngineManager.class);
				
				var netLexicon = rendererManager.getDefaultVisualLexicon();
				var set1 = getVisualProperties(netLexicon, CyNode.class, nodeCGIdentifiers);
				reloadImages(reloaded, set1);
				
				var tableLexicon = rendererManager.getDefaultTableVisualLexicon();
				var set2 = getVisualProperties(tableLexicon, CyColumn.class, columnCGIdentifiers);
				reloadImages(reloaded, set2);
			}
		}

		@Override
		public void cancel() {
			canceled = true;
		}
		
		private Set<VisualProperty<?>> getVisualProperties(VisualLexicon lexicon, Class<?> type, String[] identifiers) {
			var props = new HashSet<VisualProperty<?>>();
			
			for (var id : identifiers) {
				var vp = lexicon.lookup(type, id);
				
				if (vp != null)
					props.add(vp);
			}
			
			return props;
		}
		
		private void reloadImages(Collection<MissingImageCustomGraphics<?>> reloaded, Set<VisualProperty<?>> props) {
			// Create a set of visual styles that contain reloaded custom graphics
			var updatedStyles = new HashSet<VisualStyle>();
			var vmManager = serviceRegistrar.getService(VisualMappingManager.class);
			
			for (var style : vmManager.getAllVisualStyles()) {
				for (var vp : props) {
					// First check the default value
					var defValue = style.getDefaultValue(vp);
					
					if (defValue != null && reloaded.contains(defValue)) {
						updatedStyles.add(style);
						break;
					}
					
					if (canceled)
						return;
					
					// Then check the mapping
					var fn = style.getVisualMappingFunction(vp);
					
					if (fn instanceof PassthroughMapping) {
						// Just add this style; we don't want to check all networks' mapped attributes
						updatedStyles.add(style);
						break;
					} else if (fn instanceof DiscreteMapping) {
						var dm = (DiscreteMapping<?, ?>) fn;
						var map = dm.getAll();
						
						for (var mcg : reloaded) {
							if (map.containsValue(mcg)) {
								updatedStyles.add(style);
								break;
							}
						}
					} else if (fn instanceof ContinuousMapping) {
						var cm = (ContinuousMapping<?, ?>) fn;
						
						for (var point : cm.getAllPoints()) {
							var range = point.getRange();
							
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
			
			for (var networkView : networkViews) {
				if (canceled)
					return;
				
				// Check bypass values
				for (var nv : networkView.getNodeViews()) {
					for (var vp : props) {
						if (nv.isDirectlyLocked(vp)) {
							var value = nv.getVisualProperty(vp);
							
							if (canceled) return;
							
							// Set the same value again to force the renderer to repaint the node image
							if (value != null && reloaded.contains(value))
								nv.setLockedValue(vp, value);
						}
					}
				}
				
				// Only re-apply the styles that contain at least one reloaded image, as checked before
				var style = vmManager.getVisualStyle(networkView);
				
				if (updatedStyles.contains(style)) {
					style.apply(networkView);
					networkView.updateView();
				}
			}
		}
	}
}

package org.cytoscape.ding.customgraphicsmgr.internal;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.ding.customgraphics.CustomGraphicsManager;
import org.cytoscape.ding.customgraphics.CyCustomGraphics;
import org.cytoscape.ding.customgraphics.NullCustomGraphics;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.property.CyProperty;
import org.cytoscape.session.CySession;
import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.session.events.SessionAboutToBeSavedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CustomGraphicsManagerImpl implements CustomGraphicsManager, CyShutdownListener,
		SessionAboutToBeSavedListener, SessionLoadedListener {

	private static final Logger logger = LoggerFactory.getLogger(CustomGraphicsManagerImpl.class);

	private static final String IMAGE_DIR_NAME = "images3";
	private static final String APP_NAME = "org.cytoscape.ding.customgraphicsmgr";
	private static final String SESSION_IMAGE_DIR_NAME = "custom_graphics";

	protected final Map<Long, CyCustomGraphics> graphicsMap = new ConcurrentHashMap<Long, CyCustomGraphics>();

	// URL to hash code map. For images associated with URL.
	protected final Map<URL, Long> sourceMap = new ConcurrentHashMap<URL, Long>();

	// Null Object
	private static final CyCustomGraphics NULL = NullCustomGraphics.getNullObject();

	private final File imageHomeDirectory;
	protected final Map<CyCustomGraphics, Boolean> isUsedCustomGraphics;
	private final DialogTaskManager taskManager;
	
	private final CyEventHelper eventHelper;

	/**
	 * Creates an image pool object and restore existing images from user
	 * resource directory.
	 */
	public CustomGraphicsManagerImpl(final CyProperty<Properties> properties, final DialogTaskManager taskManager,
			final CyApplicationConfiguration config, final CyEventHelper eventHelper) {

		this.taskManager = taskManager;
		this.eventHelper = eventHelper;
		this.isUsedCustomGraphics = new HashMap<CyCustomGraphics, Boolean>();

		if (properties == null)
			throw new NullPointerException("Property object is null.");

		final Properties props = properties.getProperties();

		if (props == null)
			throw new NullPointerException("Property is missing.");

		this.imageHomeDirectory = new File(config.getConfigurationDirectoryLocation(), IMAGE_DIR_NAME);

		logger.debug("\n!!!!!!!!!!!!!!!!! Cytoscape image directory: " + imageHomeDirectory.toString());

		graphicsMap.put(NULL.getIdentifier(), NULL);
		this.isUsedCustomGraphics.put(NULL, false);

		final RestoreImageTaskFactory taskFactory = new RestoreImageTaskFactory(imageHomeDirectory, this, eventHelper);
		taskManager.execute(taskFactory);
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
		if (cg != null && cg != NULL) {
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

	/**
	 * Get a collection of all Custom Graphics in current session.
	 * 
	 * @return
	 */
	@Override
	public Collection<CyCustomGraphics> getAllCustomGraphics() {
		return graphicsMap.values();
	}

	/**
	 * Remove all custom graphics from memory.
	 */
	@Override
	public void removeAllCustomGraphics() {
		this.graphicsMap.clear();
		this.sourceMap.clear();
		this.isUsedCustomGraphics.clear();

		// Null Graphics should not be removed.
		this.graphicsMap.put(NULL.getIdentifier(), NULL);

	}

	/**
	 * Convert current list of custom graphics into Property object.
	 * 
	 * @return
	 */
	@Override
	public Properties getMetadata() {
		// Null graphics object should not be in this property.
		graphicsMap.remove(NULL.getIdentifier());

		final Properties props = new Properties();
		// Use hash code as the key, and value will be a string returned by
		// toString() method.
		// This means all CyCustomGraphics implementations should have a special
		// toString method.
		for (final CyCustomGraphics graphics : graphicsMap.values())
			props.setProperty(graphics.getIdentifier().toString(), graphics.toString());
		graphicsMap.put(NULL.getIdentifier(), NULL);
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
			taskManager.execute(factory);
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
		final SaveGraphicsToSessionTaskFactory factory = new SaveGraphicsToSessionTaskFactory(imageHomeDirectory, this, e);

		try {
			taskManager.execute(factory);
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
					final RestoreImageTaskFactory taskFactory = new RestoreImageTaskFactory(parent, this, eventHelper);
					taskManager.execute(taskFactory);
				}
			}
		}
	}
}

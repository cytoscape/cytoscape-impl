package org.cytoscape.internal.actions;

import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.internal.task.OpenRecentSessionTaskFactory;
import org.cytoscape.io.read.CySessionReaderManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.work.TaskFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update menu
 * 
 */
public class RecentSessionManager implements SessionLoadedListener, CyShutdownListener {
	
	private static final Logger logger = LoggerFactory.getLogger(RecentSessionManager.class);
	
	private static final String MENU_CATEGORY = "File.Recent Session";

	private final RecentlyOpenedTracker tracker;
	private final CyServiceRegistrar registrar;

	private final CySessionManager sessionManager;
	private final CySessionReaderManager readerManager;
	private final CyApplicationManager appManager;

	private final Set<OpenRecentSessionTaskFactory> currentMenuItems;
	
	private final DummyAction factory;

	public RecentSessionManager(final RecentlyOpenedTracker tracker, final CyServiceRegistrar registrar,
			final CySessionManager sessionManager, final CySessionReaderManager readerManager,
			final CyApplicationManager appManager) {
		this.tracker = tracker;
		this.registrar = registrar;
		this.sessionManager = sessionManager;
		this.readerManager = readerManager;
		this.appManager = appManager;
		
		this.currentMenuItems = new HashSet<OpenRecentSessionTaskFactory>();

		factory = new DummyAction();
		
		updateMenuItems();
	}

	private void updateMenuItems() {
		
		// If there is no recent items, add dummy menu.
		if(tracker.getRecentlyOpenedURLs().size() == 0) {
			registrar.registerService(factory, CyAction.class, new Hashtable<String, String>());
			return;
		}
			
		// Unregister services
		registrar.unregisterService(factory, CyAction.class);
		for (final OpenRecentSessionTaskFactory currentItem : currentMenuItems)
			registrar.unregisterAllServices(currentItem);

		currentMenuItems.clear();

		final List<URL> urls = tracker.getRecentlyOpenedURLs();

		for (final URL url : urls) {
			final Dictionary<String, String> dict = new Hashtable<String, String>();
			dict.put("preferredMenu", MENU_CATEGORY);
			dict.put("title", url.getFile());
			dict.put("menuGravity", "6.0");
			final OpenRecentSessionTaskFactory factory = new OpenRecentSessionTaskFactory(sessionManager, readerManager, appManager, tracker, url);
			registrar.registerService(factory, TaskFactory.class, dict);

			this.currentMenuItems.add(factory);
		}

	}

	@Override
	public void handleEvent(SessionLoadedEvent e) {
		updateMenuItems();
	}
	
	
	/**
	 * Dummy action to add menu item when no entry is available.
	 */
	private final class DummyAction extends AbstractCyAction {

		private static final long serialVersionUID = 4904285068314580548L;

		public DummyAction() {
			super("(No recent session files)");
			setPreferredMenu(MENU_CATEGORY);
			setMenuGravity(6.0f);
			this.setEnabled(false);
		}

		@Override
		public void actionPerformed(ActionEvent e) {}
	}

	@Override
	public void handleEvent(CyShutdownEvent e) {
		logger.info("Saving recently used session file list...");
		try {
			tracker.writeOut();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			throw new RuntimeException("Could not save recently opened session file list.", ex);
		}
	}
	
	

}

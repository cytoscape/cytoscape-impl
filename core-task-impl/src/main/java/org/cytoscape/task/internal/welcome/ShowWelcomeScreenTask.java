package org.cytoscape.task.internal.welcome;

import javax.swing.JDialog;
import javax.swing.SwingUtilities;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.datasource.DataSourceManager;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.task.internal.session.OpenSessionTaskFactory;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;

public class ShowWelcomeScreenTask extends AbstractTask {

	private final OpenBrowser openBrowserServiceRef;
	private final RecentlyOpenedTracker fileTracker;

	private final TaskManager guiTaskManager;
	private final OpenSpecifiedSessionTaskFactory taskFactory;
	private final OpenSessionTaskFactory openTaskFactory;
	private final LoadMitabFileTaskFactory loadTF;
	private final CyApplicationConfiguration config;
	private final TaskFactory layoutTF;
	
	private final DataSourceManager dsManager;

	public ShowWelcomeScreenTask(final OpenBrowser openBrowserServiceRef, RecentlyOpenedTracker fileTracker,
			TaskManager guiTaskManager, OpenSpecifiedSessionTaskFactory taskFactory,
			final OpenSessionTaskFactory openTaskFactory, final LoadMitabFileTaskFactory loadTF,
			final CyApplicationConfiguration config, final TaskFactory layoutTF, final DataSourceManager dsManager) {
		this.openBrowserServiceRef = openBrowserServiceRef;
		this.openTaskFactory = openTaskFactory;
		this.loadTF = loadTF;
		this.config = config;
		this.layoutTF = layoutTF;
		
		this.dsManager = dsManager;

		this.fileTracker = fileTracker;

		this.guiTaskManager = guiTaskManager;
		this.taskFactory = taskFactory;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final JDialog welcome = new WelcomeScreenDialog(null, openBrowserServiceRef, fileTracker,
						guiTaskManager, taskFactory, openTaskFactory, loadTF, config, layoutTF, dsManager);
				welcome.setVisible(true);
			}
		});
	}

}

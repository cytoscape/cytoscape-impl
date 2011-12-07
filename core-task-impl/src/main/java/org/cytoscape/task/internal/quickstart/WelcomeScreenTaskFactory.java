package org.cytoscape.task.internal.quickstart;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.io.util.RecentlyOpenedTracker;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.task.internal.quickstart.subnetworkbuilder.SubnetworkBuilderUtil;
import org.cytoscape.task.internal.session.OpenSessionTaskFactory;
import org.cytoscape.task.internal.welcome.LoadMitabFileTaskFactory;
import org.cytoscape.task.internal.welcome.OpenSpecifiedSessionTaskFactory;
import org.cytoscape.task.internal.welcome.ShowWelcomeScreenTask;
import org.cytoscape.util.swing.OpenBrowser;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;

public class WelcomeScreenTaskFactory extends QuickStartTaskFactory {

	private OpenBrowser openBrowserServiceRef;
	private RecentlyOpenedTracker fileTracker;

	private TaskManager guiTaskManager;
	private OpenSpecifiedSessionTaskFactory taskFactory;
	private final OpenSessionTaskFactory openTaskFactory;
	private final LoadMitabFileTaskFactory loadTF;
	private final CyApplicationConfiguration config;
	private final TaskFactory layoutTF;
	
	private final BookmarksUtil bkUtil;
	private final Bookmarks bookmarks;

	public WelcomeScreenTaskFactory(OpenBrowser openBrowserServiceRef, final ImportTaskUtil util,
			final CyNetworkManager networkManager, final SubnetworkBuilderUtil subnetworkUtil,
			RecentlyOpenedTracker fileTracker, TaskManager guiTaskManager, OpenSpecifiedSessionTaskFactory taskFactory,
			final OpenSessionTaskFactory openTaskFactory, final LoadMitabFileTaskFactory loadTF,
			final CyApplicationConfiguration config, final TaskFactory layoutTF, BookmarksUtil bkUtil,
			Bookmarks bookmarks) {
		super(util, networkManager, subnetworkUtil);
		this.openBrowserServiceRef = openBrowserServiceRef;
		this.fileTracker = fileTracker;
		this.loadTF = loadTF;
		this.config = config;
		this.layoutTF = layoutTF;
		
		this.bkUtil = bkUtil;
		this.bookmarks = bookmarks;

		this.guiTaskManager = guiTaskManager;
		this.taskFactory = taskFactory;
		this.openTaskFactory = openTaskFactory;
	}

	public TaskIterator createTaskIterator() {
		// return new TaskIterator(new StartTask(new QuickStartState(), util,
		// networkManager, subnetworkUtil, openBrowserServiceRef));
		return new TaskIterator(new ShowWelcomeScreenTask(openBrowserServiceRef, fileTracker, this.guiTaskManager,
				this.taskFactory, openTaskFactory, loadTF, config, layoutTF, bkUtil, bookmarks));
	}
}

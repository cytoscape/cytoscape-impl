package org.cytoscape.cg.internal.task;

import java.io.File;
import java.util.ArrayList;

import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class SaveGraphicsToSessionTask extends AbstractTask {
	
	private static final String APP_NAME = "org.cytoscape.ding.customgraphicsmgr";

	private final File imageHomeDirectory;
	private final SessionAboutToBeSavedEvent e;

	SaveGraphicsToSessionTask(File imageHomeDirectory, SessionAboutToBeSavedEvent e) {
		this.imageHomeDirectory = imageHomeDirectory;
		this.e = e;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		// Add it to the apps list
		var fileList = new ArrayList<File>();
		var fileArray = imageHomeDirectory.list();

		for (var file : fileArray)
			fileList.add(new File(imageHomeDirectory, file));

		e.addAppFiles(APP_NAME, fileList);
	}
}

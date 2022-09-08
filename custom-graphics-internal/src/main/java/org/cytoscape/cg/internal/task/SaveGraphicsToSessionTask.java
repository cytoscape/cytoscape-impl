package org.cytoscape.cg.internal.task;

import java.io.File;
import java.util.ArrayList;

import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class SaveGraphicsToSessionTask extends AbstractTask {
	
	private static final String APP_NAME = "org.cytoscape.ding.customgraphicsmgr";

	private final File imageHomeDirectory;
	private final SessionAboutToBeSavedEvent event;

	SaveGraphicsToSessionTask(File imageHomeDirectory, SessionAboutToBeSavedEvent event) {
		this.imageHomeDirectory = imageHomeDirectory;
		this.event = event;
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Save Graphics to Session");
		tm.setStatusMessage("Saving image library to session file...");
		tm.setProgress(0.0);
		
		// Add it to the apps list
		var fileList = new ArrayList<File>();
		var fileArray = imageHomeDirectory.list();

		for (var filename : fileArray) {
			var file = new File(imageHomeDirectory, filename);
			fileList.add(file);
		}

		event.addAppFiles(APP_NAME, fileList);
	}
}

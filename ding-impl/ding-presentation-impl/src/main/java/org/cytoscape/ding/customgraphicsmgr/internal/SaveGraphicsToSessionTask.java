package org.cytoscape.ding.customgraphicsmgr.internal;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.cytoscape.session.events.SessionAboutToBeSavedEvent;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskMonitor;

public class SaveGraphicsToSessionTask implements Task {
	
	private static final String APP_NAME = "org.cytoscape.ding.customgraphicsmgr";
	
	private final File imageHomeDirectory;
	private final SessionAboutToBeSavedEvent e;
	
	SaveGraphicsToSessionTask(final File imageHomeDirectory, final SessionAboutToBeSavedEvent e) {
		this.imageHomeDirectory = imageHomeDirectory;
		this.e = e;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		// Add it to the apps list
		final List<File> fileList = new ArrayList<File>();
		final String[] fileArray = imageHomeDirectory.list();
		for (final String file : fileArray)
			fileList.add(new File(imageHomeDirectory, file));

		e.addAppFiles(APP_NAME, fileList);
	}

	@Override
	public void cancel() {
		// TODO Auto-generated method stub
	}

}

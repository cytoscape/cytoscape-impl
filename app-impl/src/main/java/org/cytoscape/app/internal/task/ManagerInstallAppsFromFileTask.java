package org.cytoscape.app.internal.task;

import java.io.File;
import java.awt.Container;
import java.awt.FileDialog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.LinkedList;
import java.util.Arrays;
import javax.swing.JFrame;
import java.awt.Window;
import javax.swing.JDialog;


import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.cytoscape.app.internal.task.InstallAppsFromFileTask;
import org.cytoscape.app.internal.task.ShowInstalledAppsTask;


public class ManagerInstallAppsFromFileTask extends AbstractTask {
	final AppManager appManager;
  final TaskManager taskManager;
  final CyServiceRegistrar serviceRegistrar;

	public ManagerInstallAppsFromFileTask(final AppManager appManager, final TaskManager taskManager, final CyServiceRegistrar serviceRegistrar) {
		this.appManager = appManager;
    this.taskManager = taskManager;
    this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
    final FileUtil fileUtil = serviceRegistrar.getService(FileUtil.class);
    FileChooserFilter fileChooserFilter = new FileChooserFilter(
				"Jar, Zip, and Karaf Kar Files (*.jar, *.zip, *.kar)", new String[] { "jar", "zip", "kar" });

		Collection<FileChooserFilter> fileChooserFilters = new LinkedList<FileChooserFilter>();
		fileChooserFilters.add(fileChooserFilter);
    JDialog wrapper = new JDialog((Window)null);
    wrapper.setVisible(true);
		final File[] files = fileUtil.getFiles(wrapper, "Choose file(s)", FileUtil.LOAD, FileUtil.LAST_DIRECTORY,
				"Install", true, fileChooserFilters);

		if (files != null) {
			TaskIterator ti = new TaskIterator();
			ti.append(new InstallAppsFromFileTask(Arrays.asList(files), appManager, true));
			ti.append(new ShowInstalledAppsTask(wrapper));
			taskManager.setExecutionContext(wrapper);
			taskManager.execute(ti);
	   }
  }
}

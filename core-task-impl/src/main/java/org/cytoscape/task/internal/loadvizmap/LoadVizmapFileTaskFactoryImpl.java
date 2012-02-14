package org.cytoscape.task.internal.loadvizmap;

import org.cytoscape.io.read.VizmapReaderManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.task.creation.LoadVisualStyles;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.io.File;


public class LoadVizmapFileTaskFactoryImpl implements TaskFactory, LoadVisualStyles {

	private final VizmapReaderManager vizmapReaderMgr;
	private final VisualMappingManager vmMgr;
	private final SynchronousTaskManager<?> syncTaskManager;

	private LoadVizmapFileTask task; 
	
	public LoadVizmapFileTaskFactoryImpl(VizmapReaderManager vizmapReaderMgr, VisualMappingManager vmMgr, SynchronousTaskManager<?> syncTaskManager) {
		this.vizmapReaderMgr = vizmapReaderMgr;
		this.vmMgr = vmMgr;
		this.syncTaskManager = syncTaskManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		task = new LoadVizmapFileTask(vizmapReaderMgr, vmMgr);
		return new TaskIterator(2,task);
	}

	public Set<VisualStyle> loadStyles(File f) {
		// Set up map containing values to be assigned to tunables.
		// The name "file" is the name of the tunable field in LoadVizmapFileTask.
		Map<String,Object> m = new HashMap<String,Object>();
		m.put("file",f);

		syncTaskManager.setExecutionContext(m);
		syncTaskManager.execute(this);

		return task.getStyles();
	}
}

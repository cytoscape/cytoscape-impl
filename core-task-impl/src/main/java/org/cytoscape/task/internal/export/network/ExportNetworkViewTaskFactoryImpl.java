package org.cytoscape.task.internal.export.network;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.export.network.ExportNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class ExportNetworkViewTaskFactoryImpl extends AbstractNetworkViewTaskFactory implements ExportNetworkViewTaskFactory {

	private CyNetworkViewWriterManager writerManager;

	private final TunableSetter tunableSetter; 

	
	public ExportNetworkViewTaskFactoryImpl(CyNetworkViewWriterManager writerManager, TunableSetter tunableSetter) {
		this.writerManager = writerManager;
		this.tunableSetter = tunableSetter;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView view) {
		return new TaskIterator(2,new CyNetworkViewWriter(writerManager, view));
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView view, File file) {
		final Map<String, Object> m = new HashMap<String, Object>();
		m.put("OutputFile", file);

		return tunableSetter.createTaskIterator(this.createTaskIterator(view), m); 
	}

}

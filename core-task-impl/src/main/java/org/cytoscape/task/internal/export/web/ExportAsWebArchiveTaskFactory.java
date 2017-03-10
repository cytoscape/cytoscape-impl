package org.cytoscape.task.internal.export.web;

import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.session.CySessionManager;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskIterator;

public class ExportAsWebArchiveTaskFactory extends AbstractTaskFactory {

	private static final Integer TH = 3000;
	
	private CySessionWriterFactory fullWriterFactory;
	private CySessionWriterFactory simpleWriterFactory;
	private CySessionWriterFactory zippedWriterFactory;
	
	private final CyNetworkManager networkManager;
	private final CyApplicationManager applicationManager;
	private final CySessionManager sessionManager;
	
	
	public ExportAsWebArchiveTaskFactory(final CyNetworkManager networkManager, final CyApplicationManager applicationManager,
			final CySessionManager sessionManager) {
		super();
		this.networkManager = networkManager;
		this.applicationManager = applicationManager;
		this.sessionManager = sessionManager;
	}

	/**
	 * 
	 * Find correct writer for the web archive.
	 * 
	 * @param writerFactory
	 * @param props
	 */
	@SuppressWarnings("rawtypes")
	public void registerFactory(final CySessionWriterFactory writerFactory, final Map props) {
		final Object id = props.get(ServiceProperties.ID);
		if(id == null) {
			return;
		}
		
		if (id.equals("fullWebSessionWriterFactory")) {
			this.fullWriterFactory = writerFactory;
		}

		if (id.equals("simpleWebSessionWriterFactory")) {
			this.simpleWriterFactory = writerFactory;
		}
		
		if (id.equals("zippedJsonWriterFactory")) {
			this.zippedWriterFactory = writerFactory;
		}
	}

	@SuppressWarnings("rawtypes")
	public void unregisterFactory(final CySessionWriterFactory writerFactory, final Map props) {
	}

	@Override
	public TaskIterator createTaskIterator() {
		final Set<CyNetwork> networks = networkManager.getNetworkSet();
		
		boolean showWarning = false;
		
		for(final CyNetwork network: networks) {
			final int nodeCount = network.getNodeCount();
			final int edgeCount = network.getEdgeCount();
			
			if(nodeCount >TH || edgeCount > TH) {
				showWarning = true;
				break;
			}
		}
		
		final ExportAsWebArchiveTask exportTask = 
				new ExportAsWebArchiveTask(fullWriterFactory, simpleWriterFactory, zippedWriterFactory, 
						applicationManager, sessionManager);
		
		if(showWarning) {
			return new TaskIterator(new ShowWarningTask(exportTask));
		} else {
			return new TaskIterator(exportTask);
		}
	}
}

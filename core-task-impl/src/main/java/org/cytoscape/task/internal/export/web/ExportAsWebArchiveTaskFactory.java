package org.cytoscape.task.internal.export.web;

import java.util.Map;

import org.cytoscape.io.write.CySessionWriterFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.ServiceProperties;
import org.cytoscape.work.TaskIterator;

public class ExportAsWebArchiveTaskFactory extends AbstractTaskFactory {

	private CySessionWriterFactory fullWriterFactory;
	private CySessionWriterFactory simpleWriterFactory;

	/**
	 * 
	 * Find correct writer for the web archive.
	 * 
	 * @param writerFactory
	 * @param props
	 */
	@SuppressWarnings("rawtypes")
	public void registerFactory(final CySessionWriterFactory writerFactory, final Map props) {
		if (props.get(ServiceProperties.ID).equals("fullWebSessionWriterFactory")) {
			this.fullWriterFactory = writerFactory;
		}

		if (props.get(ServiceProperties.ID).equals("simpleWebSessionWriterFactory")) {
			this.simpleWriterFactory = writerFactory;
		}
	}

	@SuppressWarnings("rawtypes")
	public void unregisterFactory(final CySessionWriterFactory writerFactory, final Map props) {
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new ExportAsWebArchiveTask(fullWriterFactory, simpleWriterFactory));
	}
}

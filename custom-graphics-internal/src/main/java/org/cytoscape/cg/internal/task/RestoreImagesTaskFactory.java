package org.cytoscape.cg.internal.task;

import java.io.File;
import java.net.URL;
import java.util.Set;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

public class RestoreImagesTaskFactory extends AbstractTaskFactory {

	private final File imageLocation;
	private final CyServiceRegistrar serviceRegistrar;
	private final Set<URL> defaultImageURLs;

	public RestoreImagesTaskFactory(
			Set<URL> defaultImageURLs,
			File imageLocation,
			CyServiceRegistrar serviceRegistrar
	) {
		this.serviceRegistrar = serviceRegistrar;
		this.imageLocation = imageLocation;
		this.defaultImageURLs = defaultImageURLs;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new RestoreImagesTask(defaultImageURLs, imageLocation, serviceRegistrar));
	}
}

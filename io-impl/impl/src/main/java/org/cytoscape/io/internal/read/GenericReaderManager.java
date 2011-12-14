package org.cytoscape.io.internal.read;


import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.work.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GenericReaderManager<T extends InputStreamTaskFactory, R extends Task>  {
	
	private static final Logger logger = LoggerFactory.getLogger( GenericReaderManager.class ); 
	
	protected final DataCategory category;
	protected final StreamUtil streamUtil;
	
	protected final Set<T> factories;
	

	public GenericReaderManager(final DataCategory category, final StreamUtil streamUtil) {
		this.category = category;
		this.streamUtil = streamUtil;
		
		factories = new HashSet<T>();
	}
	
	/**
	 * Listener for OSGi service
	 * 
	 * @param factory
	 * @param props
	 */
	public void addInputStreamTaskFactory(T factory, @SuppressWarnings("rawtypes") Map props) {
		if (factory == null)
			logger.warn("Specified factory is null!");
		else if (factory.getFileFilter().getDataCategory() == category) {
			logger.debug("adding IO taskFactory (factory = " + factory + 
			            ", category = " + category + ")");
			factories.add(factory);
		}
	}

	/**
	 * Listener for OSGi service
	 * 
	 * @param factory
	 * @param props
	 */
	public void removeInputStreamTaskFactory(T factory, @SuppressWarnings("rawtypes") Map props) {
		factories.remove(factory);
	}

	/**
	 * Gets the GraphReader that is capable of reading the specified file.
	 * 
	 * @param fileName
	 *            File name or null if no reader is capable of reading the file.
	 * @return GraphReader capable of reading the specified file.
	 */
	public R getReader(URI uri, String inputName) {
		
		if(uri == null) {
			logger.warn("URI is null");
			return null;
		}
		
		for (final T factory : factories) {
			final CyFileFilter cff = factory.getFileFilter();
			logger.info("4 ### Current Filter = " + cff.getDescription());
			
			logger.debug("Trying factory: " + factory + " with filter: " + cff);

			if (cff.accepts(uri, category)) {
				try {
					logger.info("Successfully found matched factory " + factory);
					// This returns strean using proxy if it exists.
					InputStream stream = streamUtil.getInputStream(uri.toURL());
					if (!stream.markSupported()) {
						stream = new BufferedInputStream(stream);
					}
					
					factory.setInputStream(stream, inputName);
					return (R) factory.createTaskIterator().next();
				} catch (IOException e) {
					logger.warn("Error opening stream to URI: " + uri.toString(), e);
				}
			}
		}

		logger.warn("No reader found for uri: " + uri.toString());
	 	return null;	
	}

	public R getReader(InputStream stream, String inputName) {
		try {

			if ( !stream.markSupported() )
				stream = new BufferedInputStream(stream);

			for (T factory : factories) {
				CyFileFilter cff = factory.getFileFilter();
				logger.debug("trying READER: " + factory + " with filter: " + cff);

				// Because we don't know who will provide the file filter or
				// what they might do with the InputStream, we provide a copy
				// of the first 2KB rather than the stream itself. 
				if (cff.accepts(CopyInputStream.copyKBytes(stream,1), category)) {
					logger.debug("successfully matched READER " + factory);
					factory.setInputStream(stream, inputName);
					return (R)factory.createTaskIterator().next();	
				}
			}
		} catch (IOException ioe) {
			logger.warn("Error setting input stream", ioe);
		}

		logger.warn("No reader found for input stream");
	 	return null;	
	}
}

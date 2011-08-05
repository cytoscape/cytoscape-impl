package org.cytoscape.internal.shutdown;


import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.AbstractCyWriter;
import org.cytoscape.io.write.CyPropertyWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.property.CyProperty;

import java.io.File;


/**
 * A utility Task implementation that will write the specified View to the
 * the specified image file using the specified RenderingEngine.
 */
public final class PropertyWriter extends AbstractCyWriter<CyPropertyWriterManager> {
	private final CyProperty property;
	private final String fileFilterDescription;

	/**
	 * @param writerManager  The {@link org.cytoscape.io.write.CyPropertyWriterManager} used to determine which type of
	 * file should be written.
	 * @param property       The {@link org.cytoscape.property.CyProperty} that should be serialised.
	 * @param outputFile     Where to write the serialised data to.
	 */
	public PropertyWriter(final CyPropertyWriterManager writerManager,
			      final CyFileFilter fileFilter, final CyProperty property,
			      final File outputFile)
	{
		super(writerManager);
		this.property = property;
		fileFilterDescription = fileFilter.getDescription();
		setOutputFile(outputFile);
	}

	protected String getExportFileFormat() {
		return fileFilterDescription;
	}

	/**
	 * {@inheritDoc}
	 */
	protected CyWriter getWriter(final CyFileFilter filter, final File file) throws Exception {
		return writerManager.getWriter(property.getProperties(), filter, file);
	}
}

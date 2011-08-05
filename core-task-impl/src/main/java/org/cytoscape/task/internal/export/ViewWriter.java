package org.cytoscape.task.internal.export;


import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.RenderingEngine;
import org.cytoscape.work.Tunable;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.PresentationWriterManager;

import java.io.File;


/**
 * A utility Task implementation that will write the specified View to the
 * the specified image file using the specified RenderingEngine.
 */
public final class ViewWriter extends TunableAbstractCyWriter<PresentationWriterManager> {
	private final View<?> view;
	private final RenderingEngine<?> re;

	/**
	 * @param writerManager The {@link org.cytoscape.io.write.PresentationWriterManager} used to determine which type of
	 * file should be written.
	 * @param view The View object to be written to the specified file.
	 * @param re The RenderingEngine used to generate the image to be written to the file.
	 */
	public ViewWriter(final PresentationWriterManager writerManager, final View<?> view, final RenderingEngine<?> re ) {
		super(writerManager);

		if ( view == null )
			throw new NullPointerException("view is null");
		this.view = view;

		if ( re == null )
			throw new NullPointerException("rendering engine is null");
		this.re = re;
	}

	/**
	 * {@inheritDoc}
	 */
	protected CyWriter getWriter(CyFileFilter filter, File file) throws Exception {
		if (!fileExtensionIsOk(file))
			file = addOrReplaceExtension(outputFile);
		return writerManager.getWriter(view,re,filter,file);
	}

	@Tunable(description="Save Image As:", params="fileCategory=image;input=false")
	public File getOutputFile() {
		return outputFile;
	}
}

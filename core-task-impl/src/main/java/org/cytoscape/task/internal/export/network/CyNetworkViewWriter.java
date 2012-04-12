package org.cytoscape.task.internal.export.network;


import java.io.File;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.task.internal.export.TunableAbstractCyWriter;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.Tunable;


/**
 * A utility Task implementation specifically for writing a {@link org.cytoscape.view.model.CyNetworkView}.
 */
public final class CyNetworkViewWriter extends TunableAbstractCyWriter<CyNetworkViewWriterFactory,CyNetworkViewWriterManager> {
	// the view to be written
	private final CyNetworkView view;

	/**
	 * @param writerManager The {@link org.cytoscape.io.write.CyNetworkViewWriterManager} used to determine which 
	 * {@link org.cytoscape.io.write.CyNetworkViewWriterFactory} to use to write the file.
	 * @param view The {@link org.cytoscape.view.model.CyNetworkView} to be written out. 
	 */
	public CyNetworkViewWriter(final CyNetworkViewWriterManager writerManager, final CyNetworkView view ) {
		super(writerManager);
		
		if (view == null)
			throw new NullPointerException("View is null!");
		this.view = view;
	}

	/**
	 * {@inheritDoc}  
	 */
	@Override
	protected CyWriter getWriter(CyFileFilter filter, File file)  throws Exception{
		if (!fileExtensionIsOk(file))
			file = addOrReplaceExtension(outputFile);

		return writerManager.getWriter(view,filter,file);
	}
	
	@Tunable(description="Save Network As:", params="fileCategory=network;input=false", dependsOn="options!=")
	public  File getOutputFile() {	
		return outputFile;
	}
	
	@ProvidesTitle
	public String getTitle() {
		return "Export Network";
	}
}

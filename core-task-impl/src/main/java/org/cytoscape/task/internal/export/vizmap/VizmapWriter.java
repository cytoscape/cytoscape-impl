package org.cytoscape.task.internal.export.vizmap;

import java.io.File;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.VizmapWriterManager;
import org.cytoscape.task.internal.export.TunableAbstractCyWriter;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

public class VizmapWriter extends TunableAbstractCyWriter<VizmapWriterManager> {

	private final VisualMappingManager vmMgr;

	public VizmapWriter(VizmapWriterManager writerManager, VisualMappingManager vmMgr) {
		super(writerManager);
		if (vmMgr == null) throw new NullPointerException("VisualMappingManager is null");
		this.vmMgr = vmMgr;
	}

	@Override
	protected CyWriter getWriter(CyFileFilter filter, File file) throws Exception {
		if (!fileExtensionIsOk(file))
			file = addOrReplaceExtension(outputFile);

		Set<VisualStyle> styles = vmMgr.getAllVisualStyles();

		return writerManager.getWriter(styles, filter, file);
	}
}

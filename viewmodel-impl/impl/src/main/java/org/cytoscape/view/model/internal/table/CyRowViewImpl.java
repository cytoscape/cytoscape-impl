package org.cytoscape.view.model.internal.table;

import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.internal.base.CyViewBase;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.ViewLock;

public class CyRowViewImpl extends CyViewBase<CyRow> implements View<CyRow> {

	private final CyTableViewImpl parent;
	
	public CyRowViewImpl(CyTableViewImpl parent, CyRow model) {
		super(model);
		this.parent = parent;
	}

	@Override
	public View<?> getParentViewModel() {
		return parent;
	}

	@Override
	public VPStore getVPStore() {
		return parent.rowVPs;
	}

	@Override
	public ViewLock getLock() {
		return parent.rowLock;
	}

	@Override
	public CyEventHelper getEventHelper() {
		return parent.getEventHelper();
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return parent.getVisualLexicon();
	}
	
}

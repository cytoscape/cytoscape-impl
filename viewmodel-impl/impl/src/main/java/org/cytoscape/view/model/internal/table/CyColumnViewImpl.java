package org.cytoscape.view.model.internal.table;

import java.util.function.Function;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.events.TableViewChangedEvent;
import org.cytoscape.view.model.events.ViewChangeRecord;
import org.cytoscape.view.model.internal.base.CyViewBase;
import org.cytoscape.view.model.internal.base.VPStore;
import org.cytoscape.view.model.internal.base.ViewLock;
import org.cytoscape.view.model.table.CyColumnView;

public class CyColumnViewImpl extends CyViewBase<CyColumn> implements CyColumnView {

	private final CyTableViewImpl parent;
	
	public CyColumnViewImpl(CyTableViewImpl parent, CyColumn model) {
		super(model);
		this.parent = parent;
	}

	@Override
	public VPStore getVPStore() {
		return parent.columnVPs;
	}

	@Override
	public ViewLock getLock() {
		return parent.columnLock;
	}

	@Override
	public VisualLexicon getVisualLexicon() {
		return parent.getVisualLexicon();
	}

	@Override
	public <T, V extends T> void setVisualProperty(VisualProperty<? extends T> vp, Function<CyRow,T> mapping) {
		setVisualProperty(vp, (Object)mapping);
	}
	
	@SuppressWarnings("unchecked")
	public <T> Function<CyRow,T> getVisualPropertyCellFunction(VisualProperty<T> vp) {
		return (Function<CyRow,T>) getVisualProperty(vp);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void fireViewChangedEvent(VisualProperty<?> vp, Object value, boolean lockedValue) {
		var record = new ViewChangeRecord<>(this, vp, value, lockedValue);
		parent.getEventHelper().addEventPayload(parent, record, TableViewChangedEvent.class);
	}
}

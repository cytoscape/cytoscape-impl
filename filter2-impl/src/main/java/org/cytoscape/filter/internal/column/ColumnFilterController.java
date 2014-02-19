package org.cytoscape.filter.internal.column;

import org.cytoscape.filter.internal.view.RangeChooserController;

public interface ColumnFilterController {
	ColumnFilter getFilter();
	RangeChooserController getRangeChooserController();
	void synchronize(ColumnFilterView view);
}

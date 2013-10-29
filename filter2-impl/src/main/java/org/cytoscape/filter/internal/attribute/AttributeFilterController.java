package org.cytoscape.filter.internal.attribute;

import org.cytoscape.filter.internal.view.RangeChooserController;

public interface AttributeFilterController {
	AttributeFilter getFilter();
	RangeChooserController getRangeChooserController();
	void synchronize(AttributeFilterView view);
}

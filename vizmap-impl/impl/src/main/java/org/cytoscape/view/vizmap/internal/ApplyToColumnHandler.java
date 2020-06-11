package org.cytoscape.view.vizmap.internal;

import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualPropertyDependency;
import org.cytoscape.view.vizmap.VisualStyle;

public class ApplyToColumnHandler extends AbstractApplyHandler<CyColumn> {

	private VisualLexicon lexicon;
	
	public ApplyToColumnHandler(VisualStyle style, CyServiceRegistrar serviceRegistrar) {
		super(style, serviceRegistrar, CyColumn.class, BasicTableVisualLexicon.CELL);
	}
	
	@Override
	protected VisualLexicon getVisualLexicon() {
		if(lexicon == null) {
			var applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
			var tableViewRenderer = applicationManager.getDefaultTableViewRenderer();
			
			lexicon = tableViewRenderer
				.getRenderingEngineFactory(NetworkViewRenderer.DEFAULT_CONTEXT)
				.getVisualLexicon();
		}
		return lexicon;
	}
	
	private boolean isCellVP(VisualProperty<?> vp) {
		VisualLexicon lexicon = getVisualLexicon();
		var descendants = lexicon.getAllDescendants(BasicTableVisualLexicon.CELL);
		return descendants.contains(vp);
	}

	@Override
	protected void applyMappedValue(View<CyColumn> view, VisualProperty<?> vp, Object value, VisualMappingFunction<?, ?> mapping) {
		final Set<VisualPropertyDependency<?>> depSet = dependencyParents.get(vp);
		
		// If this property has already received a propagated value from a previous
		// enabled dependency, do not apply this mapping's value over it.
		if (!isParentOfDependency(vp) && !isChildOfEnabledDependency(vp)) {
			if(isCellVP(vp)) {
				((CyColumnView)view).setCellVisualProperty(vp, mapping::getMappedValue);
			} else {
				view.setVisualProperty(vp, value);
			}
		} else if (depSet != null) {
			for (final VisualPropertyDependency<?> dep : depSet) {
				// The dependency has a higher priority over children's mappings when enabled.
				if (dep.isDependencyEnabled())
					propagateValue(view, vp, value, dep.getVisualProperties(), false);
			}
		}
	}
}

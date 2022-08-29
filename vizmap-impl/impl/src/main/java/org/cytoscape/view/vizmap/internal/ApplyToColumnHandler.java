package org.cytoscape.view.vizmap.internal;

import java.util.LinkedList;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.NetworkViewRenderer;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.View;
import org.cytoscape.view.model.VisualLexicon;
import org.cytoscape.view.model.VisualLexiconNode;
import org.cytoscape.view.model.VisualProperty;
import org.cytoscape.view.model.table.CyColumnView;
import org.cytoscape.view.presentation.property.table.BasicTableVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
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

	protected void applyMappedValue(View<CyColumn> view, VisualProperty<?> vp, VisualMappingFunction<?, ?> mapping) {
		if(isCellVP(vp)) {
			var currStyle = style;
			((CyColumnView)view).setCellVisualProperty(vp, row -> {
				var val = mapping.getMappedValue(row);
				if(val == null) 
					val = currStyle.getDefaultValue(vp);
				return val;
			});
		}
		
		// MKTODO I have no idea how to handle dependencies in this context.
//		final Set<VisualPropertyDependency<?>> depSet = dependencyParents.get(vp);
//		
//		// If this property has already received a propagated value from a previous
//		// enabled dependency, do not apply this mapping's value over it.
//		if (!isParentOfDependency(vp) && !isChildOfEnabledDependency(vp)) {
//			if(isCellVP(vp)) {
//				((CyColumnView)view).setCellVisualProperty(vp, mapping::getMappedValue);
//			} else {
////				view.setVisualProperty(vp, value);
//			}
//		} else if (depSet != null) {
//			for (final VisualPropertyDependency<?> dep : depSet) {
//				// The dependency has a higher priority over children's mappings when enabled.
//				if (dep.isDependencyEnabled())
//					propagateValue(view, vp, value, dep.getVisualProperties(), false);
//			}
//		}
	}
	
	
	@Override
	public void apply(final CyRow row, final View<CyColumn> view) {
		if(view == null)
			return;
		
		if (updateDependencyMaps)
			updateDependencyMaps();
		
		view.batch(v -> {
			// Clear visual properties first
			view.clearVisualProperties();
			
			// Get current Visual Lexicon
			final VisualLexicon lexicon = getVisualLexicon();
			
			final LinkedList<VisualLexiconNode> descendants = new LinkedList<>();
			descendants.addAll(lexicon.getVisualLexiconNode(rootVisualProperty).getChildren());
			
			while (!descendants.isEmpty()) {
				final VisualLexiconNode node = descendants.pop();
				final VisualProperty<?> vp = node.getVisualProperty();
				
				if (vp.getTargetDataType() != targetDataType)
					continue; // Because NETWORK has node/edge properties as descendants as well
				
				final VisualMappingFunction<?, ?> mapping = style.getVisualMappingFunction(vp);
				
				if (mapping != null) {
					// Apply the mapped value...
					applyMappedValue(view, vp, mapping);
				} else {
					// Apply the default value...
					applyDefaultValue(view, vp, lexicon);
				}
				
				descendants.addAll(node.getChildren());
			}
		});
	}
}

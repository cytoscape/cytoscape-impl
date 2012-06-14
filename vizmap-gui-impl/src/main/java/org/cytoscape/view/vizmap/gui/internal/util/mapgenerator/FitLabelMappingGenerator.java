package org.cytoscape.view.vizmap.gui.internal.util.mapgenerator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.view.vizmap.VisualMappingFunction;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;


/**
 * Generates node width based on label size.  Only accepts SUID as the key.
 *
 */
public class FitLabelMappingGenerator<V extends Number> extends AbstractDiscreteMappingGenerator<V>{

	private final CyApplicationManager appManager;
	private final VisualMappingManager vmm;
	
	public FitLabelMappingGenerator(final Class<V> type, final CyApplicationManager appManager,
			final VisualMappingManager vmm) {
		super(type);
		this.vmm = vmm;
		this.appManager = appManager;
	}

	@Override
	public <T> Map<T, V> generateMap(final Set<T> tableValues) {
		// Generate map for the current network view.
		final CyNetworkView networkView = appManager.getCurrentNetworkView();
		if(networkView == null)
			return Collections.emptyMap();
		
		final VisualStyle style = vmm.getCurrentVisualStyle();
		// Check label size mapping exists or not
		final VisualMappingFunction<?, Integer> fontSizeMapping = style.getVisualMappingFunction(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
		// Use default label width for checking.  TODO: should we use mapping?
		final Double maxLabelWidth = style.getDefaultValue(BasicVisualLexicon.NODE_LABEL_WIDTH);
		
		final Map<T, V> valueMap = new HashMap<T, V>();
		
		for(final T attrVal: tableValues) {
			Long suid = null;
			try {
				suid = Long.class.cast(attrVal);
			} catch(Exception e) {
				throw new IllegalArgumentException("This generator only works with Long (SUID).", e);
			}
			final CyNode node = networkView.getModel().getNode(suid);
			if(node == null)
				continue;
			
			final View<CyNode> nodeView = networkView.getNodeView(node);
			if(nodeView == null)
				continue;
			
			final String labelText = nodeView.getVisualProperty(BasicVisualLexicon.NODE_LABEL);
			final int textLen = labelText.length();
			final int fontSize;
			if(fontSizeMapping == null)
				fontSize = style.getDefaultValue(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
			else
				fontSize = nodeView.getVisualProperty(BasicVisualLexicon.NODE_LABEL_FONT_SIZE);
			
			final Double width = fontSize*textLen*0.7;
			if(maxLabelWidth>width)
				valueMap.put(attrVal, (V) width);
			else {
				valueMap.put(attrVal, (V) maxLabelWidth);
			}
		}
		
		return valueMap;
	}
}

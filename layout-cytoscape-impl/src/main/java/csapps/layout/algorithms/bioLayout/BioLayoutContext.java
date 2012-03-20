package csapps.layout.algorithms.bioLayout;

import java.util.Set;

import org.cytoscape.view.layout.AbstractLayoutAlgorithmContext;
import org.cytoscape.work.Tunable;

public abstract class BioLayoutContext extends AbstractLayoutAlgorithmContext {
	public BioLayoutContext(boolean supportsSelectedOnly, Set<Class<?>> supportedNodeAttributes, Set<Class<?>> supportedEdgeAttributes) {
		super(supportsSelectedOnly, supportedNodeAttributes, supportedEdgeAttributes);
	}

	/**
	 * Whether or not to initialize by randomizing all points
	 */
	@Tunable(description="Randomize graph before layout", groups="Standard settings")
	public boolean randomize = true;
}

package csapps.layout.algorithms.bioLayout;

import org.cytoscape.view.layout.AbstractLayoutContext;
import org.cytoscape.work.Tunable;

public abstract class BioLayoutContext extends AbstractLayoutContext {
	/**
	 * Whether or not to initialize by randomizing all points
	 */
	@Tunable(description="Randomize graph before layout", groups="Standard settings")
	public boolean randomize = true;
}

package csapps.layout.algorithms.bioLayout;

import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.undo.UndoSupport;

public abstract class BioLayoutAlgorithm extends AbstractLayoutAlgorithm {
	
	public BioLayoutAlgorithm(UndoSupport undo, String computerName,
			String humanName, boolean supportsSelectedOnly) {
		super(undo, computerName, humanName, supportsSelectedOnly);
	}

	/**
	 * Whether or not to initialize by randomizing all points
	 */
	@Tunable(description="Randomize graph before layout", groups="Standard settings")
	public boolean randomize = true;
}

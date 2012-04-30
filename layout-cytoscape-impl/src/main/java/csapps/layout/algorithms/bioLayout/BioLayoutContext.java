package csapps.layout.algorithms.bioLayout;

import org.cytoscape.view.layout.EdgeWeighter;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.ContainsTunables;

public abstract class BioLayoutContext {
	
	@ContainsTunables
	public EdgeWeighter edgeWeighter = new EdgeWeighter();
	
	/**
	 * Whether or not to initialize by randomizing all points
	 */
	@Tunable(description="Randomize graph before layout", groups="Standard settings")
	public boolean randomize = true;
}

package csapps.layout;


import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.layout.AbstractLayoutContext;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.jgraph.plugins.layouts.AnnealingLayoutAlgorithm;
import org.jgraph.plugins.layouts.CircleGraphLayout;
import org.jgraph.plugins.layouts.GEMLayoutAlgorithm;
import org.jgraph.plugins.layouts.JGraphLayoutAlgorithm;
import org.jgraph.plugins.layouts.JGraphLayoutSettings;
import org.jgraph.plugins.layouts.MoenLayoutAlgorithm;
import org.jgraph.plugins.layouts.RadialTreeLayoutAlgorithm;
import org.jgraph.plugins.layouts.SpringEmbeddedLayoutAlgorithm;
import org.jgraph.plugins.layouts.SugiyamaLayoutAlgorithm;
import org.jgraph.plugins.layouts.TreeLayoutAlgorithm;


public class JGraphLayoutWrapper extends AbstractLayoutAlgorithm<AbstractLayoutContext> {
	public static final int ANNEALING = 0;
	public static final int MOEN = 1;
	public static final int CIRCLE_GRAPH = 2;
	public static final int RADIAL_TREE = 3;
	public static final int GEM = 4;
	public static final int SPRING_EMBEDDED = 5;
	public static final int SUGIYAMA = 6;
	public static final int TREE = 7;

	private int layout_type = 0;
	private JGraphLayoutSettings layoutSettings = null;
	private JGraphLayoutAlgorithm layout = null;
	
	/**
	 * Creates a new GridNodeLayout object.
	 */
	public JGraphLayoutWrapper(int layout_type) {
		// names here will be overridden by provided methods
		super("jgraph", "jgraph");
		
		this.layout_type = layout_type;

		switch (layout_type) {
		case ANNEALING:
			layout = new AnnealingLayoutAlgorithm();

			break;

		case MOEN:
			layout = new MoenLayoutAlgorithm();

			break;

		case CIRCLE_GRAPH:
			layout = new CircleGraphLayout();

			break;

		case RADIAL_TREE:
			layout = new RadialTreeLayoutAlgorithm();

			break;

		case GEM:
			layout = new GEMLayoutAlgorithm(new AnnealingLayoutAlgorithm());

			break;

		case SPRING_EMBEDDED:
			layout = new SpringEmbeddedLayoutAlgorithm();

			break;

		case SUGIYAMA:
			layout = new SugiyamaLayoutAlgorithm();

			break;

		case TREE:
			layout = new TreeLayoutAlgorithm();

			break;
		}

		layoutSettings = layout.createSettings();
	}

	public TaskIterator createTaskIterator(CyNetworkView networkView, AbstractLayoutContext context, Set<View<CyNode>> nodesToLayOut) {
		return new TaskIterator(new JGraphLayoutWrapperTask(getName(), networkView, nodesToLayOut, getSupportedNodeAttributeTypes(), getSupportedEdgeAttributeTypes(), getInitialAttributeList(), context, layout, layoutSettings));
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String getName() {
		switch (layout_type) {
			case ANNEALING:
				return "jgraph-annealing";

			case MOEN:
				return "jgraph-moen";

			case CIRCLE_GRAPH:
				return "jgraph-circle";

			case RADIAL_TREE:
				return "jgraph-radial-tree";

			case GEM:
				return "jgraph-gem";

			case SPRING_EMBEDDED:
				return "jgraph-spring";

			case SUGIYAMA:
				return "jgraph-sugiyama";

			case TREE:
				return "jgraph-tree";
		}

		return "";
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String toString() {
		switch (layout_type) {
			case ANNEALING:
				return "Simulated Annealing Layout";

			case MOEN:
				return "MOEN Layout";

			case CIRCLE_GRAPH:
				return "Circle Layout";

			case RADIAL_TREE:
				return "Radial Tree Layout";

			case GEM:
				return "GEM Layout";

			case SPRING_EMBEDDED:
				return "Spring Embedded Layout";

			case SUGIYAMA:
				return "Sugiyama Layout";

			case TREE:
				return "Tree Layout";
		}

		return "";
	}
	
	@Override
	public AbstractLayoutContext createLayoutContext() {
		return new AbstractLayoutContext();
	}
}

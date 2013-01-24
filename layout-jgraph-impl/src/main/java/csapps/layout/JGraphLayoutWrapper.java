package csapps.layout;

/*
 * #%L
 * Cytoscape JGraph Layout Impl (layout-jgraph-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013 The Cytoscape Consortium
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */



import java.util.Set;

import org.cytoscape.model.CyNode;
import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.undo.UndoSupport;
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


public class JGraphLayoutWrapper extends AbstractLayoutAlgorithm {
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
	public JGraphLayoutWrapper(int layout_type, UndoSupport undo) {
		// names here will be overridden by provided methods
		super("jgraph", "jgraph", undo);
		
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

	public TaskIterator createTaskIterator(CyNetworkView networkView, Object context, Set<View<CyNode>> nodesToLayOut, String attrName) {
		return new TaskIterator(new JGraphLayoutWrapperTask(toString(), networkView, nodesToLayOut, context, layout, layoutSettings, undoSupport));
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
	
}

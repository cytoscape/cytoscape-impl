/*
 * This is based on the ISOMLayout from the JUNG project.
 */
package csplugins.layout.algorithms.graphPartition;


import java.util.Iterator;
//import java.util.List;

import org.cytoscape.view.layout.AbstractLayoutAlgorithm;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.TunableValidator.ValidationState;
import org.cytoscape.work.undo.UndoSupport;


public class ISOMLayout extends AbstractLayoutAlgorithm implements TunableValidator {
	@Tunable(description="Number of iterations")
	public int maxEpoch = 5000;
	@Tunable(description="Radius constant")
	public int radiusConstantTime = 100;
	@Tunable(description="Radius")
	public int radius = 20;
	@Tunable(description="Minimum radius")
	public int minRadius = 1;
	@Tunable(description="Initial adaptation")
	public double initialAdaptation = 90.0D / 100.0D;
	@Tunable(description="Minimum adaptation value")
	public double minAdaptation = 0;
	@Tunable(description="Size factor")
	public double sizeFactor = 100;
	@Tunable(description="Cooling factor")
	public double coolingFactor = 2;
	@Tunable(description="Don't partition graph before layout", groups="Standard settings")
	public boolean singlePartition;

	/**
	 * Creates a new ISOMLayout object.
	 */
	public ISOMLayout(UndoSupport undoSupport) {
		super(undoSupport,"isom", "Inverted Self-Organizing Map Layout", true);
	}

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
	
	public TaskIterator getTaskIterator() {
		if (selectedOnly)
			initStaticNodes();

		return new TaskIterator(
			new ISOMLayoutTask(networkView, getName(), selectedOnly, staticNodes,
					   maxEpoch, radiusConstantTime, radius, minRadius,
					   initialAdaptation, minAdaptation, sizeFactor,
					   coolingFactor, singlePartition));
	}
}

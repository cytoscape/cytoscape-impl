package csapps.layout.algorithms.graphPartition;

import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;

public class ISOMLayoutContext implements TunableValidator {
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

	@Override // TODO
	public ValidationState getValidationState(final Appendable errMsg) {
		return ValidationState.OK;
	}
	
}

package csapps.layout.algorithms;

import org.cytoscape.view.layout.AbstractLayoutContext;
import org.cytoscape.work.Tunable;

public class GroupAttributesLayoutContext extends AbstractLayoutContext {
	/*
	  Layout parameters:
	    - spacingx: Horizontal spacing (on the x-axis) between two partitions in a row.
	    - spacingy: Vertical spacing (on the y-axis) between the largest partitions of two rows.
	    - maxwidth: Maximum width of a row
	    - minrad:   Minimum radius of a partition.
	    - radmult:  The scale of the radius of the partition. Increasing this value
	                will increase the size of the partition proportionally.
	 */
	@Tunable(description="Horizontal spacing between two partitions in a row")
	public double spacingx = 400.0;
	@Tunable(description="Vertical spacing between the largest partitions of two rows")
	public double spacingy = 400.0;
	@Tunable(description="Maximum width of a row")
	public double maxwidth = 5000.0;
	@Tunable(description="Minimum width of a partition")
	public double minrad = 100.0;
	@Tunable(description="Scale of the radius of the partition")
	public double radmult = 50.0;
}

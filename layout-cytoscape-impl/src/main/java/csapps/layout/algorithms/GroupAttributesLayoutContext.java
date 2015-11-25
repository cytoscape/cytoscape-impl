package csapps.layout.algorithms;

/*
 * #%L
 * Cytoscape Layout Algorithms Impl (layout-cytoscape-impl)
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

import org.cytoscape.work.Tunable;

public class GroupAttributesLayoutContext  {
	/*
	  Layout parameters:
	    - spacingx: Horizontal spacing (on the x-axis) between two partitions in a row.
	    - spacingy: Vertical spacing (on the y-axis) between the largest partitions of two rows.
	    - maxwidth: Maximum width of a row
	    - minrad:   Minimum radius of a partition.
	    - radmult:  The scale of the radius of the partition. Increasing this value
	                will increase the size of the partition proportionally.
	 */
	@Tunable(description="Horizontal spacing between two partitions in a row:")
	public double spacingx = 400.0;
	@Tunable(description="Vertical spacing between the largest partitions of two rows:")
	public double spacingy = 400.0;
	@Tunable(description="Maximum width of a row:")
	public double maxwidth = 5000.0;
	@Tunable(description="Minimum width of a partition:")
	public double minrad = 100.0;
	@Tunable(description="Scale of the radius of the partition:")
	public double radmult = 50.0;
}

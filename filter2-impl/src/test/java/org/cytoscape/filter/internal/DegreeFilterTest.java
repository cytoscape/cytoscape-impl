package org.cytoscape.filter.internal;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.cytoscape.filter.internal.filters.degree.DegreeFilter;
import org.cytoscape.filter.model.ValidationWarning;
import org.cytoscape.model.CyEdge;
import org.junit.Test;

public class DegreeFilterTest {

	@Test
	public void testInvalidEdgeType() {
		DegreeFilter degreeFilter = new DegreeFilter();
		degreeFilter.setEdgeType(CyEdge.Type.DIRECTED);
		List<ValidationWarning> warnings = degreeFilter.validateCreation();
		
		assertEquals(warnings.size(), 1);
		
		degreeFilter.setEdgeType(CyEdge.Type.UNDIRECTED);
		warnings = degreeFilter.validateCreation();
		assertEquals(warnings.size(), 1);
	}
	
	@Test
	public void testValidEdgeType() {
		DegreeFilter degreeFilter = new DegreeFilter();
		degreeFilter.setEdgeType(CyEdge.Type.ANY);
		List<ValidationWarning> warnings = degreeFilter.validateCreation();
		assertEquals(warnings.size(), 0);
		
		degreeFilter.setEdgeType(CyEdge.Type.INCOMING);
		warnings = degreeFilter.validateCreation();
		assertEquals(warnings.size(), 0);
		
		degreeFilter.setEdgeType(CyEdge.Type.OUTGOING);
		warnings = degreeFilter.validateCreation();
		assertEquals(warnings.size(), 0);
	}
	
}

package org.cytoscape.filter.internal.range;

public interface RangeListener<N> {
	
	public void rangeChanged(N low, N high);
	
}
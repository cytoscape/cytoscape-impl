package org.cytoscape.ding;


public interface Handle {
	
	double getXFraction();
	double getYFraction();

//	void setXFraction(final double x);
//	void setYFraction(final double y);
	
	void setPoint(double x, double y);
}

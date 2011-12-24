package org.cytoscape.ding.impl;

import org.cytoscape.ding.Handle;


/**
 * A simple implementation of edge handle.
 *
 */
public class HandleImpl implements Handle {
	
	private static final double MIN = 0d;
	private static final double MAX = 1d;
	
	private double x;
	private double y;
	
	public HandleImpl(final double x, final double y) {
		if(validate(x) && validate(y)) {
			this.x = x;
			this.y = y;
		} else {
			throw new IllegalArgumentException("Given value is out-of-range.");
		}
	}

	private boolean validate(final double value) {
		if(value>=MIN && value <= MAX)
			return true;
		else
			return false;
	}
	
	@Override
	public double getXFraction() {
		return x;
	}

	@Override
	public double getYFraction() {
		return y;
	}

	@Override
	public void setXFraction(double x) {
		if(validate(x))
			this.x = x;
		else
			throw new IllegalArgumentException("Given value is out-of-range.");
	}

	@Override
	public void setYFraction(double y) {
		if(validate(y))
			this.y = y;
		else
			throw new IllegalArgumentException("Given value is out-of-range.");
	}

}

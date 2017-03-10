package org.ivis.layout.util;

import java.awt.Point;

/**
 * This class defines a polyomino, which includes many small fragments.
 */

public final class Polyomino{
	// the number of cells
	public int l;

	//the resulting placement coordinates
	public int x,y;
	
	public String label;

	// polyomino cells
	public Point []coord;
}

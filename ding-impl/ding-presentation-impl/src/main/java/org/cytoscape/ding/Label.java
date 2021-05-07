package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2021 The Cytoscape Consortium
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

import java.awt.Font;
import java.awt.Paint;

public interface Label {

  public static int NORTHWEST = 0;
  public static int NORTH = 1;
  public static int NORTHEAST = 2;

  public static int WEST = 3;
  public static int CENTER = 4;
  public static int EAST = 5;

  public static int SOUTHWEST = 6;
  public static int SOUTH = 7;
  public static int SOUTHEAST = 8;

  public static int SOURCE_BOUND = 9;
  public static int TARGET_BOUND = 10;

  public static int JUSTIFY_CENTER = 64;
  public static int JUSTIFY_LEFT = 65;
  public static int JUSTIFY_RIGHT = 66;
 
  public static int NONE = 127;

  /**
   * Give the Label a hint on where to draw itself.
   * <B>NOTE:</B> This should be thought of as a hint only, not 
   * all labels will support all positions
   */

 	/**
	 * Get the paint used to paint this nodes text.
	 * @return Paint
	 */
	Paint getTextPaint();

	/**
	 * Set the paint used to paint this nodes text.
	 * @param textPaint
	 */		
	void setTextPaint(Paint textPaint) ;
	
		
	String getText() ;

	/**
	 * Set the text for this node. The text will be broken up into multiple
	 * lines based on the size of the text and the bounds width of this node.
	 */
	void setText(String aText) ;
	
	/**
	 * Returns the font of this PText.
	 * @return the font of this PText.
	 */ 
	Font getFont() ;
	
	/**
	 * Set the font of this PText. Note that in Piccolo if you want to change
	 * the size of a text object it's often a better idea to scale the PText
	 * node instead of changing the font size to get that same effect. Using
	 * very large font sizes can slow performance.
	 */
	void setFont(Font aFont) ;
	
}

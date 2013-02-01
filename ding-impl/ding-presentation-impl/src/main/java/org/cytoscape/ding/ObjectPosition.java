package org.cytoscape.ding;

/*
 * #%L
 * Cytoscape Ding View/Presentation Impl (ding-presentation-impl)
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

/**
 * Interface representing relative location of graphics objects,
 * such as labels or custom graphics.
 * 
 *
 */
public interface ObjectPosition {
	
	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Position getAnchor();

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Position getTargetAnchor();

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public Justification getJustify();

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getOffsetX();

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getOffsetY();

	/**
	 *  DOCUMENT ME!
	 *
	 * @param b DOCUMENT ME!
	 */
	public void setAnchor(Position position);

	/**
	 *  DOCUMENT ME!
	 *
	 * @param b DOCUMENT ME!
	 */
	public void setTargetAnchor(Position position);

	/**
	 *  DOCUMENT ME!
	 *
	 * @param b DOCUMENT ME!
	 */
	public void setJustify(Justification position);

	/**
	 *  DOCUMENT ME!
	 *
	 * @param d DOCUMENT ME!
	 */
	public void setOffsetX(double d);

	/**
	 *  DOCUMENT ME!
	 *
	 * @param d DOCUMENT ME!
	 */
	public void setOffsetY(double d);

	
	/**
	 * Create a short String to be used by reader/writer.
	 * @return serialized string.
	 */
	String toSerializableString();
}
package org.cytoscape.ding.impl;

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

import static org.cytoscape.ding.Justification.JUSTIFY_CENTER;
import static org.cytoscape.ding.Position.CENTER;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cytoscape.ding.Justification;
import org.cytoscape.ding.ObjectPosition;
import org.cytoscape.ding.Position;


/**
* An implementation of {@link ObjectPosition}.
*/
public class ObjectPositionImpl implements ObjectPosition {
	
	private static final Pattern p = Pattern
			.compile("^([NSEWC]{1,2}+),([NSEWC]{1,2}+),([clr]{1}+),(-?\\d+(.\\d+)?),(-?\\d+(.\\d+)?)$");
	
	public static final ObjectPosition DEFAULT_POSITION = new ObjectPositionImpl();
	
	private Position objectAnchor;
	private Position targetAnchor;
	private Justification justify;

	private double xOffset;
	private double yOffset;

	/**
	 * Creates a new ObjectPosition object.
	 */
	public ObjectPositionImpl() {
		this(CENTER, CENTER, JUSTIFY_CENTER, 0.0, 0.0);
	}

	/**
	 * Copy constructor
	 * 
	 * @param p original position.
	 */
	public ObjectPositionImpl(final ObjectPosition p) {
		targetAnchor = p.getTargetAnchor();
		objectAnchor = p.getAnchor();
		xOffset = p.getOffsetX();
		yOffset = p.getOffsetY();
		justify = p.getJustify();
	}

	/**
	 * Creates a new ObjectPosition object.
	 * 
	 * @param targ
	 *            DOCUMENT ME!
	 * @param lab
	 *            DOCUMENT ME!
	 * @param just
	 *            DOCUMENT ME!
	 * @param x
	 *            DOCUMENT ME!
	 * @param y
	 *            DOCUMENT ME!
	 */
	public ObjectPositionImpl(final Position targ, final Position lab,
			final Justification just, final double x, final double y) {
		targetAnchor = targ;
		objectAnchor = lab;
		justify = just;
		xOffset = x;
		yOffset = y;
	}


	@Override
	public Position getAnchor() {
		return objectAnchor;
	}

	@Override
	public Position getTargetAnchor() {
		return targetAnchor;
	}

	@Override
	public Justification getJustify() {
		return justify;
	}

	@Override
	public double getOffsetX() {
		return xOffset;
	}

	@Override
	public double getOffsetY() {
		return yOffset;
	}

	@Override
	public void setAnchor(Position p) {
		objectAnchor = p;
	}

	@Override
	public void setTargetAnchor(Position p) {
		targetAnchor = p;
	}

	@Override
	public void setJustify(Justification j) {
		justify = j;
	}

	@Override
	public void setOffsetX(double d) {
		xOffset = d;
	}

	@Override
	public void setOffsetY(double d) {
		yOffset = d;
	}

	@Override
	public boolean equals(Object lp) {
		// Accepts non-null ObjectPosition only.
		if (lp == null || lp instanceof ObjectPosition == false)
			return false;

		final ObjectPosition p = (ObjectPosition) lp;

		if (Math.abs(p.getOffsetX() - xOffset) > 0.0000001)
			return false;
	
		if (Math.abs(p.getOffsetY() - yOffset) > 0.0000001)
			return false;
		
		if (p.getAnchor() != objectAnchor)
			return false;

		if (p.getTargetAnchor() != targetAnchor)
			return false;
		
		if (p.getJustify() != justify)
			return false;

		return true;
	}


	@Override public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append("target: ").append(targetAnchor.getName());
		sb.append("  object: ").append(objectAnchor.getName());
		sb.append("  justify: ").append(justify.getName());
		sb.append("  X offset: ").append(Double.toString(xOffset));
		sb.append("  Y offset: ").append(Double.toString(yOffset));

		return sb.toString();
	}

	
	private String shortString() {
		// force the locale to US so that we consistently serialize
		final DecimalFormat df = new DecimalFormat("#0.00;-#0.00", new DecimalFormatSymbols(Locale.US));

		final StringBuilder sb = new StringBuilder();
		sb.append(targetAnchor.getShortName());
		sb.append(",");
		sb.append(objectAnchor.getShortName());
		sb.append(",");
		sb.append(justify.getShortName());
		sb.append(",");
		sb.append(df.format(xOffset));
		sb.append(",");
		sb.append(df.format(yOffset));

		return sb.toString();
	}
	
	
	@Override
	public String toSerializableString() {
		return shortString();
	}
	
	
	/**
	 * 
	 * @param serializableString
	 * @return Never returns null.  If invalid, simply returns default.
	 */
	public static ObjectPosition parse(final String serializableString) {
		
		final Matcher m = p.matcher(serializableString);
		if (m.matches()) {
			final ObjectPosition lp = new ObjectPositionImpl();
			lp.setTargetAnchor(Position.parse(m.group(1)));
			lp.setAnchor(Position.parse(m.group(2)));
			lp.setJustify(Justification.parse(m.group(3)));
			lp.setOffsetX(Double.valueOf(m.group(4)));
			lp.setOffsetY(Double.valueOf(m.group(6)));
			return lp;
		}

		return DEFAULT_POSITION;
	}
}

package org.cytoscape.io.internal.read.expression;

/*
 * #%L
 * Cytoscape IO Impl (io-impl)
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

//mRNAMeasurement:  encapsulate the ratio/signficance pair

/**
 *
 */
public class mRNAMeasurement implements java.io.Serializable {
	private final static long serialVersionUID = 1202339866840143L;
	private double expressionRatio;
	private double significance;

	/**
	 * Creates a new mRNAMeasurement object.
	 *
	 * @param ratioString  DOCUMENT ME!
	 * @param significanceString  DOCUMENT ME!
	 */
	public mRNAMeasurement(String ratioString, String significanceString) {
		expressionRatio = -99999.9;

		try {
			expressionRatio = Double.parseDouble(ratioString);
		} catch (NumberFormatException ignore) {
			;
		}

		significance = -99999.9;

		try {
			significance = Double.parseDouble(significanceString);
		} catch (NumberFormatException ignore) {
			;
		}
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getRatio() {
		return expressionRatio;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public double getSignificance() {
		return significance;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(expressionRatio);
		sb.append(",  ");
		sb.append(significance);

		return sb.toString();
	} // toString

	/**
	 *  DOCUMENT ME!
	 *
	 * @param new_ratio DOCUMENT ME!
	 */
	public void setRatio(double new_ratio) {
		this.expressionRatio = new_ratio;
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param new_significance DOCUMENT ME!
	 */
	public void setSignificance(double new_significance) {
		this.significance = new_significance;
	}
} // mRNAMeasurement

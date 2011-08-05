/*
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)

  The Cytoscape Consortium is:
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Institut Pasteur
  - Agilent Technologies

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.psi_mi.internal.model;


/**
 * Encapsulates a Single External Reference.
 *
 * @author Ethan Cerami
 */
public class ExternalReference implements Comparable<ExternalReference> {
	/**
	 * Database Name.
	 */
	private String database;

	/**
	 * Database ID.
	 */
	private String id;

	/**
	 * Constructor.
	 *
	 * @param db Database name.
	 * @param id Database id.
	 */
	public ExternalReference(String db, String id) {
		this.database = db;
		this.id = id;
	}

	/**
	 * Gets the Database Name.
	 *
	 * @return Database name.
	 */
	public String getDatabase() {
		return database;
	}

	/**
	 * Sets the Database Name.
	 *
	 * @param db Database name.
	 */
	public void setDatabase(String db) {
		this.database = db;
	}

	/**
	 * Gets the Database ID.
	 *
	 * @return Database ID.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Overrides default equals() method.
	 * Two external references are considered equal if they share the same
	 * database and id.
	 *
	 * @param obj Object to test.
	 * @return true or false.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof ExternalReference) {
			ExternalReference other = (ExternalReference) obj;

			return (database.equals(other.getDatabase()) && id.equals(other.getId()));
		} else {
			return super.equals(obj);
		}
	}

	/**
	 * Gets Hash Code.
	 *
	 * @return Integer Hash Code.
	 */
	public int hashCode() {
		String str = new String(database + ":" + id);

		return str.hashCode();
	}

	/**
	 * Overrides default toString() method.
	 *
	 * @return String Representation.
	 */
	public String toString() {
		return new String("External Reference  -->  Database:  [" + database + "], ID:  [" + id
		                  + "]");
	}

	/**
	 * Comparison Operator.
	 *
	 * @param other Other Object.
	 * @return integer value.
	 */
	public int compareTo(ExternalReference other) {
		String otherDbName = other.getDatabase();

		if (!otherDbName.equals(database)) {
			return otherDbName.compareToIgnoreCase(database);
		} else {
			String otherId = other.getId();

			return otherId.compareToIgnoreCase(id);
		}
	}
}

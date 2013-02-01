package org.cytoscape.psi_mi.internal.model;

/*
 * #%L
 * Cytoscape PSI-MI Impl (psi-mi-impl)
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

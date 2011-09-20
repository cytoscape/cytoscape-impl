// $Id: ExternalLink.java,v 1.4 2006/06/15 22:06:02 grossb Exp $
//------------------------------------------------------------------------------
/** Copyright (c) 2006 Memorial Sloan-Kettering Cancer Center.
 **
 ** Code written by: Ethan Cerami
 ** Authors: Ethan Cerami, Gary Bader, Chris Sander
 **
 ** This library is free software; you can redistribute it and/or modify it
 ** under the terms of the GNU Lesser General Public License as published
 ** by the Free Software Foundation; either version 2.1 of the License, or
 ** any later version.
 **
 ** This library is distributed in the hope that it will be useful, but
 ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
 ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
 ** documentation provided hereunder is on an "as is" basis, and
 ** Memorial Sloan-Kettering Cancer Center
 ** has no obligations to provide maintenance, support,
 ** updates, enhancements or modifications.  In no event shall
 ** Memorial Sloan-Kettering Cancer Center
 ** be liable to any party for direct, indirect, special,
 ** incidental or consequential damages, including lost profits, arising
 ** out of the use of this software and its documentation, even if
 ** Memorial Sloan-Kettering Cancer Center
 ** has been advised of the possibility of such damage.  See
 ** the GNU Lesser General Public License for more details.
 **
 ** You should have received a copy of the GNU Lesser General Public License
 ** along with this library; if not, write to the Free Software Foundation,
 ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 **/
package org.cytoscape.biopax.internal;


/**
 * Model Object for Encapsulating an External Link.
 *
 * @author Ethan Cerami
 */
@Deprecated
public class ExternalLink {
	private String dbName;
	private String id;
	private String relType;
	private String title;
	private String year;
	private String author;
	private String url;
	private String source;
	
	/**
	 * Constructor.
	 *
	 * @param dbName Database name.
	 * @param id     Unique ID.
	 */
	public ExternalLink(String dbName, String id) {
		this.dbName = dbName;
		this.id = id;
	}

	/**
	 * Gets the Database Name.
	 *
	 * @return Database Name.
	 */
	public String getDbName() {
		return dbName;
	}

	/**
	 * Sets the Database Name.
	 *
	 * @param dbName Database Name.
	 */
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	/**
	 * Gets the Unique ID.
	 *
	 * @return Unique ID.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the Unique ID.
	 *
	 * @param id Unique ID.
	 */
	public void setId(String id) {
		this.id = id;
	}

	public String getRelType() {
		return relType;
	}

	public void setRelType(String relType) {
		this.relType = relType;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
	
	
}

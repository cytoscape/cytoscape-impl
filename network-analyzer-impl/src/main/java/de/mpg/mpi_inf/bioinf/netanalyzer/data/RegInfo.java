package de.mpg.mpi_inf.bioinf.netanalyzer.data;

/*
 * #%L
 * Cytoscape NetworkAnalyzer Impl (network-analyzer-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2013
 *   Max Planck Institute for Informatics, Saarbruecken, Germany
 *   The Cytoscape Consortium
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
 * Class storing and managing the NetworkAnalyzer registration information.
 * 
 * @author Yassen Assenov
 * @author Nadezhda Doncheva
 */
public class RegInfo {

	/**
	 * Initialize a new instance of <code>RegInfo</code> with all user's data except registration code.
	 * 
	 * @param aName
	 *            User's name.
	 * @param aSurname
	 *            User's surname.
	 * @param aInstitute
	 *            User's institution.
	 * @param aEmail
	 *            User's email.
	 */
	public RegInfo(String aName, String aSurname, String aInstitute, String aEmail) {
		name = aName;
		surname = aSurname;
		institute = aInstitute;
		email = aEmail;
	}

	/**
	 * Get user's name.
	 * 
	 * @return User's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set new user's name.
	 * 
	 * @param aName
	 *            User's name.
	 */
	public void setName(String aName) {
		name = aName;
	}

	/**
	 * Get user's surname.
	 * 
	 * @return User's surname.
	 */
	public String getSurname() {
		return surname;
	}

	/**
	 * Set new user's surname.
	 * 
	 * @param aSurname
	 *            User's surname.
	 */
	public void setSurname(String aSurname) {
		surname = aSurname;
	}

	/**
	 * Get user's institute.
	 * 
	 * @return User's institute.
	 */
	public String getInstitute() {
		return institute;
	}

	/**
	 * Set new user's institute.
	 * 
	 * @param aInstitute
	 *            New user's institute.
	 */
	public void setInstitute(String aInstitute) {
		institute = aInstitute;
	}

	/**
	 * Get user's email address.
	 * 
	 * @return User's email address.
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Set new user's email address.
	 * 
	 * @param aEmail
	 *            New user's email address.
	 */
	public void setEmail(String aEmail) {
		email = aEmail;
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append(name);
		buffer.append("\t");
		buffer.append(surname);
		buffer.append("\t");
		buffer.append(institute);
		buffer.append("\t");
		buffer.append(email);
		buffer.append("\t");
		return buffer.toString();
	}

	/**
	 * User's name.
	 */
	private String name;

	/**
	 * User's surname.
	 */
	private String surname;

	/**
	 * User's institute.
	 */
	private String institute;

	/**
	 * User's email address.
	 */
	private String email;
}

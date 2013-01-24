package org.cytoscape.psi_mi.internal.data_mapper;

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
 * Encapsulates a Mapping Exception.
 *
 * @author Ethan Cerami
 */
@SuppressWarnings("serial")
public class MapperException extends Exception {
	private String humanReadableErrorMessage;

	/**
	 * Constructor.
	 *
	 * @param rootCause                 Root Cause.
	 * @param humanReadableErrorMessage HumanReadableErrorMessage
	 */
	public MapperException(Throwable rootCause, String humanReadableErrorMessage) {
		super(rootCause);
		this.humanReadableErrorMessage = humanReadableErrorMessage;
	}

	/**
	 * Constructor.
	 *
	 * @param humanReadableErrorMessage HumanReadableErrorMessage
	 */
	public MapperException(String humanReadableErrorMessage) {
		this.humanReadableErrorMessage = humanReadableErrorMessage;
	}

	/**
	 * Gets Human Readable Error Message.
	 *
	 * @return msg Message
	 */
	public String getHumanReadableErrorMessage() {
		return this.humanReadableErrorMessage;
	}
}

package org.cytoscape.psi_mi.internal.util;

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
 * Indicates communication error with the data service.
 *
 * @author Ethan Cerami
 */
@SuppressWarnings("serial")
public class DataServiceException extends Exception {
	private String humanReadableErrorMessage;

	/**
	 * Constructor.
	 *
	 * @param rootCause Root Cause.
	 * @param humanReadableErrorMessage Human Readable Error Message.
	 */
	public DataServiceException(Throwable rootCause, String humanReadableErrorMessage) {
		super(rootCause);
		this.humanReadableErrorMessage = humanReadableErrorMessage;
	}

	/**
	 * Constructor.
	 *
	 * @param humanReadableErrorMessage Message.
	 */
	public DataServiceException(String humanReadableErrorMessage) {
		this.humanReadableErrorMessage = humanReadableErrorMessage;
	}

	/**
	 * Gets Exception Message.
	 *
	 * @return Error Message.
	 */
	public String getMessage() {
		return this.humanReadableErrorMessage;
	}
}

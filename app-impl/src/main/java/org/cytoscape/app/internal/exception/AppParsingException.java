package org.cytoscape.app.internal.exception;

/*
 * #%L
 * Cytoscape App Impl (app-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2008 - 2013 The Cytoscape Consortium
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

import java.io.File;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.manager.AppParser;

/**
 * An exception thrown by the {@link AppParser} when it encounters errors while attempting
 * to parse a given {@link File} object as an {@link App} object.
 */
public class AppParsingException extends Exception {

	/** Long serial version identifier required by the Serializable class */
	private static final long serialVersionUID = 7578373418714543699L;
	
	public AppParsingException(String message) {
		super(message);
	}
	
	public AppParsingException(String message, Throwable cause) {
		super(message, cause);
	}
}

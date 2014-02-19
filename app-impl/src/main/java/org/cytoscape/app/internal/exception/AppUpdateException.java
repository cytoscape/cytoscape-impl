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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An exception thrown to signal errors found while attempting to perform 
 * an update on an app
 */
public class AppUpdateException extends Exception {

	private static final Logger logger = LoggerFactory.getLogger(AppUpdateException.class);
	
	private static final long serialVersionUID = 4741554087496424850L;

	public AppUpdateException(String message) {
		super(message);
		
		logger.info(message);
	}
	
	public AppUpdateException(String message, Throwable cause) {
		super(message, cause);
	}
}

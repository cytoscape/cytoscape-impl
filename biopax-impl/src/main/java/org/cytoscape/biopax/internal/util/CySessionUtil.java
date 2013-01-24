package org.cytoscape.biopax.internal.util;

/*
 * #%L
 * Cytoscape BioPAX Impl (biopax-impl)
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
 * Indicates if Cytoscape Session is in Progress.
 */
public class CySessionUtil {
    private static boolean sessionReadingInProgress;

    /**
     * Is Session Reading in Progress?
     * @return true or false.
     */
    public static boolean isSessionReadingInProgress() {
        return sessionReadingInProgress;
    }

    /**
     * Sets if Session Reading is in Progress.
     * @param inProgress true or false.
     */
    public static void setSessionReadingInProgress(boolean inProgress) {
        sessionReadingInProgress = inProgress;
    }
}
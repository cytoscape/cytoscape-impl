package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

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
 * Storage class for file types that are supported by the plugin for reading and/or writing data.
 * <p>
 * This class contains all instances of
 * {@link de.mpg.mpi_inf.bioinf.netanalyzer.ui.ExtensionFileFilter} created within this plugin.
 * </p>
 * 
 * @author Yassen Assenov
 */
public final class SupportedExtensions {

	/**
	 * Extension filter for JPEG images.
	 */
	public static ExtensionFileFilter jpegFilesFilter = new ExtensionFileFilter(".jpeg", ".jpg",
			"Jpeg images (.jpeg, .jpg)");

	/**
	 * Extension filter for PNG images.
	 */
	public static ExtensionFileFilter pngFilesFilter = new ExtensionFileFilter(".png",
			"Portable Network Graphic images (.png)");

	/**
	 * Extension filter for SVG images.
	 */
	public static ExtensionFileFilter svgFilesFilter = new ExtensionFileFilter(".svg",
			"Scalable Vector Graphics (.svg)");

	/**
	 * Extension filter for .netstats data files.
	 */
	public static final ExtensionFileFilter netStatsFilter = new ExtensionFileFilter(".netstats",
			"Network Statistics (.netstats)");
}

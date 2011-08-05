/*
 * Copyright (c) 2006, 2007, 2008, 2010, Max Planck Institute for Informatics, Saarbruecken, Germany.
 *
 * This file is part of NetworkAnalyzer.
 * 
 * NetworkAnalyzer is free software: you can redistribute it and/or modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 * 
 * NetworkAnalyzer is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
 * for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with NetworkAnalyzer. If not, see
 * <http://www.gnu.org/licenses/>.
 */

package de.mpg.mpi_inf.bioinf.netanalyzer.ui;

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

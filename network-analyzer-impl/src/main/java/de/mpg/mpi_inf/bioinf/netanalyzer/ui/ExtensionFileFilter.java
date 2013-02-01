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

import java.io.File;

import javax.swing.filechooser.FileFilter;

/**
 * Fle filter based on file's extension. Instances of this class are used by Swing's class
 * <code>JFileChooser</code>.
 * <p>
 * This class follows a different conception than the one in JCommon - it encodes a file type by its
 * description and one or more acceptable extensions, whereas
 * <code>org.jfree.ui.ExtensionFileFilter</code> supports only one extension per file type.
 * </p>
 * <p>
 * The behavior of this class is similar to <code>cytoscape.util.CyFileFilter</code>. It is,
 * however, more efficient when the number of acceptable extensions is low.
 * </p>
 * 
 * @author Yassen Assenov
 */
public class ExtensionFileFilter extends FileFilter {

	/**
	 * Initializes a new instance of <code>ExtensionFileFilter</code>.
	 * 
	 * @param aExtension Extension to be used in accepting filenames.
	 * @param aDescription Description of the file type, identified by the specified extension.
	 */
	public ExtensionFileFilter(String aExtension, String aDescription) {
		super();

		extensions = new String[1];
		setExtension(0, aExtension);
		description = aDescription;
	}

	/**
	 * Initializes a new instance of <code>ExtensionFileFilter</code>.
	 * 
	 * @param aExtension1 First extension to be used in accepting filenames.
	 * @param aExtension2 Second extension to be used in accepting filenames.
	 * @param aDescription Description of the file type, identified by the specified extension.
	 */
	public ExtensionFileFilter(String aExtension1, String aExtension2, String aDescription) {
		super();

		extensions = new String[2];
		setExtension(0, aExtension1);
		setExtension(1, aExtension2);
		description = aDescription;
	}

	/**
	 * Initializes a new instance of <code>ExtensionFileFilter</code>.
	 * 
	 * @param aExtensions Extensions to be used in accepting filenames.
	 * @param aDescription Description of the file type, identified by the specified extensions.
	 */
	public ExtensionFileFilter(String[] aExtensions, String aDescription) {
		super();

		extensions = new String[aExtensions.length];
		for (int i = 0; i < aExtensions.length; ++i) {
			setExtension(i, aExtensions[i]);
		}

		description = aDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
	 */
	@Override
	public boolean accept(File file) {
		if (file.isDirectory() || hasExtension(file.getName())) {
			return true;
		}
		return false;
	}

	/**
	 * Appends an extension to a specified file.
	 * <p>
	 * The extension appended is the typical extension of the filetypes identifed by this instance.
	 * </p>
	 * 
	 * @param aFile File to get an extension.
	 * @return File with the same name as the specified one, but with appended extension.
	 */
	public File appendExtension(File aFile) {
		return new File(aFile.getAbsolutePath() + extensions[0]);
	}

	/**
	 * Appends an extension to a filename.
	 * <p>
	 * The extension appended is the typical extension of the filetypes identifed by this instance.
	 * </p>
	 * 
	 * @param aFileName Name of file to get an extension.
	 * @return <code>String</code> containing the given filename with appended extension.
	 */
	public String appendExtension(String aFileName) {
		return aFileName + extensions[0];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.filechooser.FileFilter#getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * Gets the typical extension.
	 * 
	 * @return <code>String</code> encoding the typical extension of the filetypes identified by
	 *         this instance.
	 */
	public String getExtension() {
		return extensions[0].substring(1);
	}

	/**
	 * Checks if a given filename ends with one of the accepted extensions.
	 * 
	 * @param aFile File, whose name is to be checked.
	 * @return <code>true</code> if the name of <code>aFile</code> ends with one of the accepted
	 *         extensions; <code>false</code> otherwise.
	 */
	public boolean hasExtension(File aFile) {
		return hasExtension(aFile.getName());
	}

	/**
	 * Checks if a given filename ends with one of the accepted extensions.
	 * 
	 * @param aFilename Name of file to be checked.
	 * @return <code>true</code> if the name of <code>aFile</code> ends with one of the accepted
	 *         extensions; <code>false</code> otherwise.
	 */
	public boolean hasExtension(String aFilename) {
		for (int i = 0; i < extensions.length; ++i) {
			if (aFilename.toLowerCase().endsWith(extensions[i])) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Sets one of the accepted extensions.
	 * <p>
	 * This method is called only from the constructor. It set the value of {@link #extensions}<code>[aIndex]</code>
	 * to <code>aExtension</code>, prefixing it by &quot;.&quot; if necessary.
	 * </p>
	 * 
	 * @param aIndex Index of the extension to set.
	 * @param aExtension Value to set.
	 */
	private void setExtension(int aIndex, String aExtension) {
		if (aExtension.startsWith(".")) {
			extensions[aIndex] = aExtension.toLowerCase();
		} else {
			extensions[aIndex] = "." + aExtension.toLowerCase();
		}
	}

	/**
	 * Array of extensions to be used in accepting filenames.
	 * <p>
	 * Every extension is a word in lower case letters, prefixed by the symbol &quot;.&quot;.
	 * </p>
	 */
	private String[] extensions;

	/**
	 * Description of file type, identified by the extensions.
	 */
	private String description;
}

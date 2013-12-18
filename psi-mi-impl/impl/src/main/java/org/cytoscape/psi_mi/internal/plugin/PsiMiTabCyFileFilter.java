package org.cytoscape.psi_mi.internal.plugin;

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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.DataCategory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PsiMiTabCyFileFilter implements CyFileFilter {

	private final Set<String> extensions;
	private final String description;
	private final Set<String> contentTypes;

	public PsiMiTabCyFileFilter() {
		extensions = new HashSet<String>();
		extensions.add("mitab");
		
		contentTypes = new HashSet<String>();
		contentTypes.add("text/psi-mi-tab");

		this.description = "PSI-MI TAB 2.5 file";
	}

	@Override
	public boolean accepts(URI uri, DataCategory category) {
		if (!category.equals(DataCategory.NETWORK))
			return false;
		
		final String ext = CommonsIOFilenameUtils.getExtension(uri.toString());
		if(extensions.contains(ext))
			return true;
		else
			return false;
	}


	@Override
	public boolean accepts(InputStream stream, DataCategory category) {
		if (!category.equals(DataCategory.NETWORK)) {
			return false;
		}
		try {
			return checkFirstLine(stream);
		} catch (IOException e) {
			Logger logger = LoggerFactory.getLogger(getClass());
			logger.error("Error while checking header", e);
			return false;
		}
	}
	
	private boolean checkFirstLine(InputStream stream) throws IOException {
		final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
		final String line = reader.readLine();
		
		if (line != null) {
			String[] parts = line.split("\t");
			if(parts.length >= 15)
				return true;
			else {
				return false;
			}
		}
		return false;
	}


	@Override
	public Set<String> getExtensions() {
		return extensions;
	}

	@Override
	public Set<String> getContentTypes() {
		return contentTypes;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public DataCategory getDataCategory() {
		return DataCategory.NETWORK;
	}

	/*
	 * Licensed to the Apache Software Foundation (ASF) under one or more
	 * contributor license agreements.  See the NOTICE file distributed with
	 * this work for additional information regarding copyright ownership.
	 * The ASF licenses this file to You under the Apache License, Version 2.0
	 * (the "License"); you may not use this file except in compliance with
	 * the License.  You may obtain a copy of the License at
	 * 
	 *      http://www.apache.org/licenses/LICENSE-2.0
	 * 
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */
	/**
	 * Taken from org.apache.commons.io.FilenameUtils 2.1 to resolve 
	 * conflicts.  PSIMI library requires commons-io 1.4.
	 * 
	 * TODO: Update PSIMI library and remove this.
	 */
	private static final class CommonsIOFilenameUtils {
	    /**
	     * The extension separator character.
	     * @since Commons IO 1.4
	     */
	    public static final char EXTENSION_SEPARATOR = '.';

	    /**
	     * The Unix separator character.
	     */
	    private static final char UNIX_SEPARATOR = '/';

	    /**
	     * The Windows separator character.
	     */
	    private static final char WINDOWS_SEPARATOR = '\\';
	    
	    /**
	     * Gets the extension of a filename.
	     * <p>
	     * This method returns the textual part of the filename after the last dot.
	     * There must be no directory separator after the dot.
	     * <pre>
	     * foo.txt      --> "txt"
	     * a/b/c.jpg    --> "jpg"
	     * a/b.txt/c    --> ""
	     * a/b/c        --> ""
	     * </pre>
	     * <p>
	     * The output will be the same irrespective of the machine that the code is running on.
	     *
	     * @param filename the filename to retrieve the extension of.
	     * @return the extension of the file or an empty string if none exists or <code>null</code>
	     * if the filename is <code>null</code>.
	     */
	    public static String getExtension(String filename) {
	        if (filename == null) {
	            return null;
	        }
	        int index = indexOfExtension(filename);
	        if (index == -1) {
	            return "";
	        } else {
	            return filename.substring(index + 1);
	        }
	    }

	    /**
	     * Returns the index of the last extension separator character, which is a dot.
	     * <p>
	     * This method also checks that there is no directory separator after the last dot.
	     * To do this it uses {@link #indexOfLastSeparator(String)} which will
	     * handle a file in either Unix or Windows format.
	     * <p>
	     * The output will be the same irrespective of the machine that the code is running on.
	     * 
	     * @param filename  the filename to find the last path separator in, null returns -1
	     * @return the index of the last separator character, or -1 if there
	     * is no such character
	     */
	    public static int indexOfExtension(String filename) {
	        if (filename == null) {
	            return -1;
	        }
	        int extensionPos = filename.lastIndexOf(EXTENSION_SEPARATOR);
	        int lastSeparator = indexOfLastSeparator(filename);
	        return (lastSeparator > extensionPos ? -1 : extensionPos);
	    }
	    
	    /**
	     * Returns the index of the last directory separator character.
	     * <p>
	     * This method will handle a file in either Unix or Windows format.
	     * The position of the last forward or backslash is returned.
	     * <p>
	     * The output will be the same irrespective of the machine that the code is running on.
	     * 
	     * @param filename  the filename to find the last path separator in, null returns -1
	     * @return the index of the last separator character, or -1 if there
	     * is no such character
	     */
	    public static int indexOfLastSeparator(String filename) {
	        if (filename == null) {
	            return -1;
	        }
	        int lastUnixPos = filename.lastIndexOf(UNIX_SEPARATOR);
	        int lastWindowsPos = filename.lastIndexOf(WINDOWS_SEPARATOR);
	        return Math.max(lastUnixPos, lastWindowsPos);
	    }
	}
}

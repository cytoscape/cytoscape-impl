package org.cytoscape.task.internal.export;

/*
 * #%L
 * Cytoscape Core Task Impl (core-task-impl)
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


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FilenameUtils;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyWriterFactory;
import org.cytoscape.io.write.CyWriterManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


/**
 * An abstract utility implementation of a Task that writes a user defined 
 * file to a file type determined by a provided writer manager.  This class
 * is meant to be extended for specific file types such that the appropriate
 * {@link org.cytoscape.io.write.CyWriter} can be identified.
 * @param <T> Generic type that extends CyWriterManager.
 * @CyAPI.Abstract.Class
 */
public abstract class AbstractCyWriter<S extends CyWriterFactory,T extends CyWriterManager<S>> extends AbstractTask
	implements CyWriter
{
	/** The file to be written. */
	protected File outputFile;
	/** The output stream used to write the file */
	protected ByteArrayOutputStream outputStream;

	/**
	 * The method sets the file to be written.  This field should not
	 * be called directly, but rather handled by the {@link org.cytoscape.work.Tunable}
	 * processing. This method is the "setter" portion of a
	 * getter/setter tunable method pair.
	 * @param f The file to be written.
	 */
	public final void setOutputFile(File f) {
		if ( f != null )
			outputFile = f;
	}

	/**
	 * This method gets the file to be written.  This method should not
	 * be called directly, but rather handled by the {@link org.cytoscape.work.Tunable}
	 * processing. This method is the "getter" portion of a
	 * getter/setter tunable method pair.
	 * @return The file to be written.
	 */
	public File getOutputFile() {
		return outputFile;
	}

	/** An implementation of this method should return a file format description
	 * from {@link CyFileFilter}, such that the string can be found in the descriptionFilterMap.
	 * @return a file format description from {@link CyFileFilter}.
	 */
	abstract protected String getExportFileFormat();
	
	/** This method returns a suggested file name for the network/table/view to be exported.
	 * @return the suggested file name
	 */
	abstract protected String getExportName();
	
	/** @return The {*@link CyWriter} to be used to write the file */
	abstract protected CyWriter getWriter();
	
	/** A Map that maps file filter description strings to {@link CyFileFilter}s*/
	private final Map<String,CyFileFilter> descriptionFilterMap;

	/** A Map that maps file filter extension strings to {@link CyFileFilter}s*/
	private final Map<String,CyFileFilter> extensionFilterMap;

	/** The CyWriterManager specified in the constructor **/
	protected final T writerManager;
	
	/** The CyApplicationManager specified in the constructor **/
	protected final CyApplicationManager cyApplicationManager;

	/**
	 * Constructor.
	 * @param writerManager The CyWriterManager to be used to determine which
	 * {@link org.cytoscape.io.write.CyWriter} to be used to write the file chosen by the user. 
	 * @param CyApplicationManager The CyApplicationManager to be used to get the 
	 * current working directory.
	 */
	public AbstractCyWriter(T writerManager, CyApplicationManager cyApplicationManager) {
		if (writerManager == null)
			throw new NullPointerException("CyWriterManager is null");
		this.writerManager = writerManager;
		this.cyApplicationManager = cyApplicationManager;
		
		outputStream = new ByteArrayOutputStream();
		
		descriptionFilterMap = new TreeMap<String,CyFileFilter>();
		extensionFilterMap = new TreeMap<String,CyFileFilter>();
		for (CyFileFilter f : writerManager.getAvailableWriterFilters()) {
			descriptionFilterMap.put(f.getDescription(), f);
			for ( String ext : f.getExtensions() ) {
				extensionFilterMap.put(ext.toLowerCase(),f);
			}
		}
	}

	/**
	 * This method processes the chosen input file and output type and attempts
	 * to write the file.
	 * @param tm The {@link org.cytoscape.work.TaskMonitor} provided by the TaskManager execution environment.
	 */
	public final void run(final TaskMonitor tm) throws Exception {
		getWriter().run(tm);
		FileOutputStream fos = new FileOutputStream(outputFile);
		outputStream.writeTo(fos);
		fos.close();
	}

	/**
	 * Should return a {@link org.cytoscape.io.write.CyWriter} object for writing 
	 * a file of the specified type.
	 * @param filter The specific type of file to be written.
	 * @return a {@link org.cytoscape.io.write.CyWriter} object for writing a file 
	 * of the specified type.
	 * @throws Exception 
	 */
	protected abstract CyWriter getWriter(CyFileFilter filter) throws Exception;
	
	/**
	 * Returns a file in the current directory with a suggested name and extension
	 * suitable for the current export parameters.
	 * @return a File in the current directory with a name/extension determined by the exporter.
	 */
	protected File getSuggestedFile() {
		String exportName = getExportName();
		if (exportName == null || exportName.trim().isEmpty())
			exportName = "Untitled";
		
		return new File(cyApplicationManager.getCurrentDirectory(), exportName + "." +
				getFileFilter(getExportFileFormat()).getExtensions().iterator().next());
	}

	/**
	 * Returns a collection of human readable descriptions of all of the file filters 
	 * available for this writer.
	 * @return a collection of human readable descriptions of all of the file filters 
	 * available for this writer.
	 */
	protected final Collection<String> getFileFilterDescriptions() {
		return descriptionFilterMap.keySet();
	}

	/**
	 * Returns a CyFileFilter that matches the specifed description where the description
	 * is either the human readable text returned from #getFileFilterDescriptions or a
	 * valid extension for the CyFileFilter.  Duplicate file extensions (e.g. xml) will
	 * return an arbitrary filter.
	 * @param description A human readable description or file extension string.
	 * @returns a CyFileFilter matching the description or extension.
	 */
	protected final CyFileFilter getFileFilter(String description) {
		if (description == null)
			return null;

		CyFileFilter f = descriptionFilterMap.get(description);
		if ( f != null )
			return f;
		else
			return extensionFilterMap.get(description.toLowerCase());
	}
	
	/**
	 * Returns whether the given File's extension is acceptable for the selected file format
	 * @param file The input file
	 * @return true if the extension is accepted for the selected file format, and false otherwise
	 */
	protected final boolean fileExtensionIsOk(final File file) {
		final String exportFileFormat = getExportFileFormat();
		
		if (exportFileFormat == null)
			return true;

		final CyFileFilter filter = getFileFilter(exportFileFormat);
		
		if (filter == null)
			return true;

		return filter.getExtensions().contains(FilenameUtils.getExtension(file.getName()));
	}
	
	/**
	 * Adds or replaces the extension of the given File with one suitable for the selected export file format
	 * @param file The input file
	 * @return a File with an extension suitable for the selected export file format
	 */
	protected final File addOrReplaceExtension(final File file) {
		final CyFileFilter filter = getFileFilter(getExportFileFormat());
		
		if (filter == null)
			return file;

		final Iterator<String> extensions = filter.getExtensions().iterator();
		
		if (!extensions.hasNext())
			return file;

		final String filterExtension = extensions.next();
		String fileName = file.getAbsolutePath();
		final String fileExtension = FilenameUtils.getExtension(fileName);
		
		if (!filterExtension.trim().equals(fileExtension.trim()))
			fileName = FilenameUtils.removeExtension(fileName) + "." + filterExtension;
		
		return new File(fileName);
	}
}

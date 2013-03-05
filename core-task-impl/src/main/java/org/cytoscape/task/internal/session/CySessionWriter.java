package org.cytoscape.task.internal.session;

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


import org.cytoscape.session.CySession;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CySessionWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * A utility Task implementation that writes a {@link org.cytoscape.session.CySession} to a file.
 * @CyAPI.Final.Class
 */
public final class CySessionWriter extends AbstractTask implements CyWriter {
	private final CySession session; 
	private final CySessionWriterManager writerMgr; 
	private File outputFile; 

	Logger logger = LoggerFactory.getLogger(CySessionWriter.class);
	
	/**
	 * Constructs this CySessionWriter.
	 * @param writerMgr The {@link org.cytoscape.io.write.CySessionWriterManager} contains single expected
	 * {@link org.cytoscape.io.write.CySessionWriterFactory} to use to write the file.
	 * @param session The {@link org.cytoscape.session.CySession} to be written out. 
	 * @param outputFile The file the {@link org.cytoscape.session.CySession} should be written to.
 	 */
	public CySessionWriter(CySessionWriterManager writerMgr, CySession session, File outputFile) {
		if (writerMgr == null)
			throw new NullPointerException("Writer Manager is null");
		this.writerMgr = writerMgr;

		if (session == null)
			throw new NullPointerException("Session Manager is null");
		this.session = session;

		if (outputFile == null)
			throw new NullPointerException("Output File is null");
		this.outputFile = outputFile;
	}

	/**
	 * The method that will actually write the specified session to the specified
	 * file.
	 * @param tm The {@link org.cytoscape.work.TaskMonitor} provided by the TaskManager execution environment.
	 */
	public final void run(TaskMonitor tm) throws Exception {
		final List<CyFileFilter> filters = writerMgr.getAvailableWriterFilters();
		
		if (filters == null || filters.size() < 1)
			throw new NullPointerException("No Session file filters found");
		if (filters.size() > 1)
			throw new IllegalArgumentException("Found too many session filters.");

		String filepath = outputFile.getAbsolutePath();
		
		if (!filepath.toLowerCase().endsWith(".cys")) {
			filepath += ".cys";
			outputFile = new File(filepath);
		}
		
		if (!canWrite(outputFile)) {
			throw new IOException(
					"Session not saved. Cytoscape does not have write permission to save the session at \""
							+ outputFile.getParentFile().getAbsolutePath()
							+ "\". Choose another folder or change the folder permissions.");
		}
		
		// Write to a temporary file first, to prevent the original file from being damaged, in case there is any error	
		File tmpFile = new File(filepath + ".tmp");
		int count = 0;
		
		while (tmpFile.exists() && count < 100) {
			try {
				// Just in case there is already a temporary file with the same name
				tmpFile.delete();
			} catch (final Exception e) {
				logger.warn("Cannot delete old temporary session file: " + tmpFile, e);
				tmpFile = new File(tmpFile.getAbsolutePath() + ".tmp" + (++count));
			}
		}
		
		final CyWriter writer = writerMgr.getWriter(session, filters.get(0), tmpFile);
		
		if (writer == null)
			throw new NullPointerException("No CyWriter found for specified file type.");

		// If the main task is successfully executed, this task will move the temp file to the actual output path
		final ReplaceFileTask replaceFileTask = new ReplaceFileTask(tmpFile, outputFile);
		
		insertTasksAfterCurrentTask(writer, replaceFileTask);
	}

	boolean canWrite(final File file) {
		final File dir = file.isDirectory() ? file : file.getParentFile();
		
		if (!dir.canWrite()) {
			// Should be enough for most OSs
			return false;
		} else {
			// File.canWrite doesn't work well on Windows, so let's try a workaround
			final File dummy = new File(dir, "empty_" + System.currentTimeMillis() + ".tmp"); 
			
			try {
				// Create and delete a dummy file in order to check file permissions.
				dummy.createNewFile();
			} catch(final IOException ioe) {
				return false;
			} finally {
				try {
					if (dummy != null) dummy.delete();
				} catch(final Exception e) {
					logger.warn("Cannot delete dummy file used to test write permission: " + dummy, e);
				}
			}
		}
		
		return true;
	}

	static boolean HasFileExtension(final String pathName) {
		final int lastDotPos = pathName.lastIndexOf('.');
		final int lastSlashPos = pathName.lastIndexOf(File.separatorChar);
		return lastSlashPos < lastDotPos; // Yes, this also works if one or both of lastSlashPos and lastDotPos are -1!
	}
	
	private class ReplaceFileTask extends AbstractTask {
		private final File temp;
		private final File output;
		
		public ReplaceFileTask(final File temp, final File output) {
			this.temp = temp;
			this.output = output;
		}

		@Override
		public void run(final TaskMonitor taskMonitor) throws Exception {
			File backup = null;
			
			// If the there is already a session file, create a backup first
			if (output.exists()) {
				backup = new File(output.getAbsolutePath() + ".bkp");
				int count = 0;
				
				while (backup.exists() && count < 100) {
					try {
						// Just in case there is already a backup file with the same name
						// (depending on the OS, the destination file has to be deleted before calling renameTo()).
						backup.delete();
					} catch (final Exception e) {
						logger.warn("Cannot delete old session backup file: " + backup, e);
						backup = new File(output.getAbsolutePath() + ".bkp" + (++count));
					}
				}
				
				// Try to move the original session file to the backup one
				if (output.renameTo(backup)) {
					if (output.exists()) {
						// This should never happen, but let's make sure the file is deleted,
						// because we will move the temporary file to the actual output one
						// (again, the OS may require the destination file to be deleted first).
						try {
							output.delete();
						} catch (final Exception e) {
							logger.warn("Cannot delete old session file: " + output, e);
						}
					}
				} else {
					throw new RuntimeException("Cannot create a backup of the original session file. " +
							"The current session was saved at: " + temp.getAbsolutePath());
				}
			}
			
			// Finally try to move the temporary file (current saved session) to the original output file
			if (temp.renameTo(output)) {
				if (temp.exists()) {
					// This should never happen, but make sure the temp file is deleted.
					try {
						temp.delete();
					} catch (final Exception e) {
						logger.warn("Cannot delete temporary file: " + temp.getAbsolutePath(), e);
					}
				}
				
				// Now it's safe to delete the backup, if there is one.
				if (backup != null && backup.exists()) {
					try {
						backup.delete();
					} catch (final Exception e) {
						logger.warn("Cannot delete backup file: " + backup.getAbsolutePath(), e);
					}
				}
			} else {
				throw new RuntimeException("Cannot move the temporary file of the saved session to " + 
						output.getAbsolutePath() +  ". The current session was saved at: " + 
						temp.getAbsolutePath());
			}
		}
	}
}

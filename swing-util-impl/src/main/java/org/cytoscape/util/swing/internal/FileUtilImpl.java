package org.cytoscape.util.swing.internal;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;

/*
 * #%L
 * Cytoscape Swing Utility Impl (swing-util-impl)
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2006 - 2016 The Cytoscape Consortium
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

class FileUtilImpl implements FileUtil {
	
	private final CyServiceRegistrar serviceRegistrar;

	FileUtilImpl(final CyServiceRegistrar serviceRegistrar) {
		this.serviceRegistrar = serviceRegistrar;
	}

	@Override
	public File getFile(final Component parent, final String title, final int loadSaveCustom,
			final Collection<FileChooserFilter> filters) {
		return getFile(parent, title, loadSaveCustom, null, null, filters);
	}

	@Override
	public File getFile(final Component parent, final String title, final int loadSaveCustom,
	                    final String startDir, final String customApproveText,
	                    final Collection<FileChooserFilter> filters) {
		File[] result = getFiles(parent, title, loadSaveCustom, startDir,
					 customApproveText, false, filters);

		return ((result == null) || (result.length <= 0)) ? null : result[0];
	}

	@Override
	public File[] getFiles(final Component parent, final String title,
	                       final int loadSaveCustom,
	                       final Collection<FileChooserFilter> filters) {
		return getFiles(parent, title, loadSaveCustom, null, null, true, filters);
	}

	@Override
	public File[] getFiles(final Component parent, final String title,
	                       final int loadSaveCustom, final String startDir,
	                       final String customApproveText,
	                       final Collection<FileChooserFilter> filters) {
		return getFiles(parent, title, loadSaveCustom, startDir,
				customApproveText, true, filters);
	}

	@Override
	public File[] getFiles(final Component parent, final String title, final int loadSaveCustom, String startDir,
			final String customApproveText, final boolean multiselect, final Collection<FileChooserFilter> filters) {
		
		if (parent == null)
			throw new NullPointerException("\"parent\" must not be null.");
		
		final String osName = System.getProperty("os.name");
		final CyApplicationManager applicationManager = serviceRegistrar.getService(CyApplicationManager.class);
		
		if (osName.startsWith("Mac")) {
			// This is a Macintosh, use the AWT style file dialog
			
			final String fileDialogForDirectories = System.getProperty("apple.awt.fileDialogForDirectories");
			System.setProperty("apple.awt.fileDialogForDirectories", "false");

			try {
				final FileDialog chooser;
				if (parent instanceof Frame)
					chooser = new FileDialog((Frame) parent, title, loadSaveCustom);
				else if (parent instanceof Dialog)
					chooser = new FileDialog((Dialog) parent, title, loadSaveCustom);
				else if (parent instanceof JMenuItem) {
					JComponent jcomponent = (JComponent) ((JPopupMenu)parent.getParent()).getInvoker();
					chooser = new FileDialog((Frame) jcomponent.getTopLevelAncestor(), title, loadSaveCustom);
				} else {
					throw new IllegalArgumentException("Cannot (not implemented yet) create a dialog " +
							"own by a parent component of type: " + parent.getClass().getCanonicalName());
				}

				if (startDir != null)
					chooser.setDirectory(startDir);
				else
					chooser.setDirectory(applicationManager.getCurrentDirectory().getAbsolutePath());
				
				chooser.setModal(true);
				chooser.setFilenameFilter(new CombinedFilenameFilter(filters));
				chooser.setLocationRelativeTo(parent);
				chooser.setMultipleMode(multiselect);
				chooser.setVisible(true);

				if (chooser.getFile() != null) {
					final File[] results;
					if (loadSaveCustom == SAVE) {
						String newFileName = chooser.getFile();
						final String fileNameWithExt = addFileExt(filters, newFileName);
						
						if (!fileNameWithExt.equals(newFileName)) {
							final File file = new File(chooser.getDirectory() + File.separator + fileNameWithExt);
							
							if (file.exists()) {
								int answer =
										JOptionPane.showConfirmDialog(
											parent,
											"The file '" + file.getName()
											+ "' already exists. Are you sure you want to overwrite it?",
											"File exists",
											JOptionPane.YES_NO_OPTION,
											JOptionPane.WARNING_MESSAGE);
									
								if (answer == JOptionPane.NO_OPTION) // Try again
									return getFiles(parent, title, loadSaveCustom, file.getParent(), customApproveText,
											multiselect, filters);
							}
							newFileName = fileNameWithExt;
						}
						results = new File[1];
						results[0] = new File(chooser.getDirectory() + File.separator + newFileName);
					}
					else
						 results = chooser.getFiles();

					if (chooser.getDirectory() != null)
						applicationManager.setCurrentDirectory(new File(chooser.getDirectory()));

					return results;
				}
			} finally {
				if (fileDialogForDirectories != null)
					System.setProperty("apple.awt.fileDialogForDirectories", fileDialogForDirectories);
			}

			return null;
		} else {
			// this is not a Mac, use the Swing based file dialog
			final JFileChooser chooser;
			if(startDir != null)
				chooser = new JFileChooser(new File(startDir));
			else
				chooser = new JFileChooser(applicationManager.getCurrentDirectory());
			
			// set multiple selection, if applicable
			chooser.setMultiSelectionEnabled(multiselect);

			// set the dialog title
			chooser.setDialogTitle(title);
			chooser.setAcceptAllFileFilterUsed(loadSaveCustom == LOAD);

			int i = 0;
			FileChooserFilter defaultFilter = null;
			for (final FileChooserFilter filter : filters) {
				// If we're down to the last filter and we haven't yet selected a default,
				// do it now!
				if (++i == filters.size() && defaultFilter == null)
					defaultFilter = filter;

				// If we haven't yet selected a default and our filter's description starts
				// with "All ", make it the default.
				else if (defaultFilter == null && filter.getDescription().startsWith("All "))
					defaultFilter = filter;
				chooser.addChoosableFileFilter(filter);
			}

			File[] results = null;
			File tmp = null;

			// set the dialog type
			if (loadSaveCustom == LOAD) {
				if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						results = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						results = new File[1];
						results[0] = tmp;
					}
					
					if (filters != null && !filters.isEmpty()) {
						boolean extensionFound = false;
						for (int k = 0; k < results.length; ++k) {
							String path = results[k].getPath();
							for (final FileChooserFilter filter : filters) {
								String []filterExtensions = filter.getExtensions();
								for (int t = 0; t < filterExtensions.length; ++t) {
									if(filterExtensions[t].equals("") || path.endsWith("."+filterExtensions[t]))
										extensionFound = true;
								}
							}
							if (!extensionFound) {
									JOptionPane.showMessageDialog(
										chooser,
										"Cytoscape does not recognize files with suffix '"
										+ path.substring(path.lastIndexOf("."))
										+ "' . Please choose another file.",
										"File extension incorrect",
										JOptionPane.WARNING_MESSAGE);
									return null;
							}
							extensionFound = false;
						}
					}
				}
			} else if (loadSaveCustom == SAVE) {
				if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
					if (multiselect) {
						results = chooser.getSelectedFiles();
					} else if ((tmp = chooser.getSelectedFile()) != null) {
						results = new File[1];
						results[0] = tmp;
					}

					// FileDialog checks for overwrites, but JFileChooser does
					// not, so we need to do so ourselves:
					for (int k = 0; k < results.length; ++k) {
						File file = results[k];
						final String filePath = file.getAbsolutePath();
						final String filePathWithExt = addFileExt(filters, filePath);
						
						// Add an extension to the filename, if necessary and possible
						if (!filePathWithExt.equals(filePath)) {
							file = new File(filePathWithExt);
							results[k] = file;
						}
							
						if (file.exists()) {
							int answer =
								JOptionPane.showConfirmDialog(
									chooser,
									"The file '" + file.getName()
									+ "' already exists. Are you sure you want to overwrite it?",
									"File exists",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE);
							
							if (answer == JOptionPane.NO_OPTION) // Try again
								return getFiles(parent, title, loadSaveCustom, file.getParent(), customApproveText,
										multiselect, filters);
						}
					}
				}
			} else {
				if (chooser.showDialog(parent, customApproveText) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						results = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						results = new File[1];
						results[0] = tmp;
					}
				}
			}

			if (results != null && chooser.getCurrentDirectory().getPath() != null)
				applicationManager.setCurrentDirectory(chooser.getCurrentDirectory());

			return results;
		}
	}
	
	private String addFileExt(final Collection<FileChooserFilter> filters, String fileName) {
		final Set<String> extSet = new LinkedHashSet<>();
		
		for (final FileChooserFilter filter : filters) {
			final String[] exts = filter.getExtensions();
			
			for (String ext : exts)
				extSet.add(ext);
		}
		
		// If there is more than one possible extension, don't add any extension here,
		// because we don't know which one should be chosen.
		if (extSet.size() == 1) {
			// Check file name has the extension
			final String upperName = fileName.toUpperCase();
			final String ext = extSet.iterator().next();
			
			if (!upperName.endsWith("." + ext.toUpperCase()))
				fileName = fileName + "." + ext;
		}
		
		return fileName;
	}

	private static final class CombinedFilenameFilter implements FilenameFilter {
		
		private final Collection<FileChooserFilter> filters;

		CombinedFilenameFilter(final Collection<FileChooserFilter> filters) {
			this.filters = filters;
		}

		@Override
		public boolean accept(final File dir, final String name) {
			if (filters.isEmpty())
				return true;

			final File path = new File(dir, name);
			for (final FileChooserFilter filter : filters) {
				if (filter.accept(path))
					return true;
			}

			return false;
		}
	}
}

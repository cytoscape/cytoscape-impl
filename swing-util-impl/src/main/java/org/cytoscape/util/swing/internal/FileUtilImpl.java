/*
  File: FileUtil.java

  Copyright (c) 2006, 2011, The Cytoscape Consortium (www.cytoscape.org)

  This library is free software; you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as published
  by the Free Software Foundation; either version 2.1 of the License, or
  any later version.

  This library is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  documentation provided hereunder is on an "as is" basis, and the
  Institute for Systems Biology and the Whitehead Institute
  have no obligations to provide maintenance, support,
  updates, enhancements or modifications.  In no event shall the
  Institute for Systems Biology and the Whitehead Institute
  be liable to any party for direct, indirect, special,
  incidental or consequential damages, including lost profits, arising
  out of the use of this software and its documentation, even if the
  Institute for Systems Biology and the Whitehead Institute
  have been advised of the possibility of such damage.  See
  the GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, write to the Free Software Foundation,
  Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
*/
package org.cytoscape.util.swing.internal;


import java.awt.Component;
import java.awt.Dialog;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.cytoscape.property.CyProperty;
import org.cytoscape.util.swing.FileChooserFilter;
import org.cytoscape.util.swing.FileUtil;


class FileUtilImpl implements FileUtil {
	
	private final Properties coreProperties;

	FileUtilImpl(final CyProperty<Properties> cyCoreProperty) {
		coreProperties = cyCoreProperty.getProperties();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getFile(final Component parent, final String title, final int load_save_custom,
			final Collection<FileChooserFilter> filters) {
		return getFile(parent, title, load_save_custom, null, null, filters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File getFile(final Component parent, final String title, final int load_save_custom,
	                    final String start_dir, final String custom_approve_text,
	                    final Collection<FileChooserFilter> filters)
	{
		File[] result = getFiles(parent, title, load_save_custom, start_dir,
					 custom_approve_text, false, filters);

		return ((result == null) || (result.length <= 0)) ? null : result[0];
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File[] getFiles(final Component parent, final String title,
	                       final int load_save_custom,
	                       final Collection<FileChooserFilter> filters)
	{
		return getFiles(parent, title, load_save_custom, null, null, true, filters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File[] getFiles(final Component parent, final String title,
	                       final int load_save_custom, final String start_dir,
	                       final String custom_approve_text,
	                       final Collection<FileChooserFilter> filters)
	{
		return getFiles(parent, title, load_save_custom, start_dir,
				custom_approve_text, true, filters);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public File[] getFiles(final Component parent, final String title, final int load_save_custom, String start_dir,
			final String custom_approve_text, final boolean multiselect, final Collection<FileChooserFilter> filters) {
		
		if (parent == null)
			throw new NullPointerException("\"parent\" must not be null!");

		if (start_dir == null)
			start_dir = coreProperties.getProperty(FileUtil.LAST_DIRECTORY, System.getProperty("user.dir"));
		
		final String osName = System.getProperty("os.name");
		
		if (osName.startsWith("Mac")) {
			// This is a Macintosh, use the AWT style file dialog
			
			final String fileDialogForDirectories = System.getProperty("apple.awt.fileDialogForDirectories");
			System.setProperty("apple.awt.fileDialogForDirectories", "false");

			try {
				final FileDialog chooser;
				if (parent instanceof Frame)
					chooser = new FileDialog((Frame) parent, title, load_save_custom);
				else if (parent instanceof Dialog)
					chooser = new FileDialog((Dialog) parent, title, load_save_custom);
				else if (parent instanceof JMenuItem) {
					JComponent jcomponent = (JComponent) ((JPopupMenu)parent.getParent()).getInvoker();
					chooser = new FileDialog((Frame) jcomponent.getTopLevelAncestor(), title, load_save_custom);
				} else {
					throw new IllegalArgumentException("Cannot (not implemented yet) create a dialog " +
							"own by a parent component of type: " + parent.getClass().getCanonicalName());
				}

				if (start_dir != null)
					chooser.setDirectory(start_dir);
				
				chooser.setModal(true);
				chooser.setFilenameFilter(new CombinedFilenameFilter(filters));
				chooser.setLocationRelativeTo(parent);
				chooser.setVisible(true);

				if (chooser.getFile() != null) {
					//TODO: how can we select multiple files on Mac?
					final File[] results = new File[1];
					String newFileName = chooser.getFile();
					
					//We need to do this check in the writers/readers
					//if (load_save_custom == SAVE)
					//	newFileName = addFileExt(filters, newFileName);
					
					results[0] = new File(chooser.getDirectory() + File.separator + newFileName);

					if (chooser.getDirectory() != null)
						coreProperties.setProperty(FileUtil.LAST_DIRECTORY, chooser.getDirectory());

					return results;
				}
			} finally {
				if(fileDialogForDirectories != null)
					System.setProperty("apple.awt.fileDialogForDirectories", fileDialogForDirectories);
			}

			return null;
		} else {
			// this is not a Mac, use the Swing based file dialog
			final File start = new File(start_dir);
			final JFileChooser chooser = new JFileChooser(start);
			
			// set multiple selection, if applicable
			chooser.setMultiSelectionEnabled(multiselect);

			// set the dialog title
			chooser.setDialogTitle(title);
			chooser.setAcceptAllFileFilterUsed(load_save_custom == LOAD);

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
			if (load_save_custom == LOAD) {
				if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						results = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						results = new File[1];
						results[0] = tmp;
					}
				}
			} else if (load_save_custom == SAVE) {
				if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						results = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						results = new File[1];
						results[0] = tmp;
					}

					// FileDialog checks for overwrites, but JFileChooser does
					// not, so we need to do so ourselves:
					for (int k = 0; k < results.length; ++k) {
						if (results[k].exists()) {
							int answer =
								JOptionPane.showConfirmDialog(
									chooser,
									"The file '"
									+ results[k].getName()
									+ "' already exists, are you sure you want to overwrite it?",
									"File exists",
									JOptionPane.YES_NO_OPTION,
									JOptionPane.WARNING_MESSAGE);
							if (answer == JOptionPane.NO_OPTION){
								return null;
							}
						}
					}
				}
			} else {
				if (chooser.showDialog(parent, custom_approve_text) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						results = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						results = new File[1];
						results[0] = tmp;
					}
				}
			}

			if (results != null && chooser.getCurrentDirectory().getPath() != null)
				coreProperties.setProperty(FileUtil.LAST_DIRECTORY,
				                           chooser.getCurrentDirectory().getPath());

			return results;
		}
	}
	
	private String addFileExt(final Collection<FileChooserFilter> filters, final String fileName) {
		final Set<String> extSet = new HashSet<String>();
		for(final FileChooserFilter filter: filters) {
			final String[] exts = filter.getExtensions();
			for(String ext:exts)
				extSet.add(ext);
		}
		
		// Check file name has  
		final String upperName = fileName.toUpperCase();
		for(String ext: extSet) {
			if(upperName.endsWith("." + ext.toUpperCase()))
				return fileName;
		}
		
		
		// Need to add ext
		String fullFileName = fileName;
		try {
			fullFileName = fileName + "." + extSet.iterator().next();
		}
		catch(Exception e){
			//If the category is "UNSPECIFIED", we may get null-pointer exception here
		}
		
		return fullFileName;
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

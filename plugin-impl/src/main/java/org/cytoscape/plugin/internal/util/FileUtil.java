/*
  File: FileUtil.java

  Copyright (c) 2006, 2010, The Cytoscape Consortium (www.cytoscape.org)

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
package org.cytoscape.plugin.internal.util;

import org.cytoscape.work.TaskMonitor;

import java.awt.Component;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.URL;

import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;

import org.cytoscape.plugin.internal.PluginTracker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.cytoscape.io.CyFileFilter;

/**
 * Provides a platform-dependent way to open files. Mainly
 * because Mac would prefer that you use java.awt.FileDialog
 * instead of the Swing FileChooser.
 */
public abstract class FileUtil {
	/**
	 *  This class exists to work around a bug in JFileChooser that will allow editing of
	 *  file and directory names even when LOAD has been specified.
	 */
	static class NoEditFileChooser extends JFileChooser {
		public NoEditFileChooser(final File start) {
			super(start);

			final JList list = findFileList(this);
			if (list == null)
				return;

			for (MouseListener l : list.getMouseListeners()) {
				if (l.getClass().getName().indexOf("FilePane") >= 0) {
					list.removeMouseListener(l);
					list.addMouseListener(new MyMouseListener(l));
				}
			}
		}

		private JList findFileList(final Component comp) {
			if (comp instanceof JList)
				return (JList)comp;

			if (comp instanceof Container) {
				for (Component child : ((Container)comp).getComponents()) {
					JList list = findFileList(child);
					if (list != null)
						return list;	
				}
			}

			return null;
		}

		private class MyMouseListener extends MouseAdapter {
			MyMouseListener(final MouseListener listenerChain) {
				m_listenerChain = listenerChain;
			}
			
			public void mouseClicked(final MouseEvent event) {
				if (event.getClickCount() > 1)
					m_listenerChain.mouseClicked(event);
			}
			
			private MouseListener m_listenerChain;
		}
	}

	protected static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
	/**
	 *
	 */
	public static int LOAD = FileDialog.LOAD;

	/**
	 *
	 */
	public static int SAVE = FileDialog.SAVE;

	/**
	 *
	 */
	public static int CUSTOM = LOAD + SAVE;

	/**
	 * A string that defines a simplified java regular expression for a URL.
	 * This may need to be updated to be more precise.
	 */
	public static final String urlPattern = 
		"^(jar\\:)?((http|https|ftp|file)+\\:\\/+\\S+)(\\!\\/\\S*)?$";

	/**
	 * Returns a File object, this method should be used instead
	 * of rolling your own JFileChooser.
	 *
	 * @return the location of the selcted file
	 * @param title the title of the dialog box
	 * @param load_save_custom a flag for the type of file dialog
	 */
	public static File getFile(String title, int load_save_custom) {
		return getFile(title, load_save_custom, new CyFileFilter[] {  }, null, null);
	}

	/**
	  * Returns a File object, this method should be used instead
	  * of rolling your own JFileChooser.
	  *
	  * @return the location of the selected file
	  * @param title the title of the dialog box
	  * @param load_save_custom a flag for the type of file dialog
	  * @param filters an array of CyFileFilters that let you filter
	  *                based on extension
	  */
	public static File getFile(String title, int load_save_custom, CyFileFilter[] filters) {
		return getFile(title, load_save_custom, filters, null, null);
	}

	/**
	  * Returns a File object, this method should be used instead
	  * of rolling your own JFileChooser.
	  *
	  * @return the location of the selected file
	  * @param title the title of the dialog box
	  * @param load_save_custom a flag for the type of file dialog
	  * @param filters an array of CyFileFilters that let you filter
	  *                based on extension
	  * @param selectedFile selectedFile
	  */	
	public static File getFile(String title, int load_save_custom, CyFileFilter filter, File selectedFile) {
		CyFileFilter[] filters = new CyFileFilter[1];
		filters[0] = filter;
		File[] selectedFiles = new File[1];
		selectedFiles[0] = selectedFile;
		
		File[] result = getFiles(null, title, load_save_custom, filters, null, null, false, selectedFiles);

		return ((result == null) || (result.length <= 0)) ? null : result[0];

	}

	
	/**
	 * Returns a File object, this method should be used instead
	 * of rolling your own JFileChooser.
	 *
	 * @return the location of the selcted file
	 * @param title the title of the dialog box
	 * @param load_save_custom a flag for the type of file dialog
	 * @param filters an array of CyFileFilters that let you filter
	 *                based on extension
	 * @param start_dir an alternate start dir, if null the default
	 *                  cytoscape MUD will be used
	 * @param custom_approve_text if this is a custom dialog, then
	 *                            custom text should be on the approve
	 *                            button.
	 */
	public static File getFile(String title, int load_save_custom, CyFileFilter[] filters,
	                           String start_dir, String custom_approve_text) {
		File[] result = getFiles(title, load_save_custom, filters, start_dir, custom_approve_text,
		                         false);

		return ((result == null) || (result.length <= 0)) ? null : result[0];
	}

	/**
	 * Returns an array of File objects, this method should be used instead
	 * of rolling your own JFileChooser.
	 *
	 * @return the location of the selcted file
	 * @param title the title of the dialog box
	 * @param load_save_custom a flag for the type of file dialog
	 * @param filters an array of CyFileFilters that let you filter
	 *                based on extension
	 */
	public static File[] getFiles(String title, int load_save_custom, CyFileFilter[] filters) {
		return getFiles(null,title, load_save_custom, filters, null, null, true);
	}

	/**
	 * Returns an array of File objects, this method should be used instead
	 * of rolling your own JFileChooser.
	 * @return the location of the selcted file
	 * @param parent the parent component of the JFileChooser dialog
	 * @param title the title of the dialog box
	 * @param load_save_custom a flag for the type of file dialog
	 * @param filters an array of CyFileFilters that let you filter
	 *                based on extension
	 */
	public static File[] getFiles(Component parent, String title, int load_save_custom, CyFileFilter[] filters) {
		return getFiles(parent,title, load_save_custom, filters, null, null, true);
	}
	
	
	/**
	 * Returns a list of File objects, this method should be used instead
	 * of rolling your own JFileChooser.
	 *
	 * @return and array of selected files, or null if none are selected
	 * @param title the title of the dialog box
	 * @param load_save_custom a flag for the type of file dialog
	 * @param filters an array of CyFileFilters that let you filter
	 *                based on extension
	 * @param start_dir an alternate start dir, if null the default
	 *                  cytoscape MUD will be used
	 * @param custom_approve_text if this is a custom dialog, then
	 *                            custom text should be on the approve
	 *                            button.
	 */
	public static File[] getFiles(String title, int load_save_custom, CyFileFilter[] filters,
	                              String start_dir, String custom_approve_text) {
		return getFiles(null,title, load_save_custom, filters, start_dir, custom_approve_text, true);
	}

	
	/**
	  * Returns a list of File objects, this method should be used instead
	  * of rolling your own JFileChooser.
	  *
	  * @return and array of selected files, or null if none are selected
	  * @param title the title of the dialog box
	  * @param load_save_custom a flag for the type of file dialog
	  * @param filters an array of CyFileFilters that let you filter
	  *                based on extension
	  * @param start_dir an alternate start dir, if null the default
	  *                  cytoscape MUD will be used
	  * @param custom_approve_text if this is a custom dialog, then
	  *                            custom text should be on the approve
	  *                            button.
	  * @param multiselect Enable selection of multiple files (Macs are
	  *                    still limited to a single file because we use
	  *                    FileDialog there -- is this fixed in Java 1.5?)
	  */	
	public static File[] getFiles(String title, int load_save_custom, CyFileFilter[] filters,
           String start_dir, String custom_approve_text, boolean multiselect) {
		return getFiles(null, title, load_save_custom, filters, start_dir, custom_approve_text, multiselect);
	}
	
	/**
	  * Returns a list of File objects, this method should be used instead
	  * of rolling your own JFileChooser.
	  *
	  * @return and array of selected files, or null if none are selected
	  * @param parent the parent of the JFileChooser dialog
	  * @param title the title of the dialog box
	  * @param load_save_custom a flag for the type of file dialog
	  * @param filters an array of CyFileFilters that let you filter
	  *                based on extension
	  * @param start_dir an alternate start dir, if null the default
	  *                  cytoscape MUD will be used
	  * @param custom_approve_text if this is a custom dialog, then
	  *                            custom text should be on the approve
	  *                            button.
	  * @param multiselect Enable selection of multiple files (Macs are
	  *                    still limited to a single file because we use
	  *                    FileDialog there -- is this fixed in Java 1.5?)
	  */
	public static File[] getFiles(Component parent, String title, int load_save_custom, CyFileFilter[] filters,
            String start_dir, String custom_approve_text, boolean multiselect) {
		return getFiles(parent, title, load_save_custom, filters, start_dir, custom_approve_text, multiselect, null);
	}
	
	
	/**
	  * Returns a list of File objects, this method should be used instead
	  * of rolling your own JFileChooser.
	  *
	  * @return and array of selected files, or null if none are selected
	  * @param parent the parent of the JFileChooser dialog
	  * @param title the title of the dialog box
	  * @param load_save_custom a flag for the type of file dialog
	  * @param filters an array of CyFileFilters that let you filter
	  *                based on extension
	  * @param start_dir an alternate start dir, if null the default
	  *                  cytoscape MUD will be used
	  * @param custom_approve_text if this is a custom dialog, then
	  *                            custom text should be on the approve
	  *                            button.
	  * @param multiselect Enable selection of multiple files (Macs are
	  *                    still limited to a single file because we use
	  *                    FileDialog there -- is this fixed in Java 1.5?)
	  * @param selectedFiles The list of selected files                
	  */
	public static File[] getFiles(Component parent, String title, int load_save_custom, CyFileFilter[] filters,
	                              String start_dir, String custom_approve_text, boolean multiselect, File[] selectedFiles) {

		/*
		
		if (parent == null) {
			parent = Cytoscape.getDesktop();
		}
		
		File start = null;

		if (start_dir == null) {
			start = CytoscapeInit.getMRUD();
		} else {
			start = new File(start_dir);
		}

		String osName = System.getProperty("os.name");

		//logger.info( "Os name: "+osName );
		if (osName.startsWith("Mac")) {
			// this is a Macintosh, use the AWT style file dialog
			FileDialog chooser = new FileDialog(Cytoscape.getDesktop(), title, load_save_custom);

			final File mostRecentlyUsedDirectory = CytoscapeInit.getMRUD();
			if (mostRecentlyUsedDirectory != null)
				chooser.setDirectory(mostRecentlyUsedDirectory.toString());

			if (!multiselect && selectedFiles != null)
				chooser.setFile(selectedFiles[0].toString());
		
			// we can only set the one filter; therefore, create a special
			// version of CyFileFilter that contains all extensions
			CyFileFilter fileFilter = new CyFileFilter();

			for (int i = 0; i < filters.length; i++) {
				Iterator iter;
				for (iter = filters[i].getExtensionSet().iterator(); iter.hasNext(); // Empty! )
					fileFilter.addExtension((String) iter.next());
			}

			fileFilter.setDescription("All network files");
			chooser.setFilenameFilter(fileFilter);

			chooser.setVisible(true);

			if (chooser.getFile() != null) {
				File[] result = new File[1];
				result[0] = new File(chooser.getDirectory() + "/" + chooser.getFile());

				if (chooser.getDirectory() != null)
					CytoscapeInit.setMRUD(new File(chooser.getDirectory()));

				return result;
			}

			return null;
		} else {
			// this is not a mac, use the Swing based file dialog
			final JFileChooser chooser = (load_save_custom == LOAD) ? new NoEditFileChooser(start) : new JFileChooser(start);

			if (multiselect && selectedFiles != null){
				chooser.setSelectedFiles(selectedFiles);					
			}
			if (!multiselect && selectedFiles != null){
				chooser.setSelectedFile(selectedFiles[0]);					
			}
			
			// set multiple selection, if applicable
			chooser.setMultiSelectionEnabled(multiselect);

			// set the dialog title
			chooser.setDialogTitle(title);

			// add filters
			for (int i = 0; i < filters.length; ++i) {
				chooser.addChoosableFileFilter(filters[i]);
			}

			File[] result = null;
			File tmp = null;

			// set the dialog type
			if (load_save_custom == LOAD) {
				if (chooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						result = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						result = new File[1];
						result[0] = tmp;
					}
				}
			} else if (load_save_custom == SAVE) {
				if (chooser.showSaveDialog(parent) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						result = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						result = new File[1];
						result[0] = tmp;
					}
					// FileDialog checks for overwrte, but JFileChooser does not, so we need to do
					// so ourselves
					for (int i = 0; i < result.length; i++) {
						if (result[i].exists()) {
							int answer = JOptionPane.showConfirmDialog(chooser, 
							   "The file '"+result[i].getName()+"' already exists, are you sure you want to overwrite it?",
							   "File exists", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
							if (answer == 1) 
								return null;
						}
					}
				}
			} else {
				if (chooser.showDialog(parent, custom_approve_text) == JFileChooser.APPROVE_OPTION) {
					if (multiselect)
						result = chooser.getSelectedFiles();
					else if ((tmp = chooser.getSelectedFile()) != null) {
						result = new File[1];
						result[0] = tmp;
					}
				}
			}

			if ((result != null) && (start_dir == null))
				CytoscapeInit.setMRUD(chooser.getCurrentDirectory());

			return result;
		}
		*/
		return null;
	}
	
	/**
	 *  DOCUMENT ME!
	 *
	 * @param name DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static InputStream getInputStream(String name) {
		return getInputStream(name, null);
	}

	/**
	 *  DOCUMENT ME!
	 *
	 * @param name DOCUMENT ME!
	 * @param monitor DOCUMENT ME!
	 *
	 * @return  DOCUMENT ME!
	 */
	public static InputStream getInputStream(String name, TaskMonitor monitor) {
		InputStream in = null;

		try {
			if (name.matches(urlPattern)) {
				URL u = new URL(name);
				// in = u.openStream();
                // Use URLUtil to get the InputStream since we might be using a proxy server 
				// and because pages may be cached:
				in = URLUtil.getBasicInputStream(u);
			} else
				in = new FileInputStream(name);
		} catch (IOException ioe) {
			logger.error("Unable to open file '"+name+"': "+ioe.getMessage());

			if (monitor != null)
				monitor.setStatusMessage(ioe.getMessage());
		}

		return in;
	}

	/**
	 *
	 * @param filename 
	 *		File to read in
	 *
	 * @return  The contents of the given file as a string.
	 */
	public static String getInputString(String filename) {
		try {
			InputStream stream = getInputStream(filename);
			return getInputString(stream);
		} catch (IOException ioe) {
			logger.warn("Couldn't create string from '"+filename+"': "+ioe.getMessage());
		}

		return null;
	}

	/**
	 *
	 * @param inputStream 
	 *		An InputStream
	 *
	 * @return  The contents of the given file as a string.
	 */
	public static String getInputString(InputStream inputStream) throws IOException {
		String lineSep = System.getProperty("line.separator");
		StringBuffer sb = new StringBuffer();
		String line = null;
		BufferedReader br = null;

		try {
			br = new BufferedReader(new InputStreamReader(inputStream));
			while ((line = br.readLine()) != null)
				sb.append(line + lineSep);
		}
		finally {
			if (br != null) {
				br.close();
			}
		}

		return sb.toString();
	}
}

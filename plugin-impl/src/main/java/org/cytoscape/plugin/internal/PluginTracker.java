/*
 File: PluginTracker.java 
 Copyright (c) 2006, 2007, The Cytoscape Consortium (www.cytoscape.org)

 The Cytoscape Consortium is:
 - Institute for Systems Biology
 - University of California San Diego
 - Memorial Sloan-Kettering Cancer Center
 - Institut Pasteur
 - Agilent Technologies

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

package org.cytoscape.plugin.internal;

import org.cytoscape.plugin.internal.PluginInfo.AuthorInfo;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;

import org.jdom.input.SAXBuilder;

import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;

import java.util.*;


public class PluginTracker {
	private static final Logger logger = LoggerFactory.getLogger(PluginTracker.class);

	private Document trackerDoc;
	private File installFile;
	private HashMap<String, Element> infoObjMap;
	private Set<Element> corruptedElements;
	private boolean corruptedElementsFound = false;
	
	/**
	 * Used for testing
	 * 
	 * @param FileName
	 *            Xml file name
	 * @param Dir
	 *            directory to to write xml file
	 * @throws IOException
	 */
	protected PluginTracker(File Dir, String FileName) throws IOException, TrackerException {
		installFile = new File(Dir, FileName);
		init();
	}
	
	protected PluginTracker(File file) throws IOException, TrackerException {
		installFile = file;
		init();
	}
	
	/*
	 * Used for tests.
	 */
	protected File getTrackerFile() {
		return installFile;
	}
	
	/*
	 * Sets up the xml doc for tracking.
	 */
	private void init() throws IOException, TrackerException {
		corruptedElements = new HashSet<Element>();
		
		if (PluginManager.usingWebstartManager()) { 
			// we don't want the old webstart file
			installFile.delete();
		}
	
		if (installFile.exists() && installFile.length() > 0) {
			SAXBuilder Builder = new SAXBuilder(false);
			try {
				FileInputStream is = null;

				try {
					is = new FileInputStream(installFile);
					trackerDoc = Builder.build(is, installFile.toURI().toURL().toString());
				} finally {
					if (is != null) {
						is.close();
					}
				}
				removeMissingIdEntries();
				write();
				validateTrackerDoc();
			} catch (Exception jde) {
				installFile.delete();
				createCleanDoc();
				throw new TrackerException("Plugin tracking file is corrupted.  Please reinstall your plugins. Deleting " + installFile.getAbsolutePath(), jde);
			} finally {
				createPluginTable();
			}
		} else {
			createCleanDoc();
			createPluginTable();
		}
	}

	/**
	 * Will throw an exception if the tracker document doesn't contain the necessary
	 * elements. The goal is to force a dummy plugin table to be created.  
	 */
	private void validateTrackerDoc() {
		for (PluginStatus ps: PluginStatus.values()) {
			// several of these calls could also produce an NPE
			Iterator<Element> iter = trackerDoc.getRootElement().getChild(ps.getTagName()).getChildren().iterator();
			if ( iter == null )
				throw new NullPointerException("corrupted tracker file");

		}
	}

	private void createCleanDoc() {
		logger.warn("Plugin tracker file: " + installFile.getAbsolutePath());
		trackerDoc = new Document();
		trackerDoc.setRootElement(new Element("CytoscapePlugin"));
		trackerDoc.getRootElement().addContent(new Element(PluginStatus.CURRENT.getTagName()));
		trackerDoc.getRootElement().addContent(new Element(PluginStatus.INSTALL.getTagName()));
		trackerDoc.getRootElement().addContent(new Element(PluginStatus.DELETE.getTagName()));
		write();
	}
	
	/* In order to maintain a list of plugins that does not duplicate entries due to lack of a unique identifier
	 * all entries that lack an unique id will be purged and expected to re-register.  User will see no difference.
	 */
	private void removeMissingIdEntries() {
		List<Element> Plugins = trackerDoc.getRootElement().getChild(PluginStatus.CURRENT.getTagName()).getChildren(pluginTag);
		List<Element> PluginsToRemove = new ArrayList<Element>();
		
		for (Element plugin: Plugins) {
			if (plugin.getChild(uniqueIdTag) == null ||
				plugin.getChild(uniqueIdTag).getTextTrim().length() <= 0) 
				PluginsToRemove.add(plugin);
		}
		
		for (Element child: PluginsToRemove) 
			trackerDoc.getRootElement().getChild(PluginStatus.CURRENT.getTagName()).removeContent(child);
		
	}
	
	/**
	 * Gets a list of plugins by their status. CURRENT: currently installed
	 * DELETED: to be deleted INSTALL: to be installed
	 * 
	 * @param Status
	 * @return List of PluginInfo objects
	 */
	protected List<PluginInfo> getPluginListByStatus(PluginStatus Status) {
		return getPluginContent(trackerDoc.getRootElement().getChild(Status.getTagName()));
	}
	
	/**
	 * Get the list of all downloadable object by their status.
	 * CURRENT: currently installed
	 * DELETED: to be deleted
	 * INSTALLED: to be installed
	 * 
	 * @param Status
	 * @return
	 */
	protected List<DownloadableInfo> getDownloadableListByStatus(PluginStatus Status) {
		return this.getDownloadableContent(trackerDoc.getRootElement().getChild(Status.getTagName()));
	}
	
	/**
	 * 
	 * Gets a list of themes by their status. CURRENT: currently installed
	 * DELETED: to be deleted INSTALL: to be installed
	 * 
	 * @param Status
	 * @return List of ThemeInfo objects
	 */
	protected List<ThemeInfo> getThemeListByStatus(PluginStatus Status) {
		return getThemeContent(trackerDoc.getRootElement().getChild(Status.getTagName()));
	}
	
	protected void addDownloadable(DownloadableInfo obj, PluginStatus Status) {
		Element Parent = trackerDoc.getRootElement().getChild(Status.getTagName());
		
		switch (obj.getType()) {
		case PLUGIN:
			addPlugin((PluginInfo) obj, Status);
			break;
		case THEME:
			addTheme((ThemeInfo) obj, Status);
			break;
		}
	}
	
	/**
	 * Adds the given ThemeInof object to the list of themes sharing the given
	 * status.
	 * 
	 * @param obj
	 * @param Status
	 */
	private void addTheme(ThemeInfo obj, PluginStatus Status) {
		Element ThemeParent = trackerDoc.getRootElement().getChild(Status.getTagName());
		Element Theme = getMatchingInfoObj(obj, Status);

		if (Theme != null) {
			Theme = updateBasicElement(obj, Theme);
			Theme.getChild(PluginXml.PLUGIN_LIST.getTag()).removeChildren(PluginXml.PLUGIN.getTag());

			for (PluginInfo plugin: obj.getPlugins()) {
				Element ThemePlugin = getMatchingInfoObj(plugin, Status); // XXX not sure this will get the right element
				ThemePlugin = updatePluginElement(plugin, ThemePlugin);
				Theme.getChild(PluginXml.PLUGIN_LIST.getTag()).addContent(ThemePlugin);
			}
		} else {
			Theme = createThemeContent(obj);
			ThemeParent.addContent(Theme);
			this.infoObjMap.put(infoMapKey(obj, Status), Theme);
			logger.info("Adding theme " + obj.getName() + " status " + Status.name());
		}
		write();
	}
	
	private Element updateBasicElement(DownloadableInfo obj, Element element) {
		if (!obj.getCategory().equals(Category.NONE.getCategoryText())) {
			element.getChild(categoryTag).setText(obj.getCategory());
		}
		element.getChild(cytoVersTag).setText(obj.getCytoscapeVersion());
		element.getChild(descTag).setText(obj.getDescription());

		if (element.getChild(pluginVersTag) != null) {
			element.getChild(pluginVersTag).setText(obj.getObjectVersion());
		} else {
			Element PluginVersion = new Element(pluginVersTag);
			element.addContent( PluginVersion.setText(obj.getObjectVersion()) );
		}
		
		if (element.getChild(PluginXml.RELEASE_DATE.getTag()) != null) { 
			element.getChild(PluginXml.RELEASE_DATE.getTag()).setText(obj.getReleaseDate());
		} else {
			Element ReleaseDate = new Element(PluginXml.RELEASE_DATE.getTag());
			element.addContent( ReleaseDate.setText(obj.getReleaseDate()) );
		}
		return element;
	}

	
	private Element updatePluginElement(PluginInfo obj, Element Plugin) {
		if (!obj.getName().equals(obj.getPluginClassName())) {
			Plugin.getChild(nameTag).setText(obj.getName());
		}
		Plugin = updateBasicElement(obj, Plugin);
		Plugin.getChild(installLocTag).setText(obj.getInstallLocation());
		
		if (obj.getPluginClassName() != null) {
			Plugin.getChild(classTag).setText(obj.getPluginClassName());
		}

		Plugin.removeChild(authorListTag);
		Element Authors = new Element(authorListTag);
		for(AuthorInfo ai: obj.getAuthors()) {
			Element Author = new Element(authorTag);
			Author.addContent( new Element(nameTag).setText(ai.getAuthor()) );
			Author.addContent( new Element(instTag).setText(ai.getInstitution()) );
			Authors.addContent(Author);
		}
		Plugin.addContent(Authors);

		return Plugin;
	}
	
	/**
	 * Adds the given PluginInfo object to the list of plugins sharing the given
	 * status.
	 * 
	 * @param obj
	 * @param Status
	 */
	private void addPlugin(PluginInfo obj, PluginStatus Status) {
		Element PluginParent = trackerDoc.getRootElement().getChild(Status.getTagName());
		
		Element PluginEl = getMatchingInfoObj(obj, Status);
		if (PluginEl != null) {
			updatePluginElement(obj, PluginEl);
			infoObjMap.put(this.infoMapKey(obj, Status), PluginEl);
		} else {
			Element NewPlugin = createPluginContent(obj);
			PluginParent.addContent(NewPlugin);
			infoObjMap.put(this.infoMapKey(obj, Status), NewPlugin);
			logger.info("Adding plugin " + obj.getName() + " status " + Status.name());
		}
		write();
	}

	/**
	 * Removes the given DownloadableInfo object from the list of plugins/themes sharing the
	 * given status.
	 * 
	 * @param obj
	 * @param Status
	 */
	protected void removeDownloadable(DownloadableInfo obj, PluginStatus Status) {
		Element Parent = trackerDoc.getRootElement().getChild(Status.getTagName());
		Element InfoObj = this.getMatchingInfoObj(obj, Status);
		if (InfoObj != null) {
			Parent.removeContent(InfoObj);
			infoObjMap.remove( this.infoMapKey(obj, Status) );
			
			if (obj.getType().equals(DownloadableType.THEME)) {
				ThemeInfo theme = (ThemeInfo) obj;
				for (PluginInfo themePlugin: theme.getPlugins()) {
					infoObjMap.remove( this.infoMapKey(themePlugin, Status) );
				}
			}
			logger.info("Removing plugin/theme " + obj.getName() + " status " + Status.name());
			write();
		}
	}
	

	/**
	 * Matches one of the following rule: 1. Plugin class name 2. uniqueID &&
	 * projUrl 3. Plugin specific Url (on the assumption that no two plugins can
	 * be downloaded from the same url)
	 * 
	 * @param Obj
	 * @param Tag
	 * @return
	 */
	// TODO this needs to go through both the pluginlist and the theme pluginlist
	// may need different method or two different lists as the first list will actually 
	// contain elements of the second (which is why there is an error)
	protected Element getMatchingInfoObj(DownloadableInfo Obj, PluginStatus Status) {
		String Key = this.infoMapKey(Obj, Status);
		if (Key != null) { // actually kinda ugly but easiest way to handle plugins w/o ids
			return this.infoObjMap.get(Key);
		} else {
			return null;
		}
	}

	private void createPluginTable() {
		this.infoObjMap = new HashMap<String, Element>();
		for (PluginStatus ps: PluginStatus.values()) {
			// A missing status tag should probably not happen because we check for that in
			// validateTrackerDoc(). Only if createCleanDoc() fails to produce something usable
			// will we run into problems here.
			Iterator<Element> iter = trackerDoc.getRootElement().getChild(ps.getTagName()).getChildren().iterator();

			while (iter.hasNext()) {
				Element el = iter.next();
				if (el.getName().equals(PluginXml.THEME.getTag())) {
					// make sure the theme gets added
					infoObjMap.put(infoMapKey(el, ps), el);
					
					Iterator<Element> ptIter = el.getChild(PluginXml.PLUGIN_LIST.getTag()).getChildren(PluginXml.PLUGIN.getTag()).iterator();
					while (ptIter.hasNext()) {
						Element pEl = ptIter.next();
						String key = this.infoMapKey(pEl, ps);
						// all theme plugins should be added too
						infoObjMap.put(key, pEl);
					}
				} else if (el.getName().equals(PluginXml.PLUGIN.getTag())) {
					String key = this.infoMapKey(el, ps); 
					infoObjMap.put(key, el);
				} else {
					logger.warn("Unknown tag in plugin tracker file: "+el.getName());
				}
			}
		}
	}
	
	// important to have the type in this key or a plugin and theme with the same id will appear to be the same object
	private String infoMapKey(DownloadableInfo Obj, PluginStatus Status) {
		return (Obj.getID() != null)? Obj.getID() + "_" + Obj.getType().value() + "_" + Obj.getDownloadableURL() + "_" + Status.getTagName(): null;
	}
	private String infoMapKey(Element el, PluginStatus Status) {
		return  el.getChildTextTrim(uniqueIdTag) + "_" +  el.getName() + "_" + el.getChildTextTrim(downloadUrlTag) + "_" + Status.getTagName();
	}

	
	/**
	 * Writes doc to file
	 */
	protected void write() {
		// before writing remove all corrupted elements
		for (Element e: corruptedElements) {
			e.detach();
		}
		
		try {
			XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
			FileWriter writer = null;
            try {
				writer = new FileWriter(installFile);
                out.output(trackerDoc, writer);
            }
            finally {
                if (writer != null) {
                    writer.close();
                }
            }
		} catch (IOException E) {
			logger.warn("Error writing plugin status file "+E.toString());
		}
	}

	/**
	 * @return True if one or more corrupted elements were found.
	 */
	public boolean hasCorruptedElements() {
		return this.corruptedElementsFound;
	}
	
	/**
	 * @return Total number of corrupted elements found in the file.
	 */
	public int getTotalCorruptedElements() {
		return corruptedElements.size();
	}
	
	/**
	 * Clears the list of elements and sets the found flag to false.
	 * This should only be called after checking the hadCorruptedElements()
	 * or getTotalCorruptedElements();
	 */
	public void clearCorruptedElements() {
		corruptedElementsFound = false;
		corruptedElements.clear();
	}
	
	private void addCorruptedElement(Element e) {
		logger.warn("** Adding corrupted element **");
		corruptedElements.add(e);
		corruptedElementsFound = true;
	}
	
	
	public String toString() {
		XMLOutputter out = new XMLOutputter(Format.getPrettyFormat());
		return out.outputString(trackerDoc);
	}
	
	
	/**
	 * Deletes the tracker file. This is currently never used outside of tests.
	 */
	protected void delete() {
		if (installFile.exists())
			installFile.delete();
	}
	
	/*
	 * Set up the object with all the basic fields filled in
	 */
	private DownloadableInfo createBasicObject(Element e, DownloadableType Type) {
		DownloadableInfo Info = null;
		
		if (e.getChildren().size() > 0 && e.getChild(uniqueIdTag) != null) {
			switch (Type) {
			case PLUGIN:
				Info = new PluginInfo(e.getChildTextTrim(uniqueIdTag));
				break;
			case THEME:
				Info = new ThemeInfo(e.getChildTextTrim(uniqueIdTag));
				break;
			}
	
			if (e.getChild(nameTag) == null ||
				e.getChild(descTag) == null ||
				e.getChild(cytoVersTag) == null ||
				e.getChild(urlTag) == null ||
				e.getChild(downloadUrlTag) == null) {
				return null;
			} 
				
				Info.setName(e.getChildTextTrim(nameTag));
				Info.setDescription(e.getChildTextTrim(descTag));
				Info.addCytoscapeVersion(e.getChildTextTrim(cytoVersTag));
				Info.setObjectUrl(e.getChildTextTrim(urlTag));
				Info.setDownloadableURL(e.getChildTextTrim(downloadUrlTag));
	
				if (e.getChild(categoryTag) != null)
					Info.setCategory(e.getChildTextTrim(categoryTag));
				if (e.getChild(PluginXml.RELEASE_DATE.getTag()) != null)
					Info.setReleaseDate(e.getChildTextTrim(PluginXml.RELEASE_DATE.getTag()));
		}
		
		return Info;
	}
	
	
	// Not sure this is the right way to do this but for now...
	private List<ThemeInfo> getThemeContent(Element ThemeParentTag) {
		List<ThemeInfo> Content = new ArrayList<ThemeInfo>();
		List<Element> Themes = ThemeParentTag.getChildren(PluginXml.THEME.getTag());
		
		for (Element CurrentTheme: Themes) {
			ThemeInfo themeInfo = (ThemeInfo) createBasicObject(CurrentTheme, DownloadableType.THEME);
			if (themeInfo == null || 
				CurrentTheme.getChild(PluginXml.THEME_VERSION.getTag()) == null ||
				CurrentTheme.getChild(PluginXml.PLUGIN_LIST.getTag()) == null ||
				CurrentTheme.getChild(PluginXml.PLUGIN_LIST.getTag()).getChildren(PluginXml.PLUGIN.getTag()).size() <= 0) {
				this.addCorruptedElement(CurrentTheme);
				continue;
			}

			themeInfo.setObjectVersion( Double.valueOf(CurrentTheme.getChildTextTrim(PluginXml.THEME_VERSION.getTag())) );
			// add plugins
			Iterator<Element> pluginI = CurrentTheme.getChild(PluginXml.PLUGIN_LIST.getTag()).getChildren(PluginXml.PLUGIN.getTag()).iterator();
			while (pluginI.hasNext()) {
				PluginInfo pluginInfo = createPluginObject(pluginI.next());
				if (pluginInfo == null) { 
					this.addCorruptedElement(CurrentTheme);
					break;
				}
				pluginInfo.setParent(themeInfo);
				themeInfo.addPlugin(pluginInfo);
			}
			// if it wasn't corrupted
			if (!corruptedElements.contains(CurrentTheme)) {
				Content.add(themeInfo);
			}
		}
		return Content;
	}
	
	// TODO PluginFileReader does much the same stuff, need to merge the two (??
	// maybe)
	/*
	 * Takes a list of elemnts, creates the PluginInfo object for each and
	 * returns list of objects
	 */
	private List<PluginInfo> getPluginContent(Element PluginParentTag) {
		List<PluginInfo> Content = new ArrayList<PluginInfo>();

		List<Element> Plugins = PluginParentTag.getChildren(pluginTag);

		for (Element CurrentPlugin : Plugins) {
			PluginInfo Info = createPluginObject(CurrentPlugin);
			if (Info == null) {
				// remove the Element and move on
				this.addCorruptedElement(CurrentPlugin);
				continue;
			}
			Content.add(Info);
		}
		return Content;
	}
	
	/**
	 * Gets all downloadable objects currently available.
	 * @param Parent
	 * @return
	 */
	private List<DownloadableInfo> getDownloadableContent(Element Parent) {
		List<DownloadableInfo> Content = new ArrayList<DownloadableInfo>();
		
		Content.addAll( getPluginContent(Parent) );
		Content.addAll( getThemeContent(Parent) );
		
		return Content;
	}
	
	

	/*
	 * Create the PluginInfo object from a <plugin>...</plugin> tree 
	 */
	private PluginInfo createPluginObject(Element PluginElement) {
    	PluginInfo Info = (PluginInfo) createBasicObject(PluginElement, DownloadableType.PLUGIN);

    	if (Info == null ||
    		PluginElement.getChildren().size() <= 0 ||
    		PluginElement.getChild(classTag) == null ||
    		PluginElement.getChild(installLocTag) == null ||
    		PluginElement.getChild(pluginVersTag) == null ||
    		PluginElement.getChild(fileTypeTag) == null ||
    		PluginElement.getChild(fileListTag) == null) {
    		// bad xml, remove it and return null
    		//PluginElement.getParent().removeContent(PluginElement);
    		return null;
    	} 
    	
    	Info.setPluginClassName(PluginElement.getChildTextTrim(classTag));
    	Info.setInstallLocation(PluginElement.getChildTextTrim(installLocTag));
		Info.setObjectVersion(Double.valueOf(PluginElement.getChildTextTrim(pluginVersTag)));
    	Info.setProjectUrl(PluginElement.getChildTextTrim(projUrlTag));

		
		// set file type
		String FileType = PluginElement.getChildTextTrim(fileTypeTag);
		if (FileType.equalsIgnoreCase(PluginInfo.FileType.JAR.toString())) {
			Info.setFiletype(PluginInfo.FileType.JAR);
		} else if (FileType.equalsIgnoreCase(PluginInfo.FileType.ZIP.toString())) {
			Info.setFiletype(PluginInfo.FileType.ZIP);
		}
		
		// add plugin files
		List<Element> Files = PluginElement.getChild(fileListTag).getChildren(fileTag);
		for (Element File : Files) {
			Info.addFileName(File.getTextTrim());
		}

		// add plugin authors
		if (PluginElement.getChild(authorListTag) != null) {
			List<Element> Authors = PluginElement.getChild(authorListTag)
			                                     .getChildren(authorTag);
			for (Element Author : Authors) {
				Info.addAuthor(Author.getChildTextTrim(nameTag),
				               Author.getChildTextTrim(instTag));
			}
		}

		Info = PluginFileReader.addLicense(Info, PluginElement);
    	
		return Info;
	}

	
	private Element createBasicContent(DownloadableInfo obj, Element e) {
		e.addContent(new Element(uniqueIdTag).setText(obj.getID()));
		e.addContent(new Element(nameTag).setText(obj.getName()));
		e.addContent(new Element(descTag).setText(obj.getDescription()));
		e.addContent(new Element(cytoVersTag).setText(obj.getCytoscapeVersion()));
		e.addContent(new Element(urlTag).setText(obj.getObjectUrl()));
		e.addContent(new Element(downloadUrlTag).setText(obj.getDownloadableURL()));
		e.addContent(new Element(categoryTag).setText(obj.getCategory()));
		e.addContent(new Element(PluginXml.RELEASE_DATE.getTag()).setText(obj.getReleaseDate()));
		
		return e;
	}
	
	private Element createThemeContent(ThemeInfo obj) {
		Element Theme = new Element(PluginXml.THEME.getTag());
		
		Theme = createBasicContent(obj, Theme);
		Theme.addContent(new Element(PluginXml.THEME_VERSION.getTag()).setText(obj.getObjectVersion()));
		
		Element PluginList = new Element(PluginXml.PLUGIN_LIST.getTag());
		for (PluginInfo plugin: obj.getPlugins()) {
			PluginList.addContent(createPluginContent(plugin));
		}
		Theme.addContent(PluginList);
		
		return Theme;
	}
	
	/*
	 * Create the plugin tag with all the appropriate tags for the PluginInfo
	 * object
	 */
	private Element createPluginContent(PluginInfo obj) {
		Element Plugin = new Element(pluginTag);

		Plugin = createBasicContent(obj, Plugin);
		
		Plugin.addContent(new Element(pluginVersTag).setText(obj.getObjectVersion()));
		Plugin.addContent(new Element(classTag).setText(obj.getPluginClassName()));
		Plugin.addContent(new Element(projUrlTag).setText(obj.getProjectUrl()));
		Plugin.addContent(new Element(fileTypeTag).setText(obj.getFileType().toString()));
		Plugin.addContent(new Element(installLocTag).setText(obj.getInstallLocation()));
		
		// license
		Element License = new Element(licenseTag);
		License.addContent( new Element("text").setText(obj.getLicenseText()) );
		Plugin.addContent(License);
		
		// authors
		Element AuthorList = new Element(authorListTag);
		for (AuthorInfo CurrentAuthor : obj.getAuthors()) {
			Element Author = new Element(authorTag);
			Author.addContent(new Element(nameTag).setText(CurrentAuthor.getAuthor()));
			Author.addContent(new Element(instTag).setText(CurrentAuthor.getInstitution()));
			AuthorList.addContent(Author);
		}
		Plugin.addContent(AuthorList);

		// files
		Element FileList = new Element(fileListTag);
		for (String FileName : obj.getFileList()) {
			FileList.addContent(new Element(fileTag).setText(FileName));
		}
		Plugin.addContent(FileList);

		return Plugin;
	}

	// XML Tags to prevent misspelling issues, PluginFileReader uses most of the
	// same tags, the xml needs to stay consistent
	private String cytoVersTag = "cytoscapeVersion";

	private String nameTag = PluginXml.NAME.getTag();

	private String descTag = PluginXml.DESCRIPTION.getTag();
	
	private String classTag = PluginXml.CLASS_NAME.getTag();
	
	private String pluginVersTag = PluginXml.PLUGIN_VERSION.getTag();
	
	private String urlTag = PluginXml.URL.getTag();
	
	private String projUrlTag = PluginXml.PROJECT_URL.getTag();
	
	private String downloadUrlTag = PluginXml.DOWNLOAD_URL.getTag();
	
	private String categoryTag = PluginXml.CATEGORY.getTag();
	
	private String fileListTag = PluginXml.FILE_LIST.getTag();
	
	private String fileTag = PluginXml.FILE.getTag();
	
	private String pluginListTag = PluginXml.PLUGIN_LIST.getTag();
	
	private String pluginTag = PluginXml.PLUGIN.getTag();
	
	private String authorListTag = PluginXml.AUTHOR_LIST.getTag();
	
	private String authorTag = PluginXml.AUTHOR.getTag();
	
	private String instTag = PluginXml.INSTITUTION.getTag();
	
	private String uniqueIdTag = PluginXml.UNIQUE_ID.getTag();
	
	private String fileTypeTag = PluginXml.FILE_TYPE.getTag();
	
	private String licenseTag = PluginXml.LICENSE.getTag();
	
	private String installLocTag = PluginXml.INSTALL_LOCATION.getTag();
}

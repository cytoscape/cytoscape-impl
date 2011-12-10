/*
 File: AppTracker.java 
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

package org.cytoscape.app.internal;

import org.cytoscape.app.internal.AppInfo.AuthorInfo;

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


public class AppTracker {
	private static final Logger logger = LoggerFactory.getLogger(AppTracker.class);

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
	protected AppTracker(File Dir, String FileName) throws IOException, TrackerException {
		installFile = new File(Dir, FileName);
		init();
	}
	
	protected AppTracker(File file) throws IOException, TrackerException {
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
		
		if (AppManager.usingWebstartManager()) { 
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
				throw new TrackerException("App tracking file is corrupted.  Please reinstall your apps. Deleting " + installFile.getAbsolutePath(), jde);
			} finally {
				createAppTable();
			}
		} else {
			createCleanDoc();
			createAppTable();
		}
	}

	/**
	 * Will throw an exception if the tracker document doesn't contain the necessary
	 * elements. The goal is to force a dummy app table to be created.  
	 */
	private void validateTrackerDoc() {
		for (AppStatus ps: AppStatus.values()) {
			// several of these calls could also produce an NPE
			Iterator<Element> iter = trackerDoc.getRootElement().getChild(ps.getTagName()).getChildren().iterator();
			if ( iter == null )
				throw new NullPointerException("corrupted tracker file");

		}
	}

	private void createCleanDoc() {
		logger.warn("App tracker file: " + installFile.getAbsolutePath());
		trackerDoc = new Document();
		trackerDoc.setRootElement(new Element("CytoscapeApp"));
		trackerDoc.getRootElement().addContent(new Element(AppStatus.CURRENT.getTagName()));
		trackerDoc.getRootElement().addContent(new Element(AppStatus.INSTALL.getTagName()));
		trackerDoc.getRootElement().addContent(new Element(AppStatus.DELETE.getTagName()));
		write();
	}
	
	/* In order to maintain a list of apps that does not duplicate entries due to lack of a unique identifier
	 * all entries that lack an unique id will be purged and expected to re-register.  User will see no difference.
	 */
	private void removeMissingIdEntries() {
		List<Element> Apps = trackerDoc.getRootElement().getChild(AppStatus.CURRENT.getTagName()).getChildren(appTag);
		List<Element> AppsToRemove = new ArrayList<Element>();
		
		for (Element app: Apps) {
			if (app.getChild(uniqueIdTag) == null ||
				app.getChild(uniqueIdTag).getTextTrim().length() <= 0) 
				AppsToRemove.add(app);
		}
		
		for (Element child: AppsToRemove) 
			trackerDoc.getRootElement().getChild(AppStatus.CURRENT.getTagName()).removeContent(child);
		
	}
	
	/**
	 * Gets a list of apps by their status. CURRENT: currently installed
	 * DELETED: to be deleted INSTALL: to be installed
	 * 
	 * @param Status
	 * @return List of AppInfo objects
	 */
	protected List<AppInfo> getAppListByStatus(AppStatus Status) {
		return getAppContent(trackerDoc.getRootElement().getChild(Status.getTagName()));
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
	protected List<DownloadableInfo> getDownloadableListByStatus(AppStatus Status) {
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
	protected List<ThemeInfo> getThemeListByStatus(AppStatus Status) {
		return getThemeContent(trackerDoc.getRootElement().getChild(Status.getTagName()));
	}
	
	protected void addDownloadable(DownloadableInfo obj, AppStatus Status) {
		Element Parent = trackerDoc.getRootElement().getChild(Status.getTagName());
		
		switch (obj.getType()) {
		case APP:
			addApp((AppInfo) obj, Status);
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
	private void addTheme(ThemeInfo obj, AppStatus Status) {
		Element ThemeParent = trackerDoc.getRootElement().getChild(Status.getTagName());
		Element Theme = getMatchingInfoObj(obj, Status);

		if (Theme != null) {
			Theme = updateBasicElement(obj, Theme);
			Theme.getChild(AppXml.APP_LIST.getTag()).removeChildren(AppXml.APP.getTag());

			for (AppInfo app: obj.getApps()) {
				Element ThemeApp = getMatchingInfoObj(app, Status); // XXX not sure this will get the right element
				ThemeApp = updateAppElement(app, ThemeApp);
				Theme.getChild(AppXml.APP_LIST.getTag()).addContent(ThemeApp);
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

		if (element.getChild(appVersTag) != null) {
			element.getChild(appVersTag).setText(obj.getObjectVersion());
		} else {
			Element AppVersion = new Element(appVersTag);
			element.addContent( AppVersion.setText(obj.getObjectVersion()) );
		}
		
		if (element.getChild(AppXml.RELEASE_DATE.getTag()) != null) { 
			element.getChild(AppXml.RELEASE_DATE.getTag()).setText(obj.getReleaseDate());
		} else {
			Element ReleaseDate = new Element(AppXml.RELEASE_DATE.getTag());
			element.addContent( ReleaseDate.setText(obj.getReleaseDate()) );
		}
		return element;
	}

	
	private Element updateAppElement(AppInfo obj, Element App) {
		if (!obj.getName().equals(obj.getAppClassName())) {
			App.getChild(nameTag).setText(obj.getName());
		}
		App = updateBasicElement(obj, App);
		App.getChild(installLocTag).setText(obj.getInstallLocation());
		
		if (obj.getAppClassName() != null) {
			App.getChild(classTag).setText(obj.getAppClassName());
		}

		App.removeChild(authorListTag);
		Element Authors = new Element(authorListTag);
		for(AuthorInfo ai: obj.getAuthors()) {
			Element Author = new Element(authorTag);
			Author.addContent( new Element(nameTag).setText(ai.getAuthor()) );
			Author.addContent( new Element(instTag).setText(ai.getInstitution()) );
			Authors.addContent(Author);
		}
		App.addContent(Authors);

		return App;
	}
	
	/**
	 * Adds the given AppInfo object to the list of apps sharing the given
	 * status.
	 * 
	 * @param obj
	 * @param Status
	 */
	private void addApp(AppInfo obj, AppStatus Status) {
		Element AppParent = trackerDoc.getRootElement().getChild(Status.getTagName());
		
		Element AppEl = getMatchingInfoObj(obj, Status);
		if (AppEl != null) {
			updateAppElement(obj, AppEl);
			infoObjMap.put(this.infoMapKey(obj, Status), AppEl);
		} else {
			Element NewApp = createAppContent(obj);
			AppParent.addContent(NewApp);
			infoObjMap.put(this.infoMapKey(obj, Status), NewApp);
			logger.info("Adding app " + obj.getName() + " status " + Status.name());
		}
		write();
	}

	/**
	 * Removes the given DownloadableInfo object from the list of apps/themes sharing the
	 * given status.
	 * 
	 * @param obj
	 * @param Status
	 */
	protected void removeDownloadable(DownloadableInfo obj, AppStatus Status) {
		Element Parent = trackerDoc.getRootElement().getChild(Status.getTagName());
		Element InfoObj = this.getMatchingInfoObj(obj, Status);
		if (InfoObj != null) {
			Parent.removeContent(InfoObj);
			infoObjMap.remove( this.infoMapKey(obj, Status) );
			
			if (obj.getType().equals(DownloadableType.THEME)) {
				ThemeInfo theme = (ThemeInfo) obj;
				for (AppInfo themeApp: theme.getApps()) {
					infoObjMap.remove( this.infoMapKey(themeApp, Status) );
				}
			}
			logger.info("Removing app/theme " + obj.getName() + " status " + Status.name());
			write();
		}
	}
	

	/**
	 * Matches one of the following rule: 1. App class name 2. uniqueID &&
	 * projUrl 3. App specific Url (on the assumption that no two apps can
	 * be downloaded from the same url)
	 * 
	 * @param Obj
	 * @param Tag
	 * @return
	 */
	// TODO this needs to go through both the applist and the theme applist
	// may need different method or two different lists as the first list will actually 
	// contain elements of the second (which is why there is an error)
	protected Element getMatchingInfoObj(DownloadableInfo Obj, AppStatus Status) {
		String Key = this.infoMapKey(Obj, Status);
		if (Key != null) { // actually kinda ugly but easiest way to handle apps w/o ids
			return this.infoObjMap.get(Key);
		} else {
			return null;
		}
	}

	private void createAppTable() {
		this.infoObjMap = new HashMap<String, Element>();
		for (AppStatus ps: AppStatus.values()) {
			// A missing status tag should probably not happen because we check for that in
			// validateTrackerDoc(). Only if createCleanDoc() fails to produce something usable
			// will we run into problems here.
			Iterator<Element> iter = trackerDoc.getRootElement().getChild(ps.getTagName()).getChildren().iterator();

			while (iter.hasNext()) {
				Element el = iter.next();
				if (el.getName().equals(AppXml.THEME.getTag())) {
					// make sure the theme gets added
					infoObjMap.put(infoMapKey(el, ps), el);
					
					Iterator<Element> ptIter = el.getChild(AppXml.APP_LIST.getTag()).getChildren(AppXml.APP.getTag()).iterator();
					while (ptIter.hasNext()) {
						Element pEl = ptIter.next();
						String key = this.infoMapKey(pEl, ps);
						// all theme apps should be added too
						infoObjMap.put(key, pEl);
					}
				} else if (el.getName().equals(AppXml.APP.getTag())) {
					String key = this.infoMapKey(el, ps); 
					infoObjMap.put(key, el);
				} else {
					logger.warn("Unknown tag in app tracker file: "+el.getName());
				}
			}
		}
	}
	
	// important to have the type in this key or a app and theme with the same id will appear to be the same object
	private String infoMapKey(DownloadableInfo Obj, AppStatus Status) {
		return (Obj.getID() != null)? Obj.getID() + "_" + Obj.getType().value() + "_" + Obj.getDownloadableURL() + "_" + Status.getTagName(): null;
	}
	private String infoMapKey(Element el, AppStatus Status) {
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
			logger.warn("Error writing app status file "+E.toString());
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
			case APP:
				Info = new AppInfo(e.getChildTextTrim(uniqueIdTag));
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
				if (e.getChild(AppXml.RELEASE_DATE.getTag()) != null)
					Info.setReleaseDate(e.getChildTextTrim(AppXml.RELEASE_DATE.getTag()));
		}
		
		return Info;
	}
	
	
	// Not sure this is the right way to do this but for now...
	private List<ThemeInfo> getThemeContent(Element ThemeParentTag) {
		List<ThemeInfo> Content = new ArrayList<ThemeInfo>();
		List<Element> Themes = ThemeParentTag.getChildren(AppXml.THEME.getTag());
		
		for (Element CurrentTheme: Themes) {
			ThemeInfo themeInfo = (ThemeInfo) createBasicObject(CurrentTheme, DownloadableType.THEME);
			if (themeInfo == null || 
				CurrentTheme.getChild(AppXml.THEME_VERSION.getTag()) == null ||
				CurrentTheme.getChild(AppXml.APP_LIST.getTag()) == null ||
				CurrentTheme.getChild(AppXml.APP_LIST.getTag()).getChildren(AppXml.APP.getTag()).size() <= 0) {
				this.addCorruptedElement(CurrentTheme);
				continue;
			}

			themeInfo.setObjectVersion( CurrentTheme.getChildTextTrim(AppXml.THEME_VERSION.getTag()));
			// add apps
			Iterator<Element> appI = CurrentTheme.getChild(AppXml.APP_LIST.getTag()).getChildren(AppXml.APP.getTag()).iterator();
			while (appI.hasNext()) {
				AppInfo appInfo = createAppObject(appI.next());
				if (appInfo == null) { 
					this.addCorruptedElement(CurrentTheme);
					break;
				}
				appInfo.setParent(themeInfo);
				themeInfo.addApp(appInfo);
			}
			// if it wasn't corrupted
			if (!corruptedElements.contains(CurrentTheme)) {
				Content.add(themeInfo);
			}
		}
		return Content;
	}
	
	// TODO AppFileReader does much the same stuff, need to merge the two (??
	// maybe)
	/*
	 * Takes a list of elemnts, creates the AppInfo object for each and
	 * returns list of objects
	 */
	private List<AppInfo> getAppContent(Element AppParentTag) {
		List<AppInfo> Content = new ArrayList<AppInfo>();

		List<Element> Apps = AppParentTag.getChildren(appTag);

		for (Element CurrentApp : Apps) {
			AppInfo Info = createAppObject(CurrentApp);
			if (Info == null) {
				// remove the Element and move on
				this.addCorruptedElement(CurrentApp);
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
		
		Content.addAll( getAppContent(Parent) );
		Content.addAll( getThemeContent(Parent) );
		
		return Content;
	}
	
	

	/*
	 * Create the AppInfo object from a <app>...</app> tree 
	 */
	private AppInfo createAppObject(Element AppElement) {
    	AppInfo Info = (AppInfo) createBasicObject(AppElement, DownloadableType.APP);

    	if (Info == null ||
    		AppElement.getChildren().size() <= 0 ||
    		AppElement.getChild(classTag) == null ||
    		AppElement.getChild(installLocTag) == null ||
    		AppElement.getChild(appVersTag) == null ||
    		AppElement.getChild(fileTypeTag) == null ||
    		AppElement.getChild(fileListTag) == null) {
    		// bad xml, remove it and return null
    		//AppElement.getParent().removeContent(AppElement);
    		return null;
    	} 
    	
    	Info.setAppClassName(AppElement.getChildTextTrim(classTag));
    	Info.setInstallLocation(AppElement.getChildTextTrim(installLocTag));
		Info.setObjectVersion(AppElement.getChildTextTrim(appVersTag));
    	Info.setProjectUrl(AppElement.getChildTextTrim(projUrlTag));

		
		// set file type
		String FileType = AppElement.getChildTextTrim(fileTypeTag);
		if (FileType.equalsIgnoreCase(AppInfo.FileType.JAR.toString())) {
			Info.setFiletype(AppInfo.FileType.JAR);
		} else if (FileType.equalsIgnoreCase(AppInfo.FileType.ZIP.toString())) {
			Info.setFiletype(AppInfo.FileType.ZIP);
		}
		
		// add app files
		List<Element> Files = AppElement.getChild(fileListTag).getChildren(fileTag);
		for (Element File : Files) {
			Info.addFileName(File.getTextTrim());
		}

		// add app authors
		if (AppElement.getChild(authorListTag) != null) {
			List<Element> Authors = AppElement.getChild(authorListTag)
			                                     .getChildren(authorTag);
			for (Element Author : Authors) {
				Info.addAuthor(Author.getChildTextTrim(nameTag),
				               Author.getChildTextTrim(instTag));
			}
		}

		Info = AppFileReader.addLicense(Info, AppElement);
    	
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
		e.addContent(new Element(AppXml.RELEASE_DATE.getTag()).setText(obj.getReleaseDate()));
		
		return e;
	}
	
	private Element createThemeContent(ThemeInfo obj) {
		Element Theme = new Element(AppXml.THEME.getTag());
		
		Theme = createBasicContent(obj, Theme);
		Theme.addContent(new Element(AppXml.THEME_VERSION.getTag()).setText(obj.getObjectVersion()));
		
		Element AppList = new Element(AppXml.APP_LIST.getTag());
		for (AppInfo app: obj.getApps()) {
			AppList.addContent(createAppContent(app));
		}
		Theme.addContent(AppList);
		
		return Theme;
	}
	
	/*
	 * Create the app tag with all the appropriate tags for the AppInfo
	 * object
	 */
	private Element createAppContent(AppInfo obj) {
		Element App = new Element(appTag);

		App = createBasicContent(obj, App);
		
		App.addContent(new Element(appVersTag).setText(obj.getObjectVersion()));
		App.addContent(new Element(classTag).setText(obj.getAppClassName()));
		App.addContent(new Element(projUrlTag).setText(obj.getProjectUrl()));
		App.addContent(new Element(fileTypeTag).setText(obj.getFileType().toString()));
		App.addContent(new Element(installLocTag).setText(obj.getInstallLocation()));
		
		// license
		Element License = new Element(licenseTag);
		License.addContent( new Element("text").setText(obj.getLicenseText()) );
		App.addContent(License);
		
		// authors
		Element AuthorList = new Element(authorListTag);
		for (AuthorInfo CurrentAuthor : obj.getAuthors()) {
			Element Author = new Element(authorTag);
			Author.addContent(new Element(nameTag).setText(CurrentAuthor.getAuthor()));
			Author.addContent(new Element(instTag).setText(CurrentAuthor.getInstitution()));
			AuthorList.addContent(Author);
		}
		App.addContent(AuthorList);

		// files
		Element FileList = new Element(fileListTag);
		for (String FileName : obj.getFileList()) {
			FileList.addContent(new Element(fileTag).setText(FileName));
		}
		App.addContent(FileList);

		return App;
	}

	// XML Tags to prevent misspelling issues, AppFileReader uses most of the
	// same tags, the xml needs to stay consistent
	private String cytoVersTag = "cytoscapeVersion";

	private String nameTag = AppXml.NAME.getTag();

	private String descTag = AppXml.DESCRIPTION.getTag();
	
	private String classTag = AppXml.CLASS_NAME.getTag();
	
	private String appVersTag = AppXml.APP_VERSION.getTag();
	
	private String urlTag = AppXml.URL.getTag();
	
	private String projUrlTag = AppXml.PROJECT_URL.getTag();
	
	private String downloadUrlTag = AppXml.DOWNLOAD_URL.getTag();
	
	private String categoryTag = AppXml.CATEGORY.getTag();
	
	private String fileListTag = AppXml.FILE_LIST.getTag();
	
	private String fileTag = AppXml.FILE.getTag();
	
	private String appListTag = AppXml.APP_LIST.getTag();
	
	private String appTag = AppXml.APP.getTag();
	
	private String authorListTag = AppXml.AUTHOR_LIST.getTag();
	
	private String authorTag = AppXml.AUTHOR.getTag();
	
	private String instTag = AppXml.INSTITUTION.getTag();
	
	private String uniqueIdTag = AppXml.UNIQUE_ID.getTag();
	
	private String fileTypeTag = AppXml.FILE_TYPE.getTag();
	
	private String licenseTag = AppXml.LICENSE.getTag();
	
	private String installLocTag = AppXml.INSTALL_LOCATION.getTag();
}

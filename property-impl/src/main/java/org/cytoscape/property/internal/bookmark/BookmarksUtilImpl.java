/*
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
package org.cytoscape.property.internal.bookmark;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.property.bookmark.Attribute;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.property.bookmark.Category;
import org.cytoscape.property.bookmark.DataSource;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.datasource.DefaultDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Utility methods for getting entries in the bookmark object.
 * 
 * @author kono
 * 
 */
public class BookmarksUtilImpl implements BookmarksUtil {
	
	//Map to keep information of new bookmarks converted to DataSource for dataSourceManager
	private  Map<DataSource, org.cytoscape.io.datasource.DataSource> dataSourceMap;
	private static final Logger logger = LoggerFactory.getLogger(BookmarksUtilImpl.class);
	private CyServiceRegistrar register;
	
	public BookmarksUtilImpl( final CyServiceRegistrar register){

		this.register =register;
		this.dataSourceMap = new HashMap<DataSource, org.cytoscape.io.datasource.DataSource>();
		
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cytoscape.property.internal.bookmark.BookmarksUtil#getDataSourceList
	 * (java.lang.String, java.util.List)
	 */
	public List<DataSource> getDataSourceList(String categoryName,
			List<Category> categoryList) {
		final Category targetCat = getCategory(categoryName, categoryList);

		if (targetCat != null) {
			return extractDataSources(targetCat);
		} else {
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cytoscape.property.internal.bookmark.BookmarksUtil#getCategory(java
	 * .lang.String, java.util.List)
	 */
	public Category getCategory(String categoryName, List<Category> categoryList) {
		Category result = null;

		for (Category cat : categoryList) {
			if (cat.getName().equals(categoryName)) {
				result = cat;

				break;
			} else {
				List<Category> subCategories = extractCategory(cat);

				if ((subCategories.size() != 0) && (result == null)) {
					result = getCategory(categoryName, subCategories);
				}
			}

			if (result != null) {
				break;
			}
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cytoscape.property.internal.bookmark.BookmarksUtil#getAttribute(org
	 * .cytoscape.properties.bookmark.DataSource, java.lang.String)
	 */
	public String getAttribute(DataSource source, String attrName) {
		List<Attribute> attrs = source.getAttribute();

		for (Attribute attr : attrs) {
			if (attrName.equals(attr.getName())) {
				return attr.getContent();
			}
		}

		return null;
	}

	private List<DataSource> extractDataSources(Category cat) {
		final List<Object> entries = cat.getCategoryOrDataSource();
		final List<DataSource> datasourceList = new ArrayList<DataSource>();

		for (Object obj : entries) {
			if (obj.getClass() == DataSource.class) {
				datasourceList.add((DataSource) obj);
			}
		}

		return datasourceList;
	}

	private List<Category> extractCategory(Category cat) {
		final List<Object> entries = cat.getCategoryOrDataSource();
		final List<Category> categoryList = new ArrayList<Category>();

		for (Object obj : entries) {
			if (obj.getClass() == Category.class) {
				categoryList.add((Category) obj);
			}
		}

		return categoryList;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cytoscape.property.internal.bookmark.BookmarksUtil#saveBookmark(org
	 * .cytoscape.properties.bookmark.Bookmarks, java.lang.String,
	 * org.cytoscape.properties.bookmark.DataSource)
	 */
	public void saveBookmark(Bookmarks pBookmarks, String pCategoryName,
			DataSource pDataSource) {		
		saveBookmark(pBookmarks, pCategoryName, pDataSource, "Example");
	}
	
	
	public 	void saveBookmark(Bookmarks pBookmarks, String pCategoryName,
			DataSource pDataSource, String pProvider){
		if (pBookmarks == null) {
			pBookmarks = new Bookmarks();
		}

		List<Category> theCategoryList = pBookmarks.getCategory();

		// if the category does not exist, create it
		if (theCategoryList.size() == 0) {
			Category theCategory = new Category();
			theCategory.setName(pCategoryName);
			theCategoryList.add(theCategory);
		}

		Category theCategory = getCategory(pCategoryName, theCategoryList);

		if (theCategory == null) {
			Category newCategory = new Category();
			newCategory.setName(pCategoryName);
			theCategoryList.add(newCategory);
			theCategory = newCategory;
		}

		List<Object> theObjList = theCategory.getCategoryOrDataSource();

		if(!theObjList.contains(pDataSource))
			theObjList.add(pDataSource);
		
		org.cytoscape.io.datasource.DataSource data = convertToDataSource(pBookmarks,pCategoryName,pDataSource, pProvider);

		if(data != null && !dataSourceMap.containsKey(pDataSource))
		{
			
			register.registerService(data, org.cytoscape.io.datasource.DataSource.class, new Properties());
			dataSourceMap.put(pDataSource, data);
		}
		
	}
	
	
	public org.cytoscape.io.datasource.DataSource convertToDataSource(Bookmarks pBookmarks, 
			String pCategoryName,DataSource pDataSource){		
		return convertToDataSource(pBookmarks,pCategoryName, pDataSource, "Example");
	}

	
	public org.cytoscape.io.datasource.DataSource convertToDataSource(Bookmarks pBookmarks, 
			String pCategoryName, DataSource pDataSource, String pProvider){
		
		final String location = pDataSource.getHref();
		final String name = pDataSource.getName();
		final String description = "From Bookmarks";
		//final String provider = "Example";
		DataCategory dataType;
		URL url = null;
		
		if(pCategoryName.contentEquals("network") )
		{
			dataType = DataCategory.NETWORK;
		}else if(pCategoryName.contentEquals("table") ){
			dataType = DataCategory.TABLE;
		}else {
			return null;
		}
		
		try {
			url = new URL(location);
		} catch (MalformedURLException e) {
			logger.warn("Bookmark file coniatin invalid URL: " + location);
			return null;
		}

		return (new DefaultDataSource(name, pProvider, description, dataType, url));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cytoscape.property.internal.bookmark.BookmarksUtil#deleteBookmark
	 * (org.cytoscape.properties.bookmark.Bookmarks, java.lang.String,
	 * org.cytoscape.properties.bookmark.DataSource)
	 */
	public boolean deleteBookmark(Bookmarks pBookmarks, String pCategoryName,
			DataSource pDataSource) {
		org.cytoscape.io.datasource.DataSource data = null ;
		if (!containsBookmarks(pBookmarks, pCategoryName, pDataSource)) {
			return false;
		}

		List<Category> theCategoryList = pBookmarks.getCategory();
		Category theCategory = getCategory(pCategoryName, theCategoryList);

		List<Object> theObjList = theCategory.getCategoryOrDataSource();

		for (int i = 0; i < theObjList.size(); i++) {
			Object obj = theObjList.get(i);

			if (obj instanceof DataSource) {
				DataSource theDataSource = (DataSource) obj;

				if (theDataSource.getName().equalsIgnoreCase(
						pDataSource.getName())) {
					
					if(dataSourceMap.containsKey(pDataSource))
					{
						
						register.unregisterService(dataSourceMap.get(pDataSource), org.cytoscape.io.datasource.DataSource.class);
						dataSourceMap.remove(pDataSource);
					}
					theObjList.remove(i);
				}
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.cytoscape.property.internal.bookmark.BookmarksUtil#isInBookmarks(
	 * org.cytoscape.properties.bookmark.Bookmarks, java.lang.String,
	 * org.cytoscape.properties.bookmark.DataSource)
	 */
	public boolean containsBookmarks(Bookmarks pBookmarks, String pCategoryName,
			DataSource pDataSource) {
		if (pBookmarks == null) {
			return false;
		}

		List<DataSource> theDataSources = getDataSourceList(pCategoryName,
				pBookmarks.getCategory());

		if ((theDataSources == null) || (theDataSources.size() == 0)) {
			return false;
		}

		for (DataSource theDataSource : theDataSources) {
			if (theDataSource.getName().equalsIgnoreCase(pDataSource.getName())) {
				return true;
			}
		}

		return false;
	}

}

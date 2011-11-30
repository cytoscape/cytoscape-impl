package org.cytoscape.work.internal.tunables;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;

import org.cytoscape.property.CyProperty;
import org.cytoscape.property.bookmark.Bookmarks;
import org.cytoscape.property.bookmark.BookmarksUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.GUITunableHandlerFactory;
import org.cytoscape.work.swing.GUITunableHandler;

public class URLHandlerFactory implements GUITunableHandlerFactory {
	
	private final Bookmarks bookmarks;
	private final BookmarksUtil bookmarksUtil;

	public URLHandlerFactory(CyProperty<Bookmarks> book, BookmarksUtil bookmarksUtil) {
		this.bookmarks = (Bookmarks)book.getProperties();
		this.bookmarksUtil = bookmarksUtil;
		
	}

	public GUITunableHandler createTunableHandler(Field field, Object instance, Tunable tunable) {
		if ( field.getType() != URL.class)
			return null;
		
		return new URLHandler(field, instance, tunable, bookmarks, bookmarksUtil);
	}

	public GUITunableHandler createTunableHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		if ( getter.getReturnType() != URL.class)
			return null;
		
		return new URLHandler(getter, setter, instance, tunable, bookmarks, bookmarksUtil);
	}

}

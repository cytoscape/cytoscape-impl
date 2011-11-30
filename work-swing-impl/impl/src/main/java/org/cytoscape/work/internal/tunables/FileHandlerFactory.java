package org.cytoscape.work.internal.tunables;


import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;

import org.cytoscape.util.swing.FileUtil;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.internal.tunables.utils.SupportedFileTypesManager;
import org.cytoscape.work.swing.GUITunableHandler;
import org.cytoscape.work.swing.GUITunableHandlerFactory;


public final class FileHandlerFactory implements GUITunableHandlerFactory {
	private final FileUtil fileUtil;
	private final SupportedFileTypesManager fileTypesManager;

	public FileHandlerFactory(final FileUtil fileUtil, final SupportedFileTypesManager fileTypesManager) {
		this.fileUtil = fileUtil;
		this.fileTypesManager = fileTypesManager;
	}

	public GUITunableHandler createTunableHandler(Field field, Object instance, Tunable tunable) {
		if (!File.class.isAssignableFrom(field.getType()))
			return null;

		return new FileHandler(field, instance, tunable, fileTypesManager, fileUtil);
	}

	public GUITunableHandler createTunableHandler(Method getter, Method setter, Object instance, Tunable tunable) {
		if (!File.class.isAssignableFrom(getter.getReturnType()))
			return null;

		return new FileHandler(getter, setter, instance, tunable, fileTypesManager, fileUtil);
	}

}


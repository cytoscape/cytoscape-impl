package org.cytoscape.app.internal.tunable;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.cytoscape.app.internal.manager.App;
import org.cytoscape.app.internal.task.ResolveAppConflictTask.AppConflict;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.swing.AbstractGUITunableHandler;
import org.cytoscape.work.swing.DirectlyPresentableTunableHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppConflictHandler extends AbstractGUITunableHandler implements DirectlyPresentableTunableHandler {

	private static final Logger logger = LoggerFactory.getLogger(AppConflictHandler.class);
	
	protected AppConflictHandler(Field field, Object instance, Tunable tunable) {
		super(field, instance, tunable);
	}

	protected AppConflictHandler(Method getter, Method setter,
			Object instance, Tunable tunable) {
		super(getter, setter, instance, tunable);
	}

	@Override
	public boolean setTunableDirectly(Window possibleParent) {
		try {
			AppConflict conflict = (AppConflict) getValue();
			Map<App, App> appsToReplace = conflict.getAppsToReplace();
			JPanel panel = new JPanel();
	    	panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
	    	JLabel title = new JLabel("The following "+ (appsToReplace.size() == 1 ? "app" : "apps") + " will be replaced:");
	    	title.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	panel.add(title);
	    	panel.add(Box.createVerticalStrut(title.getPreferredSize().height));
	    	String deps = "";
	    	for(Entry<App, App> entry: appsToReplace.entrySet()) {
				deps += entry.getKey().getAppName() + " (to be installed: " +
						entry.getKey().getVersion() + ", " + entry.getValue().getVersion() +
						" currently installed)\n";
			}
	    	deps = deps.substring(0, deps.length() - 1);
	    	JTextArea textArea = new JTextArea(deps);
	    	textArea.setRows(Math.min(appsToReplace.size(), 10));
			textArea.setEditable(false);
			textArea.setHighlighter(null); // disables text selection
			textArea.setBorder(null);
			textArea.setOpaque(false);
			
	    	JScrollPane scrollPane = new JScrollPane(textArea);
	    	scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	scrollPane.setBorder(null);
	    	scrollPane.getViewport().setOpaque(false);
	    	scrollPane.setOpaque(false);
	    	panel.add(scrollPane);
	    	panel.add(Box.createVerticalStrut(title.getPreferredSize().height));
	    	JLabel message = new JLabel("Continue?");
	    	message.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	panel.add(message);
	    	Dimension size = panel.getPreferredSize();
	    	if(size.width > 600) {
	    		size.width = 600;
	    		panel.setPreferredSize(size);
	    	}
	    	int response = JOptionPane.showConfirmDialog(possibleParent, panel, 
					"Replace "+ (appsToReplace.size() == 1 ? "App" : "Apps"), JOptionPane.OK_CANCEL_OPTION);
			conflict.setReplaceApps(response);
		} catch (IllegalAccessException e) {
			logger.warn("Error accessing conflict object",e);
		} catch (InvocationTargetException e) {
			logger.warn("Exception thrown by conflict object",e);
		}
		return true;
	}

	@Override
	public boolean isForcedToSetDirectly() {
		return true;
	}

	@Override
	public void handle() {
		// stub method
	}

}

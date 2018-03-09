package org.cytoscape.internal.prefs;

import java.util.Properties;

public interface IPrefsPanel {
	public void initUI();
	public void adjust()	;
	public void install(Properties properties);
	public void extract(Properties properties);

	public String getDisplayName();
	public String getIcon();
	public String getTooltip();
	public int getGravity();
	public String getPropFileName();

}

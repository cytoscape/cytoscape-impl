package org.cytoscape.opencl.cycl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListSingleSelection;


public class CyCLSettingsTask extends AbstractTask implements TunableValidator 
{	
	@ProvidesTitle
	public String getTitle() 
	{
		return "OpenCL Settings";
	}
	
	static final String OPENCL_PREFERREDNAME = "opencl.device.preferred";
	
	private static final List<String> KEYS = Arrays.asList(OPENCL_PREFERREDNAME);
	
    private static final List<String> DEVICE_NAMES = new ArrayList<>();

	@Tunable(description="Preferred Device:")
	public ListSingleSelection<String> preferredNameList;

	private final Map<String, String> oldSettings;
	private final Properties properties;

	public CyCLSettingsTask(CyProperty<Properties> properties) 
	{
		oldSettings = new HashMap<String, String>();
		this.properties = properties.getProperties();
				
		DEVICE_NAMES.clear();
		List<CyCLDevice> devices = CyCL.getDevices();
		for (CyCLDevice device : devices)
			DEVICE_NAMES.add(device.name);
		
		preferredNameList = new ListSingleSelection<String>(DEVICE_NAMES);
		
		try 
		{
            final String preferredName = this.properties.getProperty(OPENCL_PREFERREDNAME);
            if (DEVICE_NAMES.contains(preferredName))
            	preferredNameList.setSelectedValue(preferredName);
            else
            	preferredNameList.setSelectedValue(DEVICE_NAMES.get(0));
		} 
		catch (IllegalArgumentException e) 
		{
			preferredNameList.setSelectedValue(DEVICE_NAMES.get(0));
		}

        assignSystemProperties();
	}

    public void assignSystemProperties() 
    {
        
    }
	
	@Override
	public ValidationState getValidationState(final Appendable errMsg) 
	{	
		storeSettings();

		revertSettings();
		
		return ValidationState.OK;
	}

	@Override
	public void run(TaskMonitor taskMonitor) 
	{
		taskMonitor.setProgress(0.0);
		
		storeSettings();
		oldSettings.clear();
		
		taskMonitor.setProgress(1.0);
	}

	void storeSettings() 
	{
		oldSettings.clear();
		for (String key : KEYS) 
		{
			if (properties.getProperty(key) != null)
				oldSettings.put(key, properties.getProperty(key));
			properties.remove(key);
		}

		properties.setProperty(OPENCL_PREFERREDNAME, preferredNameList.getSelectedValue());
        
        assignSystemProperties();
	}

	void revertSettings() 
	{
		for (String key : KEYS) 
		{
			properties.remove(key);
			
			if (oldSettings.containsKey(key))
				properties.setProperty(key, oldSettings.get(key));
		}
		oldSettings.clear();
        
        assignSystemProperties();
	}
}

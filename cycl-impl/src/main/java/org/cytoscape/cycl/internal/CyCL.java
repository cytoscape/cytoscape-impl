package org.cytoscape.cycl.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.cycl.CyCLDevice;
import org.cytoscape.property.CyProperty;
import org.lwjgl.opencl.CL;


/***
 * Central OpenCL service that is initialized on startup and stores all available devices.
 * 
 * @author Dimitry Tegunov
 *
 */
public class CyCL 
{
	public static Object initSync = new Object();
	public static Object sync = new Object();
	private static List<CyCLDevice> devices = new ArrayList<>();
	private static boolean isInitialized = false;

	public static List<CyCLDevice> getDevices()
	{
		if (devices == null)
			devices = new ArrayList<>();

		return devices;
	}

	/**
	 * Loads all necessary native libraries, initializes LWJGL and populates the device list.
	 * Should be called only once on startup.
	 * 
	 * @param applicationConfig Instance of Cytoscape's application configuration service
	 * @param propertyService Instance of Cytoscape's property service for cyPropertyName=cytoscape3.props
	 * @return True if initialized correctly; false otherwise
	 */
	public static boolean initialize(CyApplicationConfiguration applicationConfig, CyProperty<Properties> propertyService)
	{
		synchronized (initSync)
		{
			if (isInitialized)
				return true;

			{
				try
				{
					// dummy.createNewFile();

          CL.destroy();
					CL.create();

					// Populate device list
					Properties globalProps = propertyService.getProperties();
					String preferredDevice = globalProps.getProperty("opencl.device.preferred");

					if (preferredDevice == null)
						preferredDevice = "";

					devices = CyCLDeviceImpl.getAll(preferredDevice);

					if (devices == null || devices.size() == 0)
						return false;
				}
				catch (Throwable e)
				{
					e.printStackTrace();
					return false;
				}
			}

			isInitialized = true;

			return true;
		}
	}

  public static boolean isInitialized() { return isInitialized; }

	public static void makePreferred(String name)
	{
		synchronized (initSync)
		{
			if (devices == null)
				return;

			CyCLDevice newPreferred = null;
			for (CyCLDevice device : devices)
				if (device.getName().equals(name))
				{
					newPreferred = device;
					break;
				}

			if (newPreferred != null)
			{
				devices.remove(newPreferred);
				devices.add(0, newPreferred);
			}
		}
	}
}

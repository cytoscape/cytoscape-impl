package org.cytoscape.opencl.cycl;

import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import org.lwjgl.LWJGLException;
import org.lwjgl.opencl.CL;


/***
 * Central OpenCL service that is initialized on startup and stores all available devices.
 * 
 * @author Dimitry Tegunov
 *
 */
public class CyCL 
{
	private static List<CyCLDevice> devices;
	private static boolean isInitialized = false;
	
	public CyCL()
	{
	}
	
	public static List<CyCLDevice> getDevices()
	{
		if (devices == null)
			initialize("");
		
		return devices;
	}
	
	/***
	 * Loads all necessary native libraries, initializes LWJGL and populates the device list.
	 * Should be called only once on startup.
	 */
	public static boolean initialize(String preferredDevice)
	{		
		if (isInitialized)
			return true;
				
		try
		{
			CL.create();
		
			// Populate device list
			devices = CyCLDevice.getAll(preferredDevice);
			
			if (devices == null || devices.size() == 0)
				return false;
		}
		catch (LWJGLException e)
		{
			return false;
		}
		
		isInitialized = true;
		
		return true;
	}
}

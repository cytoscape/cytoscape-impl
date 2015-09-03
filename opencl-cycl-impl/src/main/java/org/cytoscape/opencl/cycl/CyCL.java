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
		
		String osArch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name").toLowerCase();

        // Load libraries for current OS and architecture
        if (osName.startsWith("win")) 
        {
            if (osArch.equalsIgnoreCase("amd64")) 
            {
                System.loadLibrary("jinput-raw_64");
                System.loadLibrary("jinput-dx8_64");
                System.loadLibrary("lwjgl64");
                System.loadLibrary("OpenAL64");
            } 
            else if (osArch.equalsIgnoreCase("i386")) 
            {
                System.loadLibrary("jinput-dx8");
                System.loadLibrary("jinput-raw");
                System.loadLibrary("lwjgl");
                System.loadLibrary("OpenAL32");
            }
            else
            	return false;
        } 
        else if (osName.startsWith("linux")) 
        {
            if (osArch.equalsIgnoreCase("amd64")) 
            {
                System.loadLibrary("libjinput-linux64");
                System.loadLibrary("liblwjgl64");
                System.loadLibrary("libopenal64");
            } 
            else if (osArch.equalsIgnoreCase("i386")) 
            {
                System.loadLibrary("libjinput-linux");
                System.loadLibrary("liblwjgl");
                System.loadLibrary("libopenal");
            }
            else
            	return false;
        } 
        else if (osName.startsWith("mac"))
        {
            System.loadLibrary("libjinput-osx");
            System.loadLibrary("liblwjgl");
            System.loadLibrary("openal");
        }
        else
        	return false;
				
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

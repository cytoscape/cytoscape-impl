package org.cytoscape.opencl.cycl;

import org.lwjgl.opencl.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

/***
 * Represents functionality associated with a single OpenCL device.
 * All operations, e. g. memory allocation or kernel execution, should be
 * performed through the appropriate CyCLDevice object. 
 * All devices present in the system are initialized as a CyCLDevice object
 * when Cytoscape starts. Manual creation should not be needed.
 * 
 * @author Dimitry Tegunov
 *
 */
public class CyCLDevice
{
	public enum DeviceTypes
	{
		CPU,
		GPU,
		Accelerator
	}
	
	private Boolean finalized;
	
	public String platformName;
	
	public final String name;
	public final String vendor;
	public final String version;
	public final DeviceTypes type;
	public final int computeUnits;
	public final long workItemDimensions;
	public final long[] maxWorkItemSizes;
	public final long maxWorkGroupSize;
	public final long clockFrequency;
	public final int addressBits;
	public final long maxMallocSize;
	public final long globalMemSize;
	public final boolean supportsECC;
	public final String localMemType;
	public final long localMemSize;
	public final long maxConstBufferSize;
	public final boolean supportsImages;
	public final int maxReadImageArgs;
	public final int maxWriteImageArgs;
	public final long[] image2DMaxSize;
	public final long[] image3DMaxSize;
	
	public final int prefWidthChar;
	public final int prefWidthShort;
	public final int prefWidthInt;
	public final int prefWidthLong;
	public final int prefWidthFloat;
	public final int prefWidthDouble;
	
	private final CLDevice device;
	private final CLPlatform devicePlatform;
	private final CyCLContext context;
	private final HashMap<String, CyCLProgram> programs;
	
	public final long bestBlockSize;
	public final long bestWarpSize;
	
	// Logarithmic scale, lower is better
	public final double benchmarkScore;
	
	/***
	 * Initializes a context for the device, acquires all property values and runs a benchmark.
	 * 
	 * @param device LWJGL device ID
	 * @param platform LWJGL platform ID
	 */
	private CyCLDevice(CLDevice device, CLPlatform platform, boolean doBenchmark)
	{
		finalized = false;
		
		this.device = device;
		devicePlatform = platform;
		context = new CyCLContext(this);
		programs = new HashMap<>();
		
		// Obtain information about the platform the device belongs to
		platformName = devicePlatform.getInfoString(CL10.CL_PLATFORM_NAME);
		//if (platformName.indexOf(" ") > -1)
			//platformName = platformName.substring(0, platformName.indexOf(" "));

		// Obtain information about the device
		vendor = device.getInfoString(CL10.CL_DEVICE_VENDOR);
		version = device.getInfoString(CL10.CL_DEVICE_VERSION);
		name = version + " " + device.getInfoString(CL10.CL_DEVICE_NAME);
		
		// Device type can be in theory a combination of multiple enum values, GPU is probably the most important indicator
		long longType = device.getInfoLong(CL10.CL_DEVICE_TYPE);
		if((longType & CL10.CL_DEVICE_TYPE_GPU) != 0)
			type = DeviceTypes.GPU;
		else if((longType & CL10.CL_DEVICE_TYPE_ACCELERATOR) != 0)
			type = DeviceTypes.Accelerator;
		else //if((longType & CL10.CL_DEVICE_TYPE_CPU) != 0)
			type = DeviceTypes.CPU;
		
		computeUnits = device.getInfoInt(CL10.CL_DEVICE_MAX_COMPUTE_UNITS);
		workItemDimensions = device.getInfoLong(CL10.CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
		maxWorkItemSizes = device.getInfoSizeArray(CL10.CL_DEVICE_MAX_WORK_ITEM_SIZES);
		maxWorkGroupSize = device.getInfoSize(CL10.CL_DEVICE_MAX_WORK_GROUP_SIZE);
		clockFrequency = device.getInfoLong(CL10.CL_DEVICE_MAX_CLOCK_FREQUENCY);
		addressBits = device.getInfoInt(CL10.CL_DEVICE_ADDRESS_BITS);
		maxMallocSize = device.getInfoLong(CL10.CL_DEVICE_MAX_MEM_ALLOC_SIZE);
		globalMemSize = device.getInfoLong(CL10.CL_DEVICE_GLOBAL_MEM_SIZE);
		supportsECC = device.getInfoInt(CL10.CL_DEVICE_ERROR_CORRECTION_SUPPORT) > 0;
		localMemType = device.getInfoInt(CL10.CL_DEVICE_LOCAL_MEM_TYPE) == 1 ? "local" : "global";
		localMemSize = device.getInfoLong(CL10.CL_DEVICE_LOCAL_MEM_SIZE);
		maxConstBufferSize = device.getInfoLong(CL10.CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE);
		supportsImages = device.getInfoInt(CL10.CL_DEVICE_IMAGE_SUPPORT) > 0;
		maxReadImageArgs = device.getInfoInt(CL10.CL_DEVICE_MAX_READ_IMAGE_ARGS);
		maxWriteImageArgs = device.getInfoInt(CL10.CL_DEVICE_MAX_WRITE_IMAGE_ARGS);
		image2DMaxSize = new long[] { device.getInfoSize(CL10.CL_DEVICE_IMAGE2D_MAX_WIDTH),
									  device.getInfoSize(CL10.CL_DEVICE_IMAGE2D_MAX_HEIGHT) };
		image3DMaxSize = new long[] { device.getInfoSize(CL10.CL_DEVICE_IMAGE3D_MAX_WIDTH),
									  device.getInfoSize(CL10.CL_DEVICE_IMAGE3D_MAX_HEIGHT),
									  device.getInfoSize(CL10.CL_DEVICE_IMAGE3D_MAX_DEPTH) };

		prefWidthChar = device.getInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR);
		prefWidthShort = device.getInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT);
		prefWidthInt = device.getInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_);
		prefWidthLong = device.getInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG);
		prefWidthFloat = device.getInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT);
		prefWidthDouble = device.getInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE);
		
		// Nvidia and Intel have 32 threads per warp, AMD has 64
		if(type == DeviceTypes.GPU)
    	{
    		if(platformName.toLowerCase().contains("amd"))
    		{
    			bestBlockSize = 128;
    			bestWarpSize = 64;
    		}
    		else if(platformName.toLowerCase().contains("nvidia"))
    		{
    			bestBlockSize = 192;
    			bestWarpSize = 32;
    		}
    		else if(platformName.toLowerCase().contains("intel"))
    		{
    			bestBlockSize = 128;
    			bestWarpSize = 32;
    		}
            else if(platformName.toLowerCase().contains("apple"))   // These guys are special.
            {
                String lowName = name.toLowerCase();
                
                if(lowName.contains("radeon") || lowName.contains("fire"))
                {
                    bestBlockSize = 128;
                    bestWarpSize = 64;
                }
                else if(lowName.contains("geforce") ||
                        lowName.contains("gtx") ||
                        lowName.contains("quadro") ||
                        lowName.contains("tesla"))
                {
                    bestBlockSize = 192;
                    bestWarpSize = 32;
                }
                else    // Probably Intel.
                {
                    bestBlockSize = 128;
                    bestWarpSize = 32;
                }
            }
    		else	// A new player has entered the GPU market!
    		{
    			bestBlockSize = 32;
    			bestWarpSize = 32;
    		}
    	}
		else	// CPU
		{
			bestBlockSize = 1;
			bestWarpSize = 1;
		}
		
        CyCLProgram program = null;
        try
		{
			program = new CyCLProgram(context, this, getClass().getResource("/Benchmark.cl"), new String[] { "BenchmarkKernel" }, null, true);
		}
		catch (Exception e1) { throw new RuntimeException("Could not build benchmark program."); }
	        
		// Run the benchmark     
		if (doBenchmark)
		{
	        
	        int n = 1 << 13;
	        int[] a = new int[n];
	        int[] b = new int[n];
	        int[] c = new int[n];
	        
	        for(int i=0; i < n; i++)
	        {
	        	a[i] = i;
	        	b[i] = i;
	        }
	
	        CyCLBuffer bufferA = new CyCLBuffer(context, a);
	        CyCLBuffer bufferB = new CyCLBuffer(context, b);
	        CyCLBuffer bufferC = new CyCLBuffer(context, int.class, n);
	        
	        // Warm up
	        program.getKernel("BenchmarkKernel").execute(new long[] { n }, null,
					 bufferA,
					 bufferB,
					 bufferC,
					 n);
	        bufferC.getFromDevice(c);
	        
	        List<Double> logTimes = new ArrayList<>();
	        
	        // Benchmark       
	        for (int i = 0; i < 4; i++)
			{
	        	long timeStart = System.nanoTime();
	        	
	        	int benchN = 1 << (10 + i);
	
				program.getKernel("BenchmarkKernel").execute(new long[] { benchN }, null,
															 bufferA,
															 bufferB,
															 bufferC,
															 benchN);
				CL10.clFinish(context.getQueue());
	        	
		        long timeStop = System.nanoTime();
		        logTimes.add(Math.log((double)(timeStop - timeStart) * 1e-9));
			}
	        // Get back the result to check its correctness
	        bufferC.getFromDevice(c);
	        
	        double diffsum = 0.0;
	        for (int i = 0; i < logTimes.size() - 1; i++)
	        	diffsum += logTimes.get(i + 1) - logTimes.get(i);
	        diffsum /= (double)(logTimes.size() - 1);
	        benchmarkScore = Math.exp(diffsum);        
	        
	        for (int i = 0; i < c.length; i++)
			{
				if (c[i] != Math.max(0, i - 1))
					throw new RuntimeException("OpenCL benchmark produced wrong values.");
			}        
	        
	        // Clean up after benchmark
	        try
			{	        
		        bufferA.free();
		        bufferB.free();
		        bufferC.free();
			}
			catch (Throwable e)	{ throw new RuntimeException("Could not release resources."); }
		}
		else
		{
			benchmarkScore = 0.0;
		}
		        
		program.finalize();
	}
	    
	/***
	 * Gets the underlying LWJGL device ID.
	 * 
	 * @return LWJGL device ID
	 */
    public CLDevice getDevice()
    {
    	return device;
    }
    
    /***
     * Gets the underlying LWJGL platform ID.
     * 
     * @return LWJGL platform ID
     */
    public CLPlatform getPlatform()
    {
    	return devicePlatform;
    }
    
    /***
     * Suggests an optimal block (work item) size for the given global item count.
     * 
     * @param n Global item count
     * @return Optimal block size for this architecture
     */
    public long getBestBlockSize(long n)
    {
    	return Math.min(bestBlockSize, (n + bestWarpSize - 1) / bestWarpSize * bestWarpSize); 
    }
    
    /***
     * Determines if a program with the given name has already been compiled and stored.
     * 
     * @param name Program name
     * @return True if the program has been compiled, false otherwise
     */
    public Boolean hasProgram(String name)
    {
    	return programs.containsKey(name);
    }
    
    /***
     * Attempts to find a pre-compiled program with the given name.
     * 
     * @param name Program name
     * @return The program if it is found, null otherwise
     */
    public CyCLProgram getProgram(String name)
    {
    	if (!hasProgram(name))
    		return null;
    	else
    		return programs.get(name);
    }
    
    /***
     * Compiles a program and its kernels, and stores it for further use.
     * 
     * @param name Program name
     * @param resourcePath Path to the resource with the program's text
     * @param kernelNames An array of kernel names, as used in the program
     * @param defines Dictionary of definitions to be injected as "#define key value"; can be null
     * @return The program if it has been successfully compiled
     */
    public CyCLProgram addProgram(String name, URL resourcePath, String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation)
    {
    	if (hasProgram(name))
    		return getProgram(name);
    	
    	HashMap<String, String> alldefines = getDeviceSpecificDefines();
    	if (defines != null)
	    	for (Entry<String, String> entry : defines.entrySet())
	    		alldefines.put(entry.getKey(), entry.getValue());
    	
    	CyCLProgram added;
    	try
    	{
    		added = new CyCLProgram(context, this, resourcePath, kernelNames, alldefines, silentCompilation);
    	}
    	catch (Exception e)
    	{
    		throw new RuntimeException();
    	}
    	
    	programs.put(name, added);
    	
    	return added;
    }
    
    /***
     * Compiles a program and its kernels, and stores it, possibly replacing (and destroying) an old instance.
     * 
     * @param name Program name
     * @param resourcePath Path to the resource with the program's text
     * @param kernelNames An array of kernel names, as used in the program
     * @param defines Dictionary of definitions to be injected as "#define key value"; can be null
     * @return The program if it has been successfully compiled
     */
    public CyCLProgram forceAddProgram(String name, URL resourcePath, String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation)
    {
    	if (hasProgram(name))
    	{
    		try 
    		{ 
    			getProgram(name).finalize(); 
    		}
			catch (Throwable e)	{ }
    		programs.remove(name);
    	}
    	
    	HashMap<String, String> alldefines = getDeviceSpecificDefines();
    	if (defines != null)
	    	for (Entry<String, String> entry : defines.entrySet())
	    		alldefines.put(entry.getKey(), entry.getValue());
    	
    	CyCLProgram added;
    	try
    	{
    		added = new CyCLProgram(context, this, resourcePath, kernelNames, alldefines, silentCompilation);
    	}
    	catch (Exception e)
    	{
    		throw new RuntimeException();
    	}
    	
    	programs.put(name, added);
    	
    	return added;
    }
    
    /***
     * Gets a dictionary of device-specific values that will be defined in the compiled program
     * 
     * @return Dictionary of key-value pairs, as in "#define key value"
     */
    public HashMap<String, String> getDeviceSpecificDefines()
    {
    	HashMap<String, String> defines = new HashMap<>();
    	if (this.type == DeviceTypes.GPU)
    		defines.put("CYCL_GPU", "");
    	else
    		defines.put("CYCL_CPU", "");
    	defines.put("CYCL_WARP", String.valueOf(this.bestWarpSize));
    	
    	return defines;
    }
    
    /***
     * Pauses the calling thread until all items in the device's command queue have been finished.
     */
    public void finishQueue()
    {
    	CL10.clFinish(context.getQueue());
    }
    
    /***
     * Allocates memory on this device without filling it with any data.
     * @param type Buffer element type
     * @param elements Number of elements
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(Class<?> type, int elements)
    {
    	return new CyCLBuffer(context, type, elements);
    }
    
    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Byte array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(byte[] data)
    {
    	return new CyCLBuffer(context, data);
    }
    
    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Int16 array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(short[] data)
    {
    	return new CyCLBuffer(context, data);
    }
    
    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Int32 array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(int[] data)
    {
    	return new CyCLBuffer(context, data);
    }
    
    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Int64 array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(long[] data)
    {
    	return new CyCLBuffer(context, data);
    }
    
    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Float32 array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(float[] data)
    {
    	return new CyCLBuffer(context, data);
    }
    
    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Float64 array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(double[] data)
    {
    	return new CyCLBuffer(context, data);
    }
	
    /***
     * Releases all native resources associated with the device.
     * Object cannot be used anymore once this method has been called.
     */
	@Override
	protected void finalize()
	{
		try
		{
			if(finalized)
				return;
			
			for(Entry<String, CyCLProgram> entry : programs.entrySet())
				entry.getValue().finalize();
			
			context.finalize();
			
			finalized = true;		
			super.finalize();
		}
		catch (Throwable exc)
		{
			System.out.println(exc.getMessage());
			throw new RuntimeException("Could not finalize CyCLDevice object.");
		}
	}

	/***
	 * Initializes all devices present in the system, removes duplicates 
	 * (same device, different platforms), and returns them as a list.
	 * 
	 * @return List of all initialized devices
	 */
    public static List<CyCLDevice> getAll(final String preferredDevice)
    {
    	List<CyCLDevice> devices = new ArrayList<>();
    	
        for(CLPlatform platform : CLPlatform.getPlatforms())
        {
            for(CLDevice id : platform.getDevices(CL10.CL_DEVICE_TYPE_ALL))
            {
            	CyCLDevice newDevice;
            	try 
            	{ 
            		newDevice = new CyCLDevice(id, platform, preferredDevice.equals("")); // Benchmark only if there is no preferred device.
            	} 
            	catch (Exception e1) 
            	{ 
            		continue; 
            	}
            	devices.add(newDevice);
            }
        }
    	
    	final class DeviceComparator implements Comparator<CyCLDevice> 
    	{
    	    @Override
    	    public int compare(CyCLDevice o1, CyCLDevice o2) 
    	    {
    	    	if (o1.name.equals(preferredDevice))
    	    		return -1;
    	    	else
    	    		return Double.compare(o1.benchmarkScore, o2.benchmarkScore);
    	    }
    	}    	
    	devices.sort(new DeviceComparator());
    	
    	/*List<CyCLDevice> uniqueDevices = new ArrayList<>();
    	for (CyCLDevice device : devices)
		{
			Boolean unique = true;
			for (CyCLDevice included : uniqueDevices)
				if (included.name.toLowerCase().equals(device.name.toLowerCase()) && !included.platformName.toLowerCase().equals(device.platformName.toLowerCase()))
				{
					//unique = false;
					//break;
				}
			if (unique)
				uniqueDevices.add(device);
			else
				try { device.finalize(); } catch (Throwable e) { }
		}*/
    	
    	return devices;
    }
}

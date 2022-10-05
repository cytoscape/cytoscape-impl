package org.cytoscape.cycl.internal;

import org.cytoscape.cycl.*;

import org.lwjgl.*;
import org.lwjgl.opencl.*;
import org.lwjgl.system.*;

import java.nio.*;

import static org.lwjgl.opencl.CL10.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
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
public class CyCLDeviceImpl implements CyCLDevice
{
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

  private final CyCLContext context;
  private final HashMap<String, CyCLProgram> programs;

  private final long device;
  private final CyCLPlatformImpl devicePlatform;

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
  private CyCLDeviceImpl(long device, CyCLPlatform platform, boolean doBenchmark)
  {
    finalized = false;

    this.device = device;
    devicePlatform = (CyCLPlatformImpl)platform;

    // Thread.dumpStack();

    // Obtain information about the platform the device belongs to
    platformName = devicePlatform.getPlatformInfoStringUTF8(CL10.CL_PLATFORM_NAME);
    System.out.println("Platform: "+platformName);
    context = new CyCLContextImpl(platform, this);
    programs = new HashMap<>();

    // Obtain information about the platform the device belongs to
    // platformName = devicePlatform.getInfoString(CL10.CL_PLATFORM_NAME);
    //if (platformName.indexOf(" ") > -1)
      //platformName = platformName.substring(0, platformName.indexOf(" "));

    // Obtain information about the device
    CLCapabilities caps = CL.createDeviceCapabilities(device, devicePlatform.getCapabilities());
    vendor = getDeviceInfoStringUTF8(CL_DEVICE_VENDOR);
    version = getDeviceInfoStringUTF8(CL_DEVICE_VERSION);
    name = version + " " + getDeviceInfoStringUTF8(CL_DEVICE_NAME);

    // Device type can be in theory a combination of multiple enum values, GPU is probably the most important indicator
    long longType = getDeviceInfoLong(CL10.CL_DEVICE_TYPE);
    if((longType & CL10.CL_DEVICE_TYPE_GPU) != 0)
      type = DeviceTypes.GPU;
    else if((longType & CL10.CL_DEVICE_TYPE_ACCELERATOR) != 0)
      type = DeviceTypes.Accelerator;
    else //if((longType & CL10.CL_DEVICE_TYPE_CPU) != 0)
      type = DeviceTypes.CPU;

    computeUnits = getDeviceInfoInt(CL10.CL_DEVICE_MAX_COMPUTE_UNITS);
    workItemDimensions = getDeviceInfoLong(CL10.CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
    maxWorkItemSizes = getDeviceInfoLongArray(CL10.CL_DEVICE_MAX_WORK_ITEM_SIZES);
    maxWorkGroupSize = getDeviceInfoLong(CL10.CL_DEVICE_MAX_WORK_GROUP_SIZE);
    clockFrequency = getDeviceInfoLong(CL10.CL_DEVICE_MAX_CLOCK_FREQUENCY);
    addressBits = getDeviceInfoInt(CL10.CL_DEVICE_ADDRESS_BITS);
    maxMallocSize = getDeviceInfoLong(CL10.CL_DEVICE_MAX_MEM_ALLOC_SIZE);
    globalMemSize = getDeviceInfoLong(CL10.CL_DEVICE_GLOBAL_MEM_SIZE);
    supportsECC = getDeviceInfoInt(CL10.CL_DEVICE_ERROR_CORRECTION_SUPPORT) > 0;
    localMemType = getDeviceInfoInt(CL10.CL_DEVICE_LOCAL_MEM_TYPE) == 1 ? "local" : "global";
    localMemSize = getDeviceInfoLong(CL10.CL_DEVICE_LOCAL_MEM_SIZE);
    maxConstBufferSize = getDeviceInfoLong(CL10.CL_DEVICE_MAX_CONSTANT_BUFFER_SIZE);
    supportsImages = getDeviceInfoInt(CL10.CL_DEVICE_IMAGE_SUPPORT) > 0;
    maxReadImageArgs = getDeviceInfoInt(CL10.CL_DEVICE_MAX_READ_IMAGE_ARGS);
    maxWriteImageArgs = getDeviceInfoInt(CL10.CL_DEVICE_MAX_WRITE_IMAGE_ARGS);
    image2DMaxSize = new long[] { getDeviceInfoLong(CL10.CL_DEVICE_IMAGE2D_MAX_WIDTH),
                    getDeviceInfoLong(CL10.CL_DEVICE_IMAGE2D_MAX_HEIGHT) };
    image3DMaxSize = new long[] { getDeviceInfoLong(CL10.CL_DEVICE_IMAGE3D_MAX_WIDTH),
                    getDeviceInfoLong(CL10.CL_DEVICE_IMAGE3D_MAX_HEIGHT),
                    getDeviceInfoLong(CL10.CL_DEVICE_IMAGE3D_MAX_DEPTH) };

    prefWidthChar = getDeviceInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_CHAR);
    prefWidthShort = getDeviceInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_SHORT);
    prefWidthInt = getDeviceInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_INT);
    prefWidthLong = getDeviceInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_LONG);
    prefWidthFloat = getDeviceInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_FLOAT);
    prefWidthDouble = getDeviceInfoInt(CL10.CL_DEVICE_PREFERRED_VECTOR_WIDTH_DOUBLE);

    System.out.println("Device: "+name);

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
        else  // A new player has entered the GPU market!
        {
          bestBlockSize = 32;
          bestWarpSize = 32;
        }
      }
    else  // CPU
    {
      bestBlockSize = 1;
      bestWarpSize = 1;
    }


    // Run the benchmark
    if (doBenchmark)
    {
      benchmarkScore = performBenchmark(false /*do not use offsets*/);
    }
    else
    {
      benchmarkScore = 0.0;
    }
  }

  /**
   * Runs a simple benchmark on the device. If there is a problem, a {@link CyCLException} is thrown, otherwise
   * it returns the benchmark score.
   * @param useOffsets if true, the benchmark includes offset workloads
   * @return the benchmark value. The lower, the better.
   */
  public double performBenchmark(boolean useOffsets)
  {
    String sumProgramSource =
      "__kernel void BenchmarkKernel(__global const int* a, __global const int* b, __global int* c, int const size) {\n"
          + "  const int itemId = get_global_id(0);\n" 
          + "  if(itemId < size) {\n"
          + "    c[itemId] = a[itemId] + b[itemId];\n" 
          + "  }\n" 
          + "}";

    CyCLProgramImpl program = null;
    try {
      program = new CyCLProgramImpl(context, this, getClass().getResource("/Benchmark.cl"), new String[] { "BenchmarkKernel" }, null, false);
      // program = new CyCLProgram(context, this, sumProgramSource, new String[] { "BenchmarkKernel" }, null, false);
    }
    catch (Exception e1) {
      throw new CyCLException("Could not build benchmark program.", e1);
    }

    try {
      IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);

      int n = 1 << 13;
      int[] a = new int[n];
      int[] b = new int[n];
      int[] c = new int[n];

      for(int i=0; i < n; i++)
      {
        a[i] = i;
        b[i] = i;
      }

      CyCLBuffer bufferA = createWriteBuffer(a);
      CyCLBuffer bufferB = createWriteBuffer(b);
      CyCLBuffer bufferC = createReadBuffer(c);

      List<Double> logTimes = new ArrayList<>();
      try ( MemoryStack stack = stackPush() ) {

        int argIndex = 0;
        CyCLKernel cyKernel = program.getKernel("BenchmarkKernel");
        long kernel = cyKernel.getKernel();

        String kernelName = cyKernel.getKernelInfoStringUTF8(CL10.CL_KERNEL_FUNCTION_NAME);
        int kernelArgs = cyKernel.getKernelInfoInt(CL10.CL_KERNEL_NUM_ARGS);

        // Warm up
        cyKernel.execute(new long[] { n }, null,
                           bufferA,
                           bufferB,
                           bufferC,
                           n);

        bufferC.getFromDevice(c);
        clFinish(context.getQueue());

        // Benchmark
        for (int i = 0; i < 4; i++)
        {
          long timeStart = System.nanoTime();

          int benchN = 1 << (10 + i);

          if(!useOffsets) {
            program.getKernel("BenchmarkKernel").execute(new long[] { benchN }, null,
                               bufferA,
                               bufferB,
                               bufferC,
                               benchN);
          } else {
            for(int offset = 0; offset < benchN; offset += 128)
            {
              program.getKernel("BenchmarkKernel").executeWithOffset(new long[] { benchN }, null, new long[] {offset},
                                bufferA,
                                bufferB,
                                bufferC,
                                benchN);
            }
          }


          CL10.clFinish(context.getQueue());

          long timeStop = System.nanoTime();
          logTimes.add(Math.log((double)(timeStop - timeStart) * 1e-9));
        }

        // Get back the result to check its correctness
        bufferC.getFromDevice(c);

        for (int i = 0; i < c.length; i++)
        {
          if (c[i] != Math.max(0, i - 1)) {
            System.out.println("OpenCL benchmark produced wrong value for "+i+".  Expected "+Math.max(0,i-1)+" but got "+c[i]);
            throw new CyCLException("OpenCL benchmark produced wrong value for "+i+".  Expected "+Math.max(0,i-1)+" but got "+c[i]);
          }
        }

      } finally {
          bufferA.free();
          bufferB.free();
          bufferC.free();
      }

      double diffsum = 0.0;
      for (int i = 0; i < logTimes.size() - 1; i++)
        diffsum += logTimes.get(i + 1) - logTimes.get(i);
      diffsum /= (double)(logTimes.size() - 1);

      return Math.exp(diffsum);
    } catch (CyCLException ex) {
      //just rethrow
      ex.printStackTrace();
      throw ex;
    } catch (Exception ex) {
      ex.printStackTrace();
      throw new CyCLException("Error running benchmark", ex);
    } finally {
      program.finalize();
    }

  }

  /***
   * Gets the underlying LWJGL device ID.
   *
   * @return LWJGL device ID
   */
    public long getDevice()
    {
      return device;
    }

    /***
     * Gets the underlying LWJGL platform ID.
     *
     * @return LWJGL platform ID
     */
    public CyCLPlatform getPlatform()
    {
      return devicePlatform;
    }

    /***
     * Return the maximum work group size for this device
     *
     * @return maximum work group size
     */
    public long getMaxWorkGroupSize()
    {
      return maxWorkGroupSize;
    }

    /***
     * Return the best block size for this device
     *
     * @return Optimal block size for this architecture
     */
    public long getBestBlockSize()
    {
      return bestBlockSize;
    }

    /***
     * Return the best warp size for this device
     *
     * @return Best warp size for this architecture
     */
    public long getBestWarpSize()
    {
      return bestWarpSize;
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
     * @param programSources Strings containing the individual files comprising the program
     * @param kernelNames An array of kernel names, as used in the program
     * @param defines Dictionary of definitions to be injected as "#define key value"; can be null
     * @return The program if it has been successfully compiled
     */
    public CyCLProgram addProgram(String name, String[] programSources, String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation)
    {
      if (hasProgram(name))
        return getProgram(name);

      HashMap<String, String> alldefines = getDeviceSpecificDefines();
      if (defines != null)
        alldefines.putAll(defines);

      CyCLProgram added;
      added = new CyCLProgramImpl(context, this, programSources, kernelNames, alldefines, silentCompilation);

      programs.put(name, added);

      return added;

    }

    /***
     * Compiles a program and its kernels, and stores it for further use.
     *
     * @param name Program name
     * @param programSource A sring containing the program source
     * @param kernelNames An array of kernel names, as used in the program
     * @param defines Dictionary of definitions to be injected as "#define key value"; can be null
     * @return The program if it has been successfully compiled
     */
    public CyCLProgram addProgram(String name, String programSource, String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation)
    {
      return addProgram(name, new String[] {programSource}, kernelNames, defines, silentCompilation);
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

    try
    {
        InputStream programTextStream = resourcePath.openStream();
        Scanner programTextScanner = new Scanner(programTextStream, "UTF-8");
        String programText = programTextScanner.useDelimiter("\\Z").next();
        programTextScanner.close();
          programTextStream.close();

          return addProgram(name, new String[]{programText}, kernelNames, defines, silentCompilation);
    }
    catch (IOException ex)
    {
      throw new CyCLException("Error reading OpenCL program.", ex);
    }

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
          CyCLProgramImpl prog = (CyCLProgramImpl)getProgram(name);
          prog.finalize();
        }
      catch (Throwable e)  { }
        programs.remove(name);
      }

      HashMap<String, String> alldefines = getDeviceSpecificDefines();
      if (defines != null)
        for (Entry<String, String> entry : defines.entrySet())
          alldefines.put(entry.getKey(), entry.getValue());

      CyCLProgram added;
      try
      {
        added = new CyCLProgramImpl(context, this, resourcePath, kernelNames, alldefines, silentCompilation);
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
     * This assumes it's going to be a read buffer!!
     * @param type Buffer element type
     * @param elements Number of elements
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(Class<?> type, int elements)
    {
      return new CyCLBufferImpl(context, type, elements, CL10.CL_MEM_READ_ONLY);
    }

    /***
     * Allocates memory on this device without filling it with any data.
     * @param type Buffer element type
     * @param elements Number of elements
     * @param bits the CL_MEM bits that describe the buffer type
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(Class<?> type, int elements, int bits)
    {
      return new CyCLBufferImpl(context, type, elements, bits);
    }

    /***
     * Allocate a buffer that will be sent to the device and fills
     * it with host data.
     * @param data Byte array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createWriteBuffer(byte[] data) {
      return createBuffer(data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
    }

    /***
     * Allocate a buffer that will be read from the device.
     * @param data Byte array the data will be copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createReadBuffer(byte[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_ONLY);
    }

    /***
     * Allocate a buffer that will be both read and written to/from the device.
     * @param data Byte array the data will be filled with and copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(byte[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_USE_HOST_PTR);
    }

    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Byte array with data to be copied
     * @param bits the CL_MEM bits that describe the buffer type
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(byte[] data, int bits)
    {
      return new CyCLBufferImpl(context, data, bits);
    }

    /***
     * Allocate a buffer that will be sent to the device and fills
     * it with host data.
     * @param data Int16 array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createWriteBuffer(short[] data) {
      return createBuffer(data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
    }

    /***
     * Allocate a buffer that will be read from the device.
     * @param data Int16 array the data will be copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createReadBuffer(short[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_ONLY);
    }

    /***
     * Allocate a buffer that will be both read and written to/from the device.
     * @param data Int16 array the data will be filled with and copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(short[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_USE_HOST_PTR);
    }

    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Int16 array with data to be copied
     * @param bits the CL_MEM bits that describe the buffer type
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(short[] data, int bits)
    {
      return new CyCLBufferImpl(context, data, bits);
    }

    /***
     * Allocate a buffer that will be sent to the device and fills
     * it with host data.
     * @param data Int32 array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createWriteBuffer(int[] data) {
      return createBuffer(data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
    }

    /***
     * Allocate a buffer that will be read from the device.
     * @param data Int32 array the data will be copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createReadBuffer(int[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_ONLY);
    }

    /***
     * Allocate a buffer that will be both read and written to/from the device.
     * @param data Int32 array the data will be filled with and copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(int[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_COPY_HOST_PTR);
    }
    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Int32 array with data to be copied
     * @param bits the CL_MEM bits that describe the buffer type
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(int[] data, int bits)
    {
      return new CyCLBufferImpl(context, data, bits);
    }

    /***
     * Allocate a buffer that will be sent to the device and fills
     * it with host data.
     * @param data Int64 array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createWriteBuffer(long[] data) {
      return createBuffer(data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
    }

    /***
     * Allocate a buffer that will be read from the device.
     * @param data Int64 array the data will be copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createReadBuffer(long[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_ONLY);
    }

    /***
     * Allocate a buffer that will be both read and written to/from the device.
     * @param data Int64 array the data will be filled with and copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(long[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_USE_HOST_PTR);
    }

    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Int64 array with data to be copied
     * @param bits the CL_MEM bits that describe the buffer type
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(long[] data, int bits)
    {
      return new CyCLBufferImpl(context, data, bits);
    }

    /***
     * Allocate a buffer that will be sent to the device and fills
     * it with host data.
     * @param data Float32 array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createWriteBuffer(float[] data) {
      return createBuffer(data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
    }

    /***
     * Allocate a buffer that will be read from the device.
     * @param data Float32 array the data will be copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createReadBuffer(float[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_ONLY);
    }

    /***
     * Allocate a buffer that will be both read and written to/from the device.
     * @param data Float32 array the data will be filled with and copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(float[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_WRITE| CL10.CL_MEM_USE_HOST_PTR);
    }

    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Float32 array with data to be copied
     * @param bits the CL_MEM bits that describe the buffer type
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(float[] data, int bits)
    {
      return new CyCLBufferImpl(context, data, bits);
    }

    /***
     * Allocate a buffer that will be sent to the device and fills
     * it with host data.
     * @param data Float64 array with data to be copied
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createWriteBuffer(double[] data) {
      return createBuffer(data, CL10.CL_MEM_WRITE_ONLY | CL10.CL_MEM_COPY_HOST_PTR);
    }

    /***
     * Allocate a buffer that will be read from the device.
     * @param data Float64 array the data will be copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createReadBuffer(double[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_ONLY);
    }

    /***
     * Allocate a buffer that will be both read and written to/from the device.
     * @param data Float64 array the data will be filled with and copied into
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(double[] data) {
      return createBuffer(data, CL10.CL_MEM_READ_WRITE | CL10.CL_MEM_USE_HOST_PTR);
    }

    /***
     * Allocates memory on this device and fills it with host data.
     * @param data Float64 array with data to be copied
     * @param bits the CL_MEM bits that describe the buffer type
     * @return CyCLBuffer object with a pointer to the allocated memory
     */
    public CyCLBuffer createBuffer(double[] data, int bits)
    {
      return new CyCLBufferImpl(context, data, bits);
    }

  @Override
  public CyCLLocalSize createLocalSize(long size) {
    return new CyCLLocalSizeImpl(size);
  }


  /*
   * Various device information strings
   */
  public String getName() {
    return name;
  }

  public String getVendor() {
    return vendor;
  }

  public String getVersion() {
    return version;
  }

  public DeviceTypes getType() {
    return type;
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

      if(programs != null)
      {
        for(Entry<String, CyCLProgram> entry : programs.entrySet())
        {
          if(entry.getValue() != null)
          {
            CyCLProgramImpl prog = (CyCLProgramImpl)entry.getValue();
            prog.finalize();
          }
        }
      }

      if(context != null)
      {
        ((CyCLContextImpl)context).finalize();
      }

      finalized = true;
    }
    catch (Throwable exc)
    {
      System.out.println("Could not finalize CyCLDevice " + name + ": " + exc.getMessage());
      throw new RuntimeException("Could not finalize CyCLDevice object.", exc);
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
      Exception savedException = null;

        System.out.println("Got "+CyCLPlatformImpl.getPlatforms().size()+" platforms");
        for(CyCLPlatform platform : CyCLPlatformImpl.getPlatforms())
        {
          try {
            long[] ids = platform.getDevices(CL10.CL_DEVICE_TYPE_ALL);
            if (ids == null)
              continue; // No devices for this platform
                        //
            System.out.println("Got "+ids.length+" devices");

            for(long id : ids)
            {
              try
              {
                CyCLDeviceImpl newDevice = new CyCLDeviceImpl(id, platform, preferredDevice.equals("")); // Benchmark only if there is no preferred device.
                devices.add(newDevice);
              }
              catch (Exception e1)
              {
                // We may have a device that is mis-behaving, but we should still keep on trying
                savedException = e1;
                e1.printStackTrace();
                throw new CyCLException(e1);
              }
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        if (devices.size() == 0 && savedException != null) {
          savedException.printStackTrace();
          throw new CyCLException(savedException);
        }

      final class DeviceComparator implements Comparator<CyCLDevice>
      {
          @Override
          public int compare(CyCLDevice o1, CyCLDevice o2)
          {
            if (o1.getName().equals(preferredDevice))
              return -1;
            else {
              double score1 = ((CyCLDeviceImpl)o1).benchmarkScore;
              double score2 = ((CyCLDeviceImpl)o2).benchmarkScore;
              return Double.compare(score1, score2);
            }
          }
      }
      devices.sort(new DeviceComparator());

      return devices;
    }

    int getDeviceInfoInt(int param_name) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer pl = stack.mallocInt(1);
            CyCLUtils.checkCLError(clGetDeviceInfo(this.device, param_name, pl, null));
            return pl.get(0);
        }
    }

    long getDeviceInfoLong(int param_name) {
        try (MemoryStack stack = stackPush()) {
            LongBuffer pl = stack.mallocLong(1);
            CyCLUtils.checkCLError(clGetDeviceInfo(this.device, param_name, pl, null));
            return pl.get(0);
        }
    }

    long getDeviceInfoPointer(int param_name) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);
            CyCLUtils.checkCLError(clGetDeviceInfo(this.device, param_name, pp, null));
            return pp.get(0);
        }
    }

    long[] getDeviceInfoLongArray(int param_name) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);
            CyCLUtils.checkCLError(clGetDeviceInfo(this.device, param_name, (ByteBuffer)null, pp));

            int size = (int)pp.get(0);

            LongBuffer buffer = stack.mallocLong(size);
            CyCLUtils.checkCLError(clGetDeviceInfo(this.device, param_name, buffer, null));
            long[] ret = new long[size];
            buffer.get(ret);
            return ret;
        }
    }

    String getDeviceInfoStringUTF8(int param_name) {
        try (MemoryStack stack = stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);
            CyCLUtils.checkCLError(clGetDeviceInfo(this.device, param_name, (ByteBuffer)null, pp));
            int bytes = (int)pp.get(0);

            ByteBuffer buffer = stack.malloc(bytes);
            CyCLUtils.checkCLError(clGetDeviceInfo(this.device, param_name, buffer, null));

            return memUTF8(buffer, bytes - 1);
        }
    }
}

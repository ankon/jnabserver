package jNab.ext.persistency;

import jNab.core.bunny.Bunny;
import jNab.core.bunny.Burrow;
import jNab.core.choreography.Choreography;
import jNab.core.choreography.ChoreographyLibrary;
import jNab.core.exceptions.PluginCreationException;
import jNab.core.misc.ChorFileNameFilter;
import jNab.core.misc.Couple;
import jNab.core.misc.SerFileNameFilter;
import jNab.core.plugins.AbstractPlugin;
import jNab.core.plugins.PluginFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Persistency service for jNab server.
 * 
 * @author Juha-Pekka Rajaniemi
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class Serializer
{
    /**
     * Path where to serialized files are (or are to be) stored.
     */
    protected File serializedFilesPath;

    /**
     * Creating a new serializer instance, for a given resource path.
     * 
     * @param serializedFilesPath path where to serialized files are (or are to be) stored.
     * 
     */
    public Serializer(File serializedFilesPath)
    {
	this.serializedFilesPath = serializedFilesPath;
    }

    /**
     * Seriliazing a bunny.
     * 
     * @param bunny the bunny to serialize.
     * @throws IOException if an IO exception occured during serialization or if resources path is not a valid directory.
     */
    public void saveBunny(Bunny bunny) throws IOException
    {
	// Destination file is a file in resources path whose name is bunny's serial
	File f = new File(new File(this.serializedFilesPath, "bunnies"), bunny.getSerialNumber() + ".ser");

	// Serializing bunny data
	FileOutputStream fos = new FileOutputStream(f);
	this.writeBunnyToOutputStream(fos, bunny);
	try
	{
	    fos.close();
	}
	catch (IOException e)
	{}
    }

    /**
     * Unserializing a bunny from an input stream.
     * 
     * @param in the input stream where to read the serialized data.
     * @param pluginFactory the pulign factory used to create plugins.
     * @return the bunny loaded from the input stream.
     * @throws IOException if serialized data is corrupted.
     */
    protected Bunny readBunnyFromInputStream(InputStream in, PluginFactory pluginFactory) throws IOException
    {
	DataInputStream dis = new DataInputStream(in);

	// Reading bunny serial number
	int serialBytesLength = dis.readInt();
	byte[] serialBytes = new byte[serialBytesLength];
	dis.readFully(serialBytes);
	String serialNumber = new String(serialBytes, "US-ASCII");

	Bunny bunny = new Bunny(serialNumber);

	// Reading bunny name
	int nameBytesLength = dis.readInt();
	byte[] nameBytes = new byte[nameBytesLength];
	dis.readFully(nameBytes);
	bunny.setName(new String(nameBytes, "US-ASCII"));

	// Reading ping interval
	bunny.setPingInterval(dis.readInt());

	// Reading plugins count
	int pluginCount = dis.readInt();

	for (int i = 0; i < pluginCount; i++)
	{
	    // Reading plugin
	    AbstractPlugin plugin = null;
	    try
	    {
		plugin = this.readPluginFromInputStream(dis, pluginFactory);
	    }
	    catch (PluginCreationException e)
	    {
		continue;
	    }

	    // Adding plugin
	    plugin.setBunny(bunny);
	    bunny.addPlugin(plugin);
	}

	return bunny;
    }

    /**
     * Serializing a bunny to an output stream.
     * 
     * @param out the stream where to write the serialized data.
     * @param bunny the bunny to serialize.
     * @throws IOException if writing failed.
     */
    public void writeBunnyToOutputStream(OutputStream out, Bunny bunny) throws IOException
    {
	DataOutputStream dos = new DataOutputStream(out);

	// Saving bunny serial number
	byte[] serialBytes = bunny.getSerialNumber().getBytes("US-ASCII");
	dos.writeInt(serialBytes.length);
	dos.write(serialBytes);

	// Saving bunny name
	byte[] nameBytes = bunny.getName().getBytes("US-ASCII");
	dos.writeInt(nameBytes.length);
	dos.write(nameBytes);

	// Saving ping interval
	dos.writeInt(bunny.getPingInterval());

	// Saving plugins ...

	// Writing plugins count
	dos.writeInt(bunny.getPlugins().size());

	for (AbstractPlugin plugin : bunny.getPlugins())
	{
	    // Writing plugin
	    this.writePluginToOutputStream(dos, plugin);
	}
    }

    /**
     * Unserializing bunnies.
     * 
     * @param burrow the burrow where to store bunnies.
     * @param pluginFactory the pulign factory used to create plugins.
     */
    public void loadBunnies(Burrow burrow, PluginFactory pluginFactory)
    {
	for (File f : new File(this.serializedFilesPath, "bunnies").listFiles(new SerFileNameFilter()))
	{
	    try
	    {
		FileInputStream fis = new FileInputStream(f);
		Bunny bunny = this.readBunnyFromInputStream(fis, pluginFactory);
		try
		{
		    fis.close();
		}
		catch (IOException e)
		{}
		burrow.addBunny(bunny);
	    }
	    catch (Exception e)
	    {
		// Exceptions are ignored, only valid files lead to add bunnies to burrow
	    }
	}
    }

    /**
     * Serializing a choreography to an output stream.
     * 
     * @param out the output stream where to write serialized data.
     * @param choreography the choreography to write to the output stream.
     * @throws IOException if saving failed.
     */
    protected void writeChoreographyToOutputStream(OutputStream out, Choreography choreography) throws IOException
    {
	// Retrieving choreography data
	byte[] choreographyData = choreography.getData();

	DataOutputStream dos = new DataOutputStream(out);

	// Writing header
	// N.B. the header consists in a 0x00 value follow by the 3 least significant bytes of the
	// choregraphy data size integer value. Since the data size is supposed to fit in 3 bytes, writing the
	// serialized int value gives the same result
	dos.writeInt(choreographyData.length);

	// Writing data
	dos.write(choreographyData);

	// Writing footer
	dos.writeInt(0);

	// Flushing stream
	dos.flush();

	// Closing DataOutputStream instance
	try
	{
	    dos.close();
	}
	catch (IOException e)
	{}
    }

    /**
     * Unserializing a choreography from an input stream.
     * 
     * @param in the input stream where to read serialized data.
     * @param name the name of the choreography.
     * @return the loaded choreography.
     * @throws IOException if serialized data is corrupted.
     */
    protected Choreography readChoreographyFromInputStream(InputStream in, String name) throws IOException
    {
	// Buffering data
	ByteArrayOutputStream bos = new ByteArrayOutputStream();
	while (true)
	{
	    int available = in.available();
	    if (available > 0)
	    {
		byte[] buffer = new byte[available];
		int amountRead = in.read(buffer);
		bos.write(buffer);
		if (amountRead != -1) continue;
	    }
	    break;
	}

	byte[] fileData = bos.toByteArray();
	ByteArrayInputStream bis = new ByteArrayInputStream(fileData);
	DataInputStream dis = new DataInputStream(bis);

	// Checking the first byte
	if (fileData[0] != 0) throw new IOException("Invalid chor file, magic number missing");

	// Checking choreography length
	int dataLength = dis.readInt();
	if (fileData.length - 8 != dataLength)
	{
	    throw new IOException("Invalid chor file, less data than expected");
	}
	try
	{
	    bis.close();
	    bos.close();
	}
	catch (IOException e)
	{}

	// Creating a new choreography instance and filling it with data

	Choreography choreography = new Choreography(name);
	// Removing header and footer
	byte[] choreographyData = new byte[dataLength];
	for (int i = 0; i < dataLength; i++)
	    choreographyData[i] = fileData[i + 4];
	choreography.setData(choreographyData);

	return choreography;
    }

    /**
     * Restoring data for all choreographies handled by the server.
     * 
     * @param choreographyLibrary the choreography library where to store loaded choreographies.
     * 
     */
    public void loadChoreographies(ChoreographyLibrary choreographyLibrary)
    {
	for (File f : new File(this.serializedFilesPath, "choreographies").listFiles(new ChorFileNameFilter()))
	{
	    // Reading choreography file
	    String choreographyName = f.getName().substring(0, f.getName().lastIndexOf('.'));
	    Choreography c = null;
	    try
	    {
		FileInputStream fis = new FileInputStream(f);
		c = this.readChoreographyFromInputStream(fis, choreographyName);
		try
		{
		    fis.close();
		}
		catch (IOException e)
		{}
	    }
	    catch (Exception e)
	    {
		// Exceptions are ignored, only valid files lead to register choreographies to manager
	    }

	    // Registering choreography
	    choreographyLibrary.registerChoreography(c);
	}
    }

    /**
     * Saving data for a given choreography.
     * 
     * @param choreography the choreography to save.
     * @throws IOException if an IO exception occured during serialization or if resources path is not a valid directory.
     */
    public void saveChoreography(Choreography choreography) throws IOException
    {
	File file = new File(new File(this.serializedFilesPath, "choreographies"), choreography.getName() + ".chor");

	FileOutputStream fos = new FileOutputStream(file);
	this.writeChoreographyToOutputStream(fos, choreography);
	try
	{
	    fos.close();
	}
	catch (IOException e)
	{}
    }

    /**
     * Unserializing a plugin from an input stream.
     * 
     * @param in the input stream where to read serialized data.
     * @param pluginFactory the pulign factory used to create plugins.
     * @return the plugin loaded from the input stream.
     * @throws IOException if a read failure occurs.
     * @throws PluginCreationException if the plugin could not be created correctly.
     */
    public AbstractPlugin readPluginFromInputStream(InputStream in, PluginFactory pluginFactory) throws IOException,
	    PluginCreationException
    {
	DataInputStream dis = new DataInputStream(in);

	// Reading plugin name
	int pluginNameBytesLength = dis.readInt();
	byte[] pluginNameBytes = new byte[pluginNameBytesLength];
	dis.readFully(pluginNameBytes);

	String pluginName = new String(pluginNameBytes, "US-ASCII");
	Map<String, Couple<Boolean, String>> parameters = new HashMap<String, Couple<Boolean, String>>();

	// Reading parameters count
	int parametersCount = dis.readInt();
	for (int i = 0; i < parametersCount; i++)
	{
	    // Reading parameter name
	    int parameterNameBytesLength = dis.readInt();
	    byte[] parameterNameBytes = new byte[parameterNameBytesLength];
	    dis.readFully(parameterNameBytes);
	    String parameterName = new String(parameterNameBytes, "US-ASCII");

	    // Reading parameter setting indicator
	    boolean isParameterSet = dis.readBoolean();

	    // Reading parameter value
	    String parameterValue = null;
	    if (isParameterSet)
	    {
		int parameterValueBytesLength = dis.readInt();
		byte[] parameterValueBytes = new byte[parameterValueBytesLength];
		dis.readFully(parameterValueBytes);
		parameterValue = new String(parameterValueBytes, "US-ASCII");
	    }

	    // Adding parameter to temp map
	    parameters.put(parameterName, new Couple<Boolean, String>(isParameterSet, parameterValue));
	}

	// Creating a plugin instance
	AbstractPlugin plugin = pluginFactory.createPlugin(pluginName);
	plugin.setParameters(parameters);

	return plugin;
    }

    /**
     * Serializing the plugin to an output stream.
     * 
     * @param out the output stream where to write serialized data.
     * @param plugin the plugin to serialize.
     * @throws IOException if a write failure occurs.
     */
    public void writePluginToOutputStream(OutputStream out, AbstractPlugin plugin) throws IOException
    {
	DataOutputStream dos = new DataOutputStream(out);

	// Saving plugin name
	byte[] pluginNameBytes = plugin.getName().getBytes("US-ASCII");
	dos.writeInt(pluginNameBytes.length);
	dos.write(pluginNameBytes);

	// Saving parameters count
	int parametersCount = plugin.getParameters().size();
	dos.writeInt(parametersCount);

	// Saving parameters
	for (Map.Entry<String, Couple<Boolean, String>> parameterEntry : plugin.getParameters().entrySet())
	{
	    // Saving parameter name
	    byte[] parameterNameBytes = parameterEntry.getKey().getBytes("US-ASCII");
	    dos.writeInt(parameterNameBytes.length);
	    dos.write(parameterNameBytes);

	    // Saving parameter setting indicator

	    boolean isParameterSet = parameterEntry.getValue().getFirstElement();
	    dos.writeBoolean(isParameterSet);

	    // Saving parameter value
	    if (isParameterSet)
	    {
		byte[] parameterValueBytes = parameterEntry.getValue().getSecondElement().getBytes("US-ASCII");
		dos.writeInt(parameterValueBytes.length);
		dos.write(parameterValueBytes);
	    }
	}
    }
}

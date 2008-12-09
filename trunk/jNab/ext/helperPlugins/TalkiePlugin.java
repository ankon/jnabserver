package jNab.ext.helperPlugins;

import jNab.core.events.RecordEventListener;
import jNab.core.exceptions.NoSuchBunnyException;
import jNab.core.plugins.AbstractPlugin;
import jNab.core.protocol.MessageBlock;
import jNab.core.protocol.Packet;
import jNab.core.protocol.PingIntervalBlock;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Plugin echoing on another bunny what has been recorded on the bunny to which this plugin is attached.
 * 
 * 
 * @author Sylvain Gizard
 * @author Sebastien Jean
 */
public class TalkiePlugin extends AbstractPlugin implements RecordEventListener
{
    /**
     * Plugin name.
     */
    private static String PLUGIN_NAME = "Talkie_Plugin";

    /**
     * Set of parameters supported by the plugin;
     */
    private static String[] PARAMETERS = { "receiver" };

    /**
     * Creating a new Talkie plugin instance.
     */
    public TalkiePlugin()
    {
	super(PLUGIN_NAME, PARAMETERS);
    }

    /**
     * Echoing the recorded message on the buddy bunny.
     * 
     * @see RecordEventListener#onSimpleRecord(byte[])
     */
    public void onSimpleRecord(byte[] data)
    {
	String receiver = this.getParameterValue("receiver");
	if (receiver == null) return;

	// Saving the recorded audio
	try
	{
	    FileOutputStream fos = new FileOutputStream("message" + receiver + ".wav");
	    for (int element : data)
		fos.write(element);

	    fos.close();
	}
	catch (IOException e)
	{
	    return;
	}

	// Sending the message to the buddy bunny
	try
	{
	    MessageBlock mb = new MessageBlock(600);
	    mb.addPlayLocalSoundCommand("message" + receiver + ".wav");
	    mb.addWaitPreviousEndCommand();
	    Packet p = new Packet();
	    p.addBlock(mb);
	    p.addBlock(new PingIntervalBlock(1));

	    this.bunny.getBurrow().getBunny(receiver).addPacket(p);
	}
	catch (NoSuchBunnyException e)
	{
	    return;
	}
    }

    /**
     * @see jNab.core.events.RecordEventListener#onDoubleRecord(byte[])
     */
    public void onDoubleRecord(byte[] data)
    {}
}
